import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { extraerError } from '../compartido/extraer-error';

/**
 * @component Login
 * @description Pantalla de inicio de sesión del administrador. Valida que haya usuario y
 * contraseña, llama a AuthService y, si todo sale bien, entra a /inicio.
 */
@Component({
  selector: 'app-login',
  imports: [FormsModule],
  templateUrl: './login.html',
  styleUrl: './login.css',
})
export class Login {
  /** Servicio de autenticación. */
  private authService = inject(AuthService);

  /** Router para entrar a la aplicación después de iniciar sesión. */
  private router = inject(Router);

  /** Usuario escrito en el formulario. */
  protected readonly usuario = signal('');

  /** Contraseña escrita en el formulario. */
  protected readonly clave = signal('');

  /** true si la contraseña se muestra en texto plano. */
  protected readonly verClave = signal(false);

  /** true mientras se espera la respuesta del backend. */
  protected readonly cargando = signal(false);

  /** Mensaje de error del formulario (vacío si no hay error). */
  protected readonly mensajeError = signal('');

  /** Valida el formulario e inicia sesión. Si las credenciales son correctas, navega a /inicio. */
  iniciarSesion(): void {
    this.mensajeError.set('');

    if (!this.usuario().trim() || !this.clave()) {
      this.mensajeError.set('Debes ingresar usuario y contraseña.');
      return;
    }

    this.cargando.set(true);
    this.authService.login(this.usuario().trim(), this.clave()).subscribe({
      next: () => {
        this.cargando.set(false);
        this.router.navigate(['/inicio']);
      },
      error: (err) => {
        this.cargando.set(false);
        this.mensajeError.set(extraerError(err, 'No se pudo iniciar sesión.'));
      },
    });
  }
}
