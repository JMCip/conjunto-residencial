export interface ConceptoCobroModel {
  idConcepto: number;
  nombre: string;
  descripcion: string;
  valorBase: number;
}

/** Lo que se envía al crear o editar: el id lo pone la base (al editar va en la URL). */
export interface DatosConcepto {
  nombre: string;
  descripcion: string;
  valorBase: number;
}

/** Largos y tope que acepta el backend (columnas de concepto_cobro). */
export const MAX_NOMBRE_CONCEPTO = 100;
export const MAX_DESCRIPCION_CONCEPTO = 255;
export const MAX_VALOR_BASE_CONCEPTO = 9999999999;

/**
 * Conceptos que el sistema usa para cobrar sanciones, reservas y celdas adicionales.
 * Son los mismos que protege el back: no se pueden renombrar ni eliminar.
 */
export const CONCEPTOS_PROTEGIDOS = ['Sanción', 'Salón comunal', 'Parqueadero adicional', 'Administración',];

/** true si el concepto es uno de los protegidos (compara sin importar mayúsculas). */
export function esConceptoProtegido(nombre: string): boolean {
  const buscado = nombre.trim().toLowerCase();
  return CONCEPTOS_PROTEGIDOS.some((p) => p.toLowerCase() === buscado);
}

/** De dónde sale el valor de una línea según su concepto. */
export type OrigenLinea = 'SANCION' | 'RESERVA' | 'ASIGNACION' | 'LIBRE';

export function origenDeConcepto(nombre: string): OrigenLinea {
  const n = nombre.trim().toLowerCase();
  if (n === 'sanción') return 'SANCION';
  if (n === 'salón comunal') return 'RESERVA';
  if (n === 'parqueadero adicional') return 'ASIGNACION';
  return 'LIBRE';
}
