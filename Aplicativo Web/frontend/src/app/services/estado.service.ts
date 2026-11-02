import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { InfoBdModel } from '../models/info-bd.model';

const RUTA_ESTADO = '/estado';
const RUTA_BD = '/bd/prueba';


@Injectable({ providedIn: 'root' })
export class EstadoService {
  private cliente: HttpClient = inject(HttpClient);

  getEstado(): Observable<string> {
    return this.cliente.get(`${environment.apiUrl}${RUTA_ESTADO}`, { responseType: 'text' });
  }

  getInfoBd(): Observable<InfoBdModel> {
    return this.cliente.get<InfoBdModel>(`${environment.apiUrl}${RUTA_BD}`);
  }
}
