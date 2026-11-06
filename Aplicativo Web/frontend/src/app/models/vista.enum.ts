/** Secciones del menú lateral. Cada valor es también la ruta de su pantalla (/finanzas…). */
export type Vista =
  'inicio' | 'personas-casas' | 'finanzas' | 'parqueadero' | 'salon-comunal' | 'sanciones';

/** Nombre que se muestra para cada sección en el menú y en las migas del topbar. */
export const NOMBRES_VISTA: Record<Vista, string> = {
  inicio: 'Inicio',
  'personas-casas': 'Personas y casas',
  finanzas: 'Finanzas',
  parqueadero: 'Parqueadero',
  'salon-comunal': 'Salón comunal',
  sanciones: 'Sanciones',
};
