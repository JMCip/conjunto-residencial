import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { forkJoin } from 'rxjs';
import { ResidenciaModel } from '../../../models/residencia.model';
import { CasaModel, nombreCasa } from '../../../models/casa.model';
import {
  llavePersona,
  nombrePersona,
  PersonaModel,
} from '../../../models/persona.model';
import { ResidenciaService } from '../../../services/residencia.service';
import { CasaService } from '../../../services/casa.service';
import { PersonaService } from '../../../services/persona.service';
import { NotificacionService } from '../../../services/notificacion.service';
import { extraerError } from '../../../compartido/extraer-error';

/**
 * @component GestionResidencias
 * @description Pestaña Residencias: en qué casa vive (o vivió) cada residente, con fecha de
 * ingreso y de salida. El residente sale de GET /api/residentes.
 */
@Component({
  selector: 'app-gestion-residencias',
  imports: [DatePipe],
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

  /** Funciones para la plantilla. */
  protected readonly nombreCasa = nombreCasa;

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
}
