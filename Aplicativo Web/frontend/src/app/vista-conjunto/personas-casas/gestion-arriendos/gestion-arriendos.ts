import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { CurrencyPipe, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { forkJoin } from 'rxjs';
import { ArriendoModel } from '../../../models/arriendo.model';
import { CasaModel, llaveCasa, nombreCasa, separarLlaveCasa } from '../../../models/casa.model';
import {
  llavePersona,
  nombrePersona,
  PersonaModel,
  separarLlavePersona,
} from '../../../models/persona.model';
import { ArriendoService } from '../../../services/arriendo.service';
import { CasaService } from '../../../services/casa.service';
import { PersonaService } from '../../../services/persona.service';
import { NotificacionService } from '../../../services/notificacion.service';
import { ModalFormulario } from '../../../compartido/modal-formulario/modal-formulario';
import { DialogoConfirmar } from '../../../compartido/dialogo-confirmar/dialogo-confirmar';
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
  imports: [CurrencyPipe, DatePipe, FormsModule, ModalFormulario, DialogoConfirmar],
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

  // ---------- Modal de crear / editar ----------

  protected readonly modoModal = signal<'crear' | 'editar' | null>(null);
  private idEditando = 0;
  protected readonly formCasa = signal('');
  protected readonly formPropietario = signal('');
  protected readonly formArrendatario = signal('');
  protected readonly formCuota = signal<number | null>(null);
  protected readonly formFechaInicio = signal('');
  protected readonly formFechaFin = signal('');
  protected readonly formEstado = signal<ArriendoModel['estadoContrato']>('VIGENTE');
  protected readonly errorFormulario = signal<string | null>(null);
  protected readonly guardando = signal(false);

  // ---------- Diálogo de eliminar ----------

  protected readonly arriendoAEliminar = signal<ArriendoModel | null>(null);
  protected readonly eliminando = signal(false);

  /** Constantes y funciones para la plantilla. */
  protected readonly estadosContrato = ESTADOS_CONTRATO;
  protected readonly nombreCasa = nombreCasa;
  protected readonly llaveCasa = llaveCasa;
  protected readonly llavePersona = llavePersona;
  protected readonly nombrePersona = nombrePersona;

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

  /** Abre el modal vacío. */
  abrirModalCrear(): void {
    this.idEditando = 0;
    this.formCasa.set('');
    this.formPropietario.set('');
    this.formArrendatario.set('');
    this.formCuota.set(null);
    this.formFechaInicio.set('');
    this.formFechaFin.set('');
    this.formEstado.set('VIGENTE');
    this.errorFormulario.set(null);
    this.modoModal.set('crear');
  }

  /**
   * Abre el modal con los datos de un contrato.
   * @param a Contrato elegido en la tabla.
   */
  abrirModalEditar(a: ArriendoModel): void {
    this.idEditando = a.idContrato;
    this.formCasa.set(llaveCasa(a.casaManzana, a.casaNumero));
    this.formPropietario.set(llavePersona(a.propietarioTipoDocumento, a.propietarioNumeroDocumento));
    this.formArrendatario.set(
      llavePersona(a.arrendatarioTipoDocumento, a.arrendatarioNumeroDocumento),
    );
    this.formCuota.set(a.cuotaMensual);
    this.formFechaInicio.set(a.fechaInicio);
    this.formFechaFin.set(a.fechaFin ?? '');
    this.formEstado.set(a.estadoContrato);
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

    const casa = separarLlaveCasa(this.formCasa());
    const propietario = separarLlavePersona(this.formPropietario());
    const arrendatario = separarLlavePersona(this.formArrendatario());
    const datos = {
      cuotaMensual: this.formCuota()!,
      fechaInicio: this.formFechaInicio(),
      fechaFin: this.formFechaFin() || null,
      estadoContrato: this.formEstado(),
      arrendatarioTipoDocumento: arrendatario.tipo,
      arrendatarioNumeroDocumento: arrendatario.numero,
      casaManzana: casa.manzana,
      casaNumero: casa.numero,
      propietarioTipoDocumento: propietario.tipo,
      propietarioNumeroDocumento: propietario.numero,
    };
    const creando = this.modoModal() === 'crear';
    const peticion = creando
      ? this.arriendoService.postCrear(datos)
      : this.arriendoService.putActualizar({ idContrato: this.idEditando, ...datos });

    this.guardando.set(true);
    peticion.subscribe({
      next: () => {
        this.guardando.set(false);
        this.modoModal.set(null);
        this.notificacionService.exito(
          `Contrato de ${nombreCasa(casa.manzana, casa.numero)} ${creando ? 'registrado' : 'actualizado'} correctamente.`,
        );
        this.cargarArriendos();
      },
      error: (err) => {
        this.guardando.set(false);
        this.errorFormulario.set(
          extraerError(err, `No se pudo ${creando ? 'registrar' : 'actualizar'} el contrato.`),
        );
      },
    });
  }

  /** Pide confirmación antes de eliminar. */
  pedirEliminar(a: ArriendoModel): void {
    this.arriendoAEliminar.set(a);
  }

  /** Cierra el diálogo sin eliminar. */
  cancelarEliminar(): void {
    this.arriendoAEliminar.set(null);
  }

  /** Envía el DELETE del contrato confirmado. */
  confirmarEliminar(): void {
    const a = this.arriendoAEliminar();
    if (!a) return;
    this.eliminando.set(true);
    this.arriendoService.deleteEliminar(a.idContrato).subscribe({
      next: () => {
        this.eliminando.set(false);
        this.arriendoAEliminar.set(null);
        this.notificacionService.exito('Contrato eliminado correctamente.');
        this.cargarArriendos();
      },
      error: (err) => {
        this.eliminando.set(false);
        this.arriendoAEliminar.set(null);
        this.notificacionService.error(
          extraerError(err, 'No se pudo eliminar el contrato.'),
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
    if (!this.formCasa()) return 'Elige la casa.';
    if (!this.formPropietario()) return 'Elige el propietario que firma.';
    if (!this.formArrendatario()) return 'Elige el arrendatario.';
    const cuota = this.formCuota();
    if (cuota === null || cuota <= 0) return 'La cuota mensual debe ser mayor que 0.';
    if (!this.formFechaInicio()) return 'Escribe la fecha de inicio.';
    if (this.formFechaFin() && this.formFechaFin() < this.formFechaInicio()) {
      return 'La fecha de fin no puede ser anterior a la de inicio.';
    }
    return null;
  }
}
