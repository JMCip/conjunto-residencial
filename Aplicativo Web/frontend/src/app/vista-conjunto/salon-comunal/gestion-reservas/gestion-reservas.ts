import {Component, computed, inject, OnInit, signal} from '@angular/core';
import {CurrencyPipe, DatePipe, DecimalPipe} from '@angular/common';
import {ConceptoCobroModel} from '../../../models/concepto-cobro.model';
import {ConceptoCobroService} from '../../../services/concepto-cobro.service';
import { catchError, forkJoin, of } from 'rxjs';

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

import {ReservaSalonService} from '../../../services/reserva-salon.service';
import {CasaService} from '../../../services/casa.service';
import {NotificacionService} from '../../../services/notificacion.service';
import {extraerError} from '../../../compartido/extraer-error';
import {ModalFormulario} from '../../../compartido/modal-formulario/modal-formulario';
import {DialogoConfirmar} from '../../../compartido/dialogo-confirmar/dialogo-confirmar';

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

/** Nombre del concepto del catálogo con la tarifa por hora del salón (en minúsculas). */
const CONCEPTO_TARIFA = 'salón comunal';

/** Nombre del concepto del catálogo con el depósito sugerido (en minúsculas). */
const CONCEPTO_DEPOSITO = 'depósito salón comunal';

/**
 * Busca un concepto por nombre sin importar mayúsculas ni espacios.
 * @param lista Conceptos del catálogo.
 * @param nombre Nombre en minúsculas.
 */
function buscarConcepto(lista: ConceptoCobroModel[], nombre: string): ConceptoCobroModel | null {
  return lista.find((c) => c.nombre.trim().toLowerCase() === nombre) ?? null;
}

