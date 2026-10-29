import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { environment } from '../../environments/environment';
import { PersonaModel } from '../models/persona.model';

/** Ruta provisional del recurso (se confirma cuando el backend la publique). */
const RUTA = '/personas';

/**
 * Servicio que consulta las personas del conjunto (propietarios, arrendatarios, residentes y visitantes).
 * Todavía no tiene mock: con usarMocks en true devuelve una lista vacía (módulo pendiente).
 */
@Injectable({ providedIn: 'root' })
export class PersonaService {
  private cliente: HttpClient = inject(HttpClient);

  /** URL base del recurso. Relativa: el proxy la reenvía al backend. */
  private readonly urlbase = `${environment.apiUrl}${RUTA}`;

  /**
   * Obtiene todas las personas.
   * @returns Observable con la lista de personas.
   */
  getTodas(): Observable<PersonaModel[]> {
    if (environment.usarMocks) return of([]);
    return this.cliente.get<PersonaModel[]>(this.urlbase);
  }
}
