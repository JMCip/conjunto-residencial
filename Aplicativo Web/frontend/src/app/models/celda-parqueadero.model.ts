export interface CeldaParqueaderoModel {
  idCelda: number;
  tipo: string;
  estado: string;
}


export const TIPOS_CELDA = ['CARRO', 'MOTO', 'VISITANTE'] as const;


export const ESTADOS_CELDA = ['DISPONIBLE', 'ASIGNADA'] as const;


export const ETIQUETA_CELDA: Record<string, string> = {
  CARRO: 'Carro',
  MOTO: 'Moto',
  VISITANTE: 'Visitante',
  DISPONIBLE: 'Disponible',
  ASIGNADA: 'Asignada',
};


export function nombreCelda(idCelda: number): string {
  return `Celda ${idCelda}`;
}
