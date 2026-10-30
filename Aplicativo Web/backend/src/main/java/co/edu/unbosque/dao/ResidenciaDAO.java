package co.edu.unbosque.dao;
import co.edu.unbosque.config.ConexionBD;
import co.edu.unbosque.model.Residencia;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ResidenciaDAO {
	
	private static final String LISTAR = """
			SELECT id_residencia, fecha_ingreso, fecha_salida, residente_tipo_documento, residente_numero_documento,
			       casa_numero, casa_manzana
			FROM residencia
			ORDER BY casa_manzana, casa_numero, fecha_ingreso
			""";
	
	public List<Residencia> listar() throws SQLException {
		List<Residencia> residencias = new ArrayList<>();
		try (	Connection c = ConexionBD.obtenerConexion();
				PreparedStatement ps = c.prepareStatement(LISTAR);
				ResultSet rs = ps.executeQuery()) {
			
			while (rs.next()) {
				residencias.add(mapear(rs));
			}
		}
		return residencias;
	}
	
	private Residencia mapear(ResultSet rs) throws SQLException {
		return new Residencia(
				rs.getInt("id_residencia"),
				rs.getString("fecha_ingreso"),
				rs.getString("fecha_salida"),
				rs.getString("residente_tipo_documento"),
				rs.getString("residente_numero_documento"),
				rs.getInt("casa_numero"),
				rs.getInt("casa_manzana")
				);
	}
}
