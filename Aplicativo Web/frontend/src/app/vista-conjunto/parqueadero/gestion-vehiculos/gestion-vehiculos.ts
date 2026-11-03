import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { forkJoin } from 'rxjs';
import {
  ETIQUETA_TIPO_VEHICULO,
  VehiculoModel,
} from '../../../models/vehiculo.model';
import {
  llavePersona,
  nombrePersona,
  PersonaModel,
} from '../../../models/persona.model';
import { VehiculoService } from '../../../services/vehiculo.service';
import { PersonaService } from '../../../services/persona.service';
import { NotificacionService } from '../../../services/notificacion.service';
import { extraerError } from '../../../compartido/extraer-error';

/**
 * @component GestionVehiculos
 * @description Pestaña Vehículos de Parqueadero: CRUD de los carros y motos de cada persona.
 * La placa es la llave: no se cambia al editar (los parqueos la usan).
 */
@Component({
  selector: 'app-gestion-vehiculos',
  imports: [],
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

  /** Constantes y funciones para la plantilla. */
  protected readonly etiquetaTipo = ETIQUETA_TIPO_VEHICULO;

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
}
