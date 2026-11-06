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
import { Login } from './login/login';
import { authGuard, loginGuard } from './guards/auth.guard';

/**
 * Rutas de la aplicación. /login es pública; el resto vive dentro del layout VistaConjunto
 * y está protegido por {@link authGuard}. Cualquier ruta desconocida vuelve a /inicio.
 */
export const routes: Routes = [

  {
    path: 'login',
    component: Login,
    canActivate: [loginGuard],
    title: 'Iniciar sesión · Conjunto Residencial',
  },

  {
    path: '',
    component: VistaConjunto,
    canActivate: [authGuard],
    canActivateChild: [authGuard],
    children: [
      { path: '', redirectTo: 'inicio', pathMatch: 'full' },
      { path: 'inicio', component: DashboardConjunto, title: 'Inicio · Conjunto Residencial' },
      {
        path: 'personas-casas',
        component: PersonasCasas,
        title: 'Personas y casas · Conjunto Residencial',
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
