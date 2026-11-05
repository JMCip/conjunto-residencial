import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { CurrencyPipe, DatePipe, DecimalPipe } from '@angular/common';
import { forkJoin } from 'rxjs';
import {
  ESTADOS_RESERVA,
  ESTILO_RESERVA,
  ETIQUETA_RESERVA,
  EstadoReserva,
  FILTRO_RESERVA,
  ReservaSalonModel,
} from '../../../models/reserva-salon.model';
import {
  CasaModel,
  llaveCasa,
  nombreCasa,
  separarLlaveCasa,
} from '../../../models/casa.model';
import { ReservaSalonService } from '../../../services/reserva-salon.service';
import { CasaService } from '../../../services/casa.service';
import { NotificacionService } from '../../../services/notificacion.service';
import { extraerError } from '../../../compartido/extraer-error';
import { ModalFormulario } from '../../../compartido/modal-formulario/modal-formulario';
import { DialogoConfirmar } from '../../../compartido/dialogo-confirmar/dialogo-confirmar';

/** Filtro del control segmentado: todas o un estado. */
type FiltroReserva = 'TODAS' | EstadoReserva;

/** Acción pendiente de confirmar sobre una reserva. */
interface AccionReserva {
  tipo: 'CANCELADA' | 'FINALIZADA' | 'DEPOSITO_RETENIDO' | 'ELIMINAR';
  reserva: ReservaSalonModel;
}

/** Campos del formulario (todo como texto). Las fechas son de <input type="datetime-local">. */
interface FormularioReserva {
  casa: string;
  horaInicio: string;
  horaFinal: string;
  valorPorHora: string;
  valorDeposito: string;
}

const FORMULARIO_VACIO: FormularioReserva = {
  casa: '',
  horaInicio: '',
  horaFinal: '',
  valorPorHora: '',
  valorDeposito: '',
};

/**
 * @component GestionReservas
 * @description Reservas del salón comunal. Se crean (quedan CONFIRMADAS) y después solo se les
 * cambia el estado: cancelar, finalizar o retener el depósito. Las horas y los valores no se editan:
 * para otro horario se cancela y se crea una reserva nueva. Solo se eliminan las canceladas.
 */
@Component({
  selector: 'app-gestion-reservas',
  imports: [CurrencyPipe, DatePipe, DecimalPipe, ModalFormulario, DialogoConfirmar],
  templateUrl: './gestion-reservas.html',
  styleUrl: './gestion-reservas.css',
})
export class GestionReservas implements OnInit {
  private reservaService = inject(ReservaSalonService);
  private casaService = inject(CasaService);
  private notificacionService = inject(NotificacionService);

  protected readonly reservas = signal<ReservaSalonModel[]>([]);
  protected readonly casas = signal<CasaModel[]>([]);
  protected readonly cargando = signal(true);
  protected readonly errorCarga = signal(false);
  protected readonly filtroEstado = signal<FiltroReserva>('TODAS');

  protected readonly reservasFiltradas = computed(() =>
    this.reservas()
      .filter((r) => this.filtroEstado() === 'TODAS' || r.estado === this.filtroEstado())
      .sort((a, b) => b.horaInicio.localeCompare(a.horaInicio)),
  );

  protected readonly conteos = computed(() => {
    const conteo: Record<string, number> = { TODAS: this.reservas().length };
    for (const estado of ESTADOS_RESERVA) {
      conteo[estado] = this.reservas().filter((r) => r.estado === estado).length;
    }
    return conteo;
  });

  protected readonly textoConteo = computed(() => {
    const visibles = this.reservasFiltradas().length;
    const total = this.reservas().length;
    const palabra = visibles === 1 ? 'reserva' : 'reservas';
    return visibles === total ? `${visibles} ${palabra}` : `${visibles} ${palabra} de ${total}`;
  });

  protected readonly etiquetaReserva = ETIQUETA_RESERVA;
  protected readonly estiloReserva = ESTILO_RESERVA;
  protected readonly opcionesFiltro: FiltroReserva[] = ['TODAS', ...ESTADOS_RESERVA];
  protected readonly nombreCasa = nombreCasa;
  protected readonly llaveCasa = llaveCasa;

