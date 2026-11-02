import { afterNextRender, Component, ElementRef, input, output, viewChild } from '@angular/core';

/**
 * @component DialogoConfirmar
 * @description Ventana pequeña para confirmar una acción que no se puede deshacer
 * (eliminar un registro). No hace nada por sí sola: avisa confirmar o cancelar.
 */
@Component({
  selector: 'app-dialogo-confirmar',
  imports: [],
  templateUrl: './dialogo-confirmar.html',
  styleUrl: './dialogo-confirmar.css',
  host: { '(document:keydown.escape)': 'cancelarSiSePuede()' },
})
export class DialogoConfirmar {
  /** Pregunta en negrita ("¿Eliminar la casa?"). */
  readonly titulo = input.required<string>();

  /** Explicación de qué se va a borrar. */
  readonly mensaje = input.required<string>();

  /** Texto del botón de confirmar. */
  readonly textoConfirmar = input('Eliminar');

  /** true mientras se envía al backend: deshabilita los botones. */
  readonly procesando = input(false);

  /** Avisa que el usuario confirmó. */
  readonly confirmar = output<void>();

  /** Avisa que el usuario se arrepintió (Cancelar o Escape). */
  readonly cancelar = output<void>();

  /** Botón Cancelar: recibe el foco al abrir, para que Enter no borre por accidente. */
  private readonly botonCancelar = viewChild.required<ElementRef<HTMLButtonElement>>('cancelarBtn');

  constructor() {
    afterNextRender(() => this.botonCancelar().nativeElement.focus());
  }

  /** Cancela con Escape */
  cancelarSiSePuede(): void {
    if (!this.procesando()) this.cancelar.emit();
  }
}
