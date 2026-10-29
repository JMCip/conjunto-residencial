import { Component, input, output } from '@angular/core';
import { Vista } from '../../models/vista.enum';

/** Un paso del avance hacia tener datos reales. */
interface PasoAvance {
  nombre: string;
  estado: 'En curso' | 'Pendiente' | 'Lista';
  /** DESIGN.md: "Pendiente" y "En curso" usan el color ámbar; "Lista", el verde. */
  estilo: 'pendiente' | 'ok';
}

/**
 * Avance del proyecto, igual para todos los módulos sin datos.
 * Se actualiza aquí cuando cambie el estado del Modelo Relacional, las tablas o el backend.
 */
const PASOS_AVANCE: PasoAvance[] = [
  { nombre: 'Modelo relacional en 3FN', estado: 'En curso', estilo: 'pendiente' },
  { nombre: 'Tablas en PostgreSQL', estado: 'Pendiente', estilo: 'pendiente' },
  { nombre: 'Consulta en el backend', estado: 'Pendiente', estilo: 'pendiente' },
];

/**
 * @component ModuloPendiente
 * @description Tarjeta reutilizable para los módulos que todavía no tienen endpoint ni mock:
 * explica qué mostrarán y cuánto falta para conectarlos.
 */
@Component({
  selector: 'app-modulo-pendiente',
  imports: [],
  templateUrl: './modulo-pendiente.html',
  styleUrl: './modulo-pendiente.css',
})
export class ModuloPendiente {
  /** Nombre del módulo ("Parqueadero"). */
  readonly modulo = input.required<string>();

  /** Qué mostrará el módulo cuando esté conectado (completa la frase "aquí verás…"). */
  readonly descripcion = input.required<string>();

  /** Pide al contenedor cambiar de sección (botones de navegación). */
  readonly irA = output<Vista>();

  /** Pasos del avance (constante). */
  protected readonly pasos = PASOS_AVANCE;
}
