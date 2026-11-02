import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { forkJoin } from 'rxjs';
import { ResidenciaModel } from '../../../models/residencia.model';
import { CasaModel, llaveCasa, nombreCasa, separarLlaveCasa } from '../../../models/casa.model';
import {
  llavePersona,
  nombrePersona,
  PersonaModel,
  separarLlavePersona,
} from '../../../models/persona.model';
import { ResidenciaService } from '../../../services/residencia.service';
import { CasaService } from '../../../services/casa.service';
import { PersonaService } from '../../../services/persona.service';
import { NotificacionService } from '../../../services/notificacion.service';
import { ModalFormulario } from '../../../compartido/modal-formulario/modal-formulario';
import { DialogoConfirmar } from '../../../compartido/dialogo-confirmar/dialogo-confirmar';
import { extraerError } from '../../../compartido/extraer-error';

/**
 * @component GestionResidencias
 * @description Pestaña Residencias: en qué casa vive (o vivió) cada residente, con fecha de
 * ingreso y de salida. El residente sale de GET /api/residentes.
 */
@Component({
  selector: 'app-gestion-residencias',
  imports: [DatePipe, FormsModule, ModalFormulario, DialogoConfirmar],
  templateUrl: './gestion-residencias.html',
  styleUrl: './gestion-residencias.css',
})
export class GestionResidencias implements OnInit {
  private residenciaService = inject(ResidenciaService);
  private casaService = inject(CasaService);
  private personaService = inject(PersonaService);
  private notificacionService = inject(NotificacionService);

  /** Residencias obtenidas del backend. */
  protected readonly residencias = signal<ResidenciaModel[]>([]);

  /** Casas y residentes para los selectores. */
  protected readonly casas = signal<CasaModel[]>([]);
  protected readonly residentes = signal<PersonaModel[]>([]);

  /** Nombre de cada residente por su llave "CC|123". */
  private readonly nombres = computed(
    () =>
      new Map(
        this.residentes().map((p) => [
          llavePersona(p.tipoDocumento, p.numeroDocumento),
          nombrePersona(p),
        ]),
      ),
  );

  protected readonly cargando = signal(true);
  protected readonly errorCarga = signal(false);
  protected readonly busqueda = signal('');

  /** Residencias visibles según la búsqueda (casa o residente). */
  protected readonly residenciasFiltradas = computed(() => {
    const texto = this.busqueda().trim().toLowerCase();
    if (!texto) return this.residencias();
    return this.residencias().filter((r) =>
      `${nombreCasa(r.casaManzana, r.casaNumero)} ${this.residente(r)}`
        .toLowerCase()
        .includes(texto),
    );
  });

  /** "10 residencias" o "2 residencias de 10". */
  protected readonly textoConteo = computed(() => {
    const visibles = this.residenciasFiltradas().length;
    const total = this.residencias().length;
    const palabra = visibles === 1 ? 'residencia' : 'residencias';
    return visibles === total ? `${visibles} ${palabra}` : `${visibles} ${palabra} de ${total}`;
  });

  // ---------- Modal de crear / editar ----------

  protected readonly modoModal = signal<'crear' | 'editar' | null>(null);
  private idEditando = 0;
  protected readonly formCasa = signal('');
  protected readonly formResidente = signal('');
  protected readonly formFechaIngreso = signal('');
  protected readonly formFechaSalida = signal('');
  protected readonly errorFormulario = signal<string | null>(null);
  protected readonly guardando = signal(false);

  // ---------- Diálogo de eliminar ----------

  protected readonly residenciaAEliminar = signal<ResidenciaModel | null>(null);
  protected readonly eliminando = signal(false);

  /** Funciones para la plantilla. */
  protected readonly nombreCasa = nombreCasa;
  protected readonly llaveCasa = llaveCasa;
  protected readonly llavePersona = llavePersona;
  protected readonly nombrePersona = nombrePersona;

  ngOnInit(): void {
    this.cargarResidencias();
  }

