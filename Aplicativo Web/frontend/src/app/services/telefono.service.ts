import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { TelefonoModel } from '../models/telefono.model';

/**
 * Servicio que consulta y modifica los teléfonos de las personas a través de /api/telefonos.
 * Todas las URL son relativas: el proxy de desarrollo las reenvía al backend.
 */
@Injectable({
  providedIn: 'root',
})
export class TelefonoService {
  /** Cliente HTTP de Angular para llamar al backend. */
  private cliente: HttpClient = inject(HttpClient);

  /** URL base del recurso (/api/telefonos). */
  private readonly urlbase = `${environment.apiUrl}/telefonos`;

  /**
   * Obtiene todos los teléfonos.
   * @returns Observable con la lista completa.
   */
  getTodos(): Observable<TelefonoModel[]> {
    return this.cliente.get<TelefonoModel[]>(this.urlbase);
  }

  /**
   * Obtiene un teléfono por su llave.
   * @param tipoDocumento Tipo de documento del dueño.
   * @param numeroDocumento Número de documento del dueño.
   * @param telefono Número de teléfono.
   * @returns Observable con el registro.
   */
  getPorLlave(tipoDocumento: string, numeroDocumento: string, telefono: string): Observable<TelefonoModel> {
    return this.cliente.get<TelefonoModel>(
      `${this.urlbase}/${tipoDocumento}/${numeroDocumento}/${telefono}`
    );
  }

  /**
   * Registra un teléfono para una persona.
   * @param telefono Número y dueño del teléfono.
   * @returns Observable con el mensaje del backend.
   */
  crear(telefono: TelefonoModel): Observable<{ mensaje: string }> {
    return this.cliente.post<{ mensaje: string }>(this.urlbase, telefono);
  }

  /**
   * Cambia el número de un teléfono. La URL lleva el número actual y el cuerpo el nuevo.
   * @param tipoDocumento Tipo de documento del dueño.
   * @param numeroDocumento Número de documento del dueño.
   * @param telefonoActual Número que se reemplaza.
   * @param telefonoNuevo Número nuevo.
   * @returns Observable con el mensaje del backend.
   */
  actualizar(
    tipoDocumento: string,
    numeroDocumento: string,
    telefonoActual: string,
    telefonoNuevo: string
  ): Observable<{ mensaje: string }> {
    return this.cliente.put<{ mensaje: string }>(
      `${this.urlbase}/${tipoDocumento}/${numeroDocumento}/${telefonoActual}`,
      { telefono: telefonoNuevo }
    );
  }

  /**
   * Elimina un teléfono.
   * @param tipoDocumento Tipo de documento del dueño.
   * @param numeroDocumento Número de documento del dueño.
   * @param telefono Número de teléfono.
   * @returns Observable con el mensaje del backend.
   */
  eliminar(tipoDocumento: string, numeroDocumento: string, telefono: string): Observable<{ mensaje: string }> {
    return this.cliente.delete<{ mensaje: string }>(
      `${this.urlbase}/${tipoDocumento}/${numeroDocumento}/${telefono}`
    );
  }

}
