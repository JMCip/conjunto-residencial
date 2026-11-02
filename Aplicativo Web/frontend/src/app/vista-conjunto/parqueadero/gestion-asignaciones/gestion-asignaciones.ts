import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { forkJoin } from 'rxjs';
import { AsignacionCeldaModel } from '../../../models/asignacion-celda.model';
import { CasaModel, llaveCasa, nombreCasa, separarLlaveCasa } from '../../../models/casa.model';
import {
  CeldaParqueaderoModel,
  ETIQUETA_CELDA,
  nombreCelda,
} from '../../../models/celda-parqueadero.model';
import { AsignacionCeldaService } from '../../../services/asignacion-celda.service';
import { CasaService } from '../../../services/casa.service';
import { CeldaParqueaderoService } from '../../../services/celda-parqueadero.service';
import { NotificacionService } from '../../../services/notificacion.service';
import { ModalFormulario } from '../../../compartido/modal-formulario/modal-formulario';
import { DialogoConfirmar } from '../../../compartido/dialogo-confirmar/dialogo-confirmar';
import { extraerError } from '../../../compartido/extraer-error';

/**
 * @component GestionAsignaciones
 * @description Pestaña Asignaciones de Parqueadero: qué celda tiene asignada cada casa y desde
 * cuándo. CRUD con modal; casas y celdas salen de sus propios GET.
 */
@Component({
  selector: 'app-gestion-asignaciones',
  imports: [DatePipe, FormsModule, ModalFormulario, DialogoConfirmar],
  templateUrl: './gestion-asignaciones.html',
  styleUrl: './gestion-asignaciones.css',
})
export class GestionAsignaciones implements OnInit {
  private asignacionService = inject(AsignacionCeldaService);
  private casaService = inject(CasaService);
  private celdaService = inject(CeldaParqueaderoService);
  private notificacionService = inject(NotificacionService);

  /** Asignaciones obtenidas del backend. */
  protected readonly asignaciones = signal<AsignacionCeldaModel[]>([]);

  /** Casas y celdas para los selectores. */
  protected readonly casas = signal<CasaModel[]>([]);
  protected readonly celdas = signal<CeldaParqueaderoModel[]>([]);

  protected readonly cargando = signal(true);
  protected readonly errorCarga = signal(false);
  protected readonly busqueda = signal('');

  /** Asignaciones visibles según la búsqueda (celda o casa). */
  protected readonly asignacionesFiltradas = computed(() => {
    const texto = this.busqueda().trim().toLowerCase();
    if (!texto) return this.asignaciones();
    return this.asignaciones().filter((a) =>
      `${nombreCelda(a.idCelda)} ${nombreCasa(a.casaManzana, a.casaNumero)}`
        .toLowerCase()
        .includes(texto),
    );
  });

  /** "8 asignaciones" o "2 asignaciones de 8". */
  protected readonly textoConteo = computed(() => {
    const visibles = this.asignacionesFiltradas().length;
    const total = this.asignaciones().length;
    const palabra = visibles === 1 ? 'asignación' : 'asignaciones';
    return visibles === total ? `${visibles} ${palabra}` : `${visibles} ${palabra} de ${total}`;
  });

  // ---------- Modal de crear / editar ----------

  protected readonly modoModal = signal<'crear' | 'editar' | null>(null);
  private idEditando = 0;
  protected readonly formCasa = signal('');
  protected readonly formCelda = signal<number | null>(null);
  protected readonly formFechaInicio = signal('');
  protected readonly formFechaFin = signal('');
  protected readonly errorFormulario = signal<string | null>(null);
  protected readonly guardando = signal(false);

  // ---------- Diálogo de eliminar ----------

  protected readonly asignacionAEliminar = signal<AsignacionCeldaModel | null>(null);
  protected readonly eliminando = signal(false);

  /** Constantes y funciones para la plantilla. */
  protected readonly nombreCasa = nombreCasa;
  protected readonly llaveCasa = llaveCasa;
  protected readonly nombreCelda = nombreCelda;
  protected readonly etiquetaCelda = ETIQUETA_CELDA;

  ngOnInit(): void {
    this.cargarAsignaciones();
  }

