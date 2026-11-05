export type EstadoReserva = 'CONFIRMADA' | 'FINALIZADA' | 'CANCELADA' | 'DEPOSITO_RETENIDO';

export interface ReservaSalonModel {
  idReserva: number;
  fechaReserva: string;
  horaInicio: string;
  horaFinal: string;
  valorDeposito: number;
  valorPorHora: number;
  estado: EstadoReserva;
  casaNumero: number;
  casaManzana: number;
  horasReservadas: number;
  valorTotal: number;
}

/** Lo que se envía al crear: el back pone fecha, estado (CONFIRMADA), horas y total. */
export interface NuevaReservaSalon {
  horaInicio: string; // yyyy-MM-ddTHH:mm:ss
  horaFinal: string;
  valorDeposito: number;
  valorPorHora: number;
  casaNumero: number;
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

/** Texto de cada botón del filtro (plural). */
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
