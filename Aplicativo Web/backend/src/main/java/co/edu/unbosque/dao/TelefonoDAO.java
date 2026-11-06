package co.edu.unbosque.dao;
import co.edu.unbosque.config.ConexionBD;
import co.edu.unbosque.model.Telefono;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a datos de los teléfonos con JDBC.
 * <p>
 * Cada método abre su propia conexión con {@link co.edu.unbosque.config.ConexionBD} y usa
 * {@link java.sql.PreparedStatement} para enviar los parámetros de forma segura.
 */
public class TelefonoDAO {
	
	/** Consulta que lista todos los registros de la tabla {@code telefono}. */
	private static final String LISTAR = """
			SELECT telefono, persona_tipo_documento, persona_numero_documento
			FROM telefono
			ORDER BY persona_tipo_documento, persona_numero_documento, telefono
			""";
	
	/**
	 * Consulta los teléfonos registrados.
	 *
	 * @return lista de teléfonos; vacía si no hay registros
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 */
	public List<Telefono> listar() throws SQLException {
		List<Telefono> telefonos = new ArrayList<>();
		try (	Connection c = ConexionBD.obtenerConexion();
				PreparedStatement ps = c.prepareStatement(LISTAR);
				ResultSet rs = ps.executeQuery()) {
			
			while (rs.next()) {
				telefonos.add(mapear(rs));
			}
		}
		return telefonos;
	}
	
	/**
	 * Convierte la fila actual del resultado en un teléfono.
	 *
	 * @param rs resultado de la consulta, ubicado en la fila que se va a leer
	 * @return teléfono leído
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 */
	private Telefono mapear(ResultSet rs) throws SQLException {
		return new Telefono(
				rs.getString("telefono"),
				rs.getString("persona_tipo_documento"),
				rs.getString("persona_numero_documento")
				);
	}
	
	/** Consulta que busca un registro de la tabla {@code telefono} por su llave primaria. */
	private static final String BUSCAR = """
			SELECT telefono, persona_tipo_documento, persona_numero_documento
			FROM telefono
			WHERE CAST(telefono AS VARCHAR) = ?
			AND persona_tipo_documento = ?
			AND persona_numero_documento = ?
			""";

	/**
	 * Busca un teléfono por su llave primaria.
	 *
	 * @param telefono número de teléfono
	 * @param tipoDoc tipo de documento de la persona
	 * @param numeroDoc número de documento de la persona
	 * @return teléfono encontrado, o {@code null} si no existe
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 */
	public Telefono buscar(String telefono, String tipoDoc, String numeroDoc) throws SQLException {
		try (Connection c = ConexionBD.obtenerConexion();
				PreparedStatement ps = c.prepareStatement(BUSCAR)) {
			ps.setString(1, telefono);
			ps.setString(2, tipoDoc);
			ps.setString(3, numeroDoc);
			try (ResultSet rs = ps.executeQuery()) {
				return rs.next() ? mapear(rs) : null;
			}
		}
	}

	/** Sentencia que inserta un registro en la tabla {@code telefono}. */
	private static final String INSERTAR = """
			INSERT INTO telefono (telefono, persona_tipo_documento, persona_numero_documento)
			VALUES (CAST(? AS NUMERIC), ?, ?)
			""";

	/**
	 * Inserta un teléfono en la base de datos.
	 *
	 * @param t teléfono que se va a insertaro
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 */
	public void insertar(Telefono t) throws SQLException {
		try (Connection c = ConexionBD.obtenerConexion();
				PreparedStatement ps = c.prepareStatement(INSERTAR)) {
			ps.setString(1, t.getTelefono());
			ps.setString(2, t.getPersonaTipoDocumento());
			ps.setString(3, t.getPersonaNumeroDocumento());
			ps.executeUpdate();
		}
	}

	/** Sentencia que reemplaza un número de teléfono de una persona por otro. */
	private static final String ACTUALIZAR = """
			UPDATE telefono
			SET telefono = CAST(? AS NUMERIC)
			WHERE CAST(telefono AS VARCHAR) = ?
			AND persona_tipo_documento = ?
			AND persona_numero_documento = ?
			""";

	/**
	 * Reemplaza un número de teléfono de la persona por otro.
	 *
	 * @param telefonoActual número que se reemplaza
	 * @param telefonoNuevo número nuevo
	 * @param tipoDoc tipo de documento de la persona
	 * @param numeroDoc número de documento de la persona
	 * @return {@code true} si el teléfono existía y se actualizó
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 */
	public boolean actualizar(String telefonoActual, String telefonoNuevo, String tipoDoc, String numeroDoc)
			throws SQLException {
		try (Connection c = ConexionBD.obtenerConexion();
				PreparedStatement ps = c.prepareStatement(ACTUALIZAR)) {
			ps.setString(1, telefonoNuevo);
			ps.setString(2, telefonoActual);
			ps.setString(3, tipoDoc);
			ps.setString(4, numeroDoc);
			return ps.executeUpdate() > 0;
		}
	}

	/** Sentencia que elimina un registro de la tabla {@code telefono} por su llave primaria. */
	private static final String ELIMINAR = """
			DELETE FROM telefono
			WHERE CAST(telefono AS VARCHAR) = ?
			AND persona_tipo_documento = ?
			AND persona_numero_documento = ?
			""";

	/**
	 * Elimina el teléfono indicado por su llave primaria.
	 *
	 * @param telefono número de teléfono
	 * @param tipoDoc tipo de documento de la persona
	 * @param numeroDoc número de documento de la persona
	 * @return {@code true} si el registro existía y se eliminó
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 */
	public boolean eliminar(String telefono, String tipoDoc, String numeroDoc) throws SQLException {
		try (Connection c = ConexionBD.obtenerConexion();
				PreparedStatement ps = c.prepareStatement(ELIMINAR)) {
			ps.setString(1, telefono);
			ps.setString(2, tipoDoc);
			ps.setString(3, numeroDoc);
			return ps.executeUpdate() > 0;
		}
	}
	
}
