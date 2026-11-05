import { Component, computed, inject, input, OnInit, output, signal } from '@angular/core';
import { CurrencyPipe, DatePipe } from '@angular/common';
import { forkJoin } from 'rxjs';
import { origenDeConcepto, ConceptoCobroModel } from '../../../models/concepto-cobro.model';
import {
  DetalleFacturaModel,
  MAX_VALOR_LINEA,
  NuevaLineaFactura,
} from '../../../models/detalle-factura.model';
import {
  ESTILO_FACTURA,
  ETIQUETA_FACTURA,
  FacturaModel,
  facturaAbierta,
} from '../../../models/factura.model';
import { nombreCasa } from '../../../models/casa.model';
import { SancionModel, textoTipoSancion } from '../../../models/sancion.model';
import { ReservaSalonModel } from '../../../models/reserva-salon.model';
import { AsignacionCeldaModel } from '../../../models/asignacion-celda.model';
import { DetalleFacturaService } from '../../../services/detalle-factura.service';
import { ConceptoCobroService } from '../../../services/concepto-cobro.service';
import { SancionService } from '../../../services/sancion.service';
import { ReservaSalonService } from '../../../services/reserva-salon.service';
import { AsignacionCeldaService } from '../../../services/asignacion-celda.service';
import { NotificacionService } from '../../../services/notificacion.service';
import { extraerError } from '../../../compartido/extraer-error';
import { ModalFormulario } from '../../../compartido/modal-formulario/modal-formulario';
import { DialogoConfirmar } from '../../../compartido/dialogo-confirmar/dialogo-confirmar';

/** Campos del formulario para agregar una línea (todo como texto). */
interface FormularioLinea {
  idConcepto: string;
  idOrigen: string; // id de la sanción, reserva o asignación elegida
  valor: string; // solo en líneas libres
}

const FORMULARIO_VACIO: FormularioLinea = { idConcepto: '', idOrigen: '', valor: '' };

/**
 * @component DetalleFacturaModal
 * @description Ventana con las líneas de una factura: resumen (total, pagado, saldo), tabla de
 * líneas y formulario para agregar más. Mientras la factura esté PENDIENTE o ABONADA se pueden
 * agregar, editar (solo las libres) y eliminar líneas. Si es PAGADA o VENCIDA solo se consulta.
 * Cada vez que cambia algo avisa con `cambio` para que Finanzas recargue los totales.
 */
@Component({
  selector: 'app-detalle-factura-modal',
  imports: [CurrencyPipe, DatePipe, ModalFormulario, DialogoConfirmar],
  templateUrl: './detalle-factura-modal.html',
  styleUrl: './detalle-factura-modal.css',
})
export class DetalleFacturaModal implements OnInit {
  private detalleService = inject(DetalleFacturaService);
  private conceptoService = inject(ConceptoCobroService);
  private sancionService = inject(SancionService);
  private reservaService = inject(ReservaSalonService);
  private asignacionService = inject(AsignacionCeldaService);
  private notificacionService = inject(NotificacionService);

  /** Factura que se muestra (Finanzas la renueva cuando se recargan las facturas). */
  readonly factura = input.required<FacturaModel>();

  /** Avisa que se agregó, editó o eliminó una línea. */
  readonly cambio = output<void>();

  /** Avisa que el usuario cerró la ventana. */
  readonly cerrar = output<void>();

  protected readonly detalles = signal<DetalleFacturaModel[]>([]);
  protected readonly conceptos = signal<ConceptoCobroModel[]>([]);
  private readonly sanciones = signal<SancionModel[]>([]);
  private readonly reservas = signal<ReservaSalonModel[]>([]);
  private readonly asignaciones = signal<AsignacionCeldaModel[]>([]);

  protected readonly cargando = signal(true);
  protected readonly errorCarga = signal(false);

  /** true si todavía se pueden cambiar las líneas. */
  protected readonly abierta = computed(() => facturaAbierta(this.factura()));

  /** Líneas de esta factura, en orden. */
  protected readonly lineas = computed(() =>
    this.detalles()
      .filter((d) => d.numeroFactura === this.factura().numeroFactura)
      .sort((a, b) => a.numeroLinea - b.numeroLinea),
  );

  /** Sanciones de la casa que se pueden cobrar: confirmadas, con multa y sin cobrar aún. */
  protected readonly sancionesLibres = computed(() => {
    const f = this.factura();
    const usadas = new Set(this.detalles().map((d) => d.idSancion));
    return this.sanciones().filter(
      (s) =>
        s.casaNumero === f.casaNumero &&
        s.casaManzana === f.casaManzana &&
        s.estadoProceso === 'CONFIRMADA' &&
        (s.valor ?? 0) > 0 &&
        !usadas.has(s.idSancion),
    );
  });

