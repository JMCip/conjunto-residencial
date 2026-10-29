import { Component, computed, inject, input, OnInit, output, signal } from '@angular/core';
import { NOMBRES_VISTA, Vista } from '../../models/vista.enum';
import { InfoBdModel } from '../../models/info-bd.model';
import { EstadoService } from '../../services/estado.service';

/** Ítem del menú: a qué sección lleva y qué ícono usa. */
interface ItemMenu {
  vista: Vista;
  icono: string;
}

/** Grupo del menú con su etiqueta en mayúsculas. */
interface GrupoMenu {
  etiqueta: string;
  items: ItemMenu[];
}

/** Estructura fija del menú (DESIGN.md: GENERAL, COMUNIDAD, ADMINISTRACIÓN). */
const GRUPOS_MENU: GrupoMenu[] = [
  { etiqueta: 'General', items: [{ vista: 'inicio', icono: 'fa-table-cells-large' }] },
  {
    etiqueta: 'Comunidad',
    items: [
      { vista: 'personas-casas', icono: 'fa-people-roof' },
      { vista: 'sanciones', icono: 'fa-triangle-exclamation' },
    ],
  },
  {
    etiqueta: 'Administración',
    items: [
      { vista: 'finanzas', icono: 'fa-file-invoice-dollar' },
      { vista: 'parqueadero', icono: 'fa-square-parking' },
      { vista: 'salon-comunal', icono: 'fa-calendar-check' },
    ],
  },
];

/**
 * @component SidebarConjunto
 * @description Menú lateral verde bosque: marca, secciones agrupadas, botón para colapsar
 * y, en el pie, el estado real de la conexión con la base de datos.
 */
@Component({
  selector: 'app-sidebar-conjunto',
  imports: [],
  templateUrl: './sidebar-conjunto.html',
  styleUrl: './sidebar-conjunto.css',
})
export class SidebarConjunto implements OnInit {
  private estadoService = inject(EstadoService);

  /** Sección que está viendo el usuario (la marca como activa). */
  readonly vistaActiva = input.required<Vista>();

  /** Avisa al contenedor qué sección se eligió. */
  readonly vistaSeleccionada = output<Vista>();

  /** true cuando el menú está colapsado (solo íconos). */
  protected readonly isCollapsed = signal(false);

  /** Datos de la base si la consulta respondió. */
  protected readonly infoBd = signal<InfoBdModel | null>(null);

  /** true si la consulta a la base falló. */
  protected readonly errorBd = signal(false);

  /** "PostgreSQL 18.6" a partir del texto largo de version(). */
  protected readonly versionCorta = computed(() => {
    const info = this.infoBd();
    return info ? info.version.split(' ').slice(0, 2).join(' ') : '';
  });

  /** Texto del estado de la base, para el tooltip del pie cuando está colapsado. */
  protected readonly textoEstadoBd = computed(() => {
    if (this.infoBd()) return 'Base de datos conectada';
    if (this.errorBd()) return 'Sin conexión con la base';
    return 'Consultando la base…';
  });

  /** Grupos e ítems del menú (constantes). */
  protected readonly grupos = GRUPOS_MENU;

  /** Nombre visible de cada sección (constante). */
  protected readonly nombres = NOMBRES_VISTA;

  ngOnInit(): void {
    this.estadoService.getInfoBd().subscribe({
      next: (datos) => this.infoBd.set(datos),
      error: () => this.errorBd.set(true),
    });
  }

  /** Colapsa o expande el menú. */
  toggleSidebar(): void {
    this.isCollapsed.update((valor) => !valor);
  }

  /**
   * Avisa al contenedor la sección elegida.
   * @param vista Sección a mostrar.
   */
  seleccionar(vista: Vista): void {
    this.vistaSeleccionada.emit(vista);
  }
}
