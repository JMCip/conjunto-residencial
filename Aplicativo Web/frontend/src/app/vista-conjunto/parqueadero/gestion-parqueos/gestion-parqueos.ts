import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { forkJoin } from 'rxjs';
import {
  aInputFechaHora,
  desdeInputFechaHora,
  ParqueoModel,
  textoFechaHora,
} from '../../../models/parqueo.model';
import { ETIQUETA_TIPO_VEHICULO, VehiculoModel } from '../../../models/vehiculo.model';
import {
  CeldaParqueaderoModel,
  ETIQUETA_CELDA,
  nombreCelda,
} from '../../../models/celda-parqueadero.model';
import { ParqueoService } from '../../../services/parqueo.service';
import { VehiculoService } from '../../../services/vehiculo.service';
import { CeldaParqueaderoService } from '../../../services/celda-parqueadero.service';
import { NotificacionService } from '../../../services/notificacion.service';
import { ModalFormulario } from '../../../compartido/modal-formulario/modal-formulario';
import { DialogoConfirmar } from '../../../compartido/dialogo-confirmar/dialogo-confirmar';
import { extraerError } from '../../../compartido/extraer-error';

/**
 * @component GestionParqueos
 * @description Pestaña Parqueos de Parqueadero: entradas y salidas de vehículos en las celdas.
 * Las fechas son TIMESTAMP ("2026-10-28 07:30:00"); el formulario usa datetime-local.
 */
@Component({
  selector: 'app-gestion-parqueos',
  imports: [FormsModule, ModalFormulario, DialogoConfirmar],
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

  // ---------- Modal de crear / editar ----------

  protected readonly modoModal = signal<'crear' | 'editar' | null>(null);
  private idEditando = 0;
  protected readonly formPlaca = signal('');
  protected readonly formCelda = signal<number | null>(null);
  protected readonly formEntrada = signal('');
  protected readonly formSalida = signal('');
  protected readonly errorFormulario = signal<string | null>(null);
  protected readonly guardando = signal(false);

  // ---------- Diálogo de eliminar ----------

  protected readonly parqueoAEliminar = signal<ParqueoModel | null>(null);
  protected readonly eliminando = signal(false);

  /** Constantes y funciones para la plantilla. */
  protected readonly nombreCelda = nombreCelda;
  protected readonly etiquetaCelda = ETIQUETA_CELDA;
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

  /** Abre el modal vacío, con la entrada en la hora actual. */
  abrirModalCrear(): void {
    const ahora = new Date();
    ahora.setMinutes(ahora.getMinutes() - ahora.getTimezoneOffset());
    this.idEditando = 0;
    this.formPlaca.set('');
    this.formCelda.set(null);
    this.formEntrada.set(ahora.toISOString().slice(0, 16));
    this.formSalida.set('');
    this.errorFormulario.set(null);
    this.modoModal.set('crear');
  }

  /**
   * Abre el modal con los datos de un parqueo (por ejemplo, para registrar la salida).
   * @param p Parqueo elegido en la tabla.
   */
  abrirModalEditar(p: ParqueoModel): void {
    this.idEditando = p.idParqueo;
    this.formPlaca.set(p.vehiculoPlaca);
    this.formCelda.set(p.idCelda);
    this.formEntrada.set(aInputFechaHora(p.fechaHoraEntrada));
    this.formSalida.set(aInputFechaHora(p.fechaHoraSalida));
    this.errorFormulario.set(null);
    this.modoModal.set('editar');
  }

  /** Cierra el modal sin guardar. */
  cerrarModal(): void {
    this.modoModal.set(null);
  }

  /** Valida y llama a crear o actualizar. */
  guardar(): void {
    const error = this.validar();
    this.errorFormulario.set(error);
    if (error) return;

    const datos = {
      fechaHoraEntrada: desdeInputFechaHora(this.formEntrada())!,
      fechaHoraSalida: desdeInputFechaHora(this.formSalida()),
      vehiculoPlaca: this.formPlaca(),
      idCelda: Number(this.formCelda()),
    };
    const creando = this.modoModal() === 'crear';
    const peticion = creando
      ? this.parqueoService.postCrear(datos)
      : this.parqueoService.putActualizar({ idParqueo: this.idEditando, ...datos });

    this.guardando.set(true);
    peticion.subscribe({
      next: () => {
        this.guardando.set(false);
        this.modoModal.set(null);
        this.notificacionService.exito(
          `Parqueo de ${datos.vehiculoPlaca} ${creando ? 'registrado' : 'actualizado'} correctamente.`,
        );
        this.cargarParqueos();
      },
      error: (err) => {
        this.guardando.set(false);
        this.errorFormulario.set(
          extraerError(err, `No se pudo ${creando ? 'registrar' : 'actualizar'} el parqueo.`),
        );
      },
    });
  }

  /** Pide confirmación antes de eliminar. */
  pedirEliminar(p: ParqueoModel): void {
    this.parqueoAEliminar.set(p);
  }

  /** Cierra el diálogo sin eliminar. */
  cancelarEliminar(): void {
    this.parqueoAEliminar.set(null);
  }

  /** Envía el DELETE del parqueo confirmado. */
  confirmarEliminar(): void {
    const p = this.parqueoAEliminar();
    if (!p) return;
    this.eliminando.set(true);
    this.parqueoService.deleteEliminar(p.idParqueo).subscribe({
      next: () => {
        this.eliminando.set(false);
        this.parqueoAEliminar.set(null);
        this.notificacionService.exito('Parqueo eliminado correctamente.');
        this.cargarParqueos();
      },
      error: (err) => {
        this.eliminando.set(false);
        this.parqueoAEliminar.set(null);
        this.notificacionService.error(
          extraerError(err, 'No se pudo eliminar el parqueo.'),
          'No se eliminó',
        );
      },
    });
  }

  /**
   * Revisa los campos antes de enviar.
   * @returns Mensaje del primer problema, o null si todo está bien.
   */
  private validar(): string | null {
    if (!this.formPlaca()) return 'Elige el vehículo.';
    if (this.formCelda() === null) return 'Elige la celda.';
    if (!this.formEntrada()) return 'Escribe la fecha y hora de entrada.';
    if (this.formSalida() && this.formSalida() <= this.formEntrada()) {
      return 'La salida debe ser después de la entrada.';
    }
    return null;
  }
}
