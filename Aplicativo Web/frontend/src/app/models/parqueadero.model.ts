/** Parqueadero del conjunto y su asignación (provisional hasta tener el Modelo Relacional). */
export interface ParqueaderoModel {
  id: number;
  numero: string;
  tipo: 'CARRO' | 'MOTO' | 'VISITANTE';
  estado: 'DISPONIBLE' | 'OCUPADO';
  idCasa: number | null;
  casa: string | null;
}
