package co.edu.unbosque.dao;

import co.edu.unbosque.config.ConexionBD;
import co.edu.unbosque.model.Pago;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PagoDAO {

	private static final String LISTAR = """
			SELECT id_pago, fecha, valor, metodo_pago, factura_numero_factura,
			       persona_tipo_documento, persona_numero_documento
			FROM pago
			ORDER BY fecha, id_pago
			""";

	
	private static final String BLOQUEAR_FACTURA =
			"SELECT numero_factura FROM factura WHERE numero_factura = ? FOR UPDATE";

	private static final String INSERTAR = """
			INSERT INTO pago (fecha, valor, metodo_pago, factura_numero_factura,
			                  persona_tipo_documento, persona_numero_documento)
			SELECT ?::date, ?::numeric, ?, ?, ?, ?
			WHERE ?::numeric <= COALESCE((SELECT SUM(valor) FROM detalle_factura
			                              WHERE factura_numero_factura = ?), 0)
			                  - COALESCE((SELECT SUM(valor) FROM pago
			                              WHERE factura_numero_factura = ?), 0)
			RETURNING id_pago
			""";

	private static final String ACTUALIZAR_ESTADO = """
			UPDATE factura
			SET estado = CASE
			        WHEN COALESCE((SELECT SUM(valor) FROM pago
			                       WHERE factura_numero_factura = factura.numero_factura), 0)
			             >= COALESCE((SELECT SUM(valor) FROM detalle_factura
			                          WHERE factura_numero_factura = factura.numero_factura), 0)
			             THEN 'PAGADA'
			        WHEN estado = 'PENDIENTE' THEN 'ABONADA'
			        ELSE estado
			    END
			WHERE numero_factura = ?
			""";

	private static final String EXISTE_PERSONA = """
			SELECT EXISTS (SELECT 1 FROM persona
			               WHERE tipo_documento = ? AND numero_documento = ?)
			""";

	public List<Pago> listar() throws SQLException {
		List<Pago> pagos = new ArrayList<>();
		try (Connection c = ConexionBD.obtenerConexion();
		     PreparedStatement ps = c.prepareStatement(LISTAR);
		     ResultSet rs = ps.executeQuery()) {

			while (rs.next()) {
				pagos.add(mapear(rs));
			}
		}
		return pagos;
	}

	public int registrar(Pago p, LocalDate fecha, BigDecimal valor, String metodo) throws SQLException {
		try (Connection c = ConexionBD.obtenerConexion()) {
			c.setAutoCommit(false);
			try {
				try (PreparedStatement ps = c.prepareStatement(BLOQUEAR_FACTURA)) {
					ps.setInt(1, p.getNumeroFactura());
					try (ResultSet rs = ps.executeQuery()) {
						if (!rs.next()) {
							c.rollback();
							return 0;
						}
					}
				}

				int id;
				try (PreparedStatement ps = c.prepareStatement(INSERTAR)) {
					ps.setObject(1, fecha);
					ps.setBigDecimal(2, valor);
					ps.setString(3, metodo);
					ps.setInt(4, p.getNumeroFactura());
					ps.setString(5, p.getPersonaTipoDocumento());
					ps.setString(6, p.getPersonaNumeroDocumento());
					ps.setBigDecimal(7, valor);
					ps.setInt(8, p.getNumeroFactura());
					ps.setInt(9, p.getNumeroFactura());
					try (ResultSet rs = ps.executeQuery()) {
						if (!rs.next()) {
							c.rollback();
							return -1;
						}
						id = rs.getInt(1);
					}
				}

				try (PreparedStatement ps = c.prepareStatement(ACTUALIZAR_ESTADO)) {
					ps.setInt(1, p.getNumeroFactura());
					ps.executeUpdate();
				}

				c.commit();
				return id;
			} catch (SQLException e) {
				c.rollback();
				throw e;
			}
		}
	}

	public boolean existePersona(String tipoDocumento, String numeroDocumento) throws SQLException {
		try (Connection c = ConexionBD.obtenerConexion();
		     PreparedStatement ps = c.prepareStatement(EXISTE_PERSONA)) {
			ps.setString(1, tipoDocumento);
			ps.setString(2, numeroDocumento);
			try (ResultSet rs = ps.executeQuery()) {
				rs.next();
				return rs.getBoolean(1);
			}
		}
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