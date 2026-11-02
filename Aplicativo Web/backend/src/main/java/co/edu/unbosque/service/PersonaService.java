package co.edu.unbosque.service;

import co.edu.unbosque.exception.DatoInvalidoException;
import co.edu.unbosque.exception.RecursoNoEncontradoException;

import java.sql.SQLException;
import java.util.List;

import co.edu.unbosque.dao.PersonaDAO;
import co.edu.unbosque.model.Persona;

public class PersonaService {
	
	private final PersonaDAO pDao = new PersonaDAO();
		
	public List<Persona> listar () throws SQLException {
		return pDao.listar();
	}
	
	public Persona consultar(String tipoDoc, String numeroDoc)
			throws SQLException, RecursoNoEncontradoException {
		Persona p = pDao.buscar(tipoDoc, numeroDoc);
		if (p == null) {
			throw new RecursoNoEncontradoException("Persona no encontrada");
		}
		return p;
	}

	public void crear(Persona p) throws SQLException, DatoInvalidoException {
		if (p.getTipoDocumento() == null || p.getTipoDocumento().isBlank()
				|| p.getNumeroDocumento() == null || p.getNumeroDocumento().isBlank()) {
			throw new DatoInvalidoException("tipoDocumento y numeroDocumento son obligatorios");
		}
		validarDatos(p);
		pDao.insertar(p);
	}

	public void actualizar(Persona p) throws SQLException, DatoInvalidoException, RecursoNoEncontradoException {
		validarDatos(p);
		if (!pDao.actualizar(p)) {
			throw new RecursoNoEncontradoException("Persona no encontrada");
		}
	}

	public void eliminar(String tipoDoc, String numeroDoc) throws SQLException, RecursoNoEncontradoException {
		if (!pDao.eliminar(tipoDoc, numeroDoc)) {
			throw new RecursoNoEncontradoException("Persona no encontrada");
		}
	}

	private void validarDatos(Persona p) throws DatoInvalidoException {
		if (p.getPrimerNombre() == null || p.getPrimerNombre().isBlank()
				|| p.getPrimerApellido() == null || p.getPrimerApellido().isBlank()) {
			throw new DatoInvalidoException("primerNombre y primerApellido son obligatorios");
		}
		if (p.getCorreo() != null && !p.getCorreo().isBlank()
				&& !p.getCorreo().matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
			throw new DatoInvalidoException("correo no tiene un formato válido");
		}
	}
	
}



