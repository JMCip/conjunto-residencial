/**
 * Teléfono de una persona. El número es la llave del registro.
 * Corresponde al JSON de GET /api/telefonos (tabla `telefono`).
 */
export interface TelefonoModel {
  /** Número de teléfono (la base lo guarda como NUMERIC y el backend lo entrega como texto). */
  telefono: string;
  /** Tipo de documento de la persona dueña del teléfono. */
  personaTipoDocumento: string;
  /** Número de documento de la persona dueña del teléfono. */
  personaNumeroDocumento: string;
}
