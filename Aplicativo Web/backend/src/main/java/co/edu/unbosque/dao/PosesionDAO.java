package co.edu.unbosque.dao;
import co.edu.unbosque.config.ConexionBD;
import co.edu.unbosque.model.Posesion;
import java.math.BigDecimal;
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
	
	private static final String BUSCAR = """
			SELECT id_posesion, porcentaje, fecha_compra, fecha_venta, casa_numero, casa_manzana,
			       propietario_tipo_documento, propietario_numero_documento
			FROM posesion
			WHERE id_posesion = ?
			""";

	public Posesion buscar(int id) throws SQLException {
		try (Connection c = ConexionBD.obtenerConexion();
				PreparedStatement ps = c.prepareStatement(BUSCAR)) {
			ps.setInt(1, id);
			try (ResultSet rs = ps.executeQuery()) {
				return rs.next() ? mapear(rs) : null;
			}
		}
	}

	private static final String SUMA_VIGENTE = """
			SELECT COALESCE(SUM(porcentaje), 0)
			FROM posesion
			WHERE casa_numero = ?
			AND casa_manzana = ?
			AND fecha_venta IS NULL
			AND id_posesion <> ?
			""";

	public BigDecimal sumaPorcentajeVigente(int casaNumero, int casaManzana, int excluirId) throws SQLException {
		try (Connection c = ConexionBD.obtenerConexion();
				PreparedStatement ps = c.prepareStatement(SUMA_VIGENTE)) {
			ps.setInt(1, casaNumero);
			ps.setInt(2, casaManzana);
			ps.setInt(3, excluirId);
			try (ResultSet rs = ps.executeQuery()) {
				rs.next();
				return rs.getBigDecimal(1);
			}
		}
	}

	private static final String INSERTAR = """
			INSERT INTO posesion (porcentaje, fecha_compra, fecha_venta, casa_numero, casa_manzana,
			                      propietario_tipo_documento, propietario_numero_documento)
			VALUES (?, CAST(? AS DATE), CAST(? AS DATE), ?, ?, ?, ?)
			RETURNING id_posesion
			""";

	public int insertar(Posesion p) throws SQLException {
		try (Connection c = ConexionBD.obtenerConexion();
				PreparedStatement ps = c.prepareStatement(INSERTAR)) {
			ps.setBigDecimal(1, p.getPorcentaje());
			ps.setString(2, p.getFechaCompra());
			ps.setString(3, p.getFechaVenta());
			ps.setInt(4, p.getCasaNumero());
			ps.setInt(5, p.getCasaManzana());
			ps.setString(6, p.getPropietarioTipoDocumento());
			ps.setString(7, p.getPropietarioNumeroDocumento());
			try (ResultSet rs = ps.executeQuery()) {
				rs.next();
				return rs.getInt(1);
			}
		}
	}

	private static final String ACTUALIZAR = """
			UPDATE posesion
			SET porcentaje = ?,
			    fecha_compra = CAST(? AS DATE),
			    fecha_venta = CAST(? AS DATE)
			WHERE id_posesion = ?
			""";

	public boolean actualizar(Posesion p) throws SQLException {
		try (Connection c = ConexionBD.obtenerConexion();
				PreparedStatement ps = c.prepareStatement(ACTUALIZAR)) {
			ps.setBigDecimal(1, p.getPorcentaje());
			ps.setString(2, p.getFechaCompra());
			ps.setString(3, p.getFechaVenta());
			ps.setInt(4, p.getIdPosesion());
			return ps.executeUpdate() > 0;
		}
	}

	private static final String ELIMINAR = """
			DELETE FROM posesion
			WHERE id_posesion = ?
			""";

	public boolean eliminar(int id) throws SQLException {
		try (Connection c = ConexionBD.obtenerConexion();
				PreparedStatement ps = c.prepareStatement(ELIMINAR)) {
			ps.setInt(1, id);
			return ps.executeUpdate() > 0;
		}
	}
	
}
