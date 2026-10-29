/** Sanción impuesta a una casa o residente (provisional hasta tener el Modelo Relacional). */
export interface SancionModel {
  id: number;
  idCasa: number;
  casa: string;
  fecha: string;
  motivo: string;
  valor: number;
  estado: 'ACTIVA' | 'PAGADA';
}
