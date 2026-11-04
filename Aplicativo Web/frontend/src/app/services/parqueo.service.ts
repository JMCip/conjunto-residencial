import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { ParqueoModel } from '../models/parqueo.model';

@Injectable({
  providedIn: 'root',
})
export class ParqueoService {
  private cliente: HttpClient = inject(HttpClient);

  private readonly urlbase = `${environment.apiUrl}/parqueos`;

  getTodos(): Observable<ParqueoModel[]> {
    return this.cliente.get<ParqueoModel[]>(this.urlbase);
  }

  getPorId(id: number): Observable<ParqueoModel> {
    return this.cliente.get<ParqueoModel>(`${this.urlbase}/${id}`);
  }

  crear(parqueo: Omit<ParqueoModel, 'idParqueo'>): Observable<{ mensaje: string; idParqueo: number }> {
    return this.cliente.post<{ mensaje: string; idParqueo: number }>(this.urlbase, parqueo);
  }

  actualizar(parqueo: ParqueoModel): Observable<{ mensaje: string }> {
    return this.cliente.put<{ mensaje: string }>(`${this.urlbase}/${parqueo.idParqueo}`, parqueo);
  }

  eliminar(id: number): Observable<{ mensaje: string }> {
    return this.cliente.delete<{ mensaje: string }>(`${this.urlbase}/${id}`);
  }
}
