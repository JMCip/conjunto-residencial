/**
 * Asignación de una celda de parqueadero a una casa durante un periodo.
 * Corresponde al JSON de GET /api/asignaciones-celda (tabla `asignacion_celda`).
 */
export interface AsignacionCeldaModel {
  /** Identificador de la asignación (lo genera la base de datos). */
  idAsignacion: number;
  /** Fecha desde la que la casa usa la celda (yyyy-MM-dd). */
  fechaInicio: string;
  /** Fecha en que terminó la asignación (yyyy-MM-dd); null si sigue vigente. */
  fechaFin: string | null;
  /** Número de la casa a la que se asigna la celda. */
  casaNumero: number;
  /** Manzana de la casa a la que se asigna la celda. */
  casaManzana: number;
  /** Celda de parqueadero asignada. */
  idCelda: number;
}
