import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { forkJoin } from 'rxjs';
import { VisitaModel } from '../../../models/visita.model';
import { CasaModel, nombreCasa } from '../../../models/casa.model';
import {
  llavePersona,
  nombrePersona,
  PersonaModel,
  separarLlavePersona,
} from '../../../models/persona.model';
import { VisitaService } from '../../../services/visita.service';
import { CasaService } from '../../../services/casa.service';
import { PersonaService } from '../../../services/persona.service';
import { NotificacionService } from '../../../services/notificacion.service';
import { extraerError } from '../../../compartido/extraer-error';
import { ModalFormulario } from '../../../compartido/modal-formulario/modal-formulario';
import { DialogoConfirmar } from '../../../compartido/dialogo-confirmar/dialogo-confirmar';

/** Campos del formulario (todo como texto). */
interface FormularioVisita {
  casa: string;
  visitante: string;
  fechaEntrada: string;
  fechaSalida: string;
}

/** Valores iniciales del formulario (todo vacío). */
const FORMULARIO_VACIO: FormularioVisita = {
  casa: '',
  visitante: '',
  fechaEntrada: '',
  fechaSalida: '',
};

/**
 * @component GestionVisitas
 * @description Pestaña Visitas: qué visitante fue a qué casa, con fecha de
 * entrada y de salida. El visitante sale de GET /api/visitantes.
 */
@Component({
  selector: 'app-gestion-visitas',
  imports: [DatePipe, ModalFormulario, DialogoConfirmar],
  templateUrl: './gestion-visitas.html',
  styleUrl: './gestion-visitas.css',
})
export class GestionVisitas implements OnInit {
  /** Servicio de visitas. */
  private visitaService = inject(VisitaService);
  /** Servicio de casas. */
  private casaService = inject(CasaService);
  /** Servicio de personas y roles. */
  private personaService = inject(PersonaService);
  /** Servicio de avisos flotantes. */
  private notificacionService = inject(NotificacionService);

  /** Visitas obtenidas del backend. */
  protected readonly visitas = signal<VisitaModel[]>([]);

  /** Casas y visitantes para los selectores. */
  protected readonly casas = signal<CasaModel[]>([]);
  /** Lista de visitantes consultada al backend. */
  protected readonly visitantes = signal<PersonaModel[]>([]);

  /** Nombre de cada visitante por su llave "CC|123". */
  private readonly nombres = computed(
    () =>
      new Map(
        this.visitantes().map((p) => [
          llavePersona(p.tipoDocumento, p.numeroDocumento),
          nombrePersona(p),
        ]),
      ),
  );

  /** true mientras se consulta la información. */
  protected readonly cargando = signal(true);
  /** true si la consulta de la información falló. */
  protected readonly errorCarga = signal(false);
  /** Texto escrito en el buscador. */
  protected readonly busqueda = signal('');

  /** Visitas visibles según la búsqueda (casa o visitante). */
  protected readonly visitasFiltradas = computed(() => {
    const texto = this.busqueda().trim().toLowerCase();
    if (!texto) return this.visitas();
    return this.visitas().filter((r) =>
      `${nombreCasa(r.casaManzana, r.casaNumero)} ${this.visitante(r)}`
        .toLowerCase()
        .includes(texto),
    );
  });

  /** "10 visitas" o "2 visitas de 10". */
  protected readonly textoConteo = computed(() => {
    const visibles = this.visitasFiltradas().length;
    const total = this.visitas().length;
    const palabra = visibles === 1 ? 'visita' : 'visitas';
    return visibles === total ? `${visibles} ${palabra}` : `${visibles} ${palabra} de ${total}`;
  });

  /** Función del modelo para mostrar el nombre de una casa en la plantilla. */
  protected readonly nombreCasa = nombreCasa;
  /** Función del modelo para armar la llave de una persona en la plantilla. */
  protected readonly llavePersona = llavePersona;
  /** Función del modelo para mostrar el nombre de una persona en la plantilla. */
  protected readonly nombrePersona = nombrePersona;

  /** Modal de crear/editar. */
  protected readonly modalAbierto = signal(false);

  /** Visita que se edita (null = crear). */
  protected readonly editando = signal<VisitaModel | null>(null);

  /** Valores actuales del formulario del modal. */
  protected readonly formulario = signal<FormularioVisita>({ ...FORMULARIO_VACIO });
  /** true mientras se envía el formulario al backend. */
  protected readonly guardando = signal(false);
  /** Mensaje de validación del formulario (null si no hay error). */
  protected readonly errorFormulario = signal<string | null>(null);

  /** Visita pendiente de confirmar para eliminar. */
  protected readonly visitaAEliminar = signal<VisitaModel | null>(null);
  /** true mientras se envía la eliminación al backend. */
  protected readonly eliminando = signal(false);

  /** Consulta los datos al abrir la pantalla. */
  ngOnInit(): void {
    this.cargarVisitas();
  }

