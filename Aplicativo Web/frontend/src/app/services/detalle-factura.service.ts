import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { DetalleFacturaModel, NuevaLineaFactura } from '../models/detalle-factura.model';
import { RespuestaApiModel } from '../models/respuesta-api.model';

@Injectable({
  providedIn: 'root',
})
export class DetalleFacturaService {
  private cliente: HttpClient = inject(HttpClient);

  private readonly urlbase = `${environment.apiUrl}/detalles-factura`;

  getTodos(): Observable<DetalleFacturaModel[]> {
    return this.cliente.get<DetalleFacturaModel[]>(this.urlbase);
  }

  crear(
    linea: NuevaLineaFactura,
  ): Observable<{ mensaje: string; numeroFactura: number; numeroLinea: number }> {
    return this.cliente.post<{ mensaje: string; numeroFactura: number; numeroLinea: number }>(
      this.urlbase,
      linea,
    );
  }

  /** Solo líneas libres (sin sanción, reserva ni asignación). */
  actualizarValor(factura: number, linea: number, valor: number): Observable<RespuestaApiModel> {
    return this.cliente.put<RespuestaApiModel>(`${this.urlbase}/${factura}/${linea}`, { valor });
  }

  eliminar(factura: number, linea: number): Observable<RespuestaApiModel> {
    return this.cliente.delete<RespuestaApiModel>(`${this.urlbase}/${factura}/${linea}`);
  }
}
