import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { CambiosSancion, NuevaSancion, SancionModel } from '../models/sancion.model';

@Injectable({
  providedIn: 'root',
})
export class SancionService {
  private cliente: HttpClient = inject(HttpClient);

  private readonly urlbase = `${environment.apiUrl}/sanciones`;

  getTodos(): Observable<SancionModel[]> {
    return this.cliente.get<SancionModel[]>(this.urlbase);
  }

  /** El back responde { mensaje, idSancion } y la deja EN_DESCARGOS. */
  crear(sancion: NuevaSancion): Observable<{ mensaje: string; idSancion: number }> {
    return this.cliente.post<{ mensaje: string; idSancion: number }>(this.urlbase, sancion);
  }

  /** Solo para sanciones EN_DESCARGOS: registrar descargos, confirmar (con multa opcional) o anular. */
  actualizar(id: number, cambios: CambiosSancion): Observable<{ mensaje: string }> {
    return this.cliente.put<{ mensaje: string }>(`${this.urlbase}/${id}`, cambios);
  }

  /** Solo sanciones ANULADAS. */
  eliminar(id: number): Observable<{ mensaje: string }> {
    return this.cliente.delete<{ mensaje: string }>(`${this.urlbase}/${id}`);
  }
}
