package co.edu.unbosque.service;

import java.sql.SQLException;
import java.util.List;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import co.edu.unbosque.exception.DatoInvalidoException;
import co.edu.unbosque.exception.RecursoNoEncontradoException;

import co.edu.unbosque.dao.VisitaDAO;
import co.edu.unbosque.model.Visita;

public class VisitaService {
	
	private final VisitaDAO visitaDao = new VisitaDAO();
	
	public List<Visita> listar() throws SQLException {
		return visitaDao.listar();
	}
	
	public Visita consultar(int id) throws SQLException, RecursoNoEncontradoException {
		Visita v = visitaDao.buscar(id);
		if (v == null) {
			throw new RecursoNoEncontradoException("Visita no encontrada");
		}
		return v;
	}

	public int crear(Visita v) throws SQLException, DatoInvalidoException {
		if (v.getCasaNumero() <= 0 || v.getCasaManzana() <= 0
				|| v.getVisitanteTipoDocumento() == null || v.getVisitanteTipoDocumento().isBlank()
				|| v.getVisitanteNumeroDocumento() == null || v.getVisitanteNumeroDocumento().isBlank()) {
			throw new DatoInvalidoException(
					"casaNumero, casaManzana, visitanteTipoDocumento y visitanteNumeroDocumento son obligatorios");
		}
		validarFechas(v);
		return visitaDao.insertar(v);
	}

	public void actualizar(Visita v) throws SQLException, DatoInvalidoException, RecursoNoEncontradoException {
		validarFechas(v);
		if (!visitaDao.actualizar(v)) {
			throw new RecursoNoEncontradoException("Visita no encontrada");
		}
	}

	public void eliminar(int id) throws SQLException, RecursoNoEncontradoException {
		if (!visitaDao.eliminar(id)) {
			throw new RecursoNoEncontradoException("Visita no encontrada");
		}
	}

	private void validarFechas(Visita v) throws DatoInvalidoException {
		if (v.getFechaSalida() != null && v.getFechaSalida().isBlank()) {
			v.setFechaSalida(null);
		}
		if (v.getFechaEntrada() == null || v.getFechaEntrada().isBlank()) {
			throw new DatoInvalidoException("fechaEntrada es obligatoria (formato AAAA-MM-DD)");
		}
		try {
			LocalDate entrada = LocalDate.parse(v.getFechaEntrada());
			if (v.getFechaSalida() != null && LocalDate.parse(v.getFechaSalida()).isBefore(entrada)) {
				throw new DatoInvalidoException("fechaSalida no puede ser anterior a fechaEntrada");
			}
		} catch (DateTimeParseException e) {
			throw new DatoInvalidoException("Las fechas deben tener el formato AAAA-MM-DD");
		}
	}
}
