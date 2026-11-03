import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { CurrencyPipe, DatePipe, DecimalPipe } from '@angular/common';
import { forkJoin } from 'rxjs';
import {
  ESTADOS_RESERVA,
  ESTILO_RESERVA,
  ETIQUETA_RESERVA,
  ReservaSalonModel,
} from '../../../models/reserva-salon.model';
import { CasaModel, nombreCasa } from '../../../models/casa.model';
import { ReservaSalonService } from '../../../services/reserva-salon.service';
import { CasaService } from '../../../services/casa.service';
import { NotificacionService } from '../../../services/notificacion.service';
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
  imports: [CurrencyPipe, DatePipe, DecimalPipe],
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

  /** Constantes y funciones para la plantilla. */
  protected readonly etiquetaReserva = ETIQUETA_RESERVA;
  protected readonly estiloReserva = ESTILO_RESERVA;
  protected readonly opcionesFiltro: FiltroReserva[] = ['TODAS', ...ESTADOS_RESERVA];
  protected readonly nombreCasa = nombreCasa;

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
}
