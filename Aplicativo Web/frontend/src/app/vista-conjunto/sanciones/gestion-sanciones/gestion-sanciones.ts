import {Component, computed, inject, OnInit, signal} from '@angular/core';
import {CurrencyPipe, DatePipe} from '@angular/common';
import {catchError, forkJoin, of } from 'rxjs';
import {ConceptoCobroModel} from '../../../models/concepto-cobro.model';
import {ConceptoCobroService} from '../../../services/concepto-cobro.service';

import {
  CambiosSancion,
  ESTADOS_SANCION,
  ESTILO_SANCION,
  ETIQUETA_ESTADO_SANCION,
  EstadoSancion,
  FILTRO_SANCION,
  SancionModel,
  SUGERENCIAS_TIPO_SANCION,
  sinDescargos,
  TEXTO_SIN_DESCARGOS,
  textoTipoSancion,
} from '../../../models/sancion.model';
import {
  CasaModel,
  llaveCasa,
  nombreCasa,
  separarLlaveCasa,
} from '../../../models/casa.model';
import {SancionService} from '../../../services/sancion.service';
import {CasaService} from '../../../services/casa.service';
import {NotificacionService} from '../../../services/notificacion.service';
import {extraerError} from '../../../compartido/extraer-error';
import {ModalFormulario} from '../../../compartido/modal-formulario/modal-formulario';
import {DialogoConfirmar} from '../../../compartido/dialogo-confirmar/dialogo-confirmar';

/** Longitud máxima del tipo de sanción (columna `tipo`). */
const MAX_TIPO = 30;

/** Longitud máxima de la descripción (columna `descripcion`). */
const MAX_DESCRIPCION = 255;

/** Longitud máxima de los descargos (columna `descargos`). */
const MAX_DESCARGOS = 500;

/** Filtro del control segmentado: todas o un estado. */
type FiltroSancion = 'TODAS' | EstadoSancion;

/** Acción pendiente de confirmar sobre una sanción (en un diálogo). */
interface AccionSancion {
  tipo: 'ANULAR' | 'ELIMINAR';
  sancion: SancionModel;
}

/** Modal para avanzar el proceso: registrar descargos o confirmar la sanción. */
interface ProcesoSancion {
  modo: 'DESCARGOS' | 'CONFIRMAR';
  sancion: SancionModel;
}

/** Campos del formulario de crear (todo como texto). Las fechas son de <input type="date">. */
interface FormularioSancion {
  casa: string;
  tipo: string;
  fechaIncidente: string;
  fechaNotificacion: string;
  descripcion: string;
  descargos: string;
}

/** Campos del modal de proceso: se usa uno u otro según el modo. */
interface FormularioProceso {
  descargos: string;
  valor: string;
}

/** Valores iniciales del formulario (todo vacío). */
const FORMULARIO_VACIO: FormularioSancion = {
  casa: '',
  tipo: '',
  fechaIncidente: '',
  fechaNotificacion: '',
  descripcion: '',
  descargos: '',
};

/**
 * Fecha de hoy como "yyyy-MM-dd" en la hora LOCAL (toISOString usaría UTC y en Colombia,
 * de noche, daría el día siguiente).
 */
function hoyISO(): string {
  const d = new Date();
  const mes = String(d.getMonth() + 1).padStart(2, '0');
  const dia = String(d.getDate()).padStart(2, '0');
  return `${d.getFullYear()}-${mes}-${dia}`;
}

/**
 * @component GestionSanciones
 * @description Sanciones a las casas. Se crean (quedan EN_DESCARGOS) y mientras estén así se les
 * pueden registrar descargos, confirmarlas (con multa opcional) o anularlas. Una vez decididas
 * ya no se modifican. Solo se eliminan las anuladas.
 */
@Component({
  selector: 'app-gestion-sanciones',
  imports: [CurrencyPipe, DatePipe, ModalFormulario, DialogoConfirmar],
  templateUrl: './gestion-sanciones.html',
  styleUrl: './gestion-sanciones.css',
})

