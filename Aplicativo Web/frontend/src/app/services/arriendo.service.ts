import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { ArriendoModel } from '../models/arriendo.model';

@Injectable({
  providedIn: 'root',
})
export class ArriendoService {
  private cliente: HttpClient = inject(HttpClient);

  private readonly urlbase = `${environment.apiUrl}/arriendos`;

  getTodos(): Observable<ArriendoModel[]> {
    return this.cliente.get<ArriendoModel[]>(this.urlbase);
  }
}
