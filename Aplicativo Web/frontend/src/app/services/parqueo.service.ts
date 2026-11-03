import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { ParqueoModel } from '../models/parqueo.model';

@Injectable({
  providedIn: 'root',
})
export class ParqueoService {
  private cliente: HttpClient = inject(HttpClient);

  private readonly urlbase = `${environment.apiUrl}/parqueos`;

  getTodos(): Observable<ParqueoModel[]> {
    return this.cliente.get<ParqueoModel[]>(this.urlbase);
  }
}
