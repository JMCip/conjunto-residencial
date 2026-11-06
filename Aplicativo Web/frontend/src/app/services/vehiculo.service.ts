import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { VehiculoModel } from '../models/vehiculo.model';

/**
 * Servicio que consulta y modifica los vehículos a través de /api/vehiculos.
 * Todas las URL son relativas: el proxy de desarrollo las reenvía al backend.
 */
@Injectable({
  providedIn: 'root',
})
export class VehiculoService {
  /** Cliente HTTP de Angular para llamar al backend. */
  private cliente: HttpClient = inject(HttpClient);

  /** URL base del recurso (/api/vehiculos). */
  private readonly urlbase = `${environment.apiUrl}/vehiculos`;

  /**
   * Obtiene todos los vehículos.
   * @returns Observable con la lista completa.
   */
  getTodos(): Observable<VehiculoModel[]> {
    return this.cliente.get<VehiculoModel[]>(this.urlbase);
  }

  /**
   * Obtiene un vehículo por su placa.
   * @param placa Placa del vehículo.
   * @returns Observable con el registro.
   */
  getPorPlaca(placa: string): Observable<VehiculoModel> {
    return this.cliente.get<VehiculoModel>(`${this.urlbase}/${placa}`);
  }

  /**
   * Registra un vehículo.
   * @param vehiculo Datos del vehículo, incluida su placa.
   * @returns Observable con el mensaje del backend.
   */
  crear(vehiculo: VehiculoModel): Observable<{ mensaje: string }> {
    return this.cliente.post<{ mensaje: string }>(this.urlbase, vehiculo);
  }

  /**
   * Actualiza un vehículo; la placa va en la URL y no se puede cambiar.
   * @param vehiculo Vehículo con los datos nuevos.
   * @returns Observable con el mensaje del backend.
   */
  actualizar(vehiculo: VehiculoModel): Observable<{ mensaje: string }> {
    return this.cliente.put<{ mensaje: string }>(`${this.urlbase}/${vehiculo.placa}`, vehiculo);
  }

  /**
   * Elimina un vehículo.
   * @param placa Placa del vehículo.
   * @returns Observable con el mensaje del backend.
   */
  eliminar(placa: string): Observable<{ mensaje: string }> {
    return this.cliente.delete<{ mensaje: string }>(`${this.urlbase}/${placa}`);
  }

}
