/**
 * Pago registrado a una factura.
 * Corresponde al JSON de GET /api/pagos (tabla `pago`).
 */
export interface PagoModel {
  /** Identificador del pago (lo genera la base de datos). */
  idPago: number;
  /** Fecha del pago (yyyy-MM-dd). */
  fecha: string;
  /** Valor pagado en pesos. */
  valor: number;
  /** Medio de pago (EFECTIVO, TRANSFERENCIA, PSE o TARJETA). */
  metodoPago: string;
  /** Factura a la que se aplica el pago. */
  numeroFactura: number;
  /** Tipo de documento de la persona que paga. */
  personaTipoDocumento: string;
  /** Número de documento de la persona que paga. */
  personaNumeroDocumento: string;
}

/** Medios de pago que acepta el backend. */
export type MetodoPago = 'EFECTIVO' | 'TRANSFERENCIA' | 'PSE' | 'TARJETA';

/** Opciones del selector de medio de pago: valor que se envía y texto que se muestra. */
export const METODOS_PAGO: { valor: MetodoPago; etiqueta: string }[] = [
  { valor: 'EFECTIVO', etiqueta: 'Efectivo' },
  { valor: 'TRANSFERENCIA', etiqueta: 'Transferencia' },
  { valor: 'PSE', etiqueta: 'PSE' },
  { valor: 'TARJETA', etiqueta: 'Tarjeta' },
];

/** Datos que se envían al registrar un pago: todos los del pago menos el id. */
export type NuevoPago = Omit<PagoModel, 'idPago'>;

/** Respuesta de POST /api/pagos. */
export interface RespuestaPagoModel {
  /** Mensaje de confirmación del backend. */
  mensaje: string;
  /** Identificador del pago creado. */
  idPago: number;
  /** Estado en que quedó la factura (ABONADA, PAGADA o VENCIDA). */
  estadoFactura: string;
  /** Saldo que le queda a la factura después del pago. */
  saldo: number;
}
