package co.edu.unbosque.dao;
import co.edu.unbosque.config.ConexionBD;
import co.edu.unbosque.model.Pago;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PagoDAO {
	
	private static final String LISTAR = """
			SELECT id_pago, fecha, valor, metodo_pago, factura_numero_factura, persona_tipo_documento, persona_numero_documento
			FROM pago
			ORDER BY fecha, id_pago
			""";
	
	public List<Pago> listar() throws SQLException {
		List<Pago> pagos = new ArrayList<>();
		try (	Connection c = ConexionBD.obtenerConexion();
				PreparedStatement ps = c.prepareStatement(LISTAR);
				ResultSet rs = ps.executeQuery()) {
			
			while (rs.next()) {
				pagos.add(mapear(rs));
			}
		}
		return pagos;
	}
	
	private Pago mapear(ResultSet rs) throws SQLException {
		return new Pago(
				rs.getInt("id_pago"),
				rs.getString("fecha"),
				rs.getBigDecimal("valor"),
				rs.getString("metodo_pago"),
				rs.getInt("factura_numero_factura"),
				rs.getString("persona_tipo_documento"),
				rs.getString("persona_numero_documento")
				);
	}
}
