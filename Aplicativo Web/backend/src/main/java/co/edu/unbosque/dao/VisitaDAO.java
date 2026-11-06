package co.edu.unbosque.dao;
import co.edu.unbosque.config.ConexionBD;
import co.edu.unbosque.model.Visita;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a datos de las visitas con JDBC.
 * <p>
 * Cada método abre su propia conexión con {@link co.edu.unbosque.config.ConexionBD} y usa
 * {@link java.sql.PreparedStatement} para enviar los parámetros de forma segura.
 */
public class VisitaDAO {
	
	/** Consulta que lista todos los registros de la tabla {@code visita}. */
	private static final String LISTAR = """
			SELECT id_visita, fecha_entrada, fecha_salida, visitante_tipo_documento, visitante_numero_documento,
			       casa_numero, casa_manzana
			FROM visita
			ORDER BY fecha_entrada DESC, id_visita DESC
			""";
	
	/**
	 * Consulta las visitas registradas.
	 *
	 * @return lista de visitas; vacía si no hay registros
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 */
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
	
	/**
	 * Convierte la fila actual del resultado en una visita.
	 *
	 * @param rs resultado de la consulta, ubicado en la fila que se va a leer
	 * @return visita leída
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 */
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
	
	/** Consulta que busca un registro de la tabla {@code visita} por su llave primaria. */
	private static final String BUSCAR = """
			SELECT id_visita, fecha_entrada, fecha_salida, visitante_tipo_documento, visitante_numero_documento,
			       casa_numero, casa_manzana
			FROM visita
			WHERE id_visita = ?
			""";

	/**
	 * Busca una visita por su llave primaria.
	 *
	 * @param id identificador de la visita
	 * @return visita encontrada, o {@code null} si no existe
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 */
	public Visita buscar(int id) throws SQLException {
		try (Connection c = ConexionBD.obtenerConexion();
				PreparedStatement ps = c.prepareStatement(BUSCAR)) {
			ps.setInt(1, id);
			try (ResultSet rs = ps.executeQuery()) {
				return rs.next() ? mapear(rs) : null;
			}
		}
	}

	/** Sentencia que inserta un registro en la tabla {@code visita}. */
	private static final String INSERTAR = """
			INSERT INTO visita (fecha_entrada, fecha_salida, visitante_tipo_documento, visitante_numero_documento,
			                    casa_numero, casa_manzana)
			VALUES (CAST(? AS DATE), CAST(? AS DATE), ?, ?, ?, ?)
			RETURNING id_visita
			""";

	/**
	 * Inserta una visita en la base de datos.
	 *
	 * @param v visita que se va a insertara
	 * @return identificador generado por la base de datos
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 */
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

	/** Sentencia que actualiza un registro de la tabla {@code visita}. */
	private static final String ACTUALIZAR = """
			UPDATE visita
			SET fecha_entrada = CAST(? AS DATE),
			    fecha_salida = CAST(? AS DATE)
			WHERE id_visita = ?
			""";

	/**
	 * Actualiza la visita indicada por su llave primaria.
	 *
	 * @param v visita con su llave primaria y los datos nuevosa
	 * @return {@code true} si el registro existía y se actualizó
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 */
	public boolean actualizar(Visita v) throws SQLException {
		try (Connection c = ConexionBD.obtenerConexion();
				PreparedStatement ps = c.prepareStatement(ACTUALIZAR)) {
			ps.setString(1, v.getFechaEntrada());
			ps.setString(2, v.getFechaSalida());
			ps.setInt(3, v.getIdVisita());
			return ps.executeUpdate() > 0;
		}
	}

	/** Sentencia que elimina un registro de la tabla {@code visita} por su llave primaria. */
	private static final String ELIMINAR = """
			DELETE FROM visita
			WHERE id_visita = ?
			""";

	/**
	 * Elimina la visita indicada por su llave primaria.
	 *
	 * @param id identificador de la visita
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
