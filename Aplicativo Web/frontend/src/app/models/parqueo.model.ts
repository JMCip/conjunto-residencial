/**
 * Registro de entrada y salida de un vehículo en una celda.
 * Corresponde al JSON de GET /api/parqueos (tabla `parqueo`).
 */
export interface ParqueoModel {
  /** Identificador del parqueo (lo genera la base de datos). */
  idParqueo: number;
  /** Fecha y hora de entrada (yyyy-MM-dd HH:mm:ss). */
  fechaHoraEntrada: string;
  /** Fecha y hora de salida (yyyy-MM-dd HH:mm:ss); null si el vehículo sigue adentro. */
  fechaHoraSalida: string | null;
  /** Placa del vehículo. */
  vehiculoPlaca: string;
  /** Celda que ocupa el vehículo. */
  idCelda: number;
}

/**
 * Convierte una fecha y hora del backend al valor de un <input type="datetime-local">.
 * @param texto Fecha y hora con el formato "2026-10-28 07:30:00", o null.
 * @returns Texto con el formato "2026-10-28T07:30", o '' si no hay fecha.
 */
export function aInputFechaHora(texto: string | null): string {
  return texto ? texto.replace(' ', 'T').slice(0, 16) : '';
}

/**
 * Convierte el valor de un <input type="datetime-local"> al formato que usa el backend.
 * @param valor Texto con el formato "2026-10-28T07:30", o '' si el campo está vacío.
 * @returns Texto con el formato "2026-10-28 07:30:00", o null si el campo está vacío.
 */
export function desdeInputFechaHora(valor: string): string | null {
  return valor ? `${valor.replace('T', ' ')}:00` : null;
}

/**
 * Da formato de tabla a una fecha y hora del backend.
 * @param texto Fecha y hora con el formato "2026-10-28 07:30:00".
 * @returns Texto con el formato "28/10/2026 07:30".
 */
export function textoFechaHora(texto: string): string {
  return `${texto.slice(8, 10)}/${texto.slice(5, 7)}/${texto.slice(0, 4)} ${texto.slice(11, 16)}`;
}
