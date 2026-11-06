package co.edu.unbosque.service;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import co.edu.unbosque.dao.FacturaDAO;
import co.edu.unbosque.exception.ConflictoException;
import co.edu.unbosque.exception.DatoInvalidoException;
import co.edu.unbosque.exception.RecursoNoEncontradoException;
import co.edu.unbosque.model.Factura;

/**
 * Lógica de negocio de las facturas.
 * <p>
 * Valida los datos recibidos y las reglas del conjunto antes de llamar al DAO, y comunica los
 * errores con las excepciones del paquete {@code exception}.
 */
public class FacturaService {

	/** Acceso a datos de las facturas. */
	private final FacturaDAO facturaDao = new FacturaDAO();

	/**
	 * Lista las facturas registradas.
	 *
	 * @return lista de facturas; vacía si no hay registros
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 */
	public List<Factura> listar() throws SQLException {
		return facturaDao.listar();
	}

	/**
	 * Crea una factura {@code PENDIENTE} para una casa. Valida el periodo y las fechas, que la casa
	 * exista y que no tenga ya una factura del mismo periodo.
	 *
	 * @param f factura que se va a crear
	 * @return número de la factura creada
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 * @throws DatoInvalidoException si algún dato no cumple las validaciones
	 * @throws RecursoNoEncontradoException si la casa no existe
	 * @throws ConflictoException si la casa ya tiene una factura del periodo
	 */
	public int crear(Factura f) throws SQLException, DatoInvalidoException,
			RecursoNoEncontradoException, ConflictoException {
		YearMonth periodo = parsearPeriodo(f.getPeriodo());
		LocalDate emision = parsearFecha("fechaEmision", f.getFechaEmision());
		LocalDate vencimiento = parsearFecha("fechaVencimiento", f.getFechaVencimiento());
		if (vencimiento.isBefore(emision)) {
			throw new DatoInvalidoException("fechaVencimiento no puede ser anterior a fechaEmision");
		}

		if (!facturaDao.existeCasa(f.getCasaNumero(), f.getCasaManzana())) {
			throw new RecursoNoEncontradoException("Casa no encontrada");
		}
		
		if (facturaDao.existePeriodo(f.getCasaNumero(), f.getCasaManzana(), periodo.toString())) {
			throw new ConflictoException("La casa ya tiene una factura del periodo " + periodo);
		}

		f.setPeriodo(periodo.toString());
		f.setEstado("PENDIENTE");
		return facturaDao.insertar(f, emision, vencimiento);
	}

	/**
	 * Cambia la fecha de vencimiento de una factura {@code PENDIENTE} o {@code ABONADA}.
	 *
	 * @param id número de la factura
	 * @param cambios factura con la nueva fecha de vencimiento
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 * @throws DatoInvalidoException si algún dato no cumple las validaciones
	 * @throws RecursoNoEncontradoException si la factura no existe
	 * @throws ConflictoException si la factura no está {@code PENDIENTE} ni {@code ABONADA}
	 */
	public void actualizar(int id, Factura cambios) throws SQLException, DatoInvalidoException,
			RecursoNoEncontradoException, ConflictoException {
		LocalDate vencimiento = parsearFecha("fechaVencimiento", cambios.getFechaVencimiento());

		Factura actual = facturaDao.buscarPorId(id);
		if (actual == null) {
			throw new RecursoNoEncontradoException("Factura no encontrada");
		}
		if (!"PENDIENTE".equals(actual.getEstado()) && !"ABONADA".equals(actual.getEstado())) {
			throw new ConflictoException(
					"Solo se puede cambiar el vencimiento de una factura PENDIENTE o ABONADA (estado actual: "
							+ actual.getEstado() + ")");
		}
		if (vencimiento.isBefore(LocalDate.parse(actual.getFechaEmision()))) {
			throw new DatoInvalidoException("fechaVencimiento no puede ser anterior a fechaEmision");
		}

		if (!facturaDao.actualizarVencimiento(id, vencimiento)) {
			throw new ConflictoException("La factura ya no está PENDIENTE ni ABONADA");
		}
	}

	/**
	 * Elimina una factura {@code PENDIENTE} sin líneas ni pagos.
	 *
	 * @param id número de la factura
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 * @throws RecursoNoEncontradoException si la factura no existe
	 * @throws ConflictoException si la factura no está {@code PENDIENTE} o tiene líneas o pagos
	 */
	public void eliminar(int id) throws SQLException, RecursoNoEncontradoException, ConflictoException {
		Factura actual = facturaDao.buscarPorId(id);
		if (actual == null) {
			throw new RecursoNoEncontradoException("Factura no encontrada");
		}
		if (!"PENDIENTE".equals(actual.getEstado())) {
			throw new ConflictoException(
					"Solo se puede eliminar una factura PENDIENTE (estado actual: " + actual.getEstado() + ")");
		}
		try {
			if (!facturaDao.eliminar(id)) {
				throw new RecursoNoEncontradoException("Factura no encontrada");
			}
		} catch (SQLException e) {
			
			if ("23503".equals(e.getSQLState())) {
				throw new ConflictoException(
						"La factura tiene líneas de detalle o pagos asociados y no se puede eliminar");
			}
			throw e;
		}
	}

	/**
	 * Pasa a {@code VENCIDA} las facturas {@code PENDIENTE} o {@code ABONADA} cuya fecha de
	 * vencimiento ya pasó.
	 *
	 * @return cantidad de facturas marcadas como vencidas
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 */
	public int marcarVencidas() throws SQLException {
		return facturaDao.marcarVencidas(LocalDate.now());
	}

	/**
	 * Convierte el texto del periodo ({@code AAAA-MM}) en un {@link YearMonth}.
	 *
	 * @param texto periodo recibido
	 * @return periodo convertido
	 * @throws DatoInvalidoException si algún dato no cumple las validaciones
	 */
	private YearMonth parsearPeriodo(String texto) throws DatoInvalidoException {
		if (texto == null || texto.isBlank()) {
			throw new DatoInvalidoException("periodo es obligatorio");
		}
		try {
			return YearMonth.parse(texto);
		} catch (Exception e) {
			throw new DatoInvalidoException("periodo debe tener formato yyyy-MM, por ejemplo 2026-11");
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
	private LocalDate parsearFecha(String campo, String texto) throws DatoInvalidoException {
		if (texto == null || texto.isBlank()) {
			throw new DatoInvalidoException(campo + " es obligatoria");
		}
		try {
			return LocalDate.parse(texto);
		} catch (Exception e) {
			throw new DatoInvalidoException(campo + " debe tener formato yyyy-MM-dd, por ejemplo 2026-11-30");
		}
	}
}