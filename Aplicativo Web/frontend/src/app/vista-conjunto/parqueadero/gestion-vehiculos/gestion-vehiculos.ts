import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { forkJoin } from 'rxjs';
import {
  ETIQUETA_TIPO_VEHICULO,
  TIPOS_VEHICULO,
  VehiculoModel,
} from '../../../models/vehiculo.model';
import {
  llavePersona,
  nombrePersona,
  PersonaModel,
  separarLlavePersona,
} from '../../../models/persona.model';
import { VehiculoService } from '../../../services/vehiculo.service';
import { PersonaService } from '../../../services/persona.service';
import { NotificacionService } from '../../../services/notificacion.service';
import { extraerError } from '../../../compartido/extraer-error';
import { ModalFormulario } from '../../../compartido/modal-formulario/modal-formulario';
import { DialogoConfirmar } from '../../../compartido/dialogo-confirmar/dialogo-confirmar';

/** Campos del formulario (todo como texto; los vacíos se pasan a null al guardar). */
interface FormularioVehiculo {
  placa: string;
  tipo: string;
  marca: string;
  modelo: string;
  persona: string;
}

/** Valores iniciales del formulario (todo vacío). */
const FORMULARIO_VACIO: FormularioVehiculo = {
  placa: '',
  tipo: 'CARRO',
  marca: '',
  modelo: '',
  persona: '',
};

/**
 * @component GestionVehiculos
 * @description Pestaña Vehículos de Parqueadero: CRUD de los carros y motos de cada persona.
 * La placa es la llave: no se cambia al editar (los parqueos la usan).
 */
@Component({
  selector: 'app-gestion-vehiculos',
  imports: [ModalFormulario, DialogoConfirmar],
  templateUrl: './gestion-vehiculos.html',
  styleUrl: './gestion-vehiculos.css',
})
export class GestionVehiculos implements OnInit {
  /** Servicio de vehículos. */
  private vehiculoService = inject(VehiculoService);
  /** Servicio de personas y roles. */
  private personaService = inject(PersonaService);
  /** Servicio de avisos flotantes. */
  private notificacionService = inject(NotificacionService);

  /** Vehículos obtenidos del backend. */
  protected readonly vehiculos = signal<VehiculoModel[]>([]);

  /** Personas, para el selector de dueño y para mostrar su nombre. */
  protected readonly personas = signal<PersonaModel[]>([]);

