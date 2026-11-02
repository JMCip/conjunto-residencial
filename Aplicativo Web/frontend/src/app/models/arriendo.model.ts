export interface ArriendoModel {
  idContrato: number;
  cuotaMensual: number;
  fechaInicio: string;
  fechaFin: string | null;
  estadoContrato: 'VIGENTE' | 'FINALIZADO';
  arrendatarioTipoDocumento: string;
  arrendatarioNumeroDocumento: string;
  casaNumero: number;
  casaManzana: number;
  propietarioTipoDocumento: string;
  propietarioNumeroDocumento: string;
}
