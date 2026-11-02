import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { ConceptoCobroModel } from '../models/concepto-cobro.model';

@Injectable({
  providedIn: 'root',
})
export class ConceptoCobroService {
  private cliente: HttpClient = inject(HttpClient);

  private readonly urlbase = `${environment.apiUrl}/conceptos`;

  getTodos(): Observable<ConceptoCobroModel[]> {
    return this.cliente.get<ConceptoCobroModel[]>(this.urlbase);
  }
}
