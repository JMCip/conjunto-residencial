import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { ArriendoModel } from '../models/arriendo.model';

@Injectable({
  providedIn: 'root',
})
export class ArriendoService {
  private cliente: HttpClient = inject(HttpClient);

  private readonly urlbase = `${environment.apiUrl}/arriendos`;

  getTodos(): Observable<ArriendoModel[]> {
    return this.cliente.get<ArriendoModel[]>(this.urlbase);
  }

  getPorId(id: number): Observable<ArriendoModel> {
    return this.cliente.get<ArriendoModel>(`${this.urlbase}/${id}`);
  }

  crear(arriendo: Omit<ArriendoModel, 'idContrato'>): Observable<{ mensaje: string; idContrato: number }> {
    return this.cliente.post<{ mensaje: string; idContrato: number }>(this.urlbase, arriendo);
  }

  actualizar(arriendo: ArriendoModel): Observable<{ mensaje: string }> {
    return this.cliente.put<{ mensaje: string }>(`${this.urlbase}/${arriendo.idContrato}`, arriendo);
  }

  eliminar(id: number): Observable<{ mensaje: string }> {
    return this.cliente.delete<{ mensaje: string }>(`${this.urlbase}/${id}`);
  }
}


