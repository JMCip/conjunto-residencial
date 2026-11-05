import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { CeldaParqueaderoModel } from '../models/celda-parqueadero.model';

@Injectable({
  providedIn: 'root',
})
export class CeldaParqueaderoService {
  private cliente: HttpClient = inject(HttpClient);

  private readonly urlbase = `${environment.apiUrl}/celdas`;

  getTodos(): Observable<CeldaParqueaderoModel[]> {
    return this.cliente.get<CeldaParqueaderoModel[]>(this.urlbase);
  }

  getPorId(id: number): Observable<CeldaParqueaderoModel> {
    return this.cliente.get<CeldaParqueaderoModel>(`${this.urlbase}/${id}`);
  }

  crear(celda: Omit<CeldaParqueaderoModel, 'idCelda'>): Observable<{ mensaje: string; idCelda: number }> {
    return this.cliente.post<{ mensaje: string; idCelda: number }>(this.urlbase, celda);
  }

  actualizar(celda: CeldaParqueaderoModel): Observable<{ mensaje: string }> {
    return this.cliente.put<{ mensaje: string }>(`${this.urlbase}/${celda.idCelda}`, celda);
  }

  eliminar(id: number): Observable<{ mensaje: string }> {
    return this.cliente.delete<{ mensaje: string }>(`${this.urlbase}/${id}`);
  }

}
