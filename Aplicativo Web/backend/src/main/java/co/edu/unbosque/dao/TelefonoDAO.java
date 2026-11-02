package co.edu.unbosque.dao;
import co.edu.unbosque.config.ConexionBD;
import co.edu.unbosque.model.Telefono;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TelefonoDAO {
	
	private static final String LISTAR = """
			SELECT telefono, persona_tipo_documento, persona_numero_documento
			FROM telefono
			ORDER BY persona_tipo_documento, persona_numero_documento, telefono
			""";
	
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
	
	private Telefono mapear(ResultSet rs) throws SQLException {
		return new Telefono(
				rs.getString("telefono"),
				rs.getString("persona_tipo_documento"),
				rs.getString("persona_numero_documento")
				);
	}
	
	private static final String BUSCAR = """
			SELECT telefono, persona_tipo_documento, persona_numero_documento
			FROM telefono
			WHERE CAST(telefono AS VARCHAR) = ?
			AND persona_tipo_documento = ?
			AND persona_numero_documento = ?
			""";

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

	private static final String INSERTAR = """
			INSERT INTO telefono (telefono, persona_tipo_documento, persona_numero_documento)
			VALUES (CAST(? AS NUMERIC), ?, ?)
			""";

	public void insertar(Telefono t) throws SQLException {
		try (Connection c = ConexionBD.obtenerConexion();
				PreparedStatement ps = c.prepareStatement(INSERTAR)) {
			ps.setString(1, t.getTelefono());
			ps.setString(2, t.getPersonaTipoDocumento());
			ps.setString(3, t.getPersonaNumeroDocumento());
			ps.executeUpdate();
		}
	}

	private static final String ACTUALIZAR = """
			UPDATE telefono
			SET telefono = CAST(? AS NUMERIC)
			WHERE CAST(telefono AS VARCHAR) = ?
			AND persona_tipo_documento = ?
			AND persona_numero_documento = ?
			""";

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

	private static final String ELIMINAR = """
			DELETE FROM telefono
			WHERE CAST(telefono AS VARCHAR) = ?
			AND persona_tipo_documento = ?
			AND persona_numero_documento = ?
			""";

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
