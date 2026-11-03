import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { DatePipe, DecimalPipe } from '@angular/common';
import { forkJoin } from 'rxjs';
import { PosesionModel } from '../../../models/posesion.model';
import { CasaModel, nombreCasa } from '../../../models/casa.model';
import {
  llavePersona,
  nombrePersona,
  PersonaModel,
} from '../../../models/persona.model';
import { PosesionService } from '../../../services/posesion.service';
import { CasaService } from '../../../services/casa.service';
import { PersonaService } from '../../../services/persona.service';
import { NotificacionService } from '../../../services/notificacion.service';
import { extraerError } from '../../../compartido/extraer-error';

/**
 * @component GestionPosesiones
 * @description Pestaña Posesiones: qué propietario es dueño de qué casa, con su porcentaje y
 * las fechas de compra y venta. CRUD con modal; el propietario sale de GET /api/propietarios.
 */
@Component({
  selector: 'app-gestion-posesiones',
  imports: [DatePipe, DecimalPipe],
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
}
