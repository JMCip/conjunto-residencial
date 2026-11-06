package co.edu.unbosque.service;

import java.sql.SQLException;
import java.util.List;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import co.edu.unbosque.dao.RolDAO;
import co.edu.unbosque.exception.ConflictoException;
import co.edu.unbosque.exception.DatoInvalidoException;
import co.edu.unbosque.exception.RecursoNoEncontradoException;
import java.util.Set;
import co.edu.unbosque.dao.ArriendoDAO;
import co.edu.unbosque.model.Arriendo;

/**
 * Lógica de negocio de los contratos de arriendo.
 * <p>
 * Valida los datos recibidos y las reglas del conjunto antes de llamar al DAO, y comunica los
 * errores con las excepciones del paquete {@code exception}.
 */
public class ArriendoService {
	
	/** Acceso a datos de los contratos de arriendo. */
	private final ArriendoDAO arriendoDao = new ArriendoDAO();
	/** Estados válidos de un contrato. */
	private static final Set<String> ESTADOS = Set.of("VIGENTE", "FINALIZADO");
	/** Acceso a datos de los roles. */
	private final RolDAO rolDao = new RolDAO();
	
	/**
	 * Lista los contratos de arriendo registrados.
	 *
	 * @return lista de contratos de arriendo; vacía si no hay registros
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 */
	public List<Arriendo> listar() throws SQLException {
		return arriendoDao.listar();
	}
	
	/** Estado de un contrato vigente. */
	private static final String VIGENTE = "VIGENTE";

	/**
	 * Consulta un contrato de arriendo por su llave primaria.
	 *
	 * @param id identificador del contrato de arriendo
	 * @return contrato de arriendo encontrado
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 * @throws RecursoNoEncontradoException si el contrato de arriendo no existe
	 */
	public Arriendo consultar(int id) throws SQLException, RecursoNoEncontradoException {
		Arriendo a = arriendoDao.buscar(id);
		if (a == null) {
			throw new RecursoNoEncontradoException("Contrato no encontrado");
		}
		return a;
	}

	/**
	 * Registra un contrato de arriendo. Exige los documentos y la casa, valida los datos, verifica que
	 * el arrendatario tenga ese rol y no sea propietario de la casa, que quien firma sea propietario
	 * vigente de la casa y que la casa no tenga ya un contrato vigente (regla S-3).
	 *
	 * @param a contrato que se va a crear
	 * @return identificador del contrato creado
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 * @throws DatoInvalidoException si algún dato no cumple las validaciones
	 * @throws ConflictoException si falta un rol, el propietario no es vigente o la casa ya tiene un
	 *         contrato vigente
	 */
	public int crear(Arriendo a) throws SQLException, DatoInvalidoException, ConflictoException {
		if (a.getCasaNumero() <= 0 || a.getCasaManzana() <= 0
				|| a.getArrendatarioTipoDocumento() == null || a.getArrendatarioTipoDocumento().isBlank()
				|| a.getArrendatarioNumeroDocumento() == null || a.getArrendatarioNumeroDocumento().isBlank()
				|| a.getPropietarioTipoDocumento() == null || a.getPropietarioTipoDocumento().isBlank()
				|| a.getPropietarioNumeroDocumento() == null || a.getPropietarioNumeroDocumento().isBlank()) {
			throw new DatoInvalidoException(
					"casaNumero, casaManzana y el documento del arrendatario y del propietario son obligatorios");
		}
		validarDatos(a);
		
		if (!rolDao.tieneRol("arrendatario", a.getArrendatarioTipoDocumento(), a.getArrendatarioNumeroDocumento())) {
			throw new ConflictoException("La persona no tiene el rol de arrendatario");
		}
		if (!rolDao.tieneRol("propietario", a.getPropietarioTipoDocumento(), a.getPropietarioNumeroDocumento())) {
			throw new ConflictoException("La persona que firma no tiene el rol de propietario");
		}

		if (arriendoDao.esPropietarioVigente(a.getCasaNumero(), a.getCasaManzana(),
				a.getArrendatarioTipoDocumento(), a.getArrendatarioNumeroDocumento())) {
			throw new ConflictoException("El arrendatario es propietario de esa casa");
		}
		
		if (!arriendoDao.esPropietarioVigente(a.getCasaNumero(), a.getCasaManzana(),
				a.getPropietarioTipoDocumento(), a.getPropietarioNumeroDocumento())) {
			throw new ConflictoException("El propietario que firma no es propietario vigente de esa casa");
		}
		validarUnicoVigente(a, a.getCasaNumero(), a.getCasaManzana(), 0);
		return arriendoDao.insertar(a);
	}

