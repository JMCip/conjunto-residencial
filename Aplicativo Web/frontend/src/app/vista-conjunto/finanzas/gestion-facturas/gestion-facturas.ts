import { Component, inject, OnInit, output, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { CasaModel, llaveCasa, nombreCasa, separarLlaveCasa } from '../../../models/casa.model';
import { FacturaModel } from '../../../models/factura.model';
import { CasaService } from '../../../services/casa.service';
import { FacturaService } from '../../../services/factura.service';
import { NotificacionService } from '../../../services/notificacion.service';
import { extraerError } from '../../../compartido/extraer-error';
import { hoyISO, sumarDias } from '../../../compartido/fechas';
import { ModalFormulario } from '../../../compartido/modal-formulario/modal-formulario';
import { DialogoConfirmar } from '../../../compartido/dialogo-confirmar/dialogo-confirmar';

/** Campos del formulario de crear (todo como texto). */
interface FormularioFactura {
  /** Llave de la casa elegida ("manzana|número"). */
  casa: string;

  /** Periodo facturado (yyyy-MM, valor de <input type="month">). */
  periodo: string;

  /** Fecha de emisión (yyyy-MM-dd). */
  fechaEmision: string;

  /** Fecha de vencimiento (yyyy-MM-dd). */
  fechaVencimiento: string;
}

/** Valores iniciales del formulario (todo vacío). */
const FORMULARIO_VACIO: FormularioFactura = {
  casa: '',
  periodo: '',
  fechaEmision: '',
  fechaVencimiento: '',
};

/** Días entre la emisión y el vencimiento que se proponen al crear. */
const DIAS_VENCIMIENTO = 15;

/**
 * @component GestionFacturas
 * @description Ventanas para crear una factura, cambiar su vencimiento, eliminarla y marcar las
 * vencidas. No dibuja ninguna tarjeta: Finanzas la usa con una referencia (#gestion) y le pide
 * abrir cada ventana desde la tabla o desde la cabecera.
 */
@Component({
  selector: 'app-gestion-facturas',
  imports: [DatePipe, ModalFormulario, DialogoConfirmar],
  templateUrl: './gestion-facturas.html',
  styleUrl: './gestion-facturas.css',
})
export class GestionFacturas implements OnInit {
  /** Servicio de facturas. */
  private facturaService = inject(FacturaService);
  /** Servicio de casas. */
  private casaService = inject(CasaService);
  /** Servicio de avisos flotantes. */
  private notificacionService = inject(NotificacionService);

  /** Avisa que algo cambió (vencimiento, eliminación, vencidas) para recargar las facturas. */
  readonly cambio = output<void>();

  /** Avisa que se creó una factura, con su número, para abrir su detalle. */
  readonly creada = output<number>();

  /** Lista de casas consultada al backend. */
  protected readonly casas = signal<CasaModel[]>([]);
  /** Función del modelo para mostrar el nombre de una casa en la plantilla. */
  protected readonly nombreCasa = nombreCasa;
  /** Función del modelo para armar la llave de una casa en la plantilla. */
  protected readonly llaveCasa = llaveCasa;

  /** Modal de crear. */
  protected readonly modalCrear = signal(false);
  /** Valores actuales del formulario del modal. */
  protected readonly formulario = signal<FormularioFactura>({ ...FORMULARIO_VACIO });

  /** Modal de cambiar vencimiento (null = cerrado). */
  protected readonly editando = signal<FacturaModel | null>(null);

  /** Fecha de vencimiento escrita en el modal de edición. */
  protected readonly vencimiento = signal('');

  /** Lo usan los dos modales (nunca hay dos abiertos a la vez). */
  protected readonly guardando = signal(false);
  /** Mensaje de validación del formulario (null si no hay error). */
  protected readonly errorFormulario = signal<string | null>(null);

  /** Factura pendiente de confirmar para eliminar. */
  protected readonly facturaAEliminar = signal<FacturaModel | null>(null);
  /** true mientras se envía la acción al backend. */
  protected readonly procesando = signal(false);

  /** Diálogo de "Marcar vencidas". */
  protected readonly confirmandoVencidas = signal(false);

  /** Consulta los datos al abrir la pantalla. */
  ngOnInit(): void {
    this.casaService.getTodos().subscribe({
      next: (casas) => this.casas.set(casas),
      error: (err) =>
        this.notificacionService.error(
          extraerError(err, 'No se pudieron consultar las casas.'),
          'Error de conexión',
        ),
    });
  }

  /** Abre el modal de crear con el mes y las fechas de hoy ya propuestos. */
  abrirCrear(): void {
    const hoy = hoyISO();
    this.formulario.set({
      casa: '',
      periodo: hoy.slice(0, 7),
      fechaEmision: hoy,
      fechaVencimiento: sumarDias(hoy, DIAS_VENCIMIENTO),
    });
    this.errorFormulario.set(null);
    this.modalCrear.set(true);
  }

  /**
   * Actualiza un campo del formulario de crear.
   * @param campo Nombre del campo.
   * @param valor Texto escrito.
   */
  cambiarCampo(campo: keyof FormularioFactura, valor: string): void {
    this.formulario.update((f) => ({ ...f, [campo]: valor }));
  }

  /**
   * Valida lo mismo que el backend y crea la factura. Si la casa ya tiene factura del periodo, el
   * backend responde 409 y el mensaje se muestra en el modal.
   */
  guardarCrear(): void {
    const f = this.formulario();

    if (!f.casa) {
      this.errorFormulario.set('Elige la casa a la que se factura.');
      return;
    }
    if (!/^\d{4}-(0[1-9]|1[0-2])$/.test(f.periodo)) {
      this.errorFormulario.set('El periodo debe tener formato año-mes, por ejemplo 2026-11.');
      return;
    }
    if (!f.fechaEmision) {
      this.errorFormulario.set('Indica la fecha de emisión.');
      return;
    }
    if (!f.fechaVencimiento) {
      this.errorFormulario.set('Indica la fecha de vencimiento.');
      return;
    }
    if (f.fechaVencimiento < f.fechaEmision) {
      this.errorFormulario.set('El vencimiento no puede ser anterior a la emisión.');
      return;
    }

    const { manzana, numero } = separarLlaveCasa(f.casa);
    this.errorFormulario.set(null);
    this.guardando.set(true);

    this.facturaService
      .crear({
        periodo: f.periodo,
        fechaEmision: f.fechaEmision,
        fechaVencimiento: f.fechaVencimiento,
        casaNumero: numero,
        casaManzana: manzana,
      })
      .subscribe({
        next: (respuesta) => {
          this.guardando.set(false);
          this.modalCrear.set(false);
          this.notificacionService.exito('Factura creada. Ahora agrégale sus líneas.', 'Listo');
          this.creada.emit(respuesta.numeroFactura);
        },
        error: (err) => {
          this.guardando.set(false);
          this.errorFormulario.set(extraerError(err, 'No se pudo crear la factura.'));
        },
      });
  }

  /** Abre el modal para cambiar el vencimiento de una factura PENDIENTE o ABONADA. */
  abrirEditar(f: FacturaModel): void {
    this.vencimiento.set(f.fechaVencimiento);
    this.errorFormulario.set(null);
    this.editando.set(f);
  }

  /** Valida y guarda la nueva fecha de vencimiento. */
  guardarEditar(): void {
    const f = this.editando();
    if (!f) return;
    const nueva = this.vencimiento();

    if (!nueva) {
      this.errorFormulario.set('Indica la nueva fecha de vencimiento.');
      return;
    }
    if (nueva < f.fechaEmision) {
      this.errorFormulario.set('El vencimiento no puede ser anterior a la emisión.');
      return;
    }

    this.errorFormulario.set(null);
    this.guardando.set(true);
    this.facturaService.actualizarVencimiento(f.numeroFactura, nueva).subscribe({
      next: () => {
        this.guardando.set(false);
        this.editando.set(null);
        this.notificacionService.exito('Vencimiento actualizado.', 'Listo');
        this.cambio.emit();
      },
      error: (err) => {
        this.guardando.set(false);
        this.errorFormulario.set(extraerError(err, 'No se pudo actualizar la factura.'));
      },
    });
  }

  /** Pide confirmación para eliminar una factura. */
  pedirEliminar(f: FacturaModel): void {
    this.facturaAEliminar.set(f);
  }

  /**
   * Arma el texto del diálogo de eliminar una factura.
   * @param f Factura que se va a eliminar.
   * @returns Mensaje para el diálogo de confirmación.
   */
  mensajeEliminar(f: FacturaModel): string {
    return (
      `Se eliminará la factura N.º ${f.numeroFactura} de ${nombreCasa(f.casaManzana, f.casaNumero)} ` +
      `(periodo ${f.periodo}). Solo se puede eliminar si está pendiente y no tiene líneas ni pagos. ` +
      'Esta acción no se puede deshacer.'
    );
  }

  /**
   * Elimina la factura confirmada en el diálogo y avisa a Finanzas. Si tiene líneas o pagos, el
   * backend responde 409 con el motivo.
   */
  confirmarEliminar(): void {
    const f = this.facturaAEliminar();
    if (!f) return;
    this.procesando.set(true);
    this.facturaService.eliminar(f.numeroFactura).subscribe({
      next: () => {
        this.procesando.set(false);
        this.facturaAEliminar.set(null);
        this.notificacionService.exito('Factura eliminada.', 'Listo');
        this.cambio.emit();
      },
      error: (err) => {
        this.procesando.set(false);
        this.facturaAEliminar.set(null);
        this.notificacionService.error(
          extraerError(err, 'No se pudo eliminar la factura.'),
          'No se pudo eliminar',
        );
      },
    });
  }

  /** Abre el diálogo de "Marcar vencidas". */
  pedirMarcarVencidas(): void {
    this.confirmandoVencidas.set(true);
  }

  /** Marca como vencidas las facturas con vencimiento pasado y avisa a Finanzas. */
  confirmarVencidas(): void {
    this.procesando.set(true);
    this.facturaService.marcarVencidas().subscribe({
      next: (r) => {
        this.procesando.set(false);
        this.confirmandoVencidas.set(false);
        this.notificacionService.exito(
          r.cantidad === 1
            ? '1 factura pasó a vencida.'
            : `${r.cantidad} facturas pasaron a vencidas.`,
          'Listo',
        );
        this.cambio.emit();
      },
      error: (err) => {
        this.procesando.set(false);
        this.confirmandoVencidas.set(false);
        this.notificacionService.error(
          extraerError(err, 'No se pudieron marcar las facturas vencidas.'),
          'No se pudo completar',
        );
      },
    });
  }
}
