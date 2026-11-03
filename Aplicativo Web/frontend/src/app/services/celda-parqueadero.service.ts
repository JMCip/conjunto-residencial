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
}
