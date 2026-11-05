export interface DetalleFacturaModel {
  numeroFactura: number;
  numeroLinea: number;
  idConcepto: number;
  valor: number;
  idAsignacion: number | null;
  idReserva: number | null;
  idSancion: number | null;
}
/** Lo que se envía al crear una línea. Solo se llena UNO de idSancion, idReserva o idAsignacion. */
export interface NuevaLineaFactura {
  numeroFactura: number;
  idConcepto: number;
  valor?: number; // solo en líneas libres (administración, cuotas extra…)
  idAsignacion?: number;
  idReserva?: number;
  idSancion?: number;
}

export const MAX_VALOR_LINEA = 9999999999;
