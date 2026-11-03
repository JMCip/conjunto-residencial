package co.edu.unbosque.service;

import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import co.edu.unbosque.dao.PersonaDAO;
import co.edu.unbosque.exception.ConflictoException;
import co.edu.unbosque.exception.DatoInvalidoException;
import co.edu.unbosque.exception.RecursoNoEncontradoException;
import co.edu.unbosque.dao.RolDAO;
import co.edu.unbosque.model.Persona;

public class RolService {
	
	private final RolDAO rolDao = new RolDAO();
	
	public List<Persona> listarPropietarios() throws SQLException {
		return rolDao.listarPropietarios();
	}
	
	public List<Persona> listarArrendatarios() throws SQLException {
		return rolDao.listarArrendatarios();
	}
	
	public List<Persona> listarResidentes() throws SQLException {
		return rolDao.listarResidentes();
	}
	
	public List<Persona> listarVisitantes() throws SQLException {
		return rolDao.listarVisitantes();
	}
	
	private static final Set<String> ROLES = Set.of("propietario", "arrendatario", "residente", "visitante");

	private final PersonaDAO personaDao = new PersonaDAO();

	public void asignar(String rol, String tipoDoc, String numeroDoc)
			throws SQLException, DatoInvalidoException, RecursoNoEncontradoException, ConflictoException {
		rol = validarRol(rol);
		if (personaDao.buscar(tipoDoc, numeroDoc) == null) {
			throw new RecursoNoEncontradoException("Persona no encontrada");
		}
		if (rolDao.tieneRol(rol, tipoDoc, numeroDoc)) {
			throw new ConflictoException("La persona ya tiene el rol " + rol);
		}
		rolDao.asignar(rol, tipoDoc, numeroDoc);
	}

	public void quitar(String rol, String tipoDoc, String numeroDoc)
			throws SQLException, DatoInvalidoException, RecursoNoEncontradoException {
		rol = validarRol(rol);
		if (!rolDao.quitar(rol, tipoDoc, numeroDoc)) {
			throw new RecursoNoEncontradoException("La persona no tiene el rol " + rol);
		}
	}

	private String validarRol(String rol) throws DatoInvalidoException {
		String r = rol == null ? "" : rol.trim().toLowerCase();
		if (!ROLES.contains(r)) {
			throw new DatoInvalidoException("rol debe ser propietario, arrendatario, residente o visitante");
		}
		return r;
	}
	
	
}
