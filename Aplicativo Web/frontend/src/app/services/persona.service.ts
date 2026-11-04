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

  /**
   * Consulta una persona por su documento.
   * @param tipoDocumento Tipo de documento.
   * @param numeroDocumento Número de documento.
   */
  getPorDocumento(tipoDocumento: string, numeroDocumento: string): Observable<PersonaModel> {
    return this.cliente.get<PersonaModel>(
      `${this.urlbase}/personas/${tipoDocumento}/${numeroDocumento}`
    );
  }

  /**
   * Crea una persona. Queda sin roles hasta que se le asigne alguno.
   * @param persona Datos de la persona.
   */
  crear(persona: PersonaModel): Observable<{ mensaje: string }> {
    return this.cliente.post<{ mensaje: string }>(this.urlbase + '/personas', persona);
  }

  /**
   * Actualiza nombres, apellidos y correo. El documento viaja en la URL y no cambia.
   * @param persona Datos de la persona (incluye su documento).
   */
  actualizar(persona: PersonaModel): Observable<{ mensaje: string }> {
    return this.cliente.put<{ mensaje: string }>(
      `${this.urlbase}/personas/${persona.tipoDocumento}/${persona.numeroDocumento}`,
      persona
    );
  }

  /**
   * Elimina una persona. Responde 409 si tiene roles, teléfonos o historial asociado.
   * @param tipoDocumento Tipo de documento.
   * @param numeroDocumento Número de documento.
   */
  eliminar(tipoDocumento: string, numeroDocumento: string): Observable<{ mensaje: string }> {
    return this.cliente.delete<{ mensaje: string }>(
      `${this.urlbase}/personas/${tipoDocumento}/${numeroDocumento}`
    );
  }

  /**
   * Asigna un rol a una persona existente.
   * @param rol En singular: propietario, arrendatario, residente o visitante.
   */
  asignarRol(tipoDocumento: string, numeroDocumento: string, rol: string): Observable<{ mensaje: string }> {
    return this.cliente.post<{ mensaje: string }>(
      `${this.urlbase}/personas/${tipoDocumento}/${numeroDocumento}/roles/${rol}`,
      null
    );
  }

  /**
   * Quita un rol a una persona. Responde 409 si el rol tiene historial asociado.
   * @param rol En singular: propietario, arrendatario, residente o visitante.
   */
  quitarRol(tipoDocumento: string, numeroDocumento: string, rol: string): Observable<{ mensaje: string }> {
    return this.cliente.delete<{ mensaje: string }>(
      `${this.urlbase}/personas/${tipoDocumento}/${numeroDocumento}/roles/${rol}`
    );
  }
}


