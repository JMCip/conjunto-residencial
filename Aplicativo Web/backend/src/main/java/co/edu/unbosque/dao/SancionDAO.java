package co.edu.unbosque.dao;
import co.edu.unbosque.config.ConexionBD;
import co.edu.unbosque.model.Sancion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SancionDAO {
	
	private static final String LISTAR = """
			SELECT id_sancion, fecha_incidente, fecha_notificacion, tipo, descripcion, descargos, estado_proceso,
			       valor, casa_numero, casa_manzana
			FROM sancion
			ORDER BY fecha_incidente DESC
			""";
	
	public List<Sancion> listar() throws SQLException {
		List<Sancion> sanciones = new ArrayList<>();
		try (	Connection c = ConexionBD.obtenerConexion();
				PreparedStatement ps = c.prepareStatement(LISTAR);
				ResultSet rs = ps.executeQuery()) {
			
			while (rs.next()) {
				sanciones.add(mapear(rs));
			}
		}
		return sanciones;
	}
	
	private Sancion mapear(ResultSet rs) throws SQLException {
		return new Sancion(
				rs.getInt("id_sancion"),
				rs.getString("fecha_incidente"),
				rs.getString("fecha_notificacion"),
				rs.getString("tipo"),
				rs.getString("descripcion"),
				rs.getString("descargos"),
				rs.getString("estado_proceso"),
				rs.getBigDecimal("valor"),
				rs.getInt("casa_numero"),
				rs.getInt("casa_manzana")
				);
	}
}
