/**
 * Concepto del catálogo de cobros (administración, sanción, salón comunal…).
 * Corresponde al JSON de GET /api/conceptos (tabla `concepto_cobro`).
 */
export interface ConceptoCobroModel {
  /** Identificador del concepto (lo genera la base de datos). */
  idConcepto: number;
  /** Nombre del concepto; es único en el catálogo. */
  nombre: string;
  /** Descripción del concepto. */
  descripcion: string;
  /** Valor vigente del concepto en pesos. */
  valorBase: number;
}

/** Datos que se envían al crear o editar un concepto (al editar, el id va en la URL). */
export interface DatosConcepto {
  /** Nombre del concepto. */
  nombre: string;
  /** Descripción del concepto. */
  descripcion: string;
  /** Valor vigente del concepto en pesos. */
  valorBase: number;
}

/** Longitud máxima del nombre (columna `nombre` de `concepto_cobro`). */
export const MAX_NOMBRE_CONCEPTO = 100;

/** Longitud máxima de la descripción (columna `descripcion` de `concepto_cobro`). */
export const MAX_DESCRIPCION_CONCEPTO = 255;

/** Valor base máximo que admite la columna NUMERIC(12,2). */
export const MAX_VALOR_BASE_CONCEPTO = 9999999999;

/**
 * Conceptos que el sistema usa para cobrar administración, sanciones, reservas y celdas
 * adicionales. El backend no permite renombrarlos ni eliminarlos.
 */
export const CONCEPTOS_PROTEGIDOS = ['Sanción', 'Salón comunal', 'Parqueadero adicional', 'Administración',];

/**
 * Indica si un concepto es uno de los protegidos.
 * @param nombre Nombre del concepto (se compara sin distinguir mayúsculas).
 * @returns true si el concepto está en {@link CONCEPTOS_PROTEGIDOS}.
 */
export function esConceptoProtegido(nombre: string): boolean {
  const buscado = nombre.trim().toLowerCase();
  return CONCEPTOS_PROTEGIDOS.some((p) => p.toLowerCase() === buscado);
}

/**
 * Origen del valor de una línea de factura: una sanción, una reserva, una asignación de celda
 * o un cobro libre cuyo valor se escribe a mano.
 */
export type OrigenLinea = 'SANCION' | 'RESERVA' | 'ASIGNACION' | 'LIBRE';

/**
 * Determina de dónde sale el valor de una línea según el nombre de su concepto.
 * @param nombre Nombre del concepto de cobro.
 * @returns Origen de la línea.
 */
export function origenDeConcepto(nombre: string): OrigenLinea {
  const n = nombre.trim().toLowerCase();
  if (n === 'sanción') return 'SANCION';
  if (n === 'salón comunal') return 'RESERVA';
  if (n === 'parqueadero adicional') return 'ASIGNACION';
  return 'LIBRE';
}
