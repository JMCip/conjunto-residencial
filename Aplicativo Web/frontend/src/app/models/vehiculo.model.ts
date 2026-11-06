/**
 * Vehículo de una persona. La placa es la llave del registro.
 * Corresponde al JSON de GET /api/vehiculos (tabla `vehiculo`).
 */
export interface VehiculoModel {
  /** Placa del vehículo. */
  placa: string;
  /** Marca; null si no se registró. */
  marca: string | null;
  /** Año del modelo; null si no se registró. */
  modelo: number | null;
  /** Tipo de vehículo: CARRO o MOTO. */
  tipo: string;
  /** Tipo de documento del dueño. */
  personaTipoDocumento: string;
  /** Número de documento del dueño. */
  personaNumeroDocumento: string;
}

/** Tipos de vehículo que acepta la base de datos. */
export const TIPOS_VEHICULO = ['CARRO', 'MOTO'] as const;

/** Texto que se muestra para cada tipo de vehículo. */
export const ETIQUETA_TIPO_VEHICULO: Record<string, string> = {
  CARRO: 'Carro',
  MOTO: 'Moto',
};
