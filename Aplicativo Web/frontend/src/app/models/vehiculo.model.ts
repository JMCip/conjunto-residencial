export interface VehiculoModel {
  placa: string;
  marca: string | null;
  modelo: number | null;
  tipo: string;
  personaTipoDocumento: string;
  personaNumeroDocumento: string;
}

/** Tipos posibles de vehículo. */
export const TIPOS_VEHICULO = ['CARRO', 'MOTO'] as const;

/** Texto que se muestra para cada tipo de vehículo. */
export const ETIQUETA_TIPO_VEHICULO: Record<string, string> = {
  CARRO: 'Carro',
  MOTO: 'Moto',
};
