import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { NOMBRES_VISTA, Vista } from '../../models/vista.enum';
import { InfoBdModel } from '../../models/info-bd.model';
import { EstadoService } from '../../services/estado.service';
import { AuthService } from '../../services/auth.service';

/** Ítem del menú: a qué sección lleva y qué ícono usa. */
interface ItemMenu {
  /** Sección a la que lleva el ítem. */
  vista: Vista;

  /** Ícono de Font Awesome del ítem. */
  icono: string;
}

/** Grupo del menú con su etiqueta en mayúsculas. */
interface GrupoMenu {
  /** Nombre del grupo, en mayúsculas. */
  etiqueta: string;

  /** Ítems del grupo, en orden. */
  items: ItemMenu[];
}

/** Estructura fija del menú: General, Comunidad y Administración. */
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
 * Cada ítem es un enlace a su ruta; `routerLinkActive` marca el de la URL actual.
 */
@Component({
  selector: 'app-sidebar-conjunto',
  imports: [RouterLink, RouterLinkActive],
  templateUrl: './sidebar-conjunto.html',
  styleUrl: './sidebar-conjunto.css',
})
export class SidebarConjunto implements OnInit {
  /** Servicio que revisa la conexión con el backend y la base de datos. */
  private estadoService = inject(EstadoService);
  /** Servicio de autenticación. */
  private authService = inject(AuthService);
  /** Router de Angular para navegar entre pantallas. */
  private router = inject(Router);

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

  /** Consulta el estado de la conexión con la base de datos para el pie del menú. */
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

  /** Cierra la sesión y vuelve al login. */
  cerrarSesion(): void {
    this.authService.logout();
    this.router.navigate(['/login'], { replaceUrl: true });
  }

}
