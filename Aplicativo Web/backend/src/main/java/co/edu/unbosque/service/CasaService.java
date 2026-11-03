package co.edu.unbosque.service;

import co.edu.unbosque.dao.CasaDao;
import co.edu.unbosque.exception.DatoInvalidoException;
import co.edu.unbosque.exception.RecursoNoEncontradoException;
import co.edu.unbosque.model.Casa;
import java.util.Set;
import java.sql.SQLException;
import java.util.List;

public class CasaService {

	private final CasaDao casaDao = new CasaDao();
	private static final Set<String> ESTADOS = Set.of("HABITADA", "ARRENDADA", "DESOCUPADA");

	public List<Casa> listar() throws SQLException {
		return casaDao.listar();
	}

	public void crear(Casa c) throws SQLException, DatoInvalidoException {
		if (c.getNumero() <= 0 || c.getManzana() <= 0) {
			throw new DatoInvalidoException("numero y manzana deben ser mayores que 0");
		}
		validarDatos(c);
		casaDao.insertar(c);
	}

	public void actualizar(Casa c) throws SQLException, DatoInvalidoException, RecursoNoEncontradoException {
		validarDatos(c);
		if (!casaDao.actualizar(c)) {
			throw new RecursoNoEncontradoException("Casa no encontrada");
		}
	}

	public void eliminar(int numero, int manzana) throws SQLException, RecursoNoEncontradoException {
		if (!casaDao.eliminar(numero, manzana)) {
			throw new RecursoNoEncontradoException("Casa no encontrada");
		}
	}
	
	private void validarDatos(Casa c) throws DatoInvalidoException {
		if (c.getArea() == null || c.getEstado() == null || c.getEstado().isBlank()) {
			throw new DatoInvalidoException("area y estado son obligatorios");
		}
		if (c.getArea().signum() <= 0) {
			throw new DatoInvalidoException("area debe ser mayor que 0");
		}
		c.setEstado(c.getEstado().trim().toUpperCase());
		if (!ESTADOS.contains(c.getEstado())) {
			throw new DatoInvalidoException("estado debe ser HABITADA, ARRENDADA o DESOCUPADA");
		}
		if (c.getNumeroHabitaciones() != null && c.getNumeroHabitaciones() < 0) {
			throw new DatoInvalidoException("numeroHabitaciones no puede ser negativo");
		}
	}
	
}