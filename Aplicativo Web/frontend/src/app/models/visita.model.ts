export interface VisitaModel {
  idVisita: number;
  fechaEntrada: string;
  fechaSalida: string | null;
  visitanteTipoDocumento: string;
  visitanteNumeroDocumento: string;
  casaNumero: number;
  casaManzana: number;
}
