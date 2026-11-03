import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import {
  CasaModel,
  ESTILO_CASA,
  ETIQUETA_CASA,
  nombreCasa as nombreMzCasa,
} from '../../../models/casa.model';
import { CasaService } from '../../../services/casa.service';
import { NotificacionService } from '../../../services/notificacion.service';
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
  imports: [DecimalPipe],
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
}
