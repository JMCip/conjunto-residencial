/**
 * Línea de una factura. Su llave es la combinación número de factura + número de línea.
 * Corresponde al JSON de GET /api/detalles-factura (tabla `detalle_factura`).
 */
export interface DetalleFacturaModel {
  /** Factura a la que pertenece la línea. */
  numeroFactura: number;
  /** Número consecutivo de la línea dentro de la factura. */
  numeroLinea: number;
  /** Concepto de cobro de la línea. */
  idConcepto: number;
  /** Valor cobrado en pesos. */
  valor: number;
  /** Asignación de celda que se cobra; null si la línea no viene de una celda adicional. */
  idAsignacion: number | null;
  /** Reserva del salón que se cobra; null si la línea no viene de una reserva. */
  idReserva: number | null;
  /** Sanción que se cobra; null si la línea no viene de una sanción. */
  idSancion: number | null;
}

/**
 * Datos que se envían al crear una línea. Solo se llena uno de idSancion, idReserva o
 * idAsignacion; si no se llena ninguno, la línea es un cobro libre.
 */
export interface NuevaLineaFactura {
  /** Factura a la que se agrega la línea. */
  numeroFactura: number;
  /** Concepto de cobro de la línea. */
  idConcepto: number;
  /** Valor en pesos; solo aplica en los cobros libres (administración, cuotas extraordinarias…). */
  valor?: number;
  /** Asignación de celda adicional que se cobra. */
  idAsignacion?: number;
  /** Reserva del salón que se cobra. */
  idReserva?: number;
  /** Sanción que se cobra. */
  idSancion?: number;
}

/** Valor máximo de una línea que admite la columna NUMERIC(12,2). */
export const MAX_VALOR_LINEA = 9999999999;
