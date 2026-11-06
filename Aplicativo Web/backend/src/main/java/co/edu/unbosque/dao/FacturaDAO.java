package co.edu.unbosque.dao;

import co.edu.unbosque.config.ConexionBD;
import co.edu.unbosque.model.Factura;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a datos de las facturas con JDBC.
 * <p>
 * Cada método abre su propia conexión con {@link co.edu.unbosque.config.ConexionBD} y usa
 * {@link java.sql.PreparedStatement} para enviar los parámetros de forma segura.
 */
public class FacturaDAO {

	/**
	 * Consulta que lista las facturas con su total, lo pagado y el saldo, calculados a partir de las
	 * líneas y los pagos.
	 */
	private static final String LISTAR = """
			SELECT f.numero_factura, 
					f.periodo, 
					f.fecha_emision, 
					f.fecha_vencimiento, 
					f.estado,
			       COALESCE(t.total, 0) AS total,
			       f.casa_numero, f.casa_manzana,
			       COALESCE(p.pagado, 0) AS total_pagado,
			       COALESCE(t.total, 0) - COALESCE(p.pagado, 0) AS saldo
			FROM factura f
			LEFT JOIN (SELECT factura_numero_factura, SUM(valor) AS total
			           FROM detalle_factura
			           GROUP BY factura_numero_factura) t
			       ON t.factura_numero_factura = f.numero_factura
			LEFT JOIN (SELECT factura_numero_factura, SUM(valor) AS pagado
			           FROM pago
			           GROUP BY factura_numero_factura) p
			       ON p.factura_numero_factura = f.numero_factura
			ORDER BY f.periodo, f.casa_manzana, f.casa_numero
			""";

	/** Consulta que busca una factura por su número, con su total, lo pagado y el saldo. */
	private static final String BUSCAR_POR_ID = """
			SELECT f.numero_factura, f.periodo, f.fecha_emision, f.fecha_vencimiento, f.estado,
			       COALESCE(t.total, 0) AS total,
			       f.casa_numero, f.casa_manzana,
			       COALESCE(p.pagado, 0) AS total_pagado,
			       COALESCE(t.total, 0) - COALESCE(p.pagado, 0) AS saldo
			FROM factura f
			LEFT JOIN (SELECT factura_numero_factura, SUM(valor) AS total
			           FROM detalle_factura
			           GROUP BY factura_numero_factura) t
			       ON t.factura_numero_factura = f.numero_factura
			LEFT JOIN (SELECT factura_numero_factura, SUM(valor) AS pagado
			           FROM pago
			           GROUP BY factura_numero_factura) p
			       ON p.factura_numero_factura = f.numero_factura
			WHERE f.numero_factura = ?
			""";

	/** Sentencia que inserta un registro en la tabla {@code factura}. */
	private static final String INSERTAR = """
			INSERT INTO factura (periodo, fecha_emision, fecha_vencimiento, estado, casa_numero, casa_manzana)
			VALUES (?, ?, ?, ?, ?, ?)
			RETURNING numero_factura
			""";

	/**
	 * Sentencia que cambia la fecha de vencimiento de una factura, solo si está {@code PENDIENTE} o
	 * {@code ABONADA}.
	 */
	private static final String ACTUALIZAR_VENCIMIENTO = """
			UPDATE factura
			SET fecha_vencimiento = ?
			WHERE numero_factura = ? AND estado IN ('PENDIENTE', 'ABONADA')
			""";

	/** Sentencia que elimina un registro de la tabla {@code factura} por su llave primaria. */
	private static final String ELIMINAR = "DELETE FROM factura WHERE numero_factura = ?";

	/**
	 * Sentencia que pasa a {@code VENCIDA} las facturas {@code PENDIENTE} o {@code ABONADA} cuya fecha
	 * de vencimiento ya pasó.
	 */
	private static final String MARCAR_VENCIDAS = """
			UPDATE factura
			SET estado = 'VENCIDA'
			WHERE estado IN ('PENDIENTE', 'ABONADA') AND fecha_vencimiento < ?
			""";

	/** Consulta que indica si existe la casa con ese número y manzana. */
	private static final String EXISTE_CASA =
			"SELECT EXISTS (SELECT 1 FROM casa WHERE numero = ? AND manzana = ?)";

	/** Consulta que indica si la casa ya tiene una factura de ese periodo. */
	private static final String EXISTE_PERIODO = """
			SELECT EXISTS (SELECT 1 FROM factura
			               WHERE casa_numero = ? AND casa_manzana = ? AND periodo = ?)
			""";

	/**
	 * Consulta las facturas registradas.
	 *
	 * @return lista de facturas; vacía si no hay registros
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 */
	public List<Factura> listar() throws SQLException {
		List<Factura> facturas = new ArrayList<>();
		try (Connection c = ConexionBD.obtenerConexion();
		     PreparedStatement ps = c.prepareStatement(LISTAR);
		     ResultSet rs = ps.executeQuery()) {

			while (rs.next()) {
				facturas.add(mapear(rs));
			}
		}
		return facturas;
	}

