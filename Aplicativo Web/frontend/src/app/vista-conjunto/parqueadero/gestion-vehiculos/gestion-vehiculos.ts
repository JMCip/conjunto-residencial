import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
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
import { ModalFormulario } from '../../../compartido/modal-formulario/modal-formulario';
import { DialogoConfirmar } from '../../../compartido/dialogo-confirmar/dialogo-confirmar';
import { extraerError } from '../../../compartido/extraer-error';

/**
 * @component GestionVehiculos
 * @description Pestaña Vehículos de Parqueadero: CRUD de los carros y motos de cada persona.
 * La placa es la llave: no se cambia al editar (los parqueos la usan).
 */
@Component({
  selector: 'app-gestion-vehiculos',
  imports: [FormsModule, ModalFormulario, DialogoConfirmar],
  templateUrl: './gestion-vehiculos.html',
  styleUrl: './gestion-vehiculos.css',
})
export class GestionVehiculos implements OnInit {
  private vehiculoService = inject(VehiculoService);
  private personaService = inject(PersonaService);
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

  protected readonly cargando = signal(true);
  protected readonly errorCarga = signal(false);
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

  // ---------- Modal de crear / editar ----------

  protected readonly modoModal = signal<'crear' | 'editar' | null>(null);
  protected readonly formPlaca = signal('');
  protected readonly formMarca = signal('');
  protected readonly formModelo = signal<number | null>(null);
  protected readonly formTipo = signal<string>('CARRO');
  protected readonly formPersona = signal('');
  protected readonly errorFormulario = signal<string | null>(null);
  protected readonly guardando = signal(false);

  // ---------- Diálogo de eliminar ----------

  protected readonly vehiculoAEliminar = signal<VehiculoModel | null>(null);
  protected readonly eliminando = signal(false);

  /** Constantes y funciones para la plantilla. */
  protected readonly tiposVehiculo = TIPOS_VEHICULO;
  protected readonly etiquetaTipo = ETIQUETA_TIPO_VEHICULO;
  protected readonly llavePersona = llavePersona;
  protected readonly nombrePersona = nombrePersona;

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

  /** Abre el modal vacío. */
  abrirModalCrear(): void {
    this.formPlaca.set('');
    this.formMarca.set('');
    this.formModelo.set(null);
    this.formTipo.set('CARRO');
    this.formPersona.set('');
    this.errorFormulario.set(null);
    this.modoModal.set('crear');
  }

  /**
   * Abre el modal con los datos de un vehículo.
   * @param v Vehículo elegido en la tabla.
   */
  abrirModalEditar(v: VehiculoModel): void {
    this.formPlaca.set(v.placa);
    this.formMarca.set(v.marca ?? '');
    this.formModelo.set(v.modelo);
    this.formTipo.set(v.tipo);
    this.formPersona.set(llavePersona(v.personaTipoDocumento, v.personaNumeroDocumento));
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

    const persona = separarLlavePersona(this.formPersona());
    const vehiculo: VehiculoModel = {
      placa: this.formPlaca().trim().toUpperCase(),
      marca: this.formMarca().trim() || null,
      modelo: this.formModelo(),
      tipo: this.formTipo(),
      personaTipoDocumento: persona.tipo,
      personaNumeroDocumento: persona.numero,
    };
    const creando = this.modoModal() === 'crear';
    const peticion = creando
      ? this.vehiculoService.postCrear(vehiculo)
      : this.vehiculoService.putActualizar(vehiculo.placa, vehiculo);

    this.guardando.set(true);
    peticion.subscribe({
      next: () => {
        this.guardando.set(false);
        this.modoModal.set(null);
        this.notificacionService.exito(
          `Vehículo ${vehiculo.placa} ${creando ? 'registrado' : 'actualizado'} correctamente.`,
        );
        this.cargarVehiculos();
      },
      error: (err) => {
        this.guardando.set(false);
        this.errorFormulario.set(
          extraerError(err, `No se pudo ${creando ? 'registrar' : 'actualizar'} el vehículo.`),
        );
      },
    });
  }

  /** Pide confirmación antes de eliminar. */
  pedirEliminar(v: VehiculoModel): void {
    this.vehiculoAEliminar.set(v);
  }

  /** Cierra el diálogo sin eliminar. */
  cancelarEliminar(): void {
    this.vehiculoAEliminar.set(null);
  }

  /** Envía el DELETE del vehículo confirmado. */
  confirmarEliminar(): void {
    const v = this.vehiculoAEliminar();
    if (!v) return;
    this.eliminando.set(true);
    this.vehiculoService.deleteEliminar(v.placa).subscribe({
      next: () => {
        this.eliminando.set(false);
        this.vehiculoAEliminar.set(null);
        this.notificacionService.exito(`Vehículo ${v.placa} eliminado correctamente.`);
        this.cargarVehiculos();
      },
      error: (err) => {
        this.eliminando.set(false);
        this.vehiculoAEliminar.set(null);
        this.notificacionService.error(
          extraerError(err, 'No se pudo eliminar el vehículo.'),
          'No se eliminó',
        );
      },
    });
  }

  /**
   * Revisa los campos antes de enviar (incluye el largo máximo de cada texto).
   * @returns Mensaje del primer problema, o null si todo está bien.
   */
  private validar(): string | null {
    if (!/^[A-Za-z0-9]{5,10}$/.test(this.formPlaca().trim())) {
      return 'La placa debe tener de 5 a 10 letras o números, sin espacios ni guiones.';
    }
    if (this.formMarca().trim().length > 20) return 'La marca puede tener máximo 20 letras.';
    const modelo = this.formModelo();
    if (modelo !== null && (!Number.isInteger(modelo) || modelo < 1950 || modelo > 2100)) {
      return 'El modelo debe ser un año entre 1950 y 2100 (o déjalo vacío).';
    }
    if (!this.formTipo()) return 'Elige el tipo de vehículo.';
    if (!this.formPersona()) return 'Elige el dueño del vehículo.';
    return null;
  }
}
