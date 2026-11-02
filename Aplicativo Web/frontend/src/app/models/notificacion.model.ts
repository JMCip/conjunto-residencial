export type TipoNotificacion = 'exito' | 'error' | 'info';

export interface NotificacionModel {
  id: number;
  tipo: TipoNotificacion;
  titulo: string;
  mensaje: string;
}
