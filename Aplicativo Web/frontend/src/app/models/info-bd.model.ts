/** Respuesta de GET /api/bd/prueba: datos de la conexión del backend con PostgreSQL. */
export interface InfoBdModel {
  /** Usuario de base de datos con el que se conecta el backend. */
  usuario: string;
  /** Nombre de la base de datos. */
  base: string;
  /** Versión del servidor PostgreSQL. */
  version: string;
}
