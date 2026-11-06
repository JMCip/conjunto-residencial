package co.edu.unbosque.service;

import java.sql.SQLException;
import java.util.List;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import co.edu.unbosque.exception.DatoInvalidoException;
import co.edu.unbosque.exception.RecursoNoEncontradoException;
import co.edu.unbosque.dao.RolDAO;
import co.edu.unbosque.exception.ConflictoException;
import co.edu.unbosque.dao.VisitaDAO;
import co.edu.unbosque.model.Visita;

/**
 * Lógica de negocio de las visitas.
 * <p>
 * Valida los datos recibidos y las reglas del conjunto antes de llamar al DAO, y comunica los
 * errores con las excepciones del paquete {@code exception}.
 */
public class VisitaService {
	
	/** Acceso a datos de las visitas. */
	private final VisitaDAO visitaDao = new VisitaDAO();
	/** Acceso a datos de los roles. */
	private final RolDAO rolDao = new RolDAO();
	
	/**
	 * Lista las visitas registradas.
	 *
	 * @return lista de visitas; vacía si no hay registros
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 */
	public List<Visita> listar() throws SQLException {
		return visitaDao.listar();
	}
	
	/**
	 * Consulta una visita por su llave primaria.
	 *
	 * @param id identificador de la visita
	 * @return visita encontrada
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 * @throws RecursoNoEncontradoException si la visita no existe
	 */
	public Visita consultar(int id) throws SQLException, RecursoNoEncontradoException {
		Visita v = visitaDao.buscar(id);
		if (v == null) {
			throw new RecursoNoEncontradoException("Visita no encontrada");
		}
		return v;
	}

	/**
	 * Registra una visita. La persona debe tener el rol de visitante.
	 *
	 * @param v visita que se va a crear
	 * @return identificador de la visita creada
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 * @throws DatoInvalidoException si algún dato no cumple las validaciones
	 * @throws ConflictoException si la persona no tiene el rol de visitante
	 */
	public int crear(Visita v) throws SQLException, DatoInvalidoException, ConflictoException {
		if (v.getCasaNumero() <= 0 || v.getCasaManzana() <= 0
				|| v.getVisitanteTipoDocumento() == null || v.getVisitanteTipoDocumento().isBlank()
				|| v.getVisitanteNumeroDocumento() == null || v.getVisitanteNumeroDocumento().isBlank()) {
			throw new DatoInvalidoException(
					"casaNumero, casaManzana, visitanteTipoDocumento y visitanteNumeroDocumento son obligatorios");
		}
		validarFechas(v);
		if (!rolDao.tieneRol("visitante", v.getVisitanteTipoDocumento(), v.getVisitanteNumeroDocumento())) {
			throw new ConflictoException("La persona no tiene el rol de visitante");
		}
		return visitaDao.insertar(v);
	}

	/**
	 * Actualiza las fechas de una visita.
	 *
	 * @param v visita con su identificador y las fechas nuevas
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 * @throws DatoInvalidoException si algún dato no cumple las validaciones
	 * @throws RecursoNoEncontradoException si la visita no existe
	 */
	public void actualizar(Visita v) throws SQLException, DatoInvalidoException, RecursoNoEncontradoException {
		validarFechas(v);
		if (!visitaDao.actualizar(v)) {
			throw new RecursoNoEncontradoException("Visita no encontrada");
		}
	}

	/**
	 * Elimina una visita.
	 *
	 * @param id identificador de la visita
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 * @throws RecursoNoEncontradoException si la visita no existe
	 */
	public void eliminar(int id) throws SQLException, RecursoNoEncontradoException {
		if (!visitaDao.eliminar(id)) {
			throw new RecursoNoEncontradoException("Visita no encontrada");
		}
	}

	/**
	 * Valida que la entrada exista, el formato de las fechas y que la salida no sea anterior a la
	 * entrada. Una salida vacía se guarda como {@code null}.
	 *
	 * @param v visita que se valida
	 * @throws DatoInvalidoException si algún dato no cumple las validaciones
	 */
	private void validarFechas(Visita v) throws DatoInvalidoException {
		if (v.getFechaSalida() != null && v.getFechaSalida().isBlank()) {
			v.setFechaSalida(null);
		}
		if (v.getFechaEntrada() == null || v.getFechaEntrada().isBlank()) {
			throw new DatoInvalidoException("fechaEntrada es obligatoria (formato AAAA-MM-DD)");
		}
		try {
			LocalDate entrada = LocalDate.parse(v.getFechaEntrada());
			if (v.getFechaSalida() != null && LocalDate.parse(v.getFechaSalida()).isBefore(entrada)) {
				throw new DatoInvalidoException("fechaSalida no puede ser anterior a fechaEntrada");
			}
		} catch (DateTimeParseException e) {
			throw new DatoInvalidoException("Las fechas deben tener el formato AAAA-MM-DD");
		}
	}
}
