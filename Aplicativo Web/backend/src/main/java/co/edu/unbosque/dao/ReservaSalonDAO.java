package co.edu.unbosque.dao;
import co.edu.unbosque.config.ConexionBD;
import co.edu.unbosque.model.ReservaSalon;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ReservaSalonDAO {
	
	private static final String LISTAR = """
			SELECT id_reserva, fecha_reserva, hora_inicio, hora_final, valor_deposito, valor_por_hora, estado,
			       casa_numero, casa_manzana,
			       ROUND(EXTRACT(EPOCH FROM (hora_final - hora_inicio)) / 3600, 2) AS horas_reservadas,
			       ROUND(EXTRACT(EPOCH FROM (hora_final - hora_inicio)) / 3600 * valor_por_hora, 2) AS valor_total
			FROM reserva_salon
			ORDER BY hora_inicio
			""";
	
	public List<ReservaSalon> listar() throws SQLException {
		List<ReservaSalon> reservas = new ArrayList<>();
		try (	Connection c = ConexionBD.obtenerConexion();
				PreparedStatement ps = c.prepareStatement(LISTAR);
				ResultSet rs = ps.executeQuery()) {
			
			while (rs.next()) {
				reservas.add(mapear(rs));
			}
		}
		return reservas;
	}
	
	private ReservaSalon mapear(ResultSet rs) throws SQLException {
		return new ReservaSalon(
				rs.getInt("id_reserva"),
				rs.getString("fecha_reserva"),
				rs.getString("hora_inicio"),
				rs.getString("hora_final"),
				rs.getBigDecimal("valor_deposito"),
				rs.getBigDecimal("valor_por_hora"),
				rs.getString("estado"),
				rs.getInt("casa_numero"),
				rs.getInt("casa_manzana"),
				rs.getBigDecimal("horas_reservadas"),
				rs.getBigDecimal("valor_total")
				);
	}
}
