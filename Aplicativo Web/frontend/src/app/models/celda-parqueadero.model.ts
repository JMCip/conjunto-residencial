/**
 * Celda del parqueadero del conjunto.
 * Corresponde al JSON de GET /api/celdas (tabla `celda_parqueadero`).
 */
export interface CeldaParqueaderoModel {
  /** Identificador de la celda (lo genera la base de datos). */
  idCelda: number;
  /** Tipo de celda: PROPIA, ADICIONAL o VISITANTES. */
  tipo: string;
  /** Estado de la celda: OCUPADA o DESOCUPADA. */
  estado: string;
}

/** Tipos de celda que acepta la base de datos. */
export const TIPOS_CELDA = ['PROPIA', 'ADICIONAL', 'VISITANTES'] as const;

/** Estados de celda que acepta la base de datos. */
export const ESTADOS_CELDA = ['OCUPADA', 'DESOCUPADA'] as const;

/** Texto que se muestra para cada tipo y estado de celda. */
export const ETIQUETA_CELDA: Record<string, string> = {
  PROPIA: 'Propia',
  ADICIONAL: 'Adicional',
  VISITANTES: 'Visitantes',
  OCUPADA: 'Ocupada',
  DESOCUPADA: 'Desocupada',
};

/**
 * Nombre con el que se muestra una celda.
 * @param idCelda Identificador de la celda.
 * @returns Texto con el formato "Celda 7".
 */
export function nombreCelda(idCelda: number): string {
  return `Celda ${idCelda}`;
}
