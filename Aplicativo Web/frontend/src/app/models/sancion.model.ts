export type EstadoSancion = 'EN_DESCARGOS' | 'CONFIRMADA' | 'ANULADA';

export interface SancionModel {
  idSancion: number;
  fechaIncidente: string;
  fechaNotificacion: string;
  tipo: string;
  descripcion: string;
  descargos: string;
  estadoProceso: EstadoSancion;
  valor: number | null;
  casaNumero: number;
  casaManzana: number;
}

/** Lo que se envía al crear: el back pone el estado (EN_DESCARGOS) y no acepta valor. */
export interface NuevaSancion {
  fechaIncidente: string; // yyyy-MM-dd
  fechaNotificacion: string; // yyyy-MM-dd
  tipo: string;
  descripcion: string;
  descargos?: string;
  casaNumero: number;
  casaManzana: number;
}

/** Lo que se envía al avanzar el proceso (PUT): descargos, decisión o ambos. */
export interface CambiosSancion {
  descargos?: string;
  estadoProceso?: 'CONFIRMADA' | 'ANULADA';
  valor?: number;
}

/** Texto que el back guarda en descargos cuando no se envían al crear. */
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

/** Sugerencias para el campo tipo (en el back es texto libre de máximo 30 caracteres). */
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
 * Texto legible de un tipo. El back guarda el tipo en mayúsculas ("RUIDO NOCTURNO"),
 * aquí se muestra como "Ruido nocturno".
 */
export function textoTipoSancion(tipo: string): string {
  const conocido = ETIQUETA_TIPO_SANCION[tipo];
  if (conocido) return conocido;
  const texto = tipo.replace(/_/g, ' ').trim().toLowerCase();
  return texto.charAt(0).toUpperCase() + texto.slice(1);
}

/** true si todavía no se han registrado descargos. */
export function sinDescargos(s: SancionModel): boolean {
  return !s.descargos || s.descargos === TEXTO_SIN_DESCARGOS;
}
