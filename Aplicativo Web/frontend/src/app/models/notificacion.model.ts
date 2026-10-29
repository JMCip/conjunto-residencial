/** Tipo de aviso: define su color e ícono. */
export type TipoNotificacion = 'exito' | 'error' | 'info';

/** Aviso flotante que se muestra arriba a la derecha (lo crea NotificacionService). */
export interface NotificacionModel {
  /** Identificador único, para el `track` del `@for` y para cerrarlo. */
  id: number;
  tipo: TipoNotificacion;
  titulo: string;
  mensaje: string;
}
