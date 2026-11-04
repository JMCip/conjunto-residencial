import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { ResidenciaModel } from '../models/residencia.model';

@Injectable({
  providedIn: 'root',
})
export class ResidenciaService {
  private cliente: HttpClient = inject(HttpClient);

  private readonly urlbase = `${environment.apiUrl}/residencias`;

  getTodos(): Observable<ResidenciaModel[]> {
    return this.cliente.get<ResidenciaModel[]>(this.urlbase);
  }

  getPorId(id: number): Observable<ResidenciaModel> {
    return this.cliente.get<ResidenciaModel>(`${this.urlbase}/${id}`);
  }

  crear(residencia: Omit<ResidenciaModel, 'idResidencia'>): Observable<{ mensaje: string; idResidencia: number }> {
    return this.cliente.post<{ mensaje: string; idResidencia: number }>(this.urlbase, residencia);
  }

  actualizar(residencia: ResidenciaModel): Observable<{ mensaje: string }> {
    return this.cliente.put<{ mensaje: string }>(`${this.urlbase}/${residencia.idResidencia}`, residencia);
  }

  eliminar(id: number): Observable<{ mensaje: string }> {
    return this.cliente.delete<{ mensaje: string }>(`${this.urlbase}/${id}`);
  }
}


