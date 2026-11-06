package co.edu.unbosque.service;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import co.edu.unbosque.dao.SancionDAO;
import co.edu.unbosque.exception.ConflictoException;
import co.edu.unbosque.exception.DatoInvalidoException;
import co.edu.unbosque.exception.RecursoNoEncontradoException;
import co.edu.unbosque.model.Sancion;

/**
 * Lógica de negocio de las sanciones.
 * <p>
 * Valida los datos recibidos y las reglas del conjunto antes de llamar al DAO, y comunica los
 * errores con las excepciones del paquete {@code exception}.
 */
public class SancionService {

	/** Decisiones con las que se cierra el proceso de descargos. */
	private static final Set<String> DECISIONES = Set.of("CONFIRMADA", "ANULADA");

	/** Texto de descargos que se guarda cuando la sanción se crea sin descargos. */
	private static final String DESCARGOS_PENDIENTES = "Pendiente de descargos";

	/** Acceso a datos de las sanciones. */
	private final SancionDAO sancionDao = new SancionDAO();

	/**
	 * Lista las sanciones registradas.
	 *
	 * @return lista de sanciones; vacía si no hay registros
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 */
	public List<Sancion> listar() throws SQLException {
		return sancionDao.listar();
	}

	/**
	 * Registra una sanción {@code EN_DESCARGOS} para una casa. El tipo se guarda en mayúsculas y, si
	 * no llegan descargos, se guarda un texto de descargos pendientes. El valor de la multa no se
	 * define al crear.
	 *
	 * @param s sanción que se va a crear
	 * @return identificador de la sanción creada
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 * @throws DatoInvalidoException si algún dato no cumple las validaciones
	 * @throws RecursoNoEncontradoException si la casa no existe
	 */
	public int crear(Sancion s) throws SQLException, DatoInvalidoException, RecursoNoEncontradoException {
		validarCampos(s);
		LocalDate incidente = parsear("fechaIncidente", s.getFechaIncidente());
		LocalDate notificacion = parsear("fechaNotificacion", s.getFechaNotificacion());
		validarFechas(incidente, notificacion);

		if (!sancionDao.existeCasa(s.getCasaNumero(), s.getCasaManzana())) {
			throw new RecursoNoEncontradoException("Casa no encontrada");
		}

		s.setTipo(s.getTipo().trim().toUpperCase());
		if (s.getDescargos() == null || s.getDescargos().isBlank()) {
			s.setDescargos(DESCARGOS_PENDIENTES);
		}
		s.setEstadoProceso("EN_DESCARGOS");
		return sancionDao.insertar(s, incidente, notificacion);
	}

	/**
	 * Registra los descargos y/o la decisión ({@code CONFIRMADA} con valor de multa, o
	 * {@code ANULADA}) de una sanción {@code EN_DESCARGOS}.
	 *
	 * @param id identificador de la sanción
	 * @param cambios descargos, estado del proceso y valor de la multa
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 * @throws DatoInvalidoException si algún dato no cumple las validaciones
	 * @throws RecursoNoEncontradoException si la sanción no existe
	 * @throws ConflictoException si la sanción ya no está {@code EN_DESCARGOS}
	 */
	public void actualizar(int id, Sancion cambios) throws SQLException, DatoInvalidoException,
			RecursoNoEncontradoException, ConflictoException {
		String descargos = cambios.getDescargos();
		String decision = cambios.getEstadoProceso();
		BigDecimal valor = cambios.getValor();

		boolean hayDescargos = descargos != null && !descargos.isBlank();
		boolean hayDecision = decision != null && !decision.isBlank();

		if (!hayDescargos && !hayDecision) {
			throw new DatoInvalidoException("Envía descargos, estadoProceso (CONFIRMADA o ANULADA), o ambos");
		}
		if (hayDescargos && descargos.length() > 500) {
			throw new DatoInvalidoException("descargos no puede superar 500 caracteres");
		}
		if (hayDecision) {
			decision = decision.toUpperCase();
			if (!DECISIONES.contains(decision)) {
				throw new DatoInvalidoException("estadoProceso debe ser CONFIRMADA o ANULADA");
			}
		}
		if (!hayDecision && valor != null) {
			throw new DatoInvalidoException("El valor de la multa solo se define al decidir (CONFIRMADA)");
		}
		if ("ANULADA".equals(decision) && valor != null) {
			throw new DatoInvalidoException("Una sanción ANULADA no lleva valor");
		}
		if (valor != null && valor.compareTo(BigDecimal.ZERO) <= 0) {
			throw new DatoInvalidoException("valor de la multa debe ser mayor que 0");
		}

		Sancion actual = sancionDao.buscarPorId(id);
		if (actual == null) {
			throw new RecursoNoEncontradoException("Sanción no encontrada");
		}
		if (!"EN_DESCARGOS".equals(actual.getEstadoProceso())) {
			throw new ConflictoException(
					"Solo se puede modificar una sanción EN_DESCARGOS (estado actual: "
							+ actual.getEstadoProceso() + ")");
		}

		String descargosFinal = hayDescargos ? descargos : actual.getDescargos();
		String estadoFinal = hayDecision ? decision : "EN_DESCARGOS";

		if (!sancionDao.actualizarProceso(id, descargosFinal, estadoFinal, valor)) {
			throw new ConflictoException("La sanción ya no está EN_DESCARGOS");
		}
	}

