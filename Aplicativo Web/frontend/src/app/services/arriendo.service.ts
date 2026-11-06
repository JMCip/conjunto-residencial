import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { ArriendoModel } from '../models/arriendo.model';

/**
 * Servicio que consulta y modifica los contratos de arriendo a través de /api/arriendos.
 * Todas las URL son relativas: el proxy de desarrollo las reenvía al backend.
 */
@Injectable({
  providedIn: 'root',
})
export class ArriendoService {
  /** Cliente HTTP de Angular para llamar al backend. */
  private cliente: HttpClient = inject(HttpClient);

  /** URL base del recurso (/api/arriendos). */
  private readonly urlbase = `${environment.apiUrl}/arriendos`;

  /**
   * Obtiene todos los contratos de arriendo.
   * @returns Observable con la lista completa.
   */
  getTodos(): Observable<ArriendoModel[]> {
    return this.cliente.get<ArriendoModel[]>(this.urlbase);
  }

  /**
   * Obtiene un contrato de arriendo por su identificador.
   * @param id Valor de idContrato.
   * @returns Observable con el registro.
   */
  getPorId(id: number): Observable<ArriendoModel> {
    return this.cliente.get<ArriendoModel>(`${this.urlbase}/${id}`);
  }

  /**
   * Crea un contrato de arriendo; el identificador lo genera la base de datos.
   * @param arriendo Datos del registro sin idContrato.
   * @returns Observable con el mensaje del backend y el idContrato creado.
   */
  crear(arriendo: Omit<ArriendoModel, 'idContrato'>): Observable<{ mensaje: string; idContrato: number }> {
    return this.cliente.post<{ mensaje: string; idContrato: number }>(this.urlbase, arriendo);
  }

  /**
   * Actualiza un contrato de arriendo; el identificador va en la URL.
   * @param arriendo Registro con los datos nuevos.
   * @returns Observable con el mensaje del backend.
   */
  actualizar(arriendo: ArriendoModel): Observable<{ mensaje: string }> {
    return this.cliente.put<{ mensaje: string }>(`${this.urlbase}/${arriendo.idContrato}`, arriendo);
  }

  /**
   * Elimina un contrato de arriendo.
   * @param id Valor de idContrato.
   * @returns Observable con el mensaje del backend.
   */
  eliminar(id: number): Observable<{ mensaje: string }> {
    return this.cliente.delete<{ mensaje: string }>(`${this.urlbase}/${id}`);
  }
}

