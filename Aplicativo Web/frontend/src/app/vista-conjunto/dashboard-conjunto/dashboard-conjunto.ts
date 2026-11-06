import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, forkJoin, of } from 'rxjs';
import { Vista } from '../../models/vista.enum';
import { FacturaModel } from '../../models/factura.model';
import { CasaModel } from '../../models/casa.model';
import { PersonaModel } from '../../models/persona.model';
import { ReservaSalonModel } from '../../models/reserva-salon.model';
import { InfoBdModel } from '../../models/info-bd.model';
import { FacturaService } from '../../services/factura.service';
import { CasaService } from '../../services/casa.service';
import { PersonaService } from '../../services/persona.service';
import { ReservaSalonService } from '../../services/reserva-salon.service';
import { EstadoService } from '../../services/estado.service';
import { NotificacionService } from '../../services/notificacion.service';
import { Metrica, MetricasDashboard } from './metricas-dashboard/metricas-dashboard';
import { UltimasFacturas } from './ultimas-facturas/ultimas-facturas';
import { ProximasReservas } from './proximas-reservas/proximas-reservas';
import { EstadoConexion, EstadoConsulta } from './estado-conexion/estado-conexion';

/** Resultado del forkJoin del Inicio. `null` = esa fuente falló. */
interface DatosInicio {
  /** Facturas, o null si la consulta falló. */
  facturas: FacturaModel[] | null;

  /** Casas, o null si la consulta falló. */
  casas: CasaModel[] | null;

  /** Residentes, o null si la consulta falló. */
  residentes: PersonaModel[] | null;

  /** Reservas, o null si la consulta falló. */
  reservas: ReservaSalonModel[] | null;
}

/** Mensaje del aviso cuando alguna consulta al backend falla. */
const AVISO_BACKEND = 'Verificar el back.';

/**
 * Convierte una fecha a texto 'AAAA-MM-DD' en hora local, para compararla con las fechas del JSON.
 * @param fecha Fecha a convertir.
 */
function aIsoLocal(fecha: Date): string {
  const mes = String(fecha.getMonth() + 1).padStart(2, '0');
  const dia = String(fecha.getDate()).padStart(2, '0');
  return `${fecha.getFullYear()}-${mes}-${dia}`;
}

/**
 * @component DashboardConjunto
 * @description Inicio. Consulta todos los datos (métricas, facturas, reservas y conexión)
 * y se los pasa a sus subcomponentes, que solo se encargan de dibujarlos.
 */
@Component({
  selector: 'app-dashboard-conjunto',
  imports: [MetricasDashboard, UltimasFacturas, ProximasReservas, EstadoConexion],
  templateUrl: './dashboard-conjunto.html',
  styleUrl: './dashboard-conjunto.css',
})
export class DashboardConjunto implements OnInit {
  /** Servicio de facturas. */
  private facturaService = inject(FacturaService);
  /** Servicio de casas. */
  private casaService = inject(CasaService);
  /** Servicio de personas y roles. */
  private personaService = inject(PersonaService);
  /** Servicio de reservas del salón comunal. */
  private reservaSalonService = inject(ReservaSalonService);
  /** Servicio que revisa la conexión con el backend y la base de datos. */
  private estadoService = inject(EstadoService);
  /** Servicio de avisos flotantes. */
  private notificacionService = inject(NotificacionService);
  /** Router de Angular para navegar entre pantallas. */
  private router = inject(Router);

  /** true mientras llegan los datos de las métricas y listas. */
  protected readonly cargando = signal(true);

  /** Datos de todas las fuentes, o null mientras cargan. */
  protected readonly datos = signal<DatosInicio | null>(null);

  /** Estado de GET /api/estado. */
  protected readonly estadoBackend = signal<EstadoConsulta>('consultando');

  /** Estado de GET /api/bd/prueba. */
  protected readonly estadoBd = signal<EstadoConsulta>('consultando');

  /** Datos de la base si la consulta respondió. */
  protected readonly infoBd = signal<InfoBdModel | null>(null);

  /** Fecha de hoy (filtro de próximas reservas). */
  protected readonly hoy = signal(new Date());

  /** true si la consulta de facturas falló. */
  protected readonly errorFacturas = computed(() => this.datos()?.facturas === null);

  /** Facturas que todavía no se han pagado del todo (pendientes, vencidas y abonadas). */
  private readonly facturasSinPagar = computed(() =>
    (this.datos()?.facturas ?? []).filter((f) => f.estado !== 'PAGADA'),
  );

