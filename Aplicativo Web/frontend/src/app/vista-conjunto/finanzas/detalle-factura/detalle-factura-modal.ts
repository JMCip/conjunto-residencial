import { Component, computed, inject, input, OnInit, output, signal } from '@angular/core';
import { CurrencyPipe, DatePipe } from '@angular/common';
import { concatMap, forkJoin, from, toArray } from 'rxjs';
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
import { CeldaParqueaderoModel } from '../../../models/celda-parqueadero.model';
import { DetalleFacturaService } from '../../../services/detalle-factura.service';
import { ConceptoCobroService } from '../../../services/concepto-cobro.service';
import { SancionService } from '../../../services/sancion.service';
import { ReservaSalonService } from '../../../services/reserva-salon.service';
import { AsignacionCeldaService } from '../../../services/asignacion-celda.service';
import { CeldaParqueaderoService } from '../../../services/celda-parqueadero.service';
import { NotificacionService } from '../../../services/notificacion.service';
import { extraerError } from '../../../compartido/extraer-error';
import { ModalFormulario } from '../../../compartido/modal-formulario/modal-formulario';
import { DialogoConfirmar } from '../../../compartido/dialogo-confirmar/dialogo-confirmar';

/** Campos del formulario para agregar una línea libre (todo como texto). */
interface FormularioLinea {
  idConcepto: string;
  valor: string;
}

/** Valores iniciales del formulario (todo vacío). */
const FORMULARIO_VACIO: FormularioLinea = { idConcepto: '', valor: '' };

/**
 * Cargo que la casa tiene pendiente y que esta factura puede recoger:
 * la administración del mes, una sanción, una reserva o una celda adicional.
 */
interface CargoPendiente {
  /** Llave única para el @for y para saber cuál se está agregando ("SANCION-3"). */
  clave: string;
  /** Ícono de Font Awesome según el tipo. */
  icono: string;
  /** "Administración", "Sanción #3", "Reserva #4", "Celda adicional #12". */
  titulo: string;
  /** Texto corto con el detalle ("Ruido · confirmada", "12/11/2026 · 3 h"). */
  detalle: string;
  valor: number;
  /** Lo que se envía al backend para crear la línea. */
  linea: NuevaLineaFactura;
}

/**
 * @component DetalleFacturaModal
 * @description Ventana con las líneas de una factura: resumen (total, pagado, saldo), tabla de
 * líneas, los cargos pendientes de la casa y un formulario para cobros libres.
 * Los cargos con origen (sanción, reserva, celda adicional) nacen en su propio módulo: aquí solo
 * se recogen desde "Cargos pendientes", uno por uno o todos a la vez. El formulario de abajo
 * queda para los cobros libres (intereses de mora, cuotas extraordinarias…).
 * Mientras la factura esté PENDIENTE o ABONADA se pueden agregar, editar (solo las libres) y
 * eliminar líneas. Si es PAGADA o VENCIDA solo se consulta.
 * Cada vez que cambia algo avisa con `cambio` para que Finanzas recargue los totales.
 */
@Component({
  selector: 'app-detalle-factura-modal',
  imports: [CurrencyPipe, DatePipe, ModalFormulario, DialogoConfirmar],
  templateUrl: './detalle-factura-modal.html',
  styleUrl: './detalle-factura-modal.css',
})
export class DetalleFacturaModal implements OnInit {
  /** Servicio de líneas de factura. */
  private detalleService = inject(DetalleFacturaService);
  /** Servicio del catálogo de conceptos de cobro. */
  private conceptoService = inject(ConceptoCobroService);
  /** Servicio de sanciones. */
  private sancionService = inject(SancionService);
  /** Servicio de reservas del salón comunal. */
  private reservaService = inject(ReservaSalonService);
  /** Servicio de asignaciones de celdas. */
  private asignacionService = inject(AsignacionCeldaService);
  /** Servicio de celdas del parqueadero. */
  private celdaService = inject(CeldaParqueaderoService);
  /** Servicio de avisos flotantes. */
  private notificacionService = inject(NotificacionService);

  /** Factura que se muestra (Finanzas la renueva cuando se recargan las facturas). */
  readonly factura = input.required<FacturaModel>();

  /** Avisa que se agregó, editó o eliminó una línea. */
  readonly cambio = output<void>();

