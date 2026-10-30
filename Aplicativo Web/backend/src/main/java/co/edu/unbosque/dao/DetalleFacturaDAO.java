package co.edu.unbosque.dao;
import co.edu.unbosque.config.ConexionBD;
import co.edu.unbosque.model.DetalleFactura;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DetalleFacturaDAO {
	
	private static final String LISTAR = """
			SELECT factura_numero_factura, numero_linea, concepto_cobro_id_concepto, valor,
			       asignacion_celda_id_asignacion, reserva_salon_id_reserva, sancion_id_sancion
			FROM detalle_factura
			ORDER BY factura_numero_factura, numero_linea
			""";
	
	public List<DetalleFactura> listar() throws SQLException {
		List<DetalleFactura> detalles = new ArrayList<>();
		try (	Connection c = ConexionBD.obtenerConexion();
				PreparedStatement ps = c.prepareStatement(LISTAR);
				ResultSet rs = ps.executeQuery()) {
			
			while (rs.next()) {
				detalles.add(mapear(rs));
			}
		}
		return detalles;
	}
	
	private DetalleFactura mapear(ResultSet rs) throws SQLException {
		return new DetalleFactura(
				rs.getInt("factura_numero_factura"),
				rs.getInt("numero_linea"),
				rs.getInt("concepto_cobro_id_concepto"),
				rs.getBigDecimal("valor"),
				rs.getObject("asignacion_celda_id_asignacion", Integer.class),
				rs.getObject("reserva_salon_id_reserva", Integer.class),
				rs.getObject("sancion_id_sancion", Integer.class)
				);
	}
}
