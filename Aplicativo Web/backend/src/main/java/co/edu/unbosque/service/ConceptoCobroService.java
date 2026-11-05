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

public class ConceptoCobroService {

	private static final List<String> PROTEGIDOS = List.of("Sanción", "Salón comunal", "Parqueadero adicional", "Administración");

	private static final int MAX_NOMBRE = 100;
	private static final int MAX_DESCRIPCION = 255;
	private static final BigDecimal MAXIMO = new BigDecimal("9999999999.99");

	private final ConceptoCobroDAO conceptoCobroDao = new ConceptoCobroDAO();

	public List<ConceptoCobro> listar() throws SQLException {
		return conceptoCobroDao.listar();
	}

	public int crear(ConceptoCobro c) throws SQLException, DatoInvalidoException, ConflictoException {
		String nombre = validarNombre(c.getNombre());
		String descripcion = validarDescripcion(c.getDescripcion());
		BigDecimal valorBase = validarValorBase(c.getValorBase());

		if (conceptoCobroDao.existeNombre(nombre, 0)) {
			throw new ConflictoException("Ya existe un concepto de cobro llamado '" + nombre + "'");
		}
		return conceptoCobroDao.insertar(nombre, descripcion, valorBase);
	}

	
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

	private boolean esProtegido(String nombre) {
		return PROTEGIDOS.stream().anyMatch(p -> p.equalsIgnoreCase(nombre.trim()));
	}

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