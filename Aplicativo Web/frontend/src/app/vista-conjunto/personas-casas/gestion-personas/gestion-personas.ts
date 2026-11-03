import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { catchError, forkJoin, Observable, of } from 'rxjs';
import {
  llavePersona,
  nombrePersona,
  PersonaModel,
  ROLES_PERSONA,
  RutaRol,
} from '../../../models/persona.model';
import { PersonaService } from '../../../services/persona.service';
import { NotificacionService } from '../../../services/notificacion.service';
import { extraerError } from '../../../compartido/extraer-error';

/**
 * @component GestionPersonas
 * @description Pestaña Personas: CRUD de personas con sus roles (propietario, arrendatario,
 * residente, visitante). Los roles se leen de las 4 rutas de roles y se cambian con casillas.
 */
@Component({
  selector: 'app-gestion-personas',
  imports: [],
  templateUrl: './gestion-personas.html',
  styleUrl: './gestion-personas.css',
})
export class GestionPersonas implements OnInit {
  private personaService = inject(PersonaService);
  private notificacionService = inject(NotificacionService);

  /** Personas obtenidas del backend. */
  protected readonly personas = signal<PersonaModel[]>([]);

  /** Roles de cada persona: llave "CC|123" → ['propietarios', 'residentes']. */
  protected readonly rolesPorPersona = signal<Map<string, RutaRol[]>>(new Map());

  /** true mientras llega la lista. */
  protected readonly cargando = signal(true);

  /** true si la consulta de personas falló. */
  protected readonly errorCarga = signal(false);

  /** Texto escrito en el buscador. */
  protected readonly busqueda = signal('');

  /** Personas visibles según la búsqueda (nombre, documento, correo o rol). */
  protected readonly personasFiltradas = computed(() => {
    const texto = this.busqueda().trim().toLowerCase();
    if (!texto) return this.personas();
    return this.personas().filter((p) =>
      [nombrePersona(p), p.tipoDocumento, p.numeroDocumento, p.correo ?? '', this.textoRoles(p)]
        .join(' ')
        .toLowerCase()
        .includes(texto),
    );
  });

  /** "12 personas" o "3 personas de 12". */
  protected readonly textoConteo = computed(() => {
    const visibles = this.personasFiltradas().length;
    const total = this.personas().length;
    const palabra = visibles === 1 ? 'persona' : 'personas';
    return visibles === total ? `${visibles} ${palabra}` : `${visibles} ${palabra} de ${total}`;
  });

  /** Constantes y funciones para la plantilla. */
  protected readonly nombrePersona = nombrePersona;

  ngOnInit(): void {
    this.cargarPersonas();
  }

  /** Consulta las personas y, a la vez, las 4 listas de roles. */
  cargarPersonas(): void {
    this.cargando.set(true);
    this.errorCarga.set(false);
    const rol = (lista: Observable<PersonaModel[]>) => lista.pipe(catchError(() => of([])));
    forkJoin({
      personas: this.personaService.getTodos(),
      propietarios: rol(this.personaService.getPropietarios()),
      arrendatarios: rol(this.personaService.getArrendatarios()),
      residentes: rol(this.personaService.getResidentes()),
      visitantes: rol(this.personaService.getVisitantes()),
    }).subscribe({
      next: ({ personas, ...listasRoles }) => {
        const mapa = new Map<string, RutaRol[]>();
        for (const { ruta } of ROLES_PERSONA) {
          for (const p of listasRoles[ruta]) {
            const llave = llavePersona(p.tipoDocumento, p.numeroDocumento);
            mapa.set(llave, [...(mapa.get(llave) ?? []), ruta]);
          }
        }
        this.personas.set(personas);
        this.rolesPorPersona.set(mapa);
        this.cargando.set(false);
      },
      error: (err) => {
        this.errorCarga.set(true);
        this.cargando.set(false);
        this.notificacionService.error(
          extraerError(err, 'No se pudieron consultar las personas.'),
          'Error de conexión',
        );
      },
    });
  }

  /**
   * Roles de una persona.
   * @param p Persona de la tabla.
   */
  rolesDe(p: PersonaModel): RutaRol[] {
    return this.rolesPorPersona().get(llavePersona(p.tipoDocumento, p.numeroDocumento)) ?? [];
  }

  /**
   * Texto singular de un rol ("Propietario").
   * @param ruta Ruta del rol.
   */
  textoRol(ruta: RutaRol): string {
    return ROLES_PERSONA.find((r) => r.ruta === ruta)?.texto ?? ruta;
  }

  /** "Propietario, Residente" (para buscar por rol). */
  private textoRoles(p: PersonaModel): string {
    return this.rolesDe(p)
      .map((r) => this.textoRol(r))
      .join(' ');
  }
}
