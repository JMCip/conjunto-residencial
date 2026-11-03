import { Component, computed, inject, OnInit, signal } from '@angular/core';
import {
  CeldaParqueaderoModel,
  ESTADOS_CELDA,
  ETIQUETA_CELDA,
  nombreCelda,
} from '../../../models/celda-parqueadero.model';
import { CeldaParqueaderoService } from '../../../services/celda-parqueadero.service';
import { NotificacionService } from '../../../services/notificacion.service';
import { extraerError } from '../../../compartido/extraer-error';

/**
 * @component GestionCeldas
 * @description Pestaña Celdas de Parqueadero: CRUD de las celdas (tipo y estado).
 * El número de celda lo pone la base al crear.
 */
@Component({
  selector: 'app-gestion-celdas',
  imports: [],
  templateUrl: './gestion-celdas.html',
  styleUrl: './gestion-celdas.css',
})
export class GestionCeldas implements OnInit {
  private celdaService = inject(CeldaParqueaderoService);
  private notificacionService = inject(NotificacionService);

  /** Celdas obtenidas del backend. */
  protected readonly celdas = signal<CeldaParqueaderoModel[]>([]);

  protected readonly cargando = signal(true);
  protected readonly errorCarga = signal(false);

  /** Filtro por estado: 'TODAS', 'DISPONIBLE' o 'ASIGNADA'. */
  protected readonly filtroEstado = signal<string>('TODAS');

  /** Celdas visibles según el filtro. */
  protected readonly celdasFiltradas = computed(() =>
    this.filtroEstado() === 'TODAS'
      ? this.celdas()
      : this.celdas().filter((c) => c.estado === this.filtroEstado()),
  );

  /** Cuántas celdas hay de cada estado (para los botones del filtro). */
  protected readonly conteos = computed(() => {
    const conteo: Record<string, number> = { TODAS: this.celdas().length };
    for (const estado of ESTADOS_CELDA) {
      conteo[estado] = this.celdas().filter((c) => c.estado === estado).length;
    }
    return conteo;
  });

  /** "12 celdas" o "4 celdas de 12". */
  protected readonly textoConteo = computed(() => {
    const visibles = this.celdasFiltradas().length;
    const total = this.celdas().length;
    const palabra = visibles === 1 ? 'celda' : 'celdas';
    return visibles === total ? `${visibles} ${palabra}` : `${visibles} ${palabra} de ${total}`;
  });

  /** Constantes y funciones para la plantilla. */
  protected readonly etiqueta = ETIQUETA_CELDA;
  protected readonly nombreCelda = nombreCelda;
  protected readonly opcionesFiltro = ['TODAS', ...ESTADOS_CELDA];

  ngOnInit(): void {
    this.cargarCeldas();
  }

  /** Consulta las celdas. */
  cargarCeldas(): void {
    this.cargando.set(true);
    this.errorCarga.set(false);
    this.celdaService.getTodos().subscribe({
      next: (datos) => {
        this.celdas.set(datos);
        this.cargando.set(false);
      },
      error: (err) => {
        this.errorCarga.set(true);
        this.cargando.set(false);
        this.notificacionService.error(
          extraerError(err, 'No se pudieron consultar las celdas.'),
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
    return opcion === 'TODAS' ? 'Todas' : `${ETIQUETA_CELDA[opcion]}s`;
  }
}
