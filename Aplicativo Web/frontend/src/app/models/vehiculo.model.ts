export interface VehiculoModel {
  placa: string;
  marca: string | null;
  modelo: number | null;
  tipo: string;
  personaTipoDocumento: string;
  personaNumeroDocumento: string;
}

export const TIPOS_VEHICULO = ['CARRO', 'MOTO'] as const;


export const ETIQUETA_TIPO_VEHICULO: Record<string, string> = {
  CARRO: 'Carro',
  MOTO: 'Moto',
};