/** Valores iniciales del formulario (todo vacío). */
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
  /** Servicio de reservas del salón comunal. */
  private reservaService = inject(ReservaSalonService);
  /** Servicio de casas. */
  private casaService = inject(CasaService);
  /** Servicio de avisos flotantes. */
  private notificacionService = inject(NotificacionService);
  /** Servicio del catálogo de conceptos de cobro. */
  private conceptoService = inject(ConceptoCobroService);

  /** Lista de reservas consultada al backend. */
  protected readonly reservas = signal<ReservaSalonModel[]>([]);
  /** Lista de casas consultada al backend. */
  protected readonly casas = signal<CasaModel[]>([]);
  /** true mientras se consulta la información. */
  protected readonly cargando = signal(true);
  /** true si la consulta de la información falló. */
  protected readonly errorCarga = signal(false);
  /** Filtro de estado seleccionado en el control segmentado. */
  protected readonly filtroEstado = signal<FiltroReserva>('TODAS');
  /** Lista de conceptos consultada al backend. */
  protected readonly conceptos = signal<ConceptoCobroModel[]>([]);

  /** Tarifa vigente por hora (concepto "Salón comunal"); null si no está en el catálogo. */
  protected readonly tarifaHora = computed(
    () => buscarConcepto(this.conceptos(), CONCEPTO_TARIFA)?.valorBase ?? null,
  );

  /** Depósito sugerido (concepto "Depósito salón comunal"); null si no existe. */
  protected readonly depositoSugerido = computed(
    () => buscarConcepto(this.conceptos(), CONCEPTO_DEPOSITO)?.valorBase ?? null,
  );

  /** Reservas visibles según el filtro de estado, de la más reciente a la más antigua. */
  protected readonly reservasFiltradas = computed(() =>
    this.reservas()
      .filter((r) => this.filtroEstado() === 'TODAS' || r.estado === this.filtroEstado())
      .sort((a, b) => b.horaInicio.localeCompare(a.horaInicio)),
  );

  /** Cantidad de registros de cada estado, para los botones del filtro. */
  protected readonly conteos = computed(() => {
    const conteo: Record<string, number> = {TODAS: this.reservas().length};
    for (const estado of ESTADOS_RESERVA) {
      conteo[estado] = this.reservas().filter((r) => r.estado === estado).length;
    }
    return conteo;
  });

  /** Texto del pie de la tabla con la cantidad de registros visibles. */
  protected readonly textoConteo = computed(() => {
    const visibles = this.reservasFiltradas().length;
    const total = this.reservas().length;
    const palabra = visibles === 1 ? 'reserva' : 'reservas';
    return visibles === total ? `${visibles} ${palabra}` : `${visibles} ${palabra} de ${total}`;
  });

  /** Texto de cada estado de reserva (constante del modelo). */
  protected readonly etiquetaReserva = ETIQUETA_RESERVA;
  /** Color de insignia de cada estado de reserva (constante del modelo). */
  protected readonly estiloReserva = ESTILO_RESERVA;

  /** Opciones del control segmentado: todas y cada estado. */
  protected readonly opcionesFiltro: FiltroReserva[] = ['TODAS', ...ESTADOS_RESERVA];
  /** Función del modelo para mostrar el nombre de una casa en la plantilla. */
  protected readonly nombreCasa = nombreCasa;
  /** Función del modelo para armar la llave de una casa en la plantilla. */
  protected readonly llaveCasa = llaveCasa;

  /** Modal de crear. */
  protected readonly modalAbierto = signal(false);
  /** Valores actuales del formulario del modal. */
  protected readonly formulario = signal<FormularioReserva>({...FORMULARIO_VACIO});
  /** true mientras se envía el formulario al backend. */
  protected readonly guardando = signal(false);
  /** Mensaje de validación del formulario (null si no hay error). */
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
    return {horas, total};
  });

  /** Acción (cancelar, finalizar, retener o eliminar) pendiente de confirmar. */
  protected readonly accion = signal<AccionReserva | null>(null);
  /** true mientras se envía la acción al backend. */
  protected readonly procesando = signal(false);

  /** Consulta los datos al abrir la pantalla. */
  ngOnInit(): void {
    this.cargarReservas();
  }

  /**
   * Consulta reservas, casas y conceptos a la vez. Si el catálogo de conceptos falla, la tabla se
   * muestra igual y la tarifa queda editable para escribirla a mano.
   */
  cargarReservas(): void {
    this.cargando.set(true);
    this.errorCarga.set(false);
    forkJoin({
      reservas: this.reservaService.getTodos(),
      casas: this.casaService.getTodos(),
      conceptos: this.conceptoService.getTodos().pipe(catchError(() => of([]))),
    }).subscribe({
      next: ({ reservas, casas, conceptos }) => {
        this.reservas.set(reservas);
        this.casas.set(casas);
        this.conceptos.set(conceptos);
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
   * Texto de un botón del filtro.
   * @param opcion Valor del filtro.
   * @returns "Todas" o el estado en plural.
   */
  textoFiltro(opcion: FiltroReserva): string {
    return opcion === 'TODAS' ? 'Todas' : FILTRO_RESERVA[opcion];
  }

  /** "14:00 – 20:00" a partir de dos TIMESTAMP ("2026-09-12 14:00:00"). */
  franja(r: ReservaSalonModel): string {
    return `${r.horaInicio.slice(11, 16)} – ${r.horaFinal.slice(11, 16)}`;
  }

  /**
   * Indica si la reserva ya empezó: el backend solo permite finalizarla o retener el depósito desde
   * ese momento.
   * @param r Reserva.
   * @returns true si la hora de inicio ya pasó.
   */
  yaComenzo(r: ReservaSalonModel): boolean {
    return new Date(r.horaInicio.replace(' ', 'T')).getTime() <= Date.now();
  }

  /**
   * Abre el modal de crear con la tarifa y el depósito del catálogo ya propuestos. Si no existe el
   * concepto del depósito, se propone 0 porque el campo es obligatorio en la base de datos.
   */
  abrirCrear(): void {
    const tarifa = this.tarifaHora();
    const deposito = this.depositoSugerido();
    this.formulario.set({
      ...FORMULARIO_VACIO,
      valorPorHora: tarifa !== null ? String(tarifa) : '',
      valorDeposito: deposito !== null ? String(deposito) : '0',
    });
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
   * @param valor Texto escrito.
   */
  cambiarCampo(campo: keyof FormularioReserva, valor: string): void {
    this.formulario.update((f) => ({...f, [campo]: valor}));
  }

  /**
   * Valida el formulario y crea la reserva. El backend revisa además la mora, las sanciones
   * pendientes y los cruces de horario.
   */
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

    const valorPorHora = this.tarifaHora() ?? Number(f.valorPorHora);

    if (!f.valorPorHora.trim() || !Number.isFinite(valorPorHora) || valorPorHora <= 0) {
      this.errorFormulario.set('El valor por hora debe ser un número mayor a 0 (sin puntos ni comas).');
      return;
    }
    const valorDeposito = Number(f.valorDeposito);
    if (!f.valorDeposito.trim() || !Number.isFinite(valorDeposito) || valorDeposito < 0) {
      this.errorFormulario.set('El depósito debe ser un número igual o mayor a 0 (sin puntos ni comas).');
      return;
    }

    const {manzana, numero} = separarLlaveCasa(f.casa);
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

  /**
   * Título del diálogo de confirmación según la acción.
   * @param a Acción pendiente.
   * @returns Pregunta del diálogo.
   */
  tituloAccion(a: AccionReserva): string {
    switch (a.tipo) {
      case 'CANCELADA':
        return '¿Cancelar la reserva?';
      case 'FINALIZADA':
        return '¿Finalizar la reserva?';
      case 'DEPOSITO_RETENIDO':
        return '¿Retener el depósito?';
      case 'ELIMINAR':
        return '¿Eliminar la reserva?';
    }
  }

  /**
   * Mensaje del diálogo de confirmación según la acción.
   * @param a Acción pendiente.
   * @returns Explicación de lo que va a pasar.
   */
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

  /**
   * Texto del botón de confirmar según la acción.
   * @param a Acción pendiente.
   * @returns Texto del botón.
   */
  textoConfirmarAccion(a: AccionReserva): string {
    switch (a.tipo) {
      case 'CANCELADA':
        return 'Cancelar reserva';
      case 'FINALIZADA':
        return 'Finalizar';
      case 'DEPOSITO_RETENIDO':
        return 'Retener depósito';
      case 'ELIMINAR':
        return 'Eliminar';
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
