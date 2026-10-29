/** Reserva del salón comunal (provisional hasta tener el Modelo Relacional). */
export interface ReservaModel {
  id: number;
  idCasa: number;
  casa: string;
  fecha: string;
  horaInicio: string;
  horaFin: string;
  motivo: string;
  estado: 'CONFIRMADA' | 'PENDIENTE' | 'CANCELADA';
}
