package co.edu.unbosque.service;

import java.sql.SQLException;
import java.util.List;
import java.time.Year;

import co.edu.unbosque.exception.DatoInvalidoException;
import co.edu.unbosque.exception.RecursoNoEncontradoException;

import co.edu.unbosque.dao.VehiculoDAO;
import co.edu.unbosque.model.Vehiculo;
import java.util.Set;

/**
 * Lógica de negocio de los vehículos.
 * <p>
 * Valida los datos recibidos y las reglas del conjunto antes de llamar al DAO, y comunica los
 * errores con las excepciones del paquete {@code exception}.
 */
public class VehiculoService {
	
	/** Acceso a datos de los vehículos. */
	private final VehiculoDAO vehiculoDao = new VehiculoDAO();
	/** Tipos válidos de vehículo. */
	private static final Set<String> TIPOS = Set.of("CARRO", "MOTO");
	
	/**
	 * Lista los vehículos registrados.
	 *
	 * @return lista de vehículos; vacía si no hay registros
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 */
	public List<Vehiculo> listar() throws SQLException {
		return vehiculoDao.listar();
	}
	
	/**
	 * Consulta un vehículo por su llave primaria.
	 *
	 * @param placa placa del vehículo
	 * @return vehículo encontrado
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 * @throws RecursoNoEncontradoException si el vehículo no existe
	 */
	public Vehiculo consultar(String placa) throws SQLException, RecursoNoEncontradoException {
		Vehiculo v = vehiculoDao.buscar(placa.toUpperCase());
		if (v == null) {
			throw new RecursoNoEncontradoException("Vehículo no encontrado");
		}
		return v;
	}

	/**
	 * Registra un vehículo. La placa debe tener de 5 a 7 caracteres alfanuméricos y se guarda en
	 * mayúsculas.
	 *
	 * @param v vehículo que se va a crear
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 * @throws DatoInvalidoException si algún dato no cumple las validaciones
	 */
	public void crear(Vehiculo v) throws SQLException, DatoInvalidoException {
		if (v.getPlaca() == null || !v.getPlaca().matches("[A-Za-z0-9]{5,7}")) {
			throw new DatoInvalidoException("placa debe tener entre 5 y 7 caracteres alfanuméricos");
		}
		v.setPlaca(v.getPlaca().toUpperCase());
		validarDatos(v);
		vehiculoDao.insertar(v);
	}

	/**
	 * Actualiza los datos de un vehículo; la placa se busca en mayúsculas.
	 *
	 * @param v vehículo con su placa y los datos nuevos
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 * @throws DatoInvalidoException si algún dato no cumple las validaciones
	 * @throws RecursoNoEncontradoException si el vehículo no existe
	 */
	public void actualizar(Vehiculo v) throws SQLException, DatoInvalidoException, RecursoNoEncontradoException {
		v.setPlaca(v.getPlaca().toUpperCase());
		validarDatos(v);
		if (!vehiculoDao.actualizar(v)) {
			throw new RecursoNoEncontradoException("Vehículo no encontrado");
		}
	}

	/**
	 * Elimina un vehículo.
	 *
	 * @param placa placa del vehículo
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 * @throws RecursoNoEncontradoException si el vehículo no existe
	 */
	public void eliminar(String placa) throws SQLException, RecursoNoEncontradoException {
		if (!vehiculoDao.eliminar(placa.toUpperCase())) {
			throw new RecursoNoEncontradoException("Vehículo no encontrado");
		}
	}

	/**
	 * Valida el tipo (lo pasa a mayúsculas), el documento del dueño y el año del modelo.
	 *
	 * @param v vehículo que se valida
	 * @throws DatoInvalidoException si algún dato no cumple las validaciones
	 */
	private void validarDatos(Vehiculo v) throws DatoInvalidoException {
		if (v.getTipo() == null || v.getTipo().isBlank()
				|| v.getPersonaTipoDocumento() == null || v.getPersonaTipoDocumento().isBlank()
				|| v.getPersonaNumeroDocumento() == null || v.getPersonaNumeroDocumento().isBlank()) {
			throw new DatoInvalidoException("tipo, personaTipoDocumento y personaNumeroDocumento son obligatorios");
		}
		v.setTipo(v.getTipo().trim().toUpperCase());
		if (!TIPOS.contains(v.getTipo())) {
			throw new DatoInvalidoException("tipo debe ser CARRO o MOTO");
		}
		if (v.getModelo() != null && (v.getModelo() < 1900 || v.getModelo() > Year.now().getValue() + 1)) {
			throw new DatoInvalidoException("modelo debe ser un año entre 1900 y " + (Year.now().getValue() + 1));
		}
	}
	
}
