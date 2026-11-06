import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { forkJoin } from 'rxjs';
import { AsignacionCeldaModel } from '../../../models/asignacion-celda.model';
import { CasaModel, nombreCasa } from '../../../models/casa.model';
import {
  CeldaParqueaderoModel,
  nombreCelda,
} from '../../../models/celda-parqueadero.model';
import { AsignacionCeldaService } from '../../../services/asignacion-celda.service';
import { CasaService } from '../../../services/casa.service';
import { CeldaParqueaderoService } from '../../../services/celda-parqueadero.service';
import { NotificacionService } from '../../../services/notificacion.service';
import { extraerError } from '../../../compartido/extraer-error';

/**
 * @component GestionAsignaciones
 * @description Pestaña Asignaciones de Parqueadero: qué celda tiene asignada cada casa y desde
 * cuándo. CRUD con modal; casas y celdas salen de sus propios GET.
 */
@Component({
  selector: 'app-gestion-asignaciones',
  imports: [DatePipe],
  templateUrl: './gestion-asignaciones.html',
  styleUrl: './gestion-asignaciones.css',
})
export class GestionAsignaciones implements OnInit {
  /** Servicio de asignaciones de celdas. */
  private asignacionService = inject(AsignacionCeldaService);
  /** Servicio de casas. */
  private casaService = inject(CasaService);
  /** Servicio de celdas del parqueadero. */
  private celdaService = inject(CeldaParqueaderoService);
  /** Servicio de avisos flotantes. */
  private notificacionService = inject(NotificacionService);

  /** Asignaciones obtenidas del backend. */
  protected readonly asignaciones = signal<AsignacionCeldaModel[]>([]);

  /** Casas y celdas para los selectores. */
  protected readonly casas = signal<CasaModel[]>([]);
  /** Lista de celdas consultada al backend. */
  protected readonly celdas = signal<CeldaParqueaderoModel[]>([]);

  /** true mientras se consulta la información. */
  protected readonly cargando = signal(true);
  /** true si la consulta de la información falló. */
  protected readonly errorCarga = signal(false);
  /** Texto escrito en el buscador. */
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

  /** Función del modelo para mostrar el nombre de una casa en la plantilla. */
  protected readonly nombreCasa = nombreCasa;
  /** Función del modelo para mostrar el nombre de una celda en la plantilla. */
  protected readonly nombreCelda = nombreCelda;

  /** Consulta los datos al abrir la pantalla. */
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
}
