package co.edu.unbosque.dao;
import co.edu.unbosque.config.ConexionBD;
import co.edu.unbosque.model.Visita;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class VisitaDAO {
	
	private static final String LISTAR = """
			SELECT id_visita, fecha_entrada, fecha_salida, visitante_tipo_documento, visitante_numero_documento,
			       casa_numero, casa_manzana
			FROM visita
			ORDER BY fecha_entrada DESC, id_visita DESC
			""";
	
	public List<Visita> listar() throws SQLException {
		List<Visita> visitas = new ArrayList<>();
		try (	Connection c = ConexionBD.obtenerConexion();
				PreparedStatement ps = c.prepareStatement(LISTAR);
				ResultSet rs = ps.executeQuery()) {
			
			while (rs.next()) {
				visitas.add(mapear(rs));
			}
		}
		return visitas;
	}
	
	private Visita mapear(ResultSet rs) throws SQLException {
		return new Visita(
				rs.getInt("id_visita"),
				rs.getString("fecha_entrada"),
				rs.getString("fecha_salida"),
				rs.getString("visitante_tipo_documento"),
				rs.getString("visitante_numero_documento"),
				rs.getInt("casa_numero"),
				rs.getInt("casa_manzana")
				);
	}
	
	private static final String BUSCAR = """
			SELECT id_visita, fecha_entrada, fecha_salida, visitante_tipo_documento, visitante_numero_documento,
			       casa_numero, casa_manzana
			FROM visita
			WHERE id_visita = ?
			""";

	public Visita buscar(int id) throws SQLException {
		try (Connection c = ConexionBD.obtenerConexion();
				PreparedStatement ps = c.prepareStatement(BUSCAR)) {
			ps.setInt(1, id);
			try (ResultSet rs = ps.executeQuery()) {
				return rs.next() ? mapear(rs) : null;
			}
		}
	}

	private static final String INSERTAR = """
			INSERT INTO visita (fecha_entrada, fecha_salida, visitante_tipo_documento, visitante_numero_documento,
			                    casa_numero, casa_manzana)
			VALUES (CAST(? AS DATE), CAST(? AS DATE), ?, ?, ?, ?)
			RETURNING id_visita
			""";

	public int insertar(Visita v) throws SQLException {
		try (Connection c = ConexionBD.obtenerConexion();
				PreparedStatement ps = c.prepareStatement(INSERTAR)) {
			ps.setString(1, v.getFechaEntrada());
			ps.setString(2, v.getFechaSalida());
			ps.setString(3, v.getVisitanteTipoDocumento());
			ps.setString(4, v.getVisitanteNumeroDocumento());
			ps.setInt(5, v.getCasaNumero());
			ps.setInt(6, v.getCasaManzana());
			try (ResultSet rs = ps.executeQuery()) {
				rs.next();
				return rs.getInt(1);
			}
		}
	}

	private static final String ACTUALIZAR = """
			UPDATE visita
			SET fecha_entrada = CAST(? AS DATE),
			    fecha_salida = CAST(? AS DATE)
			WHERE id_visita = ?
			""";

	public boolean actualizar(Visita v) throws SQLException {
		try (Connection c = ConexionBD.obtenerConexion();
				PreparedStatement ps = c.prepareStatement(ACTUALIZAR)) {
			ps.setString(1, v.getFechaEntrada());
			ps.setString(2, v.getFechaSalida());
			ps.setInt(3, v.getIdVisita());
			return ps.executeUpdate() > 0;
		}
	}

	private static final String ELIMINAR = """
			DELETE FROM visita
			WHERE id_visita = ?
			""";

	public boolean eliminar(int id) throws SQLException {
		try (Connection c = ConexionBD.obtenerConexion();
				PreparedStatement ps = c.prepareStatement(ELIMINAR)) {
			ps.setInt(1, id);
			return ps.executeUpdate() > 0;
		}
	}
	
}
