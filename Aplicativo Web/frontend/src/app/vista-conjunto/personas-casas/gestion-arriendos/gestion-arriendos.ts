import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { CurrencyPipe, DatePipe } from '@angular/common';
import { forkJoin } from 'rxjs';
import { ArriendoModel } from '../../../models/arriendo.model';
import { CasaModel, nombreCasa } from '../../../models/casa.model';
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
import { extraerError } from '../../../compartido/extraer-error';
import { ModalFormulario } from '../../../compartido/modal-formulario/modal-formulario';
import { DialogoConfirmar } from '../../../compartido/dialogo-confirmar/dialogo-confirmar';

/** Estados del contrato*/
const ESTADOS_CONTRATO: ArriendoModel['estadoContrato'][] = ['VIGENTE', 'FINALIZADO'];

/** Campos del formulario (todo como texto). */
interface FormularioArriendo {
  casa: string;
  propietario: string;
  arrendatario: string;
  cuotaMensual: string;
  fechaInicio: string;
  fechaFin: string;
  estadoContrato: ArriendoModel['estadoContrato'];
}

/** Valores iniciales del formulario (todo vacío). */
const FORMULARIO_VACIO: FormularioArriendo = {
  casa: '',
  propietario: '',
  arrendatario: '',
  cuotaMensual: '',
  fechaInicio: '',
  fechaFin: '',
  estadoContrato: 'VIGENTE',
};

/**
 * @component GestionArriendos
 * @description Pestaña Arriendos: contratos entre un propietario y un arrendatario sobre una
 * casa. La base solo permite un contrato VIGENTE por casa (índice único).
 */
@Component({
  selector: 'app-gestion-arriendos',
  imports: [CurrencyPipe, DatePipe, ModalFormulario, DialogoConfirmar],
  templateUrl: './gestion-arriendos.html',
  styleUrl: './gestion-arriendos.css',
})
export class GestionArriendos implements OnInit {
  /** Servicio de contratos de arriendo. */
  private arriendoService = inject(ArriendoService);
  /** Servicio de casas. */
  private casaService = inject(CasaService);
  /** Servicio de personas y roles. */
  private personaService = inject(PersonaService);
  /** Servicio de avisos flotantes. */
  private notificacionService = inject(NotificacionService);

  /** Contratos obtenidos del backend. */
  protected readonly arriendos = signal<ArriendoModel[]>([]);

  /** Casas, propietarios y arrendatarios para los selectores. */
  protected readonly casas = signal<CasaModel[]>([]);
  /** Lista de propietarios consultada al backend. */
  protected readonly propietarios = signal<PersonaModel[]>([]);
  /** Lista de arrendatarios consultada al backend. */
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

  /** true mientras se consulta la información. */
  protected readonly cargando = signal(true);
  /** true si la consulta de la información falló. */
  protected readonly errorCarga = signal(false);
  /** Texto escrito en el buscador. */
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

  /** Función del modelo para mostrar el nombre de una casa en la plantilla. */
  protected readonly nombreCasa = nombreCasa;
  /** Función del modelo para armar la llave de una persona en la plantilla. */
  protected readonly llavePersona = llavePersona;
  /** Función del modelo para mostrar el nombre de una persona en la plantilla. */
  protected readonly nombrePersona = nombrePersona;
  /** Estados de contrato para el selector. */
  protected readonly estadosContrato = ESTADOS_CONTRATO;

  /** Modal de crear/editar. */
  protected readonly modalAbierto = signal(false);

  /** Contrato que se edita (null = crear). */
  protected readonly editando = signal<ArriendoModel | null>(null);

  /** Valores actuales del formulario del modal. */
  protected readonly formulario = signal<FormularioArriendo>({ ...FORMULARIO_VACIO });
  /** true mientras se envía el formulario al backend. */
  protected readonly guardando = signal(false);
  /** Mensaje de validación del formulario (null si no hay error). */
  protected readonly errorFormulario = signal<string | null>(null);

  /** Contrato pendiente de confirmar para eliminar. */
  protected readonly arriendoAEliminar = signal<ArriendoModel | null>(null);
  /** true mientras se envía la eliminación al backend. */
  protected readonly eliminando = signal(false);

  /** Consulta los datos al abrir la pantalla. */
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

