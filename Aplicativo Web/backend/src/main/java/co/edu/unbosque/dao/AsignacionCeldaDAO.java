package co.edu.unbosque.dao;
import co.edu.unbosque.config.ConexionBD;
import co.edu.unbosque.model.AsignacionCelda;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AsignacionCeldaDAO {
	
	private static final String LISTAR = """
			SELECT id_asignacion, fecha_inicio, fecha_fin, casa_numero, casa_manzana, celda_parqueadero_id_celda
			FROM asignacion_celda
			ORDER BY id_asignacion
			""";
	
	public List<AsignacionCelda> listar() throws SQLException {
		List<AsignacionCelda> asignaciones = new ArrayList<>();
		try (	Connection c = ConexionBD.obtenerConexion();
				PreparedStatement ps = c.prepareStatement(LISTAR);
				ResultSet rs = ps.executeQuery()) {
			
			while (rs.next()) {
				asignaciones.add(mapear(rs));
			}
		}
		return asignaciones;
	}
	
	private AsignacionCelda mapear(ResultSet rs) throws SQLException {
		return new AsignacionCelda(
				rs.getInt("id_asignacion"),
				rs.getString("fecha_inicio"),
				rs.getString("fecha_fin"),
				rs.getInt("casa_numero"),
				rs.getInt("casa_manzana"),
				rs.getInt("celda_parqueadero_id_celda")
				);
	}
}
