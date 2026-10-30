package co.edu.unbosque.dao;
import co.edu.unbosque.config.ConexionBD;
import co.edu.unbosque.model.Visita;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class VisitaDAO {
	
	private static final String LISTAR = """
			SELECT id_visita, fecha_entrada, fecha_salida, visitante_tipo_documento, visitante_numero_documento,
			       casa_numero, casa_manzana
			FROM visita
			ORDER BY fecha_entrada DESC, id_visita DESC
			""";
	
	public List<Visita> listar() throws SQLException {
		List<Visita> visitas = new ArrayList<>();
		try (	Connection c = ConexionBD.obtenerConexion();
				PreparedStatement ps = c.prepareStatement(LISTAR);
				ResultSet rs = ps.executeQuery()) {
			
			while (rs.next()) {
				visitas.add(mapear(rs));
			}
		}
		return visitas;
	}
	
	private Visita mapear(ResultSet rs) throws SQLException {
		return new Visita(
				rs.getInt("id_visita"),
				rs.getString("fecha_entrada"),
				rs.getString("fecha_salida"),
				rs.getString("visitante_tipo_documento"),
				rs.getString("visitante_numero_documento"),
				rs.getInt("casa_numero"),
				rs.getInt("casa_manzana")
				);
	}
}
