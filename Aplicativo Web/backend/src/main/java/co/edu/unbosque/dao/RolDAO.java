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
 * Acceso a datos de los roles de las personas (propietario, arrendatario, residente y visitante).
 * <p>
 * Cada rol es una tabla con el documento de la persona. Los nombres de tabla se toman de un mapa
 * cerrado, nunca del texto que envía el cliente.
 */
public class RolDAO {
	
	/** Consulta que lista las personas con rol de propietario. */
	private static final String LISTAR_PROPIETARIOS = """
			SELECT p.tipo_documento, p.numero_documento, p.correo, p.primer_nombre, p.segundo_nombre,
			       p.primer_apellido, p.segundo_apellido
			FROM persona p
			JOIN propietario r ON r.tipo_documento = p.tipo_documento
			            AND r.numero_documento = p.numero_documento
			ORDER BY p.primer_nombre, p.primer_apellido
			""";
	
	/** Consulta que lista las personas con rol de arrendatario. */
	private static final String LISTAR_ARRENDATARIOS = """
			SELECT p.tipo_documento, p.numero_documento, p.correo, p.primer_nombre, p.segundo_nombre,
			       p.primer_apellido, p.segundo_apellido
			FROM persona p
			JOIN arrendatario r ON r.tipo_documento = p.tipo_documento
			            AND r.numero_documento = p.numero_documento
			ORDER BY p.primer_nombre, p.primer_apellido
			""";
	
	/** Consulta que lista las personas con rol de residente. */
	private static final String LISTAR_RESIDENTES = """
			SELECT p.tipo_documento, p.numero_documento, p.correo, p.primer_nombre, p.segundo_nombre,
			       p.primer_apellido, p.segundo_apellido
			FROM persona p
			JOIN residente r ON r.tipo_documento = p.tipo_documento
			            AND r.numero_documento = p.numero_documento
			ORDER BY p.primer_nombre, p.primer_apellido
			""";
	
	/** Consulta que lista las personas con rol de visitante. */
	private static final String LISTAR_VISITANTES = """
			SELECT p.tipo_documento, p.numero_documento, p.correo, p.primer_nombre, p.segundo_nombre,
			       p.primer_apellido, p.segundo_apellido
			FROM persona p
			JOIN visitante r ON r.tipo_documento = p.tipo_documento
			            AND r.numero_documento = p.numero_documento
			ORDER BY p.primer_nombre, p.primer_apellido
			""";
	
	/**
	 * Lista las personas con rol de propietario, ordenadas por nombre.
	 *
	 * @return propietarios registrados
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 */
	public List<Persona> listarPropietarios() throws SQLException {
		return listarPorRol(LISTAR_PROPIETARIOS);
	}
	
	/**
	 * Lista las personas con rol de arrendatario, ordenadas por nombre.
	 *
	 * @return arrendatarios registrados
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 */
	public List<Persona> listarArrendatarios() throws SQLException {
		return listarPorRol(LISTAR_ARRENDATARIOS);
	}
	
	/**
	 * Lista las personas con rol de residente, ordenadas por nombre.
	 *
	 * @return residentes registrados
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 */
	public List<Persona> listarResidentes() throws SQLException {
		return listarPorRol(LISTAR_RESIDENTES);
	}
	
	/**
	 * Lista las personas con rol de visitante, ordenadas por nombre.
	 *
	 * @return visitantes registrados
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 */
	public List<Persona> listarVisitantes() throws SQLException {
		return listarPorRol(LISTAR_VISITANTES);
	}
	
	/**
	 * Ejecuta una de las consultas de listado por rol.
	 *
	 * @param sql consulta del rol
	 * @return personas con ese rol
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 */
	private List<Persona> listarPorRol(String sql) throws SQLException {
		List<Persona> personas = new ArrayList<>();
		try (	Connection c = ConexionBD.obtenerConexion();
				PreparedStatement ps = c.prepareStatement(sql);
				ResultSet rs = ps.executeQuery()) {
			
			while (rs.next()) {
				personas.add(mapear(rs));
			}
		}
		return personas;
	}
	
	/**
	 * Convierte la fila actual en una {@link Persona}.
	 *
	 * @param rs resultado de la consulta, ubicado en la fila que se va a leer
	 * @return persona leída
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 */
	private Persona mapear(ResultSet rs) throws SQLException {
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

	/** Relación entre cada rol válido y la tabla que lo guarda. */
	private static final java.util.Map<String, String> TABLAS = java.util.Map.of(
			"propietario", "propietario",
			"arrendatario", "arrendatario",
			"residente", "residente",
			"visitante", "visitante");

	/**
	 * Indica si la persona tiene el rol.
	 *
	 * @param rol nombre del rol: {@code propietario}, {@code arrendatario}, {@code residente} o
	 *         {@code visitante}
	 * @param tipoDoc tipo de documento de la persona
	 * @param numeroDoc número de documento de la persona
	 * @return {@code true} si la persona tiene el rol
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 */
	public boolean tieneRol(String rol, String tipoDoc, String numeroDoc) throws SQLException {
		String sql = "SELECT COUNT(*) FROM " + TABLAS.get(rol)
				+ " WHERE tipo_documento = ? AND numero_documento = ?";
		try (Connection c = ConexionBD.obtenerConexion();
				PreparedStatement ps = c.prepareStatement(sql)) {
			ps.setString(1, tipoDoc);
			ps.setString(2, numeroDoc);
			try (ResultSet rs = ps.executeQuery()) {
				rs.next();
				return rs.getInt(1) > 0;
			}
		}
	}

	/**
	 * Asigna el rol a la persona.
	 *
	 * @param rol nombre del rol: {@code propietario}, {@code arrendatario}, {@code residente} o
	 *         {@code visitante}
	 * @param tipoDoc tipo de documento de la persona
	 * @param numeroDoc número de documento de la persona
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 */
	public void asignar(String rol, String tipoDoc, String numeroDoc) throws SQLException {
		String sql = "INSERT INTO " + TABLAS.get(rol) + " (tipo_documento, numero_documento) VALUES (?, ?)";
		try (Connection c = ConexionBD.obtenerConexion();
				PreparedStatement ps = c.prepareStatement(sql)) {
			ps.setString(1, tipoDoc);
			ps.setString(2, numeroDoc);
			ps.executeUpdate();
		}
	}

	/**
	 * Quita el rol a la persona.
	 *
	 * @param rol nombre del rol: {@code propietario}, {@code arrendatario}, {@code residente} o
	 *         {@code visitante}
	 * @param tipoDoc tipo de documento de la persona
	 * @param numeroDoc número de documento de la persona
	 * @return {@code true} si la persona tenía el rol y se quitó
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 */
	public boolean quitar(String rol, String tipoDoc, String numeroDoc) throws SQLException {
		String sql = "DELETE FROM " + TABLAS.get(rol) + " WHERE tipo_documento = ? AND numero_documento = ?";
		try (Connection c = ConexionBD.obtenerConexion();
				PreparedStatement ps = c.prepareStatement(sql)) {
			ps.setString(1, tipoDoc);
			ps.setString(2, numeroDoc);
			return ps.executeUpdate() > 0;
		}
	}
	
}
