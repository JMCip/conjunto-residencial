
export interface ResidenciaModel {
  idResidencia: number;
  fechaIngreso: string;
  fechaSalida: string | null;
  residenteTipoDocumento: string;
  residenteNumeroDocumento: string;
  casaNumero: number;
  casaManzana: number;
}
