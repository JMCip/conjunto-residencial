import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { FacturaModel, NuevaFactura } from '../models/factura.model';
import { RespuestaApiModel } from '../models/respuesta-api.model';

/**
 * Servicio que consulta y modifica las facturas a través de /api/facturas.
 * Todas las URL son relativas: el proxy de desarrollo las reenvía al backend.
 */
@Injectable({
  providedIn: 'root',
})
export class FacturaService {
  /** Cliente HTTP de Angular para llamar al backend. */
  private cliente: HttpClient = inject(HttpClient);

  /** URL base del recurso (/api/facturas). */
  private readonly urlbase = `${environment.apiUrl}/facturas`;

  /**
   * Obtiene todas las facturas con su total, lo pagado y el saldo.
   * @returns Observable con la lista completa.
   */
  getTodos(): Observable<FacturaModel[]> {
    return this.cliente.get<FacturaModel[]>(this.urlbase);
  }

  /**
   * Crea una factura; queda PENDIENTE y sin líneas.
   * @param f Periodo, fechas y casa de la factura.
   * @returns Observable con el mensaje y el número de la factura creada.
   */
  crear(f: NuevaFactura): Observable<{ mensaje: string; numeroFactura: number }> {
    return this.cliente.post<{ mensaje: string; numeroFactura: number }>(this.urlbase, f);
  }

  /**
   * Cambia la fecha de vencimiento de una factura PENDIENTE o ABONADA.
   * @param id Número de la factura.
   * @param fechaVencimiento Fecha nueva (yyyy-MM-dd).
   * @returns Observable con el mensaje del backend.
   */
  actualizarVencimiento(id: number, fechaVencimiento: string): Observable<RespuestaApiModel> {
    return this.cliente.put<RespuestaApiModel>(`${this.urlbase}/${id}`, { fechaVencimiento });
  }

  /**
   * Elimina una factura PENDIENTE que no tenga líneas ni pagos.
   * @param id Número de la factura.
   * @returns Observable con el mensaje del backend.
   */
  eliminar(id: number): Observable<RespuestaApiModel> {
    return this.cliente.delete<RespuestaApiModel>(`${this.urlbase}/${id}`);
  }

  /**
   * Pasa a VENCIDA las facturas PENDIENTE o ABONADA cuya fecha de vencimiento ya pasó.
   * @returns Observable con el mensaje y la cantidad de facturas actualizadas.
   */
  marcarVencidas(): Observable<{ mensaje: string; cantidad: number }> {
    return this.cliente.post<{ mensaje: string; cantidad: number }>(
      `${this.urlbase}/marcar-vencidas`,
      {},
    );
  }
}
