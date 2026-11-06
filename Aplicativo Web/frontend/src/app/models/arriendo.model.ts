/**
 * Contrato de arriendo de una casa entre un propietario y un arrendatario.
 * Corresponde al JSON de GET /api/arriendos (tabla `arriendo`).
 */
export interface ArriendoModel {
  /** Identificador del contrato (lo genera la base de datos). */
  idContrato: number;
  /** Valor mensual del arriendo en pesos. */
  cuotaMensual: number;
  /** Fecha de inicio del contrato (yyyy-MM-dd). */
  fechaInicio: string;
  /** Fecha de terminación (yyyy-MM-dd); null si el contrato no tiene fecha de fin. */
  fechaFin: string | null;
  /** Estado del contrato. */
  estadoContrato: 'VIGENTE' | 'FINALIZADO';
  /** Tipo de documento del arrendatario. */
  arrendatarioTipoDocumento: string;
  /** Número de documento del arrendatario. */
  arrendatarioNumeroDocumento: string;
  /** Número de la casa arrendada. */
  casaNumero: number;
  /** Manzana de la casa arrendada. */
  casaManzana: number;
  /** Tipo de documento del propietario que arrienda. */
  propietarioTipoDocumento: string;
  /** Número de documento del propietario que arrienda. */
  propietarioNumeroDocumento: string;
}