	/**
	 * Elimina una sanción {@code ANULADA} que no se haya facturado.
	 *
	 * @param id identificador de la sanción
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 * @throws RecursoNoEncontradoException si la sanción no existe
	 * @throws ConflictoException si la sanción no está {@code ANULADA} o ya fue facturada
	 */
	public void eliminar(int id) throws SQLException, RecursoNoEncontradoException, ConflictoException {
		Sancion actual = sancionDao.buscarPorId(id);
		if (actual == null) {
			throw new RecursoNoEncontradoException("Sanción no encontrada");
		}
		if (!"ANULADA".equals(actual.getEstadoProceso())) {
			throw new ConflictoException(
					"Solo se puede eliminar una sanción ANULADA (estado actual: "
							+ actual.getEstadoProceso() + ")");
		}
		try {
			if (!sancionDao.eliminar(id)) {
				throw new RecursoNoEncontradoException("Sanción no encontrada");
			}
		} catch (SQLException e) {
			if ("23503".equals(e.getSQLState())) {
				throw new ConflictoException("La sanción ya fue facturada y no se puede eliminar");
			}
			throw e;
		}
	}

	/**
	 * Valida el tipo, la descripción y los descargos, y que el valor de la multa no se envíe al crear.
	 *
	 * @param s sanción que se valida
	 * @throws DatoInvalidoException si algún dato no cumple las validaciones
	 */
	private void validarCampos(Sancion s) throws DatoInvalidoException {
		if (s.getTipo() == null || s.getTipo().isBlank()) {
			throw new DatoInvalidoException("tipo es obligatorio");
		}
		if (s.getTipo().trim().length() > 30) {
			throw new DatoInvalidoException("tipo no puede superar 30 caracteres");
		}
		if (s.getDescripcion() == null || s.getDescripcion().isBlank()) {
			throw new DatoInvalidoException("descripcion es obligatoria");
		}
		if (s.getDescripcion().length() > 255) {
			throw new DatoInvalidoException("descripcion no puede superar 255 caracteres");
		}
		if (s.getDescargos() != null && s.getDescargos().length() > 500) {
			throw new DatoInvalidoException("descargos no puede superar 500 caracteres");
		}
		if (s.getValor() != null) {
			throw new DatoInvalidoException(
					"El valor de la multa se define al decidir la sanción (PUT con estadoProceso CONFIRMADA)");
		}
	}

	/**
	 * Valida que las fechas no sean futuras y que la notificación no sea anterior al incidente.
	 *
	 * @param incidente fecha del incidente
	 * @param notificacion fecha de notificación
	 * @throws DatoInvalidoException si algún dato no cumple las validaciones
	 */
	private void validarFechas(LocalDate incidente, LocalDate notificacion) throws DatoInvalidoException {
		LocalDate hoy = LocalDate.now();
		if (incidente.isAfter(hoy)) {
			throw new DatoInvalidoException("fechaIncidente no puede ser futura");
		}
		if (notificacion.isBefore(incidente)) {
			throw new DatoInvalidoException("fechaNotificacion no puede ser anterior a fechaIncidente");
		}
		if (notificacion.isAfter(hoy)) {
			throw new DatoInvalidoException("fechaNotificacion no puede ser futura");
		}
	}

	/**
	 * Convierte el texto de una fecha ({@code AAAA-MM-DD}) en un {@link LocalDate}.
	 *
	 * @param campo nombre del campo, para el mensaje de error
	 * @param texto fecha recibida
	 * @return fecha convertida
	 * @throws DatoInvalidoException si algún dato no cumple las validaciones
	 */
	private LocalDate parsear(String campo, String texto) throws DatoInvalidoException {
		if (texto == null || texto.isBlank()) {
			throw new DatoInvalidoException(campo + " es obligatoria");
		}
		try {
			return LocalDate.parse(texto);
		} catch (Exception e) {
			throw new DatoInvalidoException(campo + " debe tener formato yyyy-MM-dd, por ejemplo 2026-10-15");
		}
	}
}