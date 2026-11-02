import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { FacturaModel } from '../../models/factura.model';
import { FacturaService } from '../../services/factura.service';
import { NotificacionService } from '../../services/notificacion.service';
import { TotalesFinanzas } from './totales-finanzas/totales-finanzas';
import { FiltroEstado, FiltrosFacturas } from './filtros-facturas/filtros-facturas';
import { TablaFacturas } from './tabla-facturas/tabla-facturas';

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
 * @description Pantalla de facturas. Consulta las facturas, guarda el filtro y la búsqueda,
 * y reparte los datos entre sus subcomponentes (totales, filtros y tabla).
 */
@Component({
  selector: 'app-finanzas',
  imports: [TotalesFinanzas, FiltrosFacturas, TablaFacturas],
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

  /** Cuántas facturas hay de cada estado (para los botones del filtro). */
  protected readonly conteos = computed<Record<FiltroEstado, number>>(() => {
    const lista = this.facturas();
    return {
      TODAS: lista.length,
      PAGADA: lista.filter((f) => f.estado === 'PAGADA').length,
      PENDIENTE: lista.filter((f) => f.estado === 'PENDIENTE').length,
      VENCIDA: lista.filter((f) => f.estado === 'VENCIDA').length,
      ABONADA: lista.filter((f) => f.estado === 'ABONADA').length,
    };
  });

  /** Facturas visibles según el filtro y la búsqueda, de la más reciente a la más antigua. */
  protected readonly facturasFiltradas = computed(() => {
    const filtro = this.filtroEstado();
    const texto = normalizar(this.busqueda());
    return this.facturas()
      .filter((f) => filtro === 'TODAS' || f.estado === filtro)
      .filter(
        (f) =>
          !texto ||
          normalizar(
            `casa ${f.casaNumero} mz ${f.casaManzana} ${f.numeroFactura} ${f.periodo}`,
          ).includes(texto),
      )
      .sort(
        (a, b) =>
          b.fechaVencimiento.localeCompare(a.fechaVencimiento) || a.numeroFactura - b.numeroFactura,
      );
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
    this.facturaService.getTodos().subscribe({
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
