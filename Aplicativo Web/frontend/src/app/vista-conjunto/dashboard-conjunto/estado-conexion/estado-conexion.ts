import { Component, input } from '@angular/core';
import { InfoBdModel } from '../../../models/info-bd.model';

/** Estado de una consulta de diagnóstico (backend o base). */
export type EstadoConsulta = 'consultando' | 'ok' | 'error';

/** Texto de la insignia para cada estado. */
const TEXTO_CONEXION: Record<EstadoConsulta, string> = {
  consultando: 'Consultando…',
  ok: 'En línea',
  error: 'Sin conexión',
};

/** Color de la insignia para cada estado. */
const ESTILO_CONEXION: Record<EstadoConsulta, 'neutro' | 'ok' | 'alerta'> = {
  consultando: 'neutro',
  ok: 'ok',
  error: 'alerta',
};

/**
 * @component EstadoConexion
 * @description Tarjeta del Inicio que muestra si responden el backend y PostgreSQL,
 * y con qué usuario, base y versión está conectado.
 */
@Component({
  selector: 'app-estado-conexion',
  imports: [],
  templateUrl: './estado-conexion.html',
  styleUrl: './estado-conexion.css',
})
export class EstadoConexion {
  /** Resultado de GET /api/estado. */
  readonly estadoBackend = input.required<EstadoConsulta>();

  /** Resultado de GET /api/bd/prueba. */
  readonly estadoBd = input.required<EstadoConsulta>();

  /** Datos de la base, o null si aún no llegan o la consulta falló. */
  readonly infoBd = input.required<InfoBdModel | null>();

  /** Diccionarios de texto y color (constantes). */
  protected readonly textoConexion = TEXTO_CONEXION;
  protected readonly estiloConexion = ESTILO_CONEXION;
}
