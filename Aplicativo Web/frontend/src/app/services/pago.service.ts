import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { PagoModel } from '../models/pago.model';


@Injectable({
  providedIn: 'root',
})
export class PagoService {
  private cliente: HttpClient = inject(HttpClient);


  private readonly urlbase = `${environment.apiUrl}/pagos`;

  getTodos(): Observable<PagoModel[]> {
    return this.cliente.get<PagoModel[]>(this.urlbase);
  }
}
