package co.edu.unbosque.dao;

import co.edu.unbosque.config.ConexionBD;
import co.edu.unbosque.model.Sancion;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a datos de las sanciones con JDBC.
 * <p>
 * Cada método abre su propia conexión con {@link co.edu.unbosque.config.ConexionBD} y usa
 * {@link java.sql.PreparedStatement} para enviar los parámetros de forma segura.
 */
public class SancionDAO {

	/** Consulta que lista todos los registros de la tabla {@code sancion}. */
	private static final String LISTAR = """
			SELECT id_sancion, 
				fecha_incidente, 
				fecha_notificacion, 
				tipo, 
				descripcion, 
				descargos, 
				estado_proceso,
			    valor, 
			    casa_numero, 
			    casa_manzana
			FROM sancion
			ORDER BY fecha_incidente DESC
			""";

	/** Consulta que busca un registro de la tabla {@code sancion} por su llave primaria. */
	private static final String BUSCAR_POR_ID = """
			SELECT id_sancion, 
					fecha_incidente, 
					fecha_notificacion, 
					tipo, 
					descripcion, 
					descargos, 
					estado_proceso,
			        valor, 
			        casa_numero, 
			        casa_manzana
			FROM sancion
			WHERE id_sancion = ?
			""";

	/** Sentencia que inserta un registro en la tabla {@code sancion}. */
	private static final String INSERTAR = """
			INSERT INTO sancion (fecha_incidente, fecha_notificacion, tipo, descripcion, descargos,
			                     estado_proceso, valor, casa_numero, casa_manzana)
			VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
			RETURNING id_sancion
			""";

	/**
	 * Sentencia que actualiza los descargos, el estado y el valor de una sanción, solo si sigue
	 * {@code EN_DESCARGOS}.
	 */
	private static final String ACTUALIZAR_PROCESO = """
			UPDATE sancion
			SET descargos = ?, 
				estado_proceso = ?, 
				valor = ?
			WHERE id_sancion = ? 
			AND estado_proceso = 'EN_DESCARGOS'
			""";

	/** Sentencia que elimina un registro de la tabla {@code sancion} por su llave primaria. */
	private static final String ELIMINAR = "DELETE FROM sancion WHERE id_sancion = ?";

	/** Consulta que indica si existe la casa con ese número y manzana. */
	private static final String EXISTE_CASA =
			"SELECT EXISTS (SELECT 1 FROM casa WHERE numero = ? AND manzana = ?)";

	/**
	 * Consulta las sanciones registradas.
	 *
	 * @return lista de sanciones; vacía si no hay registros
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 */
	public List<Sancion> listar() throws SQLException {
		List<Sancion> sanciones = new ArrayList<>();
		try (Connection c = ConexionBD.obtenerConexion();
		     PreparedStatement ps = c.prepareStatement(LISTAR);
		     ResultSet rs = ps.executeQuery()) {

			while (rs.next()) {
				sanciones.add(mapear(rs));
			}
		}
		return sanciones;
	}

	/**
	 * Busca una sanción por su llave primaria.
	 *
	 * @param id identificador de la sanción
	 * @return sanción encontrada, o {@code null} si no existe
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 */
	public Sancion buscarPorId(int id) throws SQLException {
		try (Connection c = ConexionBD.obtenerConexion();
		     PreparedStatement ps = c.prepareStatement(BUSCAR_POR_ID)) {
			ps.setInt(1, id);
			try (ResultSet rs = ps.executeQuery()) {
				return rs.next() ? mapear(rs) : null;
			}
		}
	}

	/**
	 * Inserta una sanción.
	 *
	 * @param s sanción con el tipo, la descripción, los descargos, el estado y la casa
	 * @param incidente fecha del incidente
	 * @param notificacion fecha de notificación
	 * @return identificador de la sanción generada
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 */
	public int insertar(Sancion s, LocalDate incidente, LocalDate notificacion) throws SQLException {
		try (Connection c = ConexionBD.obtenerConexion();
		     PreparedStatement ps = c.prepareStatement(INSERTAR)) {
			ps.setObject(1, incidente);
			ps.setObject(2, notificacion);
			ps.setString(3, s.getTipo());
			ps.setString(4, s.getDescripcion());
			ps.setString(5, s.getDescargos());
			ps.setString(6, s.getEstadoProceso());
			ps.setBigDecimal(7, s.getValor());
			ps.setInt(8, s.getCasaNumero());
			ps.setInt(9, s.getCasaManzana());
			try (ResultSet rs = ps.executeQuery()) {
				rs.next();
				return rs.getInt(1);
			}
		}
	}

	/**
	 * Actualiza los descargos, el estado y el valor de una sanción, solo si sigue
	 * {@code EN_DESCARGOS}.
	 *
	 * @param id identificador de la sanción
	 * @param descargos descargos que quedan guardados
	 * @param estado nuevo estado del proceso
	 * @param valor valor de la multa; {@code null} si no se confirma
	 * @return {@code true} si la sanción se actualizó
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 */
	public boolean actualizarProceso(int id, String descargos, String estado, BigDecimal valor)
			throws SQLException {
		try (Connection c = ConexionBD.obtenerConexion();
		     PreparedStatement ps = c.prepareStatement(ACTUALIZAR_PROCESO)) {
			ps.setString(1, descargos);
			ps.setString(2, estado);
			ps.setBigDecimal(3, valor);
			ps.setInt(4, id);
			return ps.executeUpdate() > 0;
		}
	}

	/**
	 * Elimina la sanción indicada por su llave primaria.
	 *
	 * @param id identificador de la sanción
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

	/**
	 * Indica si existe la casa.
	 *
	 * @param numero número de la casa
	 * @param manzana manzana de la casa
	 * @return {@code true} si la casa existe
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 */
	public boolean existeCasa(int numero, int manzana) throws SQLException {
		try (Connection c = ConexionBD.obtenerConexion();
		     PreparedStatement ps = c.prepareStatement(EXISTE_CASA)) {
			ps.setInt(1, numero);
			ps.setInt(2, manzana);
			try (ResultSet rs = ps.executeQuery()) {
				rs.next();
				return rs.getBoolean(1);
			}
		}
	}

	/**
	 * Convierte la fila actual del resultado en una sanción.
	 *
	 * @param rs resultado de la consulta, ubicado en la fila que se va a leer
	 * @return sanción leída
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 */
	private Sancion mapear(ResultSet rs) throws SQLException {
		return new Sancion(
				rs.getInt("id_sancion"),
				rs.getString("fecha_incidente"),
				rs.getString("fecha_notificacion"),
				rs.getString("tipo"),
				rs.getString("descripcion"),
				rs.getString("descargos"),
				rs.getString("estado_proceso"),
				rs.getBigDecimal("valor"),
				rs.getInt("casa_numero"),
				rs.getInt("casa_manzana")
		);
	}
}