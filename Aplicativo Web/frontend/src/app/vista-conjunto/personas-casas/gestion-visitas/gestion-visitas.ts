import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { forkJoin } from 'rxjs';
import { VisitaModel } from '../../../models/visita.model';
import { CasaModel, llaveCasa, nombreCasa, separarLlaveCasa } from '../../../models/casa.model';
import {
  llavePersona,
  nombrePersona,
  PersonaModel,
  separarLlavePersona,
} from '../../../models/persona.model';
import { VisitaService } from '../../../services/visita.service';
import { CasaService } from '../../../services/casa.service';
import { PersonaService } from '../../../services/persona.service';
import { NotificacionService } from '../../../services/notificacion.service';
import { ModalFormulario } from '../../../compartido/modal-formulario/modal-formulario';
import { DialogoConfirmar } from '../../../compartido/dialogo-confirmar/dialogo-confirmar';
import { extraerError } from '../../../compartido/extraer-error';

/**
 * @component GestionVisitas
 * @description Pestaña Visitas: qué visitante fue a qué casa, con fecha de
 * entrada y de salida. El visitante sale de GET /api/visitantes.
 */
@Component({
  selector: 'app-gestion-visitas',
  imports: [DatePipe, FormsModule, ModalFormulario, DialogoConfirmar],
  templateUrl: './gestion-visitas.html',
  styleUrl: './gestion-visitas.css',
})
export class GestionVisitas implements OnInit {
  private visitaService = inject(VisitaService);
  private casaService = inject(CasaService);
  private personaService = inject(PersonaService);
  private notificacionService = inject(NotificacionService);

  /** Visitas obtenidas del backend. */
  protected readonly visitas = signal<VisitaModel[]>([]);

  /** Casas y visitantes para los selectores. */
  protected readonly casas = signal<CasaModel[]>([]);
  protected readonly visitantes = signal<PersonaModel[]>([]);

  /** Nombre de cada visitante por su llave "CC|123". */
  private readonly nombres = computed(
    () =>
      new Map(
        this.visitantes().map((p) => [
          llavePersona(p.tipoDocumento, p.numeroDocumento),
          nombrePersona(p),
        ]),
      ),
  );

  protected readonly cargando = signal(true);
  protected readonly errorCarga = signal(false);
  protected readonly busqueda = signal('');

  /** Visitas visibles según la búsqueda (casa o visitante). */
  protected readonly visitasFiltradas = computed(() => {
    const texto = this.busqueda().trim().toLowerCase();
    if (!texto) return this.visitas();
    return this.visitas().filter((r) =>
      `${nombreCasa(r.casaManzana, r.casaNumero)} ${this.visitante(r)}`
        .toLowerCase()
        .includes(texto),
    );
  });

  /** "10 visitas" o "2 visitas de 10". */
  protected readonly textoConteo = computed(() => {
    const visibles = this.visitasFiltradas().length;
    const total = this.visitas().length;
    const palabra = visibles === 1 ? 'visita' : 'visitas';
    return visibles === total ? `${visibles} ${palabra}` : `${visibles} ${palabra} de ${total}`;
  });

  // ---------- Modal de crear / editar ----------

  protected readonly modoModal = signal<'crear' | 'editar' | null>(null);
  private idEditando = 0;
  protected readonly formCasa = signal('');
  protected readonly formVisitante = signal('');
  protected readonly formFechaEntrada = signal('');
  protected readonly formFechaSalida = signal('');
  protected readonly errorFormulario = signal<string | null>(null);
  protected readonly guardando = signal(false);

  // ---------- Diálogo de eliminar ----------

  protected readonly visitaAEliminar = signal<VisitaModel | null>(null);
  protected readonly eliminando = signal(false);

  /** Funciones para la plantilla. */
  protected readonly nombreCasa = nombreCasa;
  protected readonly llaveCasa = llaveCasa;
  protected readonly llavePersona = llavePersona;
  protected readonly nombrePersona = nombrePersona;

  ngOnInit(): void {
    this.cargarVisitas();
  }

