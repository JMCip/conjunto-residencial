package co.edu.unbosque.dao;
import co.edu.unbosque.config.ConexionBD;
import co.edu.unbosque.model.Parqueo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a datos de los parqueos con JDBC.
 * <p>
 * Cada método abre su propia conexión con {@link co.edu.unbosque.config.ConexionBD} y usa
 * {@link java.sql.PreparedStatement} para enviar los parámetros de forma segura.
 */
public class ParqueoDAO {
	
	/** Consulta que lista todos los registros de la tabla {@code parqueo}. */
	private static final String LISTAR = """
			SELECT id_parqueo, fecha_hora_entrada, fecha_hora_salida, vehiculo_placa, celda_parqueadero_id_celda
			FROM parqueo
			ORDER BY fecha_hora_entrada DESC
			""";
	
	/**
	 * Consulta los parqueos registrados.
	 *
	 * @return lista de parqueos; vacía si no hay registros
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 */
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
	
	/**
	 * Convierte la fila actual del resultado en un parqueo.
	 *
	 * @param rs resultado de la consulta, ubicado en la fila que se va a leer
	 * @return parqueo leído
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 */
	private Parqueo mapear(ResultSet rs) throws SQLException {
		return new Parqueo(
				rs.getInt("id_parqueo"),
				rs.getString("fecha_hora_entrada"),
				rs.getString("fecha_hora_salida"),
				rs.getString("vehiculo_placa"),
				rs.getInt("celda_parqueadero_id_celda")
				);
	}
	
	/** Consulta que busca un registro de la tabla {@code parqueo} por su llave primaria. */
	private static final String BUSCAR = """
			SELECT id_parqueo, fecha_hora_entrada, fecha_hora_salida, vehiculo_placa, celda_parqueadero_id_celda
			FROM parqueo
			WHERE id_parqueo = ?
			""";

	/**
	 * Busca un parqueo por su llave primaria.
	 *
	 * @param id identificador del parqueo
	 * @return parqueo encontrado, o {@code null} si no existe
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 */
	public Parqueo buscar(int id) throws SQLException {
		try (Connection c = ConexionBD.obtenerConexion();
				PreparedStatement ps = c.prepareStatement(BUSCAR)) {
			ps.setInt(1, id);
			try (ResultSet rs = ps.executeQuery()) {
				return rs.next() ? mapear(rs) : null;
			}
		}
	}

	/**
	 * Consulta que cuenta los parqueos abiertos de una celda, sin contar el parqueo indicado (regla
	 * S-33: una celda tiene a lo sumo un parqueo abierto a la vez).
	 */
	private static final String CELDA_OCUPADA = """
			SELECT COUNT(*)
			FROM parqueo
			WHERE celda_parqueadero_id_celda = ?
			AND fecha_hora_salida IS NULL
			AND id_parqueo <> ?
			""";

	/**
	 * Indica si la celda tiene un parqueo abierto distinto del indicado (regla S-33).
	 *
	 * @param idCelda identificador de la celda de parqueadero
	 * @param excluirId identificador del registro que no se tiene en cuenta (el que se está
	 *         actualizando); {@code 0} al crear
	 * @return {@code true} si la celda está ocupada
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 */
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

	/**
	 * Consulta que cuenta los parqueos abiertos de un vehículo, sin contar el parqueo indicado (regla
	 * S-31: un vehículo no puede estar parqueado dos veces a la vez).
	 */
	private static final String VEHICULO_PARQUEADO = """
			SELECT COUNT(*)
			FROM parqueo
			WHERE vehiculo_placa = ?
			AND fecha_hora_salida IS NULL
			AND id_parqueo <> ?
			""";

	/**
	 * Indica si el vehículo tiene un parqueo abierto distinto del indicado (regla S-31).
	 *
	 * @param placa placa del vehículo
	 * @param excluirId identificador del registro que no se tiene en cuenta (el que se está
	 *         actualizando); {@code 0} al crear
	 * @return {@code true} si el vehículo ya está parqueado
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 */
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

	/** Sentencia que inserta un registro en la tabla {@code parqueo}. */
	private static final String INSERTAR = """
			INSERT INTO parqueo (fecha_hora_entrada, fecha_hora_salida, vehiculo_placa, celda_parqueadero_id_celda)
			VALUES (CAST(? AS TIMESTAMP), CAST(? AS TIMESTAMP), ?, ?)
			RETURNING id_parqueo
			""";

	/**
	 * Inserta un parqueo en la base de datos.
	 *
	 * @param p parqueo que se va a insertaro
	 * @return identificador generado por la base de datos
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 */
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

	/** Sentencia que actualiza un registro de la tabla {@code parqueo}. */
	private static final String ACTUALIZAR = """
			UPDATE parqueo
			SET fecha_hora_entrada = CAST(? AS TIMESTAMP),
			    fecha_hora_salida = CAST(? AS TIMESTAMP)
			WHERE id_parqueo = ?
			""";

	/**
	 * Actualiza el parqueo indicado por su llave primaria.
	 *
	 * @param p parqueo con su llave primaria y los datos nuevoso
	 * @return {@code true} si el registro existía y se actualizó
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 */
	public boolean actualizar(Parqueo p) throws SQLException {
		try (Connection c = ConexionBD.obtenerConexion();
				PreparedStatement ps = c.prepareStatement(ACTUALIZAR)) {
			ps.setString(1, p.getFechaHoraEntrada());
			ps.setString(2, p.getFechaHoraSalida());
			ps.setInt(3, p.getIdParqueo());
			return ps.executeUpdate() > 0;
		}
	}

	/** Sentencia que elimina un registro de la tabla {@code parqueo} por su llave primaria. */
	private static final String ELIMINAR = """
			DELETE FROM parqueo
			WHERE id_parqueo = ?
			""";

	/**
	 * Elimina el parqueo indicado por su llave primaria.
	 *
	 * @param id identificador del parqueo
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
	
}
