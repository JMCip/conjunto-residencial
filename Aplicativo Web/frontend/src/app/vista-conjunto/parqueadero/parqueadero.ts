import { Component } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';

/** Pestaña interna: su ruta hija (/parqueadero/<ruta>), su texto y su ícono. */
interface Pestana {
  /** Ruta hija de la pestaña. */
  ruta: string;

  /** Texto del botón. */
  texto: string;

  /** Ícono de Font Awesome del botón. */
  icono: string;
}

/** Pestañas de la sección, en orden. */
const PESTANAS: Pestana[] = [
  { ruta: 'vehiculos', texto: 'Vehículos', icono: 'fa-car' },
  { ruta: 'celdas', texto: 'Celdas', icono: 'fa-square-parking' },
  { ruta: 'asignaciones', texto: 'Asignaciones', icono: 'fa-link' },
  { ruta: 'parqueos', texto: 'Parqueos', icono: 'fa-clock' },
];

/**
 * @component Parqueadero
 * @description Sección Parqueadero. Muestra la cabecera y un control segmentado con una pestaña
 * por recurso; cada pestaña es una ruta hija que se pinta en su <router-outlet>.
 */
@Component({
  selector: 'app-parqueadero',
  imports: [RouterLink, RouterLinkActive, RouterOutlet],
  templateUrl: './parqueadero.html',
  styleUrl: './parqueadero.css',
})
export class Parqueadero {
  /** Pestañas del control segmentado (constante). */
  protected readonly pestanas = PESTANAS;
}
