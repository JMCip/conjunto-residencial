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
	
	private static final String BUSCAR = """
			SELECT id_contrato, cuota_mensual, fecha_inicio, fecha_fin, estado_contrato,
			       arrendatario_tipo_documento, arrendatario_numero_documento, casa_numero, casa_manzana,
			       propietario_tipo_documento, propietario_numero_documento
			FROM arriendo
			WHERE id_contrato = ?
			""";

	public Arriendo buscar(int id) throws SQLException {
		try (Connection c = ConexionBD.obtenerConexion();
				PreparedStatement ps = c.prepareStatement(BUSCAR)) {
			ps.setInt(1, id);
			try (ResultSet rs = ps.executeQuery()) {
				return rs.next() ? mapear(rs) : null;
			}
		}
	}

	private static final String EXISTE_VIGENTE = """
			SELECT COUNT(*)
			FROM arriendo
			WHERE casa_numero = ?
			AND casa_manzana = ?
			AND UPPER(estado_contrato) = 'VIGENTE'
			AND id_contrato <> ?
			""";

	public boolean existeVigente(int casaNumero, int casaManzana, int excluirId) throws SQLException {
		try (Connection c = ConexionBD.obtenerConexion();
				PreparedStatement ps = c.prepareStatement(EXISTE_VIGENTE)) {
			ps.setInt(1, casaNumero);
			ps.setInt(2, casaManzana);
			ps.setInt(3, excluirId);
			try (ResultSet rs = ps.executeQuery()) {
				rs.next();
				return rs.getInt(1) > 0;
			}
		}
	}

	private static final String ES_PROPIETARIO_VIGENTE = """
			SELECT COUNT(*)
			FROM posesion
			WHERE casa_numero = ?
			AND casa_manzana = ?
			AND propietario_tipo_documento = ?
			AND propietario_numero_documento = ?
			AND fecha_venta IS NULL
			""";

	public boolean esPropietarioVigente(int casaNumero, int casaManzana, String tipoDoc, String numeroDoc)
			throws SQLException {
		try (Connection c = ConexionBD.obtenerConexion();
				PreparedStatement ps = c.prepareStatement(ES_PROPIETARIO_VIGENTE)) {
			ps.setInt(1, casaNumero);
			ps.setInt(2, casaManzana);
			ps.setString(3, tipoDoc);
			ps.setString(4, numeroDoc);
			try (ResultSet rs = ps.executeQuery()) {
				rs.next();
				return rs.getInt(1) > 0;
			}
		}
	}

	private static final String INSERTAR = """
			INSERT INTO arriendo (cuota_mensual, fecha_inicio, fecha_fin, estado_contrato,
			                      arrendatario_tipo_documento, arrendatario_numero_documento,
			                      casa_numero, casa_manzana,
			                      propietario_tipo_documento, propietario_numero_documento)
			VALUES (?, CAST(? AS DATE), CAST(? AS DATE), ?, ?, ?, ?, ?, ?, ?)
			RETURNING id_contrato
			""";

	public int insertar(Arriendo a) throws SQLException {
		try (Connection c = ConexionBD.obtenerConexion();
				PreparedStatement ps = c.prepareStatement(INSERTAR)) {
			ps.setBigDecimal(1, a.getCuotaMensual());
			ps.setString(2, a.getFechaInicio());
			ps.setString(3, a.getFechaFin());
			ps.setString(4, a.getEstadoContrato());
			ps.setString(5, a.getArrendatarioTipoDocumento());
			ps.setString(6, a.getArrendatarioNumeroDocumento());
			ps.setInt(7, a.getCasaNumero());
			ps.setInt(8, a.getCasaManzana());
			ps.setString(9, a.getPropietarioTipoDocumento());
			ps.setString(10, a.getPropietarioNumeroDocumento());
			try (ResultSet rs = ps.executeQuery()) {
				rs.next();
				return rs.getInt(1);
			}
		}
	}

	private static final String ACTUALIZAR = """
			UPDATE arriendo
			SET cuota_mensual = ?,
			    fecha_inicio = CAST(? AS DATE),
			    fecha_fin = CAST(? AS DATE),
			    estado_contrato = ?
			WHERE id_contrato = ?
			""";

	public boolean actualizar(Arriendo a) throws SQLException {
		try (Connection c = ConexionBD.obtenerConexion();
				PreparedStatement ps = c.prepareStatement(ACTUALIZAR)) {
			ps.setBigDecimal(1, a.getCuotaMensual());
			ps.setString(2, a.getFechaInicio());
			ps.setString(3, a.getFechaFin());
			ps.setString(4, a.getEstadoContrato());
			ps.setInt(5, a.getIdContrato());
			return ps.executeUpdate() > 0;
		}
	}

	private static final String ELIMINAR = """
			DELETE FROM arriendo
			WHERE id_contrato = ?
			""";

	public boolean eliminar(int id) throws SQLException {
		try (Connection c = ConexionBD.obtenerConexion();
				PreparedStatement ps = c.prepareStatement(ELIMINAR)) {
			ps.setInt(1, id);
			return ps.executeUpdate() > 0;
		}
	}
	
}
