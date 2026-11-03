import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { PersonaModel } from '../models/persona.model';

/**
 * Servicio que consulta las personas del conjunto y sus roles
 * (propietarios, arrendatarios, residentes y visitantes).
 * Todas las rutas devuelven la misma forma de JSON (PersonaModel).
 */
@Injectable({
  providedIn: 'root',
})
export class PersonaService {
  private cliente: HttpClient = inject(HttpClient);

  /** URL base de la API. Cada rol tiene su propia ruta en la raíz. */
  private readonly urlbase = environment.apiUrl;

  /**
   * Obtiene todas las personas registradas.
   * @returns Observable con la lista de personas.
   */
  getTodos(): Observable<PersonaModel[]> {
    return this.cliente.get<PersonaModel[]>(this.urlbase + '/personas');
  }

  /**
   * Obtiene las personas que son propietarias.
   * @returns Observable con la lista de propietarios.
   */
  getPropietarios(): Observable<PersonaModel[]> {
    return this.cliente.get<PersonaModel[]>(this.urlbase + '/propietarios');
  }

  /**
   * Obtiene las personas que son arrendatarias.
   * @returns Observable con la lista de arrendatarios.
   */
  getArrendatarios(): Observable<PersonaModel[]> {
    return this.cliente.get<PersonaModel[]>(this.urlbase + '/arrendatarios');
  }

  /**
   * Obtiene las personas que son residentes.
   * @returns Observable con la lista de residentes.
   */
  getResidentes(): Observable<PersonaModel[]> {
    return this.cliente.get<PersonaModel[]>(this.urlbase + '/residentes');
  }

  /**
   * Obtiene las personas registradas como visitantes.
   * @returns Observable con la lista de visitantes.
   */
  getVisitantes(): Observable<PersonaModel[]> {
    return this.cliente.get<PersonaModel[]>(this.urlbase + '/visitantes');
  }
}
