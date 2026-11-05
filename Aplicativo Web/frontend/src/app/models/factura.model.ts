/** Estados posibles de una factura (valores que usa la base). */
export type EstadoFactura = 'PAGADA' | 'PENDIENTE' | 'VENCIDA' | 'ABONADA';


export interface FacturaModel {
  numeroFactura: number;
  periodo: string;
  fechaEmision: string;
  fechaVencimiento: string;
  estado: EstadoFactura;
  total: number | null;
  casaNumero: number;
  casaManzana: number;
  totalPagado: number;
  saldo: number | null;
}

export const ETIQUETA_FACTURA: Record<EstadoFactura, string> = {
  PAGADA: 'Pagada',
  PENDIENTE: 'Pendiente',
  VENCIDA: 'Vencida',
  ABONADA: 'Abonada',
};

export const ESTILO_FACTURA: Record<EstadoFactura, 'ok' | 'pendiente' | 'alerta'> = {
  PAGADA: 'ok',
  PENDIENTE: 'pendiente',
  VENCIDA: 'alerta',
  ABONADA: 'pendiente',
};

/** Lo que se envía al crear: queda PENDIENTE y sin líneas. */
export interface NuevaFactura {
  periodo: string; // yyyy-MM
  fechaEmision: string; // yyyy-MM-dd
  fechaVencimiento: string; // yyyy-MM-dd
  casaNumero: number;
  casaManzana: number;
}

/** true si todavía se puede cambiar el vencimiento y sus líneas (PENDIENTE o ABONADA). */
export function facturaAbierta(f: FacturaModel): boolean {
  return f.estado === 'PENDIENTE' || f.estado === 'ABONADA';
}
