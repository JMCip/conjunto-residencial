import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { CurrencyPipe, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { forkJoin } from 'rxjs';
import {
  ESTADOS_SANCION,
  ESTILO_SANCION,
  ETIQUETA_SANCION,
  SancionModel,
  TIPOS_SANCION,
} from '../../../models/sancion.model';
import { CasaModel, llaveCasa, nombreCasa, separarLlaveCasa } from '../../../models/casa.model';
import { SancionService } from '../../../services/sancion.service';
import { CasaService } from '../../../services/casa.service';
import { NotificacionService } from '../../../services/notificacion.service';
import { ModalFormulario } from '../../../compartido/modal-formulario/modal-formulario';
import { DialogoConfirmar } from '../../../compartido/dialogo-confirmar/dialogo-confirmar';
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
  imports: [CurrencyPipe, DatePipe, FormsModule, ModalFormulario, DialogoConfirmar],
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

  // ---------- Modal de crear / editar ----------

  protected readonly modoModal = signal<'crear' | 'editar' | null>(null);
  private idEditando = 0;
  protected readonly formCasa = signal('');
  protected readonly formTipo = signal<string>('RUIDO');
  protected readonly formFechaIncidente = signal('');
  protected readonly formFechaNotificacion = signal('');
  protected readonly formDescripcion = signal('');
  protected readonly formDescargos = signal('');
  protected readonly formEstado = signal<string>('EN_DESCARGOS');
  protected readonly formValor = signal<number | null>(null);
  protected readonly errorFormulario = signal<string | null>(null);
  protected readonly guardando = signal(false);

  // ---------- Diálogo de eliminar ----------

  protected readonly sancionAEliminar = signal<SancionModel | null>(null);
  protected readonly eliminando = signal(false);

  /** Constantes y funciones para la plantilla. */
  protected readonly tiposSancion = TIPOS_SANCION;
  protected readonly estadosSancion = ESTADOS_SANCION;
  protected readonly etiqueta = ETIQUETA_SANCION;
  protected readonly estilo = ESTILO_SANCION;
  protected readonly opcionesFiltro = ['TODAS', ...ESTADOS_SANCION];
  protected readonly maxDescripcion = MAX_DESCRIPCION;
  protected readonly maxDescargos = MAX_DESCARGOS;
  protected readonly nombreCasa = nombreCasa;
  protected readonly llaveCasa = llaveCasa;

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

  /** Abre el modal vacío. */
  abrirModalCrear(): void {
    this.idEditando = 0;
    this.formCasa.set('');
    this.formTipo.set('RUIDO');
    this.formFechaIncidente.set('');
    this.formFechaNotificacion.set('');
    this.formDescripcion.set('');
    this.formDescargos.set('');
    this.formEstado.set('EN_DESCARGOS');
    this.formValor.set(null);
    this.errorFormulario.set(null);
    this.modoModal.set('crear');
  }

  /**
   * Abre el modal con los datos de una sanción.
   * @param s Sanción elegida en la tabla.
   */
  abrirModalEditar(s: SancionModel): void {
    this.idEditando = s.idSancion;
    this.formCasa.set(llaveCasa(s.casaManzana, s.casaNumero));
    this.formTipo.set(s.tipo);
    this.formFechaIncidente.set(s.fechaIncidente);
    this.formFechaNotificacion.set(s.fechaNotificacion);
    this.formDescripcion.set(s.descripcion);
    this.formDescargos.set(s.descargos);
    this.formEstado.set(s.estadoProceso);
    this.formValor.set(s.valor);
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

    const casa = separarLlaveCasa(this.formCasa());
    const datos = {
      fechaIncidente: this.formFechaIncidente(),
      fechaNotificacion: this.formFechaNotificacion(),
      tipo: this.formTipo(),
      descripcion: this.formDescripcion().trim(),
      // descargos es NOT NULL en la base: si no hay, se guarda texto vacío.
      descargos: this.formDescargos().trim(),
      estadoProceso: this.formEstado(),
      valor: this.formValor(),
      casaManzana: casa.manzana,
      casaNumero: casa.numero,
    };
    const creando = this.modoModal() === 'crear';
    const peticion = creando
      ? this.sancionService.postCrear(datos)
      : this.sancionService.putActualizar({ idSancion: this.idEditando, ...datos });

    this.guardando.set(true);
    peticion.subscribe({
      next: () => {
        this.guardando.set(false);
        this.modoModal.set(null);
        this.notificacionService.exito(
          `Sanción a ${nombreCasa(casa.manzana, casa.numero)} ${creando ? 'registrada' : 'actualizada'} correctamente.`,
        );
        this.cargarSanciones();
      },
      error: (err) => {
        this.guardando.set(false);
        this.errorFormulario.set(
          extraerError(err, `No se pudo ${creando ? 'registrar' : 'actualizar'} la sanción.`),
        );
      },
    });
  }

  /** Pide confirmación antes de eliminar. */
  pedirEliminar(s: SancionModel): void {
    this.sancionAEliminar.set(s);
  }

  /** Cierra el diálogo sin eliminar. */
  cancelarEliminar(): void {
    this.sancionAEliminar.set(null);
  }

  /** Envía el DELETE de la sanción confirmada. */
  confirmarEliminar(): void {
    const s = this.sancionAEliminar();
    if (!s) return;
    this.eliminando.set(true);
    this.sancionService.deleteEliminar(s.idSancion).subscribe({
      next: () => {
        this.eliminando.set(false);
        this.sancionAEliminar.set(null);
        this.notificacionService.exito('Sanción eliminada correctamente.');
        this.cargarSanciones();
      },
      error: (err) => {
        this.eliminando.set(false);
        this.sancionAEliminar.set(null);
        this.notificacionService.error(
          extraerError(err, 'No se pudo eliminar la sanción.'),
          'No se eliminó',
        );
      },
    });
  }

  /**
   * Revisa los campos antes de enviar.
   * @returns Mensaje del primer problema, o null si todo está bien.
   */
  private validar(): string | null {
    if (!this.formCasa()) return 'Elige la casa.';
    if (!this.formFechaIncidente()) return 'Escribe la fecha del incidente.';
    if (!this.formFechaNotificacion()) return 'Escribe la fecha de notificación.';
    if (this.formFechaNotificacion() < this.formFechaIncidente()) {
      return 'La notificación no puede ser antes del incidente.';
    }
    const descripcion = this.formDescripcion().trim();
    if (!descripcion) return 'Escribe la descripción del incidente.';
    if (descripcion.length > MAX_DESCRIPCION) {
      return `La descripción puede tener máximo ${MAX_DESCRIPCION} caracteres.`;
    }
    if (this.formDescargos().trim().length > MAX_DESCARGOS) {
      return `Los descargos pueden tener máximo ${MAX_DESCARGOS} caracteres.`;
    }
    const valor = this.formValor();
    if (valor !== null && valor < 0) return 'El valor de la multa no puede ser negativo.';
    return null;
  }
}
