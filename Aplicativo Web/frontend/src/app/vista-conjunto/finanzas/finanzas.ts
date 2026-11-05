import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { FacturaModel } from '../../models/factura.model';
import { FacturaService } from '../../services/factura.service';
import { NotificacionService } from '../../services/notificacion.service';
import { TotalesFinanzas } from './totales-finanzas/totales-finanzas';
import { FiltroEstado, FiltrosFacturas } from './filtros-facturas/filtros-facturas';
import { TablaFacturas } from './tabla-facturas/tabla-facturas';
import { GestionConceptos } from './gestion-conceptos/gestion-conceptos';
import { GestionFacturas } from './gestion-facturas/gestion-facturas';
import { DetalleFacturaModal } from './detalle-factura/detalle-factura-modal';

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

/** Pestañas de la sección. */
type PestanaFinanzas = 'facturas' | 'conceptos';

/** Datos de cada botón de pestaña: su valor, su texto y su ícono. */
const PESTANAS: { valor: PestanaFinanzas; texto: string; icono: string }[] = [
  { valor: 'facturas', texto: 'Facturas', icono: 'fa-file-invoice-dollar' },
  { valor: 'conceptos', texto: 'Conceptos de cobro', icono: 'fa-tags' },
];

/**
 * @component Finanzas
 * @description Sección Finanzas con dos pestañas: Facturas (consulta las facturas, guarda el
 * filtro y la búsqueda, y reparte los datos entre sus subcomponentes) y Conceptos de cobro
 * (CRUD del catálogo, que se encarga solo GestionConceptos).
 */
@Component({
  selector: 'app-finanzas',
  imports: [TotalesFinanzas, FiltrosFacturas, TablaFacturas, GestionConceptos, GestionFacturas, DetalleFacturaModal],
  templateUrl: './finanzas.html',
  styleUrl: './finanzas.css',
})
export class Finanzas implements OnInit {
  private facturaService = inject(FacturaService);
  private notificacionService = inject(NotificacionService);

  /** Pestañas del control segmentado (constante). */
  protected readonly pestanas = PESTANAS;

  /** Pestaña activa. */
  protected readonly pestana = signal<PestanaFinanzas>('facturas');

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

  /** Número de la factura cuyo detalle está abierto (null = cerrado). */
  protected readonly idDetalle = signal<number | null>(null);

  /** La factura del detalle, siempre la versión más reciente de la lista. */
  protected readonly facturaDetalle = computed(
    () => this.facturas().find((f) => f.numeroFactura === this.idDetalle()) ?? null,
  );

  ngOnInit(): void {
    this.cargarFacturas();
  }

  /**
   * Consulta las facturas y actualiza el estado de la vista.
   * @param silencioso true = recarga sin esqueleto ni pantalla de error (tras crear o editar).
   */
  cargarFacturas(silencioso = false): void {
    if (!silencioso) {
      this.cargando.set(true);
      this.error.set(null);
    }
    this.facturaService.getTodos().subscribe({
      next: (datos) => {
        this.facturas.set(datos);
        this.cargando.set(false);
      },
      error: () => {
        if (!silencioso) {
          this.error.set('No se pudieron consultar las facturas.');
          this.cargando.set(false);
        }
        this.notificacionService.error(
          'Verifica que el backend esté corriendo.',
          'Error de conexión',
        );
      },
    });
  }

  /** Tras crear una factura: recarga la lista y abre su detalle para agregar líneas. */
  abrirDetalleNueva(numeroFactura: number): void {
    this.cargarFacturas(true);
    this.idDetalle.set(numeroFactura);
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
