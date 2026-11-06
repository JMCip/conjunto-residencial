package co.edu.unbosque.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.util.List;

import co.edu.unbosque.dao.ConceptoCobroDAO;
import co.edu.unbosque.exception.ConflictoException;
import co.edu.unbosque.exception.DatoInvalidoException;
import co.edu.unbosque.exception.RecursoNoEncontradoException;
import co.edu.unbosque.model.ConceptoCobro;

/**
 * Lógica de negocio de los conceptos de cobro.
 * <p>
 * Valida los datos recibidos y las reglas del conjunto antes de llamar al DAO, y comunica los
 * errores con las excepciones del paquete {@code exception}.
 */
public class ConceptoCobroService {

	/** Conceptos que el sistema usa para cobrar y que no se pueden renombrar ni eliminar. */
	private static final List<String> PROTEGIDOS = List.of("Sanción", "Salón comunal", "Parqueadero adicional", "Administración");

	/** Longitud máxima del nombre. */
	private static final int MAX_NOMBRE = 100;
	/** Longitud máxima de la descripción. */
	private static final int MAX_DESCRIPCION = 255;
	/** Valor máximo que admite la columna {@code NUMERIC(12,2)}. */
	private static final BigDecimal MAXIMO = new BigDecimal("9999999999.99");

	/** Acceso a datos de los conceptos de cobro. */
	private final ConceptoCobroDAO conceptoCobroDao = new ConceptoCobroDAO();

	/**
	 * Lista los conceptos de cobro registrados.
	 *
	 * @return lista de conceptos de cobro; vacía si no hay registros
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 */
	public List<ConceptoCobro> listar() throws SQLException {
		return conceptoCobroDao.listar();
	}

	/**
	 * Registra un concepto de cobro con nombre único.
	 *
	 * @param c concepto que se va a crear
	 * @return identificador del concepto creado
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 * @throws DatoInvalidoException si algún dato no cumple las validaciones
	 * @throws ConflictoException si ya existe un concepto con ese nombre
	 */
	public int crear(ConceptoCobro c) throws SQLException, DatoInvalidoException, ConflictoException {
		String nombre = validarNombre(c.getNombre());
		String descripcion = validarDescripcion(c.getDescripcion());
		BigDecimal valorBase = validarValorBase(c.getValorBase());

		if (conceptoCobroDao.existeNombre(nombre, 0)) {
			throw new ConflictoException("Ya existe un concepto de cobro llamado '" + nombre + "'");
		}
		return conceptoCobroDao.insertar(nombre, descripcion, valorBase);
	}

	/**
	 * Actualiza un concepto de cobro. Los conceptos protegidos no se pueden renombrar.
	 *
	 * @param id identificador del concepto
	 * @param cambios nombre, descripción y valor base nuevos
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 * @throws DatoInvalidoException si algún dato no cumple las validaciones
	 * @throws RecursoNoEncontradoException si el concepto no existe
	 * @throws ConflictoException si se renombra un concepto protegido o el nombre ya está en uso
	 */
	public void actualizar(int id, ConceptoCobro cambios) throws SQLException, DatoInvalidoException,
			RecursoNoEncontradoException, ConflictoException {
		ConceptoCobro actual = conceptoCobroDao.buscarPorId(id);
		if (actual == null) {
			throw new RecursoNoEncontradoException("Concepto de cobro no encontrado");
		}
		String nombre = validarNombre(cambios.getNombre());
		String descripcion = validarDescripcion(cambios.getDescripcion());
		BigDecimal valorBase = validarValorBase(cambios.getValorBase());

		if (esProtegido(actual.getNombre()) && !actual.getNombre().equalsIgnoreCase(nombre)) {
			throw new ConflictoException("El concepto '" + actual.getNombre()
					+ "' lo usa el sistema para cobrar sanciones, reservas o celdas adicionales y no se puede renombrar");
		}
		if (conceptoCobroDao.existeNombre(nombre, id)) {
			throw new ConflictoException("Ya existe otro concepto de cobro llamado '" + nombre + "'");
		}
		if (!conceptoCobroDao.actualizar(id, nombre, descripcion, valorBase)) {
			throw new RecursoNoEncontradoException("Concepto de cobro no encontrado");
		}
	}

