import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { forkJoin } from 'rxjs';
import { ResidenciaModel } from '../../../models/residencia.model';
import { CasaModel, nombreCasa } from '../../../models/casa.model';
import {
  llavePersona,
  nombrePersona,
  PersonaModel,
  separarLlavePersona,
} from '../../../models/persona.model';
import { ResidenciaService } from '../../../services/residencia.service';
import { CasaService } from '../../../services/casa.service';
import { PersonaService } from '../../../services/persona.service';
import { NotificacionService } from '../../../services/notificacion.service';
import { extraerError } from '../../../compartido/extraer-error';
import { ModalFormulario } from '../../../compartido/modal-formulario/modal-formulario';
import { DialogoConfirmar } from '../../../compartido/dialogo-confirmar/dialogo-confirmar';

/** Campos del formulario (todo como texto). */
interface FormularioResidencia {
  casa: string;
  residente: string;
  fechaIngreso: string;
  fechaSalida: string;
}

/** Valores iniciales del formulario (todo vacío). */
const FORMULARIO_VACIO: FormularioResidencia = {
  casa: '',
  residente: '',
  fechaIngreso: '',
  fechaSalida: '',
};

/**
 * @component GestionResidencias
 * @description Pestaña Residencias: en qué casa vive (o vivió) cada residente, con fecha de
 * ingreso y de salida. El residente sale de GET /api/residentes.
 */
@Component({
  selector: 'app-gestion-residencias',
  imports: [DatePipe, ModalFormulario, DialogoConfirmar],
  templateUrl: './gestion-residencias.html',
  styleUrl: './gestion-residencias.css',
})
export class GestionResidencias implements OnInit {
  /** Servicio de residencias. */
  private residenciaService = inject(ResidenciaService);
  /** Servicio de casas. */
  private casaService = inject(CasaService);
  /** Servicio de personas y roles. */
  private personaService = inject(PersonaService);
  /** Servicio de avisos flotantes. */
  private notificacionService = inject(NotificacionService);

  /** Residencias obtenidas del backend. */
  protected readonly residencias = signal<ResidenciaModel[]>([]);

  /** Casas y residentes para los selectores. */
  protected readonly casas = signal<CasaModel[]>([]);
  /** Lista de residentes consultada al backend. */
  protected readonly residentes = signal<PersonaModel[]>([]);

