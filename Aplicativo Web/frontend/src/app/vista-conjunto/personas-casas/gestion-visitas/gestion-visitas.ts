import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { forkJoin } from 'rxjs';
import { VisitaModel } from '../../../models/visita.model';
import { CasaModel, nombreCasa } from '../../../models/casa.model';
import {
  llavePersona,
  nombrePersona,
  PersonaModel,
} from '../../../models/persona.model';
import { VisitaService } from '../../../services/visita.service';
import { CasaService } from '../../../services/casa.service';
import { PersonaService } from '../../../services/persona.service';
import { NotificacionService } from '../../../services/notificacion.service';
import { extraerError } from '../../../compartido/extraer-error';

/**
 * @component GestionVisitas
 * @description Pestaña Visitas: qué visitante fue a qué casa, con fecha de
 * entrada y de salida. El visitante sale de GET /api/visitantes.
 */
@Component({
  selector: 'app-gestion-visitas',
  imports: [DatePipe],
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

  /** Funciones para la plantilla. */
  protected readonly nombreCasa = nombreCasa;

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
}
