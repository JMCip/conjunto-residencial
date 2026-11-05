import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { PosesionModel } from '../models/posesion.model';

@Injectable({
  providedIn: 'root',
})
export class PosesionService {
  private cliente: HttpClient = inject(HttpClient);

  private readonly urlbase = `${environment.apiUrl}/posesiones`;

  getTodos(): Observable<PosesionModel[]> {
    return this.cliente.get<PosesionModel[]>(this.urlbase);
  }

  getPorId(id: number): Observable<PosesionModel> {
    return this.cliente.get<PosesionModel>(`${this.urlbase}/${id}`);
  }

  crear(posesion: Omit<PosesionModel, 'idPosesion'>): Observable<{ mensaje: string; idPosesion: number }> {
    return this.cliente.post<{ mensaje: string; idPosesion: number }>(this.urlbase, posesion);
  }

  actualizar(posesion: PosesionModel): Observable<{ mensaje: string }> {
    return this.cliente.put<{ mensaje: string }>(`${this.urlbase}/${posesion.idPosesion}`, posesion);
  }

  eliminar(id: number): Observable<{ mensaje: string }> {
    return this.cliente.delete<{ mensaje: string }>(`${this.urlbase}/${id}`);
  }
}
