import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { VisitaModel } from '../models/visita.model';

@Injectable({
  providedIn: 'root',
})
export class VisitaService {
  private cliente: HttpClient = inject(HttpClient);

  private readonly urlbase = `${environment.apiUrl}/visitas`;

  getTodos(): Observable<VisitaModel[]> {
    return this.cliente.get<VisitaModel[]>(this.urlbase);
  }

  getPorId(id: number): Observable<VisitaModel> {
    return this.cliente.get<VisitaModel>(`${this.urlbase}/${id}`);
  }

  crear(visita: Omit<VisitaModel, 'idVisita'>): Observable<{ mensaje: string; idVisita: number }> {
    return this.cliente.post<{ mensaje: string; idVisita: number }>(this.urlbase, visita);
  }

  actualizar(visita: VisitaModel): Observable<{ mensaje: string }> {
    return this.cliente.put<{ mensaje: string }>(`${this.urlbase}/${visita.idVisita}`, visita);
  }

  eliminar(id: number): Observable<{ mensaje: string }> {
    return this.cliente.delete<{ mensaje: string }>(`${this.urlbase}/${id}`);
  }
}
