/** Estados posibles de una reserva del salón comunal. */
export type EstadoReserva = 'CONFIRMADA' | 'FINALIZADA' | 'CANCELADA' | 'DEPOSITO_RETENIDO';

/**
 * Reserva del salón comunal hecha por una casa.
 * Corresponde al JSON de GET /api/reservas (tabla `reserva_salon` con horas y total calculados).
 */
export interface ReservaSalonModel {
  /** Identificador de la reserva (lo genera la base de datos). */
  idReserva: number;
  /** Día de la reserva (yyyy-MM-dd). */
  fechaReserva: string;
  /** Fecha y hora de inicio (yyyy-MM-dd HH:mm:ss). */
  horaInicio: string;
  /** Fecha y hora de terminación (yyyy-MM-dd HH:mm:ss). */
  horaFinal: string;
  /** Depósito de garantía en pesos. */
  valorDeposito: number;
  /** Tarifa por hora pactada al momento de reservar. */
  valorPorHora: number;
  /** Estado de la reserva. */
  estado: EstadoReserva;
  /** Número de la casa que reserva. */
  casaNumero: number;
  /** Manzana de la casa que reserva. */
  casaManzana: number;
  /** Duración de la reserva en horas (calculada en el SQL). */
  horasReservadas: number;
  /** Horas reservadas por la tarifa por hora (calculado en el SQL). */
  valorTotal: number;
}

/** Datos que se envían al crear una reserva; el backend asigna la fecha, el estado, las horas y el total. */
export interface NuevaReservaSalon {
  /** Fecha y hora de inicio (yyyy-MM-ddTHH:mm:ss). */
  horaInicio: string;
  /** Fecha y hora de terminación (yyyy-MM-ddTHH:mm:ss). */
  horaFinal: string;
  /** Depósito de garantía en pesos. */
  valorDeposito: number;
  /** Tarifa por hora en pesos. */
  valorPorHora: number;
  /** Número de la casa que reserva. */
  casaNumero: number;
  /** Manzana de la casa que reserva. */
  casaManzana: number;
}

/** Estados de reserva, en el orden de los filtros. */
export const ESTADOS_RESERVA: EstadoReserva[] = [
  'CONFIRMADA',
  'FINALIZADA',
  'CANCELADA',
  'DEPOSITO_RETENIDO',
];

/** Texto que se muestra para cada estado de reserva. */
export const ETIQUETA_RESERVA: Record<EstadoReserva, string> = {
  CONFIRMADA: 'Confirmada',
  FINALIZADA: 'Finalizada',
  CANCELADA: 'Cancelada',
  DEPOSITO_RETENIDO: 'Depósito retenido',
};

/** Texto de cada botón del filtro (en plural). */
export const FILTRO_RESERVA: Record<EstadoReserva, string> = {
  CONFIRMADA: 'Confirmadas',
  FINALIZADA: 'Finalizadas',
  CANCELADA: 'Canceladas',
  DEPOSITO_RETENIDO: 'Retenidas',
};

/** Color de insignia de cada estado de reserva. */
export const ESTILO_RESERVA: Record<EstadoReserva, 'ok' | 'pendiente' | 'neutro' | 'alerta'> = {
  CONFIRMADA: 'pendiente',
  FINALIZADA: 'ok',
  CANCELADA: 'neutro',
  DEPOSITO_RETENIDO: 'alerta',
};
