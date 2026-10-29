import { Injectable, signal } from '@angular/core';
import { NotificacionModel, TipoNotificacion } from '../models/notificacion.model';

/** Milisegundos que un aviso permanece en pantalla antes de quitarse solo. */
const DURACION_MS = 5000;

/**
 * Servicio de avisos flotantes (reemplaza a ngx-toastr).
 * Guarda los avisos en un signal; el componente Notificaciones los dibuja.
 */
@Injectable({ providedIn: 'root' })
export class NotificacionService {
  /** Lista interna de avisos visibles. Solo este servicio la modifica. */
  private readonly lista = signal<NotificacionModel[]>([]);

  /** Avisos visibles, de solo lectura para los componentes. */
  readonly avisos = this.lista.asReadonly();

  /** Contador para generar ids únicos. */
  private siguienteId = 1;

  /**
   * Muestra un aviso de éxito.
   * @param mensaje Texto del aviso.
   * @param titulo Título corto en negrita.
   */
  exito(mensaje: string, titulo = 'Listo'): void {
    this.mostrar('exito', titulo, mensaje);
  }

  /**
   * Muestra un aviso de error.
   * @param mensaje Texto del aviso (por ejemplo, una pista de qué revisar).
   * @param titulo Título corto en negrita.
   */
  error(mensaje: string, titulo = 'Error'): void {
    this.mostrar('error', titulo, mensaje);
  }

  /**
   * Muestra un aviso informativo.
   * @param mensaje Texto del aviso.
   * @param titulo Título corto en negrita.
   */
  info(mensaje: string, titulo = 'Información'): void {
    this.mostrar('info', titulo, mensaje);
  }

  /**
   * Quita un aviso de la pantalla.
   * @param id Identificador del aviso.
   */
  cerrar(id: number): void {
    this.lista.update((avisos) => avisos.filter((a) => a.id !== id));
  }

  /**
   * Agrega un aviso y programa su cierre automático.
   * Si ya hay uno idéntico visible, no lo repite.
   */
  private mostrar(tipo: TipoNotificacion, titulo: string, mensaje: string): void {
    const repetido = this.lista().some(
      (a) => a.tipo === tipo && a.titulo === titulo && a.mensaje === mensaje,
    );
    if (repetido) return;

    const id = this.siguienteId++;
    this.lista.update((avisos) => [...avisos, { id, tipo, titulo, mensaje }]);
    setTimeout(() => this.cerrar(id), DURACION_MS);
  }
}