  /** Modal de crear. */
  protected readonly modalAbierto = signal(false);
  protected readonly formulario = signal<FormularioReserva>({ ...FORMULARIO_VACIO });
  protected readonly guardando = signal(false);
  protected readonly errorFormulario = signal<string | null>(null);

  /** Horas y total que se calculan mientras se llena el formulario (null si faltan datos). */
  protected readonly resumen = computed(() => {
    const f = this.formulario();
    if (!f.horaInicio || !f.horaFinal) return null;
    const inicio = new Date(f.horaInicio).getTime();
    const fin = new Date(f.horaFinal).getTime();
    if (Number.isNaN(inicio) || Number.isNaN(fin) || fin <= inicio) return null;
    const horas = (fin - inicio) / 3_600_000;
    const valor = Number(f.valorPorHora);
    const total = f.valorPorHora.trim() && Number.isFinite(valor) && valor > 0 ? horas * valor : null;
    return { horas, total };
  });

  /** Acción (cancelar, finalizar, retener o eliminar) pendiente de confirmar. */
  protected readonly accion = signal<AccionReserva | null>(null);
  protected readonly procesando = signal(false);

  ngOnInit(): void {
    this.cargarReservas();
  }

  cargarReservas(): void {
    this.cargando.set(true);
    this.errorCarga.set(false);
    forkJoin({
      reservas: this.reservaService.getTodos(),
      casas: this.casaService.getTodos(),
    }).subscribe({
      next: ({ reservas, casas }) => {
        this.reservas.set(reservas);
        this.casas.set(casas);
        this.cargando.set(false);
      },
      error: (err) => {
        this.errorCarga.set(true);
        this.cargando.set(false);
        this.notificacionService.error(
          extraerError(err, 'No se pudieron consultar las reservas.'),
          'Error de conexión',
        );
      },
    });
  }

  textoFiltro(opcion: FiltroReserva): string {
    return opcion === 'TODAS' ? 'Todas' : FILTRO_RESERVA[opcion];
  }

  /** "14:00 – 20:00" a partir de dos TIMESTAMP ("2026-09-12 14:00:00"). */
  franja(r: ReservaSalonModel): string {
    return `${r.horaInicio.slice(11, 16)} – ${r.horaFinal.slice(11, 16)}`;
  }

  /** El back solo deja finalizar o retener depósito cuando la reserva ya empezó. */
  yaComenzo(r: ReservaSalonModel): boolean {
    return new Date(r.horaInicio.replace(' ', 'T')).getTime() <= Date.now();
  }



  abrirCrear(): void {
    this.formulario.set({ ...FORMULARIO_VACIO });
    this.errorFormulario.set(null);
    this.modalAbierto.set(true);
  }

  cerrarModal(): void {
    this.modalAbierto.set(false);
  }

  cambiarCampo(campo: keyof FormularioReserva, valor: string): void {
    this.formulario.update((f) => ({ ...f, [campo]: valor }));
  }

