import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { PersonaModel } from '../models/persona.model';

@Injectable({
  providedIn: 'root',
})
export class PersonaService {
  private cliente: HttpClient = inject(HttpClient);


  private readonly urlbase = environment.apiUrl;


  getTodos(): Observable<PersonaModel[]> {
    return this.cliente.get<PersonaModel[]>(this.urlbase + '/personas');
  }


  getPropietarios(): Observable<PersonaModel[]> {
    return this.cliente.get<PersonaModel[]>(this.urlbase + '/propietarios');
  }


  getArrendatarios(): Observable<PersonaModel[]> {
    return this.cliente.get<PersonaModel[]>(this.urlbase + '/arrendatarios');
  }


  getResidentes(): Observable<PersonaModel[]> {
    return this.cliente.get<PersonaModel[]>(this.urlbase + '/residentes');
  }


  getVisitantes(): Observable<PersonaModel[]> {
    return this.cliente.get<PersonaModel[]>(this.urlbase + '/visitantes');
  }


  getPorDocumento(tipoDocumento: string, numeroDocumento: string): Observable<PersonaModel> {
    return this.cliente.get<PersonaModel>(
      `${this.urlbase}/personas/${tipoDocumento}/${numeroDocumento}`
    );
  }


  crear(persona: PersonaModel): Observable<{ mensaje: string }> {
    return this.cliente.post<{ mensaje: string }>(this.urlbase + '/personas', persona);
  }


  actualizar(persona: PersonaModel): Observable<{ mensaje: string }> {
    return this.cliente.put<{ mensaje: string }>(
      `${this.urlbase}/personas/${persona.tipoDocumento}/${persona.numeroDocumento}`,
      persona
    );
  }


  eliminar(tipoDocumento: string, numeroDocumento: string): Observable<{ mensaje: string }> {
    return this.cliente.delete<{ mensaje: string }>(
      `${this.urlbase}/personas/${tipoDocumento}/${numeroDocumento}`
    );
  }

  asignarRol(tipoDocumento: string, numeroDocumento: string, rol: string): Observable<{ mensaje: string }> {
    return this.cliente.post<{ mensaje: string }>(
      `${this.urlbase}/personas/${tipoDocumento}/${numeroDocumento}/roles/${rol}`,
      null
    );
  }

  quitarRol(tipoDocumento: string, numeroDocumento: string, rol: string): Observable<{ mensaje: string }> {
    return this.cliente.delete<{ mensaje: string }>(
      `${this.urlbase}/personas/${tipoDocumento}/${numeroDocumento}/roles/${rol}`
    );
  }
}