  /** Nombre de cada persona por su llave "CC|123". */
  private readonly nombres = computed(
    () =>
      new Map(
        this.personas().map((p) => [
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

  /** Vehículos visibles según la búsqueda (placa, marca, tipo o dueño). */
  protected readonly vehiculosFiltrados = computed(() => {
    const texto = this.busqueda().trim().toLowerCase();
    if (!texto) return this.vehiculos();
    return this.vehiculos().filter((v) =>
      `${v.placa} ${v.marca ?? ''} ${v.tipo} ${this.duenio(v)}`.toLowerCase().includes(texto),
    );
  });

  /** "6 vehículos" o "2 vehículos de 6". */
  protected readonly textoConteo = computed(() => {
    const visibles = this.vehiculosFiltrados().length;
    const total = this.vehiculos().length;
    const palabra = visibles === 1 ? 'vehículo' : 'vehículos';
    return visibles === total ? `${visibles} ${palabra}` : `${visibles} ${palabra} de ${total}`;
  });

  /** Texto de cada tipo de vehículo (constante del modelo). */
  protected readonly etiquetaTipo = ETIQUETA_TIPO_VEHICULO;
  /** Tipos de vehículo para el selector (constante del modelo). */
  protected readonly tiposVehiculo = TIPOS_VEHICULO;
  /** Función del modelo para armar la llave de una persona en la plantilla. */
  protected readonly llavePersona = llavePersona;
  /** Función del modelo para mostrar el nombre de una persona en la plantilla. */
  protected readonly nombrePersona = nombrePersona;

  /** Modal de crear/editar. */
  protected readonly modalAbierto = signal(false);

  /** Vehículo que se edita (null = crear). */
  protected readonly editando = signal<VehiculoModel | null>(null);

  /** Valores actuales del formulario del modal. */
  protected readonly formulario = signal<FormularioVehiculo>({ ...FORMULARIO_VACIO });
  /** true mientras se envía el formulario al backend. */
  protected readonly guardando = signal(false);
  /** Mensaje de validación del formulario (null si no hay error). */
  protected readonly errorFormulario = signal<string | null>(null);

  /** Vehículo pendiente de confirmar para eliminar. */
  protected readonly vehiculoAEliminar = signal<VehiculoModel | null>(null);
  /** true mientras se envía la eliminación al backend. */
  protected readonly eliminando = signal(false);

  /** Consulta los datos al abrir la pantalla. */
  ngOnInit(): void {
    this.cargarVehiculos();
  }

  /** Consulta vehículos y personas a la vez. */
  cargarVehiculos(): void {
    this.cargando.set(true);
    this.errorCarga.set(false);
    forkJoin({
      vehiculos: this.vehiculoService.getTodos(),
      personas: this.personaService.getTodos(),
    }).subscribe({
      next: ({ vehiculos, personas }) => {
        this.vehiculos.set(vehiculos);
        this.personas.set(personas);
        this.cargando.set(false);
      },
      error: (err) => {
        this.errorCarga.set(true);
        this.cargando.set(false);
        this.notificacionService.error(
          extraerError(err, 'No se pudieron consultar los vehículos.'),
          'Error de conexión',
        );
      },
    });
  }

  /**
   * Nombre del dueño (o su documento si no está en la lista).
   * @param v Vehículo de la tabla.
   */
  duenio(v: VehiculoModel): string {
    return (
      this.nombres().get(llavePersona(v.personaTipoDocumento, v.personaNumeroDocumento)) ??
      `${v.personaTipoDocumento} ${v.personaNumeroDocumento}`
    );
  }

  /** Abre el modal vacío para crear. */
  abrirCrear(): void {
    this.editando.set(null);
    this.formulario.set({ ...FORMULARIO_VACIO });
    this.errorFormulario.set(null);
    this.modalAbierto.set(true);
  }

  /**
   * Abre el modal con los datos del vehículo.
   * @param v Vehículo a editar.
   */
  abrirEditar(v: VehiculoModel): void {
    this.editando.set(v);
    this.formulario.set({
      placa: v.placa,
      tipo: v.tipo,
      marca: v.marca ?? '',
      modelo: v.modelo?.toString() ?? '',
      persona: llavePersona(v.personaTipoDocumento, v.personaNumeroDocumento),
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
  cambiarCampo(campo: keyof FormularioVehiculo, valor: string): void {
    this.formulario.update((f) => ({ ...f, [campo]: valor }));
  }

  /** Valida y guarda (crear o editar). */
  guardar(): void {
    const f = this.formulario();
    const placa = f.placa.trim().toUpperCase();
    if (!/^[A-Z0-9]{5,7}$/.test(placa)) {
      this.errorFormulario.set('La placa debe tener entre 5 y 7 letras o números, sin espacios.');
      return;
    }
    if (!f.tipo) {
      this.errorFormulario.set('Elige el tipo de vehículo.');
      return;
    }
    if (!f.persona) {
      this.errorFormulario.set('Elige el dueño del vehículo.');
      return;
    }
    let modelo: number | null = null;
    if (f.modelo.trim()) {
      modelo = Number(f.modelo);
      const maximo = new Date().getFullYear() + 1;
      if (!Number.isInteger(modelo) || modelo < 1900 || modelo > maximo) {
        this.errorFormulario.set(`El modelo debe ser un año entre 1900 y ${maximo}.`);
        return;
      }
    }

    const { tipo: tipoDoc, numero } = separarLlavePersona(f.persona);
    const vehiculo: VehiculoModel = {
      placa,
      tipo: f.tipo,
      marca: f.marca.trim() || null,
      modelo,
      personaTipoDocumento: tipoDoc,
      personaNumeroDocumento: numero,
    };

    const editado = this.editando() !== null;
    this.errorFormulario.set(null);
    this.guardando.set(true);
    const peticion = editado
      ? this.vehiculoService.actualizar(vehiculo)
      : this.vehiculoService.crear(vehiculo);

    peticion.subscribe({
      next: () => {
        this.guardando.set(false);
        this.modalAbierto.set(false);
        this.notificacionService.exito(editado ? 'Vehículo actualizado.' : 'Vehículo creado.', 'Listo');
        this.cargarVehiculos();
      },
      error: (err) => {
        this.guardando.set(false);
        this.errorFormulario.set(extraerError(err, 'No se pudo guardar el vehículo.'));
      },
    });
  }

  /** Confirma y elimina el vehículo. */
  eliminar(): void {
    const v = this.vehiculoAEliminar();
    if (!v) return;
    this.eliminando.set(true);
    this.vehiculoService.eliminar(v.placa).subscribe({
      next: () => {
        this.eliminando.set(false);
        this.vehiculoAEliminar.set(null);
        this.notificacionService.exito('Vehículo eliminado.', 'Listo');
        this.cargarVehiculos();
      },
      error: (err) => {
        this.eliminando.set(false);
        this.vehiculoAEliminar.set(null);
        this.notificacionService.error(
          extraerError(err, 'No se pudo eliminar el vehículo.'),
          'No se pudo eliminar',
        );
      },
    });
  }
}
