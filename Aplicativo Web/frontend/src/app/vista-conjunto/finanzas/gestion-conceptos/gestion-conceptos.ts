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
 * Los conceptos que usa el sistema (Sanción, Salón comunal, Parqueadero adicional) se pueden
 * editar (descripción y valor), pero no renombrar ni eliminar. Un concepto que ya se usó en una
 * línea de factura tampoco se puede eliminar: eso lo valida el back.
 */
@Component({
  selector: 'app-gestion-conceptos',
  imports: [CurrencyPipe, ModalFormulario, DialogoConfirmar],
  templateUrl: './gestion-conceptos.html',
  styleUrl: './gestion-conceptos.css',
})
export class GestionConceptos implements OnInit {
  private conceptoService = inject(ConceptoCobroService);
  private notificacionService = inject(NotificacionService);

  protected readonly conceptos = signal<ConceptoCobroModel[]>([]);
  protected readonly cargando = signal(true);
  protected readonly errorCarga = signal(false);
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

  /** Constantes y funciones para la plantilla. */
  protected readonly esProtegido = esConceptoProtegido;
  protected readonly maxNombre = MAX_NOMBRE_CONCEPTO;
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

  protected readonly formulario = signal<FormularioConcepto>({ ...FORMULARIO_VACIO });
  protected readonly guardando = signal(false);
  protected readonly errorFormulario = signal<string | null>(null);

  /** Concepto pendiente de confirmar para eliminar. */
  protected readonly conceptoAEliminar = signal<ConceptoCobroModel | null>(null);
  protected readonly eliminando = signal(false);

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

  /** Valida lo mismo que el back y guarda (crear o editar). */
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

    // Solo dígitos: así se evita que "150.000" se lea como 150.
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
        // Aquí llegan, por ejemplo, el nombre repetido (409) o el intento de renombrar un protegido.
        this.errorFormulario.set(extraerError(err, 'No se pudo guardar el concepto de cobro.'));
      },
    });
  }

  /** Confirma y elimina el concepto. */
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
        // Si el concepto ya se usó en una factura, el back responde 409 con el motivo.
        this.notificacionService.error(
          extraerError(err, 'No se pudo eliminar el concepto de cobro.'),
          'No se pudo eliminar',
        );
      },
    });
  }
}
