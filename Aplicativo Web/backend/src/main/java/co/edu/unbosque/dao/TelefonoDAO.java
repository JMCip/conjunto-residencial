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
}
