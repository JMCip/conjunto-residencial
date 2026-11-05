import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { CurrencyPipe, DatePipe } from '@angular/common';
import { forkJoin } from 'rxjs';
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
import { SancionService } from '../../../services/sancion.service';
import { CasaService } from '../../../services/casa.service';
import { NotificacionService } from '../../../services/notificacion.service';
import { extraerError } from '../../../compartido/extraer-error';
import { ModalFormulario } from '../../../compartido/modal-formulario/modal-formulario';
import { DialogoConfirmar } from '../../../compartido/dialogo-confirmar/dialogo-confirmar';

/** Largos máximos que acepta el backend. */
const MAX_TIPO = 30;
const MAX_DESCRIPCION = 255;
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
  protected readonly etiquetaEstado = ETIQUETA_ESTADO_SANCION;
  protected readonly estilo = ESTILO_SANCION;
  protected readonly opcionesFiltro: FiltroSancion[] = ['TODAS', ...ESTADOS_SANCION];
  protected readonly sugerenciasTipo = SUGERENCIAS_TIPO_SANCION;
  protected readonly nombreCasa = nombreCasa;
  protected readonly llaveCasa = llaveCasa;
  protected readonly textoTipo = textoTipoSancion;
  protected readonly sinDescargos = sinDescargos;
  protected readonly maxTipo = MAX_TIPO;
  protected readonly maxDescripcion = MAX_DESCRIPCION;
  protected readonly maxDescargos = MAX_DESCARGOS;

  /** Hoy en formato yyyy-MM-dd: tope de las fechas (el back no acepta fechas futuras). */
  protected readonly hoy = hoyISO();

  /** Modal de crear. */
  protected readonly modalAbierto = signal(false);
  protected readonly formulario = signal<FormularioSancion>({ ...FORMULARIO_VACIO });
  protected readonly guardando = signal(false);
  protected readonly errorFormulario = signal<string | null>(null);

  /** Modal de proceso (descargos o confirmar). null = cerrado. */
  protected readonly proceso = signal<ProcesoSancion | null>(null);
  protected readonly formProceso = signal<FormularioProceso>({ descargos: '', valor: '' });
  protected readonly guardandoProceso = signal(false);
  protected readonly errorProceso = signal<string | null>(null);

  /** Acción (anular o eliminar) pendiente de confirmar. */
  protected readonly accion = signal<AccionSancion | null>(null);
  protected readonly procesando = signal(false);

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
  textoFiltro(opcion: FiltroSancion): string {
    return opcion === 'TODAS' ? 'Todas' : FILTRO_SANCION[opcion];
  }



  abrirCrear(): void {
    this.formulario.set({ ...FORMULARIO_VACIO });
    this.errorFormulario.set(null);
    this.modalAbierto.set(true);
  }

  cerrarModal(): void {
    this.modalAbierto.set(false);
  }

  cambiarCampo(campo: keyof FormularioSancion, valor: string): void {
    this.formulario.update((f) => ({ ...f, [campo]: valor }));
  }

  /** Valida lo mismo que el back y crea la sanción (queda EN_DESCARGOS). */
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

    const { manzana, numero } = separarLlaveCasa(f.casa);
    this.errorFormulario.set(null);
    this.guardando.set(true);

    this.sancionService
      .crear({
        fechaIncidente: f.fechaIncidente,
        fechaNotificacion: f.fechaNotificacion,
        tipo,
        descripcion,
        ...(descargos ? { descargos } : {}),
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
    this.formProceso.set({ descargos: sinDescargos(s) ? '' : s.descargos, valor: '' });
    this.errorProceso.set(null);
    this.proceso.set({ modo: 'DESCARGOS', sancion: s });
  }

  /** Abre el modal para confirmar la sanción (con multa opcional). */
  abrirConfirmar(s: SancionModel): void {
    this.formProceso.set({ descargos: '', valor: '' });
    this.errorProceso.set(null);
    this.proceso.set({ modo: 'CONFIRMAR', sancion: s });
  }

  cerrarProceso(): void {
    this.proceso.set(null);
  }

  cambiarCampoProceso(campo: keyof FormularioProceso, valor: string): void {
    this.formProceso.update((f) => ({ ...f, [campo]: valor }));
  }

  tituloProceso(p: ProcesoSancion): string {
    return p.modo === 'DESCARGOS' ? 'Registrar descargos' : 'Confirmar sanción';
  }

  textoGuardarProceso(p: ProcesoSancion): string {
    return p.modo === 'DESCARGOS' ? 'Guardar descargos' : 'Confirmar sanción';
  }

  /** Arma el PUT según el modo, valida y lo envía. */
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
      cambios = { descargos };
    } else {
      const texto = f.valor.trim();
      if (texto) {
        // Solo dígitos: así se evita que "150.000" se lea como 150.
        const valor = Number(texto);
        if (!/^\d+$/.test(texto) || valor <= 0) {
          this.errorProceso.set(
            'La multa debe ser un número entero mayor a 0 (sin puntos ni comas).',
          );
          return;
        }
        cambios = { estadoProceso: 'CONFIRMADA', valor };
      } else {
        cambios = { estadoProceso: 'CONFIRMADA' };
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


  tituloAccion(a: AccionSancion): string {
    return a.tipo === 'ANULAR' ? '¿Anular la sanción?' : '¿Eliminar la sanción?';
  }

  mensajeAccion(a: AccionSancion): string {
    const s = a.sancion;
    const cual = `${textoTipoSancion(s.tipo)} de ${nombreCasa(s.casaManzana, s.casaNumero)} (incidente del ${s.fechaIncidente})`;
    return a.tipo === 'ANULAR'
      ? `Se anulará la sanción por ${cual}. Quedará sin multa y ya no se podrá modificar; solo podrás eliminarla.`
      : `Se eliminará la sanción anulada por ${cual}. Esta acción no se puede deshacer.`;
  }

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
        : this.sancionService.actualizar(a.sancion.idSancion, { estadoProceso: 'ANULADA' });

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
