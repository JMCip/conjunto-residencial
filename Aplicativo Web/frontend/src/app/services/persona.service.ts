import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { PersonaModel, RutaRol } from '../models/persona.model';
import { RespuestaApiModel } from '../models/respuesta-api.model';

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
   * Registra una persona nueva (sin roles).
   * @param persona Datos de la persona.
   * @returns Observable con el mensaje del backend.
   */
  postCrear(persona: PersonaModel): Observable<RespuestaApiModel> {
    return this.cliente.post<RespuestaApiModel>(this.urlbase + '/personas', persona);
  }

  /**
   * Actualiza nombres y correo. El documento es la llave y va en la URL.
   * @param persona Datos nuevos de la persona.
   * @returns Observable con el mensaje del backend.
   */
  putActualizar(persona: PersonaModel): Observable<RespuestaApiModel> {
    return this.cliente.put<RespuestaApiModel>(
      `${this.urlbase}/personas/${persona.tipoDocumento}/${persona.numeroDocumento}`,
      persona,
    );
  }

  /**
   * Elimina una persona.
   * @param tipoDocumento Tipo de documento (CC, CE…).
   * @param numeroDocumento Número de documento.
   * @returns Observable con el mensaje del backend.
   */
  deleteEliminar(tipoDocumento: string, numeroDocumento: string): Observable<RespuestaApiModel> {
    return this.cliente.delete<RespuestaApiModel>(
      `${this.urlbase}/personas/${tipoDocumento}/${numeroDocumento}`,
    );
  }

  /**
   * Le agrega un rol a una persona (fila en propietario, arrendatario, residente o visitante).
   * @param rol Ruta del rol.
   * @param tipoDocumento Tipo de documento de la persona.
   * @param numeroDocumento Número de documento de la persona.
   * @returns Observable con el mensaje del backend.
   */
  postAgregarRol(rol: RutaRol, tipoDocumento: string, numeroDocumento: string): Observable<RespuestaApiModel> {
    return this.cliente.post<RespuestaApiModel>(`${this.urlbase}/${rol}`, {
      tipoDocumento,
      numeroDocumento,
    });
  }

  /**
   * Le quita un rol a una persona.
   * @param rol Ruta del rol.
   * @param tipoDocumento Tipo de documento de la persona.
   * @param numeroDocumento Número de documento de la persona.
   * @returns Observable con el mensaje del backend.
   */
  deleteQuitarRol(rol: RutaRol, tipoDocumento: string, numeroDocumento: string): Observable<RespuestaApiModel> {
    return this.cliente.delete<RespuestaApiModel>(
      `${this.urlbase}/${rol}/${tipoDocumento}/${numeroDocumento}`,
    );
  }
}
