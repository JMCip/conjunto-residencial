package co.edu.unbosque.dao;
import co.edu.unbosque.config.ConexionBD;
import co.edu.unbosque.model.CeldaParqueadero;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a datos de las celdas de parqueadero con JDBC.
 * <p>
 * Cada método abre su propia conexión con {@link co.edu.unbosque.config.ConexionBD} y usa
 * {@link java.sql.PreparedStatement} para enviar los parámetros de forma segura.
 */
public class CeldaParqueaderoDAO {
	
	/** Consulta que lista todos los registros de la tabla {@code celda_parqueadero}. */
	private static final String LISTAR = """
			SELECT id_celda, tipo, estado
			FROM celda_parqueadero
			ORDER BY id_celda
			""";
	
	/**
	 * Consulta las celdas de parqueadero registradas.
	 *
	 * @return lista de celdas de parqueadero; vacía si no hay registros
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 */
	public List<CeldaParqueadero> listar() throws SQLException {
		List<CeldaParqueadero> celdas = new ArrayList<>();
		try (	Connection c = ConexionBD.obtenerConexion();
				PreparedStatement ps = c.prepareStatement(LISTAR);
				ResultSet rs = ps.executeQuery()) {
			
			while (rs.next()) {
				celdas.add(mapear(rs));
			}
		}
		return celdas;
	}
	
	/**
	 * Convierte la fila actual del resultado en una celda de parqueadero.
	 *
	 * @param rs resultado de la consulta, ubicado en la fila que se va a leer
	 * @return celda de parqueadero leída
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 */
	private CeldaParqueadero mapear(ResultSet rs) throws SQLException {
		return new CeldaParqueadero(
				rs.getInt("id_celda"),
				rs.getString("tipo"),
				rs.getString("estado")
				);
	}
	
	/** Consulta que busca un registro de la tabla {@code celda_parqueadero} por su llave primaria. */
	private static final String BUSCAR = """
			SELECT id_celda, tipo, estado
			FROM celda_parqueadero
			WHERE id_celda = ?
			""";

	/**
	 * Busca una celda de parqueadero por su llave primaria.
	 *
	 * @param id identificador de la celda de parqueadero
	 * @return celda de parqueadero encontrada, o {@code null} si no existe
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 */
	public CeldaParqueadero buscar(int id) throws SQLException {
		try (Connection c = ConexionBD.obtenerConexion();
				PreparedStatement ps = c.prepareStatement(BUSCAR)) {
			ps.setInt(1, id);
			try (ResultSet rs = ps.executeQuery()) {
				return rs.next() ? mapear(rs) : null;
			}
		}
	}

	/** Sentencia que inserta un registro en la tabla {@code celda_parqueadero}. */
	private static final String INSERTAR = """
			INSERT INTO celda_parqueadero (tipo, estado)
			VALUES (?, ?)
			RETURNING id_celda
			""";

	/**
	 * Inserta una celda de parqueadero en la base de datos.
	 *
	 * @param c celda de parqueadero que se va a insertara
	 * @return identificador generado por la base de datos
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 */
	public int insertar(CeldaParqueadero c) throws SQLException {
		try (Connection con = ConexionBD.obtenerConexion();
				PreparedStatement ps = con.prepareStatement(INSERTAR)) {
			ps.setString(1, c.getTipo());
			ps.setString(2, c.getEstado());
			try (ResultSet rs = ps.executeQuery()) {
				rs.next();
				return rs.getInt(1);
			}
		}
	}

	/** Sentencia que actualiza un registro de la tabla {@code celda_parqueadero}. */
	private static final String ACTUALIZAR = """
			UPDATE celda_parqueadero
			SET tipo = ?,
			    estado = ?
			WHERE id_celda = ?
			""";

	/**
	 * Actualiza la celda de parqueadero indicada por su llave primaria.
	 *
	 * @param c celda de parqueadero con su llave primaria y los datos nuevosa
	 * @return {@code true} si el registro existía y se actualizó
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 */
	public boolean actualizar(CeldaParqueadero c) throws SQLException {
		try (Connection con = ConexionBD.obtenerConexion();
				PreparedStatement ps = con.prepareStatement(ACTUALIZAR)) {
			ps.setString(1, c.getTipo());
			ps.setString(2, c.getEstado());
			ps.setInt(3, c.getIdCelda());
			return ps.executeUpdate() > 0;
		}
	}

	/**
	 * Sentencia que elimina un registro de la tabla {@code celda_parqueadero} por su llave primaria.
	 */
	private static final String ELIMINAR = """
			DELETE FROM celda_parqueadero
			WHERE id_celda = ?
			""";

	/**
	 * Elimina la celda de parqueadero indicada por su llave primaria.
	 *
	 * @param id identificador de la celda de parqueadero
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
