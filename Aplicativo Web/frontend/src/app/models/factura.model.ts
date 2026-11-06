/** Estados posibles de una factura (valores que guarda la base de datos). */
export type EstadoFactura = 'PAGADA' | 'PENDIENTE' | 'VENCIDA' | 'ABONADA';

/**
 * Factura de una casa para un periodo.
 * Corresponde al JSON de GET /api/facturas (tabla `factura` con totales calculados en el SQL).
 */
export interface FacturaModel {
  /** Número de la factura (lo genera la base de datos). */
  numeroFactura: number;
  /** Periodo facturado (yyyy-MM). */
  periodo: string;
  /** Fecha de emisión (yyyy-MM-dd). */
  fechaEmision: string;
  /** Fecha límite de pago (yyyy-MM-dd). */
  fechaVencimiento: string;
  /** Estado de la factura. */
  estado: EstadoFactura;
  /** Suma de las líneas de la factura. */
  total: number | null;
  /** Número de la casa facturada. */
  casaNumero: number;
  /** Manzana de la casa facturada. */
  casaManzana: number;
  /** Suma de los pagos registrados a la factura. */
  totalPagado: number;
  /** Lo que falta por pagar (total menos lo pagado). */
  saldo: number | null;
}

/** Texto que se muestra para cada estado de factura. */
export const ETIQUETA_FACTURA: Record<EstadoFactura, string> = {
  PAGADA: 'Pagada',
  PENDIENTE: 'Pendiente',
  VENCIDA: 'Vencida',
  ABONADA: 'Abonada',
};

/** Color de insignia de cada estado de factura. */
export const ESTILO_FACTURA: Record<EstadoFactura, 'ok' | 'pendiente' | 'alerta'> = {
  PAGADA: 'ok',
  PENDIENTE: 'pendiente',
  VENCIDA: 'alerta',
  ABONADA: 'pendiente',
};

/** Datos que se envían al crear una factura; queda PENDIENTE y sin líneas. */
export interface NuevaFactura {
  /** Periodo facturado (yyyy-MM). */
  periodo: string;
  /** Fecha de emisión (yyyy-MM-dd). */
  fechaEmision: string;
  /** Fecha límite de pago (yyyy-MM-dd). */
  fechaVencimiento: string;
  /** Número de la casa facturada. */
  casaNumero: number;
  /** Manzana de la casa facturada. */
  casaManzana: number;
}

/**
 * Indica si todavía se puede cambiar el vencimiento y las líneas de una factura.
 * @param f Factura a revisar.
 * @returns true si la factura está PENDIENTE o ABONADA.
 */
export function facturaAbierta(f: FacturaModel): boolean {
  return f.estado === 'PENDIENTE' || f.estado === 'ABONADA';
}
