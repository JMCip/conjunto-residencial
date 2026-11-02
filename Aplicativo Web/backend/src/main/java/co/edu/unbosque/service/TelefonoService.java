package co.edu.unbosque.service;

import java.sql.SQLException;
import java.util.List;
import co.edu.unbosque.exception.DatoInvalidoException;
import co.edu.unbosque.exception.RecursoNoEncontradoException;
import co.edu.unbosque.dao.TelefonoDAO;
import co.edu.unbosque.model.Telefono;

public class TelefonoService {
	
	private final TelefonoDAO telefonoDao = new TelefonoDAO();
	
	public List<Telefono> listar() throws SQLException {
		return telefonoDao.listar();
	}
	
	public Telefono consultar(String telefono, String tipoDoc, String numeroDoc)
			throws SQLException, RecursoNoEncontradoException {
		Telefono t = telefonoDao.buscar(telefono, tipoDoc, numeroDoc);
		if (t == null) {
			throw new RecursoNoEncontradoException("Teléfono no encontrado");
		}
		return t;
	}

	public void crear(Telefono t) throws SQLException, DatoInvalidoException {
		if (t.getTelefono() == null || !t.getTelefono().matches("\\d{7,15}")
				|| t.getPersonaTipoDocumento() == null || t.getPersonaTipoDocumento().isBlank()
				|| t.getPersonaNumeroDocumento() == null || t.getPersonaNumeroDocumento().isBlank()) {
			throw new DatoInvalidoException("telefono (7 a 15 dígitos), personaTipoDocumento y personaNumeroDocumento son obligatorios");
		}
		telefonoDao.insertar(t);
	}

	public void actualizar(String telefonoActual, String tipoDoc, String numeroDoc, String telefonoNuevo)
			throws SQLException, DatoInvalidoException, RecursoNoEncontradoException {
		if (telefonoNuevo == null || !telefonoNuevo.matches("\\d{7,15}")) {
			throw new DatoInvalidoException("telefono debe tener entre 7 y 15 dígitos");
		}
		if (!telefonoDao.actualizar(telefonoActual, telefonoNuevo, tipoDoc, numeroDoc)) {
			throw new RecursoNoEncontradoException("Teléfono no encontrado");
		}
	}

	public void eliminar(String telefono, String tipoDoc, String numeroDoc)
			throws SQLException, RecursoNoEncontradoException {
		if (!telefonoDao.eliminar(telefono, tipoDoc, numeroDoc)) {
			throw new RecursoNoEncontradoException("Teléfono no encontrado");
		}
	}
	
}
