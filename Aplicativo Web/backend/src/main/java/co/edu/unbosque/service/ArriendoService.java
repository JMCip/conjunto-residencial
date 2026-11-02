package co.edu.unbosque.service;

import java.sql.SQLException;
import java.util.List;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import co.edu.unbosque.exception.ConflictoException;
import co.edu.unbosque.exception.DatoInvalidoException;
import co.edu.unbosque.exception.RecursoNoEncontradoException;

import co.edu.unbosque.dao.ArriendoDAO;
import co.edu.unbosque.model.Arriendo;

public class ArriendoService {
	
	private final ArriendoDAO arriendoDao = new ArriendoDAO();
	
	public List<Arriendo> listar() throws SQLException {
		return arriendoDao.listar();
	}
	
	private static final String VIGENTE = "VIGENTE";

	public Arriendo consultar(int id) throws SQLException, RecursoNoEncontradoException {
		Arriendo a = arriendoDao.buscar(id);
		if (a == null) {
			throw new RecursoNoEncontradoException("Contrato no encontrado");
		}
		return a;
	}

	public int crear(Arriendo a) throws SQLException, DatoInvalidoException, ConflictoException {
		if (a.getCasaNumero() <= 0 || a.getCasaManzana() <= 0
				|| a.getArrendatarioTipoDocumento() == null || a.getArrendatarioTipoDocumento().isBlank()
				|| a.getArrendatarioNumeroDocumento() == null || a.getArrendatarioNumeroDocumento().isBlank()
				|| a.getPropietarioTipoDocumento() == null || a.getPropietarioTipoDocumento().isBlank()
				|| a.getPropietarioNumeroDocumento() == null || a.getPropietarioNumeroDocumento().isBlank()) {
			throw new DatoInvalidoException(
					"casaNumero, casaManzana y el documento del arrendatario y del propietario son obligatorios");
		}
		validarDatos(a);

		// S-39: el arrendatario no puede ser propietario de esa misma casa.
		if (arriendoDao.esPropietarioVigente(a.getCasaNumero(), a.getCasaManzana(),
				a.getArrendatarioTipoDocumento(), a.getArrendatarioNumeroDocumento())) {
			throw new ConflictoException("El arrendatario es propietario de esa casa");
		}
		// El que firma debe ser propietario vigente de la casa.
		if (!arriendoDao.esPropietarioVigente(a.getCasaNumero(), a.getCasaManzana(),
				a.getPropietarioTipoDocumento(), a.getPropietarioNumeroDocumento())) {
			throw new ConflictoException("El propietario que firma no es propietario vigente de esa casa");
		}
		validarUnicoVigente(a, a.getCasaNumero(), a.getCasaManzana(), 0);
		return arriendoDao.insertar(a);
	}

	public void actualizar(Arriendo a)
			throws SQLException, DatoInvalidoException, RecursoNoEncontradoException, ConflictoException {
		Arriendo actual = consultar(a.getIdContrato());
		validarDatos(a);
		validarUnicoVigente(a, actual.getCasaNumero(), actual.getCasaManzana(), a.getIdContrato());
		if (!arriendoDao.actualizar(a)) {
			throw new RecursoNoEncontradoException("Contrato no encontrado");
		}
	}

	public void eliminar(int id) throws SQLException, RecursoNoEncontradoException {
		if (!arriendoDao.eliminar(id)) {
			throw new RecursoNoEncontradoException("Contrato no encontrado");
		}
	}

	private void validarDatos(Arriendo a) throws DatoInvalidoException {
		if (a.getCuotaMensual() == null || a.getCuotaMensual().signum() <= 0) {
			throw new DatoInvalidoException("cuotaMensual debe ser mayor que 0");
		}
		if (a.getEstadoContrato() == null || a.getEstadoContrato().isBlank()) {
			throw new DatoInvalidoException("estadoContrato es obligatorio");
		}
		if (a.getFechaFin() != null && a.getFechaFin().isBlank()) {
			a.setFechaFin(null);
		}
		try {
			if (a.getFechaInicio() == null) {
				throw new DatoInvalidoException("fechaInicio es obligatoria (formato AAAA-MM-DD)");
			}
			LocalDate inicio = LocalDate.parse(a.getFechaInicio());
			if (a.getFechaFin() != null && LocalDate.parse(a.getFechaFin()).isBefore(inicio)) {
				throw new DatoInvalidoException("fechaFin no puede ser anterior a fechaInicio");
			}
		} catch (DateTimeParseException e) {
			throw new DatoInvalidoException("Las fechas deben tener el formato AAAA-MM-DD");
		}
	}

	// S-3: como máximo un contrato vigente por casa.
	private void validarUnicoVigente(Arriendo a, int numero, int manzana, int excluirId)
			throws SQLException, ConflictoException {
		if (VIGENTE.equalsIgnoreCase(a.getEstadoContrato())
				&& arriendoDao.existeVigente(numero, manzana, excluirId)) {
			throw new ConflictoException("La casa ya tiene un contrato de arriendo vigente");
		}
	}
	
	
}
