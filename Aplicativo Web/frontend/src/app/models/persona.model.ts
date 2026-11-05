export interface PersonaModel {
  tipoDocumento: string;
  numeroDocumento: string;
  correo: string | null;
  primerNombre: string;
  segunoNombre: string | null;
  primerApellido: string;
  segundoApellido: string | null;
}


export const TIPOS_DOCUMENTO = ['CC', 'CE', 'TI', 'PAS'] as const;


export type RutaRol = 'propietarios' | 'arrendatarios' | 'residentes' | 'visitantes';


export const ROLES_PERSONA: { ruta: RutaRol; texto: string }[] = [
  { ruta: 'propietarios', texto: 'Propietario' },
  { ruta: 'arrendatarios', texto: 'Arrendatario' },
  { ruta: 'residentes', texto: 'Residente' },
  { ruta: 'visitantes', texto: 'Visitante' },
];


export const ROL_SINGULAR: Record<RutaRol, string> = {
  propietarios: 'propietario',
  arrendatarios: 'arrendatario',
  residentes: 'residente',
  visitantes: 'visitante',
};


export function llavePersona(tipoDocumento: string, numeroDocumento: string): string {
  return `${tipoDocumento}|${numeroDocumento}`;
}


export function separarLlavePersona(llave: string): { tipo: string; numero: string } {
  const [tipo, numero] = llave.split('|');
  return { tipo, numero };
}


export function nombrePersona(p: PersonaModel): string {
  return [p.primerNombre, p.segunoNombre, p.primerApellido, p.segundoApellido]
    .filter(Boolean)
    .join(' ');
}
