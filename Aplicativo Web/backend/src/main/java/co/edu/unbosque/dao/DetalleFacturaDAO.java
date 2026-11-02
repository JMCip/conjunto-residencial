package co.edu.unbosque.dao;

import co.edu.unbosque.config.ConexionBD;
import co.edu.unbosque.model.ConceptoCobro;
import co.edu.unbosque.model.DetalleFactura;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class DetalleFacturaDAO {

	
	public record Origen(int casaNumero, int casaManzana, String estado, BigDecimal valor) {
	}

	private static final String LISTAR = """
			SELECT factura_numero_factura, 
				   numero_linea, 
				   concepto_cobro_id_concepto, 
				   valor,
			       asignacion_celda_id_asignacion, 
			       reserva_salon_id_reserva, 
			       sancion_id_sancion
			FROM detalle_factura
			ORDER BY factura_numero_factura, numero_linea
			""";

	private static final String BUSCAR = """
			SELECT factura_numero_factura,
			 	   numero_linea, 
			 	   concepto_cobro_id_concepto, 
			 	   valor,
			       asignacion_celda_id_asignacion, 
			       reserva_salon_id_reserva, 
			       sancion_id_sancion
			FROM detalle_factura
			WHERE factura_numero_factura = ? AND numero_linea = ?
			""";

	
	private static final String INSERTAR = """
			INSERT INTO detalle_factura (factura_numero_factura, numero_linea, valor,
			       concepto_cobro_id_concepto, asignacion_celda_id_asignacion,
			       reserva_salon_id_reserva, sancion_id_sancion)
			SELECT ?, COALESCE(MAX(numero_linea), 0) + 1, ?, ?, ?, ?, ?
			FROM detalle_factura
			WHERE factura_numero_factura = ?
			RETURNING numero_linea
			""";

	private static final String ACTUALIZAR_VALOR = """
			UPDATE detalle_factura SET valor = ?
			WHERE factura_numero_factura = ? AND numero_linea = ?
			""";

	private static final String ELIMINAR = """
			DELETE FROM detalle_factura
			WHERE factura_numero_factura = ? AND numero_linea = ?
			""";

	private static final String BUSCAR_CONCEPTO = """
			SELECT id_concepto, nombre, descripcion, valor_base
			FROM concepto_cobro WHERE id_concepto = ?
			""";

	private static final String BUSCAR_SANCION = """
			SELECT casa_numero, casa_manzana, estado_proceso, valor
			FROM sancion WHERE id_sancion = ?
			""";

	private static final String BUSCAR_RESERVA = """
			SELECT casa_numero, casa_manzana, estado,
			       ROUND(EXTRACT(EPOCH FROM (hora_final - hora_inicio)) / 3600 * valor_por_hora, 2) AS valor
			FROM reserva_salon WHERE id_reserva = ?
			""";

	private static final String BUSCAR_ASIGNACION = """
			SELECT casa_numero, casa_manzana, 'ASIGNADA' AS estado, NULL::numeric AS valor
			FROM asignacion_celda WHERE id_asignacion = ?
			""";

	private static final String SANCION_FACTURADA =
			"SELECT EXISTS (SELECT 1 FROM detalle_factura WHERE sancion_id_sancion = ?)";

	private static final String RESERVA_FACTURADA =
			"SELECT EXISTS (SELECT 1 FROM detalle_factura WHERE reserva_salon_id_reserva = ?)";

	
	private static final String ASIGNACION_EN_FACTURA = """
			SELECT EXISTS (SELECT 1 FROM detalle_factura
			               WHERE asignacion_celda_id_asignacion = ? AND factura_numero_factura = ?)
			""";

	public List<DetalleFactura> listar() throws SQLException {
		List<DetalleFactura> detalles = new ArrayList<>();
		try (Connection c = ConexionBD.obtenerConexion();
		     PreparedStatement ps = c.prepareStatement(LISTAR);
		     ResultSet rs = ps.executeQuery()) {

			while (rs.next()) {
				detalles.add(mapear(rs));
			}
		}
		return detalles;
	}

	/** Devuelve la línea, o null si no existe. */
	public DetalleFactura buscar(int factura, int linea) throws SQLException {
		try (Connection c = ConexionBD.obtenerConexion();
		     PreparedStatement ps = c.prepareStatement(BUSCAR)) {
			ps.setInt(1, factura);
			ps.setInt(2, linea);
			try (ResultSet rs = ps.executeQuery()) {
				return rs.next() ? mapear(rs) : null;
			}
		}
	}


	public int insertar(DetalleFactura d, BigDecimal valor) throws SQLException {
		try (Connection c = ConexionBD.obtenerConexion();
		     PreparedStatement ps = c.prepareStatement(INSERTAR)) {
			ps.setInt(1, d.getNumeroFactura());
			ps.setBigDecimal(2, valor);
			ps.setInt(3, d.getIdConcepto());
			ps.setObject(4, d.getIdAsignacion(), Types.INTEGER);
			ps.setObject(5, d.getIdReserva(), Types.INTEGER);
			ps.setObject(6, d.getIdSancion(), Types.INTEGER);
			ps.setInt(7, d.getNumeroFactura());
			try (ResultSet rs = ps.executeQuery()) {
				rs.next();
				return rs.getInt(1);
			}
		}
	}

	public boolean actualizarValor(int factura, int linea, BigDecimal valor) throws SQLException {
		try (Connection c = ConexionBD.obtenerConexion();
		     PreparedStatement ps = c.prepareStatement(ACTUALIZAR_VALOR)) {
			ps.setBigDecimal(1, valor);
			ps.setInt(2, factura);
			ps.setInt(3, linea);
			return ps.executeUpdate() > 0;
		}
	}

	public boolean eliminar(int factura, int linea) throws SQLException {
		try (Connection c = ConexionBD.obtenerConexion();
		     PreparedStatement ps = c.prepareStatement(ELIMINAR)) {
			ps.setInt(1, factura);
			ps.setInt(2, linea);
			return ps.executeUpdate() > 0;
		}
	}

	public ConceptoCobro buscarConcepto(int id) throws SQLException {
		try (Connection c = ConexionBD.obtenerConexion();
		     PreparedStatement ps = c.prepareStatement(BUSCAR_CONCEPTO)) {
			ps.setInt(1, id);
			try (ResultSet rs = ps.executeQuery()) {
				if (!rs.next()) {
					return null;
				}
				return new ConceptoCobro(rs.getInt("id_concepto"), rs.getString("nombre"),
						rs.getString("descripcion"), rs.getBigDecimal("valor_base"));
			}
		}
	}

	public Origen buscarSancion(int id) throws SQLException {
		return buscarOrigen(BUSCAR_SANCION, id);
	}

	public Origen buscarReserva(int id) throws SQLException {
		return buscarOrigen(BUSCAR_RESERVA, id);
	}

	public Origen buscarAsignacion(int id) throws SQLException {
		return buscarOrigen(BUSCAR_ASIGNACION, id);
	}

	public boolean sancionFacturada(int id) throws SQLException {
		return existe(SANCION_FACTURADA, id, null);
	}

	public boolean reservaFacturada(int id) throws SQLException {
		return existe(RESERVA_FACTURADA, id, null);
	}

	public boolean asignacionEnFactura(int idAsignacion, int factura) throws SQLException {
		return existe(ASIGNACION_EN_FACTURA, idAsignacion, factura);
	}

	private Origen buscarOrigen(String sql, int id) throws SQLException {
		try (Connection c = ConexionBD.obtenerConexion();
		     PreparedStatement ps = c.prepareStatement(sql)) {
			ps.setInt(1, id);
			try (ResultSet rs = ps.executeQuery()) {
				if (!rs.next()) {
					return null;
				}
				return new Origen(rs.getInt(1), rs.getInt(2), rs.getString(3), rs.getBigDecimal(4));
			}
		}
	}

	private boolean existe(String sql, int primero, Integer segundo) throws SQLException {
		try (Connection c = ConexionBD.obtenerConexion();
		     PreparedStatement ps = c.prepareStatement(sql)) {
			ps.setInt(1, primero);
			if (segundo != null) {
				ps.setInt(2, segundo);
			}
			try (ResultSet rs = ps.executeQuery()) {
				rs.next();
				return rs.getBoolean(1);
			}
		}
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