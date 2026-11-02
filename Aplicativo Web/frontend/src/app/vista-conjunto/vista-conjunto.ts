import { Component, effect, ElementRef, inject, viewChild } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { NavigationEnd, Router, RouterOutlet } from '@angular/router';
import { filter, map } from 'rxjs';
import { NOMBRES_VISTA, Vista } from '../models/vista.enum';
import { SidebarConjunto } from './sidebar-conjunto/sidebar-conjunto';
import { TopbarConjunto } from './topbar-conjunto/topbar-conjunto';

/**
 * Saca la sección del primer tramo de la URL ('/finanzas?x=1' → 'finanzas').
 * Si no es una sección conocida, devuelve 'inicio'.
 * @param url URL actual del router.
 */
function vistaDesdeUrl(url: string): Vista {
  const tramo = url.split(/[/?#]/).filter(Boolean)[0] ?? '';
  return tramo in NOMBRES_VISTA ? (tramo as Vista) : 'inicio';
}

/**
 * @component VistaConjunto
 * @description Contenedor principal de la aplicación. Arma el layout global (sidebar + topbar)
 * y pinta la sección de la URL en su `<router-outlet>` (rutas hijas de `app.routes.ts`).
 */
@Component({
  selector: 'app-vista-conjunto',
  imports: [SidebarConjunto, TopbarConjunto, RouterOutlet],
  templateUrl: './vista-conjunto.html',
  styleUrl: './vista-conjunto.css',
})
export class VistaConjunto {
  private router = inject(Router);

  /** Sección que se está viendo, leída de la URL cada vez que termina una navegación. */
  protected readonly vistaActual = toSignal(
    this.router.events.pipe(
      filter((evento): evento is NavigationEnd => evento instanceof NavigationEnd),
      map((evento) => vistaDesdeUrl(evento.urlAfterRedirects)),
    ),
    { initialValue: vistaDesdeUrl(this.router.url) },
  );

  /** Zona con scroll, para volver arriba al cambiar de sección. */
  private readonly zonaScroll = viewChild.required<ElementRef<HTMLElement>>('zona');

  constructor() {
    // Cada vez que cambia la sección, la zona de contenido vuelve arriba.
    effect(() => {
      this.vistaActual();
      this.zonaScroll().nativeElement.scrollTop = 0;
    });
  }
}
