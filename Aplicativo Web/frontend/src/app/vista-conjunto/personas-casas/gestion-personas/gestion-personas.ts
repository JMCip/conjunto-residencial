import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { catchError, forkJoin, map, Observable, of } from 'rxjs';
import {
  llavePersona,
  nombrePersona,
  PersonaModel,
  ROL_SINGULAR,
  ROLES_PERSONA,
  RutaRol,
  TIPOS_DOCUMENTO,
} from '../../../models/persona.model';
import { PersonaService } from '../../../services/persona.service';
import { NotificacionService } from '../../../services/notificacion.service';
import { extraerError } from '../../../compartido/extraer-error';
import { ModalFormulario } from '../../../compartido/modal-formulario/modal-formulario';
import { DialogoConfirmar } from '../../../compartido/dialogo-confirmar/dialogo-confirmar';

/** Campos del formulario (todo como texto, los vacíos se pasan a null al guardar). */
interface FormularioPersona {
  tipoDocumento: string;
  numeroDocumento: string;
  primerNombre: string;
  segundoNombre: string;
  primerApellido: string;
  segundoApellido: string;
  correo: string;
}

/** Valores iniciales del formulario (todo vacío). */
const FORMULARIO_VACIO: FormularioPersona = {
  tipoDocumento: 'CC',
  numeroDocumento: '',
  primerNombre: '',
  segundoNombre: '',
  primerApellido: '',
  segundoApellido: '',
  correo: '',
};

/**
 * @component GestionPersonas
 * @description Pestaña Personas: CRUD de personas con sus roles (propietario, arrendatario,
 * residente, visitante). Los roles se leen de las 4 rutas de roles y se cambian con casillas.
 */
@Component({
  selector: 'app-gestion-personas',
  imports: [ModalFormulario, DialogoConfirmar],
  templateUrl: './gestion-personas.html',
  styleUrl: './gestion-personas.css',
})
export class GestionPersonas implements OnInit {
  /** Servicio de personas y roles. */
  private personaService = inject(PersonaService);
  /** Servicio de avisos flotantes. */
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

  /** Función del modelo para mostrar el nombre de una persona en la plantilla. */
  protected readonly nombrePersona = nombrePersona;
  /** Tipos de documento para el selector (constante del modelo). */
  protected readonly tiposDocumento = TIPOS_DOCUMENTO;
  /** Roles que se pueden asignar a una persona (constante del modelo). */
  protected readonly roles = ROLES_PERSONA;

  /** Modal de crear/editar. */
  protected readonly modalAbierto = signal(false);

  /** Persona que se edita (null = crear). */
  protected readonly editando = signal<PersonaModel | null>(null);

  /** Valores actuales del formulario del modal. */
  protected readonly formulario = signal<FormularioPersona>({ ...FORMULARIO_VACIO });

  /** Roles marcados en las casillas del formulario. */
  protected readonly rolesElegidos = signal<RutaRol[]>([]);
  /** true mientras se envía el formulario al backend. */
  protected readonly guardando = signal(false);
  /** Mensaje de validación del formulario (null si no hay error). */
  protected readonly errorFormulario = signal<string | null>(null);

  /** Persona pendiente de confirmar para eliminar. */
  protected readonly personaAEliminar = signal<PersonaModel | null>(null);
  /** true mientras se envía la eliminación al backend. */
  protected readonly eliminando = signal(false);

  /** Consulta los datos al abrir la pantalla. */
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

  /** Abre el modal vacío para crear. */
  abrirCrear(): void {
    this.editando.set(null);
    this.formulario.set({ ...FORMULARIO_VACIO });
    this.rolesElegidos.set([]);
    this.errorFormulario.set(null);
    this.modalAbierto.set(true);
  }

