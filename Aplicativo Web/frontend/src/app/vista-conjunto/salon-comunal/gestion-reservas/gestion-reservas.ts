import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { CurrencyPipe, DatePipe, DecimalPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { forkJoin } from 'rxjs';
import {
  DatosReservaSalon,
  ESTADOS_RESERVA,
  ESTILO_RESERVA,
  ETIQUETA_RESERVA,
  ReservaSalonModel,
} from '../../../models/reserva-salon.model';
import { CasaModel, llaveCasa, nombreCasa, separarLlaveCasa } from '../../../models/casa.model';
import { ReservaSalonService } from '../../../services/reserva-salon.service';
import { CasaService } from '../../../services/casa.service';
import { NotificacionService } from '../../../services/notificacion.service';
import { ModalFormulario } from '../../../compartido/modal-formulario/modal-formulario';
import { DialogoConfirmar } from '../../../compartido/dialogo-confirmar/dialogo-confirmar';
import { extraerError } from '../../../compartido/extraer-error';

/** Filtro del control segmentado: todas o un estado. */
type FiltroReserva = 'TODAS' | ReservaSalonModel['estado'];

/**
 * @component GestionReservas
 * @description CRUD de las reservas del salón comunal. La reserva es de una casa, por franja
 * horaria; la base no deja cruzar dos franjas que no estén canceladas (restricción EXCLUDE).
 */
@Component({
  selector: 'app-gestion-reservas',
  imports: [CurrencyPipe, DatePipe, DecimalPipe, FormsModule, ModalFormulario, DialogoConfirmar],
  templateUrl: './gestion-reservas.html',
  styleUrl: './gestion-reservas.css',
})
export class GestionReservas implements OnInit {
  private reservaService = inject(ReservaSalonService);
  private casaService = inject(CasaService);
  private notificacionService = inject(NotificacionService);

  /** Reservas obtenidas del backend. */
  protected readonly reservas = signal<ReservaSalonModel[]>([]);

  /** Casas para el selector. */
  protected readonly casas = signal<CasaModel[]>([]);

  protected readonly cargando = signal(true);
  protected readonly errorCarga = signal(false);

  /** Filtro de estado seleccionado. */
  protected readonly filtroEstado = signal<FiltroReserva>('TODAS');

  /** Reservas visibles según el filtro, de la más reciente a la más antigua. */
  protected readonly reservasFiltradas = computed(() =>
    this.reservas()
      .filter((r) => this.filtroEstado() === 'TODAS' || r.estado === this.filtroEstado())
      .sort((a, b) => b.horaInicio.localeCompare(a.horaInicio)),
  );

  /** Cuántas reservas hay de cada estado (para los botones del filtro). */
  protected readonly conteos = computed(() => {
    const conteo: Record<string, number> = { TODAS: this.reservas().length };
    for (const estado of ESTADOS_RESERVA) {
      conteo[estado] = this.reservas().filter((r) => r.estado === estado).length;
    }
    return conteo;
  });

  /** "6 reservas" o "2 reservas de 6". */
  protected readonly textoConteo = computed(() => {
    const visibles = this.reservasFiltradas().length;
    const total = this.reservas().length;
    const palabra = visibles === 1 ? 'reserva' : 'reservas';
    return visibles === total ? `${visibles} ${palabra}` : `${visibles} ${palabra} de ${total}`;
  });

  // ---------- Modal de crear / editar ----------

  protected readonly modoModal = signal<'crear' | 'editar' | null>(null);
  private idEditando = 0;
  protected readonly formCasa = signal('');
  protected readonly formFecha = signal('');
  protected readonly formHoraInicio = signal('');
  protected readonly formHoraFinal = signal('');
  protected readonly formValorPorHora = signal<number | null>(null);
  protected readonly formDeposito = signal<number | null>(null);
  protected readonly formEstado = signal<ReservaSalonModel['estado']>('CONFIRMADA');
  protected readonly errorFormulario = signal<string | null>(null);
  protected readonly guardando = signal(false);

  /** Horas entre inicio y final mientras se llena el formulario (null si falta algo). */
  protected readonly horasCalculadas = computed(() => {
    const inicio = this.formHoraInicio();
    const final = this.formHoraFinal();
    if (!inicio || !final) return null;
    const minutos = (h: string) => Number(h.slice(0, 2)) * 60 + Number(h.slice(3, 5));
    const horas = (minutos(final) - minutos(inicio)) / 60;
    return horas > 0 ? horas : null;
  });

  /** Vista previa del total: horas × valor por hora (lo mismo que calcula el backend). */
  protected readonly totalCalculado = computed(() => {
    const horas = this.horasCalculadas();
    const valor = this.formValorPorHora();
    return horas !== null && valor !== null ? horas * valor : null;
  });

  // ---------- Diálogo de eliminar ----------

  protected readonly reservaAEliminar = signal<ReservaSalonModel | null>(null);
  protected readonly eliminando = signal(false);

  /** Constantes y funciones para la plantilla. */
  protected readonly estadosReserva = ESTADOS_RESERVA;
  protected readonly etiquetaReserva = ETIQUETA_RESERVA;
  protected readonly estiloReserva = ESTILO_RESERVA;
  protected readonly opcionesFiltro: FiltroReserva[] = ['TODAS', ...ESTADOS_RESERVA];
  protected readonly nombreCasa = nombreCasa;
  protected readonly llaveCasa = llaveCasa;

  ngOnInit(): void {
    this.cargarReservas();
  }

  /** Consulta reservas y casas a la vez. */
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

  /**
   * Texto de un botón del filtro ("Todas", "Confirmadas"…).
   * @param opcion 'TODAS' o un estado.
   */
  textoFiltro(opcion: FiltroReserva): string {
    return opcion === 'TODAS' ? 'Todas' : `${ETIQUETA_RESERVA[opcion]}s`;
  }

  /**
   * "14:00 – 20:00" a partir de dos TIMESTAMP ("2026-09-12 14:00:00").
   * @param r Reserva de la tabla.
   */
  franja(r: ReservaSalonModel): string {
    return `${r.horaInicio.slice(11, 16)} – ${r.horaFinal.slice(11, 16)}`;
  }

  /** Abre el modal vacío, con los valores de la última reserva como sugerencia. */
  abrirModalCrear(): void {
    const ultima = [...this.reservas()].sort((a, b) => b.horaInicio.localeCompare(a.horaInicio))[0];
    this.idEditando = 0;
    this.formCasa.set('');
    this.formFecha.set('');
    this.formHoraInicio.set('');
    this.formHoraFinal.set('');
    this.formValorPorHora.set(ultima?.valorPorHora ?? null);
    this.formDeposito.set(ultima?.valorDeposito ?? null);
    this.formEstado.set('CONFIRMADA');
    this.errorFormulario.set(null);
    this.modoModal.set('crear');
  }

  /**
   * Abre el modal con los datos de una reserva.
   * @param r Reserva elegida en la tabla.
   */
  abrirModalEditar(r: ReservaSalonModel): void {
    this.idEditando = r.idReserva;
    this.formCasa.set(llaveCasa(r.casaManzana, r.casaNumero));
    this.formFecha.set(r.fechaReserva);
    this.formHoraInicio.set(r.horaInicio.slice(11, 16));
    this.formHoraFinal.set(r.horaFinal.slice(11, 16));
    this.formValorPorHora.set(r.valorPorHora);
    this.formDeposito.set(r.valorDeposito);
    this.formEstado.set(r.estado);
    this.errorFormulario.set(null);
    this.modoModal.set('editar');
  }

  /** Cierra el modal sin guardar. */
  cerrarModal(): void {
    this.modoModal.set(null);
  }

  /** Valida y llama a crear o actualizar. */
  guardar(): void {
    const error = this.validar();
    this.errorFormulario.set(error);
    if (error) return;

    const datos = this.datosFormulario();
    const creando = this.modoModal() === 'crear';
    const peticion = creando
      ? this.reservaService.postCrear(datos)
      : this.reservaService.putActualizar(this.idEditando, datos);

    this.guardando.set(true);
    peticion.subscribe({
      next: () => {
        this.guardando.set(false);
        this.modoModal.set(null);
        this.notificacionService.exito(
          `Reserva de ${nombreCasa(datos.casaManzana, datos.casaNumero)} ${creando ? 'registrada' : 'actualizada'} correctamente.`,
        );
        this.cargarReservas();
      },
      error: (err) => {
        this.guardando.set(false);
        this.errorFormulario.set(
          extraerError(err, `No se pudo ${creando ? 'registrar' : 'actualizar'} la reserva.`),
        );
      },
    });
  }

  /** Pide confirmación antes de eliminar. */
  pedirEliminar(r: ReservaSalonModel): void {
    this.reservaAEliminar.set(r);
  }

  /** Cierra el diálogo sin eliminar. */
  cancelarEliminar(): void {
    this.reservaAEliminar.set(null);
  }

  /** Envía el DELETE de la reserva confirmada. */
  confirmarEliminar(): void {
    const r = this.reservaAEliminar();
    if (!r) return;
    this.eliminando.set(true);
    this.reservaService.deleteEliminar(r.idReserva).subscribe({
      next: () => {
        this.eliminando.set(false);
        this.reservaAEliminar.set(null);
        this.notificacionService.exito('Reserva eliminada correctamente.');
        this.cargarReservas();
      },
      error: (err) => {
        this.eliminando.set(false);
        this.reservaAEliminar.set(null);
        this.notificacionService.error(
          extraerError(err, 'No se pudo eliminar la reserva.'),
          'No se eliminó',
        );
      },
    });
  }

  /** Arma el JSON que se envía: hora_inicio y hora_final son TIMESTAMP "AAAA-MM-DD HH:mm:ss". */
  private datosFormulario(): DatosReservaSalon {
    const casa = separarLlaveCasa(this.formCasa());
    const fecha = this.formFecha();
    return {
      fechaReserva: fecha,
      horaInicio: `${fecha} ${this.formHoraInicio()}:00`,
      horaFinal: `${fecha} ${this.formHoraFinal()}:00`,
      valorDeposito: this.formDeposito()!,
      valorPorHora: this.formValorPorHora()!,
      estado: this.formEstado(),
      casaManzana: casa.manzana,
      casaNumero: casa.numero,
    };
  }

  /**
   * Revisa los campos antes de enviar, incluida la regla de que dos franjas no se crucen.
   * @returns Mensaje del primer problema, o null si todo está bien.
   */
  private validar(): string | null {
    if (!this.formCasa()) return 'Elige la casa.';
    if (!this.formFecha()) return 'Escribe la fecha de la reserva.';
    if (!this.formHoraInicio() || !this.formHoraFinal()) return 'Escribe la hora de inicio y la final.';
    if (this.horasCalculadas() === null) return 'La hora final debe ser después de la de inicio.';
    const valor = this.formValorPorHora();
    if (valor === null || valor <= 0) return 'El valor por hora debe ser mayor que 0.';
    const deposito = this.formDeposito();
    if (deposito === null || deposito < 0) return 'Escribe el valor del depósito (0 si no aplica).';

    // Dos franjas no pueden cruzarse; las reservas canceladas no ocupan la franja.
    if (this.formEstado() !== 'CANCELADA') {
      const { horaInicio, horaFinal } = this.datosFormulario();
      const cruce = this.reservas().find(
        (r) =>
          r.idReserva !== this.idEditando &&
          r.estado !== 'CANCELADA' &&
          horaInicio < r.horaFinal &&
          horaFinal > r.horaInicio,
      );
      if (cruce) {
        return `Esa franja se cruza con la reserva de ${nombreCasa(cruce.casaManzana, cruce.casaNumero)} (${this.franja(cruce)}).`;
      }
    }
    return null;
  }
}
