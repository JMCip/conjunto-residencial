export interface CeldaParqueaderoModel {
  idCelda: number;
  tipo: string;
  estado: string;
}


export const TIPOS_CELDA = ['PROPIA', 'ADICIONAL', 'VISITANTES'] as const;

export const ESTADOS_CELDA = ['OCUPADA', 'DESOCUPADA'] as const;

export const ETIQUETA_CELDA: Record<string, string> = {
  PROPIA: 'Propia',
  ADICIONAL: 'Adicional',
  VISITANTES: 'Visitantes',
  OCUPADA: 'Ocupada',
  DESOCUPADA: 'Desocupada',
};


export function nombreCelda(idCelda: number): string {
  return `Celda ${idCelda}`;
}
