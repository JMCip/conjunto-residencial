import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { TelefonoModel } from '../models/telefono.model';

@Injectable({
  providedIn: 'root',
})
export class TelefonoService {
  private cliente: HttpClient = inject(HttpClient);

  private readonly urlbase = `${environment.apiUrl}/telefonos`;

  getTodos(): Observable<TelefonoModel[]> {
    return this.cliente.get<TelefonoModel[]>(this.urlbase);
  }
}
