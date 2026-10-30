package co.edu.unbosque.dao;
import co.edu.unbosque.config.ConexionBD;
import co.edu.unbosque.model.Parqueo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ParqueoDAO {
	
	private static final String LISTAR = """
			SELECT id_parqueo, fecha_hora_entrada, fecha_hora_salida, vehiculo_placa, celda_parqueadero_id_celda
			FROM parqueo
			ORDER BY fecha_hora_entrada DESC
			""";
	
	public List<Parqueo> listar() throws SQLException {
		List<Parqueo> parqueos = new ArrayList<>();
		try (	Connection c = ConexionBD.obtenerConexion();
				PreparedStatement ps = c.prepareStatement(LISTAR);
				ResultSet rs = ps.executeQuery()) {
			
			while (rs.next()) {
				parqueos.add(mapear(rs));
			}
		}
		return parqueos;
	}
	
	private Parqueo mapear(ResultSet rs) throws SQLException {
		return new Parqueo(
				rs.getInt("id_parqueo"),
				rs.getString("fecha_hora_entrada"),
				rs.getString("fecha_hora_salida"),
				rs.getString("vehiculo_placa"),
				rs.getInt("celda_parqueadero_id_celda")
				);
	}
}
