/**
 * Casa del conjunto residencial. Su llave es la combinación manzana + número.
 * Corresponde al JSON de GET /api/casas (tabla `casa`).
 */
export interface CasaModel {
  /** Número de la casa dentro de su manzana. */
  numero: number;
  /** Manzana a la que pertenece la casa. */
  manzana: number;
  /** Área construida en metros cuadrados. */
  area: number;
  /** Número de habitaciones; null si no se registró. */
  numeroHabitaciones: number | null;
}

/**
 * Nombre con el que se muestra una casa en toda la aplicación.
 * @param manzana Manzana de la casa.
 * @param numero Número de la casa.
 * @returns Texto con el formato "Mz 1 · Casa 3".
 */
export function nombreCasa(manzana: number, numero: number): string {
  return `Mz ${manzana} · Casa ${numero}`;
}

/**
 * Une la llave compuesta de una casa en un solo texto, para usarla como valor de un <select>.
 * @param manzana Manzana de la casa.
 * @param numero Número de la casa.
 * @returns Texto con el formato "1|3".
 */
export function llaveCasa(manzana: number, numero: number): string {
  return `${manzana}|${numero}`;
}

/**
 * Separa una llave creada con {@link llaveCasa} en manzana y número.
 * @param llave Texto con el formato "1|3".
 * @returns Manzana y número de la casa.
 */
export function separarLlaveCasa(llave: string): { manzana: number; numero: number } {
  const [manzana, numero] = llave.split('|').map(Number);
  return { manzana, numero };
}
