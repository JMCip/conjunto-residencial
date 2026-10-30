package co.edu.unbosque.dao;
import co.edu.unbosque.config.ConexionBD;
import co.edu.unbosque.model.CeldaParqueadero;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CeldaParqueaderoDAO {
	
	private static final String LISTAR = """
			SELECT id_celda, tipo, estado
			FROM celda_parqueadero
			ORDER BY id_celda
			""";
	
	public List<CeldaParqueadero> listar() throws SQLException {
		List<CeldaParqueadero> celdas = new ArrayList<>();
		try (	Connection c = ConexionBD.obtenerConexion();
				PreparedStatement ps = c.prepareStatement(LISTAR);
				ResultSet rs = ps.executeQuery()) {
			
			while (rs.next()) {
				celdas.add(mapear(rs));
			}
		}
		return celdas;
	}
	
	private CeldaParqueadero mapear(ResultSet rs) throws SQLException {
		return new CeldaParqueadero(
				rs.getInt("id_celda"),
				rs.getString("tipo"),
				rs.getString("estado")
				);
	}
}
