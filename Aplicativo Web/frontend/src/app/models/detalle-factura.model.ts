/** Línea de una factura: un concepto cobrado (provisional hasta tener el Modelo Relacional). */
export interface DetalleFacturaModel {
  id: number;
  idFactura: number;
  concepto: string;
  valor: number;
}
