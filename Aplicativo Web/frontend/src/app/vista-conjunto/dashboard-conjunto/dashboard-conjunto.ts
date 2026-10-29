import { Component, computed, inject, OnInit, output, signal } from '@angular/core';
import { CurrencyPipe, DatePipe, DecimalPipe } from '@angular/common';
import { catchError, forkJoin, of } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Vista } from '../../models/vista.enum';
import { FacturaModel } from '../../models/factura.model';
import { CasaModel } from '../../models/casa.model';
import { PersonaModel } from '../../models/persona.model';
import { ReservaModel } from '../../models/reserva.model';
import { InfoBdModel } from '../../models/info-bd.model';
import { FacturaService } from '../../services/factura.service';
import { CasaService } from '../../services/casa.service';
import { PersonaService } from '../../services/persona.service';
import { ReservaService } from '../../services/reserva.service';
import { EstadoService } from '../../services/estado.service';
import { NotificacionService } from '../../services/notificacion.service';

/** Estado de una consulta de diagnóstico (backend o base). */
type EstadoConexion = 'consultando' | 'ok' | 'error';

/** Estilo de insignia de DESIGN.md. */
type EstiloInsignia = 'ok' | 'pendiente' | 'alerta' | 'neutro';

/** Resultado del forkJoin del Inicio. `null` = esa fuente falló. */
interface DatosInicio {
  facturas: FacturaModel[] | null;
  casas: CasaModel[] | null;
  personas: PersonaModel[] | null;
  reservas: ReservaModel[] | null;
}

/** Una tarjeta métrica. `valor: null` se muestra como "Sin datos". */
interface Metrica {
  etiqueta: string;
  valor: number | null;
  esMoneda: boolean;
  contexto: string;
  tono: 'positivo' | 'atencion' | 'neutro';
  ejemplo: boolean;
}

/** Texto e insignia de cada estado de factura. */
const ETIQUETA_FACTURA: Record<FacturaModel['estado'], string> = {
  PAGADA: 'Pagada',
  PENDIENTE: 'Pendiente',
  VENCIDA: 'Vencida',
};
const ESTILO_FACTURA: Record<FacturaModel['estado'], EstiloInsignia> = {
  PAGADA: 'ok',
  PENDIENTE: 'pendiente',
  VENCIDA: 'alerta',
};

/** Texto e insignia de cada estado de conexión. */
const TEXTO_CONEXION: Record<EstadoConexion, string> = {
  consultando: 'Consultando…',
  ok: 'En línea',
  error: 'Sin conexión',
};
const ESTILO_CONEXION: Record<EstadoConexion, EstiloInsignia> = {
  consultando: 'neutro',
  ok: 'ok',
  error: 'alerta',
};

/** Mensaje de aviso cuando el backend no responde (igual en todo el Inicio para que no se repita). */
const AVISO_BACKEND = 'Verifica que el backend esté corriendo.';

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
 * @description Inicio: métricas del conjunto, últimas facturas, próximas reservas del salón
 * y estado real de la conexión con el backend y PostgreSQL.
 */
@Component({
  selector: 'app-dashboard-conjunto',
  imports: [CurrencyPipe, DatePipe, DecimalPipe],
  templateUrl: './dashboard-conjunto.html',
  styleUrl: './dashboard-conjunto.css',
})
export class DashboardConjunto implements OnInit {
  private facturaService = inject(FacturaService);
  private casaService = inject(CasaService);
  private personaService = inject(PersonaService);
  private reservaService = inject(ReservaService);
  private estadoService = inject(EstadoService);
  private notificacionService = inject(NotificacionService);

  /** Pide al contenedor cambiar de sección ("Ver todas"). */
  readonly irA = output<Vista>();

  /** true mientras llegan los datos de las métricas y listas. */
  protected readonly cargando = signal(true);

  /** Datos de todas las fuentes, o null mientras cargan. */
  protected readonly datos = signal<DatosInicio | null>(null);

