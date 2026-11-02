package co.edu.unbosque.service;

import java.sql.SQLException;
import java.util.List;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import co.edu.unbosque.exception.ConflictoException;
import co.edu.unbosque.exception.DatoInvalidoException;
import co.edu.unbosque.exception.RecursoNoEncontradoException;
import co.edu.unbosque.dao.AsignacionCeldaDAO;
import co.edu.unbosque.model.AsignacionCelda;

public class AsignacionCeldaService {
	
	private final AsignacionCeldaDAO asignacionCeldaDao = new AsignacionCeldaDAO();
	
	public List<AsignacionCelda> listar() throws SQLException {
		return asignacionCeldaDao.listar();
	}
	
	public AsignacionCelda consultar(int id) throws SQLException, RecursoNoEncontradoException {
		AsignacionCelda a = asignacionCeldaDao.buscar(id);
		if (a == null) {
			throw new RecursoNoEncontradoException("Asignación no encontrada");
		}
		return a;
	}

	public int crear(AsignacionCelda a) throws SQLException, DatoInvalidoException, ConflictoException {
		if (a.getCasaNumero() <= 0 || a.getCasaManzana() <= 0 || a.getIdCelda() <= 0) {
			throw new DatoInvalidoException("casaNumero, casaManzana e idCelda son obligatorios");
		}
		validarFechas(a);
		if (a.getFechaFin() == null && asignacionCeldaDao.celdaConAsignacionVigente(a.getIdCelda(), 0)) {
			throw new ConflictoException("La celda ya tiene una asignación vigente");
		}
		return asignacionCeldaDao.insertar(a);
	}

	public void actualizar(AsignacionCelda a)
			throws SQLException, DatoInvalidoException, RecursoNoEncontradoException, ConflictoException {
		AsignacionCelda actual = consultar(a.getIdAsignacion());
		validarFechas(a);
		if (a.getFechaFin() == null
				&& asignacionCeldaDao.celdaConAsignacionVigente(actual.getIdCelda(), a.getIdAsignacion())) {
			throw new ConflictoException("La celda ya tiene otra asignación vigente");
		}
		if (!asignacionCeldaDao.actualizar(a)) {
			throw new RecursoNoEncontradoException("Asignación no encontrada");
		}
	}

	public void eliminar(int id) throws SQLException, RecursoNoEncontradoException {
		if (!asignacionCeldaDao.eliminar(id)) {
			throw new RecursoNoEncontradoException("Asignación no encontrada");
		}
	}

	private void validarFechas(AsignacionCelda a) throws DatoInvalidoException {
		if (a.getFechaFin() != null && a.getFechaFin().isBlank()) {
			a.setFechaFin(null);
		}
		if (a.getFechaInicio() == null || a.getFechaInicio().isBlank()) {
			throw new DatoInvalidoException("fechaInicio es obligatoria (formato AAAA-MM-DD)");
		}
		try {
			LocalDate inicio = LocalDate.parse(a.getFechaInicio());
			if (a.getFechaFin() != null && LocalDate.parse(a.getFechaFin()).isBefore(inicio)) {
				throw new DatoInvalidoException("fechaFin no puede ser anterior a fechaInicio");
			}
		} catch (DateTimeParseException e) {
			throw new DatoInvalidoException("Las fechas deben tener el formato AAAA-MM-DD");
		}
	}
}
