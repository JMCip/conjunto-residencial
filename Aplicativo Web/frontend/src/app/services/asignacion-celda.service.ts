import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { AsignacionCeldaModel } from '../models/asignacion-celda.model';

@Injectable({
  providedIn: 'root',
})
export class AsignacionCeldaService {
  private cliente: HttpClient = inject(HttpClient);

  private readonly urlbase = `${environment.apiUrl}/asignaciones-celda`;

  getTodos(): Observable<AsignacionCeldaModel[]> {
    return this.cliente.get<AsignacionCeldaModel[]>(this.urlbase);
  }
}
