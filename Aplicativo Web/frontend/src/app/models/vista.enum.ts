/** Secciones del menú lateral. Cada una corresponde a un componente dentro de vista-conjunto. */
export type Vista =
  'inicio' | 'personas-casas' | 'finanzas' | 'parqueadero' | 'salon-comunal' | 'sanciones';

/** Nombre visible de cada sección (menú y migas de pan). */
export const NOMBRES_VISTA: Record<Vista, string> = {
  inicio: 'Inicio',
  'personas-casas': 'Personas y casas',
  finanzas: 'Finanzas',
  parqueadero: 'Parqueadero',
  'salon-comunal': 'Salón comunal',
  sanciones: 'Sanciones',
};
