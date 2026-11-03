import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { CurrencyPipe, DatePipe } from '@angular/common';
import { forkJoin } from 'rxjs';
import {
  ESTADOS_SANCION,
  ESTILO_SANCION,
  ETIQUETA_SANCION,
  SancionModel,
} from '../../../models/sancion.model';
import { CasaModel, nombreCasa } from '../../../models/casa.model';
import { SancionService } from '../../../services/sancion.service';
import { CasaService } from '../../../services/casa.service';
import { NotificacionService } from '../../../services/notificacion.service';
import { extraerError } from '../../../compartido/extraer-error';

/** Largo máximo de la descripción y de los descargos. */
const MAX_DESCRIPCION = 255;
const MAX_DESCARGOS = 500;

/**
 * @component GestionSanciones
 * @description CRUD de las sanciones a las casas: incidente, notificación, tipo, descripción,
 * descargos, estado del proceso y valor de la multa.
 */
@Component({
  selector: 'app-gestion-sanciones',
  imports: [CurrencyPipe, DatePipe],
  templateUrl: './gestion-sanciones.html',
  styleUrl: './gestion-sanciones.css',
})
export class GestionSanciones implements OnInit {
  private sancionService = inject(SancionService);
  private casaService = inject(CasaService);
  private notificacionService = inject(NotificacionService);

  /** Sanciones obtenidas del backend (de la más reciente a la más antigua). */
  protected readonly sanciones = signal<SancionModel[]>([]);

  /** Casas para el selector. */
  protected readonly casas = signal<CasaModel[]>([]);

  protected readonly cargando = signal(true);
  protected readonly errorCarga = signal(false);
  protected readonly busqueda = signal('');

  /** Filtro por estado del proceso: 'TODAS' o un estado. */
  protected readonly filtroEstado = signal<string>('TODAS');

  /** Sanciones visibles según el filtro y la búsqueda (casa, tipo o descripción). */
  protected readonly sancionesFiltradas = computed(() => {
    const texto = this.busqueda().trim().toLowerCase();
    return this.sanciones()
      .filter((s) => this.filtroEstado() === 'TODAS' || s.estadoProceso === this.filtroEstado())
      .filter(
        (s) =>
          !texto ||
          `${nombreCasa(s.casaManzana, s.casaNumero)} ${ETIQUETA_SANCION[s.tipo] ?? s.tipo} ${s.descripcion}`
            .toLowerCase()
            .includes(texto),
      );
  });

  /** Cuántas sanciones hay de cada estado (para los botones del filtro). */
  protected readonly conteos = computed(() => {
    const conteo: Record<string, number> = { TODAS: this.sanciones().length };
    for (const estado of ESTADOS_SANCION) {
      conteo[estado] = this.sanciones().filter((s) => s.estadoProceso === estado).length;
    }
    return conteo;
  });

  /** "5 sanciones" o "2 sanciones de 5". */
  protected readonly textoConteo = computed(() => {
    const visibles = this.sancionesFiltradas().length;
    const total = this.sanciones().length;
    const palabra = visibles === 1 ? 'sanción' : 'sanciones';
    return visibles === total ? `${visibles} ${palabra}` : `${visibles} ${palabra} de ${total}`;
  });

  /** Constantes y funciones para la plantilla. */
  protected readonly etiqueta = ETIQUETA_SANCION;
  protected readonly estilo = ESTILO_SANCION;
  protected readonly opcionesFiltro = ['TODAS', ...ESTADOS_SANCION];
  protected readonly nombreCasa = nombreCasa;

  ngOnInit(): void {
    this.cargarSanciones();
  }

  /** Consulta sanciones y casas a la vez. */
  cargarSanciones(): void {
    this.cargando.set(true);
    this.errorCarga.set(false);
    forkJoin({
      sanciones: this.sancionService.getTodos(),
      casas: this.casaService.getTodos(),
    }).subscribe({
      next: ({ sanciones, casas }) => {
        this.sanciones.set(sanciones);
        this.casas.set(casas);
        this.cargando.set(false);
      },
      error: (err) => {
        this.errorCarga.set(true);
        this.cargando.set(false);
        this.notificacionService.error(
          extraerError(err, 'No se pudieron consultar las sanciones.'),
          'Error de conexión',
        );
      },
    });
  }

  /**
   * Texto de un botón del filtro.
   * @param opcion 'TODAS' o un estado.
   */
  textoFiltro(opcion: string): string {
    const plurales: Record<string, string> = {
      TODAS: 'Todas',
      EN_DESCARGOS: 'En descargos',
      CONFIRMADA: 'Confirmadas',
      ANULADA: 'Anuladas',
    };
    return plurales[opcion] ?? opcion;
  }
}
