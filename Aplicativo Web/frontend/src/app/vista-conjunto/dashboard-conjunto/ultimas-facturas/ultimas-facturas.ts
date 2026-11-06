import { Component, input, output } from '@angular/core';
import { CurrencyPipe } from '@angular/common';
import { ESTILO_FACTURA, ETIQUETA_FACTURA, FacturaModel } from '../../../models/factura.model';

/**
 * @component UltimasFacturas
 * @description Tarjeta del Inicio con la tabla corta de las facturas más recientes.
 * El Inicio le pasa la lista ya recortada; este componente solo la dibuja.
 */
@Component({
  selector: 'app-ultimas-facturas',
  imports: [CurrencyPipe],
  templateUrl: './ultimas-facturas.html',
  styleUrl: './ultimas-facturas.css',
})
export class UltimasFacturas {
  /** Facturas a mostrar (máximo 5, ya ordenadas). */
  readonly facturas = input.required<FacturaModel[]>();

  /** true mientras llegan los datos. */
  readonly cargando = input.required<boolean>();

  /** true si la consulta de facturas falló. */
  readonly error = input.required<boolean>();

  /** Avisa que el usuario pulsó "Ver todas". */
  readonly verTodas = output<void>();

  /** Texto de cada estado de factura (constante del modelo). */
  protected readonly etiquetaFactura = ETIQUETA_FACTURA;

  /** Color de insignia de cada estado de factura (constante del modelo). */
  protected readonly estiloFactura = ESTILO_FACTURA;
}
