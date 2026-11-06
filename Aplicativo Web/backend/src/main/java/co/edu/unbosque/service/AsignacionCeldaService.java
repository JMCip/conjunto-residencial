package co.edu.unbosque.service;

import java.sql.SQLException;
import java.util.List;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import co.edu.unbosque.exception.ConflictoException;
import co.edu.unbosque.exception.DatoInvalidoException;
import co.edu.unbosque.exception.RecursoNoEncontradoException;
import co.edu.unbosque.dao.AsignacionCeldaDAO;
import co.edu.unbosque.model.AsignacionCelda;

/**
 * Lógica de negocio de las asignaciones de celda.
 * <p>
 * Valida los datos recibidos y las reglas del conjunto antes de llamar al DAO, y comunica los
 * errores con las excepciones del paquete {@code exception}.
 */
public class AsignacionCeldaService {
	
	/** Acceso a datos de las asignaciones de celda. */
	private final AsignacionCeldaDAO asignacionCeldaDao = new AsignacionCeldaDAO();
	
	/**
	 * Lista las asignaciones de celda registradas.
	 *
	 * @return lista de asignaciones de celda; vacía si no hay registros
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 */
	public List<AsignacionCelda> listar() throws SQLException {
		return asignacionCeldaDao.listar();
	}
	
	/**
	 * Consulta una asignación de celda por su llave primaria.
	 *
	 * @param id identificador de la asignación de celda
	 * @return asignación de celda encontrada
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 * @throws RecursoNoEncontradoException si la asignación de celda no existe
	 */
	public AsignacionCelda consultar(int id) throws SQLException, RecursoNoEncontradoException {
		AsignacionCelda a = asignacionCeldaDao.buscar(id);
		if (a == null) {
			throw new RecursoNoEncontradoException("Asignación no encontrada");
		}
		return a;
	}

	/**
	 * Registra una asignación de celda. Si queda vigente (sin fecha de fin), la celda no puede tener
	 * otra asignación vigente.
	 *
	 * @param a asignación que se va a crear
	 * @return identificador de la asignación creada
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 * @throws DatoInvalidoException si algún dato no cumple las validaciones
	 * @throws ConflictoException si la celda ya tiene una asignación vigente
	 */
	public int crear(AsignacionCelda a) throws SQLException, DatoInvalidoException, ConflictoException {
		if (a.getCasaNumero() <= 0 || a.getCasaManzana() <= 0 || a.getIdCelda() <= 0) {
			throw new DatoInvalidoException("casaNumero, casaManzana e idCelda son obligatorios");
		}
		validarFechas(a);
		if (a.getFechaFin() == null && asignacionCeldaDao.celdaConAsignacionVigente(a.getIdCelda(), 0)) {
			throw new ConflictoException("La celda ya tiene una asignación vigente");
		}
		return asignacionCeldaDao.insertar(a);
	}

	/**
	 * Actualiza las fechas de una asignación. Si queda vigente, la celda no puede tener otra
	 * asignación vigente.
	 *
	 * @param a asignación con su identificador y las fechas nuevas
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 * @throws DatoInvalidoException si algún dato no cumple las validaciones
	 * @throws RecursoNoEncontradoException si la asignación de celda no existe
	 * @throws ConflictoException si la celda ya tiene otra asignación vigente
	 */
	public void actualizar(AsignacionCelda a)
			throws SQLException, DatoInvalidoException, RecursoNoEncontradoException, ConflictoException {
		AsignacionCelda actual = consultar(a.getIdAsignacion());
		validarFechas(a);
		if (a.getFechaFin() == null
				&& asignacionCeldaDao.celdaConAsignacionVigente(actual.getIdCelda(), a.getIdAsignacion())) {
			throw new ConflictoException("La celda ya tiene otra asignación vigente");
		}
		if (!asignacionCeldaDao.actualizar(a)) {
			throw new RecursoNoEncontradoException("Asignación no encontrada");
		}
	}

	/**
	 * Elimina una asignación de celda.
	 *
	 * @param id identificador de la asignación de celda
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 * @throws RecursoNoEncontradoException si la asignación de celda no existe
	 */
	public void eliminar(int id) throws SQLException, RecursoNoEncontradoException {
		if (!asignacionCeldaDao.eliminar(id)) {
			throw new RecursoNoEncontradoException("Asignación no encontrada");
		}
	}

	/**
	 * Valida que la fecha de inicio exista, que las fechas tengan formato {@code AAAA-MM-DD} y que el
	 * fin no sea anterior al inicio. Una fecha de fin vacía se guarda como {@code null}.
	 *
	 * @param a asignación que se valida
	 * @throws DatoInvalidoException si algún dato no cumple las validaciones
	 */
	private void validarFechas(AsignacionCelda a) throws DatoInvalidoException {
		if (a.getFechaFin() != null && a.getFechaFin().isBlank()) {
			a.setFechaFin(null);
		}
		if (a.getFechaInicio() == null || a.getFechaInicio().isBlank()) {
			throw new DatoInvalidoException("fechaInicio es obligatoria (formato AAAA-MM-DD)");
		}
		try {
			LocalDate inicio = LocalDate.parse(a.getFechaInicio());
			if (a.getFechaFin() != null && LocalDate.parse(a.getFechaFin()).isBefore(inicio)) {
				throw new DatoInvalidoException("fechaFin no puede ser anterior a fechaInicio");
			}
		} catch (DateTimeParseException e) {
			throw new DatoInvalidoException("Las fechas deben tener el formato AAAA-MM-DD");
		}
	}
}
