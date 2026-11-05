import { Component, computed, inject, OnInit, signal } from '@angular/core';
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
import { extraerError } from '../../../compartido/extraer-error';
import { ModalFormulario } from '../../../compartido/modal-formulario/modal-formulario';
import { DialogoConfirmar } from '../../../compartido/dialogo-confirmar/dialogo-confirmar';

/** Campos del formulario (todo como texto). */
interface FormularioParqueo {
  vehiculo: string;
  celda: string;
  entrada: string;
  salida: string;
}

const FORMULARIO_VACIO: FormularioParqueo = {
  vehiculo: '',
  celda: '',
  entrada: '',
  salida: '',
};

/**
 * @component GestionParqueos
 * @description Pestaña Parqueos de Parqueadero: entradas y salidas de vehículos en las celdas.
 * Las fechas son TIMESTAMP ("2026-10-28 07:30:00"); el formulario usa datetime-local.
 */
@Component({
  selector: 'app-gestion-parqueos',
  imports: [ModalFormulario, DialogoConfirmar],
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
  protected readonly etiquetaCelda = ETIQUETA_CELDA;

  /** Modal de crear/editar. */
  protected readonly modalAbierto = signal(false);

  /** Parqueo que se edita (null = crear). */
  protected readonly editando = signal<ParqueoModel | null>(null);

  protected readonly formulario = signal<FormularioParqueo>({ ...FORMULARIO_VACIO });
  protected readonly guardando = signal(false);
  protected readonly errorFormulario = signal<string | null>(null);

  /** Parqueo pendiente de confirmar para eliminar. */
  protected readonly parqueoAEliminar = signal<ParqueoModel | null>(null);
  protected readonly eliminando = signal(false);

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

  /** Abre el modal vacío para crear. */
  abrirCrear(): void {
    this.editando.set(null);
    this.formulario.set({ ...FORMULARIO_VACIO });
    this.errorFormulario.set(null);
    this.modalAbierto.set(true);
  }

  /**
   * Abre el modal con los datos del parqueo.
   * @param p Parqueo a editar.
   */
  abrirEditar(p: ParqueoModel): void {
    this.editando.set(p);
    this.formulario.set({
      vehiculo: p.vehiculoPlaca,
      celda: p.idCelda.toString(),
      entrada: aInputFechaHora(p.fechaHoraEntrada),
      salida: aInputFechaHora(p.fechaHoraSalida),
    });
    this.errorFormulario.set(null);
    this.modalAbierto.set(true);
  }

  /** Cierra el modal sin guardar. */
  cerrarModal(): void {
    this.modalAbierto.set(false);
  }

  /**
   * Actualiza un campo del formulario.
   * @param campo Nombre del campo.
   * @param valor Valor escrito o elegido.
   */
  cambiarCampo(campo: keyof FormularioParqueo, valor: string): void {
    this.formulario.update((f) => ({ ...f, [campo]: valor }));
  }

  /** Valida y guarda (crear o editar). */
  guardar(): void {
    const f = this.formulario();
    if (!f.vehiculo || !f.celda) {
      this.errorFormulario.set('Elige el vehículo y la celda.');
      return;
    }
    if (!f.entrada) {
      this.errorFormulario.set('La fecha y hora de entrada son obligatorias.');
      return;
    }
    if (f.salida && f.salida < f.entrada) {
      this.errorFormulario.set('La salida no puede ser anterior a la entrada.');
      return;
    }

    const entrada = desdeInputFechaHora(f.entrada) as string;
    const salida = desdeInputFechaHora(f.salida);
    const actual = this.editando();
    this.errorFormulario.set(null);
    this.guardando.set(true);

    const peticion = actual
      ? this.parqueoService.actualizar({
        ...actual,
        fechaHoraEntrada: entrada,
        fechaHoraSalida: salida,
      })
      : this.parqueoService.crear({
        fechaHoraEntrada: entrada,
        fechaHoraSalida: salida,
        vehiculoPlaca: f.vehiculo,
        idCelda: Number(f.celda),
      });

    peticion.subscribe({
      next: () => {
        this.guardando.set(false);
        this.modalAbierto.set(false);
        this.notificacionService.exito(
          actual ? 'Parqueo actualizado.' : 'Parqueo registrado.',
          'Listo',
        );
        this.cargarParqueos();
      },
      error: (err) => {
        this.guardando.set(false);
        this.errorFormulario.set(extraerError(err, 'No se pudo guardar el parqueo.'));
      },
    });
  }

  /** Confirma y elimina el parqueo. */
  eliminar(): void {
    const p = this.parqueoAEliminar();
    if (!p) return;
    this.eliminando.set(true);
    this.parqueoService.eliminar(p.idParqueo).subscribe({
      next: () => {
        this.eliminando.set(false);
        this.parqueoAEliminar.set(null);
        this.notificacionService.exito('Parqueo eliminado.', 'Listo');
        this.cargarParqueos();
      },
      error: (err) => {
        this.eliminando.set(false);
        this.parqueoAEliminar.set(null);
        this.notificacionService.error(
          extraerError(err, 'No se pudo eliminar el parqueo.'),
          'No se pudo eliminar',
        );
      },
    });
  }
}
