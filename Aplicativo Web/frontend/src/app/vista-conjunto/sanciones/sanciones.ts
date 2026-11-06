import { Component } from '@angular/core';
import { GestionSanciones } from './gestion-sanciones/gestion-sanciones';

/**
 * @component Sanciones
 * @description Sección Sanciones: cabecera y el CRUD de sanciones a las casas. Como solo tiene
 * un recurso, no lleva pestañas internas.
 */
@Component({
  selector: 'app-sanciones',
  imports: [GestionSanciones],
  templateUrl: './sanciones.html',
  styleUrl: './sanciones.css',
})
export class Sanciones {}
