import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { DetalleFacturaModel } from '../models/detalle-factura.model';

@Injectable({
  providedIn: 'root',
})
export class DetalleFacturaService {
  private cliente: HttpClient = inject(HttpClient);

  private readonly urlbase = `${environment.apiUrl}/detalles-factura`;

  getTodos(): Observable<DetalleFacturaModel[]> {
    return this.cliente.get<DetalleFacturaModel[]>(this.urlbase);
  }
}
