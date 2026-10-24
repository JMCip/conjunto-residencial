import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class Api {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = environment.apiUrl;

  get<T>(ruta: string): Observable<T> {
    return this.http.get<T>(`${this.baseUrl}${ruta}`);
  }

  getTexto(ruta: string): Observable<string> {
    return this.http.get(`${this.baseUrl}${ruta}`, { responseType: 'text' });
  }
}
