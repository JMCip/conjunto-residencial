import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import {
  CasaModel,
  ESTADOS_CASA,
  ESTILO_CASA,
  ETIQUETA_CASA,
  nombreCasa as nombreMzCasa,
} from '../../../models/casa.model';
import { CasaService } from '../../../services/casa.service';
import { NotificacionService } from '../../../services/notificacion.service';
import { ModalFormulario } from '../../../compartido/modal-formulario/modal-formulario';
import { DialogoConfirmar } from '../../../compartido/dialogo-confirmar/dialogo-confirmar';
import { extraerError } from '../../../compartido/extraer-error';

function nombreCasa(casa: CasaModel): string {
  return nombreMzCasa(casa.manzana, casa.numero);
}

/**
 * @component GestionCasas
 * @description Pestaña Casas de Personas y casas: CRUD básico.
 * Lista las casas en una tabla con buscador, y permite crear, editar y eliminar con un modal.
 */
@Component({
  selector: 'app-gestion-casas',
  imports: [DecimalPipe, FormsModule, ModalFormulario, DialogoConfirmar],
  templateUrl: './gestion-casas.html',
  styleUrl: './gestion-casas.css',
})
export class GestionCasas implements OnInit {
  private casaService = inject(CasaService);
  private notificacionService = inject(NotificacionService);

  /** Casas obtenidas del backend. */
  protected readonly casas = signal<CasaModel[]>([]);

  /** true mientras llega la lista. */
  protected readonly cargando = signal(true);

  /** true si la consulta de la lista falló. */
  protected readonly errorCarga = signal(false);

  /** Texto escrito en el buscador. */
  protected readonly busqueda = signal('');

  /** Casas visibles según la búsqueda ("mz 1", "casa 3", "habitada"…). */
  protected readonly casasFiltradas = computed(() => {
    const texto = this.busqueda().trim().toLowerCase();
    if (!texto) return this.casas();
    return this.casas().filter((c) =>
      `${nombreCasa(c)} ${ETIQUETA_CASA[c.estado] ?? c.estado}`.toLowerCase().includes(texto),
    );
  });

  /** "8 casas" o "2 casas de 8" para el pie de la tabla. */
  protected readonly textoConteo = computed(() => {
    const visibles = this.casasFiltradas().length;
    const total = this.casas().length;
    const palabra = visibles === 1 ? 'casa' : 'casas';
    return visibles === total ? `${visibles} ${palabra}` : `${visibles} ${palabra} de ${total}`;
  });

  // ---------- Modal de crear / editar ----------

  /** null = cerrado; 'crear' o 'editar' = abierto en ese modo. */
  protected readonly modoModal = signal<'crear' | 'editar' | null>(null);

  /** Campos del formulario (enlazados con [(ngModel)]). */
  protected readonly formManzana = signal<number | null>(null);
  protected readonly formNumero = signal<number | null>(null);
  protected readonly formArea = signal<number | null>(null);
  protected readonly formEstado = signal<string>('HABITADA');
  protected readonly formHabitaciones = signal<number | null>(null);

  /** "Nueva casa" o "Editar Mz 1 · Casa 3". */
  protected readonly tituloModal = computed(() =>
    this.modoModal() === 'crear'
      ? 'Nueva casa'
      : `Editar Mz ${this.formManzana()} · Casa ${this.formNumero()}`,
  );

  /** Mensaje de validación del formulario. */
  protected readonly errorFormulario = signal<string | null>(null);

  /** true mientras se envía crear o editar. */
  protected readonly guardando = signal(false);


  protected readonly casaAEliminar = signal<CasaModel | null>(null);


  protected readonly eliminando = signal(false);


  protected readonly estadosCasa = ESTADOS_CASA;
  protected readonly etiquetaCasa = ETIQUETA_CASA;
  protected readonly estiloCasa = ESTILO_CASA;
  protected readonly nombreCasa = nombreCasa;

