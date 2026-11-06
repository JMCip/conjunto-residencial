/** Estados del proceso de una sanción. */
export type EstadoSancion = 'EN_DESCARGOS' | 'CONFIRMADA' | 'ANULADA';

/**
 * Sanción a una casa por un incidente de convivencia.
 * Corresponde al JSON de GET /api/sanciones (tabla `sancion`).
 */
export interface SancionModel {
  /** Identificador de la sanción (lo genera la base de datos). */
  idSancion: number;
  /** Fecha en que ocurrió el incidente (yyyy-MM-dd). */
  fechaIncidente: string;
  /** Fecha en que se notificó a la casa (yyyy-MM-dd). */
  fechaNotificacion: string;
  /** Tipo de falta, en mayúsculas (por ejemplo "RUIDO"). */
  tipo: string;
  /** Descripción del incidente. */
  descripcion: string;
  /** Descargos presentados por la casa. */
  descargos: string;
  /** Estado del proceso. */
  estadoProceso: EstadoSancion;
  /** Valor de la multa; null si no se ha definido o la sanción no genera multa. */
  valor: number | null;
  /** Número de la casa sancionada. */
  casaNumero: number;
  /** Manzana de la casa sancionada. */
  casaManzana: number;
}

/** Datos que se envían al crear una sanción; el backend la deja EN_DESCARGOS y sin valor. */
export interface NuevaSancion {
  /** Fecha en que ocurrió el incidente (yyyy-MM-dd). */
  fechaIncidente: string;
  /** Fecha en que se notificó a la casa (yyyy-MM-dd). */
  fechaNotificacion: string;
  /** Tipo de falta. */
  tipo: string;
  /** Descripción del incidente. */
  descripcion: string;
  /** Descargos iniciales; si se omiten, el backend guarda {@link TEXTO_SIN_DESCARGOS}. */
  descargos?: string;
  /** Número de la casa sancionada. */
  casaNumero: number;
  /** Manzana de la casa sancionada. */
  casaManzana: number;
}

/** Datos que se envían al avanzar el proceso (PUT): descargos, decisión o ambos. */
export interface CambiosSancion {
  /** Descargos de la casa. */
  descargos?: string;
  /** Decisión sobre la sanción. */
  estadoProceso?: 'CONFIRMADA' | 'ANULADA';
  /** Valor de la multa; solo se envía al confirmar. */
  valor?: number;
}

/** Texto que el backend guarda en descargos cuando no se envían al crear. */
export const TEXTO_SIN_DESCARGOS = 'Pendiente de descargos';

/** Estados de sanción, en el orden de los filtros. */
export const ESTADOS_SANCION: EstadoSancion[] = ['EN_DESCARGOS', 'CONFIRMADA', 'ANULADA'];

/** Texto que se muestra para cada estado. */
export const ETIQUETA_ESTADO_SANCION: Record<EstadoSancion, string> = {
  EN_DESCARGOS: 'En descargos',
  CONFIRMADA: 'Confirmada',
  ANULADA: 'Anulada',
};

/** Texto de cada botón del filtro (plural). */
export const FILTRO_SANCION: Record<EstadoSancion, string> = {
  EN_DESCARGOS: 'En descargos',
  CONFIRMADA: 'Confirmadas',
  ANULADA: 'Anuladas',
};

/** Color de insignia de cada estado del proceso. */
export const ESTILO_SANCION: Record<EstadoSancion, 'alerta' | 'pendiente' | 'neutro'> = {
  CONFIRMADA: 'alerta',
  EN_DESCARGOS: 'pendiente',
  ANULADA: 'neutro',
};

/** Sugerencias para el campo tipo (en el backend es texto libre de máximo 30 caracteres). */
export const SUGERENCIAS_TIPO_SANCION = [
  'Ruido',
  'Mascotas',
  'Parqueo indebido',
  'Daño a zonas comunes',
];

/** Tipos guardados con otro formato (con guion bajo) que se muestran con tilde y espacios. */
const ETIQUETA_TIPO_SANCION: Record<string, string> = {
  PARQUEO_INDEBIDO: 'Parqueo indebido',
  DANO_ZONAS_COMUNES: 'Daño a zonas comunes',
};

/**
 * Convierte el tipo guardado por el backend en un texto legible.
 * @param tipo Tipo en mayúsculas, por ejemplo "RUIDO NOCTURNO" o "PARQUEO_INDEBIDO".
 * @returns Texto con mayúscula inicial, por ejemplo "Ruido nocturno".
 */
export function textoTipoSancion(tipo: string): string {
  const conocido = ETIQUETA_TIPO_SANCION[tipo];
  if (conocido) return conocido;
  const texto = tipo.replace(/_/g, ' ').trim().toLowerCase();
  return texto.charAt(0).toUpperCase() + texto.slice(1);
}

/**
 * Indica si la sanción todavía no tiene descargos registrados.
 * @param s Sanción a revisar.
 * @returns true si los descargos están vacíos o son el texto por defecto.
 */
export function sinDescargos(s: SancionModel): boolean {
  return !s.descargos || s.descargos === TEXTO_SIN_DESCARGOS;
}
