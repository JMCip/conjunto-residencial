/** Respuesta de GET /api/bd/prueba: usuario, base y versión con los que está conectado el backend. */
export interface InfoBdModel {
  usuario: string;
  base: string;
  version: string;
}
