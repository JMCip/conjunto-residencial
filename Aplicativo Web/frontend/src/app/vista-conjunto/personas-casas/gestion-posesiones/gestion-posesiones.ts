import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { DatePipe, DecimalPipe } from '@angular/common';
import { forkJoin } from 'rxjs';
import { PosesionModel } from '../../../models/posesion.model';
import { CasaModel, nombreCasa } from '../../../models/casa.model';
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
import { extraerError } from '../../../compartido/extraer-error';
import { ModalFormulario } from '../../../compartido/modal-formulario/modal-formulario';
import { DialogoConfirmar } from '../../../compartido/dialogo-confirmar/dialogo-confirmar';

/** Campos del formulario (todo como texto). */
interface FormularioPosesion {
  casa: string;
  propietario: string;
  porcentaje: string;
  fechaCompra: string;
  fechaVenta: string;
}

const FORMULARIO_VACIO: FormularioPosesion = {
  casa: '',
  propietario: '',
  porcentaje: '',
  fechaCompra: '',
  fechaVenta: '',
};

/**
 * @component GestionPosesiones
 * @description Pestaña Posesiones: qué propietario es dueño de qué casa, con su porcentaje y
 * las fechas de compra y venta. CRUD con modal; el propietario sale de GET /api/propietarios.
 */
@Component({
  selector: 'app-gestion-posesiones',
  imports: [DatePipe, DecimalPipe, ModalFormulario, DialogoConfirmar],
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

  /** Funciones para la plantilla. */
  protected readonly nombreCasa = nombreCasa;
  protected readonly llavePersona = llavePersona;
  protected readonly nombrePersona = nombrePersona;

  /** Modal de crear/editar. */
  protected readonly modalAbierto = signal(false);

  /** Posesión que se edita (null = crear). */
  protected readonly editando = signal<PosesionModel | null>(null);

  protected readonly formulario = signal<FormularioPosesion>({ ...FORMULARIO_VACIO });
  protected readonly guardando = signal(false);
  protected readonly errorFormulario = signal<string | null>(null);

  /** Posesión pendiente de confirmar para eliminar. */
  protected readonly posesionAEliminar = signal<PosesionModel | null>(null);
  protected readonly eliminando = signal(false);

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

  /**
   * Llave "manzana|numero" de una casa (para el selector).
   * @param manzana Manzana de la casa.
   * @param numero Número de la casa.
   */
  llaveCasa(manzana: number, numero: number): string {
    return `${manzana}|${numero}`;
  }

  /** Abre el modal vacío para crear. */
  abrirCrear(): void {
    this.editando.set(null);
    this.formulario.set({ ...FORMULARIO_VACIO });
    this.errorFormulario.set(null);
    this.modalAbierto.set(true);
  }

  /**
   * Abre el modal con los datos de la posesión.
   * @param p Posesión a editar.
   */
  abrirEditar(p: PosesionModel): void {
    this.editando.set(p);
    this.formulario.set({
      casa: this.llaveCasa(p.casaManzana, p.casaNumero),
      propietario: llavePersona(p.propietarioTipoDocumento, p.propietarioNumeroDocumento),
      porcentaje: p.porcentaje.toString(),
      fechaCompra: p.fechaCompra,
      fechaVenta: p.fechaVenta ?? '',
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
   * @param valor Valor escrito o elegido.
   */
  cambiarCampo(campo: keyof FormularioPosesion, valor: string): void {
    this.formulario.update((f) => ({ ...f, [campo]: valor }));
  }

  /** Valida y guarda (crear o editar). */
  guardar(): void {
    const f = this.formulario();
    if (!f.casa || !f.propietario) {
      this.errorFormulario.set('Elige la casa y el propietario.');
      return;
    }
    const porcentaje = Number(f.porcentaje);
    if (!f.porcentaje.trim() || !(porcentaje > 0) || porcentaje > 100) {
      this.errorFormulario.set('El porcentaje debe ser mayor que 0 y máximo 100.');
      return;
    }
    if (!f.fechaCompra) {
      this.errorFormulario.set('La fecha de compra es obligatoria.');
      return;
    }
    if (f.fechaVenta && f.fechaVenta < f.fechaCompra) {
      this.errorFormulario.set('La fecha de venta no puede ser anterior a la de compra.');
      return;
    }

    const fechaVenta = f.fechaVenta || null;
    const actual = this.editando();
    this.errorFormulario.set(null);
    this.guardando.set(true);

    let peticion;
    if (actual) {
      peticion = this.posesionService.actualizar({
        ...actual,
        porcentaje,
        fechaCompra: f.fechaCompra,
        fechaVenta,
      });
    } else {
      const [manzana, numero] = f.casa.split('|').map(Number);
      const { tipo, numero: documento } = separarLlavePersona(f.propietario);
      peticion = this.posesionService.crear({
        porcentaje,
        fechaCompra: f.fechaCompra,
        fechaVenta,
        casaNumero: numero,
        casaManzana: manzana,
        propietarioTipoDocumento: tipo,
        propietarioNumeroDocumento: documento,
      });
    }

    peticion.subscribe({
      next: () => {
        this.guardando.set(false);
        this.modalAbierto.set(false);
        this.notificacionService.exito(
          actual ? 'Posesión actualizada.' : 'Posesión creada.',
          'Listo',
        );
        this.cargarPosesiones();
      },
      error: (err) => {
        this.guardando.set(false);
        this.errorFormulario.set(extraerError(err, 'No se pudo guardar la posesión.'));
      },
    });
  }

  /** Confirma y elimina la posesión. */
  eliminar(): void {
    const p = this.posesionAEliminar();
    if (!p) return;
    this.eliminando.set(true);
    this.posesionService.eliminar(p.idPosesion).subscribe({
      next: () => {
        this.eliminando.set(false);
        this.posesionAEliminar.set(null);
        this.notificacionService.exito('Posesión eliminada.', 'Listo');
        this.cargarPosesiones();
      },
      error: (err) => {
        this.eliminando.set(false);
        this.posesionAEliminar.set(null);
        this.notificacionService.error(
          extraerError(err, 'No se pudo eliminar la posesión.'),
          'No se pudo eliminar',
        );
      },
    });
  }
}
