import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { FacturaModel } from '../models/factura.model';

@Injectable({
  providedIn: 'root',
})
export class FacturaService {
  private cliente: HttpClient = inject(HttpClient);

  private readonly urlbase = `${environment.apiUrl}/facturas`;

  getTodos(): Observable<FacturaModel[]> {
    return this.cliente.get<FacturaModel[]>(this.urlbase);
  }
}
