/**
 * Da formato yyyy-MM-dd a una fecha usando la hora local.
 * @param d Fecha a convertir.
 * @returns Texto con el formato "2026-11-05".
 */
function aISO(d: Date): string {
  const mes = String(d.getMonth() + 1).padStart(2, '0');
  const dia = String(d.getDate()).padStart(2, '0');
  return `${d.getFullYear()}-${mes}-${dia}`;
}

/**
 * Fecha de hoy en hora local.
 * @returns Texto con el formato yyyy-MM-dd.
 */
export function hoyISO(): string {
  return aISO(new Date());
}

/**
 * Suma días a una fecha.
 * @param iso Fecha de partida (yyyy-MM-dd).
 * @param dias Cantidad de días que se suman (puede ser negativa).
 * @returns Fecha resultante (yyyy-MM-dd).
 */
export function sumarDias(iso: string, dias: number): string {
  const [a, m, d] = iso.split('-').map(Number);
  return aISO(new Date(a, m - 1, d + dias));
}