  /** Avisa que el usuario cerró la ventana. */
  readonly cerrar = output<void>();

  /** Lista de detalles consultada al backend. */
  protected readonly detalles = signal<DetalleFacturaModel[]>([]);
  /** Lista de conceptos consultada al backend. */
  protected readonly conceptos = signal<ConceptoCobroModel[]>([]);

  /** Sanciones de todas las casas (posibles orígenes de líneas). */
  private readonly sanciones = signal<SancionModel[]>([]);

  /** Reservas de todas las casas (posibles orígenes de líneas). */
  private readonly reservas = signal<ReservaSalonModel[]>([]);

  /** Asignaciones de celdas de todas las casas (posibles orígenes de líneas). */
  private readonly asignaciones = signal<AsignacionCeldaModel[]>([]);

  /** Celdas del parqueadero, para distinguir las adicionales de las propias. */
  private readonly celdas = signal<CeldaParqueaderoModel[]>([]);

  /** true mientras se consulta la información. */
  protected readonly cargando = signal(true);
  /** true si la consulta de la información falló. */
  protected readonly errorCarga = signal(false);

  /** true si todavía se pueden cambiar las líneas. */
  protected readonly abierta = computed(() => facturaAbierta(this.factura()));

  /** Líneas de esta factura, en orden. */
  protected readonly lineas = computed(() =>
    this.detalles()
      .filter((d) => d.numeroFactura === this.factura().numeroFactura)
      .sort((a, b) => a.numeroLinea - b.numeroLinea),
  );

  /** Conceptos que se cobran a mano (sin origen): Administración, mora, cuotas… */
  protected readonly conceptosLibres = computed(() =>
    this.conceptos().filter((c) => origenDeConcepto(c.nombre) === 'LIBRE'),
  );

  /** Concepto del catálogo por nombre (sin importar mayúsculas), o null. */
  private concepto(nombre: string): ConceptoCobroModel | null {
    return this.conceptos().find((c) => c.nombre.trim().toLowerCase() === nombre) ?? null;
  }

