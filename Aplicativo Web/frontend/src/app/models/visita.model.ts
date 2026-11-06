/**
 * Visita de un visitante a una casa.
 * Corresponde al JSON de GET /api/visitas (tabla `visita`).
 */
export interface VisitaModel {
  /** Identificador de la visita (lo genera la base de datos). */
  idVisita: number;
  /** Fecha de entrada (yyyy-MM-dd). */
  fechaEntrada: string;
  /** Fecha de salida (yyyy-MM-dd); null si la visita no ha terminado. */
  fechaSalida: string | null;
  /** Tipo de documento del visitante. */
  visitanteTipoDocumento: string;
  /** Número de documento del visitante. */
  visitanteNumeroDocumento: string;
  /** Número de la casa visitada. */
  casaNumero: number;
  /** Manzana de la casa visitada. */
  casaManzana: number;
}
