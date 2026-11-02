import { Routes } from '@angular/router';
import { VistaConjunto } from './vista-conjunto/vista-conjunto';
import { DashboardConjunto } from './vista-conjunto/dashboard-conjunto/dashboard-conjunto';
import { PersonasCasas } from './vista-conjunto/personas-casas/personas-casas';
import { GestionCasas } from './vista-conjunto/personas-casas/gestion-casas/gestion-casas';
import { GestionPersonas } from './vista-conjunto/personas-casas/gestion-personas/gestion-personas';
import { GestionTelefonos } from './vista-conjunto/personas-casas/gestion-telefonos/gestion-telefonos';
import { GestionPosesiones } from './vista-conjunto/personas-casas/gestion-posesiones/gestion-posesiones';
import { GestionArriendos } from './vista-conjunto/personas-casas/gestion-arriendos/gestion-arriendos';
import { GestionResidencias } from './vista-conjunto/personas-casas/gestion-residencias/gestion-residencias';
import { GestionVisitas } from './vista-conjunto/personas-casas/gestion-visitas/gestion-visitas';
import { Finanzas } from './vista-conjunto/finanzas/finanzas';
import { Parqueadero } from './vista-conjunto/parqueadero/parqueadero';
import { GestionVehiculos } from './vista-conjunto/parqueadero/gestion-vehiculos/gestion-vehiculos';
import { GestionCeldas } from './vista-conjunto/parqueadero/gestion-celdas/gestion-celdas';
import { GestionAsignaciones } from './vista-conjunto/parqueadero/gestion-asignaciones/gestion-asignaciones';
import { GestionParqueos } from './vista-conjunto/parqueadero/gestion-parqueos/gestion-parqueos';
import { SalonComunal } from './vista-conjunto/salon-comunal/salon-comunal';
import { Sanciones } from './vista-conjunto/sanciones/sanciones';

/**
 * Rutas de la app. VistaConjunto es el layout (sidebar + topbar) y cada sección es una ruta hija
 * que se pinta en su <router-outlet>. El `path` de cada hija es el mismo texto del tipo `Vista`.
 */
export const routes: Routes = [
  {
    path: '',
    component: VistaConjunto,
    children: [
      { path: '', redirectTo: 'inicio', pathMatch: 'full' },
      { path: 'inicio', component: DashboardConjunto, title: 'Inicio · Conjunto Residencial' },
      {
        path: 'personas-casas',
        component: PersonasCasas,
        title: 'Personas y casas · Conjunto Residencial',
        // Pestañas internas: /personas-casas/casas, /personas-casas/personas…
        children: [
          { path: '', redirectTo: 'casas', pathMatch: 'full' },
          { path: 'casas', component: GestionCasas },
          { path: 'personas', component: GestionPersonas },
          { path: 'telefonos', component: GestionTelefonos },
          { path: 'posesiones', component: GestionPosesiones },
          { path: 'arriendos', component: GestionArriendos },
          { path: 'residencias', component: GestionResidencias },
          { path: 'visitas', component: GestionVisitas },
        ],
      },
      { path: 'finanzas', component: Finanzas, title: 'Finanzas · Conjunto Residencial' },
      {
        path: 'parqueadero',
        component: Parqueadero,
        title: 'Parqueadero · Conjunto Residencial',
        children: [
          { path: '', redirectTo: 'vehiculos', pathMatch: 'full' },
          { path: 'vehiculos', component: GestionVehiculos },
          { path: 'celdas', component: GestionCeldas },
          { path: 'asignaciones', component: GestionAsignaciones },
          { path: 'parqueos', component: GestionParqueos },
        ],
      },
      {
        path: 'salon-comunal',
        component: SalonComunal,
        title: 'Salón comunal · Conjunto Residencial',
      },
      { path: 'sanciones', component: Sanciones, title: 'Sanciones · Conjunto Residencial' },
    ],
  },
  { path: '**', redirectTo: 'inicio' },
];