	/**
	 * Elimina un concepto de cobro. No se eliminan los conceptos protegidos ni los que ya se usaron en
	 * facturas.
	 *
	 * @param id identificador del concepto
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 * @throws RecursoNoEncontradoException si el concepto no existe
	 * @throws ConflictoException si el concepto está protegido o ya se usó en líneas de factura
	 */
	public void eliminar(int id) throws SQLException, RecursoNoEncontradoException, ConflictoException {
		ConceptoCobro actual = conceptoCobroDao.buscarPorId(id);
		if (actual == null) {
			throw new RecursoNoEncontradoException("Concepto de cobro no encontrado");
		}
		if (esProtegido(actual.getNombre())) {
			throw new ConflictoException("El concepto '" + actual.getNombre()
					+ "' lo usa el sistema para cobrar sanciones, reservas o celdas adicionales y no se puede eliminar");
		}
		try {
			if (!conceptoCobroDao.eliminar(id)) {
				throw new RecursoNoEncontradoException("Concepto de cobro no encontrado");
			}
		} catch (SQLException e) {
			if ("23503".equals(e.getSQLState())) {
				throw new ConflictoException("El concepto ya se usó en líneas de factura y no se puede eliminar");
			}
			throw e;
		}
	}

	/**
	 * Indica si el nombre corresponde a un concepto protegido, sin distinguir mayúsculas.
	 *
	 * @param nombre nombre del concepto
	 * @return {@code true} si el concepto está protegido
	 */
	private boolean esProtegido(String nombre) {
		return PROTEGIDOS.stream().anyMatch(p -> p.equalsIgnoreCase(nombre.trim()));
	}

	/**
	 * Valida el nombre y le quita los espacios al inicio y al final.
	 *
	 * @param nombre nombre recibido
	 * @return nombre limpio
	 * @throws DatoInvalidoException si algún dato no cumple las validaciones
	 */
	private String validarNombre(String nombre) throws DatoInvalidoException {
		if (nombre == null || nombre.isBlank()) {
			throw new DatoInvalidoException("nombre es obligatorio");
		}
		String limpio = nombre.trim();
		if (limpio.length() > MAX_NOMBRE) {
			throw new DatoInvalidoException("nombre no puede superar " + MAX_NOMBRE + " caracteres");
		}
		return limpio;
	}

	/**
	 * Valida la descripción y le quita los espacios al inicio y al final.
	 *
	 * @param descripcion descripción recibida
	 * @return descripción limpia
	 * @throws DatoInvalidoException si algún dato no cumple las validaciones
	 */
	private String validarDescripcion(String descripcion) throws DatoInvalidoException {
		if (descripcion == null || descripcion.isBlank()) {
			throw new DatoInvalidoException("descripcion es obligatoria");
		}
		String limpia = descripcion.trim();
		if (limpia.length() > MAX_DESCRIPCION) {
			throw new DatoInvalidoException("descripcion no puede superar " + MAX_DESCRIPCION + " caracteres");
		}
		return limpia;
	}

	/**
	 * Valida que el valor base sea positivo y quepa en la columna, redondeado a 2 decimales.
	 *
	 * @param valor valor recibido
	 * @return valor redondeado
	 * @throws DatoInvalidoException si algún dato no cumple las validaciones
	 */
	private BigDecimal validarValorBase(BigDecimal valor) throws DatoInvalidoException {
		if (valor == null || valor.signum() <= 0) {
			throw new DatoInvalidoException("valorBase debe ser mayor que cero");
		}
		BigDecimal v = valor.setScale(2, RoundingMode.HALF_UP);
		if (v.compareTo(MAXIMO) > 0) {
			throw new DatoInvalidoException("valorBase es demasiado grande");
		}
		return v;
	}
}