  ngOnInit(): void {
    this.cargarCasas();
  }

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

  abrirModalCrear(): void {
    this.formManzana.set(null);
    this.formNumero.set(null);
    this.formArea.set(null);
    this.formEstado.set('HABITADA');
    this.formHabitaciones.set(null);
    this.errorFormulario.set(null);
    this.modoModal.set('crear');
  }


  abrirModalEditar(casa: CasaModel): void {
    this.formManzana.set(casa.manzana);
    this.formNumero.set(casa.numero);
    this.formArea.set(casa.area);
    this.formEstado.set(casa.estado);
    this.formHabitaciones.set(casa.numeroHabitaciones);
    this.errorFormulario.set(null);
    this.modoModal.set('editar');
  }

  /** Cierra el modal sin guardar. */
  cerrarModal(): void {
    this.modoModal.set(null);
  }

  /** Valida el formulario y llama a crear o actualizar según el modo. */
  guardar(): void {
    const error = this.validar();
    this.errorFormulario.set(error);
    if (error) return;

    const casa: CasaModel = {
      manzana: this.formManzana()!,
      numero: this.formNumero()!,
      area: this.formArea()!,
      estado: this.formEstado(),
      numeroHabitaciones: this.formHabitaciones(),
    };
    const creando = this.modoModal() === 'crear';
    const peticion = creando
      ? this.casaService.postCrear(casa)
      : this.casaService.putActualizar(casa);

    this.guardando.set(true);
    peticion.subscribe({
      next: () => {
        this.guardando.set(false);
        this.modoModal.set(null);
        this.notificacionService.exito(
          `${nombreCasa(casa)} ${creando ? 'creada' : 'actualizada'} correctamente.`,
        );
        this.cargarCasas();
      },
      error: (err) => {
        this.guardando.set(false);
        this.errorFormulario.set(
          extraerError(err, `No se pudo ${creando ? 'crear' : 'actualizar'} la casa.`),
        );
      },
    });
  }

  /**
   * Pide confirmación antes de eliminar.
   * @param casa Casa elegida en la tabla.
   */
  pedirEliminar(casa: CasaModel): void {
    this.casaAEliminar.set(casa);
  }

  /** Cierra el diálogo sin eliminar. */
  cancelarEliminar(): void {
    this.casaAEliminar.set(null);
  }

  /** Envía el DELETE de la casa confirmada. */
  confirmarEliminar(): void {
    const casa = this.casaAEliminar();
    if (!casa) return;
    this.eliminando.set(true);
    this.casaService.deleteEliminar(casa.manzana, casa.numero).subscribe({
      next: () => {
        this.eliminando.set(false);
        this.casaAEliminar.set(null);
        this.notificacionService.exito(`${nombreCasa(casa)} eliminada correctamente.`);
        this.cargarCasas();
      },
      error: (err) => {
        this.eliminando.set(false);
        this.casaAEliminar.set(null);
        this.notificacionService.error(
          extraerError(err, 'No se pudo eliminar la casa.'),
          'No se eliminó',
        );
      },
    });
  }

  /**
   * Revisa los campos antes de enviar.
   * @returns Mensaje del primer problema, o null si todo está bien.
   */
  private validar(): string | null {
    const enteroPositivo = (n: number | null) => n !== null && Number.isInteger(n) && n > 0;
    if (!enteroPositivo(this.formManzana())) return 'La manzana debe ser un número entero mayor que 0.';
    if (!enteroPositivo(this.formNumero())) return 'El número de casa debe ser un entero mayor que 0.';
    const area = this.formArea();
    if (area === null || area <= 0) return 'El área debe ser mayor que 0.';
    if (!this.formEstado()) return 'Elige el estado de la casa.';
    const habitaciones = this.formHabitaciones();
    if (habitaciones !== null && (!Number.isInteger(habitaciones) || habitaciones < 0)) {
      return 'Las habitaciones deben ser un número entero (o déjalo vacío).';
    }
    return null;
  }
}