  /**
   * Llave "manzana|numero" de una casa (para el selector).
   * @param manzana Manzana de la casa.
   * @param numero Número de la casa.
   */
  llaveCasa(manzana: number, numero: number): string {
    return `${manzana}|${numero}`;
  }

  /** Abre el modal vacío para crear. */
  abrirCrear(): void {
    this.editando.set(null);
    this.formulario.set({ ...FORMULARIO_VACIO });
    this.errorFormulario.set(null);
    this.modalAbierto.set(true);
  }

  /**
   * Abre el modal con los datos del contrato.
   * @param a Contrato a editar.
   */
  abrirEditar(a: ArriendoModel): void {
    this.editando.set(a);
    this.formulario.set({
      casa: this.llaveCasa(a.casaManzana, a.casaNumero),
      propietario: llavePersona(a.propietarioTipoDocumento, a.propietarioNumeroDocumento),
      arrendatario: llavePersona(a.arrendatarioTipoDocumento, a.arrendatarioNumeroDocumento),
      cuotaMensual: a.cuotaMensual.toString(),
      fechaInicio: a.fechaInicio,
      fechaFin: a.fechaFin ?? '',
      estadoContrato: a.estadoContrato,
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
  cambiarCampo(campo: keyof FormularioArriendo, valor: string): void {
    this.formulario.update((f) => ({ ...f, [campo]: valor }));
  }

  /** Valida y guarda (crear o editar). */
  guardar(): void {
    const f = this.formulario();
    if (!f.casa || !f.propietario || !f.arrendatario) {
      this.errorFormulario.set('Elige la casa, el propietario y el arrendatario.');
      return;
    }
    const cuota = Number(f.cuotaMensual);
    if (!f.cuotaMensual.trim() || !(cuota > 0)) {
      this.errorFormulario.set('La cuota mensual debe ser mayor que 0.');
      return;
    }
    if (!f.fechaInicio) {
      this.errorFormulario.set('La fecha de inicio es obligatoria.');
      return;
    }
    if (f.fechaFin && f.fechaFin < f.fechaInicio) {
      this.errorFormulario.set('La fecha de fin no puede ser anterior a la de inicio.');
      return;
    }

    const fechaFin = f.fechaFin || null;
    const actual = this.editando();
    this.errorFormulario.set(null);
    this.guardando.set(true);

    let peticion;
    if (actual) {
      peticion = this.arriendoService.actualizar({
        ...actual,
        cuotaMensual: cuota,
        fechaInicio: f.fechaInicio,
        fechaFin,
        estadoContrato: f.estadoContrato,
      });
    } else {
      const [manzana, numero] = f.casa.split('|').map(Number);
      const prop = separarLlavePersona(f.propietario);
      const arr = separarLlavePersona(f.arrendatario);
      peticion = this.arriendoService.crear({
        cuotaMensual: cuota,
        fechaInicio: f.fechaInicio,
        fechaFin,
        estadoContrato: f.estadoContrato,
        arrendatarioTipoDocumento: arr.tipo,
        arrendatarioNumeroDocumento: arr.numero,
        casaNumero: numero,
        casaManzana: manzana,
        propietarioTipoDocumento: prop.tipo,
        propietarioNumeroDocumento: prop.numero,
      });
    }

    peticion.subscribe({
      next: () => {
        this.guardando.set(false);
        this.modalAbierto.set(false);
        this.notificacionService.exito(
          actual ? 'Contrato actualizado.' : 'Contrato creado.',
          'Listo',
        );
        this.cargarArriendos();
      },
      error: (err) => {
        this.guardando.set(false);
        this.errorFormulario.set(extraerError(err, 'No se pudo guardar el contrato.'));
      },
    });
  }

  /** Confirma y elimina el contrato. */
  eliminar(): void {
    const a = this.arriendoAEliminar();
    if (!a) return;
    this.eliminando.set(true);
    this.arriendoService.eliminar(a.idContrato).subscribe({
      next: () => {
        this.eliminando.set(false);
        this.arriendoAEliminar.set(null);
        this.notificacionService.exito('Contrato eliminado.', 'Listo');
        this.cargarArriendos();
      },
      error: (err) => {
        this.eliminando.set(false);
        this.arriendoAEliminar.set(null);
        this.notificacionService.error(
          extraerError(err, 'No se pudo eliminar el contrato.'),
          'No se pudo eliminar',
        );
      },
    });
  }
}
