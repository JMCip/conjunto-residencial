import { Component, inject } from '@angular/core';
import { NotificacionService } from '../services/notificacion.service';
import { TipoNotificacion } from '../models/notificacion.model';

/**
 * @component Notificaciones
 * @description Dibuja arriba a la derecha los avisos de NotificacionService.
 * Se monta una sola vez en app.html, junto al router-outlet.
 */
@Component({
  selector: 'app-notificaciones',
  imports: [],
  templateUrl: './notificaciones.html',
  styleUrl: './notificaciones.css',
})
export class Notificaciones {
  /** Servicio con la lista de avisos visibles. */
  protected readonly notificacionService = inject(NotificacionService);

  /** Ícono de Font Awesome para cada tipo de aviso. */
  protected readonly iconos: Record<TipoNotificacion, string> = {
    exito: 'fa-circle-check',
    error: 'fa-plug-circle-xmark',
    info: 'fa-circle-info',
  };
}
