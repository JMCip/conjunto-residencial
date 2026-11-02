package co.edu.unbosque.dao;
import co.edu.unbosque.config.ConexionBD;
import co.edu.unbosque.model.Persona;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PersonaDAO {
	
	private static final String LISTAR = """ 
			SELECT tipo_documento, numero_documento, correo, primer_nombre, segundo_nombre, primer_apellido, segundo_apellido
			FROM persona
			ORDER BY primer_nombre ASC, primer_apellido ASC;
			""";
	
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
	
	private static final String BUSCAR = """
			SELECT tipo_documento, numero_documento, correo, primer_nombre, segundo_nombre, primer_apellido, segundo_apellido
			FROM persona
			WHERE tipo_documento = ?
			AND numero_documento = ?
			""";

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

	private static final String INSERTAR = """
			INSERT INTO persona (tipo_documento, numero_documento, correo, primer_nombre, segundo_nombre, primer_apellido, segundo_apellido)
			VALUES (?, ?, ?, ?, ?, ?, ?)
			""";

	public void insertar(Persona p) throws SQLException {
		try (Connection c = ConexionBD.obtenerConexion();
				PreparedStatement ps = c.prepareStatement(INSERTAR)) {
			ps.setString(1, p.getTipoDocumento());
			ps.setString(2, p.getNumeroDocumento());
			ps.setString(3, p.getCorreo());
			ps.setString(4, p.getPrimerNombre());
			ps.setString(5, p.getSegunoNombre());
			ps.setString(6, p.getPrimerApellido());
			ps.setString(7, p.getSegundoApellido());
			ps.executeUpdate();
		}
	}

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

	public boolean actualizar(Persona p) throws SQLException {
		try (Connection c = ConexionBD.obtenerConexion();
				PreparedStatement ps = c.prepareStatement(ACTUALIZAR)) {
			ps.setString(1, p.getCorreo());
			ps.setString(2, p.getPrimerNombre());
			ps.setString(3, p.getSegunoNombre());
			ps.setString(4, p.getPrimerApellido());
			ps.setString(5, p.getSegundoApellido());
			ps.setString(6, p.getTipoDocumento());
			ps.setString(7, p.getNumeroDocumento());
			return ps.executeUpdate() > 0;
		}
	}

	private static final String ELIMINAR = """
			DELETE FROM persona
			WHERE tipo_documento = ?
			AND numero_documento = ?
			""";

	public boolean eliminar(String tipoDoc, String numeroDoc) throws SQLException {
		try (Connection c = ConexionBD.obtenerConexion();
				PreparedStatement ps = c.prepareStatement(ELIMINAR)) {
			ps.setString(1, tipoDoc);
			ps.setString(2, numeroDoc);
			return ps.executeUpdate() > 0;
		}
	}
	
}
