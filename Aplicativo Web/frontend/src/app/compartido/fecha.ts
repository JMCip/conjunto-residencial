function aISO(d: Date): string {
  const mes = String(d.getMonth() + 1).padStart(2, '0');
  const dia = String(d.getDate()).padStart(2, '0');
  return `${d.getFullYear()}-${mes}-${dia}`;
}

/** Hoy como "yyyy-MM-dd" en hora LOCAL. */
export function hoyISO(): string {
  return aISO(new Date());
}

/** Suma días a una fecha "yyyy-MM-dd". */
export function sumarDias(iso: string, dias: number): string {
  const [a, m, d] = iso.split('-').map(Number);
  return aISO(new Date(a, m - 1, d + dias));
}
