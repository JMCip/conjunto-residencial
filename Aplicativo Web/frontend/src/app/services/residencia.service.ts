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
}
