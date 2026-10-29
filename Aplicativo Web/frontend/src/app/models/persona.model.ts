/** Especialización de persona según el MER. */
export type TipoPersona = 'PROPIETARIO' | 'ARRENDATARIO' | 'RESIDENTE' | 'VISITANTE';

/**
 * Persona registrada en el conjunto (provisional hasta tener el Modelo Relacional).
 * Si la especialización resulta solapada, `tipo` pasará a ser un arreglo.
 */
export interface PersonaModel {
  id: number;
  documento: string;
  nombres: string;
  apellidos: string;
  telefono: string;
  correo: string;
  tipo: TipoPersona;
  idCasa: number | null;
  casa: string | null;
}
