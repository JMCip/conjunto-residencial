import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { DetalleFacturaModel, NuevaLineaFactura } from '../models/detalle-factura.model';
import { RespuestaApiModel } from '../models/respuesta-api.model';

/**
 * Servicio que consulta y modifica las líneas de las facturas a través de /api/detalles-factura.
 * Todas las URL son relativas: el proxy de desarrollo las reenvía al backend.
 */
@Injectable({
  providedIn: 'root',
})
export class DetalleFacturaService {
  /** Cliente HTTP de Angular para llamar al backend. */
  private cliente: HttpClient = inject(HttpClient);

  /** URL base del recurso (/api/detalles-factura). */
  private readonly urlbase = `${environment.apiUrl}/detalles-factura`;

  /**
   * Obtiene todas las líneas de todas las facturas.
   * @returns Observable con la lista completa.
   */
  getTodos(): Observable<DetalleFacturaModel[]> {
    return this.cliente.get<DetalleFacturaModel[]>(this.urlbase);
  }

  /**
   * Agrega una línea a una factura. El backend calcula el valor cuando la línea viene de una sanción, una reserva o una celda adicional.
   * @param linea Datos de la línea.
   * @returns Observable con el mensaje y la llave de la línea creada.
   */
  crear(
    linea: NuevaLineaFactura,
  ): Observable<{ mensaje: string; numeroFactura: number; numeroLinea: number }> {
    return this.cliente.post<{ mensaje: string; numeroFactura: number; numeroLinea: number }>(
      this.urlbase,
      linea,
    );
  }

  /**
   * Cambia el valor de una línea libre (sin sanción, reserva ni asignación).
   * @param factura Número de la factura.
   * @param linea Número de la línea.
   * @param valor Valor nuevo en pesos.
   * @returns Observable con el mensaje del backend.
   */
  actualizarValor(factura: number, linea: number, valor: number): Observable<RespuestaApiModel> {
    return this.cliente.put<RespuestaApiModel>(`${this.urlbase}/${factura}/${linea}`, { valor });
  }

  /**
   * Elimina una línea de una factura.
   * @param factura Número de la factura.
   * @param linea Número de la línea.
   * @returns Observable con el mensaje del backend.
   */
  eliminar(factura: number, linea: number): Observable<RespuestaApiModel> {
    return this.cliente.delete<RespuestaApiModel>(`${this.urlbase}/${factura}/${linea}`);
  }
}
