package co.edu.unbosque.dao;
import co.edu.unbosque.config.ConexionBD;
import co.edu.unbosque.model.Arriendo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ArriendoDAO {
	
	private static final String LISTAR = """
			SELECT id_contrato, cuota_mensual, fecha_inicio, fecha_fin, estado_contrato,
			       arrendatario_tipo_documento, arrendatario_numero_documento, casa_numero, casa_manzana,
			       propietario_tipo_documento, propietario_numero_documento
			FROM arriendo
			ORDER BY id_contrato
			""";
	
	public List<Arriendo> listar() throws SQLException {
		List<Arriendo> arriendos = new ArrayList<>();
		try (	Connection c = ConexionBD.obtenerConexion();
				PreparedStatement ps = c.prepareStatement(LISTAR);
				ResultSet rs = ps.executeQuery()) {
			
			while (rs.next()) {
				arriendos.add(mapear(rs));
			}
		}
		return arriendos;
	}
	
	private Arriendo mapear(ResultSet rs) throws SQLException {
		return new Arriendo(
				rs.getInt("id_contrato"),
				rs.getBigDecimal("cuota_mensual"),
				rs.getString("fecha_inicio"),
				rs.getString("fecha_fin"),
				rs.getString("estado_contrato"),
				rs.getString("arrendatario_tipo_documento"),
				rs.getString("arrendatario_numero_documento"),
				rs.getInt("casa_numero"),
				rs.getInt("casa_manzana"),
				rs.getString("propietario_tipo_documento"),
				rs.getString("propietario_numero_documento")
				);
	}
}
