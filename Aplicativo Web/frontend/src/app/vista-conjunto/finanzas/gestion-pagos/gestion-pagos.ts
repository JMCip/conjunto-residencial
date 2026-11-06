import { Component, computed, inject, OnInit, output, signal } from '@angular/core';
import { CurrencyPipe, DatePipe } from '@angular/common';
import { forkJoin } from 'rxjs';
import { METODOS_PAGO, MetodoPago, PagoModel } from '../../../models/pago.model';
import { FacturaModel } from '../../../models/factura.model';
import {
  llavePersona,
  nombrePersona,
  PersonaModel,
  separarLlavePersona,
} from '../../../models/persona.model';
import { nombreCasa } from '../../../models/casa.model';
import { PagoService } from '../../../services/pago.service';
import { FacturaService } from '../../../services/factura.service';
import { PersonaService } from '../../../services/persona.service';
import { NotificacionService } from '../../../services/notificacion.service';
import { extraerError } from '../../../compartido/extraer-error';
import { hoyISO } from '../../../compartido/fechas';
import { ModalFormulario } from '../../../compartido/modal-formulario/modal-formulario';

/** Un pago listo para la tabla: el pago más los textos de su factura y de quien pagó. */
interface PagoVista extends PagoModel {
  textoFactura: string;
  textoPersona: string;
  textoMetodo: string;
}

/** Campos del formulario (todo como texto; el valor se convierte a número al guardar). */
interface FormularioPago {
  factura: string;
  persona: string;
  valor: string;
  fecha: string;
  metodoPago: MetodoPago | '';
}

/**
 * Pasa un texto a minúsculas y sin tildes, para que "maria" encuentre "María".
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
 * @component GestionPagos
 * @description Pestaña "Pagos" de Finanzas: lista los pagos y permite registrar uno nuevo.
 * Los pagos no se editan ni se eliminan (el backend no tiene PUT ni DELETE).
 */
@Component({
  selector: 'app-gestion-pagos',
  imports: [CurrencyPipe, DatePipe, ModalFormulario],
  templateUrl: './gestion-pagos.html',
  styleUrl: './gestion-pagos.css',
})
export class GestionPagos implements OnInit {
  /** Servicio de pagos. */
  private pagoService = inject(PagoService);
  /** Servicio de facturas. */
  private facturaService = inject(FacturaService);
  /** Servicio de personas y roles. */
  private personaService = inject(PersonaService);
  /** Servicio de avisos flotantes. */
  private notificacionService = inject(NotificacionService);

  /** Lista de pagos consultada al backend. */
  protected readonly pagos = signal<PagoModel[]>([]);
  /** Lista de facturas consultada al backend. */
  protected readonly facturas = signal<FacturaModel[]>([]);
  /** Lista de personas consultada al backend. */
  protected readonly personas = signal<PersonaModel[]>([]);
  /** true mientras se consulta la información. */
  protected readonly cargando = signal(true);
  /** true si la consulta de la información falló. */
  protected readonly errorCarga = signal(false);
  /** Texto escrito en el buscador. */
  protected readonly busqueda = signal('');

  /** Facturas por número, para buscar la de cada pago sin recorrer la lista cada vez. */
  private readonly facturaPorNumero = computed(
    () => new Map(this.facturas().map((f) => [f.numeroFactura, f])),
  );

  /** Nombre completo por llave "tipo|número". */
  private readonly nombrePorLlave = computed(
    () =>
      new Map(
        this.personas().map((p) => [
          llavePersona(p.tipoDocumento, p.numeroDocumento),
          nombrePersona(p),
        ]),
      ),
  );

