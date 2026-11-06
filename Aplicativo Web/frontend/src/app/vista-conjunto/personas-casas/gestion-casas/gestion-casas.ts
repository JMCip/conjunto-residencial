import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { CasaModel, nombreCasa as nombreMzCasa } from '../../../models/casa.model';
import { CasaService } from '../../../services/casa.service';
import { NotificacionService } from '../../../services/notificacion.service';
import { extraerError } from '../../../compartido/extraer-error';
import { ModalFormulario } from '../../../compartido/modal-formulario/modal-formulario';
import { DialogoConfirmar } from '../../../compartido/dialogo-confirmar/dialogo-confirmar';

/**
 * Nombre de una casa a partir del registro completo.
 * @param casa Casa.
 * @returns Texto con el formato "Mz 1 · Casa 3".
 */
function nombreCasa(casa: CasaModel): string {
  return nombreMzCasa(casa.manzana, casa.numero);
}

/** Campos del formulario (todo como texto; los vacíos se pasan a null al guardar). */
interface FormularioCasa {
  manzana: string;
  numero: string;
  area: string;
  numeroHabitaciones: string;
}

/** Valores iniciales del formulario (todo vacío). */
const FORMULARIO_VACIO: FormularioCasa = {
  manzana: '',
  numero: '',
  area: '',
  numeroHabitaciones: '',
};

/**
 * @component GestionCasas
 * @description Pestaña Casas de Personas y casas: CRUD completo.
 * Lista las casas en una tabla con buscador, y permite crear, editar y eliminar con un modal.
 * La llave (manzana + número) no se cambia al editar.
 */
@Component({
  selector: 'app-gestion-casas',
  imports: [DecimalPipe, ModalFormulario, DialogoConfirmar],
  templateUrl: './gestion-casas.html',
  styleUrl: './gestion-casas.css',
})
export class GestionCasas implements OnInit {
  /** Servicio de casas. */
  private casaService = inject(CasaService);
  /** Servicio de avisos flotantes. */
  private notificacionService = inject(NotificacionService);

  /** Lista de casas consultada al backend. */
  protected readonly casas = signal<CasaModel[]>([]);
  /** true mientras se consulta la información. */
  protected readonly cargando = signal(true);
  /** true si la consulta de la información falló. */
  protected readonly errorCarga = signal(false);
  /** Texto escrito en el buscador. */
  protected readonly busqueda = signal('');

  /** Casas visibles según la búsqueda ("mz 1", "casa 3"…). */
  protected readonly casasFiltradas = computed(() => {
    const texto = this.busqueda().trim().toLowerCase();
    if (!texto) return this.casas();
    return this.casas().filter((c) => nombreCasa(c).toLowerCase().includes(texto));
  });

  /** Texto del pie de la tabla con la cantidad de registros visibles. */
  protected readonly textoConteo = computed(() => {
    const visibles = this.casasFiltradas().length;
    const total = this.casas().length;
    const palabra = visibles === 1 ? 'casa' : 'casas';
    return visibles === total ? `${visibles} ${palabra}` : `${visibles} ${palabra} de ${total}`;
  });

  /** Función del modelo para mostrar el nombre de una casa en la plantilla. */
  protected readonly nombreCasa = nombreCasa;

  /** Modal de crear/editar. */
  protected readonly modalAbierto = signal(false);

  /** Casa que se edita (null = crear). */
  protected readonly editando = signal<CasaModel | null>(null);

  /** Valores actuales del formulario del modal. */
  protected readonly formulario = signal<FormularioCasa>({ ...FORMULARIO_VACIO });
  /** true mientras se envía el formulario al backend. */
  protected readonly guardando = signal(false);
  /** Mensaje de validación del formulario (null si no hay error). */
  protected readonly errorFormulario = signal<string | null>(null);

  /** Casa pendiente de confirmar para eliminar. */
  protected readonly casaAEliminar = signal<CasaModel | null>(null);
  /** true mientras se envía la eliminación al backend. */
  protected readonly eliminando = signal(false);