  /** Las 5 facturas más recientes por fecha de vencimiento. */
  protected readonly ultimasFacturas = computed(() =>
    [...(this.datos()?.facturas ?? [])]
      .sort(
        (a, b) =>
          b.fechaVencimiento.localeCompare(a.fechaVencimiento) || b.numeroFactura - a.numeroFactura,
      )
      .slice(0, 5),
  );

  /** Las 4 reservas más cercanas desde hoy. */
  protected readonly proximasReservas = computed(() => {
    const hoyIso = aIsoLocal(this.hoy());
    return (this.datos()?.reservas ?? [])
      .filter((r) => r.fechaReserva >= hoyIso && r.estado !== 'CANCELADA')
      .sort((a, b) => a.horaInicio.localeCompare(b.horaInicio))
      .slice(0, 4);
  });

  /** Las 4 tarjetas métricas, calculadas a partir de los datos. */
  protected readonly metricas = computed<Metrica[]>(() => {
    const datos = this.datos();
    const sinPagar = this.facturasSinPagar();
    return [
      this.metricaConteo('Casas', datos?.casas, (lista) => lista.length, 'casas registradas'),
      this.metricaConteo(
        'Residentes',
        datos?.residentes,
        (lista) => lista.length,
        'personas que viven en el conjunto',
      ),
      {
        etiqueta: 'Cartera pendiente',
        valor: datos?.facturas ? sinPagar.reduce((total, f) => total + (f.saldo ?? 0), 0) : null,
        esMoneda: true,
        contexto: !datos?.facturas
          ? 'No se pudo consultar'
          : sinPagar.length === 0
            ? 'Cartera al día'
            : `${sinPagar.length} facturas sin pagar`,
        tono: !datos?.facturas ? 'neutro' : sinPagar.length === 0 ? 'positivo' : 'atencion',
      },
      this.metricaConteo(
        'Reservas del salón',
        datos?.reservas,
        () => this.proximasReservas().length,
        'próximas reservas',
      ),
    ];
  });

  /** Consulta los datos y el estado de la conexión al abrir el Inicio. */
  ngOnInit(): void {
    this.cargarDatos();
    this.cargarConexion();
  }

  /** Consulta todas las fuentes a la vez; si una falla, las demás se muestran igual. */
  cargarDatos(): void {
    this.cargando.set(true);
    forkJoin({
      facturas: this.facturaService.getTodos().pipe(catchError(() => of(null))),
      casas: this.casaService.getTodos().pipe(catchError(() => of(null))),
      residentes: this.personaService.getResidentes().pipe(catchError(() => of(null))),
      reservas: this.reservaSalonService.getTodos().pipe(catchError(() => of(null))),
    }).subscribe((resultado) => {
      this.datos.set(resultado);
      this.cargando.set(false);
      if (Object.values(resultado).some((lista) => lista === null)) {
        this.notificacionService.error(AVISO_BACKEND, 'Error de conexión');
      }
    });
  }

  /**
   * Navega a otra sección (enlaces "Ver todas" de las tarjetas).
   * @param vista Sección de destino; es también su ruta (/finanzas, /salon-comunal…).
   */
  irA(vista: Vista): void {
    this.router.navigate(['/', vista]);
  }

  /** Consulta por separado si responden el backend y la base de datos. */
  cargarConexion(): void {
    this.estadoService.getEstado().subscribe({
      next: () => this.estadoBackend.set('ok'),
      error: () => {
        this.estadoBackend.set('error');
        this.notificacionService.error(AVISO_BACKEND, 'Error de conexión');
      },
    });

    this.estadoService.getInfoBd().subscribe({
      next: (info) => {
        this.infoBd.set(info);
        this.estadoBd.set('ok');
      },
      error: () => {
        this.estadoBd.set('error');
        this.notificacionService.error(
          'Revisa la VM, Tailscale y PostgreSQL.',
          'Sin conexión con la base',
        );
      },
    });
  }

  /**
   * Arma una métrica de conteo. Lista vacía = sin registros; null = la consulta falló.
   * @param etiqueta Texto de arriba de la tarjeta.
   * @param lista Datos de la fuente.
   * @param contar Cómo calcular la cifra.
   * @param contexto Texto de abajo cuando hay datos.
   */
  private metricaConteo<T>(
    etiqueta: string,
    lista: T[] | null | undefined,
    contar: (lista: T[]) => number,
    contexto: string,
  ): Metrica {
    const conDatos = !!lista && lista.length > 0;
    return {
      etiqueta,
      valor: conDatos ? contar(lista) : null,
      esMoneda: false,
      contexto: conDatos
        ? contexto
        : lista === null
          ? 'No se pudo consultar'
          : 'Pendiente de conexión',
      tono: 'neutro',
    };
  }
}
