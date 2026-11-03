import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { forkJoin } from 'rxjs';
import { TelefonoModel } from '../../../models/telefono.model';
import {
  llavePersona,
  nombrePersona,
  PersonaModel,
} from '../../../models/persona.model';
import { TelefonoService } from '../../../services/telefono.service';
import { PersonaService } from '../../../services/persona.service';
import { NotificacionService } from '../../../services/notificacion.service';
import { extraerError } from '../../../compartido/extraer-error';

/**
 * @component GestionTelefonos
 * @description Pestaña Teléfonos: CRUD de los teléfonos de cada persona.
 * El número de teléfono es la llave (no se repite entre personas).
 */
@Component({
  selector: 'app-gestion-telefonos',
  imports: [],
  templateUrl: './gestion-telefonos.html',
  styleUrl: './gestion-telefonos.css',
})
export class GestionTelefonos implements OnInit {
  private telefonoService = inject(TelefonoService);
  private personaService = inject(PersonaService);
  private notificacionService = inject(NotificacionService);

  /** Teléfonos obtenidos del backend. */
  protected readonly telefonos = signal<TelefonoModel[]>([]);

  /** Personas, para el selector y para mostrar el nombre del dueño. */
  protected readonly personas = signal<PersonaModel[]>([]);

  /** Nombre de cada persona por su llave "CC|123". */
  private readonly nombres = computed(
    () =>
      new Map(
        this.personas().map((p) => [llavePersona(p.tipoDocumento, p.numeroDocumento), nombrePersona(p)]),
      ),
  );

  /** true mientras llega la lista. */
  protected readonly cargando = signal(true);

  /** true si la consulta falló. */
  protected readonly errorCarga = signal(false);

  /** Texto escrito en el buscador. */
  protected readonly busqueda = signal('');

  /** Teléfonos visibles según la búsqueda (número, nombre o documento). */
  protected readonly telefonosFiltrados = computed(() => {
    const texto = this.busqueda().trim().toLowerCase();
    if (!texto) return this.telefonos();
    return this.telefonos().filter((t) =>
      `${t.telefono} ${this.duenio(t)} ${t.personaNumeroDocumento}`.toLowerCase().includes(texto),
    );
  });

  /** "15 teléfonos" o "2 teléfonos de 15". */
  protected readonly textoConteo = computed(() => {
    const visibles = this.telefonosFiltrados().length;
    const total = this.telefonos().length;
    const palabra = visibles === 1 ? 'teléfono' : 'teléfonos';
    return visibles === total ? `${visibles} ${palabra}` : `${visibles} ${palabra} de ${total}`;
  });

  /** Funciones para la plantilla. */

  ngOnInit(): void {
    this.cargarTelefonos();
  }

  /** Consulta los teléfonos y las personas a la vez. */
  cargarTelefonos(): void {
    this.cargando.set(true);
    this.errorCarga.set(false);
    forkJoin({
      telefonos: this.telefonoService.getTodos(),
      personas: this.personaService.getTodos(),
    }).subscribe({
      next: ({ telefonos, personas }) => {
        this.telefonos.set(telefonos);
        this.personas.set(personas);
        this.cargando.set(false);
      },
      error: (err) => {
        this.errorCarga.set(true);
        this.cargando.set(false);
        this.notificacionService.error(
          extraerError(err, 'No se pudieron consultar los teléfonos.'),
          'Error de conexión',
        );
      },
    });
  }

  /**
   * Nombre del dueño de un teléfono (o su documento si no está en la lista).
   * @param t Teléfono de la tabla.
   */
  duenio(t: TelefonoModel): string {
    return (
      this.nombres().get(llavePersona(t.personaTipoDocumento, t.personaNumeroDocumento)) ??
      `${t.personaTipoDocumento} ${t.personaNumeroDocumento}`
    );
  }
}
