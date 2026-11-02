export type Vista =
  'inicio' | 'personas-casas' | 'finanzas' | 'parqueadero' | 'salon-comunal' | 'sanciones';

export const NOMBRES_VISTA: Record<Vista, string> = {
  inicio: 'Inicio',
  'personas-casas': 'Personas y casas',
  finanzas: 'Finanzas',
  parqueadero: 'Parqueadero',
  'salon-comunal': 'Salón comunal',
  sanciones: 'Sanciones',
};
