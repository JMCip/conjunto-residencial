package co.edu.unbosque.dao;

import co.edu.unbosque.config.ConexionBD;
import co.edu.unbosque.model.ReservaSalon;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
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

	private static final String BUSCAR_POR_ID = """
			SELECT id_reserva, fecha_reserva, hora_inicio, hora_final, valor_deposito, valor_por_hora, estado,
			       casa_numero, casa_manzana,
			       ROUND(EXTRACT(EPOCH FROM (hora_final - hora_inicio)) / 3600, 2) AS horas_reservadas,
			       ROUND(EXTRACT(EPOCH FROM (hora_final - hora_inicio)) / 3600 * valor_por_hora, 2) AS valor_total
			FROM reserva_salon
			WHERE id_reserva = ?
			""";

	private static final String INSERTAR = """
			INSERT INTO reserva_salon (fecha_reserva, hora_inicio, hora_final, valor_deposito,
			                           valor_por_hora, estado, casa_numero, casa_manzana)
			VALUES (?, ?, ?, ?, ?, ?, ?, ?)
			RETURNING id_reserva
			""";

	private static final String ACTUALIZAR_ESTADO =
			"UPDATE reserva_salon SET estado = ? WHERE id_reserva = ?";

	private static final String ELIMINAR = "DELETE FROM reserva_salon WHERE id_reserva = ?";

	private static final String SOLAPAMIENTO = """
			SELECT EXISTS (SELECT 1 FROM reserva_salon
			               WHERE estado <> 'CANCELADA'
			                 AND hora_inicio < ? AND hora_final > ?
			                 AND id_reserva <> ?)
			""";

	private static final String EXISTE_CASA =
			"SELECT EXISTS (SELECT 1 FROM casa WHERE numero = ? AND manzana = ?)";

	private static final String CASA_EN_MORA = """
			SELECT EXISTS (SELECT 1 FROM factura
			               WHERE casa_numero = ? AND casa_manzana = ? AND estado = 'VENCIDA')
			""";

	private static final String CASA_CON_SANCION = """
			SELECT EXISTS (SELECT 1 FROM sancion
			               WHERE casa_numero = ? AND casa_manzana = ? AND estado_proceso = 'EN_DESCARGOS')
			""";

	public List<ReservaSalon> listar() throws SQLException {
		List<ReservaSalon> reservas = new ArrayList<>();
		try (Connection c = ConexionBD.obtenerConexion();
		     PreparedStatement ps = c.prepareStatement(LISTAR);
		     ResultSet rs = ps.executeQuery()) {

			while (rs.next()) {
				reservas.add(mapear(rs));
			}
		}
		return reservas;
	}

	/** Devuelve la reserva con ese id, o null si no existe. */
	public ReservaSalon buscarPorId(int id) throws SQLException {
		try (Connection c = ConexionBD.obtenerConexion();
		     PreparedStatement ps = c.prepareStatement(BUSCAR_POR_ID)) {
			ps.setInt(1, id);
			try (ResultSet rs = ps.executeQuery()) {
				return rs.next() ? mapear(rs) : null;
			}
		}
	}

	public int insertar(ReservaSalon r, LocalDateTime inicio, LocalDateTime fin) throws SQLException {
		try (Connection c = ConexionBD.obtenerConexion();
		     PreparedStatement ps = c.prepareStatement(INSERTAR)) {
			ps.setObject(1, inicio.toLocalDate());
			ps.setObject(2, inicio);
			ps.setObject(3, fin);
			ps.setBigDecimal(4, r.getValorDeposito());
			ps.setBigDecimal(5, r.getValorPorHora());
			ps.setString(6, r.getEstado());
			ps.setInt(7, r.getCasaNumero());
			ps.setInt(8, r.getCasaManzana());
			try (ResultSet rs = ps.executeQuery()) {
				rs.next();
				return rs.getInt(1);
			}
		}
	}

	public boolean actualizarEstado(int id, String estado) throws SQLException {
		try (Connection c = ConexionBD.obtenerConexion();
		     PreparedStatement ps = c.prepareStatement(ACTUALIZAR_ESTADO)) {
			ps.setString(1, estado);
			ps.setInt(2, id);
			return ps.executeUpdate() > 0;
		}
	}

	public boolean eliminar(int id) throws SQLException {
		try (Connection c = ConexionBD.obtenerConexion();
		     PreparedStatement ps = c.prepareStatement(ELIMINAR)) {
			ps.setInt(1, id);
			return ps.executeUpdate() > 0;
		}
	}

	public boolean haySolapamiento(LocalDateTime inicio, LocalDateTime fin, int excluirId) throws SQLException {
		try (Connection c = ConexionBD.obtenerConexion();
		     PreparedStatement ps = c.prepareStatement(SOLAPAMIENTO)) {
			ps.setObject(1, fin);
			ps.setObject(2, inicio);
			ps.setInt(3, excluirId);
			try (ResultSet rs = ps.executeQuery()) {
				rs.next();
				return rs.getBoolean(1);
			}
		}
	}

	public boolean existeCasa(int numero, int manzana) throws SQLException {
		return consultarCasa(EXISTE_CASA, numero, manzana);
	}

	public boolean casaEnMora(int numero, int manzana) throws SQLException {
		return consultarCasa(CASA_EN_MORA, numero, manzana);
	}

	public boolean casaConSancionPendiente(int numero, int manzana) throws SQLException {
		return consultarCasa(CASA_CON_SANCION, numero, manzana);
	}

	private boolean consultarCasa(String sql, int numero, int manzana) throws SQLException {
		try (Connection c = ConexionBD.obtenerConexion();
		     PreparedStatement ps = c.prepareStatement(sql)) {
			ps.setInt(1, numero);
			ps.setInt(2, manzana);
			try (ResultSet rs = ps.executeQuery()) {
				rs.next();
				return rs.getBoolean(1);
			}
		}
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