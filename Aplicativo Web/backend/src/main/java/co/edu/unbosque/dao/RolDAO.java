package co.edu.unbosque.dao;
import co.edu.unbosque.config.ConexionBD;
import co.edu.unbosque.model.Persona;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RolDAO {
	
	private static final String LISTAR_PROPIETARIOS = """
			SELECT p.tipo_documento, p.numero_documento, p.correo, p.primer_nombre, p.segundo_nombre,
			       p.primer_apellido, p.segundo_apellido
			FROM persona p
			JOIN propietario r ON r.tipo_documento = p.tipo_documento
			            AND r.numero_documento = p.numero_documento
			ORDER BY p.primer_nombre, p.primer_apellido
			""";
	
	private static final String LISTAR_ARRENDATARIOS = """
			SELECT p.tipo_documento, p.numero_documento, p.correo, p.primer_nombre, p.segundo_nombre,
			       p.primer_apellido, p.segundo_apellido
			FROM persona p
			JOIN arrendatario r ON r.tipo_documento = p.tipo_documento
			            AND r.numero_documento = p.numero_documento
			ORDER BY p.primer_nombre, p.primer_apellido
			""";
	
	private static final String LISTAR_RESIDENTES = """
			SELECT p.tipo_documento, p.numero_documento, p.correo, p.primer_nombre, p.segundo_nombre,
			       p.primer_apellido, p.segundo_apellido
			FROM persona p
			JOIN residente r ON r.tipo_documento = p.tipo_documento
			            AND r.numero_documento = p.numero_documento
			ORDER BY p.primer_nombre, p.primer_apellido
			""";
	
	private static final String LISTAR_VISITANTES = """
			SELECT p.tipo_documento, p.numero_documento, p.correo, p.primer_nombre, p.segundo_nombre,
			       p.primer_apellido, p.segundo_apellido
			FROM persona p
			JOIN visitante r ON r.tipo_documento = p.tipo_documento
			            AND r.numero_documento = p.numero_documento
			ORDER BY p.primer_nombre, p.primer_apellido
			""";
	
	public List<Persona> listarPropietarios() throws SQLException {
		return listarPorRol(LISTAR_PROPIETARIOS);
	}
	
	public List<Persona> listarArrendatarios() throws SQLException {
		return listarPorRol(LISTAR_ARRENDATARIOS);
	}
	
	public List<Persona> listarResidentes() throws SQLException {
		return listarPorRol(LISTAR_RESIDENTES);
	}
	
	public List<Persona> listarVisitantes() throws SQLException {
		return listarPorRol(LISTAR_VISITANTES);
	}
	
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
}
