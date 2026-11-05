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


  getPorLlave(tipoDocumento: string, numeroDocumento: string, telefono: string): Observable<TelefonoModel> {
    return this.cliente.get<TelefonoModel>(
      `${this.urlbase}/${tipoDocumento}/${numeroDocumento}/${telefono}`
    );
  }


  crear(telefono: TelefonoModel): Observable<{ mensaje: string }> {
    return this.cliente.post<{ mensaje: string }>(this.urlbase, telefono);
  }


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


  eliminar(tipoDocumento: string, numeroDocumento: string, telefono: string): Observable<{ mensaje: string }> {
    return this.cliente.delete<{ mensaje: string }>(
      `${this.urlbase}/${tipoDocumento}/${numeroDocumento}/${telefono}`
    );
  }

}
