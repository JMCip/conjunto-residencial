export interface ReservaSalonModel {
  idReserva: number;
  fechaReserva: string;
  horaInicio: string;
  horaFinal: string;
  valorDeposito: number;
  valorPorHora: number;
  estado: 'CONFIRMADA' | 'FINALIZADA' | 'CANCELADA';
  casaNumero: number;
  casaManzana: number;
  horasReservadas: number;
  valorTotal: number;
}

/**
 * Lo que se envía al crear o editar una reserva: sin el id (lo pone la base) y sin
 * horasReservadas ni valorTotal (los calcula el backend).
 */
export type DatosReservaSalon = Omit<ReservaSalonModel, 'idReserva' | 'horasReservadas' | 'valorTotal'>;

/** Estados de reserva, en el orden de los filtros. */
export const ESTADOS_RESERVA: ReservaSalonModel['estado'][] = ['CONFIRMADA', 'FINALIZADA', 'CANCELADA'];

/** Texto que se muestra para cada estado de reserva. */
export const ETIQUETA_RESERVA: Record<ReservaSalonModel['estado'], string> = {
  CONFIRMADA: 'Confirmada',
  FINALIZADA: 'Finalizada',
  CANCELADA: 'Cancelada',
};

/** Color de insignia de cada estado de reserva. */
export const ESTILO_RESERVA: Record<ReservaSalonModel['estado'], 'ok' | 'pendiente' | 'neutro'> = {
  CONFIRMADA: 'pendiente',
  FINALIZADA: 'ok',
  CANCELADA: 'neutro',
};