  /** Consulta los datos al abrir la pantalla. */
  ngOnInit(): void {
    this.cargarCasas();
  }

  /** Consulta la información de la pantalla y actualiza el estado de la vista. */
  cargarCasas(): void {
    this.cargando.set(true);
    this.errorCarga.set(false);
    this.casaService.getTodos().subscribe({
      next: (datos) => {
        this.casas.set(datos);
        this.cargando.set(false);
      },
      error: (err) => {
        this.errorCarga.set(true);
        this.cargando.set(false);
        this.notificacionService.error(
          extraerError(err, 'No se pudieron consultar las casas.'),
          'Error de conexión',
        );
      },
    });
  }

  /** Abre el modal vacío para crear. */
  abrirCrear(): void {
    this.editando.set(null);
    this.formulario.set({ ...FORMULARIO_VACIO });
    this.errorFormulario.set(null);
    this.modalAbierto.set(true);
  }

  /**
   * Abre el modal con los datos de la casa.
   * @param c Casa a editar.
   */
  abrirEditar(c: CasaModel): void {
    this.editando.set(c);
    this.formulario.set({
      manzana: c.manzana.toString(),
      numero: c.numero.toString(),
      area: c.area.toString(),
      numeroHabitaciones: c.numeroHabitaciones?.toString() ?? '',
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
  cambiarCampo(campo: keyof FormularioCasa, valor: string): void {
    this.formulario.update((f) => ({ ...f, [campo]: valor }));
  }

  /** Valida y guarda (crear o editar). */
  guardar(): void {
    const f = this.formulario();

    const manzana = Number(f.manzana);
    if (!f.manzana.trim() || !Number.isInteger(manzana) || manzana < 1) {
      this.errorFormulario.set('La manzana debe ser un número entero mayor a 0.');
      return;
    }
    const numero = Number(f.numero);
    if (!f.numero.trim() || !Number.isInteger(numero) || numero < 1) {
      this.errorFormulario.set('El número de casa debe ser un entero mayor a 0.');
      return;
    }
    const area = Number(f.area.trim().replace(',', '.'));
    if (!f.area.trim() || !Number.isFinite(area) || area <= 0) {
      this.errorFormulario.set('El área debe ser un número mayor a 0.');
      return;
    }
    let habitaciones: number | null = null;
    if (f.numeroHabitaciones.trim()) {
      habitaciones = Number(f.numeroHabitaciones);
      if (!Number.isInteger(habitaciones) || habitaciones < 0) {
        this.errorFormulario.set('Las habitaciones deben ser un entero igual o mayor a 0.');
        return;
      }
    }

    const casa: CasaModel = { numero, manzana, area, numeroHabitaciones: habitaciones };

    const editado = this.editando() !== null;
    this.errorFormulario.set(null);
    this.guardando.set(true);
    const peticion = editado
      ? this.casaService.actualizar(casa)
      : this.casaService.crear(casa);

    peticion.subscribe({
      next: () => {
        this.guardando.set(false);
        this.modalAbierto.set(false);
        this.notificacionService.exito(editado ? 'Casa actualizada.' : 'Casa creada.', 'Listo');
        this.cargarCasas();
      },
      error: (err) => {
        this.guardando.set(false);
        this.errorFormulario.set(extraerError(err, 'No se pudo guardar la casa.'));
      },
    });
  }

  /** Confirma y elimina la casa. */
  eliminar(): void {
    const c = this.casaAEliminar();
    if (!c) return;
    this.eliminando.set(true);
    this.casaService.eliminar(c.manzana, c.numero).subscribe({
      next: () => {
        this.eliminando.set(false);
        this.casaAEliminar.set(null);
        this.notificacionService.exito('Casa eliminada.', 'Listo');
        this.cargarCasas();
      },
      error: (err) => {
        this.eliminando.set(false);
        this.casaAEliminar.set(null);
        this.notificacionService.error(
          extraerError(err, 'No se pudo eliminar la casa.'),
          'No se pudo eliminar',
        );
      },
    });
  }
}
