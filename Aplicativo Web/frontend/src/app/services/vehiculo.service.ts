import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { VehiculoModel } from '../models/vehiculo.model';

@Injectable({
  providedIn: 'root',
})
export class VehiculoService {
  private cliente: HttpClient = inject(HttpClient);

  private readonly urlbase = `${environment.apiUrl}/vehiculos`;

  getTodos(): Observable<VehiculoModel[]> {
    return this.cliente.get<VehiculoModel[]>(this.urlbase);
  }


  getPorPlaca(placa: string): Observable<VehiculoModel> {
    return this.cliente.get<VehiculoModel>(`${this.urlbase}/${placa}`);
  }


  crear(vehiculo: VehiculoModel): Observable<{ mensaje: string }> {
    return this.cliente.post<{ mensaje: string }>(this.urlbase, vehiculo);
  }


  actualizar(vehiculo: VehiculoModel): Observable<{ mensaje: string }> {
    return this.cliente.put<{ mensaje: string }>(`${this.urlbase}/${vehiculo.placa}`, vehiculo);
  }


  eliminar(placa: string): Observable<{ mensaje: string }> {
    return this.cliente.delete<{ mensaje: string }>(`${this.urlbase}/${placa}`);
  }

}
