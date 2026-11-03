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
}
