package co.edu.unbosque.service;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import co.edu.unbosque.dao.RolDAO;
import co.edu.unbosque.exception.ConflictoException;
import co.edu.unbosque.exception.DatoInvalidoException;
import co.edu.unbosque.exception.RecursoNoEncontradoException;
import java.sql.SQLException;
import java.util.List;

import co.edu.unbosque.dao.PosesionDAO;
import co.edu.unbosque.model.Posesion;

/**
 * Lógica de negocio de las posesiones.
 * <p>
 * Valida los datos recibidos y las reglas del conjunto antes de llamar al DAO, y comunica los
 * errores con las excepciones del paquete {@code exception}.
 */
public class PosesionService {
	
	/** Acceso a datos de las posesiones. */
	private final PosesionDAO posesionDao = new PosesionDAO();
	/** Acceso a datos de los roles. */
	private final RolDAO rolDao = new RolDAO();
	
	/**
	 * Lista las posesiones registradas.
	 *
	 * @return lista de posesiones; vacía si no hay registros
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 */
	public List<Posesion> listar() throws SQLException {
		return posesionDao.listar();
	}
	
	/**
	 * Cien por ciento: límite del porcentaje de una posesión y de la suma de las vigentes de una casa.
	 */
	private static final BigDecimal CIEN = new BigDecimal("100");

	/**
	 * Consulta una posesión por su llave primaria.
	 *
	 * @param id identificador de la posesión
	 * @return posesión encontrada
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 * @throws RecursoNoEncontradoException si la posesión no existe
	 */
	public Posesion consultar(int id) throws SQLException, RecursoNoEncontradoException {
		Posesion p = posesionDao.buscar(id);
		if (p == null) {
			throw new RecursoNoEncontradoException("Posesión no encontrada");
		}
		return p;
	}

	/**
	 * Registra una posesión. La persona debe tener el rol de propietario y, si la posesión queda
	 * vigente, la suma de los porcentajes vigentes de la casa no puede pasar de 100.
	 *
	 * @param p posesión que se va a crear
	 * @return identificador de la posesión creada
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 * @throws DatoInvalidoException si algún dato no cumple las validaciones
	 * @throws ConflictoException si la persona no es propietaria o los porcentajes pasarían de 100
	 */
	public int crear(Posesion p) throws SQLException, DatoInvalidoException, ConflictoException {
		if (p.getCasaNumero() <= 0 || p.getCasaManzana() <= 0
				|| p.getPropietarioTipoDocumento() == null || p.getPropietarioTipoDocumento().isBlank()
				|| p.getPropietarioNumeroDocumento() == null || p.getPropietarioNumeroDocumento().isBlank()) {
			throw new DatoInvalidoException(
					"casaNumero, casaManzana, propietarioTipoDocumento y propietarioNumeroDocumento son obligatorios");
		}
		validarDatos(p);
		if (!rolDao.tieneRol("propietario", p.getPropietarioTipoDocumento(), p.getPropietarioNumeroDocumento())) {
			throw new ConflictoException("La persona no tiene el rol de propietario");
		}
		validarSuma(p.getCasaNumero(), p.getCasaManzana(), p, 0);
		return posesionDao.insertar(p);
	}

	/**
	 * Actualiza el porcentaje y las fechas de una posesión, sin que los porcentajes vigentes de la
	 * casa pasen de 100.
	 *
	 * @param p posesión con su identificador y los datos nuevos
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 * @throws DatoInvalidoException si algún dato no cumple las validaciones
	 * @throws RecursoNoEncontradoException si la posesión no existe
	 * @throws ConflictoException si los porcentajes vigentes pasarían de 100
	 */
	public void actualizar(Posesion p)
			throws SQLException, DatoInvalidoException, RecursoNoEncontradoException, ConflictoException {
		Posesion actual = consultar(p.getIdPosesion());
		validarDatos(p);
		validarSuma(actual.getCasaNumero(), actual.getCasaManzana(), p, p.getIdPosesion());
		if (!posesionDao.actualizar(p)) {
			throw new RecursoNoEncontradoException("Posesión no encontrada");
		}
	}

	/**
	 * Elimina una posesión.
	 *
	 * @param id identificador de la posesión
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 * @throws RecursoNoEncontradoException si la posesión no existe
	 */
	public void eliminar(int id) throws SQLException, RecursoNoEncontradoException {
		if (!posesionDao.eliminar(id)) {
			throw new RecursoNoEncontradoException("Posesión no encontrada");
		}
	}

	/**
	 * Valida el porcentaje (mayor que 0 y máximo 100) y las fechas. Una fecha de venta vacía se guarda
	 * como {@code null}.
	 *
	 * @param p posesión que se valida
	 * @throws DatoInvalidoException si algún dato no cumple las validaciones
	 */
	private void validarDatos(Posesion p) throws DatoInvalidoException {
		if (p.getPorcentaje() == null || p.getPorcentaje().signum() <= 0 || p.getPorcentaje().compareTo(CIEN) > 0) {
			throw new DatoInvalidoException("porcentaje debe ser mayor que 0 y máximo 100");
		}
		if (p.getFechaVenta() != null && p.getFechaVenta().isBlank()) {
			p.setFechaVenta(null);
		}
		try {
			if (p.getFechaCompra() == null) {
				throw new DatoInvalidoException("fechaCompra es obligatoria (formato AAAA-MM-DD)");
			}
			LocalDate compra = LocalDate.parse(p.getFechaCompra());
			if (p.getFechaVenta() != null && LocalDate.parse(p.getFechaVenta()).isBefore(compra)) {
				throw new DatoInvalidoException("fechaVenta no puede ser anterior a fechaCompra");
			}
		} catch (DateTimeParseException e) {
			throw new DatoInvalidoException("Las fechas deben tener el formato AAAA-MM-DD");
		}
	}

	/**
	 * Verifica que, si la posesión queda vigente, la suma de los porcentajes vigentes de la casa no
	 * pase de 100 (regla S-3).
	 *
	 * @param numero número de la casa
	 * @param manzana manzana de la casa
	 * @param p posesión que se valida
	 * @param excluirId identificador del registro que no se tiene en cuenta (el que se está
	 *         actualizando); {@code 0} al crear
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 * @throws ConflictoException si la suma pasaría de 100
	 */
	private void validarSuma(int numero, int manzana, Posesion p, int excluirId)
			throws SQLException, ConflictoException {
		if (p.getFechaVenta() != null) {
			return;
		}
		BigDecimal otros = posesionDao.sumaPorcentajeVigente(numero, manzana, excluirId);
		if (otros.add(p.getPorcentaje()).compareTo(CIEN) > 0) {
			throw new ConflictoException("Los porcentajes vigentes de la casa sumarían más de 100 (hoy: " + otros + ")");
		}
	}
}
