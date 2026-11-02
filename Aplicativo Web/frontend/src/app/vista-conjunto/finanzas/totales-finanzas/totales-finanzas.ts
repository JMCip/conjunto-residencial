import { Component, computed, input } from '@angular/core';
import { CurrencyPipe } from '@angular/common';
import { FacturaModel } from '../../../models/factura.model';

/**
 * @component TotalesFinanzas
 * @description Las 3 tarjetas de totales de Finanzas: facturado, recaudado (con barra de
 * progreso) y cartera pendiente. Recibe todas las facturas y calcula los totales.
 */
@Component({
  selector: 'app-totales-finanzas',
  imports: [CurrencyPipe],
  templateUrl: './totales-finanzas.html',
  styleUrl: './totales-finanzas.css',
})
export class TotalesFinanzas {
  /** Todas las facturas (sin filtrar). */
  readonly facturas = input.required<FacturaModel[]>();

  /** true mientras llegan los datos. */
  readonly cargando = input.required<boolean>();

  /** Suma del total de todas las facturas (las que no tienen total cuentan como 0). */
  protected readonly totalFacturado = computed(() =>
    this.facturas().reduce((total, f) => total + (f.total ?? 0), 0),
  );

  /** Suma de lo pagado en todas las facturas (incluye los abonos parciales). */
  protected readonly totalRecaudado = computed(() =>
    this.facturas().reduce((total, f) => total + f.totalPagado, 0),
  );

  /** Lo que falta por cobrar: suma de los saldos. */
  protected readonly totalPendiente = computed(() =>
    this.facturas().reduce((total, f) => total + (f.saldo ?? 0), 0),
  );

  /** Porcentaje recaudado, de 0 a 100, sin decimales. */
  protected readonly porcentajeRecaudado = computed(() => {
    const total = this.totalFacturado();
    return total === 0 ? 0 : Math.round((this.totalRecaudado() / total) * 100);
  });

  /** Cuántas facturas están pendientes. */
  protected readonly pendientes = computed(
    () => this.facturas().filter((f) => f.estado === 'PENDIENTE').length,
  );

  /** Cuántas facturas están vencidas. */
  protected readonly vencidas = computed(
    () => this.facturas().filter((f) => f.estado === 'VENCIDA').length,
  );
}
