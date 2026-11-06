import { Component, input, output } from '@angular/core';
import { CurrencyPipe, DatePipe } from '@angular/common';
import {
  ESTILO_FACTURA,
  ETIQUETA_FACTURA,
  FacturaModel,
  facturaAbierta,
} from '../../../models/factura.model';
/**
 * @component TablaFacturas
 * @description Cuerpo de la tarjeta de Finanzas: esqueleto mientras carga, estado vacío si
 * el filtro no encuentra nada, o la tabla de facturas con su pie de conteo.
 */
@Component({
  selector: 'app-tabla-facturas',
  imports: [CurrencyPipe, DatePipe],
  templateUrl: './tabla-facturas.html',
  styleUrl: './tabla-facturas.css',
})
export class TablaFacturas {
  /** Facturas visibles (ya filtradas y ordenadas por Finanzas). */
  readonly facturas = input.required<FacturaModel[]>();

  /** true mientras llegan los datos. */
  readonly cargando = input.required<boolean>();

  /** Texto del pie: "6 facturas" o "2 facturas de 6". */
  readonly textoConteo = input.required<string>();

  /** Avisa que el usuario pulsó "Ver todas las facturas" en el estado vacío. */
  readonly limpiarFiltros = output<void>();

  /** Texto de cada estado de factura (constante del modelo). */
  protected readonly etiquetaFactura = ETIQUETA_FACTURA;

  /** Color de insignia de cada estado de factura (constante del modelo). */
  protected readonly estiloFactura = ESTILO_FACTURA;

  /** Avisa que el usuario quiere ver las líneas de la factura. */
  readonly verDetalle = output<FacturaModel>();

  /** Avisa que el usuario quiere cambiar el vencimiento de la factura. */
  readonly editar = output<FacturaModel>();

  /** Avisa que el usuario quiere eliminar la factura. */
  readonly eliminar = output<FacturaModel>();

  /** Indica si una factura todavía se puede modificar (PENDIENTE o ABONADA). */
  protected readonly abierta = facturaAbierta;

}
