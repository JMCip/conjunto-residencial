package co.edu.unbosque.dao;
import co.edu.unbosque.config.ConexionBD;
import co.edu.unbosque.model.Vehiculo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a datos de los vehículos con JDBC.
 * <p>
 * Cada método abre su propia conexión con {@link co.edu.unbosque.config.ConexionBD} y usa
 * {@link java.sql.PreparedStatement} para enviar los parámetros de forma segura.
 */
public class VehiculoDAO {
	
	/** Consulta que lista todos los registros de la tabla {@code vehiculo}. */
	private static final String LISTAR = """
			SELECT placa, marca, modelo, tipo, persona_tipo_documento, persona_numero_documento
			FROM vehiculo
			ORDER BY placa
			""";
	
	/**
	 * Consulta los vehículos registrados.
	 *
	 * @return lista de vehículos; vacía si no hay registros
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 */
	public List<Vehiculo> listar() throws SQLException {
		List<Vehiculo> vehiculos = new ArrayList<>();
		try (	Connection c = ConexionBD.obtenerConexion();
				PreparedStatement ps = c.prepareStatement(LISTAR);
				ResultSet rs = ps.executeQuery()) {
			
			while (rs.next()) {
				vehiculos.add(mapear(rs));
			}
		}
		return vehiculos;
	}
	
	/**
	 * Convierte la fila actual del resultado en un vehículo.
	 *
	 * @param rs resultado de la consulta, ubicado en la fila que se va a leer
	 * @return vehículo leído
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 */
	private Vehiculo mapear(ResultSet rs) throws SQLException {
		return new Vehiculo(
				rs.getString("placa"),
				rs.getString("marca"),
				rs.getObject("modelo", Integer.class),
				rs.getString("tipo"),
				rs.getString("persona_tipo_documento"),
				rs.getString("persona_numero_documento")
				);
	}
	
	/** Consulta que busca un registro de la tabla {@code vehiculo} por su llave primaria. */
	private static final String BUSCAR = """
			SELECT placa, marca, modelo, tipo, persona_tipo_documento, persona_numero_documento
			FROM vehiculo
			WHERE placa = ?
			""";

	/**
	 * Busca un vehículo por su llave primaria.
	 *
	 * @param placa placa del vehículo
	 * @return vehículo encontrado, o {@code null} si no existe
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 */
	public Vehiculo buscar(String placa) throws SQLException {
		try (Connection c = ConexionBD.obtenerConexion();
				PreparedStatement ps = c.prepareStatement(BUSCAR)) {
			ps.setString(1, placa);
			try (ResultSet rs = ps.executeQuery()) {
				return rs.next() ? mapear(rs) : null;
			}
		}
	}

	/** Sentencia que inserta un registro en la tabla {@code vehiculo}. */
	private static final String INSERTAR = """
			INSERT INTO vehiculo (placa, marca, modelo, tipo, persona_tipo_documento, persona_numero_documento)
			VALUES (?, ?, ?, ?, ?, ?)
			""";

	/**
	 * Inserta un vehículo en la base de datos.
	 *
	 * @param v vehículo que se va a insertaro
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 */
	public void insertar(Vehiculo v) throws SQLException {
		try (Connection c = ConexionBD.obtenerConexion();
				PreparedStatement ps = c.prepareStatement(INSERTAR)) {
			ps.setString(1, v.getPlaca());
			ps.setString(2, v.getMarca());
			ps.setObject(3, v.getModelo());
			ps.setString(4, v.getTipo());
			ps.setString(5, v.getPersonaTipoDocumento());
			ps.setString(6, v.getPersonaNumeroDocumento());
			ps.executeUpdate();
		}
	}

	/** Sentencia que actualiza un registro de la tabla {@code vehiculo}. */
	private static final String ACTUALIZAR = """
			UPDATE vehiculo
			SET marca = ?,
			    modelo = ?,
			    tipo = ?,
			    persona_tipo_documento = ?,
			    persona_numero_documento = ?
			WHERE placa = ?
			""";

	/**
	 * Actualiza el vehículo indicado por su llave primaria.
	 *
	 * @param v vehículo con su llave primaria y los datos nuevoso
	 * @return {@code true} si el registro existía y se actualizó
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 */
	public boolean actualizar(Vehiculo v) throws SQLException {
		try (Connection c = ConexionBD.obtenerConexion();
				PreparedStatement ps = c.prepareStatement(ACTUALIZAR)) {
			ps.setString(1, v.getMarca());
			ps.setObject(2, v.getModelo());
			ps.setString(3, v.getTipo());
			ps.setString(4, v.getPersonaTipoDocumento());
			ps.setString(5, v.getPersonaNumeroDocumento());
			ps.setString(6, v.getPlaca());
			return ps.executeUpdate() > 0;
		}
	}

	/** Sentencia que elimina un registro de la tabla {@code vehiculo} por su llave primaria. */
	private static final String ELIMINAR = """
			DELETE FROM vehiculo
			WHERE placa = ?
			""";

	/**
	 * Elimina el vehículo indicado por su llave primaria.
	 *
	 * @param placa placa del vehículo
	 * @return {@code true} si el registro existía y se eliminó
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 */
	public boolean eliminar(String placa) throws SQLException {
		try (Connection c = ConexionBD.obtenerConexion();
				PreparedStatement ps = c.prepareStatement(ELIMINAR)) {
			ps.setString(1, placa);
			return ps.executeUpdate() > 0;
		}
	}
	
}