  /** Pagos con sus textos, del más reciente al más antiguo. */
  protected readonly pagosVista = computed<PagoVista[]>(() =>
    this.pagos()
      .map((p) => {
        const f = this.facturaPorNumero().get(p.numeroFactura);
        return {
          ...p,
          textoFactura: f
            ? `N.º ${p.numeroFactura} · ${nombreCasa(f.casaManzana, f.casaNumero)}`
            : `N.º ${p.numeroFactura}`,
          textoPersona:
            this.nombrePorLlave().get(
              llavePersona(p.personaTipoDocumento, p.personaNumeroDocumento),
            ) ?? `${p.personaTipoDocumento} ${p.personaNumeroDocumento}`,
          textoMetodo:
            METODOS_PAGO.find((m) => m.valor === p.metodoPago)?.etiqueta ?? p.metodoPago,
        };
      })
      .sort((a, b) => b.fecha.localeCompare(a.fecha) || b.idPago - a.idPago),
  );

  /** Pagos visibles según la búsqueda (factura, casa, persona, documento o método). */
  protected readonly pagosFiltrados = computed(() => {
    const texto = normalizar(this.busqueda());
    if (!texto) return this.pagosVista();
    return this.pagosVista().filter((p) =>
      normalizar(
        `${p.textoFactura} ${p.textoPersona} ${p.personaNumeroDocumento} ${p.textoMetodo}`,
      ).includes(texto),
    );
  });

  /** "5 pagos" o "1 pago de 5". */
  protected readonly textoConteo = computed(() => {
    const visibles = this.pagosFiltrados().length;
    const total = this.pagos().length;
    const palabra = visibles === 1 ? 'pago' : 'pagos';
    return visibles === total ? `${visibles} ${palabra}` : `${visibles} ${palabra} de ${total}`;
  });

  /** Suma de los pagos visibles (para el pie de la tabla). */
  protected readonly totalVisible = computed(() =>
    this.pagosFiltrados().reduce((total, p) => total + p.valor, 0),
  );

  /** Avisa a Finanzas que se registró un pago (la factura cambió de estado). */
  readonly registrado = output<void>();

  /** Opciones del selector de medio de pago (constante del modelo). */
  protected readonly metodos = METODOS_PAGO;
  /** Función del modelo para armar la llave de una persona en la plantilla. */
  protected readonly llavePersona = llavePersona;
  /** Función del modelo para mostrar el nombre de una persona en la plantilla. */
  protected readonly nombrePersona = nombrePersona;
  /** Función del modelo para mostrar el nombre de una casa en la plantilla. */
  protected readonly nombreCasa = nombreCasa;
  /** Fecha de hoy (yyyy-MM-dd), tope de los campos de fecha. */
  protected readonly hoy = hoyISO();

  /** Solo las facturas que se pueden pagar: no PAGADA y con saldo. */
  protected readonly facturasConSaldo = computed(() =>
    this.facturas()
      .filter((f) => f.estado !== 'PAGADA' && (f.saldo ?? 0) > 0)
      .sort((a, b) => a.numeroFactura - b.numeroFactura),
  );

  /** true si el modal del formulario está abierto. */
  protected readonly modalAbierto = signal(false);
  /** Valores actuales del formulario del modal. */
  protected readonly formulario = signal<FormularioPago>({
    factura: '',
    persona: '',
    valor: '',
    fecha: '',
    metodoPago: '',
  });
  /** true mientras se envía el formulario al backend. */
  protected readonly guardando = signal(false);
  /** Mensaje de validación del formulario (null si no hay error). */
  protected readonly errorFormulario = signal<string | null>(null);

  /** La factura elegida en el formulario (para el saldo y la fecha mínima). */
  protected readonly facturaElegida = computed(
    () => this.facturaPorNumero().get(Number(this.formulario().factura)) ?? null,
  );

  /** Consulta los datos al abrir la pantalla. */
  ngOnInit(): void {
    this.cargarDatos();
  }

