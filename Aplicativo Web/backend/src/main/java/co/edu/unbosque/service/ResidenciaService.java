package co.edu.unbosque.service;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import co.edu.unbosque.dao.RolDAO;
import co.edu.unbosque.exception.ConflictoException;
import co.edu.unbosque.exception.DatoInvalidoException;
import co.edu.unbosque.exception.RecursoNoEncontradoException;
import java.sql.SQLException;
import java.util.List;

import co.edu.unbosque.dao.ResidenciaDAO;
import co.edu.unbosque.model.Residencia;

/**
 * Lógica de negocio de las residencias.
 * <p>
 * Valida los datos recibidos y las reglas del conjunto antes de llamar al DAO, y comunica los
 * errores con las excepciones del paquete {@code exception}.
 */
public class ResidenciaService {
	
	/** Acceso a datos de las residencias. */
	private final ResidenciaDAO residenciaDao = new ResidenciaDAO();
	/** Acceso a datos de los roles. */
	private final RolDAO rolDao = new RolDAO();
	
	/**
	 * Lista las residencias registradas.
	 *
	 * @return lista de residencias; vacía si no hay registros
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 */
	public List<Residencia> listar() throws SQLException {
		return residenciaDao.listar();
	}
	
	/**
	 * Consulta una residencia por su llave primaria.
	 *
	 * @param id identificador de la residencia
	 * @return residencia encontrada
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 * @throws RecursoNoEncontradoException si la residencia no existe
	 */
	public Residencia consultar(int id) throws SQLException, RecursoNoEncontradoException {
		Residencia r = residenciaDao.buscar(id);
		if (r == null) {
			throw new RecursoNoEncontradoException("Residencia no encontrada");
		}
		return r;
	}

	/**
	 * Registra una residencia. La persona debe tener el rol de residente y, si queda vigente, no puede
	 * tener ya una residencia vigente en la misma casa.
	 *
	 * @param r residencia que se va a crear
	 * @return identificador de la residencia creada
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 * @throws DatoInvalidoException si algún dato no cumple las validaciones
	 * @throws ConflictoException si la persona no es residente o ya es residente vigente de la casa
	 */
	public int crear(Residencia r) throws SQLException, DatoInvalidoException, ConflictoException {
		if (r.getCasaNumero() <= 0 || r.getCasaManzana() <= 0
				|| r.getResidenteTipoDocumento() == null || r.getResidenteTipoDocumento().isBlank()
				|| r.getResidenteNumeroDocumento() == null || r.getResidenteNumeroDocumento().isBlank()) {
			throw new DatoInvalidoException(
					"casaNumero, casaManzana, residenteTipoDocumento y residenteNumeroDocumento son obligatorios");
		}
		validarFechas(r);
		
		if (!rolDao.tieneRol("residente", r.getResidenteTipoDocumento(), r.getResidenteNumeroDocumento())) {
			throw new ConflictoException("La persona no tiene el rol de residente");
		}
		
		if (r.getFechaSalida() == null && residenciaDao.existeVigente(r.getResidenteTipoDocumento(),
				r.getResidenteNumeroDocumento(), r.getCasaNumero(), r.getCasaManzana(), 0)) {
			throw new ConflictoException("La persona ya figura como residente vigente de esa casa");
		}
		return residenciaDao.insertar(r);
	}

	/**
	 * Actualiza las fechas de una residencia sin dejar dos residencias vigentes de la misma persona en
	 * la misma casa.
	 *
	 * @param r residencia con su identificador y las fechas nuevas
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 * @throws DatoInvalidoException si algún dato no cumple las validaciones
	 * @throws RecursoNoEncontradoException si la residencia no existe
	 * @throws ConflictoException si la persona ya es residente vigente de la casa
	 */
	public void actualizar(Residencia r)
			throws SQLException, DatoInvalidoException, RecursoNoEncontradoException, ConflictoException {
		Residencia actual = consultar(r.getIdResidencia());
		validarFechas(r);
		if (r.getFechaSalida() == null && residenciaDao.existeVigente(actual.getResidenteTipoDocumento(),
				actual.getResidenteNumeroDocumento(), actual.getCasaNumero(), actual.getCasaManzana(),
				r.getIdResidencia())) {
			throw new ConflictoException("La persona ya figura como residente vigente de esa casa");
		}
		if (!residenciaDao.actualizar(r)) {
			throw new RecursoNoEncontradoException("Residencia no encontrada");
		}
	}

	/**
	 * Elimina una residencia.
	 *
	 * @param id identificador de la residencia
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 * @throws RecursoNoEncontradoException si la residencia no existe
	 */
	public void eliminar(int id) throws SQLException, RecursoNoEncontradoException {
		if (!residenciaDao.eliminar(id)) {
			throw new RecursoNoEncontradoException("Residencia no encontrada");
		}
	}

	/**
	 * Valida que el ingreso exista, el formato de las fechas y que la salida no sea anterior al
	 * ingreso. Una salida vacía se guarda como {@code null}.
	 *
	 * @param r residencia que se valida
	 * @throws DatoInvalidoException si algún dato no cumple las validaciones
	 */
	private void validarFechas(Residencia r) throws DatoInvalidoException {
		if (r.getFechaSalida() != null && r.getFechaSalida().isBlank()) {
			r.setFechaSalida(null);
		}
		if (r.getFechaIngreso() == null || r.getFechaIngreso().isBlank()) {
			throw new DatoInvalidoException("fechaIngreso es obligatoria (formato AAAA-MM-DD)");
		}
		try {
			LocalDate ingreso = LocalDate.parse(r.getFechaIngreso());
			if (r.getFechaSalida() != null && LocalDate.parse(r.getFechaSalida()).isBefore(ingreso)) {
				throw new DatoInvalidoException("fechaSalida no puede ser anterior a fechaIngreso");
			}
		} catch (DateTimeParseException e) {
			throw new DatoInvalidoException("Las fechas deben tener el formato AAAA-MM-DD");
		}
	}
	
}
