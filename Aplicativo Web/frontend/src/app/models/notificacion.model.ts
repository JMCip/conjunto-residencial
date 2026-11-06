/** Tipo de aviso flotante: define su color y su ícono. */
export type TipoNotificacion = 'exito' | 'error' | 'info';

/** Aviso flotante que muestra el componente de notificaciones. */
export interface NotificacionModel {
  /** Identificador del aviso, para poder cerrarlo. */
  id: number;
  /** Tipo de aviso. */
  tipo: TipoNotificacion;
  /** Título en negrita. */
  titulo: string;
  /** Texto del aviso. */
  mensaje: string;
}
