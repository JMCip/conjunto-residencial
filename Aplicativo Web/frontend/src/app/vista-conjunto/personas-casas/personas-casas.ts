import { Component } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';

/** Pestaña interna: su ruta hija (/personas-casas/<ruta>), su texto y su ícono. */
interface Pestana {
  ruta: string;
  texto: string;
  icono: string;
}

/** Pestañas de la sección, en orden. */
const PESTANAS: Pestana[] = [
  { ruta: 'casas', texto: 'Casas', icono: 'fa-house' },
  { ruta: 'personas', texto: 'Personas', icono: 'fa-user' },
  { ruta: 'telefonos', texto: 'Teléfonos', icono: 'fa-phone' },
  { ruta: 'posesiones', texto: 'Posesiones', icono: 'fa-key' },
  { ruta: 'arriendos', texto: 'Arriendos', icono: 'fa-file-signature' },
  { ruta: 'residencias', texto: 'Residencias', icono: 'fa-bed' },
  { ruta: 'visitas', texto: 'Visitas', icono: 'fa-door-open' },
];

/**
 * @component PersonasCasas
 * @description Sección Personas y casas. Muestra la cabecera y un control segmentado con una
 * pestaña por recurso; cada pestaña es una ruta hija que se pinta en su <router-outlet>.
 */
@Component({
  selector: 'app-personas-casas',
  imports: [RouterLink, RouterLinkActive, RouterOutlet],
  templateUrl: './personas-casas.html',
  styleUrl: './personas-casas.css',
})
export class PersonasCasas {
  /** Pestañas del control segmentado (constante). */
  protected readonly pestanas = PESTANAS;
}