  /** Estado de GET /api/estado. */
  protected readonly estadoBackend = signal<EstadoConexion>('consultando');

  /** Estado de GET /api/bd/prueba. */
  protected readonly estadoBd = signal<EstadoConexion>('consultando');

  /** Datos de la base si la consulta respondió. */
  protected readonly infoBd = signal<InfoBdModel | null>(null);

  /** Fecha de hoy (mes de la cabecera y filtro de próximas reservas). */
  protected readonly hoy = signal(new Date());

  /** true si las facturas son datos de ejemplo (constante del environment). */
  protected readonly usarMocks = environment.usarMocks;

  /** Diccionarios de textos y estilos (constantes). */
  protected readonly etiquetaFactura = ETIQUETA_FACTURA;
  protected readonly estiloFactura = ESTILO_FACTURA;
  protected readonly textoConexion = TEXTO_CONEXION;
  protected readonly estiloConexion = ESTILO_CONEXION;

  /** true si la consulta de facturas falló. */
  protected readonly errorFacturas = computed(() => this.datos()?.facturas === null);

  /** Facturas que todavía no se han pagado (pendientes y vencidas). */
  private readonly facturasSinPagar = computed(() =>
    (this.datos()?.facturas ?? []).filter((f) => f.estado !== 'PAGADA'),
  );

  /** Las 5 facturas más recientes por fecha de vencimiento. */
  protected readonly ultimasFacturas = computed(() =>
    [...(this.datos()?.facturas ?? [])]
      .sort((a, b) => b.fechaVencimiento.localeCompare(a.fechaVencimiento) || b.id - a.id)
      .slice(0, 5),
  );

  /** Las 4 reservas más cercanas desde hoy. */
  protected readonly proximasReservas = computed(() => {
    const hoyIso = aIsoLocal(this.hoy());
    return (this.datos()?.reservas ?? [])
      .filter((r) => r.fecha >= hoyIso && r.estado !== 'CANCELADA')
      .sort((a, b) => a.fecha.localeCompare(b.fecha) || a.horaInicio.localeCompare(b.horaInicio))
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
        datos?.personas,
        (lista) => lista.filter((p) => p.tipo === 'RESIDENTE').length,
        'personas que viven en el conjunto',
      ),
      {
        etiqueta: 'Cartera pendiente',
        valor: datos?.facturas ? sinPagar.reduce((total, f) => total + f.valor, 0) : null,
        esMoneda: true,
        contexto: !datos?.facturas
          ? 'No se pudo consultar'
          : sinPagar.length === 0
            ? 'Cartera al día'
            : `${sinPagar.length} facturas sin pagar`,
        tono: !datos?.facturas ? 'neutro' : sinPagar.length === 0 ? 'positivo' : 'atencion',
        ejemplo: this.usarMocks,
      },
      this.metricaConteo(
        'Reservas del salón',
        datos?.reservas,
        () => this.proximasReservas().length,
        'próximas reservas',
      ),
    ];
  });

  ngOnInit(): void {
    this.cargarDatos();
    this.cargarConexion();
  }

  /** Consulta todas las fuentes a la vez; si una falla, las demás se muestran igual. */
  cargarDatos(): void {
    this.cargando.set(true);
    forkJoin({
      facturas: this.facturaService.getTodas().pipe(catchError(() => of(null))),
      casas: this.casaService.getTodas().pipe(catchError(() => of(null))),
      personas: this.personaService.getTodas().pipe(catchError(() => of(null))),
      reservas: this.reservaService.getTodas().pipe(catchError(() => of(null))),
    }).subscribe((resultado) => {
      this.datos.set(resultado);
      this.cargando.set(false);
      if (Object.values(resultado).some((lista) => lista === null)) {
        this.notificacionService.error(AVISO_BACKEND, 'Error de conexión');
      }
    });
  }

  /** Consulta el backend y la base por separado, como la antigua página de Estado. */
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
   * Arma una métrica de conteo. Lista vacía = módulo pendiente; null = la consulta falló.
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
      ejemplo: false,
    };
  }
}
