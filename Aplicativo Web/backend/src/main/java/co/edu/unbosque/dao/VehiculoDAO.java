package co.edu.unbosque.dao;
import co.edu.unbosque.config.ConexionBD;
import co.edu.unbosque.model.Vehiculo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class VehiculoDAO {
	
	private static final String LISTAR = """
			SELECT placa, marca, modelo, tipo, persona_tipo_documento, persona_numero_documento
			FROM vehiculo
			ORDER BY placa
			""";
	
	public List<Vehiculo> listar() throws SQLException {
		List<Vehiculo> vehiculos = new ArrayList<>();
		try (	Connection c = ConexionBD.obtenerConexion();
				PreparedStatement ps = c.prepareStatement(LISTAR);
				ResultSet rs = ps.executeQuery()) {
			
			while (rs.next()) {
				vehiculos.add(mapear(rs));
			}
		}
		return vehiculos;
	}
	
	private Vehiculo mapear(ResultSet rs) throws SQLException {
		return new Vehiculo(
				rs.getString("placa"),
				rs.getString("marca"),
				rs.getObject("modelo", Integer.class),
				rs.getString("tipo"),
				rs.getString("persona_tipo_documento"),
				rs.getString("persona_numero_documento")
				);
	}
}