  /** Nombre de cada residente por su llave "CC|123". */
  private readonly nombres = computed(
    () =>
      new Map(
        this.residentes().map((p) => [
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

  /** Residencias visibles según la búsqueda (casa o residente). */
  protected readonly residenciasFiltradas = computed(() => {
    const texto = this.busqueda().trim().toLowerCase();
    if (!texto) return this.residencias();
    return this.residencias().filter((r) =>
      `${nombreCasa(r.casaManzana, r.casaNumero)} ${this.residente(r)}`
        .toLowerCase()
        .includes(texto),
    );
  });

  /** "10 residencias" o "2 residencias de 10". */
  protected readonly textoConteo = computed(() => {
    const visibles = this.residenciasFiltradas().length;
    const total = this.residencias().length;
    const palabra = visibles === 1 ? 'residencia' : 'residencias';
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

  /** Residencia que se edita (null = crear). */
  protected readonly editando = signal<ResidenciaModel | null>(null);

  /** Valores actuales del formulario del modal. */
  protected readonly formulario = signal<FormularioResidencia>({ ...FORMULARIO_VACIO });
  /** true mientras se envía el formulario al backend. */
  protected readonly guardando = signal(false);
  /** Mensaje de validación del formulario (null si no hay error). */
  protected readonly errorFormulario = signal<string | null>(null);

  /** Residencia pendiente de confirmar para eliminar. */
  protected readonly residenciaAEliminar = signal<ResidenciaModel | null>(null);
  /** true mientras se envía la eliminación al backend. */
  protected readonly eliminando = signal(false);

  /** Consulta los datos al abrir la pantalla. */
  ngOnInit(): void {
    this.cargarResidencias();
  }

  /** Consulta residencias, casas y residentes a la vez. */
  cargarResidencias(): void {
    this.cargando.set(true);
    this.errorCarga.set(false);
    forkJoin({
      residencias: this.residenciaService.getTodos(),
      casas: this.casaService.getTodos(),
      residentes: this.personaService.getResidentes(),
    }).subscribe({
      next: ({ residencias, casas, residentes }) => {
        this.residencias.set(residencias);
        this.casas.set(casas);
        this.residentes.set(residentes);
        this.cargando.set(false);
      },
      error: (err) => {
        this.errorCarga.set(true);
        this.cargando.set(false);
        this.notificacionService.error(
          extraerError(err, 'No se pudieron consultar las residencias.'),
          'Error de conexión',
        );
      },
    });
  }

  /**
   * Nombre del residente (o su documento si no está en la lista).
   * @param r Residencia de la tabla.
   */
  residente(r: ResidenciaModel): string {
    return (
      this.nombres().get(llavePersona(r.residenteTipoDocumento, r.residenteNumeroDocumento)) ??
      `${r.residenteTipoDocumento} ${r.residenteNumeroDocumento}`
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
   * Abre el modal con los datos de la residencia.
   * @param r Residencia a editar.
   */
  abrirEditar(r: ResidenciaModel): void {
    this.editando.set(r);
    this.formulario.set({
      casa: this.llaveCasa(r.casaManzana, r.casaNumero),
      residente: llavePersona(r.residenteTipoDocumento, r.residenteNumeroDocumento),
      fechaIngreso: r.fechaIngreso,
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
  cambiarCampo(campo: keyof FormularioResidencia, valor: string): void {
    this.formulario.update((f) => ({ ...f, [campo]: valor }));
  }

  /** Valida y guarda (crear o editar). */
  guardar(): void {
    const f = this.formulario();
    if (!f.casa || !f.residente) {
      this.errorFormulario.set('Elige la casa y el residente.');
      return;
    }
    if (!f.fechaIngreso) {
      this.errorFormulario.set('La fecha de ingreso es obligatoria.');
      return;
    }
    if (f.fechaSalida && f.fechaSalida < f.fechaIngreso) {
      this.errorFormulario.set('La fecha de salida no puede ser anterior a la de ingreso.');
      return;
    }

    const fechaSalida = f.fechaSalida || null;
    const actual = this.editando();
    this.errorFormulario.set(null);
    this.guardando.set(true);

    let peticion;
    if (actual) {
      peticion = this.residenciaService.actualizar({
        ...actual,
        fechaIngreso: f.fechaIngreso,
        fechaSalida,
      });
    } else {
      const [manzana, numero] = f.casa.split('|').map(Number);
      const { tipo, numero: documento } = separarLlavePersona(f.residente);
      peticion = this.residenciaService.crear({
        fechaIngreso: f.fechaIngreso,
        fechaSalida,
        residenteTipoDocumento: tipo,
        residenteNumeroDocumento: documento,
        casaNumero: numero,
        casaManzana: manzana,
      });
    }

    peticion.subscribe({
      next: () => {
        this.guardando.set(false);
        this.modalAbierto.set(false);
        this.notificacionService.exito(
          actual ? 'Residencia actualizada.' : 'Residencia creada.',
          'Listo',
        );
        this.cargarResidencias();
      },
      error: (err) => {
        this.guardando.set(false);
        this.errorFormulario.set(extraerError(err, 'No se pudo guardar la residencia.'));
      },
    });
  }

  /** Confirma y elimina la residencia. */
  eliminar(): void {
    const r = this.residenciaAEliminar();
    if (!r) return;
    this.eliminando.set(true);
    this.residenciaService.eliminar(r.idResidencia).subscribe({
      next: () => {
        this.eliminando.set(false);
        this.residenciaAEliminar.set(null);
        this.notificacionService.exito('Residencia eliminada.', 'Listo');
        this.cargarResidencias();
      },
      error: (err) => {
        this.eliminando.set(false);
        this.residenciaAEliminar.set(null);
        this.notificacionService.error(
          extraerError(err, 'No se pudo eliminar la residencia.'),
          'No se pudo eliminar',
        );
      },
    });
  }
}
