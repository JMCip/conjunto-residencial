import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { forkJoin } from 'rxjs';
import { TelefonoModel } from '../../../models/telefono.model';
import {
  llavePersona,
  nombrePersona,
  PersonaModel,
  separarLlavePersona,
} from '../../../models/persona.model';
import { TelefonoService } from '../../../services/telefono.service';
import { PersonaService } from '../../../services/persona.service';
import { NotificacionService } from '../../../services/notificacion.service';
import { ModalFormulario } from '../../../compartido/modal-formulario/modal-formulario';
import { DialogoConfirmar } from '../../../compartido/dialogo-confirmar/dialogo-confirmar';
import { extraerError } from '../../../compartido/extraer-error';

/**
 * @component GestionTelefonos
 * @description Pestaña Teléfonos: CRUD de los teléfonos de cada persona.
 * El número de teléfono es la llave (no se repite entre personas).
 */
@Component({
  selector: 'app-gestion-telefonos',
  imports: [FormsModule, ModalFormulario, DialogoConfirmar],
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

  // ---------- Modal de crear / editar ----------

  protected readonly modoModal = signal<'crear' | 'editar' | null>(null);
  protected readonly formPersona = signal('');
  protected readonly formTelefono = signal('');
  /** Número que tenía al abrir Editar: va en la URL del PUT. */
  private telefonoOriginal = '';
  protected readonly errorFormulario = signal<string | null>(null);
  protected readonly guardando = signal(false);

  // ---------- Diálogo de eliminar ----------

  protected readonly telefonoAEliminar = signal<TelefonoModel | null>(null);
  protected readonly eliminando = signal(false);

  /** Funciones para la plantilla. */
  protected readonly llavePersona = llavePersona;
  protected readonly nombrePersona = nombrePersona;

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

  /** Abre el modal vacío. */
  abrirModalCrear(): void {
    this.formPersona.set('');
    this.formTelefono.set('');
    this.telefonoOriginal = '';
    this.errorFormulario.set(null);
    this.modoModal.set('crear');
  }

  /**
   * Abre el modal con un teléfono para corregir el número o cambiar el dueño.
   * @param t Teléfono elegido en la tabla.
   */
  abrirModalEditar(t: TelefonoModel): void {
    this.formPersona.set(llavePersona(t.personaTipoDocumento, t.personaNumeroDocumento));
    this.formTelefono.set(t.telefono);
    this.telefonoOriginal = t.telefono;
    this.errorFormulario.set(null);
    this.modoModal.set('editar');
  }

  /** Cierra el modal sin guardar. */
  cerrarModal(): void {
    this.modoModal.set(null);
  }

  /** Valida y llama a crear o actualizar. */
  guardar(): void {
    const numero = this.formTelefono().trim();
    let error: string | null = null;
    if (!this.formPersona()) error = 'Elige la persona.';
    else if (!/^\d{7,20}$/.test(numero)) error = 'El teléfono debe tener de 7 a 20 dígitos, sin espacios.';
    this.errorFormulario.set(error);
    if (error) return;

    const { tipo, numero: documento } = separarLlavePersona(this.formPersona());
    const telefono: TelefonoModel = {
      telefono: numero,
      personaTipoDocumento: tipo,
      personaNumeroDocumento: documento,
    };
    const creando = this.modoModal() === 'crear';
    const peticion = creando
      ? this.telefonoService.postCrear(telefono)
      : this.telefonoService.putActualizar(this.telefonoOriginal, telefono);

    this.guardando.set(true);
    peticion.subscribe({
      next: () => {
        this.guardando.set(false);
        this.modoModal.set(null);
        this.notificacionService.exito(
          `Teléfono ${numero} ${creando ? 'registrado' : 'actualizado'} correctamente.`,
        );
        this.cargarTelefonos();
      },
      error: (err) => {
        this.guardando.set(false);
        this.errorFormulario.set(
          extraerError(err, `No se pudo ${creando ? 'registrar' : 'actualizar'} el teléfono.`),
        );
      },
    });
  }

  /** Pide confirmación antes de eliminar. */
  pedirEliminar(t: TelefonoModel): void {
    this.telefonoAEliminar.set(t);
  }

  /** Cierra el diálogo sin eliminar. */
  cancelarEliminar(): void {
    this.telefonoAEliminar.set(null);
  }

  /** Envía el DELETE del teléfono confirmado. */
  confirmarEliminar(): void {
    const t = this.telefonoAEliminar();
    if (!t) return;
    this.eliminando.set(true);
    this.telefonoService.deleteEliminar(t.telefono).subscribe({
      next: () => {
        this.eliminando.set(false);
        this.telefonoAEliminar.set(null);
        this.notificacionService.exito(`Teléfono ${t.telefono} eliminado correctamente.`);
        this.cargarTelefonos();
      },
      error: (err) => {
        this.eliminando.set(false);
        this.telefonoAEliminar.set(null);
        this.notificacionService.error(
          extraerError(err, 'No se pudo eliminar el teléfono.'),
          'No se eliminó',
        );
      },
    });
  }
}
