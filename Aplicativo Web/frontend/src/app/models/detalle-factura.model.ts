export interface DetalleFacturaModel {
  numeroFactura: number;
  numeroLinea: number;
  idConcepto: number;
  valor: number;
  idAsignacion: number | null;
  idReserva: number | null;
  idSancion: number | null;
}