  /**
   * Abre el modal con los datos de la persona.
   * @param p Persona a editar.
   */
  abrirEditar(p: PersonaModel): void {
    this.editando.set(p);
    this.formulario.set({
      tipoDocumento: p.tipoDocumento,
      numeroDocumento: p.numeroDocumento,
      primerNombre: p.primerNombre,
      segundoNombre: p.segundoNombre ?? '',
      primerApellido: p.primerApellido,
      segundoApellido: p.segundoApellido ?? '',
      correo: p.correo ?? '',
    });
    this.rolesElegidos.set([...this.rolesDe(p)]);
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
  cambiarCampo(campo: keyof FormularioPersona, valor: string): void {
    this.formulario.update((f) => ({ ...f, [campo]: valor }));
  }

  /**
   * Marca o desmarca un rol.
   * @param ruta Rol a alternar.
   */
  alternarRol(ruta: RutaRol): void {
    this.rolesElegidos.update((r) =>
      r.includes(ruta) ? r.filter((x) => x !== ruta) : [...r, ruta],
    );
  }

  /** Valida y guarda (crear o editar); luego sincroniza los roles. */
  guardar(): void {
    const f = this.formulario();
    if (!f.tipoDocumento || !f.numeroDocumento.trim()) {
      this.errorFormulario.set('El tipo y el número de documento son obligatorios.');
      return;
    }
    if (!f.primerNombre.trim() || !f.primerApellido.trim()) {
      this.errorFormulario.set('El primer nombre y el primer apellido son obligatorios.');
      return;
    }
    if (f.correo.trim() && !/^[^@\s]+@[^@\s]+\.[^@\s]+$/.test(f.correo.trim())) {
      this.errorFormulario.set('El correo no tiene un formato válido.');
      return;
    }

    const persona: PersonaModel = {
      tipoDocumento: f.tipoDocumento,
      numeroDocumento: f.numeroDocumento.trim(),
      correo: f.correo.trim() || null,
      primerNombre: f.primerNombre.trim(),
      segundoNombre: f.segundoNombre.trim() || null,
      primerApellido: f.primerApellido.trim(),
      segundoApellido: f.segundoApellido.trim() || null,
    };

    this.errorFormulario.set(null);
    this.guardando.set(true);
    const peticion = this.editando()
      ? this.personaService.actualizar(persona)
      : this.personaService.crear(persona);

    peticion.subscribe({
      next: () => this.sincronizarRoles(persona),
      error: (err) => {
        this.guardando.set(false);
        this.errorFormulario.set(extraerError(err, 'No se pudo guardar la persona.'));
      },
    });
  }

  /** Asigna los roles marcados y quita los desmarcados. */
  private sincronizarRoles(p: PersonaModel): void {
    const editada = this.editando();
    const actuales = editada ? this.rolesDe(editada) : [];
    const elegidos = this.rolesElegidos();

    const operaciones: Observable<string | null>[] = [
      ...elegidos
        .filter((r) => !actuales.includes(r))
        .map((r) =>
          this.personaService.asignarRol(p.tipoDocumento, p.numeroDocumento, ROL_SINGULAR[r]).pipe(
            map(() => null),
            catchError((err) =>
              of(`${this.textoRol(r)}: ${extraerError(err, 'no se pudo asignar')}`),
            ),
          ),
        ),
      ...actuales
        .filter((r) => !elegidos.includes(r))
        .map((r) =>
          this.personaService.quitarRol(p.tipoDocumento, p.numeroDocumento, ROL_SINGULAR[r]).pipe(
            map(() => null),
            catchError((err) =>
              of(`${this.textoRol(r)}: ${extraerError(err, 'no se pudo quitar')}`),
            ),
          ),
        ),
    ];

    if (operaciones.length === 0) {
      this.terminarGuardado([]);
      return;
    }
    forkJoin(operaciones).subscribe((res) =>
      this.terminarGuardado(res.filter((m): m is string => m !== null)),
    );
  }

  /** Cierra el modal, recarga y avisa el resultado. */
  private terminarGuardado(erroresRoles: string[]): void {
    const editado = this.editando() !== null;
    this.guardando.set(false);
    this.modalAbierto.set(false);
    this.cargarPersonas();
    if (erroresRoles.length > 0) {
      this.notificacionService.error(
        `La persona se guardó, pero hubo problemas con los roles. ${erroresRoles.join(' | ')}`,
        'Roles no actualizados',
      );
    } else {
      this.notificacionService.exito(editado ? 'Persona actualizada.' : 'Persona creada.', 'Listo');
    }
  }

  /** Confirma y elimina la persona. */
  eliminar(): void {
    const p = this.personaAEliminar();
    if (!p) return;
    this.eliminando.set(true);
    this.personaService.eliminar(p.tipoDocumento, p.numeroDocumento).subscribe({
      next: () => {
        this.eliminando.set(false);
        this.personaAEliminar.set(null);
        this.notificacionService.exito('Persona eliminada.', 'Listo');
        this.cargarPersonas();
      },
      error: (err) => {
        this.eliminando.set(false);
        this.personaAEliminar.set(null);
        this.notificacionService.error(
          extraerError(err, 'No se pudo eliminar la persona.'),
          'No se pudo eliminar',
        );
      },
    });
  }
}
