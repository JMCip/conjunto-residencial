import { Component, input, output } from '@angular/core';
import { EstadoFactura } from '../../../models/factura.model';

/** Filtro del control segmentado: todas o un estado. */
export type FiltroEstado = 'TODAS' | EstadoFactura;

/** Botones del control segmentado, en orden. */
const OPCIONES_FILTRO: { valor: FiltroEstado; texto: string }[] = [
  { valor: 'TODAS', texto: 'Todas' },
  { valor: 'PAGADA', texto: 'Pagadas' },
  { valor: 'PENDIENTE', texto: 'Pendientes' },
  { valor: 'ABONADA', texto: 'Abonadas' },
  { valor: 'VENCIDA', texto: 'Vencidas' },
];

/**
 * @component FiltrosFacturas
 * @description Control segmentado por estado y buscador de Finanzas.
 * No guarda nada: muestra lo que le pasa Finanzas y le avisa cuando el usuario cambia algo.
 */
@Component({
  selector: 'app-filtros-facturas',
  imports: [],
  templateUrl: './filtros-facturas.html',
  styleUrl: './filtros-facturas.css',
})
export class FiltrosFacturas {
  /** Filtro activo. */
  readonly filtroEstado = input.required<FiltroEstado>();

  /** Texto actual del buscador. */
  readonly busqueda = input.required<string>();

  /** Cuántas facturas hay de cada estado (se muestran en los botones). */
  readonly conteos = input.required<Record<FiltroEstado, number>>();

  /** true mientras llegan los datos (oculta los conteos). */
  readonly cargando = input.required<boolean>();

  /** Avisa el filtro que eligió el usuario. */
  readonly cambiarFiltro = output<FiltroEstado>();

  /** Avisa el texto que escribió el usuario. */
  readonly buscar = output<string>();

  /** Opciones del control segmentado (constante). */
  protected readonly opcionesFiltro = OPCIONES_FILTRO;
}