  /** Reservas de la casa que se pueden cobrar: no canceladas y sin cobrar aún. */
  protected readonly reservasLibres = computed(() => {
    const f = this.factura();
    const usadas = new Set(this.detalles().map((d) => d.idReserva));
    return this.reservas().filter(
      (r) =>
        r.casaNumero === f.casaNumero &&
        r.casaManzana === f.casaManzana &&
        r.estado !== 'CANCELADA' &&
        !usadas.has(r.idReserva),
    );
  });

  /** Celdas adicionales de la casa que aún no están cobradas en ESTA factura. */
  protected readonly asignacionesLibres = computed(() => {
    const f = this.factura();
    const enEstaFactura = new Set(this.lineas().map((l) => l.idAsignacion));
    return this.asignaciones().filter(
      (a) =>
        a.casaNumero === f.casaNumero &&
        a.casaManzana === f.casaManzana &&
        !enEstaFactura.has(a.idAsignacion),
    );
  });

  /** Formulario de agregar línea. */
  protected readonly formulario = signal<FormularioLinea>({ ...FORMULARIO_VACIO });
  protected readonly errorFormulario = signal<string | null>(null);
  protected readonly guardando = signal(false);

  protected readonly conceptoElegido = computed(
    () => this.conceptos().find((c) => c.idConcepto === Number(this.formulario().idConcepto)) ?? null,
  );

  protected readonly origenElegido = computed(() => {
    const c = this.conceptoElegido();
    return c ? origenDeConcepto(c.nombre) : null;
  });

  /** Valor que se cobrará cuando el origen manda (sanción, reserva o celda). */
  protected readonly valorPrevio = computed<number | null>(() => {
    const c = this.conceptoElegido();
    const origen = this.origenElegido();
    const id = Number(this.formulario().idOrigen);
    if (!c || !id) return null;
    if (origen === 'SANCION') return this.sancionesLibres().find((s) => s.idSancion === id)?.valor ?? null;
    if (origen === 'RESERVA') return this.reservasLibres().find((r) => r.idReserva === id)?.valorTotal ?? null;
    if (origen === 'ASIGNACION') return c.valorBase;
    return null;
  });

  /** Edición del valor de una línea libre. */
  protected readonly lineaEditando = signal<number | null>(null);
  protected readonly valorEdicion = signal('');

  /** Línea pendiente de confirmar para eliminar. */
  protected readonly lineaAEliminar = signal<DetalleFacturaModel | null>(null);
  protected readonly eliminando = signal(false);

  /** Constantes y funciones para la plantilla. */
  protected readonly etiquetaFactura = ETIQUETA_FACTURA;
  protected readonly estiloFactura = ESTILO_FACTURA;
  protected readonly nombreCasa = nombreCasa;
  protected readonly textoTipo = textoTipoSancion;

  ngOnInit(): void {
    this.cargar(false);
  }

  /**
   * Consulta líneas, conceptos y los posibles orígenes.
   * @param silencioso true = no mostrar el esqueleto (se usa al recargar tras un cambio).
   */
  private cargar(silencioso: boolean): void {
    if (!silencioso) {
      this.cargando.set(true);
      this.errorCarga.set(false);
    }
    forkJoin({
      detalles: this.detalleService.getTodos(),
      conceptos: this.conceptoService.getTodos(),
      sanciones: this.sancionService.getTodos(),
      reservas: this.reservaService.getTodos(),
      asignaciones: this.asignacionService.getTodos(),
    }).subscribe({
      next: (r) => {
        this.detalles.set(r.detalles);
        this.conceptos.set(r.conceptos);
        this.sanciones.set(r.sanciones);
        this.reservas.set(r.reservas);
        this.asignaciones.set(r.asignaciones);
        this.cargando.set(false);
      },
      error: (err) => {
        this.errorCarga.set(true);
        this.cargando.set(false);
        this.notificacionService.error(
          extraerError(err, 'No se pudo consultar el detalle de la factura.'),
          'Error de conexión',
        );
      },
    });
  }

  reintentar(): void {
    this.cargar(false);
  }

  /** Nombre del concepto de una línea. */
  nombreConcepto(id: number): string {
    return this.conceptos().find((c) => c.idConcepto === id)?.nombre ?? `Concepto ${id}`;
  }

  /** "Reserva #4", "Sanción #2", "Celda adicional #7" o "—". */
  textoOrigen(l: DetalleFacturaModel): string {
    if (l.idReserva !== null) return `Reserva #${l.idReserva}`;
    if (l.idSancion !== null) return `Sanción #${l.idSancion}`;
    if (l.idAsignacion !== null) return `Celda adicional #${l.idAsignacion}`;
    return '—';
  }

  /** true si la línea no está ligada a ningún origen (su valor se puede editar). */
  esLibre(l: DetalleFacturaModel): boolean {
    return l.idReserva === null && l.idSancion === null && l.idAsignacion === null;
  }

  /** Cambia el concepto; si es libre, propone su valor base. */
  cambiarConcepto(valor: string): void {
    const c = this.conceptos().find((x) => x.idConcepto === Number(valor));
    const valorInicial = c && origenDeConcepto(c.nombre) === 'LIBRE' ? String(c.valorBase) : '';
    this.formulario.set({ idConcepto: valor, idOrigen: '', valor: valorInicial });
    this.errorFormulario.set(null);
  }

