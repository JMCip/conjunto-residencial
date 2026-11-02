package co.edu.unbosque.dao;
import co.edu.unbosque.config.ConexionBD;
import co.edu.unbosque.model.AsignacionCelda;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AsignacionCeldaDAO {
	
	private static final String LISTAR = """
			SELECT id_asignacion, fecha_inicio, fecha_fin, casa_numero, casa_manzana, celda_parqueadero_id_celda
			FROM asignacion_celda
			ORDER BY id_asignacion
			""";
	
	public List<AsignacionCelda> listar() throws SQLException {
		List<AsignacionCelda> asignaciones = new ArrayList<>();
		try (	Connection c = ConexionBD.obtenerConexion();
				PreparedStatement ps = c.prepareStatement(LISTAR);
				ResultSet rs = ps.executeQuery()) {
			
			while (rs.next()) {
				asignaciones.add(mapear(rs));
			}
		}
		return asignaciones;
	}
	
	private AsignacionCelda mapear(ResultSet rs) throws SQLException {
		return new AsignacionCelda(
				rs.getInt("id_asignacion"),
				rs.getString("fecha_inicio"),
				rs.getString("fecha_fin"),
				rs.getInt("casa_numero"),
				rs.getInt("casa_manzana"),
				rs.getInt("celda_parqueadero_id_celda")
				);
	}
	
	private static final String BUSCAR = """
			SELECT id_asignacion, fecha_inicio, fecha_fin, casa_numero, casa_manzana, celda_parqueadero_id_celda
			FROM asignacion_celda
			WHERE id_asignacion = ?
			""";

	public AsignacionCelda buscar(int id) throws SQLException {
		try (Connection c = ConexionBD.obtenerConexion();
				PreparedStatement ps = c.prepareStatement(BUSCAR)) {
			ps.setInt(1, id);
			try (ResultSet rs = ps.executeQuery()) {
				return rs.next() ? mapear(rs) : null;
			}
		}
	}

	// S-33: una celda tiene a lo sumo una asignación vigente.
	private static final String CELDA_ASIGNADA = """
			SELECT COUNT(*)
			FROM asignacion_celda
			WHERE celda_parqueadero_id_celda = ?
			AND fecha_fin IS NULL
			AND id_asignacion <> ?
			""";

	public boolean celdaConAsignacionVigente(int idCelda, int excluirId) throws SQLException {
		try (Connection c = ConexionBD.obtenerConexion();
				PreparedStatement ps = c.prepareStatement(CELDA_ASIGNADA)) {
			ps.setInt(1, idCelda);
			ps.setInt(2, excluirId);
			try (ResultSet rs = ps.executeQuery()) {
				rs.next();
				return rs.getInt(1) > 0;
			}
		}
	}

	private static final String INSERTAR = """
			INSERT INTO asignacion_celda (fecha_inicio, fecha_fin, casa_numero, casa_manzana, celda_parqueadero_id_celda)
			VALUES (CAST(? AS DATE), CAST(? AS DATE), ?, ?, ?)
			RETURNING id_asignacion
			""";

	public int insertar(AsignacionCelda a) throws SQLException {
		try (Connection c = ConexionBD.obtenerConexion();
				PreparedStatement ps = c.prepareStatement(INSERTAR)) {
			ps.setString(1, a.getFechaInicio());
			ps.setString(2, a.getFechaFin());
			ps.setInt(3, a.getCasaNumero());
			ps.setInt(4, a.getCasaManzana());
			ps.setInt(5, a.getIdCelda());
			try (ResultSet rs = ps.executeQuery()) {
				rs.next();
				return rs.getInt(1);
			}
		}
	}

	private static final String ACTUALIZAR = """
			UPDATE asignacion_celda
			SET fecha_inicio = CAST(? AS DATE),
			    fecha_fin = CAST(? AS DATE)
			WHERE id_asignacion = ?
			""";

	public boolean actualizar(AsignacionCelda a) throws SQLException {
		try (Connection c = ConexionBD.obtenerConexion();
				PreparedStatement ps = c.prepareStatement(ACTUALIZAR)) {
			ps.setString(1, a.getFechaInicio());
			ps.setString(2, a.getFechaFin());
			ps.setInt(3, a.getIdAsignacion());
			return ps.executeUpdate() > 0;
		}
	}

	private static final String ELIMINAR = """
			DELETE FROM asignacion_celda
			WHERE id_asignacion = ?
			""";

	public boolean eliminar(int id) throws SQLException {
		try (Connection c = ConexionBD.obtenerConexion();
				PreparedStatement ps = c.prepareStatement(ELIMINAR)) {
			ps.setInt(1, id);
			return ps.executeUpdate() > 0;
		}
	}
	
}
