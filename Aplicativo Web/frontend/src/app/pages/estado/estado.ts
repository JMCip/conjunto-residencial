import { Component, inject, OnInit, signal } from '@angular/core';
import { Api } from '../../core/services/api';
import { InfoBd } from '../../core/models/info-bd';

@Component({
  selector: 'app-estado',
  imports: [],
  templateUrl: './estado.html',
  styleUrl: './estado.css',
})
export class Estado implements OnInit {
  private readonly api = inject(Api);

  protected readonly mensaje = signal<string | null>(null);
  protected readonly errorBackend = signal<string | null>(null);
  protected readonly infoBd = signal<InfoBd | null>(null);
  protected readonly errorBd = signal<string | null>(null);

  ngOnInit(): void {
    this.api.getTexto('/estado').subscribe({
      next: (texto) => this.mensaje.set(texto),
      error: () => this.errorBackend.set('No se pudo contactar el backend.'),
    });

    this.api.get<InfoBd>('/bd/prueba').subscribe({
      next: (datos) => this.infoBd.set(datos),
      error: () => this.errorBd.set('No se pudo consultar la base de datos.'),
    });
  }
}
