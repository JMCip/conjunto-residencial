package co.edu.unbosque.dao;
import co.edu.unbosque.config.ConexionBD;
import co.edu.unbosque.model.Parqueo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ParqueoDAO {
	
	private static final String LISTAR = """
			SELECT id_parqueo, fecha_hora_entrada, fecha_hora_salida, vehiculo_placa, celda_parqueadero_id_celda
			FROM parqueo
			ORDER BY fecha_hora_entrada DESC
			""";
	
	public List<Parqueo> listar() throws SQLException {
		List<Parqueo> parqueos = new ArrayList<>();
		try (	Connection c = ConexionBD.obtenerConexion();
				PreparedStatement ps = c.prepareStatement(LISTAR);
				ResultSet rs = ps.executeQuery()) {
			
			while (rs.next()) {
				parqueos.add(mapear(rs));
			}
		}
		return parqueos;
	}
	
	private Parqueo mapear(ResultSet rs) throws SQLException {
		return new Parqueo(
				rs.getInt("id_parqueo"),
				rs.getString("fecha_hora_entrada"),
				rs.getString("fecha_hora_salida"),
				rs.getString("vehiculo_placa"),
				rs.getInt("celda_parqueadero_id_celda")
				);
	}
	
	private static final String BUSCAR = """
			SELECT id_parqueo, fecha_hora_entrada, fecha_hora_salida, vehiculo_placa, celda_parqueadero_id_celda
			FROM parqueo
			WHERE id_parqueo = ?
			""";

	public Parqueo buscar(int id) throws SQLException {
		try (Connection c = ConexionBD.obtenerConexion();
				PreparedStatement ps = c.prepareStatement(BUSCAR)) {
			ps.setInt(1, id);
			try (ResultSet rs = ps.executeQuery()) {
				return rs.next() ? mapear(rs) : null;
			}
		}
	}

	// S-33: una celda tiene a lo sumo un parqueo abierto a la vez.
	private static final String CELDA_OCUPADA = """
			SELECT COUNT(*)
			FROM parqueo
			WHERE celda_parqueadero_id_celda = ?
			AND fecha_hora_salida IS NULL
			AND id_parqueo <> ?
			""";

	public boolean celdaConParqueoAbierto(int idCelda, int excluirId) throws SQLException {
		try (Connection c = ConexionBD.obtenerConexion();
				PreparedStatement ps = c.prepareStatement(CELDA_OCUPADA)) {
			ps.setInt(1, idCelda);
			ps.setInt(2, excluirId);
			try (ResultSet rs = ps.executeQuery()) {
				rs.next();
				return rs.getInt(1) > 0;
			}
		}
	}

	// S-31: un vehículo no puede estar parqueado dos veces a la vez.
	private static final String VEHICULO_PARQUEADO = """
			SELECT COUNT(*)
			FROM parqueo
			WHERE vehiculo_placa = ?
			AND fecha_hora_salida IS NULL
			AND id_parqueo <> ?
			""";

	public boolean vehiculoConParqueoAbierto(String placa, int excluirId) throws SQLException {
		try (Connection c = ConexionBD.obtenerConexion();
				PreparedStatement ps = c.prepareStatement(VEHICULO_PARQUEADO)) {
			ps.setString(1, placa);
			ps.setInt(2, excluirId);
			try (ResultSet rs = ps.executeQuery()) {
				rs.next();
				return rs.getInt(1) > 0;
			}
		}
	}

	private static final String INSERTAR = """
			INSERT INTO parqueo (fecha_hora_entrada, fecha_hora_salida, vehiculo_placa, celda_parqueadero_id_celda)
			VALUES (CAST(? AS TIMESTAMP), CAST(? AS TIMESTAMP), ?, ?)
			RETURNING id_parqueo
			""";

	public int insertar(Parqueo p) throws SQLException {
		try (Connection c = ConexionBD.obtenerConexion();
				PreparedStatement ps = c.prepareStatement(INSERTAR)) {
			ps.setString(1, p.getFechaHoraEntrada());
			ps.setString(2, p.getFechaHoraSalida());
			ps.setString(3, p.getVehiculoPlaca());
			ps.setInt(4, p.getIdCelda());
			try (ResultSet rs = ps.executeQuery()) {
				rs.next();
				return rs.getInt(1);
			}
		}
	}

	private static final String ACTUALIZAR = """
			UPDATE parqueo
			SET fecha_hora_entrada = CAST(? AS TIMESTAMP),
			    fecha_hora_salida = CAST(? AS TIMESTAMP)
			WHERE id_parqueo = ?
			""";

	public boolean actualizar(Parqueo p) throws SQLException {
		try (Connection c = ConexionBD.obtenerConexion();
				PreparedStatement ps = c.prepareStatement(ACTUALIZAR)) {
			ps.setString(1, p.getFechaHoraEntrada());
			ps.setString(2, p.getFechaHoraSalida());
			ps.setInt(3, p.getIdParqueo());
			return ps.executeUpdate() > 0;
		}
	}

	private static final String ELIMINAR = """
			DELETE FROM parqueo
			WHERE id_parqueo = ?
			""";

	public boolean eliminar(int id) throws SQLException {
		try (Connection c = ConexionBD.obtenerConexion();
				PreparedStatement ps = c.prepareStatement(ELIMINAR)) {
			ps.setInt(1, id);
			return ps.executeUpdate() > 0;
		}
	}
	
}
