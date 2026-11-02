package co.edu.unbosque.service;

import co.edu.unbosque.dao.CasaDao;
import co.edu.unbosque.exception.DatoInvalidoException;
import co.edu.unbosque.exception.RecursoNoEncontradoException;
import co.edu.unbosque.model.Casa;

import java.sql.SQLException;
import java.util.List;

public class CasaService {

	private final CasaDao casaDao = new CasaDao();

	public List<Casa> listar() throws SQLException {
		return casaDao.listar();
	}

	public void crear(Casa c) throws SQLException, DatoInvalidoException {
		if (c.getArea() == null || c.getEstado() == null) {
			throw new DatoInvalidoException("area y estado son obligatorios");
		}
		casaDao.insertar(c);
	}

	public void actualizar(Casa c) throws SQLException, DatoInvalidoException, RecursoNoEncontradoException {
		if (c.getArea() == null || c.getEstado() == null) {
			throw new DatoInvalidoException("area y estado son obligatorios");
		}
		if (!casaDao.actualizar(c)) {
			throw new RecursoNoEncontradoException("Casa no encontrada");
		}
	}

	public void eliminar(int numero, int manzana) throws SQLException, RecursoNoEncontradoException {
		if (!casaDao.eliminar(numero, manzana)) {
			throw new RecursoNoEncontradoException("Casa no encontrada");
		}
	}
}