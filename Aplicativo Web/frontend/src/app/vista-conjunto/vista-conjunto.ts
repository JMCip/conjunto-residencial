import { Component, ElementRef, signal, viewChild } from '@angular/core';
import { Vista } from '../models/vista.enum';
import { SidebarConjunto } from './sidebar-conjunto/sidebar-conjunto';
import { TopbarConjunto } from './topbar-conjunto/topbar-conjunto';
import { DashboardConjunto } from './dashboard-conjunto/dashboard-conjunto';
import { PersonasCasas } from './personas-casas/personas-casas';
import { Finanzas } from './finanzas/finanzas';
import { Parqueadero } from './parqueadero/parqueadero';
import { SalonComunal } from './salon-comunal/salon-comunal';
import { Sanciones } from './sanciones/sanciones';

/**
 * @component VistaConjunto
 * @description Contenedor principal de la aplicación. Arma el layout global (sidebar + topbar)
 * y muestra la sección elegida en el menú mediante `vistaActual`.
 */
@Component({
  selector: 'app-vista-conjunto',
  imports: [
    SidebarConjunto,
    TopbarConjunto,
    DashboardConjunto,
    PersonasCasas,
    Finanzas,
    Parqueadero,
    SalonComunal,
    Sanciones,
  ],
  templateUrl: './vista-conjunto.html',
  styleUrl: './vista-conjunto.css',
})
export class VistaConjunto {
  /** Sección que se muestra en el cuerpo principal. Empieza en 'inicio'. */
  protected readonly vistaActual = signal<Vista>('inicio');

  /** Zona con scroll, para volver arriba al cambiar de sección. */
  private readonly zonaScroll = viewChild.required<ElementRef<HTMLElement>>('zona');

  /**
   * Cambia la sección visible. La llama el sidebar al elegir un ítem
   * (y el Inicio con sus enlaces "Ver todas").
   * @param vista Sección a mostrar.
   */
  cambiarVista(vista: Vista): void {
    this.vistaActual.set(vista);
    this.zonaScroll().nativeElement.scrollTop = 0;
  }
}
