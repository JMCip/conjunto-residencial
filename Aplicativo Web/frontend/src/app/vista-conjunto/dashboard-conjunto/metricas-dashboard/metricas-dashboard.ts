import { Component, input } from '@angular/core';
import { CurrencyPipe, DecimalPipe } from '@angular/common';

/** Una tarjeta métrica del Inicio. `valor: null` se muestra como "Sin datos". */
export interface Metrica {
  /** Texto de arriba de la tarjeta. */
  etiqueta: string;

  /** Cifra principal; null si no hay datos. */
  valor: number | null;

  /** true si la cifra se muestra como moneda (COP). */
  esMoneda: boolean;

  /** Texto de apoyo debajo de la cifra. */
  contexto: string;

  /** Color del texto de apoyo. */
  tono: 'positivo' | 'atencion' | 'neutro';
}

/**
 * @component MetricasDashboard
 * @description Las 4 tarjetas métricas del Inicio (casas, residentes, cartera y reservas).
 * Solo dibuja: los valores los calcula el Inicio y se los pasa por `metricas`.
 */
@Component({
  selector: 'app-metricas-dashboard',
  imports: [CurrencyPipe, DecimalPipe],
  templateUrl: './metricas-dashboard.html',
  styleUrl: './metricas-dashboard.css',
})
export class MetricasDashboard {
  /** Tarjetas a mostrar, en orden. */
  readonly metricas = input.required<Metrica[]>();

  /** true mientras llegan los datos: muestra esqueletos en lugar de cifras. */
  readonly cargando = input.required<boolean>();
}
