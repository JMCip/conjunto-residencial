import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { FacturaModel, NuevaFactura } from '../models/factura.model';
import { RespuestaApiModel } from '../models/respuesta-api.model';

@Injectable({
  providedIn: 'root',
})
export class FacturaService {
  private cliente: HttpClient = inject(HttpClient);

  private readonly urlbase = `${environment.apiUrl}/facturas`;

  getTodos(): Observable<FacturaModel[]> {
    return this.cliente.get<FacturaModel[]>(this.urlbase);
  }

  /** El back responde { mensaje, numeroFactura }; queda PENDIENTE y sin líneas. */
  crear(f: NuevaFactura): Observable<{ mensaje: string; numeroFactura: number }> {
    return this.cliente.post<{ mensaje: string; numeroFactura: number }>(this.urlbase, f);
  }

  /** Solo PENDIENTE o ABONADA. */
  actualizarVencimiento(id: number, fechaVencimiento: string): Observable<RespuestaApiModel> {
    return this.cliente.put<RespuestaApiModel>(`${this.urlbase}/${id}`, { fechaVencimiento });
  }

  /** Solo PENDIENTE sin líneas ni pagos. */
  eliminar(id: number): Observable<RespuestaApiModel> {
    return this.cliente.delete<RespuestaApiModel>(`${this.urlbase}/${id}`);
  }

  /** Pasa a VENCIDA las PENDIENTE/ABONADA con vencimiento pasado. */
  marcarVencidas(): Observable<{ mensaje: string; cantidad: number }> {
    return this.cliente.post<{ mensaje: string; cantidad: number }>(
      `${this.urlbase}/marcar-vencidas`,
      {},
    );
  }
}
