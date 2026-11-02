package co.edu.unbosque.dao;
import co.edu.unbosque.config.ConexionBD;
import co.edu.unbosque.model.Residencia;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ResidenciaDAO {
	
	private static final String LISTAR = """
			SELECT id_residencia, fecha_ingreso, fecha_salida, residente_tipo_documento, residente_numero_documento,
			       casa_numero, casa_manzana
			FROM residencia
			ORDER BY casa_manzana, casa_numero, fecha_ingreso
			""";
	
	public List<Residencia> listar() throws SQLException {
		List<Residencia> residencias = new ArrayList<>();
		try (	Connection c = ConexionBD.obtenerConexion();
				PreparedStatement ps = c.prepareStatement(LISTAR);
				ResultSet rs = ps.executeQuery()) {
			
			while (rs.next()) {
				residencias.add(mapear(rs));
			}
		}
		return residencias;
	}
	
	private Residencia mapear(ResultSet rs) throws SQLException {
		return new Residencia(
				rs.getInt("id_residencia"),
				rs.getString("fecha_ingreso"),
				rs.getString("fecha_salida"),
				rs.getString("residente_tipo_documento"),
				rs.getString("residente_numero_documento"),
				rs.getInt("casa_numero"),
				rs.getInt("casa_manzana")
				);
	}
	
	private static final String BUSCAR = """
			SELECT id_residencia, fecha_ingreso, fecha_salida, residente_tipo_documento, residente_numero_documento,
			       casa_numero, casa_manzana
			FROM residencia
			WHERE id_residencia = ?
			""";

	public Residencia buscar(int id) throws SQLException {
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
			FROM residencia
			WHERE residente_tipo_documento = ?
			AND residente_numero_documento = ?
			AND casa_numero = ?
			AND casa_manzana = ?
			AND fecha_salida IS NULL
			AND id_residencia <> ?
			""";

	public boolean existeVigente(String tipoDoc, String numeroDoc, int casaNumero, int casaManzana, int excluirId)
			throws SQLException {
		try (Connection c = ConexionBD.obtenerConexion();
				PreparedStatement ps = c.prepareStatement(EXISTE_VIGENTE)) {
			ps.setString(1, tipoDoc);
			ps.setString(2, numeroDoc);
			ps.setInt(3, casaNumero);
			ps.setInt(4, casaManzana);
			ps.setInt(5, excluirId);
			try (ResultSet rs = ps.executeQuery()) {
				rs.next();
				return rs.getInt(1) > 0;
			}
		}
	}

	private static final String INSERTAR = """
			INSERT INTO residencia (fecha_ingreso, fecha_salida, residente_tipo_documento, residente_numero_documento,
			                        casa_numero, casa_manzana)
			VALUES (CAST(? AS DATE), CAST(? AS DATE), ?, ?, ?, ?)
			RETURNING id_residencia
			""";

	public int insertar(Residencia r) throws SQLException {
		try (Connection c = ConexionBD.obtenerConexion();
				PreparedStatement ps = c.prepareStatement(INSERTAR)) {
			ps.setString(1, r.getFechaIngreso());
			ps.setString(2, r.getFechaSalida());
			ps.setString(3, r.getResidenteTipoDocumento());
			ps.setString(4, r.getResidenteNumeroDocumento());
			ps.setInt(5, r.getCasaNumero());
			ps.setInt(6, r.getCasaManzana());
			try (ResultSet rs = ps.executeQuery()) {
				rs.next();
				return rs.getInt(1);
			}
		}
	}

	private static final String ACTUALIZAR = """
			UPDATE residencia
			SET fecha_ingreso = CAST(? AS DATE),
			    fecha_salida = CAST(? AS DATE)
			WHERE id_residencia = ?
			""";

	public boolean actualizar(Residencia r) throws SQLException {
		try (Connection c = ConexionBD.obtenerConexion();
				PreparedStatement ps = c.prepareStatement(ACTUALIZAR)) {
			ps.setString(1, r.getFechaIngreso());
			ps.setString(2, r.getFechaSalida());
			ps.setInt(3, r.getIdResidencia());
			return ps.executeUpdate() > 0;
		}
	}

	private static final String ELIMINAR = """
			DELETE FROM residencia
			WHERE id_residencia = ?
			""";

	public boolean eliminar(int id) throws SQLException {
		try (Connection c = ConexionBD.obtenerConexion();
				PreparedStatement ps = c.prepareStatement(ELIMINAR)) {
			ps.setInt(1, id);
			return ps.executeUpdate() > 0;
		}
	}
	
}
