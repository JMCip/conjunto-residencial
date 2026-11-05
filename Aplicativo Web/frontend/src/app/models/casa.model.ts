export interface CasaModel {
  numero: number;
  manzana: number;
  area: number;
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
