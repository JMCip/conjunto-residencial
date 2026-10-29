import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { Notificaciones } from './notificaciones/notificaciones';

/**
 * @component App
 * @description Componente raíz: muestra la ruta actual y la capa de avisos flotantes.
 */
@Component({
  imports: [RouterOutlet, Notificaciones],
  selector: 'app-root',
  styleUrl: './app.css',
  templateUrl: './app.html',
})
export class App {}
