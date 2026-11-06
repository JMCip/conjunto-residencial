package co.edu.unbosque.service;

import co.edu.unbosque.dao.CasaDao;
import co.edu.unbosque.exception.DatoInvalidoException;
import co.edu.unbosque.exception.RecursoNoEncontradoException;
import co.edu.unbosque.model.Casa;
import java.sql.SQLException;
import java.util.List;

/**
 * Lógica de negocio de las casas.
 * <p>
 * Valida los datos recibidos y las reglas del conjunto antes de llamar al DAO, y comunica los
 * errores con las excepciones del paquete {@code exception}.
 */
public class CasaService {

	/** Acceso a datos de las casas. */
	private final CasaDao casaDao = new CasaDao();

	/**
	 * Lista las casas registradas.
	 *
	 * @return lista de casas; vacía si no hay registros
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 */
	public List<Casa> listar() throws SQLException {
		return casaDao.listar();
	}

	/**
	 * Registra una casa. El número y la manzana deben ser mayores que 0.
	 *
	 * @param c casa que se va a crear
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 * @throws DatoInvalidoException si algún dato no cumple las validaciones
	 */
	public void crear(Casa c) throws SQLException, DatoInvalidoException {
		if (c.getNumero() <= 0 || c.getManzana() <= 0) {
			throw new DatoInvalidoException("numero y manzana deben ser mayores que 0");
		}
		validarDatos(c);
		casaDao.insertar(c);
	}

	/**
	 * Actualiza el área y el número de habitaciones de una casa.
	 *
	 * @param c casa con su número, su manzana y los datos nuevos
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 * @throws DatoInvalidoException si algún dato no cumple las validaciones
	 * @throws RecursoNoEncontradoException si la casa no existe
	 */
	public void actualizar(Casa c) throws SQLException, DatoInvalidoException, RecursoNoEncontradoException {
		validarDatos(c);
		if (!casaDao.actualizar(c)) {
			throw new RecursoNoEncontradoException("Casa no encontrada");
		}
	}

	/**
	 * Elimina una casa.
	 *
	 * @param numero número de la casa
	 * @param manzana manzana de la casa
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 * @throws RecursoNoEncontradoException si la casa no existe
	 */
	public void eliminar(int numero, int manzana) throws SQLException, RecursoNoEncontradoException {
		if (!casaDao.eliminar(numero, manzana)) {
			throw new RecursoNoEncontradoException("Casa no encontrada");
		}
	}

	/**
	 * Valida que el área exista y sea mayor que 0, y que el número de habitaciones no sea negativo.
	 *
	 * @param c casa que se valida
	 * @throws DatoInvalidoException si algún dato no cumple las validaciones
	 */
	private void validarDatos(Casa c) throws DatoInvalidoException {
		if (c.getArea() == null) {
			throw new DatoInvalidoException("area es obligatoria");
		}
		if (c.getArea().signum() <= 0) {
			throw new DatoInvalidoException("area debe ser mayor que 0");
		}
		if (c.getNumeroHabitaciones() != null && c.getNumeroHabitaciones() < 0) {
			throw new DatoInvalidoException("numeroHabitaciones no puede ser negativo");
		}
	}

}