  /** Consulta visitas, casas y visitantes a la vez. */
  cargarVisitas(): void {
    this.cargando.set(true);
    this.errorCarga.set(false);
    forkJoin({
      visitas: this.visitaService.getTodos(),
      casas: this.casaService.getTodos(),
      visitantes: this.personaService.getVisitantes(),
    }).subscribe({
      next: ({ visitas, casas, visitantes }) => {
        this.visitas.set(visitas);
        this.casas.set(casas);
        this.visitantes.set(visitantes);
        this.cargando.set(false);
      },
      error: (err) => {
        this.errorCarga.set(true);
        this.cargando.set(false);
        this.notificacionService.error(
          extraerError(err, 'No se pudieron consultar las visitas.'),
          'Error de conexión',
        );
      },
    });
  }

  /**
   * Nombre del visitante (o su documento si no está en la lista).
   * @param r Visita de la tabla.
   */
  visitante(r: VisitaModel): string {
    return (
      this.nombres().get(llavePersona(r.visitanteTipoDocumento, r.visitanteNumeroDocumento)) ??
      `${r.visitanteTipoDocumento} ${r.visitanteNumeroDocumento}`
    );
  }

  /**
   * Llave "manzana|numero" de una casa (para el selector).
   * @param manzana Manzana de la casa.
   * @param numero Número de la casa.
   */
  llaveCasa(manzana: number, numero: number): string {
    return `${manzana}|${numero}`;
  }

  /** Abre el modal vacío para crear. */
  abrirCrear(): void {
    this.editando.set(null);
    this.formulario.set({ ...FORMULARIO_VACIO });
    this.errorFormulario.set(null);
    this.modalAbierto.set(true);
  }

  /**
   * Abre el modal con los datos de la visita.
   * @param r Visita a editar.
   */
  abrirEditar(r: VisitaModel): void {
    this.editando.set(r);
    this.formulario.set({
      casa: this.llaveCasa(r.casaManzana, r.casaNumero),
      visitante: llavePersona(r.visitanteTipoDocumento, r.visitanteNumeroDocumento),
      fechaEntrada: r.fechaEntrada,
      fechaSalida: r.fechaSalida ?? '',
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
   * @param valor Valor escrito o elegido.
   */
  cambiarCampo(campo: keyof FormularioVisita, valor: string): void {
    this.formulario.update((f) => ({ ...f, [campo]: valor }));
  }

  /** Valida y guarda (crear o editar). */
  guardar(): void {
    const f = this.formulario();
    if (!f.casa || !f.visitante) {
      this.errorFormulario.set('Elige la casa y el visitante.');
      return;
    }
    if (!f.fechaEntrada) {
      this.errorFormulario.set('La fecha de entrada es obligatoria.');
      return;
    }
    if (f.fechaSalida && f.fechaSalida < f.fechaEntrada) {
      this.errorFormulario.set('La fecha de salida no puede ser anterior a la de entrada.');
      return;
    }

    const fechaSalida = f.fechaSalida || null;
    const actual = this.editando();
    this.errorFormulario.set(null);
    this.guardando.set(true);

    let peticion;
    if (actual) {
      peticion = this.visitaService.actualizar({
        ...actual,
        fechaEntrada: f.fechaEntrada,
        fechaSalida,
      });
    } else {
      const [manzana, numero] = f.casa.split('|').map(Number);
      const { tipo, numero: documento } = separarLlavePersona(f.visitante);
      peticion = this.visitaService.crear({
        fechaEntrada: f.fechaEntrada,
        fechaSalida,
        visitanteTipoDocumento: tipo,
        visitanteNumeroDocumento: documento,
        casaNumero: numero,
        casaManzana: manzana,
      });
    }

    peticion.subscribe({
      next: () => {
        this.guardando.set(false);
        this.modalAbierto.set(false);
        this.notificacionService.exito(
          actual ? 'Visita actualizada.' : 'Visita creada.',
          'Listo',
        );
        this.cargarVisitas();
      },
      error: (err) => {
        this.guardando.set(false);
        this.errorFormulario.set(extraerError(err, 'No se pudo guardar la visita.'));
      },
    });
  }

  /** Confirma y elimina la visita. */
  eliminar(): void {
    const v = this.visitaAEliminar();
    if (!v) return;
    this.eliminando.set(true);
    this.visitaService.eliminar(v.idVisita).subscribe({
      next: () => {
        this.eliminando.set(false);
        this.visitaAEliminar.set(null);
        this.notificacionService.exito('Visita eliminada.', 'Listo');
        this.cargarVisitas();
      },
      error: (err) => {
        this.eliminando.set(false);
        this.visitaAEliminar.set(null);
        this.notificacionService.error(
          extraerError(err, 'No se pudo eliminar la visita.'),
          'No se pudo eliminar',
        );
      },
    });
  }
}
