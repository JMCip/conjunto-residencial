import { Component, computed, inject, OnInit, signal } from '@angular/core';
import {
  CeldaParqueaderoModel,
  ESTADOS_CELDA,
  ETIQUETA_CELDA,
  nombreCelda,
  TIPOS_CELDA,
} from '../../../models/celda-parqueadero.model';
import { CeldaParqueaderoService } from '../../../services/celda-parqueadero.service';
import { NotificacionService } from '../../../services/notificacion.service';
import { extraerError } from '../../../compartido/extraer-error';
import { ModalFormulario } from '../../../compartido/modal-formulario/modal-formulario';
import { DialogoConfirmar } from '../../../compartido/dialogo-confirmar/dialogo-confirmar';

/** Campos del formulario. */
interface FormularioCelda {
  tipo: string;
  estado: string;
}

/** Valores iniciales del formulario (todo vacío). */
const FORMULARIO_VACIO: FormularioCelda = {
  tipo: 'PROPIA',
  estado: 'DESOCUPADA',
};

/**
 * @component GestionCeldas
 * @description Pestaña Celdas de Parqueadero: CRUD de las celdas (tipo y estado).
 * El número de celda lo pone la base al crear.
 */
@Component({
  selector: 'app-gestion-celdas',
  imports: [ModalFormulario, DialogoConfirmar],
  templateUrl: './gestion-celdas.html',
  styleUrl: './gestion-celdas.css',
})
export class GestionCeldas implements OnInit {
  /** Servicio de celdas del parqueadero. */
  private celdaService = inject(CeldaParqueaderoService);
  /** Servicio de avisos flotantes. */
  private notificacionService = inject(NotificacionService);

  /** Celdas obtenidas del backend. */
  protected readonly celdas = signal<CeldaParqueaderoModel[]>([]);

  /** true mientras se consulta la información. */
  protected readonly cargando = signal(true);
  /** true si la consulta de la información falló. */
  protected readonly errorCarga = signal(false);

  /** Filtro por estado: 'TODAS', 'OCUPADA' o 'DESOCUPADA'. */
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

  /** Texto de cada tipo y estado de celda (constante del modelo). */
  protected readonly etiqueta = ETIQUETA_CELDA;
  /** Función del modelo para mostrar el nombre de una celda en la plantilla. */
  protected readonly nombreCelda = nombreCelda;
  /** Opciones del control segmentado (constante). */
  protected readonly opcionesFiltro = ['TODAS', ...ESTADOS_CELDA];
  /** Tipos de celda para el selector (constante del modelo). */
  protected readonly tiposCelda = TIPOS_CELDA;
  /** Estados de celda para el selector (constante del modelo). */
  protected readonly estadosCelda = ESTADOS_CELDA;

  /** Modal de crear/editar. */
  protected readonly modalAbierto = signal(false);

  /** Celda que se edita (null = crear). */
  protected readonly editando = signal<CeldaParqueaderoModel | null>(null);

  /** Valores actuales del formulario del modal. */
  protected readonly formulario = signal<FormularioCelda>({ ...FORMULARIO_VACIO });
  /** true mientras se envía el formulario al backend. */
  protected readonly guardando = signal(false);
  /** Mensaje de validación del formulario (null si no hay error). */
  protected readonly errorFormulario = signal<string | null>(null);

  /** Celda pendiente de confirmar para eliminar. */
  protected readonly celdaAEliminar = signal<CeldaParqueaderoModel | null>(null);
  /** true mientras se envía la eliminación al backend. */
  protected readonly eliminando = signal(false);

  /** Consulta los datos al abrir la pantalla. */
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

  /** Abre el modal vacío para crear. */
  abrirCrear(): void {
    this.editando.set(null);
    this.formulario.set({ ...FORMULARIO_VACIO });
    this.errorFormulario.set(null);
    this.modalAbierto.set(true);
  }

  /**
   * Abre el modal con los datos de la celda.
   * @param c Celda a editar.
   */
  abrirEditar(c: CeldaParqueaderoModel): void {
    this.editando.set(c);
    this.formulario.set({ tipo: c.tipo, estado: c.estado });
    this.errorFormulario.set(null);
    this.modalAbierto.set(true);
  }

  /** Cierra el modal sin guardar. */
  cerrarModal(): void {
    this.modalAbierto.set(false);
  }

  /**
   * Actualiza un campo del formulario.
   * @param campo Nombre del campo.
   * @param valor Valor elegido.
   */
  cambiarCampo(campo: keyof FormularioCelda, valor: string): void {
    this.formulario.update((f) => ({ ...f, [campo]: valor }));
  }

  /** Valida y guarda (crear o editar). */
  guardar(): void {
    const f = this.formulario();
    if (!f.tipo || !f.estado) {
      this.errorFormulario.set('El tipo y el estado son obligatorios.');
      return;
    }

    const actual = this.editando();
    this.errorFormulario.set(null);
    this.guardando.set(true);
    const peticion = actual
      ? this.celdaService.actualizar({ idCelda: actual.idCelda, tipo: f.tipo, estado: f.estado })
      : this.celdaService.crear({ tipo: f.tipo, estado: f.estado });

    peticion.subscribe({
      next: () => {
        this.guardando.set(false);
        this.modalAbierto.set(false);
        this.notificacionService.exito(actual ? 'Celda actualizada.' : 'Celda creada.', 'Listo');
        this.cargarCeldas();
      },
      error: (err) => {
        this.guardando.set(false);
        this.errorFormulario.set(extraerError(err, 'No se pudo guardar la celda.'));
      },
    });
  }

  /** Confirma y elimina la celda. */
  eliminar(): void {
    const c = this.celdaAEliminar();
    if (!c) return;
    this.eliminando.set(true);
    this.celdaService.eliminar(c.idCelda).subscribe({
      next: () => {
        this.eliminando.set(false);
        this.celdaAEliminar.set(null);
        this.notificacionService.exito('Celda eliminada.', 'Listo');
        this.cargarCeldas();
      },
      error: (err) => {
        this.eliminando.set(false);
        this.celdaAEliminar.set(null);
        this.notificacionService.error(
          extraerError(err, 'No se pudo eliminar la celda.'),
          'No se pudo eliminar',
        );
      },
    });
  }
}
