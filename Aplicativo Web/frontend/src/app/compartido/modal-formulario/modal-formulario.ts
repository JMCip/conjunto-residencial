import { afterNextRender, Component, ElementRef, inject, input, output } from '@angular/core';

/** Milisegundos en los que se ignora un segundo envío (doble clic). */
const ESPERA_ENTRE_ENVIOS = 800;

/**
 * @component ModalFormulario
 * @description Ventana modal reutilizable para crear o editar un registro. Dibuja la cabecera,
 * el pie con Cancelar/Guardar y, en el medio, los campos que le pase el padre con <ng-content>.
 * No guarda nada: solo avisa guardar o cerrar.
 */
@Component({
  selector: 'app-modal-formulario',
  imports: [],
  templateUrl: './modal-formulario.html',
  styleUrl: './modal-formulario.css',
  host: { '(document:keydown.escape)': 'cerrarSiSePuede()' },
})
export class ModalFormulario {
  /** Momento del último envío, para ignorar el doble clic. */
  private ultimoEnvio = 0;

  /** Elemento del componente, para enfocar el primer campo al abrir. */
  private elemento = inject<ElementRef<HTMLElement>>(ElementRef);

  /** 'amplio' para modales con tablas (detalle de factura). */
  readonly ancho = input<'normal' | 'amplio'>('normal');

  /** true: el pie solo muestra «Cerrar» (modales de gestión sin botón Guardar). */
  readonly soloCerrar = input(false);

  /** Título de la ventana ("Nueva casa", "Editar casa"). */
  readonly titulo = input.required<string>();

  /** Texto del botón principal. */
  readonly textoGuardar = input('Guardar');

  /** true mientras se envía al backend: deshabilita los botones. */
  readonly guardando = input(false);

  /** Mensaje de validación para mostrar arriba del pie (null = sin error). */
  readonly error = input<string | null>(null);

  /** Avisa que el usuario pulsó Guardar (o Enter en un campo). */
  readonly guardar = output<void>();

  /** Avisa que el usuario quiere cerrar (Cancelar, la X o Escape). */
  readonly cerrar = output<void>();

  /** Al abrir el modal, enfoca el primer campo habilitado del formulario. */
  constructor() {
    afterNextRender(() => {
      setTimeout(() =>
        this.elemento.nativeElement
          .querySelector<HTMLElement>('input:not(:disabled), select:not(:disabled)')
          ?.focus(),
      );
    });
  }

  /**
   * Envía el formulario sin recargar la página.
   * @param evento Evento submit del <form>.
   */
  enviar(evento: Event): void {
    evento.preventDefault();
    const ahora = Date.now();
    if (this.guardando() || ahora - this.ultimoEnvio < ESPERA_ENTRE_ENVIOS) return;
    this.ultimoEnvio = ahora;
    this.guardar.emit();
  }

  /** Cierra el modal con Escape, salvo mientras se está guardando. */
  cerrarSiSePuede(): void {
    if (!this.guardando()) this.cerrar.emit();
  }
}