export class GestionSanciones implements OnInit {
  /** Servicio de sanciones. */
  private sancionService = inject(SancionService);
  /** Servicio de casas. */
  private casaService = inject(CasaService);
  /** Servicio de avisos flotantes. */
  private notificacionService = inject(NotificacionService);
  /** Servicio del catálogo de conceptos de cobro. */
  private conceptoService = inject(ConceptoCobroService);

  /** Sanciones obtenidas del backend (de la más reciente a la más antigua). */
  protected readonly sanciones = signal<SancionModel[]>([]);

  /** Casas para el selector. */
  protected readonly casas = signal<CasaModel[]>([]);
  /** Lista de conceptos consultada al backend. */
  protected readonly conceptos = signal<ConceptoCobroModel[]>([]);
  /** true mientras se consulta la información. */
  protected readonly cargando = signal(true);
  /** true si la consulta de la información falló. */
  protected readonly errorCarga = signal(false);
  /** Texto escrito en el buscador. */
  protected readonly busqueda = signal('');
  /** Filtro de estado seleccionado en el control segmentado. */
  protected readonly filtroEstado = signal<FiltroSancion>('TODAS');

  /** Sanciones visibles según el filtro y la búsqueda (casa, tipo o descripción). */
  protected readonly sancionesFiltradas = computed(() => {
    const texto = this.busqueda().trim().toLowerCase();
    return this.sanciones()
      .filter((s) => this.filtroEstado() === 'TODAS' || s.estadoProceso === this.filtroEstado())
      .filter(
        (s) =>
          !texto ||
          `${nombreCasa(s.casaManzana, s.casaNumero)} ${textoTipoSancion(s.tipo)} ${s.descripcion}`
            .toLowerCase()
            .includes(texto),
      );
  });

  /** Cuántas sanciones hay de cada estado (para los botones del filtro). */
  protected readonly conteos = computed(() => {
    const conteo: Record<string, number> = {TODAS: this.sanciones().length};
    for (const estado of ESTADOS_SANCION) {
      conteo[estado] = this.sanciones().filter((s) => s.estadoProceso === estado).length;
    }
    return conteo;
  });

  /** Multa sugerida: valor base del concepto "Sanción"; null si no está en el catálogo. */
  protected readonly multaSugerida = computed(
    () =>
      this.conceptos().find((c) => c.nombre.trim().toLowerCase() === 'sanción')?.valorBase ?? null,
  );

  /** "5 sanciones" o "2 sanciones de 5". */
  protected readonly textoConteo = computed(() => {
    const visibles = this.sancionesFiltradas().length;
    const total = this.sanciones().length;
    const palabra = visibles === 1 ? 'sanción' : 'sanciones';
    return visibles === total ? `${visibles} ${palabra}` : `${visibles} ${palabra} de ${total}`;
  });

  /** Texto de cada estado de sanción (constante del modelo). */
  protected readonly etiquetaEstado = ETIQUETA_ESTADO_SANCION;
  /** Color de insignia de cada estado de sanción (constante del modelo). */
  protected readonly estilo = ESTILO_SANCION;

  /** Opciones del control segmentado: todas y cada estado. */
  protected readonly opcionesFiltro: FiltroSancion[] = ['TODAS', ...ESTADOS_SANCION];
  /** Sugerencias del campo tipo (constante del modelo). */
  protected readonly sugerenciasTipo = SUGERENCIAS_TIPO_SANCION;
  /** Función del modelo para mostrar el nombre de una casa en la plantilla. */
  protected readonly nombreCasa = nombreCasa;
  /** Función del modelo para armar la llave de una casa en la plantilla. */
  protected readonly llaveCasa = llaveCasa;
  /** Función del modelo para mostrar el tipo de una sanción. */
  protected readonly textoTipo = textoTipoSancion;
  /** Función del modelo que indica si una sanción no tiene descargos. */
  protected readonly sinDescargos = sinDescargos;
  /** Longitud máxima del tipo de sanción. */
  protected readonly maxTipo = MAX_TIPO;
  /** Longitud máxima de la descripción de la sanción. */
  protected readonly maxDescripcion = MAX_DESCRIPCION;
  /** Longitud máxima de los descargos. */
  protected readonly maxDescargos = MAX_DESCARGOS;

