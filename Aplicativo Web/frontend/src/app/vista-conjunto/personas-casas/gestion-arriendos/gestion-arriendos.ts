import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { CurrencyPipe, DatePipe } from '@angular/common';
import { forkJoin } from 'rxjs';
import { ArriendoModel } from '../../../models/arriendo.model';
import { CasaModel, nombreCasa } from '../../../models/casa.model';
import {
  llavePersona,
  nombrePersona,
  PersonaModel,
} from '../../../models/persona.model';
import { ArriendoService } from '../../../services/arriendo.service';
import { CasaService } from '../../../services/casa.service';
import { PersonaService } from '../../../services/persona.service';
import { NotificacionService } from '../../../services/notificacion.service';
import { extraerError } from '../../../compartido/extraer-error';

/** Estados del contrato*/
const ESTADOS_CONTRATO: ArriendoModel['estadoContrato'][] = ['VIGENTE', 'FINALIZADO'];

/**
 * @component GestionArriendos
 * @description Pestaña Arriendos: contratos entre un propietario y un arrendatario sobre una
 * casa. La base solo permite un contrato VIGENTE por casa (índice único).
 */
@Component({
  selector: 'app-gestion-arriendos',
  imports: [CurrencyPipe, DatePipe],
  templateUrl: './gestion-arriendos.html',
  styleUrl: './gestion-arriendos.css',
})
export class GestionArriendos implements OnInit {
  private arriendoService = inject(ArriendoService);
  private casaService = inject(CasaService);
  private personaService = inject(PersonaService);
  private notificacionService = inject(NotificacionService);

  /** Contratos obtenidos del backend. */
  protected readonly arriendos = signal<ArriendoModel[]>([]);

  /** Casas, propietarios y arrendatarios para los selectores. */
  protected readonly casas = signal<CasaModel[]>([]);
  protected readonly propietarios = signal<PersonaModel[]>([]);
  protected readonly arrendatarios = signal<PersonaModel[]>([]);

  /** Nombre de cada persona (propietarios y arrendatarios) por su llave "CC|123". */
  private readonly nombres = computed(
    () =>
      new Map(
        [...this.propietarios(), ...this.arrendatarios()].map((p) => [
          llavePersona(p.tipoDocumento, p.numeroDocumento),
          nombrePersona(p),
        ]),
      ),
  );

  protected readonly cargando = signal(true);
  protected readonly errorCarga = signal(false);
  protected readonly busqueda = signal('');

  /** Contratos visibles según la búsqueda (casa, personas o estado). */
  protected readonly arriendosFiltrados = computed(() => {
    const texto = this.busqueda().trim().toLowerCase();
    if (!texto) return this.arriendos();
    return this.arriendos().filter((a) =>
      [
        nombreCasa(a.casaManzana, a.casaNumero),
        this.persona(a.propietarioTipoDocumento, a.propietarioNumeroDocumento),
        this.persona(a.arrendatarioTipoDocumento, a.arrendatarioNumeroDocumento),
        a.estadoContrato,
      ]
        .join(' ')
        .toLowerCase()
        .includes(texto),
    );
  });

  /** "4 contratos" o "1 contrato de 4". */
  protected readonly textoConteo = computed(() => {
    const visibles = this.arriendosFiltrados().length;
    const total = this.arriendos().length;
    const palabra = visibles === 1 ? 'contrato' : 'contratos';
    return visibles === total ? `${visibles} ${palabra}` : `${visibles} ${palabra} de ${total}`;
  });

  /** Constantes y funciones para la plantilla. */
  protected readonly nombreCasa = nombreCasa;

  ngOnInit(): void {
    this.cargarArriendos();
  }

  /** Consulta contratos, casas, propietarios y arrendatarios a la vez. */
  cargarArriendos(): void {
    this.cargando.set(true);
    this.errorCarga.set(false);
    forkJoin({
      arriendos: this.arriendoService.getTodos(),
      casas: this.casaService.getTodos(),
      propietarios: this.personaService.getPropietarios(),
      arrendatarios: this.personaService.getArrendatarios(),
    }).subscribe({
      next: (datos) => {
        this.arriendos.set(datos.arriendos);
        this.casas.set(datos.casas);
        this.propietarios.set(datos.propietarios);
        this.arrendatarios.set(datos.arrendatarios);
        this.cargando.set(false);
      },
      error: (err) => {
        this.errorCarga.set(true);
        this.cargando.set(false);
        this.notificacionService.error(
          extraerError(err, 'No se pudieron consultar los arriendos.'),
          'Error de conexión',
        );
      },
    });
  }

  /**
   * Nombre de una persona por su documento (o el documento si no está en las listas).
   * @param tipo Tipo de documento.
   * @param numero Número de documento.
   */
  persona(tipo: string, numero: string): string {
    return this.nombres().get(llavePersona(tipo, numero)) ?? `${tipo} ${numero}`;
  }
}
