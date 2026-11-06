package co.edu.unbosque.dao;
import co.edu.unbosque.config.ConexionBD;
import co.edu.unbosque.model.Persona;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a datos de las personas con JDBC.
 * <p>
 * Cada método abre su propia conexión con {@link co.edu.unbosque.config.ConexionBD} y usa
 * {@link java.sql.PreparedStatement} para enviar los parámetros de forma segura.
 */
public class PersonaDAO {
	
	/** Consulta que lista todos los registros de la tabla {@code persona}. */
	private static final String LISTAR = """ 
			SELECT tipo_documento, numero_documento, correo, primer_nombre, segundo_nombre, primer_apellido, segundo_apellido
			FROM persona
			ORDER BY primer_nombre ASC, primer_apellido ASC;
			""";
	
	/**
	 * Consulta las personas registradas.
	 *
	 * @return lista de personas; vacía si no hay registros
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 */
	public List<Persona> listar () throws SQLException {
		List<Persona> personas = new ArrayList<>();
		try (	Connection c = ConexionBD.obtenerConexion();
				PreparedStatement ps = c.prepareStatement(LISTAR);
				ResultSet rs = ps.executeQuery()) {
			
			while (rs.next()) {
				personas.add(mapear(rs));
			}
		}
		return personas;
	}
	
	/**
	 * Convierte la fila actual del resultado en una persona.
	 *
	 * @param rs resultado de la consulta, ubicado en la fila que se va a leer
	 * @return persona leída
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 */
	private Persona mapear (ResultSet rs) throws SQLException {
		return new Persona(
				rs.getString("tipo_documento"),
				rs.getString("numero_documento"),
				rs.getString("correo"),
				rs.getString("primer_nombre"),
				rs.getString("segundo_nombre"),
				rs.getString("primer_apellido"),
				rs.getString("segundo_apellido")			
				);
	}
	
	/** Consulta que busca un registro de la tabla {@code persona} por su llave primaria. */
	private static final String BUSCAR = """
			SELECT tipo_documento, numero_documento, correo, primer_nombre, segundo_nombre, primer_apellido, segundo_apellido
			FROM persona
			WHERE tipo_documento = ?
			AND numero_documento = ?
			""";

	/**
	 * Busca una persona por su llave primaria.
	 *
	 * @param tipoDoc tipo de documento de la persona
	 * @param numeroDoc número de documento de la persona
	 * @return persona encontrada, o {@code null} si no existe
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 */
	public Persona buscar(String tipoDoc, String numeroDoc) throws SQLException {
		try (Connection c = ConexionBD.obtenerConexion();
				PreparedStatement ps = c.prepareStatement(BUSCAR)) {
			ps.setString(1, tipoDoc);
			ps.setString(2, numeroDoc);
			try (ResultSet rs = ps.executeQuery()) {
				return rs.next() ? mapear(rs) : null;
			}
		}
	}

	/** Sentencia que inserta un registro en la tabla {@code persona}. */
	private static final String INSERTAR = """
			INSERT INTO persona (tipo_documento, numero_documento, correo, primer_nombre, segundo_nombre, primer_apellido, segundo_apellido)
			VALUES (?, ?, ?, ?, ?, ?, ?)
			""";

	/**
	 * Inserta una persona en la base de datos.
	 *
	 * @param p persona que se va a insertara
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 */
	public void insertar(Persona p) throws SQLException {
		try (Connection c = ConexionBD.obtenerConexion();
				PreparedStatement ps = c.prepareStatement(INSERTAR)) {
			ps.setString(1, p.getTipoDocumento());
			ps.setString(2, p.getNumeroDocumento());
			ps.setString(3, p.getCorreo());
			ps.setString(4, p.getPrimerNombre());
			ps.setString(5, p.getSegundoNombre());
			ps.setString(6, p.getPrimerApellido());
			ps.setString(7, p.getSegundoApellido());
			ps.executeUpdate();
		}
	}

	/** Sentencia que actualiza un registro de la tabla {@code persona}. */
	private static final String ACTUALIZAR = """
			UPDATE persona
			SET correo = ?,
			    primer_nombre = ?,
			    segundo_nombre = ?,
			    primer_apellido = ?,
			    segundo_apellido = ?
			WHERE tipo_documento = ?
			AND numero_documento = ?
			""";

	/**
	 * Actualiza la persona indicada por su llave primaria.
	 *
	 * @param p persona con su llave primaria y los datos nuevosa
	 * @return {@code true} si el registro existía y se actualizó
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 */
	public boolean actualizar(Persona p) throws SQLException {
		try (Connection c = ConexionBD.obtenerConexion();
				PreparedStatement ps = c.prepareStatement(ACTUALIZAR)) {
			ps.setString(1, p.getCorreo());
			ps.setString(2, p.getPrimerNombre());
			ps.setString(3, p.getSegundoNombre());
			ps.setString(4, p.getPrimerApellido());
			ps.setString(5, p.getSegundoApellido());
			ps.setString(6, p.getTipoDocumento());
			ps.setString(7, p.getNumeroDocumento());
			return ps.executeUpdate() > 0;
		}
	}

	/** Sentencia que elimina un registro de la tabla {@code persona} por su llave primaria. */
	private static final String ELIMINAR = """
			DELETE FROM persona
			WHERE tipo_documento = ?
			AND numero_documento = ?
			""";

	/**
	 * Elimina la persona indicada por su llave primaria.
	 *
	 * @param tipoDoc tipo de documento de la persona
	 * @param numeroDoc número de documento de la persona
	 * @return {@code true} si el registro existía y se eliminó
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 */
	public boolean eliminar(String tipoDoc, String numeroDoc) throws SQLException {
		try (Connection c = ConexionBD.obtenerConexion();
				PreparedStatement ps = c.prepareStatement(ELIMINAR)) {
			ps.setString(1, tipoDoc);
			ps.setString(2, numeroDoc);
			return ps.executeUpdate() > 0;
		}
	}
	
}
