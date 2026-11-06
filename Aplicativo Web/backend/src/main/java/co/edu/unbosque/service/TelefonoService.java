package co.edu.unbosque.service;

import java.sql.SQLException;
import java.util.List;
import co.edu.unbosque.exception.DatoInvalidoException;
import co.edu.unbosque.exception.RecursoNoEncontradoException;
import co.edu.unbosque.dao.TelefonoDAO;
import co.edu.unbosque.model.Telefono;

/**
 * Lógica de negocio de los teléfonos.
 * <p>
 * Valida los datos recibidos y las reglas del conjunto antes de llamar al DAO, y comunica los
 * errores con las excepciones del paquete {@code exception}.
 */
public class TelefonoService {
	
	/** Acceso a datos de los teléfonos. */
	private final TelefonoDAO telefonoDao = new TelefonoDAO();
	
	/**
	 * Lista los teléfonos registrados.
	 *
	 * @return lista de teléfonos; vacía si no hay registros
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 */
	public List<Telefono> listar() throws SQLException {
		return telefonoDao.listar();
	}
	
	/**
	 * Consulta un teléfono por su llave primaria.
	 *
	 * @param telefono número de teléfono
	 * @param tipoDoc tipo de documento de la persona
	 * @param numeroDoc número de documento de la persona
	 * @return teléfono encontrado
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 * @throws RecursoNoEncontradoException si el teléfono no existe
	 */
	public Telefono consultar(String telefono, String tipoDoc, String numeroDoc)
			throws SQLException, RecursoNoEncontradoException {
		Telefono t = telefonoDao.buscar(telefono, tipoDoc, numeroDoc);
		if (t == null) {
			throw new RecursoNoEncontradoException("Teléfono no encontrado");
		}
		return t;
	}

	/**
	 * Registra un teléfono de una persona. El número debe tener de 7 a 15 dígitos.
	 *
	 * @param t teléfono que se va a crear
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 * @throws DatoInvalidoException si algún dato no cumple las validaciones
	 */
	public void crear(Telefono t) throws SQLException, DatoInvalidoException {
		if (t.getTelefono() == null || !t.getTelefono().matches("\\d{7,15}")
				|| t.getPersonaTipoDocumento() == null || t.getPersonaTipoDocumento().isBlank()
				|| t.getPersonaNumeroDocumento() == null || t.getPersonaNumeroDocumento().isBlank()) {
			throw new DatoInvalidoException("telefono (7 a 15 dígitos), personaTipoDocumento y personaNumeroDocumento son obligatorios");
		}
		telefonoDao.insertar(t);
	}

	/**
	 * Reemplaza un teléfono de una persona por un número nuevo de 7 a 15 dígitos.
	 *
	 * @param telefonoActual número que se reemplaza
	 * @param tipoDoc tipo de documento de la persona
	 * @param numeroDoc número de documento de la persona
	 * @param telefonoNuevo número nuevo
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 * @throws DatoInvalidoException si algún dato no cumple las validaciones
	 * @throws RecursoNoEncontradoException si el teléfono no existe
	 */
	public void actualizar(String telefonoActual, String tipoDoc, String numeroDoc, String telefonoNuevo)
			throws SQLException, DatoInvalidoException, RecursoNoEncontradoException {
		if (telefonoNuevo == null || !telefonoNuevo.matches("\\d{7,15}")) {
			throw new DatoInvalidoException("telefono debe tener entre 7 y 15 dígitos");
		}
		if (!telefonoDao.actualizar(telefonoActual, telefonoNuevo, tipoDoc, numeroDoc)) {
			throw new RecursoNoEncontradoException("Teléfono no encontrado");
		}
	}

	/**
	 * Elimina un teléfono.
	 *
	 * @param telefono número de teléfono
	 * @param tipoDoc tipo de documento de la persona
	 * @param numeroDoc número de documento de la persona
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 * @throws RecursoNoEncontradoException si el teléfono no existe
	 */
	public void eliminar(String telefono, String tipoDoc, String numeroDoc)
			throws SQLException, RecursoNoEncontradoException {
		if (!telefonoDao.eliminar(telefono, tipoDoc, numeroDoc)) {
			throw new RecursoNoEncontradoException("Teléfono no encontrado");
		}
	}
	
}
