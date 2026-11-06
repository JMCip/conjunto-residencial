/**
 * Persona registrada en el conjunto. Su llave es la combinación tipo + número de documento.
 * Corresponde al JSON de GET /api/personas (tabla `persona`).
 */
export interface PersonaModel {
  /** Tipo de documento (CC, CE, TI o PAS). */
  tipoDocumento: string;
  /** Número de documento. */
  numeroDocumento: string;
  /** Correo electrónico; null si no se registró. */
  correo: string | null;
  /** Primer nombre. */
  primerNombre: string;
  /** Segundo nombre; null si no tiene. El nombre de la propiedad es el que entrega el backend. */
  segundoNombre: string | null;
  /** Primer apellido. */
  primerApellido: string;
  /** Segundo apellido; null si no tiene. */
  segundoApellido: string | null;
}

/** Tipos de documento que acepta la base de datos. */
export const TIPOS_DOCUMENTO = ['CC', 'CE', 'TI', 'PAS'] as const;

/** Ruta del backend de cada rol de persona (subclases de la especialización). */
export type RutaRol = 'propietarios' | 'arrendatarios' | 'residentes' | 'visitantes';

/** Roles que puede tener una persona, con el texto que se muestra en las casillas. */
export const ROLES_PERSONA: { ruta: RutaRol; texto: string }[] = [
  { ruta: 'propietarios', texto: 'Propietario' },
  { ruta: 'arrendatarios', texto: 'Arrendatario' },
  { ruta: 'residentes', texto: 'Residente' },
  { ruta: 'visitantes', texto: 'Visitante' },
];

/** Nombre en singular de cada rol, para los mensajes. */
export const ROL_SINGULAR: Record<RutaRol, string> = {
  propietarios: 'propietario',
  arrendatarios: 'arrendatario',
  residentes: 'residente',
  visitantes: 'visitante',
};

/**
 * Une la llave compuesta de una persona en un solo texto, para usarla como valor de un <select>.
 * @param tipoDocumento Tipo de documento.
 * @param numeroDocumento Número de documento.
 * @returns Texto con el formato "CC|1020304050".
 */
export function llavePersona(tipoDocumento: string, numeroDocumento: string): string {
  return `${tipoDocumento}|${numeroDocumento}`;
}

/**
 * Separa una llave creada con {@link llavePersona} en tipo y número de documento.
 * @param llave Texto con el formato "CC|1020304050".
 * @returns Tipo y número de documento.
 */
export function separarLlavePersona(llave: string): { tipo: string; numero: string } {
  const [tipo, numero] = llave.split('|');
  return { tipo, numero };
}

/**
 * Nombre completo de una persona, sin los nombres o apellidos vacíos.
 * @param p Persona.
 * @returns Nombres y apellidos separados por espacios.
 */
export function nombrePersona(p: PersonaModel): string {
  return [p.primerNombre, p.segundoNombre, p.primerApellido, p.segundoApellido]
    .filter(Boolean)
    .join(' ');
}