	/**
	 * Actualiza la cuota, las fechas y el estado de un contrato. La casa y las personas no cambian.
	 *
	 * @param a contrato con su identificador y los datos nuevos
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 * @throws DatoInvalidoException si algún dato no cumple las validaciones
	 * @throws RecursoNoEncontradoException si el contrato de arriendo no existe
	 * @throws ConflictoException si el contrato queda vigente y la casa ya tiene otro vigente
	 */
	public void actualizar(Arriendo a)
			throws SQLException, DatoInvalidoException, RecursoNoEncontradoException, ConflictoException {
		Arriendo actual = consultar(a.getIdContrato());
		validarDatos(a);
		validarUnicoVigente(a, actual.getCasaNumero(), actual.getCasaManzana(), a.getIdContrato());
		if (!arriendoDao.actualizar(a)) {
			throw new RecursoNoEncontradoException("Contrato no encontrado");
		}
	}

	/**
	 * Elimina un contrato de arriendo.
	 *
	 * @param id identificador del contrato de arriendo
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 * @throws RecursoNoEncontradoException si el contrato de arriendo no existe
	 */
	public void eliminar(int id) throws SQLException, RecursoNoEncontradoException {
		if (!arriendoDao.eliminar(id)) {
			throw new RecursoNoEncontradoException("Contrato no encontrado");
		}
	}

	/**
	 * Valida la cuota, el estado (lo pasa a mayúsculas) y las fechas del contrato. Una fecha de fin
	 * vacía se guarda como {@code null}.
	 *
	 * @param a contrato que se valida
	 * @throws DatoInvalidoException si algún dato no cumple las validaciones
	 */
	private void validarDatos(Arriendo a) throws DatoInvalidoException {
		if (a.getCuotaMensual() == null || a.getCuotaMensual().signum() <= 0) {
			throw new DatoInvalidoException("cuotaMensual debe ser mayor que 0");
		}
		if (a.getEstadoContrato() == null || a.getEstadoContrato().isBlank()) {
			throw new DatoInvalidoException("estadoContrato es obligatorio");
		}
		a.setEstadoContrato(a.getEstadoContrato().trim().toUpperCase());
		if (!ESTADOS.contains(a.getEstadoContrato())) {
			throw new DatoInvalidoException("estadoContrato debe ser VIGENTE o FINALIZADO");
		}
		if (a.getFechaFin() != null && a.getFechaFin().isBlank()) {
			a.setFechaFin(null);
		}
		try {
			if (a.getFechaInicio() == null) {
				throw new DatoInvalidoException("fechaInicio es obligatoria (formato AAAA-MM-DD)");
			}
			LocalDate inicio = LocalDate.parse(a.getFechaInicio());
			if (a.getFechaFin() != null && LocalDate.parse(a.getFechaFin()).isBefore(inicio)) {
				throw new DatoInvalidoException("fechaFin no puede ser anterior a fechaInicio");
			}
		} catch (DateTimeParseException e) {
			throw new DatoInvalidoException("Las fechas deben tener el formato AAAA-MM-DD");
		}
	}

	/**
	 * Verifica que la casa no tenga otro contrato vigente si este queda vigente (regla S-3: como
	 * máximo un contrato vigente por casa).
	 *
	 * @param a contrato que se valida
	 * @param numero número de la casa
	 * @param manzana manzana de la casa
	 * @param excluirId identificador del registro que no se tiene en cuenta (el que se está
	 *         actualizando); {@code 0} al crear
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 * @throws ConflictoException si la casa ya tiene otro contrato vigente
	 */
	private void validarUnicoVigente(Arriendo a, int numero, int manzana, int excluirId)
			throws SQLException, ConflictoException {
		if (VIGENTE.equalsIgnoreCase(a.getEstadoContrato())
				&& arriendoDao.existeVigente(numero, manzana, excluirId)) {
			throw new ConflictoException("La casa ya tiene un contrato de arriendo vigente");
		}
	}

}
