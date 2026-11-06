import { Component } from '@angular/core';
import { GestionReservas } from './gestion-reservas/gestion-reservas';

/**
 * @component SalonComunal
 * @description Sección Salón comunal: cabecera y el CRUD de reservas. Como solo tiene un
 * recurso, no lleva pestañas internas.
 */
@Component({
  selector: 'app-salon-comunal',
  imports: [GestionReservas],
  templateUrl: './salon-comunal.html',
  styleUrl: './salon-comunal.css',
})
export class SalonComunal {}
