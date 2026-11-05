import { Component, computed, inject, OnInit, signal } from '@angular/core';
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
import { extraerError } from '../../../compartido/extraer-error';
import { ModalFormulario } from '../../../compartido/modal-formulario/modal-formulario';
import { DialogoConfirmar } from '../../../compartido/dialogo-confirmar/dialogo-confirmar';

/**
 * @component GestionTelefonos
 * @description Pestaña Teléfonos: CRUD de los teléfonos de cada persona.
 * El número de teléfono es la llave (no se repite entre personas).
 */
@Component({
  selector: 'app-gestion-telefonos',
  imports: [ModalFormulario, DialogoConfirmar],
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

  /** Funciones y constantes para la plantilla. */
  protected readonly llavePersona = llavePersona;
  protected readonly nombrePersona = nombrePersona;

  /** Modal de crear/editar. */
  protected readonly modalAbierto = signal(false);

  /** Teléfono que se edita (null = crear). */
  protected readonly editando = signal<TelefonoModel | null>(null);

  /** Llave "CC|123" de la persona elegida en el selector. */
  protected readonly personaElegida = signal('');

  /** Número escrito en el formulario. */
  protected readonly numeroEscrito = signal('');

  protected readonly guardando = signal(false);
  protected readonly errorFormulario = signal<string | null>(null);

  /** Teléfono pendiente de confirmar para eliminar. */
  protected readonly telefonoAEliminar = signal<TelefonoModel | null>(null);
  protected readonly eliminando = signal(false);

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

  /** Abre el modal vacío para crear. */
  abrirCrear(): void {
    this.editando.set(null);
    this.personaElegida.set('');
    this.numeroEscrito.set('');
    this.errorFormulario.set(null);
    this.modalAbierto.set(true);
  }

  /**
   * Abre el modal para cambiar el número de un teléfono.
   * @param t Teléfono a editar.
   */
  abrirEditar(t: TelefonoModel): void {
    this.editando.set(t);
    this.personaElegida.set(llavePersona(t.personaTipoDocumento, t.personaNumeroDocumento));
    this.numeroEscrito.set(t.telefono);
    this.errorFormulario.set(null);
    this.modalAbierto.set(true);
  }

  /** Cierra el modal sin guardar. */
  cerrarModal(): void {
    this.modalAbierto.set(false);
  }

  /** Valida y guarda (crear o cambiar el número). */
  guardar(): void {
    const llave = this.personaElegida();
    if (!llave) {
      this.errorFormulario.set('Elige la persona dueña del teléfono.');
      return;
    }
    const numero = this.numeroEscrito().replace(/\D/g, '');
    if (numero.length < 7 || numero.length > 15) {
      this.errorFormulario.set('El teléfono debe tener entre 7 y 15 dígitos.');
      return;
    }

    const { tipo, numero: documento } = separarLlavePersona(llave);
    const actual = this.editando();
    this.errorFormulario.set(null);
    this.guardando.set(true);

    const peticion = actual
      ? this.telefonoService.actualizar(tipo, documento, actual.telefono, numero)
      : this.telefonoService.crear({
        telefono: numero,
        personaTipoDocumento: tipo,
        personaNumeroDocumento: documento,
      });

    peticion.subscribe({
      next: () => {
        this.guardando.set(false);
        this.modalAbierto.set(false);
        this.notificacionService.exito(
          actual ? 'Teléfono actualizado.' : 'Teléfono creado.',
          'Listo',
        );
        this.cargarTelefonos();
      },
      error: (err) => {
        this.guardando.set(false);
        this.errorFormulario.set(extraerError(err, 'No se pudo guardar el teléfono.'));
      },
    });
  }

  /** Confirma y elimina el teléfono. */
  eliminar(): void {
    const t = this.telefonoAEliminar();
    if (!t) return;
    this.eliminando.set(true);
    this.telefonoService
      .eliminar(t.personaTipoDocumento, t.personaNumeroDocumento, t.telefono)
      .subscribe({
        next: () => {
          this.eliminando.set(false);
          this.telefonoAEliminar.set(null);
          this.notificacionService.exito('Teléfono eliminado.', 'Listo');
          this.cargarTelefonos();
        },
        error: (err) => {
          this.eliminando.set(false);
          this.telefonoAEliminar.set(null);
          this.notificacionService.error(
            extraerError(err, 'No se pudo eliminar el teléfono.'),
            'No se pudo eliminar',
          );
        },
      });
  }
}