  cambiarCampo(campo: 'idOrigen' | 'valor', valor: string): void {
    this.formulario.update((f) => ({ ...f, [campo]: valor }));
  }

  /** Valida lo mismo que el back y agrega la línea. También se dispara con Enter. */
  agregarLinea(): void {
    if (!this.abierta() || this.guardando()) return;

    const f = this.formulario();
    const concepto = this.conceptoElegido();
    if (!concepto) {
      this.errorFormulario.set('Elige el concepto que se cobra.');
      return;
    }

    const nueva: NuevaLineaFactura = {
      numeroFactura: this.factura().numeroFactura,
      idConcepto: concepto.idConcepto,
    };

    switch (this.origenElegido()) {
      case 'SANCION':
        if (!f.idOrigen) {
          this.errorFormulario.set('Elige la sanción que se cobra.');
          return;
        }
        nueva.idSancion = Number(f.idOrigen);
        break;
      case 'RESERVA':
        if (!f.idOrigen) {
          this.errorFormulario.set('Elige la reserva que se cobra.');
          return;
        }
        nueva.idReserva = Number(f.idOrigen);
        break;
      case 'ASIGNACION':
        if (!f.idOrigen) {
          this.errorFormulario.set('Elige la celda adicional que se cobra.');
          return;
        }
        nueva.idAsignacion = Number(f.idOrigen);
        break;
      default: {
        const texto = f.valor.trim();
        const valor = Number(texto);
        // Solo dígitos: así se evita que "150.000" se lea como 150.
        if (!/^\d+$/.test(texto) || valor <= 0) {
          this.errorFormulario.set(
            'El valor debe ser un número entero mayor a 0 (sin puntos ni comas).',
          );
          return;
        }
        if (valor > MAX_VALOR_LINEA) {
          this.errorFormulario.set('El valor es demasiado grande.');
          return;
        }
        nueva.valor = valor;
      }
    }

    this.errorFormulario.set(null);
    this.guardando.set(true);
    this.detalleService.crear(nueva).subscribe({
      next: () => {
        this.guardando.set(false);
        this.formulario.set({ ...FORMULARIO_VACIO });
        this.notificacionService.exito('Línea agregada.', 'Listo');
        this.cargar(true);
        this.cambio.emit();
      },
      error: (err) => {
        this.guardando.set(false);
        this.errorFormulario.set(extraerError(err, 'No se pudo agregar la línea.'));
      },
    });
  }

  /** Pone la fila en modo edición. */
  iniciarEdicion(l: DetalleFacturaModel): void {
    this.valorEdicion.set(String(l.valor));
    this.lineaEditando.set(l.numeroLinea);
  }

  cancelarEdicion(): void {
    this.lineaEditando.set(null);
  }

  /** Guarda el nuevo valor de una línea libre. */
  guardarEdicion(l: DetalleFacturaModel): void {
    const texto = this.valorEdicion().trim();
    const valor = Number(texto);
    if (!/^\d+$/.test(texto) || valor <= 0 || valor > MAX_VALOR_LINEA) {
      this.notificacionService.error(
        'El valor debe ser un número entero mayor a 0 (sin puntos ni comas).',
        'Valor no válido',
      );
      return;
    }
    this.detalleService.actualizarValor(l.numeroFactura, l.numeroLinea, valor).subscribe({
      next: () => {
        this.lineaEditando.set(null);
        this.notificacionService.exito('Línea actualizada.', 'Listo');
        this.cargar(true);
        this.cambio.emit();
      },
      error: (err) =>
        // Por ejemplo: el total quedaría por debajo de lo ya pagado (409).
        this.notificacionService.error(
          extraerError(err, 'No se pudo actualizar la línea.'),
          'No se pudo actualizar',
        ),
    });
  }

  mensajeEliminar(l: DetalleFacturaModel): string {
    return (
      `Se eliminará la línea ${l.numeroLinea} («${this.nombreConcepto(l.idConcepto)}») de la factura ` +
      `N.º ${l.numeroFactura}. Si estaba ligada a una sanción, reserva o celda, volverá a poder cobrarse.`
    );
  }

  confirmarEliminar(): void {
    const l = this.lineaAEliminar();
    if (!l) return;
    this.eliminando.set(true);
    this.detalleService.eliminar(l.numeroFactura, l.numeroLinea).subscribe({
      next: () => {
        this.eliminando.set(false);
        this.lineaAEliminar.set(null);
        this.notificacionService.exito('Línea eliminada.', 'Listo');
        this.cargar(true);
        this.cambio.emit();
      },
      error: (err) => {
        this.eliminando.set(false);
        this.lineaAEliminar.set(null);
        this.notificacionService.error(
          extraerError(err, 'No se pudo eliminar la línea.'),
          'No se pudo eliminar',
        );
      },
    });
  }
}