  /** Sanciones de la casa que se pueden cobrar: confirmadas, con multa y sin cobrar aún. */
  private readonly sancionesLibres = computed(() => {
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
  private readonly reservasLibres = computed(() => {
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

  /**
   * Celdas ADICIONALES de la casa vigentes en el periodo de la factura y que aún no están
   * cobradas en ESTA factura. Las celdas propias no se cobran.
   */
  private readonly asignacionesLibres = computed(() => {
    const f = this.factura();
    const adicionales = new Set(
      this.celdas()
        .filter((c) => c.tipo === 'ADICIONAL')
        .map((c) => c.idCelda),
    );
    const enEstaFactura = new Set(this.lineas().map((l) => l.idAsignacion));
    return this.asignaciones().filter(
      (a) =>
        a.casaNumero === f.casaNumero &&
        a.casaManzana === f.casaManzana &&
        adicionales.has(a.idCelda) &&
        a.fechaInicio.slice(0, 7) <= f.periodo &&
        (a.fechaFin === null || a.fechaFin.slice(0, 7) >= f.periodo) &&
        !enEstaFactura.has(a.idAsignacion),
    );
  });

  /**
   * Todo lo que esta factura puede recoger, en el orden en que se agregaría:
   * 1. La administración del periodo, si la factura todavía no la tiene.
   * 2. Las sanciones confirmadas con multa.
   * 3. Las reservas del salón (horas por la tarifa guardada en la reserva).
   * 4. Las celdas adicionales vigentes (cuota mensual del concepto).
   */
  protected readonly cargosPendientes = computed<CargoPendiente[]>(() => {
    const numeroFactura = this.factura().numeroFactura;
    const cargos: CargoPendiente[] = [];

    const admin = this.concepto('administración');
    if (admin && !this.lineas().some((l) => l.idConcepto === admin.idConcepto)) {
      cargos.push({
        clave: 'ADMIN',
        icono: 'fa-building',
        titulo: 'Administración',
        detalle: `Cuota del periodo ${this.factura().periodo}`,
        valor: admin.valorBase,
        linea: { numeroFactura, idConcepto: admin.idConcepto, valor: admin.valorBase },
      });
    }

    const sancion = this.concepto('sanción');
    if (sancion) {
      for (const s of this.sancionesLibres()) {
        cargos.push({
          clave: `SANCION-${s.idSancion}`,
          icono: 'fa-gavel',
          titulo: `Sanción #${s.idSancion}`,
          detalle: textoTipoSancion(s.tipo),
          valor: s.valor ?? 0,
          linea: { numeroFactura, idConcepto: sancion.idConcepto, idSancion: s.idSancion },
        });
      }
    }

    const salon = this.concepto('salón comunal');
    if (salon) {
      for (const r of this.reservasLibres()) {
        cargos.push({
          clave: `RESERVA-${r.idReserva}`,
          icono: 'fa-calendar-check',
          titulo: `Reserva #${r.idReserva}`,
          detalle: `${r.fechaReserva} · ${r.horasReservadas.toLocaleString('es-CO')} h`,
          valor: r.valorTotal,
          linea: { numeroFactura, idConcepto: salon.idConcepto, idReserva: r.idReserva },
        });
      }
    }

    const parqueadero = this.concepto('parqueadero adicional');
    if (parqueadero) {
      for (const a of this.asignacionesLibres()) {
        cargos.push({
          clave: `ASIGNACION-${a.idAsignacion}`,
          icono: 'fa-square-parking',
          titulo: `Celda adicional #${a.idCelda}`,
          detalle: `Asignación #${a.idAsignacion}`,
          valor: parqueadero.valorBase,
          linea: { numeroFactura, idConcepto: parqueadero.idConcepto, idAsignacion: a.idAsignacion },
        });
      }
    }

    return cargos;
  });

  /** Suma de los cargos pendientes (para el botón "Agregar todos"). */
  protected readonly totalPendientes = computed(() =>
    this.cargosPendientes().reduce((total, c) => total + c.valor, 0),
  );

  /** Clave del cargo que se está agregando ('TODOS' = todos a la vez; null = ninguno). */
  protected readonly agregandoCargo = signal<string | null>(null);

  /** Formulario de cobro libre. */
  protected readonly formulario = signal<FormularioLinea>({ ...FORMULARIO_VACIO });
  /** Mensaje de validación del formulario (null si no hay error). */
  protected readonly errorFormulario = signal<string | null>(null);
  /** true mientras se envía el formulario al backend. */
  protected readonly guardando = signal(false);

  /** Edición del valor de una línea libre. */
  protected readonly lineaEditando = signal<number | null>(null);

  /** Valor escrito en la fila que se está editando. */
  protected readonly valorEdicion = signal('');

  /** Línea pendiente de confirmar para eliminar. */
  protected readonly lineaAEliminar = signal<DetalleFacturaModel | null>(null);
  /** true mientras se envía la eliminación al backend. */
  protected readonly eliminando = signal(false);

  /** Texto de cada estado de factura (constante del modelo). */
  protected readonly etiquetaFactura = ETIQUETA_FACTURA;
  /** Color de insignia de cada estado de factura (constante del modelo). */
  protected readonly estiloFactura = ESTILO_FACTURA;
  /** Función del modelo para mostrar el nombre de una casa en la plantilla. */
  protected readonly nombreCasa = nombreCasa;

  /** Consulta los datos al abrir la pantalla. */
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
      celdas: this.celdaService.getTodos(),
    }).subscribe({
      next: (r) => {
        this.detalles.set(r.detalles);
        this.conceptos.set(r.conceptos);
        this.sanciones.set(r.sanciones);
        this.reservas.set(r.reservas);
        this.asignaciones.set(r.asignaciones);
        this.celdas.set(r.celdas);
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

  /** Vuelve a consultar el detalle después de un error. */
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

  /**
   * Agrega a la factura un cargo pendiente.
   * @param cargo Cargo de la lista "Cargos pendientes".
   */
  agregarCargo(cargo: CargoPendiente): void {
    if (this.agregandoCargo()) return;
    this.agregandoCargo.set(cargo.clave);
    this.detalleService.crear(cargo.linea).subscribe({
      next: () => {
        this.agregandoCargo.set(null);
        this.notificacionService.exito(`Se agregó «${cargo.titulo}» a la factura.`, 'Listo');
        this.cargar(true);
        this.cambio.emit();
      },
      error: (err) => {
        this.agregandoCargo.set(null);
        this.notificacionService.error(
          extraerError(err, 'No se pudo agregar el cargo.'),
          'No se pudo agregar',
        );
      },
    });
  }

  /**
   * Agrega todos los cargos pendientes, uno detrás de otro (concatMap), para que el backend numere
   * las líneas en orden sin que dos peticiones choquen. Si uno falla, los anteriores quedan guardados
   * y se recarga el detalle para mostrar el estado real.
   */
  agregarTodos(): void {
    const cargos = this.cargosPendientes();
    if (this.agregandoCargo() || cargos.length === 0) return;
    this.agregandoCargo.set('TODOS');

    from(cargos)
      .pipe(
        concatMap((c) => this.detalleService.crear(c.linea)),
        toArray(),
      )
      .subscribe({
        next: (respuestas) => {
          this.agregandoCargo.set(null);
          this.notificacionService.exito(
            respuestas.length === 1
              ? 'Se agregó 1 cargo a la factura.'
              : `Se agregaron ${respuestas.length} cargos a la factura.`,
            'Listo',
          );
          this.cargar(true);
          this.cambio.emit();
        },
        error: (err) => {
          this.agregandoCargo.set(null);
          this.notificacionService.error(
            extraerError(err, 'No se pudieron agregar todos los cargos.'),
            'No se pudo completar',
          );
          this.cargar(true);
          this.cambio.emit();
        },
      });
  }

  /** Cambia el concepto del cobro libre y propone su valor base. */
  cambiarConcepto(valor: string): void {
    const c = this.conceptosLibres().find((x) => x.idConcepto === Number(valor));
    this.formulario.set({ idConcepto: valor, valor: c ? String(c.valorBase) : '' });
    this.errorFormulario.set(null);
  }

  /**
   * Actualiza el valor del cobro libre.
   * @param valor Texto escrito.
   */
  cambiarValor(valor: string): void {
    this.formulario.update((f) => ({ ...f, valor }));
  }

  /**
   * Valida lo mismo que el backend y agrega un cobro libre; también se dispara con Enter. El valor
   * solo acepta dígitos, para que "150.000" no se interprete como 150.
   */
  agregarLinea(): void {
    if (!this.abierta() || this.guardando()) return;

    const f = this.formulario();
    const concepto = this.conceptosLibres().find((c) => c.idConcepto === Number(f.idConcepto));
    if (!concepto) {
      this.errorFormulario.set('Elige el concepto que se cobra.');
      return;
    }
    if (
      concepto.nombre.trim().toLowerCase() === 'administración' &&
      this.lineas().some((linea) => linea.idConcepto === concepto.idConcepto)
    ) {
      this.errorFormulario.set('Esta factura ya tiene una línea de Administración.');
      return;
    }

    const texto = f.valor.trim();
    const valor = Number(texto);
    if (!/^\d+$/.test(texto) || valor <= 0) {
      this.errorFormulario.set('El valor debe ser un número entero mayor a 0 (sin puntos ni comas).');
      return;
    }
    if (valor > MAX_VALOR_LINEA) {
      this.errorFormulario.set('El valor es demasiado grande.');
      return;
    }

    this.errorFormulario.set(null);
    this.guardando.set(true);
    this.detalleService
      .crear({ numeroFactura: this.factura().numeroFactura, idConcepto: concepto.idConcepto, valor })
      .subscribe({
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

  /** Sale del modo edición sin guardar. */
  cancelarEdicion(): void {
    this.lineaEditando.set(null);
  }

  /**
   * Guarda el nuevo valor de una línea libre. El backend responde 409 si el total quedaría por
   * debajo de lo ya pagado.
   * @param l Línea que se edita.
   */
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
        this.notificacionService.error(
          extraerError(err, 'No se pudo actualizar la línea.'),
          'No se pudo actualizar',
        ),
    });
  }

  /**
   * Arma el texto del diálogo de eliminar una línea.
   * @param l Línea que se va a eliminar.
   * @returns Mensaje para el diálogo de confirmación.
   */
  mensajeEliminar(l: DetalleFacturaModel): string {
    return (
      `Se eliminará la línea ${l.numeroLinea} («${this.nombreConcepto(l.idConcepto)}») de la factura ` +
      `N.º ${l.numeroFactura}. Si estaba ligada a una sanción, reserva o celda, volverá a quedar ` +
      'en los cargos pendientes.'
    );
  }

  /** Elimina la línea confirmada en el diálogo y recarga el detalle. */
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
