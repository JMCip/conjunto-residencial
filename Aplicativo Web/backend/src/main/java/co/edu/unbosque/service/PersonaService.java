package co.edu.unbosque.service;

import co.edu.unbosque.exception.DatoInvalidoException;
import co.edu.unbosque.exception.RecursoNoEncontradoException;

import java.sql.SQLException;
import java.util.List;

import co.edu.unbosque.dao.PersonaDAO;
import co.edu.unbosque.model.Persona;

/**
 * Lógica de negocio de las personas.
 * <p>
 * Valida los datos recibidos y las reglas del conjunto antes de llamar al DAO, y comunica los
 * errores con las excepciones del paquete {@code exception}.
 */
public class PersonaService {
	
	/** Acceso a datos de las personas. */
	private final PersonaDAO pDao = new PersonaDAO();
		
	/**
	 * Lista las personas registradas.
	 *
	 * @return lista de personas; vacía si no hay registros
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 */
	public List<Persona> listar () throws SQLException {
		return pDao.listar();
	}
	
	/**
	 * Consulta una persona por su llave primaria.
	 *
	 * @param tipoDoc tipo de documento de la persona
	 * @param numeroDoc número de documento de la persona
	 * @return persona encontrada
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 * @throws RecursoNoEncontradoException si la persona no existe
	 */
	public Persona consultar(String tipoDoc, String numeroDoc)
			throws SQLException, RecursoNoEncontradoException {
		Persona p = pDao.buscar(tipoDoc, numeroDoc);
		if (p == null) {
			throw new RecursoNoEncontradoException("Persona no encontrada");
		}
		return p;
	}

	/**
	 * Registra una persona. El tipo y el número de documento son obligatorios.
	 *
	 * @param p persona que se va a crear
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 * @throws DatoInvalidoException si algún dato no cumple las validaciones
	 */
	public void crear(Persona p) throws SQLException, DatoInvalidoException {
		if (p.getTipoDocumento() == null || p.getTipoDocumento().isBlank()
				|| p.getNumeroDocumento() == null || p.getNumeroDocumento().isBlank()) {
			throw new DatoInvalidoException("tipoDocumento y numeroDocumento son obligatorios");
		}
		validarDatos(p);
		pDao.insertar(p);
	}

	/**
	 * Actualiza el correo y los nombres de una persona.
	 *
	 * @param p persona con su documento y los datos nuevos
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 * @throws DatoInvalidoException si algún dato no cumple las validaciones
	 * @throws RecursoNoEncontradoException si la persona no existe
	 */
	public void actualizar(Persona p) throws SQLException, DatoInvalidoException, RecursoNoEncontradoException {
		validarDatos(p);
		if (!pDao.actualizar(p)) {
			throw new RecursoNoEncontradoException("Persona no encontrada");
		}
	}

	/**
	 * Elimina una persona.
	 *
	 * @param tipoDoc tipo de documento de la persona
	 * @param numeroDoc número de documento de la persona
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 * @throws RecursoNoEncontradoException si la persona no existe
	 */
	public void eliminar(String tipoDoc, String numeroDoc) throws SQLException, RecursoNoEncontradoException {
		if (!pDao.eliminar(tipoDoc, numeroDoc)) {
			throw new RecursoNoEncontradoException("Persona no encontrada");
		}
	}

	/**
	 * Valida que existan el primer nombre y el primer apellido, y el formato del correo si se envía.
	 *
	 * @param p persona que se valida
	 * @throws DatoInvalidoException si algún dato no cumple las validaciones
	 */
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

