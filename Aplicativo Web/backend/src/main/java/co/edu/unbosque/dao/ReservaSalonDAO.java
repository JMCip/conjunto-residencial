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

/**
 * Acceso a datos de las reservas del salón comunal con JDBC.
 * <p>
 * Cada método abre su propia conexión con {@link co.edu.unbosque.config.ConexionBD} y usa
 * {@link java.sql.PreparedStatement} para enviar los parámetros de forma segura.
 */
public class ReservaSalonDAO {

	/** Consulta que lista las reservas con las horas reservadas y el valor total calculados. */
	private static final String LISTAR = """
			SELECT id_reserva, fecha_reserva, hora_inicio, hora_final, valor_deposito, valor_por_hora, estado,
			       casa_numero, casa_manzana,
			       ROUND(EXTRACT(EPOCH FROM (hora_final - hora_inicio)) / 3600, 2) AS horas_reservadas,
			       ROUND(EXTRACT(EPOCH FROM (hora_final - hora_inicio)) / 3600 * valor_por_hora, 2) AS valor_total
			FROM reserva_salon
			ORDER BY hora_inicio
			""";

	/**
	 * Consulta que busca una reserva por su identificador, con las horas reservadas y el valor total
	 * calculados.
	 */
	private static final String BUSCAR_POR_ID = """
			SELECT id_reserva, fecha_reserva, hora_inicio, hora_final, valor_deposito, valor_por_hora, estado,
			       casa_numero, casa_manzana,
			       ROUND(EXTRACT(EPOCH FROM (hora_final - hora_inicio)) / 3600, 2) AS horas_reservadas,
			       ROUND(EXTRACT(EPOCH FROM (hora_final - hora_inicio)) / 3600 * valor_por_hora, 2) AS valor_total
			FROM reserva_salon
			WHERE id_reserva = ?
			""";

	/** Sentencia que inserta un registro en la tabla {@code reserva_salon}. */
	private static final String INSERTAR = """
			INSERT INTO reserva_salon (fecha_reserva, hora_inicio, hora_final, valor_deposito,
			                           valor_por_hora, estado, casa_numero, casa_manzana)
			VALUES (?, ?, ?, ?, ?, ?, ?, ?)
			RETURNING id_reserva
			""";

	/** Sentencia que cambia el estado de una reserva. */
	private static final String ACTUALIZAR_ESTADO =
			"UPDATE reserva_salon SET estado = ? WHERE id_reserva = ?";

	/** Sentencia que elimina un registro de la tabla {@code reserva_salon} por su llave primaria. */
	private static final String ELIMINAR = "DELETE FROM reserva_salon WHERE id_reserva = ?";

	/** Consulta que indica si alguna reserva no cancelada se cruza con la franja indicada. */
	private static final String SOLAPAMIENTO = """
			SELECT EXISTS (SELECT 1 FROM reserva_salon
			               WHERE estado <> 'CANCELADA'
			                 AND hora_inicio < ? AND hora_final > ?
			                 AND id_reserva <> ?)
			""";

	/** Consulta que indica si existe la casa con ese número y manzana. */
	private static final String EXISTE_CASA =
			"SELECT EXISTS (SELECT 1 FROM casa WHERE numero = ? AND manzana = ?)";

	/** Consulta que indica si la casa tiene facturas {@code VENCIDA}. */
	private static final String CASA_EN_MORA = """
			SELECT EXISTS (SELECT 1 FROM factura
			               WHERE casa_numero = ? AND casa_manzana = ? AND estado = 'VENCIDA')
			""";

	/** Consulta que indica si la casa tiene sanciones {@code EN_DESCARGOS}. */
	private static final String CASA_CON_SANCION = """
			SELECT EXISTS (SELECT 1 FROM sancion
			               WHERE casa_numero = ? AND casa_manzana = ? AND estado_proceso = 'EN_DESCARGOS')
			""";

	/**
	 * Consulta las reservas del salón comunal registradas.
	 *
	 * @return lista de reservas del salón comunal; vacía si no hay registros
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 */
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

