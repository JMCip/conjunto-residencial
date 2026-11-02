import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { catchError, forkJoin, Observable, of, switchMap } from 'rxjs';
import {
  llavePersona,
  nombrePersona,
  PersonaModel,
  ROLES_PERSONA,
  RutaRol,
  TIPOS_DOCUMENTO,
} from '../../../models/persona.model';
import { RespuestaApiModel } from '../../../models/respuesta-api.model';
import { PersonaService } from '../../../services/persona.service';
import { NotificacionService } from '../../../services/notificacion.service';
import { ModalFormulario } from '../../../compartido/modal-formulario/modal-formulario';
import { DialogoConfirmar } from '../../../compartido/dialogo-confirmar/dialogo-confirmar';
import { extraerError } from '../../../compartido/extraer-error';

/**
 * @component GestionPersonas
 * @description Pestaña Personas: CRUD de personas con sus roles (propietario, arrendatario,
 * residente, visitante). Los roles se leen de las 4 rutas de roles y se cambian con casillas.
 */
@Component({
  selector: 'app-gestion-personas',
  imports: [FormsModule, ModalFormulario, DialogoConfirmar],
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

  // ---------- Modal de crear / editar ----------

  /** null = cerrado; 'crear' o 'editar' = abierto en ese modo. */
  protected readonly modoModal = signal<'crear' | 'editar' | null>(null);

  /** Campos del formulario (enlazados con [(ngModel)]). */
  protected readonly formTipoDocumento = signal<string>('CC');
  protected readonly formNumeroDocumento = signal('');
  protected readonly formPrimerNombre = signal('');
  protected readonly formSegundoNombre = signal('');
  protected readonly formPrimerApellido = signal('');
  protected readonly formSegundoApellido = signal('');
  protected readonly formCorreo = signal('');
  protected readonly formRoles = signal<RutaRol[]>([]);

  /** Roles que tenía la persona al abrir Editar (para saber cuáles agregar o quitar). */
  private rolesOriginales: RutaRol[] = [];

  /** "Nueva persona" o "Editar Carlos Gómez". */
  protected readonly tituloModal = computed(() =>
    this.modoModal() === 'crear'
      ? 'Nueva persona'
      : `Editar ${this.formPrimerNombre()} ${this.formPrimerApellido()}`,
  );

  /** Mensaje de validación del formulario. */
  protected readonly errorFormulario = signal<string | null>(null);

  /** true mientras se envía crear o editar. */
  protected readonly guardando = signal(false);

  // ---------- Diálogo de eliminar ----------

  /** Persona que se va a eliminar (null = diálogo cerrado). */
  protected readonly personaAEliminar = signal<PersonaModel | null>(null);

  /** true mientras se envía el DELETE. */
  protected readonly eliminando = signal(false);

  /** Constantes y funciones para la plantilla. */
  protected readonly tiposDocumento = TIPOS_DOCUMENTO;
  protected readonly roles = ROLES_PERSONA;
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

  /** Abre el modal vacío para crear una persona. */
  abrirModalCrear(): void {
    this.formTipoDocumento.set('CC');
    this.formNumeroDocumento.set('');
    this.formPrimerNombre.set('');
    this.formSegundoNombre.set('');
    this.formPrimerApellido.set('');
    this.formSegundoApellido.set('');
    this.formCorreo.set('');
    this.formRoles.set([]);
    this.rolesOriginales = [];
    this.errorFormulario.set(null);
    this.modoModal.set('crear');
  }

  /**
   * Abre el modal con los datos de una persona. El documento no se edita: es la llave.
   * @param p Persona elegida en la tabla.
   */
  abrirModalEditar(p: PersonaModel): void {
    this.formTipoDocumento.set(p.tipoDocumento);
    this.formNumeroDocumento.set(p.numeroDocumento);
    this.formPrimerNombre.set(p.primerNombre);
    this.formSegundoNombre.set(p.segunoNombre ?? '');
    this.formPrimerApellido.set(p.primerApellido);
    this.formSegundoApellido.set(p.segundoApellido ?? '');
    this.formCorreo.set(p.correo ?? '');
    this.rolesOriginales = this.rolesDe(p);
    this.formRoles.set([...this.rolesOriginales]);
    this.errorFormulario.set(null);
    this.modoModal.set('editar');
  }

  /** Cierra el modal sin guardar. */
  cerrarModal(): void {
    this.modoModal.set(null);
  }

  /**
   * Marca o desmarca un rol en el formulario.
   * @param ruta Rol de la casilla.
   */
  alternarRol(ruta: RutaRol): void {
    this.formRoles.update((roles) =>
      roles.includes(ruta) ? roles.filter((r) => r !== ruta) : [...roles, ruta],
    );
  }

  /** Guarda la persona y después agrega o quita los roles que cambiaron. */
  guardar(): void {
    const error = this.validar();
    this.errorFormulario.set(error);
    if (error) return;

    const vacioANull = (texto: string) => texto.trim() || null;
    const persona: PersonaModel = {
      tipoDocumento: this.formTipoDocumento(),
      numeroDocumento: this.formNumeroDocumento().trim(),
      primerNombre: this.formPrimerNombre().trim(),
      segunoNombre: vacioANull(this.formSegundoNombre()),
      primerApellido: this.formPrimerApellido().trim(),
      segundoApellido: vacioANull(this.formSegundoApellido()),
      correo: vacioANull(this.formCorreo()),
    };
    const creando = this.modoModal() === 'crear';
    const { tipoDocumento: tipo, numeroDocumento: numero } = persona;

    // Roles que hay que agregar y quitar comparando con los que tenía.
    const cambiosRoles: Observable<RespuestaApiModel>[] = [
      ...this.formRoles()
        .filter((r) => !this.rolesOriginales.includes(r))
        .map((r) => this.personaService.postAgregarRol(r, tipo, numero)),
      ...this.rolesOriginales
        .filter((r) => !this.formRoles().includes(r))
        .map((r) => this.personaService.deleteQuitarRol(r, tipo, numero)),
    ];

    const peticion = creando
      ? this.personaService.postCrear(persona)
      : this.personaService.putActualizar(persona);

    this.guardando.set(true);
    peticion
      .pipe(switchMap(() => (cambiosRoles.length ? forkJoin(cambiosRoles) : of(null))))
      .subscribe({
        next: () => {
          this.guardando.set(false);
          this.modoModal.set(null);
          this.notificacionService.exito(
            `${persona.primerNombre} ${persona.primerApellido} ${creando ? 'registrada' : 'actualizada'} correctamente.`,
          );
          this.cargarPersonas();
        },
        error: (err) => {
          this.guardando.set(false);
          this.errorFormulario.set(
            extraerError(err, `No se pudo ${creando ? 'registrar' : 'actualizar'} la persona.`),
          );
          // Pudo guardar la persona y fallar un rol: recarga para mostrar lo que sí quedó.
          this.cargarPersonas();
        },
      });
  }

  /**
   * Pide confirmación antes de eliminar.
   * @param p Persona elegida en la tabla.
   */
  pedirEliminar(p: PersonaModel): void {
    this.personaAEliminar.set(p);
  }

  /** Cierra el diálogo sin eliminar. */
  cancelarEliminar(): void {
    this.personaAEliminar.set(null);
  }

  /** Envía el DELETE de la persona confirmada. */
  confirmarEliminar(): void {
    const p = this.personaAEliminar();
    if (!p) return;
    this.eliminando.set(true);
    this.personaService.deleteEliminar(p.tipoDocumento, p.numeroDocumento).subscribe({
      next: () => {
        this.eliminando.set(false);
        this.personaAEliminar.set(null);
        this.notificacionService.exito(`${nombrePersona(p)} eliminada correctamente.`);
        this.cargarPersonas();
      },
      error: (err) => {
        this.eliminando.set(false);
        this.personaAEliminar.set(null);
        this.notificacionService.error(
          extraerError(err, 'No se pudo eliminar la persona.'),
          'No se eliminó',
        );
      },
    });
  }

  /** "Propietario, Residente" (para buscar por rol). */
  private textoRoles(p: PersonaModel): string {
    return this.rolesDe(p)
      .map((r) => this.textoRol(r))
      .join(' ');
  }

  /**
   * Revisa los campos antes de enviar (incluye el largo máximo de cada texto).
   * @returns Mensaje del primer problema, o null si todo está bien.
   */
  private validar(): string | null {
    const numero = this.formNumeroDocumento().trim();
    if (!/^[A-Za-z0-9]{3,20}$/.test(numero)) {
      return 'El número de documento debe tener de 3 a 20 letras o números, sin espacios.';
    }
    const nombres: [string, string, boolean][] = [
      ['primer nombre', this.formPrimerNombre(), true],
      ['segundo nombre', this.formSegundoNombre(), false],
      ['primer apellido', this.formPrimerApellido(), true],
      ['segundo apellido', this.formSegundoApellido(), false],
    ];
    for (const [campo, valor, obligatorio] of nombres) {
      const limpio = valor.trim();
      if (obligatorio && !limpio) return `Escribe el ${campo}.`;
      if (limpio.length > 20) return `El ${campo} puede tener máximo 20 letras.`;
    }
    const correo = this.formCorreo().trim();
    if (correo && (correo.length > 150 || !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(correo))) {
      return 'El correo no es válido.';
    }
    return null;
  }
}