  /** Consulta asignaciones, casas y celdas a la vez. */
  cargarAsignaciones(): void {
    this.cargando.set(true);
    this.errorCarga.set(false);
    forkJoin({
      asignaciones: this.asignacionService.getTodos(),
      casas: this.casaService.getTodos(),
      celdas: this.celdaService.getTodos(),
    }).subscribe({
      next: ({ asignaciones, casas, celdas }) => {
        this.asignaciones.set(asignaciones);
        this.casas.set(casas);
        this.celdas.set(celdas);
        this.cargando.set(false);
      },
      error: (err) => {
        this.errorCarga.set(true);
        this.cargando.set(false);
        this.notificacionService.error(
          extraerError(err, 'No se pudieron consultar las asignaciones.'),
          'Error de conexión',
        );
      },
    });
  }

  /** Abre el modal vacío. */
  abrirModalCrear(): void {
    this.idEditando = 0;
    this.formCasa.set('');
    this.formCelda.set(null);
    this.formFechaInicio.set('');
    this.formFechaFin.set('');
    this.errorFormulario.set(null);
    this.modoModal.set('crear');
  }

  /**
   * Abre el modal con los datos de una asignación.
   * @param a Asignación elegida en la tabla.
   */
  abrirModalEditar(a: AsignacionCeldaModel): void {
    this.idEditando = a.idAsignacion;
    this.formCasa.set(llaveCasa(a.casaManzana, a.casaNumero));
    this.formCelda.set(a.idCelda);
    this.formFechaInicio.set(a.fechaInicio);
    this.formFechaFin.set(a.fechaFin ?? '');
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

    const casa = separarLlaveCasa(this.formCasa());
    const datos = {
      fechaInicio: this.formFechaInicio(),
      fechaFin: this.formFechaFin() || null,
      casaManzana: casa.manzana,
      casaNumero: casa.numero,
      idCelda: Number(this.formCelda()),
    };
    const creando = this.modoModal() === 'crear';
    const peticion = creando
      ? this.asignacionService.postCrear(datos)
      : this.asignacionService.putActualizar({ idAsignacion: this.idEditando, ...datos });

    this.guardando.set(true);
    peticion.subscribe({
      next: () => {
        this.guardando.set(false);
        this.modoModal.set(null);
        this.notificacionService.exito(
          `${nombreCelda(datos.idCelda)} ${creando ? 'asignada' : 'actualizada'} a ${nombreCasa(casa.manzana, casa.numero)}.`,
        );
        this.cargarAsignaciones();
      },
      error: (err) => {
        this.guardando.set(false);
        this.errorFormulario.set(
          extraerError(err, `No se pudo ${creando ? 'registrar' : 'actualizar'} la asignación.`),
        );
      },
    });
  }

  /** Pide confirmación antes de eliminar. */
  pedirEliminar(a: AsignacionCeldaModel): void {
    this.asignacionAEliminar.set(a);
  }

  /** Cierra el diálogo sin eliminar. */
  cancelarEliminar(): void {
    this.asignacionAEliminar.set(null);
  }

  /** Envía el DELETE de la asignación confirmada. */
  confirmarEliminar(): void {
    const a = this.asignacionAEliminar();
    if (!a) return;
    this.eliminando.set(true);
    this.asignacionService.deleteEliminar(a.idAsignacion).subscribe({
      next: () => {
        this.eliminando.set(false);
        this.asignacionAEliminar.set(null);
        this.notificacionService.exito('Asignación eliminada correctamente.');
        this.cargarAsignaciones();
      },
      error: (err) => {
        this.eliminando.set(false);
        this.asignacionAEliminar.set(null);
        this.notificacionService.error(
          extraerError(err, 'No se pudo eliminar la asignación.'),
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
    if (!this.formCasa()) return 'Elige la casa.';
    if (this.formCelda() === null) return 'Elige la celda.';
    if (!this.formFechaInicio()) return 'Escribe la fecha de inicio.';
    if (this.formFechaFin() && this.formFechaFin() < this.formFechaInicio()) {
      return 'La fecha de fin no puede ser anterior a la de inicio.';
    }
    return null;
  }
}