	/**
	 * Busca una reserva del salón comunal por su llave primaria.
	 *
	 * @param id identificador de la reserva del salón comunal
	 * @return reserva del salón comunal encontrada, o {@code null} si no existe
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 */
	public ReservaSalon buscarPorId(int id) throws SQLException {
		try (Connection c = ConexionBD.obtenerConexion();
		     PreparedStatement ps = c.prepareStatement(BUSCAR_POR_ID)) {
			ps.setInt(1, id);
			try (ResultSet rs = ps.executeQuery()) {
				return rs.next() ? mapear(rs) : null;
			}
		}
	}

	/**
	 * Inserta una reserva del salón comunal.
	 *
	 * @param r reserva con los valores, el estado y la casa
	 * @param inicio fecha y hora de inicio
	 * @param fin fecha y hora de fin
	 * @return identificador de la reserva generada
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 */
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

	/**
	 * Cambia el estado de una reserva.
	 *
	 * @param id identificador de la reserva
	 * @param estado nuevo estado
	 * @return {@code true} si la reserva existía y se actualizó
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 */
	public boolean actualizarEstado(int id, String estado) throws SQLException {
		try (Connection c = ConexionBD.obtenerConexion();
		     PreparedStatement ps = c.prepareStatement(ACTUALIZAR_ESTADO)) {
			ps.setString(1, estado);
			ps.setInt(2, id);
			return ps.executeUpdate() > 0;
		}
	}

	/**
	 * Elimina la reserva del salón comunal indicada por su llave primaria.
	 *
	 * @param id identificador de la reserva del salón comunal
	 * @return {@code true} si el registro existía y se eliminó
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 */
	public boolean eliminar(int id) throws SQLException {
		try (Connection c = ConexionBD.obtenerConexion();
		     PreparedStatement ps = c.prepareStatement(ELIMINAR)) {
			ps.setInt(1, id);
			return ps.executeUpdate() > 0;
		}
	}

	/**
	 * Indica si alguna reserva no cancelada, distinta de la indicada, se cruza con la franja.
	 *
	 * @param inicio inicio de la franja
	 * @param fin fin de la franja
	 * @param excluirId identificador del registro que no se tiene en cuenta (el que se está
	 *         actualizando); {@code 0} al crear
	 * @return {@code true} si hay un cruce de horario
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 */
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

	/**
	 * Indica si existe la casa.
	 *
	 * @param numero número de la casa
	 * @param manzana manzana de la casa
	 * @return {@code true} si la casa existe
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 */
	public boolean existeCasa(int numero, int manzana) throws SQLException {
		return consultarCasa(EXISTE_CASA, numero, manzana);
	}

	/**
	 * Indica si la casa tiene facturas vencidas.
	 *
	 * @param numero número de la casa
	 * @param manzana manzana de la casa
	 * @return {@code true} si la casa está en mora
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 */
	public boolean casaEnMora(int numero, int manzana) throws SQLException {
		return consultarCasa(CASA_EN_MORA, numero, manzana);
	}

	/**
	 * Indica si la casa tiene sanciones en proceso de descargos.
	 *
	 * @param numero número de la casa
	 * @param manzana manzana de la casa
	 * @return {@code true} si tiene sanciones pendientes
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 */
	public boolean casaConSancionPendiente(int numero, int manzana) throws SQLException {
		return consultarCasa(CASA_CON_SANCION, numero, manzana);
	}

	/**
	 * Ejecuta una consulta {@code SELECT EXISTS} sobre una casa.
	 *
	 * @param sql consulta que se ejecuta
	 * @param numero número de la casa
	 * @param manzana manzana de la casa
	 * @return resultado de la consulta
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 */
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

	/**
	 * Convierte la fila actual del resultado en una reserva del salón comunal.
	 *
	 * @param rs resultado de la consulta, ubicado en la fila que se va a leer
	 * @return reserva del salón comunal leída
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 */
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