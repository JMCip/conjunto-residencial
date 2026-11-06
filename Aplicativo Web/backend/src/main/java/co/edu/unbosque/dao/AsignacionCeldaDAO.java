package co.edu.unbosque.dao;
import co.edu.unbosque.config.ConexionBD;
import co.edu.unbosque.model.AsignacionCelda;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a datos de las asignaciones de celda con JDBC.
 * <p>
 * Cada método abre su propia conexión con {@link co.edu.unbosque.config.ConexionBD} y usa
 * {@link java.sql.PreparedStatement} para enviar los parámetros de forma segura.
 */
public class AsignacionCeldaDAO {
	
	/** Consulta que lista todos los registros de la tabla {@code asignacion_celda}. */
	private static final String LISTAR = """
			SELECT id_asignacion, fecha_inicio, fecha_fin, casa_numero, casa_manzana, celda_parqueadero_id_celda
			FROM asignacion_celda
			ORDER BY id_asignacion
			""";
	
	/**
	 * Consulta las asignaciones de celda registradas.
	 *
	 * @return lista de asignaciones de celda; vacía si no hay registros
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 */
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
	
	/**
	 * Convierte la fila actual del resultado en una asignación de celda.
	 *
	 * @param rs resultado de la consulta, ubicado en la fila que se va a leer
	 * @return asignación de celda leída
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 */
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
	
	/** Consulta que busca un registro de la tabla {@code asignacion_celda} por su llave primaria. */
	private static final String BUSCAR = """
			SELECT id_asignacion, fecha_inicio, fecha_fin, casa_numero, casa_manzana, celda_parqueadero_id_celda
			FROM asignacion_celda
			WHERE id_asignacion = ?
			""";

	/**
	 * Busca una asignación de celda por su llave primaria.
	 *
	 * @param id identificador de la asignación de celda
	 * @return asignación de celda encontrada, o {@code null} si no existe
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 */
	public AsignacionCelda buscar(int id) throws SQLException {
		try (Connection c = ConexionBD.obtenerConexion();
				PreparedStatement ps = c.prepareStatement(BUSCAR)) {
			ps.setInt(1, id);
			try (ResultSet rs = ps.executeQuery()) {
				return rs.next() ? mapear(rs) : null;
			}
		}
	}

	/**
	 * Consulta que cuenta las asignaciones vigentes (sin fecha de fin) de una celda, sin contar la
	 * asignación indicada (regla S-33: una celda tiene a lo sumo una asignación vigente).
	 */
	private static final String CELDA_ASIGNADA = """
			SELECT COUNT(*)
			FROM asignacion_celda
			WHERE celda_parqueadero_id_celda = ?
			AND fecha_fin IS NULL
			AND id_asignacion <> ?
			""";

	/**
	 * Indica si la celda ya tiene una asignación vigente distinta de la indicada (regla S-33).
	 *
	 * @param idCelda identificador de la celda de parqueadero
	 * @param excluirId identificador del registro que no se tiene en cuenta (el que se está
	 *         actualizando); {@code 0} al crear
	 * @return {@code true} si la celda ya está asignada
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 */
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

	/** Sentencia que inserta un registro en la tabla {@code asignacion_celda}. */
	private static final String INSERTAR = """
			INSERT INTO asignacion_celda (fecha_inicio, fecha_fin, casa_numero, casa_manzana, celda_parqueadero_id_celda)
			VALUES (CAST(? AS DATE), CAST(? AS DATE), ?, ?, ?)
			RETURNING id_asignacion
			""";

	/**
	 * Inserta una asignación de celda en la base de datos.
	 *
	 * @param a asignación de celda que se va a insertara
	 * @return identificador generado por la base de datos
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 */
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

	/** Sentencia que actualiza un registro de la tabla {@code asignacion_celda}. */
	private static final String ACTUALIZAR = """
			UPDATE asignacion_celda
			SET fecha_inicio = CAST(? AS DATE),
			    fecha_fin = CAST(? AS DATE)
			WHERE id_asignacion = ?
			""";

	/**
	 * Actualiza la asignación de celda indicada por su llave primaria.
	 *
	 * @param a asignación de celda con su llave primaria y los datos nuevosa
	 * @return {@code true} si el registro existía y se actualizó
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 */
	public boolean actualizar(AsignacionCelda a) throws SQLException {
		try (Connection c = ConexionBD.obtenerConexion();
				PreparedStatement ps = c.prepareStatement(ACTUALIZAR)) {
			ps.setString(1, a.getFechaInicio());
			ps.setString(2, a.getFechaFin());
			ps.setInt(3, a.getIdAsignacion());
			return ps.executeUpdate() > 0;
		}
	}

	/**
	 * Sentencia que elimina un registro de la tabla {@code asignacion_celda} por su llave primaria.
	 */
	private static final String ELIMINAR = """
			DELETE FROM asignacion_celda
			WHERE id_asignacion = ?
			""";

	/**
	 * Elimina la asignación de celda indicada por su llave primaria.
	 *
	 * @param id identificador de la asignación de celda
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
