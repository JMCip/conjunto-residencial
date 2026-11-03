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
}
