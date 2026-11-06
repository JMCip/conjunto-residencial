package co.edu.unbosque.dao;

import co.edu.unbosque.config.ConexionBD;
import co.edu.unbosque.model.Casa;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a datos de las casas con JDBC.
 * <p>
 * Cada método abre su propia conexión con {@link co.edu.unbosque.config.ConexionBD} y usa
 * {@link java.sql.PreparedStatement} para enviar los parámetros de forma segura.
 */
public class CasaDao {

	/** Consulta que lista todos los registros de la tabla {@code casa}. */
	private static final String LISTAR = """
			SELECT numero,
			manzana,
			area,
			numero_habitaciones
			FROM casa
			ORDER BY manzana, numero
			""";

	/**
	 * Consulta las casas registradas.
	 *
	 * @return lista de casas; vacía si no hay registros
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 */
	public List<Casa> listar() throws SQLException {
		List<Casa> casas = new ArrayList<>();
		try (Connection con = ConexionBD.obtenerConexion();
				PreparedStatement ps = con.prepareStatement(LISTAR);
				ResultSet rs = ps.executeQuery()) {

			while (rs.next()) {
				casas.add(mapear(rs));
			}
		}
		return casas;
	}

	/**
	 * Convierte la fila actual del resultado en una casa.
	 *
	 * @param rs resultado de la consulta, ubicado en la fila que se va a leer
	 * @return casa leída
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 */
	private Casa mapear(ResultSet rs) throws SQLException {
		return new Casa(
				rs.getInt("numero"),
				rs.getInt("manzana"),
				rs.getBigDecimal("area"),
				rs.getObject("numero_habitaciones", Integer.class));
	}

	/** Sentencia que inserta un registro en la tabla {@code casa}. */
	private static final String INSERTAR = """
			INSERT INTO casa (numero,
			manzana,
			area,
			numero_habitaciones)
			VALUES (?, ?, ?, ?)
			""";

	/**
	 * Inserta una casa en la base de datos.
	 *
	 * @param c casa que se va a insertara
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 */
	public void insertar(Casa c) throws SQLException {
		try (Connection con = ConexionBD.obtenerConexion();
				PreparedStatement ps = con.prepareStatement(INSERTAR)) {
			ps.setInt(1, c.getNumero());
			ps.setInt(2, c.getManzana());
			ps.setBigDecimal(3, c.getArea());
			ps.setObject(4, c.getNumeroHabitaciones());
			ps.executeUpdate();
		}
	}

	/** Sentencia que actualiza un registro de la tabla {@code casa}. */
	private static final String ACTUALIZAR = """
			UPDATE casa
			SET area = ?,
			    numero_habitaciones = ?
			WHERE numero = ?
			AND manzana = ?
			""";

	/**
	 * Actualiza la casa indicada por su llave primaria.
	 *
	 * @param c casa con su llave primaria y los datos nuevosa
	 * @return {@code true} si el registro existía y se actualizó
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 */
	public boolean actualizar(Casa c) throws SQLException {
		try (Connection con = ConexionBD.obtenerConexion();
				PreparedStatement ps = con.prepareStatement(ACTUALIZAR)) {
			ps.setBigDecimal(1, c.getArea());
			ps.setObject(2, c.getNumeroHabitaciones());
			ps.setInt(3, c.getNumero());
			ps.setInt(4, c.getManzana());
			return ps.executeUpdate() > 0;
		}
	}

	/** Sentencia que elimina un registro de la tabla {@code casa} por su llave primaria. */
	private static final String ELIMINAR = """
			DELETE FROM casa
			WHERE numero = ?
			AND manzana = ?
			""";

	/**
	 * Elimina la casa indicada por su llave primaria.
	 *
	 * @param numero número de la casa
	 * @param manzana manzana de la casa
	 * @return {@code true} si el registro existía y se eliminó
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 */
	public boolean eliminar(int numero, int manzana) throws SQLException {
		try (Connection con = ConexionBD.obtenerConexion();
				PreparedStatement ps = con.prepareStatement(ELIMINAR)) {
			ps.setInt(1, numero);
			ps.setInt(2, manzana);
			return ps.executeUpdate() > 0;
		}
	}

}