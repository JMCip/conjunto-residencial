export interface PersonaModel {
  tipoDocumento: string;
  numeroDocumento: string;
  correo: string | null;
  primerNombre: string;
  segunoNombre: string | null;
  primerApellido: string;
  segundoApellido: string | null;
}

/** Tipos de documento que se ofrecen en los formularios. */
export const TIPOS_DOCUMENTO = ['CC', 'CE', 'TI', 'PAS'] as const;

/** Ruta del backend de cada rol (especialización de persona). */
export type RutaRol = 'propietarios' | 'arrendatarios' | 'residentes' | 'visitantes';

/** Roles en el orden en que se muestran, con su texto en singular. */
export const ROLES_PERSONA: { ruta: RutaRol; texto: string }[] = [
  { ruta: 'propietarios', texto: 'Propietario' },
  { ruta: 'arrendatarios', texto: 'Arrendatario' },
  { ruta: 'residentes', texto: 'Residente' },
  { ruta: 'visitantes', texto: 'Visitante' },
];

/** Rol en singular, como lo espera POST/DELETE /personas/{tipo}/{numero}/roles/{rol}. */
export const ROL_SINGULAR: Record<RutaRol, string> = {
  propietarios: 'propietario',
  arrendatarios: 'arrendatario',
  residentes: 'residente',
  visitantes: 'visitante',
};

/** "CC|1010101010": llave de una persona en un solo texto (para <select> y búsquedas). */
export function llavePersona(tipoDocumento: string, numeroDocumento: string): string {
  return `${tipoDocumento}|${numeroDocumento}`;
}

/** Separa una llave "CC|1010101010" en tipo y número. */
export function separarLlavePersona(llave: string): { tipo: string; numero: string } {
  const [tipo, numero] = llave.split('|');
  return { tipo, numero };
}

/** "Carlos Andrés Gómez Ruiz". */
export function nombrePersona(p: PersonaModel): string {
  return [p.primerNombre, p.segunoNombre, p.primerApellido, p.segundoApellido]
    .filter(Boolean)
    .join(' ');
}
