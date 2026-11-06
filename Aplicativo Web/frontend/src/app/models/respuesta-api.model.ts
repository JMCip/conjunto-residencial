/** Respuesta del backend a crear, actualizar o eliminar un registro, por ejemplo { "mensaje": "Casa creada" }. */
export interface RespuestaApiModel {
  /** Mensaje de confirmación. */
  mensaje: string;
}