  /** Consulta pagos, facturas y personas a la vez. */
  cargarDatos(): void {
    this.cargando.set(true);
    this.errorCarga.set(false);
    forkJoin({
      pagos: this.pagoService.getTodos(),
      facturas: this.facturaService.getTodos(),
      personas: this.personaService.getTodos(),
    }).subscribe({
      next: ({ pagos, facturas, personas }) => {
        this.pagos.set(pagos);
        this.facturas.set(facturas);
        this.personas.set(personas);
        this.cargando.set(false);
      },
      error: (err) => {
        this.errorCarga.set(true);
        this.cargando.set(false);
        this.notificacionService.error(
          extraerError(err, 'No se pudieron consultar los pagos.'),
          'Error de conexión',
        );
      },
    });
  }

  /** Abre el modal con la fecha de hoy propuesta. */
  abrirRegistrar(): void {
    this.formulario.set({ factura: '', persona: '', valor: '', fecha: this.hoy, metodoPago: '' });
    this.errorFormulario.set(null);
    this.modalAbierto.set(true);
  }

  /**
   * Actualiza un campo del formulario.
   * @param campo Nombre del campo.
   * @param valor Lo que eligió o escribió el usuario.
   */
  cambiarCampo(campo: keyof FormularioPago, valor: string): void {
    this.formulario.update((f) => ({ ...f, [campo]: valor }));
  }

  /**
   * Al elegir factura: propone su saldo completo como valor.
   * @param numero numeroFactura como texto.
   */
  elegirFactura(numero: string): void {
    const f = this.facturaPorNumero().get(Number(numero));
    this.formulario.update((form) => ({
      ...form,
      factura: numero,
      valor: f?.saldo ? String(f.saldo) : '',
    }));
  }

  /**
   * Valida lo mismo que el backend y registra el pago. El valor acepta hasta 2 decimales con punto
   * o coma, porque una reserva de duración no exacta puede dejar saldos con centavos.
   */
  guardar(): void {
    const form = this.formulario();
    const factura = this.facturaElegida();
    const textoValor = form.valor.trim().replace(',', '.');
    const valor = Number(textoValor);

    if (!factura) {
      this.errorFormulario.set('Elige la factura que se paga.');
      return;
    }
    if (!form.persona) {
      this.errorFormulario.set('Elige la persona que paga.');
      return;
    }
    if (!/^\d+(\.\d{1,2})?$/.test(textoValor) || valor <= 0) {
      this.errorFormulario.set(
        'El valor debe ser un número mayor a 0, sin separador de miles (máximo 2 decimales).',
      );
      return;
    }
    if (valor > (factura.saldo ?? 0)) {
      this.errorFormulario.set('El valor supera el saldo de la factura.');
      return;
    }
    if (!form.fecha) {
      this.errorFormulario.set('Indica la fecha del pago.');
      return;
    }
    if (form.fecha < factura.fechaEmision) {
      this.errorFormulario.set('La fecha no puede ser anterior a la emisión de la factura.');
      return;
    }
    if (form.fecha > this.hoy) {
      this.errorFormulario.set('La fecha no puede ser futura.');
      return;
    }
    if (!form.metodoPago) {
      this.errorFormulario.set('Elige el método de pago.');
      return;
    }

    const { tipo, numero } = separarLlavePersona(form.persona);
    this.errorFormulario.set(null);
    this.guardando.set(true);

    this.pagoService
      .registrar({
        fecha: form.fecha,
        valor,
        metodoPago: form.metodoPago,
        numeroFactura: factura.numeroFactura,
        personaTipoDocumento: tipo,
        personaNumeroDocumento: numero,
      })
      .subscribe({
        next: (r) => {
          this.guardando.set(false);
          this.modalAbierto.set(false);
          this.notificacionService.exito(
            r.saldo > 0
              ? `La factura N.º ${factura.numeroFactura} quedó ${r.estadoFactura}. ` +
              `Saldo: $ ${r.saldo.toLocaleString('es-CO')}.`
              : `La factura N.º ${factura.numeroFactura} quedó PAGADA.`,
            'Pago registrado',
          );
          this.cargarDatos();
          this.registrado.emit();
        },
        error: (err) => {
          this.guardando.set(false);
          this.errorFormulario.set(extraerError(err, 'No se pudo registrar el pago.'));
        },
      });
  }
}
