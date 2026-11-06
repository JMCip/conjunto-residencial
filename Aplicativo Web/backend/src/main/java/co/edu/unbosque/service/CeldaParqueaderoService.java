package co.edu.unbosque.service;

import java.sql.SQLException;
import java.util.List;
import java.util.Set;

import co.edu.unbosque.exception.DatoInvalidoException;
import co.edu.unbosque.exception.RecursoNoEncontradoException;
import co.edu.unbosque.dao.CeldaParqueaderoDAO;
import co.edu.unbosque.model.CeldaParqueadero;

/**
 * Lógica de negocio de las celdas de parqueadero.
 * <p>
 * Valida los datos recibidos y las reglas del conjunto antes de llamar al DAO, y comunica los
 * errores con las excepciones del paquete {@code exception}.
 */
public class CeldaParqueaderoService {
	
	/** Acceso a datos de las celdas de parqueadero. */
	private final CeldaParqueaderoDAO celdaParqueaderoDao = new CeldaParqueaderoDAO();
	
	/**
	 * Lista las celdas de parqueadero registradas.
	 *
	 * @return lista de celdas de parqueadero; vacía si no hay registros
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 */
	public List<CeldaParqueadero> listar() throws SQLException {
		return celdaParqueaderoDao.listar();
	}
	
	/** Tipos válidos de celda. */
	private static final Set<String> TIPOS = Set.of("PROPIA", "ADICIONAL", "VISITANTES");
	/** Estados válidos de una celda. */
	private static final Set<String> ESTADOS = Set.of("OCUPADA", "DESOCUPADA");

	/**
	 * Consulta una celda de parqueadero por su llave primaria.
	 *
	 * @param id identificador de la celda de parqueadero
	 * @return celda de parqueadero encontrada
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 * @throws RecursoNoEncontradoException si la celda de parqueadero no existe
	 */
	public CeldaParqueadero consultar(int id) throws SQLException, RecursoNoEncontradoException {
		CeldaParqueadero c = celdaParqueaderoDao.buscar(id);
		if (c == null) {
			throw new RecursoNoEncontradoException("Celda no encontrada");
		}
		return c;
	}

	/**
	 * Valida y registra una celda de parqueadero.
	 *
	 * @param c celda de parqueadero que se va a creara
	 * @return identificador generado
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 * @throws DatoInvalidoException si algún dato no cumple las validaciones
	 */
	public int crear(CeldaParqueadero c) throws SQLException, DatoInvalidoException {
		validarDatos(c);
		return celdaParqueaderoDao.insertar(c);
	}

	/**
	 * Valida y actualiza una celda de parqueadero.
	 *
	 * @param c celda de parqueadero con su llave primaria y los datos nuevosa
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 * @throws DatoInvalidoException si algún dato no cumple las validaciones
	 * @throws RecursoNoEncontradoException si la celda de parqueadero no existe
	 */
	public void actualizar(CeldaParqueadero c)
			throws SQLException, DatoInvalidoException, RecursoNoEncontradoException {
		validarDatos(c);
		if (!celdaParqueaderoDao.actualizar(c)) {
			throw new RecursoNoEncontradoException("Celda no encontrada");
		}
	}

	/**
	 * Elimina una celda de parqueadero.
	 *
	 * @param id identificador de la celda de parqueadero
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 * @throws RecursoNoEncontradoException si la celda de parqueadero no existe
	 */
	public void eliminar(int id) throws SQLException, RecursoNoEncontradoException {
		if (!celdaParqueaderoDao.eliminar(id)) {
			throw new RecursoNoEncontradoException("Celda no encontrada");
		}
	}

	/**
	 * Valida el tipo y el estado de la celda y los pasa a mayúsculas.
	 *
	 * @param c celda que se valida
	 * @throws DatoInvalidoException si algún dato no cumple las validaciones
	 */
	private void validarDatos(CeldaParqueadero c) throws DatoInvalidoException {
		if (c.getTipo() == null || c.getTipo().isBlank() || c.getEstado() == null || c.getEstado().isBlank()) {
			throw new DatoInvalidoException("tipo y estado son obligatorios");
		}
		c.setTipo(c.getTipo().trim().toUpperCase());
		c.setEstado(c.getEstado().trim().toUpperCase());
		if (!TIPOS.contains(c.getTipo())) {
			throw new DatoInvalidoException("tipo debe ser PROPIA, ADICIONAL o VISITANTES");
		}
		if (!ESTADOS.contains(c.getEstado())) {
			throw new DatoInvalidoException("estado debe ser OCUPADA o DESOCUPADA");
		}
	}
	
}
