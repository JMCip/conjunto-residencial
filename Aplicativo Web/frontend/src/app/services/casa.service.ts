import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { CasaModel } from '../models/casa.model';

@Injectable({
  providedIn: 'root',
})
export class CasaService {
  private cliente: HttpClient = inject(HttpClient);

  private readonly urlbase = `${environment.apiUrl}/casas`;

  getTodos(): Observable<CasaModel[]> {
    return this.cliente.get<CasaModel[]>(this.urlbase);
  }
}
