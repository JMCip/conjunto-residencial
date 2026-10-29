import { Routes } from '@angular/router';
import { VistaConjunto } from './vista-conjunto/vista-conjunto';

export const routes: Routes = [
  { path: '', component: VistaConjunto },
  { path: '**', redirectTo: '' },
];
