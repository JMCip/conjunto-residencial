export interface CeldaParqueaderoModel {
  idCelda: number;
  tipo: string;
  estado: string;
}

/** Tipos posibles de celda. */
export const TIPOS_CELDA = ['CARRO', 'MOTO', 'VISITANTE'] as const;

/** Estados posibles de una celda. */
export const ESTADOS_CELDA = ['DISPONIBLE', 'ASIGNADA'] as const;

/** Texto que se muestra para cada tipo y estado de celda. */
export const ETIQUETA_CELDA: Record<string, string> = {
  CARRO: 'Carro',
  MOTO: 'Moto',
  VISITANTE: 'Visitante',
  DISPONIBLE: 'Disponible',
  ASIGNADA: 'Asignada',
};

/** "Celda 3": así se nombra una celda en toda la app. */
export function nombreCelda(idCelda: number): string {
  return `Celda ${idCelda}`;
}
