import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { PersonaModel } from '../models/persona.model';

/**
 * Servicio que consulta y modifica las personas y sus roles (propietario, arrendatario,
 * residente y visitante). Todas las URL son relativas: el proxy de desarrollo las reenvía al backend.
 */
@Injectable({
  providedIn: 'root',
})
export class PersonaService {
  /** Cliente HTTP de Angular para llamar al backend. */
  private cliente: HttpClient = inject(HttpClient);

  /** URL base del recurso (/api/personas). */
  private readonly urlbase = environment.apiUrl;

  /**
   * Obtiene todas las personas.
   * @returns Observable con la lista completa.
   */
  getTodos(): Observable<PersonaModel[]> {
    return this.cliente.get<PersonaModel[]>(this.urlbase + '/personas');
  }

  /**
   * Obtiene las personas con rol de propietario.
   * @returns Observable con la lista.
   */
  getPropietarios(): Observable<PersonaModel[]> {
    return this.cliente.get<PersonaModel[]>(this.urlbase + '/propietarios');
  }

  /**
   * Obtiene las personas con rol de arrendatario.
   * @returns Observable con la lista.
   */
  getArrendatarios(): Observable<PersonaModel[]> {
    return this.cliente.get<PersonaModel[]>(this.urlbase + '/arrendatarios');
  }

  /**
   * Obtiene las personas con rol de residente.
   * @returns Observable con la lista.
   */
  getResidentes(): Observable<PersonaModel[]> {
    return this.cliente.get<PersonaModel[]>(this.urlbase + '/residentes');
  }

  /**
   * Obtiene las personas con rol de visitante.
   * @returns Observable con la lista.
   */
  getVisitantes(): Observable<PersonaModel[]> {
    return this.cliente.get<PersonaModel[]>(this.urlbase + '/visitantes');
  }

  /**
   * Obtiene una persona por su documento.
   * @param tipoDocumento Tipo de documento.
   * @param numeroDocumento Número de documento.
   * @returns Observable con la persona.
   */
  getPorDocumento(tipoDocumento: string, numeroDocumento: string): Observable<PersonaModel> {
    return this.cliente.get<PersonaModel>(
      `${this.urlbase}/personas/${tipoDocumento}/${numeroDocumento}`
    );
  }

  /**
   * Crea una persona.
   * @param persona Datos de la persona, incluido su documento.
   * @returns Observable con el mensaje del backend.
   */
  crear(persona: PersonaModel): Observable<{ mensaje: string }> {
    return this.cliente.post<{ mensaje: string }>(this.urlbase + '/personas', persona);
  }

  /**
   * Actualiza una persona; el documento va en la URL.
   * @param persona Persona con los datos nuevos.
   * @returns Observable con el mensaje del backend.
   */
  actualizar(persona: PersonaModel): Observable<{ mensaje: string }> {
    return this.cliente.put<{ mensaje: string }>(
      `${this.urlbase}/personas/${persona.tipoDocumento}/${persona.numeroDocumento}`,
      persona
    );
  }

  /**
   * Elimina una persona.
   * @param tipoDocumento Tipo de documento.
   * @param numeroDocumento Número de documento.
   * @returns Observable con el mensaje del backend.
   */
  eliminar(tipoDocumento: string, numeroDocumento: string): Observable<{ mensaje: string }> {
    return this.cliente.delete<{ mensaje: string }>(
      `${this.urlbase}/personas/${tipoDocumento}/${numeroDocumento}`
    );
  }

  /**
   * Asigna un rol a una persona.
   * @param tipoDocumento Tipo de documento.
   * @param numeroDocumento Número de documento.
   * @param rol Ruta del rol (propietarios, arrendatarios, residentes o visitantes).
   * @returns Observable con el mensaje del backend.
   */
  asignarRol(tipoDocumento: string, numeroDocumento: string, rol: string): Observable<{ mensaje: string }> {
    return this.cliente.post<{ mensaje: string }>(
      `${this.urlbase}/personas/${tipoDocumento}/${numeroDocumento}/roles/${rol}`,
      null
    );
  }

  /**
   * Quita un rol a una persona.
   * @param tipoDocumento Tipo de documento.
   * @param numeroDocumento Número de documento.
   * @param rol Ruta del rol (propietarios, arrendatarios, residentes o visitantes).
   * @returns Observable con el mensaje del backend.
   */
  quitarRol(tipoDocumento: string, numeroDocumento: string, rol: string): Observable<{ mensaje: string }> {
    return this.cliente.delete<{ mensaje: string }>(
      `${this.urlbase}/personas/${tipoDocumento}/${numeroDocumento}/roles/${rol}`
    );
  }
}

