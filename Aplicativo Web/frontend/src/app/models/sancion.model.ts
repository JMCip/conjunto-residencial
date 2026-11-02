export interface SancionModel {
  idSancion: number;
  fechaIncidente: string;
  fechaNotificacion: string;
  tipo: string;
  descripcion: string;
  descargos: string;
  estadoProceso: string;
  valor: number | null;
  casaNumero: number;
  casaManzana: number;
}

/** Tipos posibles de sanción. */
export const TIPOS_SANCION = ['RUIDO', 'MASCOTAS', 'PARQUEO_INDEBIDO', 'DANO_ZONAS_COMUNES'] as const;

/** Estados posibles del proceso de sanción. */
export const ESTADOS_SANCION = ['EN_DESCARGOS', 'CONFIRMADA', 'ANULADA'] as const;

/** Texto que se muestra para cada tipo y estado de sanción. */
export const ETIQUETA_SANCION: Record<string, string> = {
  RUIDO: 'Ruido',
  MASCOTAS: 'Mascotas',
  PARQUEO_INDEBIDO: 'Parqueo indebido',
  DANO_ZONAS_COMUNES: 'Daño a zonas comunes',
  EN_DESCARGOS: 'En descargos',
  CONFIRMADA: 'Confirmada',
  ANULADA: 'Anulada',
};

/** Color de insignia de cada estado del proceso. */
export const ESTILO_SANCION: Record<string, 'alerta' | 'pendiente' | 'neutro'> = {
  CONFIRMADA: 'alerta',
  EN_DESCARGOS: 'pendiente',
  ANULADA: 'neutro',
};
