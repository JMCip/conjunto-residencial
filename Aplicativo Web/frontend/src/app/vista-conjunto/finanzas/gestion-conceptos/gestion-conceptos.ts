import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { CurrencyPipe } from '@angular/common';
import {
  ConceptoCobroModel,
  DatosConcepto,
  esConceptoProtegido,
  MAX_DESCRIPCION_CONCEPTO,
  MAX_NOMBRE_CONCEPTO,
  MAX_VALOR_BASE_CONCEPTO,
} from '../../../models/concepto-cobro.model';
import { ConceptoCobroService } from '../../../services/concepto-cobro.service';
import { NotificacionService } from '../../../services/notificacion.service';
import { extraerError } from '../../../compartido/extraer-error';
import { ModalFormulario } from '../../../compartido/modal-formulario/modal-formulario';
import { DialogoConfirmar } from '../../../compartido/dialogo-confirmar/dialogo-confirmar';

/** Campos del formulario (todo como texto; el valor se convierte a número al guardar). */
interface FormularioConcepto {
  nombre: string;
  descripcion: string;
  valorBase: string;
}

/** Valores iniciales del formulario (todo vacío). */
const FORMULARIO_VACIO: FormularioConcepto = {
  nombre: '',
  descripcion: '',
  valorBase: '',
};

/**
 * Pasa un texto a minúsculas y sin tildes, para que "salon" encuentre "Salón comunal".
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
 * @component GestionConceptos
 * @description Pestaña "Conceptos de cobro" de Finanzas: CRUD del catálogo de conceptos que se
 * pueden cobrar en una factura (nombre, descripción y valor base).
 * Los conceptos que usa el sistema (Administración, Sanción, Salón comunal y Parqueadero adicional)
 * se pueden editar (descripción y valor), pero no renombrar ni eliminar. Un concepto que ya se usó en una
 * línea de factura tampoco se puede eliminar: eso lo valida el backend.
 */
@Component({
  selector: 'app-gestion-conceptos',
  imports: [CurrencyPipe, ModalFormulario, DialogoConfirmar],
  templateUrl: './gestion-conceptos.html',
  styleUrl: './gestion-conceptos.css',
})
export class GestionConceptos implements OnInit {
  /** Servicio del catálogo de conceptos de cobro. */
  private conceptoService = inject(ConceptoCobroService);
  /** Servicio de avisos flotantes. */
  private notificacionService = inject(NotificacionService);

  /** Lista de conceptos consultada al backend. */
  protected readonly conceptos = signal<ConceptoCobroModel[]>([]);
  /** true mientras se consulta la información. */
  protected readonly cargando = signal(true);
  /** true si la consulta de la información falló. */
  protected readonly errorCarga = signal(false);
  /** Texto escrito en el buscador. */
  protected readonly busqueda = signal('');

  /** Conceptos visibles según la búsqueda (nombre o descripción). */
  protected readonly conceptosFiltrados = computed(() => {
    const texto = normalizar(this.busqueda());
    if (!texto) return this.conceptos();
    return this.conceptos().filter((c) =>
      normalizar(`${c.nombre} ${c.descripcion}`).includes(texto),
    );
  });

  /** "4 conceptos" o "1 concepto de 4". */
  protected readonly textoConteo = computed(() => {
    const visibles = this.conceptosFiltrados().length;
    const total = this.conceptos().length;
    const palabra = visibles === 1 ? 'concepto' : 'conceptos';
    return visibles === total ? `${visibles} ${palabra}` : `${visibles} ${palabra} de ${total}`;
  });

  /** Función del modelo que indica si un concepto es protegido. */
  protected readonly esProtegido = esConceptoProtegido;
  /** Longitud máxima del nombre del concepto. */
  protected readonly maxNombre = MAX_NOMBRE_CONCEPTO;
  /** Longitud máxima de la descripción del concepto. */
  protected readonly maxDescripcion = MAX_DESCRIPCION_CONCEPTO;

  /** Modal de crear/editar. */
  protected readonly modalAbierto = signal(false);

  /** Concepto que se edita (null = crear). */
  protected readonly editando = signal<ConceptoCobroModel | null>(null);

  /** true si se está editando un concepto protegido: su nombre queda bloqueado. */
  protected readonly nombreBloqueado = computed(() => {
    const e = this.editando();
    return e !== null && esConceptoProtegido(e.nombre);
  });

  /** Valores actuales del formulario del modal. */
  protected readonly formulario = signal<FormularioConcepto>({ ...FORMULARIO_VACIO });
  /** true mientras se envía el formulario al backend. */
  protected readonly guardando = signal(false);
  /** Mensaje de validación del formulario (null si no hay error). */
  protected readonly errorFormulario = signal<string | null>(null);

