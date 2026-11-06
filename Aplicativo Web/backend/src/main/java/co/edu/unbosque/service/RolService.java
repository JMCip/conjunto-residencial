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

/**
 * Lógica de negocio de los roles de las personas (propietario, arrendatario, residente y
 * visitante).
 */
public class RolService {
	
	/** Acceso a datos de los roles. */
	private final RolDAO rolDao = new RolDAO();
	
	/**
	 * Lista las personas con rol de propietario.
	 *
	 * @return propietarios registrados
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 */
	public List<Persona> listarPropietarios() throws SQLException {
		return rolDao.listarPropietarios();
	}
	
	/**
	 * Lista las personas con rol de arrendatario.
	 *
	 * @return arrendatarios registrados
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 */
	public List<Persona> listarArrendatarios() throws SQLException {
		return rolDao.listarArrendatarios();
	}
	
	/**
	 * Lista las personas con rol de residente.
	 *
	 * @return residentes registrados
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 */
	public List<Persona> listarResidentes() throws SQLException {
		return rolDao.listarResidentes();
	}
	
	/**
	 * Lista las personas con rol de visitante.
	 *
	 * @return visitantes registrados
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 */
	public List<Persona> listarVisitantes() throws SQLException {
		return rolDao.listarVisitantes();
	}
	
	/** Roles válidos. */
	private static final Set<String> ROLES = Set.of("propietario", "arrendatario", "residente", "visitante");

	/** Acceso a datos de las personas. */
	private final PersonaDAO personaDao = new PersonaDAO();

	/**
	 * Asigna un rol a una persona registrada que todavía no lo tiene.
	 *
	 * @param rol nombre del rol: {@code propietario}, {@code arrendatario}, {@code residente} o
	 *         {@code visitante}
	 * @param tipoDoc tipo de documento de la persona
	 * @param numeroDoc número de documento de la persona
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 * @throws DatoInvalidoException si el rol no es válido
	 * @throws RecursoNoEncontradoException si la persona no existe
	 * @throws ConflictoException si la persona ya tiene el rol
	 */
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

	/**
	 * Quita un rol a una persona.
	 *
	 * @param rol nombre del rol: {@code propietario}, {@code arrendatario}, {@code residente} o
	 *         {@code visitante}
	 * @param tipoDoc tipo de documento de la persona
	 * @param numeroDoc número de documento de la persona
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 * @throws DatoInvalidoException si el rol no es válido
	 * @throws RecursoNoEncontradoException si la persona no tiene el rol
	 */
	public void quitar(String rol, String tipoDoc, String numeroDoc)
			throws SQLException, DatoInvalidoException, RecursoNoEncontradoException {
		rol = validarRol(rol);
		if (!rolDao.quitar(rol, tipoDoc, numeroDoc)) {
			throw new RecursoNoEncontradoException("La persona no tiene el rol " + rol);
		}
	}

	/**
	 * Valida el rol y lo pasa a minúsculas.
	 *
	 * @param rol rol recibido
	 * @return rol normalizado
	 * @throws DatoInvalidoException si algún dato no cumple las validaciones
	 */
	private String validarRol(String rol) throws DatoInvalidoException {
		String r = rol == null ? "" : rol.trim().toLowerCase();
		if (!ROLES.contains(r)) {
			throw new DatoInvalidoException("rol debe ser propietario, arrendatario, residente o visitante");
		}
		return r;
	}

}
