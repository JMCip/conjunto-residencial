export interface PosesionModel {
  idPosesion: number;
  porcentaje: number;
  fechaCompra: string;
  fechaVenta: string | null;
  casaNumero: number;
  casaManzana: number;
  propietarioTipoDocumento: string;
  propietarioNumeroDocumento: string;
}