  /** Concepto pendiente de confirmar para eliminar. */
  protected readonly conceptoAEliminar = signal<ConceptoCobroModel | null>(null);
  /** true mientras se envía la eliminación al backend. */
  protected readonly eliminando = signal(false);

  /** Consulta los datos al abrir la pantalla. */
  ngOnInit(): void {
    this.cargarConceptos();
  }

  /** Consulta los conceptos y actualiza el estado de la vista. */
  cargarConceptos(): void {
    this.cargando.set(true);
    this.errorCarga.set(false);
    this.conceptoService.getTodos().subscribe({
      next: (datos) => {
        this.conceptos.set(datos);
        this.cargando.set(false);
      },
      error: (err) => {
        this.errorCarga.set(true);
        this.cargando.set(false);
        this.notificacionService.error(
          extraerError(err, 'No se pudieron consultar los conceptos de cobro.'),
          'Error de conexión',
        );
      },
    });
  }

  /** Abre el modal vacío para crear. */
  abrirCrear(): void {
    this.editando.set(null);
    this.formulario.set({ ...FORMULARIO_VACIO });
    this.errorFormulario.set(null);
    this.modalAbierto.set(true);
  }

  /**
   * Abre el modal con los datos del concepto.
   * @param c Concepto a editar.
   */
  abrirEditar(c: ConceptoCobroModel): void {
    this.editando.set(c);
    this.formulario.set({
      nombre: c.nombre,
      descripcion: c.descripcion,
      valorBase: c.valorBase.toString(),
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
   * @param valor Texto escrito.
   */
  cambiarCampo(campo: keyof FormularioConcepto, valor: string): void {
    this.formulario.update((f) => ({ ...f, [campo]: valor }));
  }

  /**
   * Valida lo mismo que el backend y guarda (crear o editar). El valor base solo acepta dígitos, para
   * que "150.000" no se interprete como 150. Los errores del backend, como un nombre repetido (409),
   * se muestran en el modal.
   */
  guardar(): void {
    const f = this.formulario();
    const nombre = f.nombre.trim();
    const descripcion = f.descripcion.trim();
    const textoValor = f.valorBase.trim();

    if (!nombre) {
      this.errorFormulario.set('El nombre es obligatorio.');
      return;
    }
    if (nombre.length > MAX_NOMBRE_CONCEPTO) {
      this.errorFormulario.set(`El nombre no puede superar ${MAX_NOMBRE_CONCEPTO} caracteres.`);
      return;
    }
    if (!descripcion) {
      this.errorFormulario.set('La descripción es obligatoria.');
      return;
    }
    if (descripcion.length > MAX_DESCRIPCION_CONCEPTO) {
      this.errorFormulario.set(
        `La descripción no puede superar ${MAX_DESCRIPCION_CONCEPTO} caracteres.`,
      );
      return;
    }

    const valorBase = Number(textoValor);
    if (!/^\d+$/.test(textoValor) || valorBase <= 0) {
      this.errorFormulario.set(
        'El valor base debe ser un número entero mayor a 0 (sin puntos ni comas).',
      );
      return;
    }
    if (valorBase > MAX_VALOR_BASE_CONCEPTO) {
      this.errorFormulario.set('El valor base es demasiado grande.');
      return;
    }

    const datos: DatosConcepto = { nombre, descripcion, valorBase };
    const actual = this.editando();

    this.errorFormulario.set(null);
    this.guardando.set(true);
    const peticion = actual
      ? this.conceptoService.actualizar(actual.idConcepto, datos)
      : this.conceptoService.crear(datos);

    peticion.subscribe({
      next: () => {
        this.guardando.set(false);
        this.modalAbierto.set(false);
        this.notificacionService.exito(
          actual ? 'Concepto de cobro actualizado.' : 'Concepto de cobro creado.',
          'Listo',
        );
        this.cargarConceptos();
      },
      error: (err) => {
        this.guardando.set(false);
        this.errorFormulario.set(extraerError(err, 'No se pudo guardar el concepto de cobro.'));
      },
    });
  }

  /** Elimina el concepto confirmado. Si ya se usó en una factura, el backend responde 409 con el motivo. */
  eliminar(): void {
    const c = this.conceptoAEliminar();
    if (!c) return;
    this.eliminando.set(true);
    this.conceptoService.eliminar(c.idConcepto).subscribe({
      next: () => {
        this.eliminando.set(false);
        this.conceptoAEliminar.set(null);
        this.notificacionService.exito('Concepto de cobro eliminado.', 'Listo');
        this.cargarConceptos();
      },
      error: (err) => {
        this.eliminando.set(false);
        this.conceptoAEliminar.set(null);
        this.notificacionService.error(
          extraerError(err, 'No se pudo eliminar el concepto de cobro.'),
          'No se pudo eliminar',
        );
      },
    });
  }
}
