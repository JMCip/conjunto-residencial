import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { SancionModel } from '../models/sancion.model';

@Injectable({
  providedIn: 'root',
})
export class SancionService {
  private cliente: HttpClient = inject(HttpClient);

  private readonly urlbase = `${environment.apiUrl}/sanciones`;

  getTodos(): Observable<SancionModel[]> {
    return this.cliente.get<SancionModel[]>(this.urlbase);
  }
}
