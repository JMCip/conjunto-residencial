export interface ParqueoModel {
  idParqueo: number;
  fechaHoraEntrada: string;
  fechaHoraSalida: string | null;
  vehiculoPlaca: string;
  idCelda: number;
}

/**
 * Pasa "2026-10-28 07:30:00" (JSON) a "2026-10-28T07:30" (valor de <input type="datetime-local">).
 * @param texto Fecha y hora del backend, o null.
 */
export function aInputFechaHora(texto: string | null): string {
  return texto ? texto.replace(' ', 'T').slice(0, 16) : '';
}

/**
 * Pasa "2026-10-28T07:30" (input) a "2026-10-28 07:30:00" (mismo formato que devuelve el GET).
 * @param valor Valor del input, o '' si está vacío.
 */
export function desdeInputFechaHora(valor: string): string | null {
  return valor ? `${valor.replace('T', ' ')}:00` : null;
}

/**
 * Pasa "2026-10-28 07:30:00" a "28/10/2026 07:30" para las tablas.
 * @param texto Fecha y hora del backend.
 */
export function textoFechaHora(texto: string): string {
  return `${texto.slice(8, 10)}/${texto.slice(5, 7)}/${texto.slice(0, 4)} ${texto.slice(11, 16)}`;
}
