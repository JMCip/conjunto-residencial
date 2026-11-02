
export interface CasaModel {
  numero: number;
  manzana: number;
  area: number;
  estado: string;
  numeroHabitaciones: number | null;
}

/** "Mz 1 · Casa 3": así se nombra una casa en toda la app. */
export function nombreCasa(manzana: number, numero: number): string {
  return `Mz ${manzana} · Casa ${numero}`;
}

/** "1|3": llave de una casa en un solo texto (para <select>). */
export function llaveCasa(manzana: number, numero: number): string {
  return `${manzana}|${numero}`;
}

/** Separa una llave "1|3" en manzana y número. */
export function separarLlaveCasa(llave: string): { manzana: number; numero: number } {
  const [manzana, numero] = llave.split('|').map(Number);
  return { manzana, numero };
}

/** Estados posibles de una casa. */
export const ESTADOS_CASA = ['HABITADA', 'ARRENDADA', 'DESOCUPADA'] as const;

/** Texto que se muestra en pantalla para cada estado de casa. */
export const ETIQUETA_CASA: Record<string, string> = {
  HABITADA: 'Habitada',
  ARRENDADA: 'Arrendada',
  DESOCUPADA: 'Desocupada',
};

/** Color de insignia de cada estado de casa. */
export const ESTILO_CASA: Record<string, 'ok' | 'pendiente' | 'neutro'> = {
  HABITADA: 'ok',
  ARRENDADA: 'pendiente',
  DESOCUPADA: 'neutro',
};
