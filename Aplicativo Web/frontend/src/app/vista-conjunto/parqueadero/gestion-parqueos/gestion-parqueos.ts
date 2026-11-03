import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { forkJoin } from 'rxjs';
import {
  ParqueoModel,
  textoFechaHora,
} from '../../../models/parqueo.model';
import { ETIQUETA_TIPO_VEHICULO, VehiculoModel } from '../../../models/vehiculo.model';
import {
  CeldaParqueaderoModel,
  nombreCelda,
} from '../../../models/celda-parqueadero.model';
import { ParqueoService } from '../../../services/parqueo.service';
import { VehiculoService } from '../../../services/vehiculo.service';
import { CeldaParqueaderoService } from '../../../services/celda-parqueadero.service';
import { NotificacionService } from '../../../services/notificacion.service';
import { extraerError } from '../../../compartido/extraer-error';

/**
 * @component GestionParqueos
 * @description Pestaña Parqueos de Parqueadero: entradas y salidas de vehículos en las celdas.
 * Las fechas son TIMESTAMP ("2026-10-28 07:30:00"); el formulario usa datetime-local.
 */
@Component({
  selector: 'app-gestion-parqueos',
  imports: [],
  templateUrl: './gestion-parqueos.html',
  styleUrl: './gestion-parqueos.css',
})
export class GestionParqueos implements OnInit {
  private parqueoService = inject(ParqueoService);
  private vehiculoService = inject(VehiculoService);
  private celdaService = inject(CeldaParqueaderoService);
  private notificacionService = inject(NotificacionService);

  /** Parqueos obtenidos del backend (vienen del más reciente al más antiguo). */
  protected readonly parqueos = signal<ParqueoModel[]>([]);

  /** Vehículos y celdas para los selectores. */
  protected readonly vehiculos = signal<VehiculoModel[]>([]);
  protected readonly celdas = signal<CeldaParqueaderoModel[]>([]);

  /** Datos de cada vehículo por su placa (para mostrar marca y tipo). */
  private readonly vehiculoPorPlaca = computed(
    () => new Map(this.vehiculos().map((v) => [v.placa, v])),
  );

  protected readonly cargando = signal(true);
  protected readonly errorCarga = signal(false);
  protected readonly busqueda = signal('');

  /** true = solo los vehículos que siguen adentro (sin salida). */
  protected readonly soloAdentro = signal(false);

  /** Parqueos visibles según la búsqueda y la casilla "Solo adentro". */
  protected readonly parqueosFiltrados = computed(() => {
    const texto = this.busqueda().trim().toLowerCase();
    return this.parqueos()
      .filter((p) => !this.soloAdentro() || !p.fechaHoraSalida)
      .filter(
        (p) =>
          !texto ||
          `${p.vehiculoPlaca} ${nombreCelda(p.idCelda)} ${this.descripcionVehiculo(p.vehiculoPlaca)}`
            .toLowerCase()
            .includes(texto),
      );
  });

  /** "15 parqueos" o "3 parqueos de 15". */
  protected readonly textoConteo = computed(() => {
    const visibles = this.parqueosFiltrados().length;
    const total = this.parqueos().length;
    const palabra = visibles === 1 ? 'parqueo' : 'parqueos';
    return visibles === total ? `${visibles} ${palabra}` : `${visibles} ${palabra} de ${total}`;
  });

  /** Constantes y funciones para la plantilla. */
  protected readonly nombreCelda = nombreCelda;
  protected readonly textoFechaHora = textoFechaHora;

  ngOnInit(): void {
    this.cargarParqueos();
  }

  /** Consulta parqueos, vehículos y celdas a la vez. */
  cargarParqueos(): void {
    this.cargando.set(true);
    this.errorCarga.set(false);
    forkJoin({
      parqueos: this.parqueoService.getTodos(),
      vehiculos: this.vehiculoService.getTodos(),
      celdas: this.celdaService.getTodos(),
    }).subscribe({
      next: ({ parqueos, vehiculos, celdas }) => {
        this.parqueos.set(parqueos);
        this.vehiculos.set(vehiculos);
        this.celdas.set(celdas);
        this.cargando.set(false);
      },
      error: (err) => {
        this.errorCarga.set(true);
        this.cargando.set(false);
        this.notificacionService.error(
          extraerError(err, 'No se pudieron consultar los parqueos.'),
          'Error de conexión',
        );
      },
    });
  }

  /**
   * "Carro · Mazda" a partir de la placa.
   * @param placa Placa del vehículo.
   */
  descripcionVehiculo(placa: string): string {
    const v = this.vehiculoPorPlaca().get(placa);
    if (!v) return '';
    return [ETIQUETA_TIPO_VEHICULO[v.tipo] ?? v.tipo, v.marca].filter(Boolean).join(' · ');
  }
}
