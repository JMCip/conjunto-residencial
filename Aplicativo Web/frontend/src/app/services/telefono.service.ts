import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { TelefonoModel } from '../models/telefono.model';

@Injectable({
  providedIn: 'root',
})
export class TelefonoService {
  private cliente: HttpClient = inject(HttpClient);

  private readonly urlbase = `${environment.apiUrl}/telefonos`;

  getTodos(): Observable<TelefonoModel[]> {
    return this.cliente.get<TelefonoModel[]>(this.urlbase);
  }

  /**
   * Consulta un teléfono por su llave (documento de la persona y número).
   */
  getPorLlave(tipoDocumento: string, numeroDocumento: string, telefono: string): Observable<TelefonoModel> {
    return this.cliente.get<TelefonoModel>(
      `${this.urlbase}/${tipoDocumento}/${numeroDocumento}/${telefono}`
    );
  }

  /**
   * Crea un teléfono para una persona existente.
   * @param telefono Debe tener entre 7 y 15 dígitos.
   */
  crear(telefono: TelefonoModel): Observable<{ mensaje: string }> {
    return this.cliente.post<{ mensaje: string }>(this.urlbase, telefono);
  }

  /**
   * Cambia el número de un teléfono. La persona y el número actual viajan en la URL;
   * en el body va solo el número nuevo.
   * @param telefonoActual Número que se quiere cambiar.
   * @param telefonoNuevo Número nuevo (7 a 15 dígitos).
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
   * Elimina un teléfono de una persona.
   */
  eliminar(tipoDocumento: string, numeroDocumento: string, telefono: string): Observable<{ mensaje: string }> {
    return this.cliente.delete<{ mensaje: string }>(
      `${this.urlbase}/${tipoDocumento}/${numeroDocumento}/${telefono}`
    );
  }

}