  /** Consulta visitas, casas y visitantes a la vez. */
  cargarVisitas(): void {
    this.cargando.set(true);
    this.errorCarga.set(false);
    forkJoin({
      visitas: this.visitaService.getTodos(),
      casas: this.casaService.getTodos(),
      visitantes: this.personaService.getVisitantes(),
    }).subscribe({
      next: ({ visitas, casas, visitantes }) => {
        this.visitas.set(visitas);
        this.casas.set(casas);
        this.visitantes.set(visitantes);
        this.cargando.set(false);
      },
      error: (err) => {
        this.errorCarga.set(true);
        this.cargando.set(false);
        this.notificacionService.error(
          extraerError(err, 'No se pudieron consultar las visitas.'),
          'Error de conexión',
        );
      },
    });
  }

  /**
   * Nombre del visitante (o su documento si no está en la lista).
   * @param r Visita de la tabla.
   */
  visitante(r: VisitaModel): string {
    return (
      this.nombres().get(llavePersona(r.visitanteTipoDocumento, r.visitanteNumeroDocumento)) ??
      `${r.visitanteTipoDocumento} ${r.visitanteNumeroDocumento}`
    );
  }

  /** Abre el modal vacío. */
  abrirModalCrear(): void {
    this.idEditando = 0;
    this.formCasa.set('');
    this.formVisitante.set('');
    this.formFechaEntrada.set('');
    this.formFechaSalida.set('');
    this.errorFormulario.set(null);
    this.modoModal.set('crear');
  }

  /**
   * Abre el modal con los datos de una visita.
   * @param r Visita elegida en la tabla.
   */
  abrirModalEditar(r: VisitaModel): void {
    this.idEditando = r.idVisita;
    this.formCasa.set(llaveCasa(r.casaManzana, r.casaNumero));
    this.formVisitante.set(llavePersona(r.visitanteTipoDocumento, r.visitanteNumeroDocumento));
    this.formFechaEntrada.set(r.fechaEntrada);
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
    const visitante = separarLlavePersona(this.formVisitante());
    const datos = {
      fechaEntrada: this.formFechaEntrada(),
      fechaSalida: this.formFechaSalida() || null,
      visitanteTipoDocumento: visitante.tipo,
      visitanteNumeroDocumento: visitante.numero,
      casaManzana: casa.manzana,
      casaNumero: casa.numero,
    };
    const creando = this.modoModal() === 'crear';
    const peticion = creando
      ? this.visitaService.postCrear(datos)
      : this.visitaService.putActualizar({ idVisita: this.idEditando, ...datos });

    this.guardando.set(true);
    peticion.subscribe({
      next: () => {
        this.guardando.set(false);
        this.modoModal.set(null);
        this.notificacionService.exito(
          `Visita en ${nombreCasa(casa.manzana, casa.numero)} ${creando ? 'registrada' : 'actualizada'} correctamente.`,
        );
        this.cargarVisitas();
      },
      error: (err) => {
        this.guardando.set(false);
        this.errorFormulario.set(
          extraerError(err, `No se pudo ${creando ? 'registrar' : 'actualizar'} la visita.`),
        );
      },
    });
  }

  /** Pide confirmación antes de eliminar. */
  pedirEliminar(r: VisitaModel): void {
    this.visitaAEliminar.set(r);
  }

  /** Cierra el diálogo sin eliminar. */
  cancelarEliminar(): void {
    this.visitaAEliminar.set(null);
  }

  /** Envía el DELETE de la visita confirmada. */
  confirmarEliminar(): void {
    const r = this.visitaAEliminar();
    if (!r) return;
    this.eliminando.set(true);
    this.visitaService.deleteEliminar(r.idVisita).subscribe({
      next: () => {
        this.eliminando.set(false);
        this.visitaAEliminar.set(null);
        this.notificacionService.exito('Visita eliminada correctamente.');
        this.cargarVisitas();
      },
      error: (err) => {
        this.eliminando.set(false);
        this.visitaAEliminar.set(null);
        this.notificacionService.error(
          extraerError(err, 'No se pudo eliminar la visita.'),
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
    if (!this.formVisitante()) return 'Elige el visitante.';
    if (!this.formFechaEntrada()) return 'Escribe la fecha de entrada.';
    if (this.formFechaSalida() && this.formFechaSalida() < this.formFechaEntrada()) {
      return 'La fecha de salida no puede ser anterior a la de entrada.';
    }
    return null;
  }
}
