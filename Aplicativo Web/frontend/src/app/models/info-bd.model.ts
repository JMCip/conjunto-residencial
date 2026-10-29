/** Respuesta de GET /api/bd/prueba: con qué usuario, base y versión está conectado el backend. */
export interface InfoBdModel {
  usuario: string;
  base: string;
  version: string;
}
