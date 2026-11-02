import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { DatePipe, DecimalPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { forkJoin } from 'rxjs';
import { PosesionModel } from '../../../models/posesion.model';
import { CasaModel, llaveCasa, nombreCasa, separarLlaveCasa } from '../../../models/casa.model';
import {
  llavePersona,
  nombrePersona,
  PersonaModel,
  separarLlavePersona,
} from '../../../models/persona.model';
import { PosesionService } from '../../../services/posesion.service';
import { CasaService } from '../../../services/casa.service';
import { PersonaService } from '../../../services/persona.service';
import { NotificacionService } from '../../../services/notificacion.service';
import { ModalFormulario } from '../../../compartido/modal-formulario/modal-formulario';
import { DialogoConfirmar } from '../../../compartido/dialogo-confirmar/dialogo-confirmar';
import { extraerError } from '../../../compartido/extraer-error';

/**
 * @component GestionPosesiones
 * @description Pestaña Posesiones: qué propietario es dueño de qué casa, con su porcentaje y
 * las fechas de compra y venta. CRUD con modal; el propietario sale de GET /api/propietarios.
 */
@Component({
  selector: 'app-gestion-posesiones',
  imports: [DatePipe, DecimalPipe, FormsModule, ModalFormulario, DialogoConfirmar],
  templateUrl: './gestion-posesiones.html',
  styleUrl: './gestion-posesiones.css',
})
export class GestionPosesiones implements OnInit {
  private posesionService = inject(PosesionService);
  private casaService = inject(CasaService);
  private personaService = inject(PersonaService);
  private notificacionService = inject(NotificacionService);

  /** Posesiones obtenidas del backend. */
  protected readonly posesiones = signal<PosesionModel[]>([]);

  /** Casas y propietarios, para los selectores y para mostrar nombres. */
  protected readonly casas = signal<CasaModel[]>([]);
  protected readonly propietarios = signal<PersonaModel[]>([]);

  /** Nombre de cada propietario por su llave "CC|123". */
  private readonly nombres = computed(
    () =>
      new Map(
        this.propietarios().map((p) => [
          llavePersona(p.tipoDocumento, p.numeroDocumento),
          nombrePersona(p),
        ]),
      ),
  );

  protected readonly cargando = signal(true);
  protected readonly errorCarga = signal(false);
  protected readonly busqueda = signal('');

  /** Posesiones visibles según la búsqueda (casa o propietario). */
  protected readonly posesionesFiltradas = computed(() => {
    const texto = this.busqueda().trim().toLowerCase();
    if (!texto) return this.posesiones();
    return this.posesiones().filter((p) =>
      `${nombreCasa(p.casaManzana, p.casaNumero)} ${this.propietario(p)}`
        .toLowerCase()
        .includes(texto),
    );
  });

  /** "9 posesiones" o "2 posesiones de 9". */
  protected readonly textoConteo = computed(() => {
    const visibles = this.posesionesFiltradas().length;
    const total = this.posesiones().length;
    const palabra = visibles === 1 ? 'posesión' : 'posesiones';
    return visibles === total ? `${visibles} ${palabra}` : `${visibles} ${palabra} de ${total}`;
  });

  // ---------- Modal de crear / editar ----------

  protected readonly modoModal = signal<'crear' | 'editar' | null>(null);
  /** Id de la posesión que se edita (va en la URL del PUT). */
  private idEditando = 0;
  protected readonly formCasa = signal('');
  protected readonly formPropietario = signal('');
  protected readonly formPorcentaje = signal<number | null>(100);
  protected readonly formFechaCompra = signal('');
  protected readonly formFechaVenta = signal('');
  protected readonly errorFormulario = signal<string | null>(null);
  protected readonly guardando = signal(false);

  // ---------- Diálogo de eliminar ----------

  protected readonly posesionAEliminar = signal<PosesionModel | null>(null);
  protected readonly eliminando = signal(false);

  /** Funciones para la plantilla. */
  protected readonly nombreCasa = nombreCasa;
  protected readonly llaveCasa = llaveCasa;
  protected readonly llavePersona = llavePersona;
  protected readonly nombrePersona = nombrePersona;

  ngOnInit(): void {
    this.cargarPosesiones();
  }

  /** Consulta posesiones, casas y propietarios a la vez. */
  cargarPosesiones(): void {
    this.cargando.set(true);
    this.errorCarga.set(false);
    forkJoin({
      posesiones: this.posesionService.getTodos(),
      casas: this.casaService.getTodos(),
      propietarios: this.personaService.getPropietarios(),
    }).subscribe({
      next: ({ posesiones, casas, propietarios }) => {
        this.posesiones.set(posesiones);
        this.casas.set(casas);
        this.propietarios.set(propietarios);
        this.cargando.set(false);
      },
      error: (err) => {
        this.errorCarga.set(true);
        this.cargando.set(false);
        this.notificacionService.error(
          extraerError(err, 'No se pudieron consultar las posesiones.'),
          'Error de conexión',
        );
      },
    });
  }

  /**
   * Nombre del propietario de una posesión (o su documento si no está en la lista).
   * @param p Posesión de la tabla.
   */
  propietario(p: PosesionModel): string {
    return (
      this.nombres().get(llavePersona(p.propietarioTipoDocumento, p.propietarioNumeroDocumento)) ??
      `${p.propietarioTipoDocumento} ${p.propietarioNumeroDocumento}`
    );
  }

  /** Abre el modal vacío. */
  abrirModalCrear(): void {
    this.idEditando = 0;
    this.formCasa.set('');
    this.formPropietario.set('');
    this.formPorcentaje.set(100);
    this.formFechaCompra.set('');
    this.formFechaVenta.set('');
    this.errorFormulario.set(null);
    this.modoModal.set('crear');
  }

  /**
   * Abre el modal con los datos de una posesión.
   * @param p Posesión elegida en la tabla.
   */
  abrirModalEditar(p: PosesionModel): void {
    this.idEditando = p.idPosesion;
    this.formCasa.set(llaveCasa(p.casaManzana, p.casaNumero));
    this.formPropietario.set(llavePersona(p.propietarioTipoDocumento, p.propietarioNumeroDocumento));
    this.formPorcentaje.set(p.porcentaje);
    this.formFechaCompra.set(p.fechaCompra);
    this.formFechaVenta.set(p.fechaVenta ?? '');
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
    const propietario = separarLlavePersona(this.formPropietario());
    const datos = {
      porcentaje: this.formPorcentaje()!,
      fechaCompra: this.formFechaCompra(),
      fechaVenta: this.formFechaVenta() || null,
      casaManzana: casa.manzana,
      casaNumero: casa.numero,
      propietarioTipoDocumento: propietario.tipo,
      propietarioNumeroDocumento: propietario.numero,
    };
    const creando = this.modoModal() === 'crear';
    const peticion = creando
      ? this.posesionService.postCrear(datos)
      : this.posesionService.putActualizar({ idPosesion: this.idEditando, ...datos });

    this.guardando.set(true);
    peticion.subscribe({
      next: () => {
        this.guardando.set(false);
        this.modoModal.set(null);
        this.notificacionService.exito(
          `Posesión de ${nombreCasa(casa.manzana, casa.numero)} ${creando ? 'registrada' : 'actualizada'} correctamente.`,
        );
        this.cargarPosesiones();
      },
      error: (err) => {
        this.guardando.set(false);
        this.errorFormulario.set(
          extraerError(err, `No se pudo ${creando ? 'registrar' : 'actualizar'} la posesión.`),
        );
      },
    });
  }

  /** Pide confirmación antes de eliminar. */
  pedirEliminar(p: PosesionModel): void {
    this.posesionAEliminar.set(p);
  }

  /** Cierra el diálogo sin eliminar. */
  cancelarEliminar(): void {
    this.posesionAEliminar.set(null);
  }

  /** Envía el DELETE de la posesión confirmada. */
  confirmarEliminar(): void {
    const p = this.posesionAEliminar();
    if (!p) return;
    this.eliminando.set(true);
    this.posesionService.deleteEliminar(p.idPosesion).subscribe({
      next: () => {
        this.eliminando.set(false);
        this.posesionAEliminar.set(null);
        this.notificacionService.exito('Posesión eliminada correctamente.');
        this.cargarPosesiones();
      },
      error: (err) => {
        this.eliminando.set(false);
        this.posesionAEliminar.set(null);
        this.notificacionService.error(
          extraerError(err, 'No se pudo eliminar la posesión.'),
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
    if (!this.formPropietario()) return 'Elige el propietario.';
    const porcentaje = this.formPorcentaje();
    if (porcentaje === null || porcentaje <= 0 || porcentaje > 100) {
      return 'El porcentaje debe ser mayor que 0 y máximo 100.';
    }
    if (!this.formFechaCompra()) return 'Escribe la fecha de compra.';
    if (this.formFechaVenta() && this.formFechaVenta() < this.formFechaCompra()) {
      return 'La fecha de venta no puede ser anterior a la de compra.';
    }
    return null;
  }
}
