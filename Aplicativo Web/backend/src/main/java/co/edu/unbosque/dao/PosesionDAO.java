package co.edu.unbosque.dao;
import co.edu.unbosque.config.ConexionBD;
import co.edu.unbosque.model.Posesion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PosesionDAO {
	
	private static final String LISTAR = """
			SELECT id_posesion, porcentaje, fecha_compra, fecha_venta, casa_numero, casa_manzana,
			       propietario_tipo_documento, propietario_numero_documento
			FROM posesion
			ORDER BY casa_manzana, casa_numero, fecha_compra
			""";
	
	public List<Posesion> listar() throws SQLException {
		List<Posesion> posesiones = new ArrayList<>();
		try (	Connection c = ConexionBD.obtenerConexion();
				PreparedStatement ps = c.prepareStatement(LISTAR);
				ResultSet rs = ps.executeQuery()) {
			
			while (rs.next()) {
				posesiones.add(mapear(rs));
			}
		}
		return posesiones;
	}
	
	private Posesion mapear(ResultSet rs) throws SQLException {
		return new Posesion(
				rs.getInt("id_posesion"),
				rs.getBigDecimal("porcentaje"),
				rs.getString("fecha_compra"),
				rs.getString("fecha_venta"),
				rs.getInt("casa_numero"),
				rs.getInt("casa_manzana"),
				rs.getString("propietario_tipo_documento"),
				rs.getString("propietario_numero_documento")
				);
	}
}