  /** Fecha de hoy (yyyy-MM-dd): tope de las fechas, porque el backend no acepta fechas futuras. */
  protected readonly hoy = hoyISO();

  /** Modal de crear. */
  protected readonly modalAbierto = signal(false);
  /** Valores actuales del formulario del modal. */
  protected readonly formulario = signal<FormularioSancion>({...FORMULARIO_VACIO});
  /** true mientras se envía el formulario al backend. */
  protected readonly guardando = signal(false);
  /** Mensaje de validación del formulario (null si no hay error). */
  protected readonly errorFormulario = signal<string | null>(null);

  /** Modal de proceso (descargos o confirmar). null = cerrado. */
  protected readonly proceso = signal<ProcesoSancion | null>(null);

  /** Valores del formulario del modal de proceso. */
  protected readonly formProceso = signal<FormularioProceso>({descargos: '', valor: ''});

  /** true mientras se envía el modal de proceso al backend. */
  protected readonly guardandoProceso = signal(false);

  /** Mensaje de validación del modal de proceso (null si no hay error). */
  protected readonly errorProceso = signal<string | null>(null);

  /** Acción (anular o eliminar) pendiente de confirmar. */
  protected readonly accion = signal<AccionSancion | null>(null);
  /** true mientras se envía la acción al backend. */
  protected readonly procesando = signal(false);

  /** Consulta los datos al abrir la pantalla. */
  ngOnInit(): void {
    this.cargarSanciones();
  }

