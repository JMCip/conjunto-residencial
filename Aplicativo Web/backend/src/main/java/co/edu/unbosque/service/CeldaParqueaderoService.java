package co.edu.unbosque.service;

import java.sql.SQLException;
import java.util.List;
import java.util.Set;

import co.edu.unbosque.exception.DatoInvalidoException;
import co.edu.unbosque.exception.RecursoNoEncontradoException;
import co.edu.unbosque.dao.CeldaParqueaderoDAO;
import co.edu.unbosque.model.CeldaParqueadero;

public class CeldaParqueaderoService {
	
	private final CeldaParqueaderoDAO celdaParqueaderoDao = new CeldaParqueaderoDAO();
	
	public List<CeldaParqueadero> listar() throws SQLException {
		return celdaParqueaderoDao.listar();
	}
	
	private static final Set<String> TIPOS = Set.of("PROPIA", "ADICIONAL", "VISITANTES");
	private static final Set<String> ESTADOS = Set.of("OCUPADA", "DESOCUPADA");

	public CeldaParqueadero consultar(int id) throws SQLException, RecursoNoEncontradoException {
		CeldaParqueadero c = celdaParqueaderoDao.buscar(id);
		if (c == null) {
			throw new RecursoNoEncontradoException("Celda no encontrada");
		}
		return c;
	}

	public int crear(CeldaParqueadero c) throws SQLException, DatoInvalidoException {
		validarDatos(c);
		return celdaParqueaderoDao.insertar(c);
	}

	public void actualizar(CeldaParqueadero c)
			throws SQLException, DatoInvalidoException, RecursoNoEncontradoException {
		validarDatos(c);
		if (!celdaParqueaderoDao.actualizar(c)) {
			throw new RecursoNoEncontradoException("Celda no encontrada");
		}
	}

	public void eliminar(int id) throws SQLException, RecursoNoEncontradoException {
		if (!celdaParqueaderoDao.eliminar(id)) {
			throw new RecursoNoEncontradoException("Celda no encontrada");
		}
	}

	private void validarDatos(CeldaParqueadero c) throws DatoInvalidoException {
		if (c.getTipo() == null || c.getTipo().isBlank() || c.getEstado() == null || c.getEstado().isBlank()) {
			throw new DatoInvalidoException("tipo y estado son obligatorios");
		}
		c.setTipo(c.getTipo().trim().toUpperCase());
		c.setEstado(c.getEstado().trim().toUpperCase());
		if (!TIPOS.contains(c.getTipo())) {
			throw new DatoInvalidoException("tipo debe ser PROPIA, ADICIONAL o VISITANTES");
		}
		if (!ESTADOS.contains(c.getEstado())) {
			throw new DatoInvalidoException("estado debe ser OCUPADA o DESOCUPADA");
		}
	}
	
}
