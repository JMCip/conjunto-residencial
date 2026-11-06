/**
 * Posesión de una casa por parte de un propietario, con su porcentaje de propiedad.
 * Corresponde al JSON de GET /api/posesiones (tabla `posesion`).
 */
export interface PosesionModel {
  /** Identificador de la posesión (lo genera la base de datos). */
  idPosesion: number;
  /** Porcentaje de la casa que pertenece al propietario. */
  porcentaje: number;
  /** Fecha de compra (yyyy-MM-dd). */
  fechaCompra: string;
  /** Fecha de venta (yyyy-MM-dd); null si todavía es dueño. */
  fechaVenta: string | null;
  /** Número de la casa. */
  casaNumero: number;
  /** Manzana de la casa. */
  casaManzana: number;
  /** Tipo de documento del propietario. */
  propietarioTipoDocumento: string;
  /** Número de documento del propietario. */
  propietarioNumeroDocumento: string;
}
