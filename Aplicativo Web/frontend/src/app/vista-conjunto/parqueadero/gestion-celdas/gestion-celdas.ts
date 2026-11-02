import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import {
  CeldaParqueaderoModel,
  ESTADOS_CELDA,
  ETIQUETA_CELDA,
  nombreCelda,
  TIPOS_CELDA,
} from '../../../models/celda-parqueadero.model';
import { CeldaParqueaderoService } from '../../../services/celda-parqueadero.service';
import { NotificacionService } from '../../../services/notificacion.service';
import { ModalFormulario } from '../../../compartido/modal-formulario/modal-formulario';
import { DialogoConfirmar } from '../../../compartido/dialogo-confirmar/dialogo-confirmar';
import { extraerError } from '../../../compartido/extraer-error';

/**
 * @component GestionCeldas
 * @description Pestaña Celdas de Parqueadero: CRUD de las celdas (tipo y estado).
 * El número de celda lo pone la base al crear.
 */
@Component({
  selector: 'app-gestion-celdas',
  imports: [FormsModule, ModalFormulario, DialogoConfirmar],
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

  // ---------- Modal de crear / editar ----------

  protected readonly modoModal = signal<'crear' | 'editar' | null>(null);
  private idEditando = 0;
  protected readonly formTipo = signal<string>('CARRO');
  protected readonly formEstado = signal<string>('DISPONIBLE');
  protected readonly errorFormulario = signal<string | null>(null);
  protected readonly guardando = signal(false);

  // ---------- Diálogo de eliminar ----------

  protected readonly celdaAEliminar = signal<CeldaParqueaderoModel | null>(null);
  protected readonly eliminando = signal(false);

  /** Constantes y funciones para la plantilla. */
  protected readonly tiposCelda = TIPOS_CELDA;
  protected readonly estadosCelda = ESTADOS_CELDA;
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

  /** Abre el modal vacío. */
  abrirModalCrear(): void {
    this.idEditando = 0;
    this.formTipo.set('CARRO');
    this.formEstado.set('DISPONIBLE');
    this.errorFormulario.set(null);
    this.modoModal.set('crear');
  }

  /**
   * Abre el modal con los datos de una celda.
   * @param c Celda elegida en la tabla.
   */
  abrirModalEditar(c: CeldaParqueaderoModel): void {
    this.idEditando = c.idCelda;
    this.formTipo.set(c.tipo);
    this.formEstado.set(c.estado);
    this.errorFormulario.set(null);
    this.modoModal.set('editar');
  }

  /** Cierra el modal sin guardar. */
  cerrarModal(): void {
    this.modoModal.set(null);
  }

  /** Llama a crear o actualizar (los dos campos son listas, no hay nada que validar). */
  guardar(): void {
    const datos = { tipo: this.formTipo(), estado: this.formEstado() };
    const creando = this.modoModal() === 'crear';
    const peticion = creando
      ? this.celdaService.postCrear(datos)
      : this.celdaService.putActualizar({ idCelda: this.idEditando, ...datos });

    this.guardando.set(true);
    peticion.subscribe({
      next: () => {
        this.guardando.set(false);
        this.modoModal.set(null);
        this.notificacionService.exito(
          creando ? 'Celda creada correctamente.' : `${nombreCelda(this.idEditando)} actualizada correctamente.`,
        );
        this.cargarCeldas();
      },
      error: (err) => {
        this.guardando.set(false);
        this.errorFormulario.set(
          extraerError(err, `No se pudo ${creando ? 'crear' : 'actualizar'} la celda.`),
        );
      },
    });
  }

  /** Pide confirmación antes de eliminar. */
  pedirEliminar(c: CeldaParqueaderoModel): void {
    this.celdaAEliminar.set(c);
  }

  /** Cierra el diálogo sin eliminar. */
  cancelarEliminar(): void {
    this.celdaAEliminar.set(null);
  }

  /** Envía el DELETE de la celda confirmada. */
  confirmarEliminar(): void {
    const c = this.celdaAEliminar();
    if (!c) return;
    this.eliminando.set(true);
    this.celdaService.deleteEliminar(c.idCelda).subscribe({
      next: () => {
        this.eliminando.set(false);
        this.celdaAEliminar.set(null);
        this.notificacionService.exito(`${nombreCelda(c.idCelda)} eliminada correctamente.`);
        this.cargarCeldas();
      },
      error: (err) => {
        this.eliminando.set(false);
        this.celdaAEliminar.set(null);
        this.notificacionService.error(
          extraerError(err, 'No se pudo eliminar la celda.'),
          'No se eliminó',
        );
      },
    });
  }
}
