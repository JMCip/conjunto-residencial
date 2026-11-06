package co.edu.unbosque.service;

import java.sql.SQLException;
import java.util.List;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

import co.edu.unbosque.exception.ConflictoException;
import co.edu.unbosque.exception.DatoInvalidoException;
import co.edu.unbosque.exception.RecursoNoEncontradoException;
import co.edu.unbosque.dao.ParqueoDAO;
import co.edu.unbosque.model.Parqueo;

/**
 * Lógica de negocio de los parqueos.
 * <p>
 * Valida los datos recibidos y las reglas del conjunto antes de llamar al DAO, y comunica los
 * errores con las excepciones del paquete {@code exception}.
 */
public class ParqueoService {
	
	/** Acceso a datos de los parqueos. */
	private final ParqueoDAO parqueoDao = new ParqueoDAO();
	
	/**
	 * Convierte una fecha y hora ({@code AAAA-MM-DD HH:MM:SS}) en un {@link LocalDateTime}.
	 *
	 * @param texto fecha y hora recibida
	 * @return fecha y hora convertida
	 */
	private LocalDateTime parsear(String texto) {
		return LocalDateTime.parse(texto.trim().replace(' ', 'T'));
	}
	
	/**
	 * Lista los parqueos registrados.
	 *
	 * @return lista de parqueos; vacía si no hay registros
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 */
	public List<Parqueo> listar() throws SQLException {
		return parqueoDao.listar();
	}
	
	/**
	 * Consulta un parqueo por su llave primaria.
	 *
	 * @param id identificador del parqueo
	 * @return parqueo encontrado
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 * @throws RecursoNoEncontradoException si el parqueo no existe
	 */
	public Parqueo consultar(int id) throws SQLException, RecursoNoEncontradoException {
		Parqueo p = parqueoDao.buscar(id);
		if (p == null) {
			throw new RecursoNoEncontradoException("Parqueo no encontrado");
		}
		return p;
	}

	/**
	 * Registra un parqueo. La placa se guarda en mayúsculas. Si queda abierto, ni la celda (regla
	 * S-33) ni el vehículo (regla S-31) pueden tener otro parqueo abierto.
	 *
	 * @param p parqueo que se va a crear
	 * @return identificador del parqueo creado
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 * @throws DatoInvalidoException si algún dato no cumple las validaciones
	 * @throws ConflictoException si la celda o el vehículo ya tienen un parqueo abierto
	 */
	public int crear(Parqueo p) throws SQLException, DatoInvalidoException, ConflictoException {
		if (p.getIdCelda() <= 0 || p.getVehiculoPlaca() == null || p.getVehiculoPlaca().isBlank()) {
			throw new DatoInvalidoException("vehiculoPlaca e idCelda son obligatorios");
		}
		p.setVehiculoPlaca(p.getVehiculoPlaca().toUpperCase());
		validarFechas(p);
		if (p.getFechaHoraSalida() == null) {
			if (parqueoDao.celdaConParqueoAbierto(p.getIdCelda(), 0)) {
				throw new ConflictoException("La celda ya tiene un parqueo abierto");
			}
			if (parqueoDao.vehiculoConParqueoAbierto(p.getVehiculoPlaca(), 0)) {
				throw new ConflictoException("El vehículo ya tiene un parqueo abierto");
			}
		}
		return parqueoDao.insertar(p);
	}

	/**
	 * Actualiza las fechas de un parqueo. Si queda abierto, ni la celda ni el vehículo pueden tener
	 * otro parqueo abierto.
	 *
	 * @param p parqueo con su identificador y las fechas nuevas
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 * @throws DatoInvalidoException si algún dato no cumple las validaciones
	 * @throws RecursoNoEncontradoException si el parqueo no existe
	 * @throws ConflictoException si la celda o el vehículo ya tienen otro parqueo abierto
	 */
	public void actualizar(Parqueo p)
			throws SQLException, DatoInvalidoException, RecursoNoEncontradoException, ConflictoException {
		Parqueo actual = consultar(p.getIdParqueo());
		validarFechas(p);
		if (p.getFechaHoraSalida() == null) {
			if (parqueoDao.celdaConParqueoAbierto(actual.getIdCelda(), p.getIdParqueo())) {
				throw new ConflictoException("La celda ya tiene un parqueo abierto");
			}
			if (parqueoDao.vehiculoConParqueoAbierto(actual.getVehiculoPlaca(), p.getIdParqueo())) {
				throw new ConflictoException("El vehículo ya tiene un parqueo abierto");
			}
		}
		if (!parqueoDao.actualizar(p)) {
			throw new RecursoNoEncontradoException("Parqueo no encontrado");
		}
	}

	/**
	 * Elimina un parqueo.
	 *
	 * @param id identificador del parqueo
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 * @throws RecursoNoEncontradoException si el parqueo no existe
	 */
	public void eliminar(int id) throws SQLException, RecursoNoEncontradoException {
		if (!parqueoDao.eliminar(id)) {
			throw new RecursoNoEncontradoException("Parqueo no encontrado");
		}
	}

	/**
	 * Valida que la entrada exista, el formato de las fechas y que la salida no sea anterior a la
	 * entrada. Una salida vacía se guarda como {@code null}.
	 *
	 * @param p parqueo que se valida
	 * @throws DatoInvalidoException si algún dato no cumple las validaciones
	 */
	private void validarFechas(Parqueo p) throws DatoInvalidoException {
		if (p.getFechaHoraSalida() != null && p.getFechaHoraSalida().isBlank()) {
			p.setFechaHoraSalida(null);
		}
		if (p.getFechaHoraEntrada() == null || p.getFechaHoraEntrada().isBlank()) {
			throw new DatoInvalidoException("fechaHoraEntrada es obligatoria (formato AAAA-MM-DD HH:MM:SS)");
		}
		try {
			LocalDateTime entrada = parsear(p.getFechaHoraEntrada());
			if (p.getFechaHoraSalida() != null && parsear(p.getFechaHoraSalida()).isBefore(entrada)) {
				throw new DatoInvalidoException("fechaHoraSalida no puede ser anterior a fechaHoraEntrada");
			}
		} catch (DateTimeParseException e) {
			throw new DatoInvalidoException("Las fechas deben tener el formato AAAA-MM-DD HH:MM:SS");
		}
	}
	
}