  /** Valida y crea la reserva. El back revisa mora, sanciones y cruces de horario. */
  guardar(): void {
    const f = this.formulario();

    if (!f.casa) {
      this.errorFormulario.set('Elige la casa que reserva.');
      return;
    }
    if (!f.horaInicio || !f.horaFinal) {
      this.errorFormulario.set('Indica la hora de inicio y la hora final.');
      return;
    }
    const inicio = new Date(f.horaInicio);
    const fin = new Date(f.horaFinal);
    if (fin <= inicio) {
      this.errorFormulario.set('La hora final debe ser posterior a la de inicio.');
      return;
    }
    if (inicio <= new Date()) {
      this.errorFormulario.set('La reserva debe ser en una fecha y hora futura.');
      return;
    }
    const valorPorHora = Number(f.valorPorHora);
    if (!f.valorPorHora.trim() || !Number.isFinite(valorPorHora) || valorPorHora <= 0) {
      this.errorFormulario.set('El valor por hora debe ser un número mayor a 0 (sin puntos ni comas).');
      return;
    }
    const valorDeposito = Number(f.valorDeposito);
    if (!f.valorDeposito.trim() || !Number.isFinite(valorDeposito) || valorDeposito < 0) {
      this.errorFormulario.set('El depósito debe ser un número igual o mayor a 0 (sin puntos ni comas).');
      return;
    }

    const { manzana, numero } = separarLlaveCasa(f.casa);
    this.errorFormulario.set(null);
    this.guardando.set(true);

    this.reservaService
      .crear({
        horaInicio: `${f.horaInicio}:00`,
        horaFinal: `${f.horaFinal}:00`,
        valorDeposito,
        valorPorHora,
        casaNumero: numero,
        casaManzana: manzana,
      })
      .subscribe({
        next: () => {
          this.guardando.set(false);
          this.modalAbierto.set(false);
          this.notificacionService.exito('Reserva creada.', 'Listo');
          this.cargarReservas();
        },
        error: (err) => {
          this.guardando.set(false);
          this.errorFormulario.set(extraerError(err, 'No se pudo crear la reserva.'));
        },
      });
  }



  tituloAccion(a: AccionReserva): string {
    switch (a.tipo) {
      case 'CANCELADA': return '¿Cancelar la reserva?';
      case 'FINALIZADA': return '¿Finalizar la reserva?';
      case 'DEPOSITO_RETENIDO': return '¿Retener el depósito?';
      case 'ELIMINAR': return '¿Eliminar la reserva?';
    }
  }

  mensajeAccion(a: AccionReserva): string {
    const r = a.reserva;
    const quien = `${nombreCasa(r.casaManzana, r.casaNumero)} (${r.fechaReserva}, ${this.franja(r)})`;
    switch (a.tipo) {
      case 'CANCELADA':
        return `Se cancelará la reserva de ${quien}. La franja quedará libre y no se puede deshacer.`;
      case 'FINALIZADA':
        return `El salón fue usado sin novedades: se marcará la reserva de ${quien} como finalizada y se devuelve el depósito.`;
      case 'DEPOSITO_RETENIDO':
        return `Hubo daños: se retendrá el depósito de la reserva de ${quien}. Esta acción no se puede deshacer.`;
      case 'ELIMINAR':
        return `Se eliminará la reserva cancelada de ${quien}. Esta acción no se puede deshacer.`;
    }
  }

  textoConfirmarAccion(a: AccionReserva): string {
    switch (a.tipo) {
      case 'CANCELADA': return 'Cancelar reserva';
      case 'FINALIZADA': return 'Finalizar';
      case 'DEPOSITO_RETENIDO': return 'Retener depósito';
      case 'ELIMINAR': return 'Eliminar';
    }
  }

  /** Ejecuta la acción confirmada en el diálogo. */
  confirmarAccion(): void {
    const a = this.accion();
    if (!a) return;

    this.procesando.set(true);
    const peticion =
      a.tipo === 'ELIMINAR'
        ? this.reservaService.eliminar(a.reserva.idReserva)
        : this.reservaService.actualizarEstado(a.reserva.idReserva, a.tipo);

    const exito: Record<AccionReserva['tipo'], string> = {
      CANCELADA: 'Reserva cancelada.',
      FINALIZADA: 'Reserva finalizada.',
      DEPOSITO_RETENIDO: 'Depósito retenido.',
      ELIMINAR: 'Reserva eliminada.',
    };

    peticion.subscribe({
      next: () => {
        this.procesando.set(false);
        this.accion.set(null);
        this.notificacionService.exito(exito[a.tipo], 'Listo');
        this.cargarReservas();
      },
      error: (err) => {
        this.procesando.set(false);
        this.accion.set(null);
        this.notificacionService.error(
          extraerError(err, 'No se pudo completar la acción.'),
          'No se pudo completar',
        );
      },
    });
  }
}
