import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { CurrencyPipe, DatePipe } from '@angular/common';
import { environment } from '../../../environments/environment';
import { FacturaModel } from '../../models/factura.model';
import { FacturaService } from '../../services/factura.service';
import { NotificacionService } from '../../services/notificacion.service';

/** Filtro del control segmentado. */
type FiltroEstado = 'TODAS' | FacturaModel['estado'];

/** Botones del control segmentado, en orden. */
const OPCIONES_FILTRO: { valor: FiltroEstado; texto: string }[] = [
  { valor: 'TODAS', texto: 'Todas' },
  { valor: 'PAGADA', texto: 'Pagadas' },
  { valor: 'PENDIENTE', texto: 'Pendientes' },
  { valor: 'VENCIDA', texto: 'Vencidas' },
];

/** Texto e insignia de cada estado de factura. */
const ETIQUETA_FACTURA: Record<FacturaModel['estado'], string> = {
  PAGADA: 'Pagada',
  PENDIENTE: 'Pendiente',
  VENCIDA: 'Vencida',
};
const ESTILO_FACTURA: Record<FacturaModel['estado'], 'ok' | 'pendiente' | 'alerta'> = {
  PAGADA: 'ok',
  PENDIENTE: 'pendiente',
  VENCIDA: 'alerta',
};

/**
 * Pasa un texto a minúsculas y sin tildes, para que "salon" encuentre "Salón".
 * @param texto Texto a normalizar.
 */
function normalizar(texto: string): string {
  return texto
    .normalize('NFD')
    .replace(/\p{Diacritic}/gu, '')
    .toLowerCase()
    .trim();
}

/**
 * @component Finanzas
 * @description Muestra las facturas del conjunto con totales, filtro por estado y buscador.
 */
@Component({
  selector: 'app-finanzas',
  imports: [CurrencyPipe, DatePipe],
  templateUrl: './finanzas.html',
  styleUrl: './finanzas.css',
})
export class Finanzas implements OnInit {
  private facturaService = inject(FacturaService);
  private notificacionService = inject(NotificacionService);

  /** Facturas obtenidas del servicio. */
  protected readonly facturas = signal<FacturaModel[]>([]);

  /** true mientras llega la respuesta. */
  protected readonly cargando = signal(true);

  /** Mensaje de error si falla la consulta. */
  protected readonly error = signal<string | null>(null);

  /** Filtro de estado seleccionado. */
  protected readonly filtroEstado = signal<FiltroEstado>('TODAS');

  /** Texto escrito en el buscador. */
  protected readonly busqueda = signal('');

  /** true si las facturas son datos de ejemplo (constante del environment). */
  protected readonly usarMocks = environment.usarMocks;

  /** Diccionarios y opciones (constantes). */
  protected readonly opcionesFiltro = OPCIONES_FILTRO;
  protected readonly etiquetaFactura = ETIQUETA_FACTURA;
  protected readonly estiloFactura = ESTILO_FACTURA;

  /** Cuántas facturas hay de cada estado (para los botones del filtro). */
  protected readonly conteos = computed<Record<FiltroEstado, number>>(() => {
    const lista = this.facturas();
    return {
      TODAS: lista.length,
      PAGADA: lista.filter((f) => f.estado === 'PAGADA').length,
      PENDIENTE: lista.filter((f) => f.estado === 'PENDIENTE').length,
      VENCIDA: lista.filter((f) => f.estado === 'VENCIDA').length,
    };
  });

  /** Facturas visibles según el filtro y la búsqueda, de la más reciente a la más antigua. */
  protected readonly facturasFiltradas = computed(() => {
    const filtro = this.filtroEstado();
    const texto = normalizar(this.busqueda());
    return this.facturas()
      .filter((f) => filtro === 'TODAS' || f.estado === filtro)
      .filter((f) => !texto || normalizar(`${f.casa} ${f.numero} ${f.periodo}`).includes(texto))
      .sort((a, b) => b.fechaVencimiento.localeCompare(a.fechaVencimiento) || a.id - b.id);
  });

  /** Suma de todas las facturas. */
  protected readonly totalFacturado = computed(() =>
    this.facturas().reduce((total, f) => total + f.valor, 0),
  );

  /** Suma de las facturas pagadas. */
  protected readonly totalRecaudado = computed(() =>
    this.facturas()
      .filter((f) => f.estado === 'PAGADA')
      .reduce((total, f) => total + f.valor, 0),
  );

  /** Lo que falta por cobrar (pendientes + vencidas). */
  protected readonly totalPendiente = computed(() => this.totalFacturado() - this.totalRecaudado());

  /** Porcentaje recaudado, de 0 a 100, sin decimales. */
  protected readonly porcentajeRecaudado = computed(() => {
    const total = this.totalFacturado();
    return total === 0 ? 0 : Math.round((this.totalRecaudado() / total) * 100);
  });

  /** "1 factura" o "6 facturas" para el pie de la tabla. */
  protected readonly textoConteo = computed(() => {
    const visibles = this.facturasFiltradas().length;
    const total = this.facturas().length;
    const palabra = visibles === 1 ? 'factura' : 'facturas';
    return visibles === total ? `${visibles} ${palabra}` : `${visibles} ${palabra} de ${total}`;
  });

  ngOnInit(): void {
    this.cargarFacturas();
  }

  /** Consulta las facturas y actualiza el estado de la vista. */
  cargarFacturas(): void {
    this.cargando.set(true);
    this.error.set(null);
    this.facturaService.getTodas().subscribe({
      next: (datos) => {
        this.facturas.set(datos);
        this.cargando.set(false);
      },
      error: () => {
        this.error.set('No se pudieron consultar las facturas.');
        this.cargando.set(false);
        this.notificacionService.error(
          'Verifica que el backend esté corriendo.',
          'Error de conexión',
        );
      },
    });
  }

  /**
   * Cambia el filtro de estado.
   * @param filtro Estado elegido en el control segmentado.
   */
  cambiarFiltro(filtro: FiltroEstado): void {
    this.filtroEstado.set(filtro);
  }

  /**
   * Guarda el texto del buscador.
   * @param texto Lo que escribió el usuario.
   */
  buscar(texto: string): void {
    this.busqueda.set(texto);
  }

  /** Quita el filtro y la búsqueda (botón del estado vacío). */
  limpiarFiltros(): void {
    this.filtroEstado.set('TODAS');
    this.busqueda.set('');
  }
}
