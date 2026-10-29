/** Factura de administración de una casa (provisional hasta tener el Modelo Relacional). */
export interface FacturaModel {
  id: number;
  numero: string;
  idCasa: number;
  casa: string;
  periodo: string;
  valor: number;
  fechaVencimiento: string;
  estado: 'PAGADA' | 'PENDIENTE' | 'VENCIDA';
}
