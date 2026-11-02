import { HttpErrorResponse } from '@angular/common/http';

/**
 * Convierte el error de una petición en un mensaje corto para el usuario.
 * El backend responde los errores como { "error": "Ya existe un registro con esa llave" }.
 * @param err Error que llega al `error:` del subscribe.
 * @param porDefecto Mensaje si no se reconoce el error.
 * @returns Texto para mostrar en el aviso.
 */
export function extraerError(err: unknown, porDefecto: string): string {
  if (!(err instanceof HttpErrorResponse)) return porDefecto;
  if ([0, 502, 503, 504].includes(err.status)) return 'Revisar Back O BD.';
  if (typeof err.error?.error === 'string') return err.error.error;
  if (err.status === 404 || err.status === 405) {
    return 'El back no tiene esa operacion.';
  }
  return porDefecto;
}