	/**
	 * Busca una factura por su llave primaria.
	 *
	 * @param id identificador de la factura
	 * @return factura encontrada, o {@code null} si no existe
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 */
	public Factura buscarPorId(int id) throws SQLException {
		try (Connection c = ConexionBD.obtenerConexion();
		     PreparedStatement ps = c.prepareStatement(BUSCAR_POR_ID)) {
			ps.setInt(1, id);
			try (ResultSet rs = ps.executeQuery()) {
				return rs.next() ? mapear(rs) : null;
			}
		}
	}

	/**
	 * Inserta una factura.
	 *
	 * @param f factura con el periodo, el estado y la casa
	 * @param emision fecha de emisión
	 * @param vencimiento fecha de vencimiento
	 * @return número de factura generado
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 */
	public int insertar(Factura f, LocalDate emision, LocalDate vencimiento) throws SQLException {
		try (Connection c = ConexionBD.obtenerConexion();
		     PreparedStatement ps = c.prepareStatement(INSERTAR)) {
			ps.setString(1, f.getPeriodo());
			ps.setObject(2, emision);
			ps.setObject(3, vencimiento);
			ps.setString(4, f.getEstado());
			ps.setInt(5, f.getCasaNumero());
			ps.setInt(6, f.getCasaManzana());
			try (ResultSet rs = ps.executeQuery()) {
				rs.next();
				return rs.getInt(1);
			}
		}
	}

	/**
	 * Cambia la fecha de vencimiento de una factura, solo si está {@code PENDIENTE} o {@code ABONADA}.
	 *
	 * @param id número de la factura
	 * @param vencimiento nueva fecha de vencimiento
	 * @return {@code true} si la factura se actualizó
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 */
	public boolean actualizarVencimiento(int id, LocalDate vencimiento) throws SQLException {
		try (Connection c = ConexionBD.obtenerConexion();
		     PreparedStatement ps = c.prepareStatement(ACTUALIZAR_VENCIMIENTO)) {
			ps.setObject(1, vencimiento);
			ps.setInt(2, id);
			return ps.executeUpdate() > 0;
		}
	}

	/**
	 * Elimina la factura indicada por su llave primaria.
	 *
	 * @param id identificador de la factura
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
	 * Pasa a {@code VENCIDA} las facturas {@code PENDIENTE} o {@code ABONADA} cuya fecha de
	 * vencimiento es anterior a la fecha indicada.
	 *
	 * @param hoy fecha de referencia
	 * @return cantidad de facturas actualizadas
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 */
	public int marcarVencidas(LocalDate hoy) throws SQLException {
		try (Connection c = ConexionBD.obtenerConexion();
		     PreparedStatement ps = c.prepareStatement(MARCAR_VENCIDAS)) {
			ps.setObject(1, hoy);
			return ps.executeUpdate();
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
		try (Connection c = ConexionBD.obtenerConexion();
		     PreparedStatement ps = c.prepareStatement(EXISTE_CASA)) {
			ps.setInt(1, numero);
			ps.setInt(2, manzana);
			try (ResultSet rs = ps.executeQuery()) {
				rs.next();
				return rs.getBoolean(1);
			}
		}
	}

	/**
	 * Indica si la casa ya tiene una factura del periodo.
	 *
	 * @param numero número de la casa
	 * @param manzana manzana de la casa
	 * @param periodo periodo en formato {@code AAAA-MM}
	 * @return {@code true} si ya existe la factura
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 */
	public boolean existePeriodo(int numero, int manzana, String periodo) throws SQLException {
		try (Connection c = ConexionBD.obtenerConexion();
		     PreparedStatement ps = c.prepareStatement(EXISTE_PERIODO)) {
			ps.setInt(1, numero);
			ps.setInt(2, manzana);
			ps.setString(3, periodo);
			try (ResultSet rs = ps.executeQuery()) {
				rs.next();
				return rs.getBoolean(1);
			}
		}
	}

	/**
	 * Convierte la fila actual del resultado en una factura.
	 *
	 * @param rs resultado de la consulta, ubicado en la fila que se va a leer
	 * @return factura leída
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 */
	private Factura mapear(ResultSet rs) throws SQLException {
		return new Factura(
				rs.getInt("numero_factura"),
				rs.getString("periodo"),
				rs.getString("fecha_emision"),
				rs.getString("fecha_vencimiento"),
				rs.getString("estado"),
				rs.getBigDecimal("total"),
				rs.getInt("casa_numero"),
				rs.getInt("casa_manzana"),
				rs.getBigDecimal("total_pagado"),
				rs.getBigDecimal("saldo")
		);
	}
}