  /** Consulta residencias, casas y residentes a la vez. */
  cargarResidencias(): void {
    this.cargando.set(true);
    this.errorCarga.set(false);
    forkJoin({
      residencias: this.residenciaService.getTodos(),
      casas: this.casaService.getTodos(),
      residentes: this.personaService.getResidentes(),
    }).subscribe({
      next: ({ residencias, casas, residentes }) => {
        this.residencias.set(residencias);
        this.casas.set(casas);
        this.residentes.set(residentes);
        this.cargando.set(false);
      },
      error: (err) => {
        this.errorCarga.set(true);
        this.cargando.set(false);
        this.notificacionService.error(
          extraerError(err, 'No se pudieron consultar las residencias.'),
          'Error de conexión',
        );
      },
    });
  }

  /**
   * Nombre del residente (o su documento si no está en la lista).
   * @param r Residencia de la tabla.
   */
  residente(r: ResidenciaModel): string {
    return (
      this.nombres().get(llavePersona(r.residenteTipoDocumento, r.residenteNumeroDocumento)) ??
      `${r.residenteTipoDocumento} ${r.residenteNumeroDocumento}`
    );
  }

  /** Abre el modal vacío. */
  abrirModalCrear(): void {
    this.idEditando = 0;
    this.formCasa.set('');
    this.formResidente.set('');
    this.formFechaIngreso.set('');
    this.formFechaSalida.set('');
    this.errorFormulario.set(null);
    this.modoModal.set('crear');
  }

  /**
   * Abre el modal con los datos de una residencia.
   * @param r Residencia elegida en la tabla.
   */
  abrirModalEditar(r: ResidenciaModel): void {
    this.idEditando = r.idResidencia;
    this.formCasa.set(llaveCasa(r.casaManzana, r.casaNumero));
    this.formResidente.set(llavePersona(r.residenteTipoDocumento, r.residenteNumeroDocumento));
    this.formFechaIngreso.set(r.fechaIngreso);
    this.formFechaSalida.set(r.fechaSalida ?? '');
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
    const residente = separarLlavePersona(this.formResidente());
    const datos = {
      fechaIngreso: this.formFechaIngreso(),
      fechaSalida: this.formFechaSalida() || null,
      residenteTipoDocumento: residente.tipo,
      residenteNumeroDocumento: residente.numero,
      casaManzana: casa.manzana,
      casaNumero: casa.numero,
    };
    const creando = this.modoModal() === 'crear';
    const peticion = creando
      ? this.residenciaService.postCrear(datos)
      : this.residenciaService.putActualizar({ idResidencia: this.idEditando, ...datos });

    this.guardando.set(true);
    peticion.subscribe({
      next: () => {
        this.guardando.set(false);
        this.modoModal.set(null);
        this.notificacionService.exito(
          `Residencia en ${nombreCasa(casa.manzana, casa.numero)} ${creando ? 'registrada' : 'actualizada'} correctamente.`,
        );
        this.cargarResidencias();
      },
      error: (err) => {
        this.guardando.set(false);
        this.errorFormulario.set(
          extraerError(err, `No se pudo ${creando ? 'registrar' : 'actualizar'} la residencia.`),
        );
      },
    });
  }

  /** Pide confirmación antes de eliminar. */
  pedirEliminar(r: ResidenciaModel): void {
    this.residenciaAEliminar.set(r);
  }

  /** Cierra el diálogo sin eliminar. */
  cancelarEliminar(): void {
    this.residenciaAEliminar.set(null);
  }

  /** Envía el DELETE de la residencia confirmada. */
  confirmarEliminar(): void {
    const r = this.residenciaAEliminar();
    if (!r) return;
    this.eliminando.set(true);
    this.residenciaService.deleteEliminar(r.idResidencia).subscribe({
      next: () => {
        this.eliminando.set(false);
        this.residenciaAEliminar.set(null);
        this.notificacionService.exito('Residencia eliminada correctamente.');
        this.cargarResidencias();
      },
      error: (err) => {
        this.eliminando.set(false);
        this.residenciaAEliminar.set(null);
        this.notificacionService.error(
          extraerError(err, 'No se pudo eliminar la residencia.'),
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
    if (!this.formResidente()) return 'Elige el residente.';
    if (!this.formFechaIngreso()) return 'Escribe la fecha de ingreso.';
    if (this.formFechaSalida() && this.formFechaSalida() < this.formFechaIngreso()) {
      return 'La fecha de salida no puede ser anterior a la de ingreso.';
    }
    return null;
  }
}