  /**
   * Consulta sanciones, casas y conceptos a la vez. Si el catálogo de conceptos falla, la tabla se
   * muestra igual y la multa queda vacía para escribirla a mano.
   */
  cargarSanciones(): void {
    this.cargando.set(true);
    this.errorCarga.set(false);
    forkJoin({
      sanciones: this.sancionService.getTodos(),
      casas: this.casaService.getTodos(),
      conceptos: this.conceptoService.getTodos().pipe(catchError(() => of([]))),
    }).subscribe({
      next: ({ sanciones, casas, conceptos }) => {
        this.sanciones.set(sanciones);
        this.casas.set(casas);
        this.conceptos.set(conceptos);
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
  textoFiltro(opcion: FiltroSancion): string {
    return opcion === 'TODAS' ? 'Todas' : FILTRO_SANCION[opcion];
  }

  /** Abre el modal vacío para crear una sanción. */
  abrirCrear(): void {
    this.formulario.set({...FORMULARIO_VACIO});
    this.errorFormulario.set(null);
    this.modalAbierto.set(true);
  }

  /** Cierra el modal de crear sin guardar. */
  cerrarModal(): void {
    this.modalAbierto.set(false);
  }

  /**
   * Actualiza un campo del formulario de crear.
   * @param campo Nombre del campo.
   * @param valor Texto escrito.
   */
  cambiarCampo(campo: keyof FormularioSancion, valor: string): void {
    this.formulario.update((f) => ({...f, [campo]: valor}));
  }

  /** Valida lo mismo que el backend y crea la sanción (queda EN_DESCARGOS). */
  guardar(): void {
    const f = this.formulario();
    const tipo = f.tipo.trim();
    const descripcion = f.descripcion.trim();
    const descargos = f.descargos.trim();

    if (!f.casa) {
      this.errorFormulario.set('Elige la casa que se sanciona.');
      return;
    }
    if (!tipo) {
      this.errorFormulario.set('Indica el tipo de sanción.');
      return;
    }
    if (tipo.length > MAX_TIPO) {
      this.errorFormulario.set(`El tipo no puede superar ${MAX_TIPO} caracteres.`);
      return;
    }
    if (!f.fechaIncidente) {
      this.errorFormulario.set('Indica la fecha del incidente.');
      return;
    }
    if (f.fechaIncidente > this.hoy) {
      this.errorFormulario.set('La fecha del incidente no puede ser futura.');
      return;
    }
    if (!f.fechaNotificacion) {
      this.errorFormulario.set('Indica la fecha de notificación.');
      return;
    }
    if (f.fechaNotificacion < f.fechaIncidente) {
      this.errorFormulario.set('La notificación no puede ser anterior al incidente.');
      return;
    }
    if (f.fechaNotificacion > this.hoy) {
      this.errorFormulario.set('La fecha de notificación no puede ser futura.');
      return;
    }
    if (!descripcion) {
      this.errorFormulario.set('Describe el incidente.');
      return;
    }
    if (descripcion.length > MAX_DESCRIPCION) {
      this.errorFormulario.set(`La descripción no puede superar ${MAX_DESCRIPCION} caracteres.`);
      return;
    }
    if (descargos.length > MAX_DESCARGOS) {
      this.errorFormulario.set(`Los descargos no pueden superar ${MAX_DESCARGOS} caracteres.`);
      return;
    }

    const {manzana, numero} = separarLlaveCasa(f.casa);
    this.errorFormulario.set(null);
    this.guardando.set(true);

    this.sancionService
      .crear({
        fechaIncidente: f.fechaIncidente,
        fechaNotificacion: f.fechaNotificacion,
        tipo,
        descripcion,
        ...(descargos ? {descargos} : {}),
        casaNumero: numero,
        casaManzana: manzana,
      })
      .subscribe({
        next: () => {
          this.guardando.set(false);
          this.modalAbierto.set(false);
          this.notificacionService.exito('Sanción registrada.', 'Listo');
          this.cargarSanciones();
        },
        error: (err) => {
          this.guardando.set(false);
          this.errorFormulario.set(extraerError(err, 'No se pudo registrar la sanción.'));
        },
      });
  }

  /** Abre el modal para registrar (o corregir) los descargos de una sanción EN_DESCARGOS. */
  abrirDescargos(s: SancionModel): void {
    this.formProceso.set({descargos: sinDescargos(s) ? '' : s.descargos, valor: ''});
    this.errorProceso.set(null);
    this.proceso.set({modo: 'DESCARGOS', sancion: s});
  }

  /** Abre el modal para confirmar la sanción (con multa opcional). */
  abrirConfirmar(s: SancionModel): void {
    const multa = this.multaSugerida();
    this.formProceso.set({ descargos: '', valor: multa !== null ? String(multa) : '' });
    this.errorProceso.set(null);
    this.proceso.set({ modo: 'CONFIRMAR', sancion: s });
  }

  /** Cierra el modal de proceso sin guardar. */
  cerrarProceso(): void {
    this.proceso.set(null);
  }

  /**
   * Actualiza un campo del modal de proceso.
   * @param campo Nombre del campo.
   * @param valor Texto escrito.
   */
  cambiarCampoProceso(campo: keyof FormularioProceso, valor: string): void {
    this.formProceso.update((f) => ({...f, [campo]: valor}));
  }

  /**
   * Título del modal de proceso según el modo.
   * @param p Proceso abierto.
   * @returns Título del modal.
   */
  tituloProceso(p: ProcesoSancion): string {
    return p.modo === 'DESCARGOS' ? 'Registrar descargos' : 'Confirmar sanción';
  }

  /**
   * Texto del botón principal del modal de proceso.
   * @param p Proceso abierto.
   * @returns Texto del botón.
   */
  textoGuardarProceso(p: ProcesoSancion): string {
    return p.modo === 'DESCARGOS' ? 'Guardar descargos' : 'Confirmar sanción';
  }

  /**
   * Arma el PUT según el modo (descargos o confirmar), lo valida y lo envía. La multa solo acepta
   * dígitos, para que "150.000" no se interprete como 150.
   */
  guardarProceso(): void {
    const p = this.proceso();
    if (!p) return;
    const f = this.formProceso();
    let cambios: CambiosSancion;

    if (p.modo === 'DESCARGOS') {
      const descargos = f.descargos.trim();
      if (!descargos) {
        this.errorProceso.set('Escribe los descargos de la casa.');
        return;
      }
      if (descargos.length > MAX_DESCARGOS) {
        this.errorProceso.set(`Los descargos no pueden superar ${MAX_DESCARGOS} caracteres.`);
        return;
      }
      cambios = {descargos};
    } else {
      const texto = f.valor.trim();
      if (texto) {
        const valor = Number(texto);
        if (!/^\d+$/.test(texto) || valor <= 0) {
          this.errorProceso.set(
            'La multa debe ser un número entero mayor a 0 (sin puntos ni comas).',
          );
          return;
        }
        cambios = {estadoProceso: 'CONFIRMADA', valor};
      } else {
        cambios = {estadoProceso: 'CONFIRMADA'};
      }
    }

    this.errorProceso.set(null);
    this.guardandoProceso.set(true);

    this.sancionService.actualizar(p.sancion.idSancion, cambios).subscribe({
      next: () => {
        this.guardandoProceso.set(false);
        this.proceso.set(null);
        this.notificacionService.exito(
          p.modo === 'DESCARGOS' ? 'Descargos registrados.' : 'Sanción confirmada.',
          'Listo',
        );
        this.cargarSanciones();
      },
      error: (err) => {
        this.guardandoProceso.set(false);
        this.errorProceso.set(extraerError(err, 'No se pudo actualizar la sanción.'));
      },
    });
  }

  /**
   * Título del diálogo de confirmación según la acción.
   * @param a Acción pendiente.
   * @returns Pregunta del diálogo.
   */
  tituloAccion(a: AccionSancion): string {
    return a.tipo === 'ANULAR' ? '¿Anular la sanción?' : '¿Eliminar la sanción?';
  }

  /**
   * Mensaje del diálogo de confirmación según la acción.
   * @param a Acción pendiente.
   * @returns Explicación de lo que va a pasar.
   */
  mensajeAccion(a: AccionSancion): string {
    const s = a.sancion;
    const cual = `${textoTipoSancion(s.tipo)} de ${nombreCasa(s.casaManzana, s.casaNumero)} (incidente del ${s.fechaIncidente})`;
    return a.tipo === 'ANULAR'
      ? `Se anulará la sanción por ${cual}. Quedará sin multa y ya no se podrá modificar; solo podrás eliminarla.`
      : `Se eliminará la sanción anulada por ${cual}. Esta acción no se puede deshacer.`;
  }

  /**
   * Texto del botón de confirmar según la acción.
   * @param a Acción pendiente.
   * @returns Texto del botón.
   */
  textoConfirmarAccion(a: AccionSancion): string {
    return a.tipo === 'ANULAR' ? 'Anular sanción' : 'Eliminar';
  }

  /** Ejecuta la acción confirmada en el diálogo. */
  confirmarAccion(): void {
    const a = this.accion();
    if (!a) return;

    this.procesando.set(true);
    const peticion =
      a.tipo === 'ELIMINAR'
        ? this.sancionService.eliminar(a.sancion.idSancion)
        : this.sancionService.actualizar(a.sancion.idSancion, {estadoProceso: 'ANULADA'});

    peticion.subscribe({
      next: () => {
        this.procesando.set(false);
        this.accion.set(null);
        this.notificacionService.exito(
          a.tipo === 'ANULAR' ? 'Sanción anulada.' : 'Sanción eliminada.',
          'Listo',
        );
        this.cargarSanciones();
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

  /** Texto mostrado cuando aún no hay descargos (por si la plantilla lo necesita). */
  protected readonly textoSinDescargos = TEXTO_SIN_DESCARGOS;
}
