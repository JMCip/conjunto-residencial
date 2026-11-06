/**
 * Periodo en que un residente vive en una casa.
 * Corresponde al JSON de GET /api/residencias (tabla `residencia`).
 */
export interface ResidenciaModel {
  /** Identificador de la residencia (lo genera la base de datos). */
  idResidencia: number;
  /** Fecha de ingreso a la casa (yyyy-MM-dd). */
  fechaIngreso: string;
  /** Fecha de salida (yyyy-MM-dd); null si todavía vive ahí. */
  fechaSalida: string | null;
  /** Tipo de documento del residente. */
  residenteTipoDocumento: string;
  /** Número de documento del residente. */
  residenteNumeroDocumento: string;
  /** Número de la casa. */
  casaNumero: number;
  /** Manzana de la casa. */
  casaManzana: number;
}
