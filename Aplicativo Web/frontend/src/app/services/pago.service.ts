import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { NuevoPago, PagoModel, RespuestaPagoModel } from '../models/pago.model';

/**
 * Servicio que consulta y registra los pagos de las facturas a través de /api/pagos.
 * Los pagos no se editan ni se eliminan: el backend solo tiene GET y POST.
 */
@Injectable({
  providedIn: 'root',
})
export class PagoService {
  /** Cliente HTTP de Angular para llamar al backend. */
  private cliente: HttpClient = inject(HttpClient);

  /** URL base del recurso (/api/pagos). */
  private readonly urlbase = `${environment.apiUrl}/pagos`;

  /**
   * Obtiene todos los pagos.
   * @returns Observable con la lista completa.
   */
  getTodos(): Observable<PagoModel[]> {
    return this.cliente.get<PagoModel[]>(this.urlbase);
  }

  /**
   * Registra un pago. El backend valida el saldo y las fechas, y cambia el estado de la factura (ABONADA o PAGADA).
   * @param pago Datos del pago sin idPago.
   * @returns Observable con el mensaje, el id del pago, el estado de la factura y su saldo.
   */
  registrar(pago: NuevoPago): Observable<RespuestaPagoModel> {
    return this.cliente.post<RespuestaPagoModel>(this.urlbase, pago);
  }
}
