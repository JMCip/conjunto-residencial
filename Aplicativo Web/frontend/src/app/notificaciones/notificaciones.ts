import { Component, inject } from '@angular/core';
import { NotificacionService } from '../services/notificacion.service';
import { TipoNotificacion } from '../models/notificacion.model';


@Component({
  selector: 'app-notificaciones',
  imports: [],
  templateUrl: './notificaciones.html',
  styleUrl: './notificaciones.css',
})
export class Notificaciones {

  protected readonly notificacionService = inject(NotificacionService);


  protected readonly iconos: Record<TipoNotificacion, string> = {
    exito: 'fa-circle-check',
    error: 'fa-plug-circle-xmark',
    info: 'fa-circle-info',
  };
}
