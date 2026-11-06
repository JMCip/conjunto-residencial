import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { InfoBdModel } from '../models/info-bd.model';

/** Ruta que responde si el backend está en funcionamiento. */
const RUTA_ESTADO = '/estado';

/** Ruta que responde con los datos de la conexión a PostgreSQL. */
const RUTA_BD = '/bd/prueba';

/** Servicio que revisa la conexión con el backend y con la base de datos. */
@Injectable({ providedIn: 'root' })
export class EstadoService {
  /** Cliente HTTP de Angular para llamar al backend. */
  private cliente: HttpClient = inject(HttpClient);

  /**
   * Consulta si el backend está en funcionamiento.
   * @returns Observable con el texto plano que responde el backend.
   */
  getEstado(): Observable<string> {
    return this.cliente.get(`${environment.apiUrl}${RUTA_ESTADO}`, { responseType: 'text' });
  }

  /**
   * Consulta los datos de la conexión del backend con PostgreSQL.
   * @returns Observable con el usuario, la base y la versión.
   */
  getInfoBd(): Observable<InfoBdModel> {
    return this.cliente.get<InfoBdModel>(`${environment.apiUrl}${RUTA_BD}`);
  }
}
