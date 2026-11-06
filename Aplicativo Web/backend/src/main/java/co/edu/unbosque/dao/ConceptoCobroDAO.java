package co.edu.unbosque.dao;

import co.edu.unbosque.config.ConexionBD;
import co.edu.unbosque.model.ConceptoCobro;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a datos de los conceptos de cobro con JDBC.
 * <p>
 * Cada método abre su propia conexión con {@link co.edu.unbosque.config.ConexionBD} y usa
 * {@link java.sql.PreparedStatement} para enviar los parámetros de forma segura.
 */
public class ConceptoCobroDAO {

	/** Consulta que lista todos los registros de la tabla {@code concepto_cobro}. */
	private static final String LISTAR = """
			SELECT id_concepto, 
				   nombre, 
				   descripcion, 
				   valor_base
			FROM concepto_cobro
			ORDER BY id_concepto
			""";

	/** Consulta que busca un registro de la tabla {@code concepto_cobro} por su llave primaria. */
	private static final String BUSCAR_POR_ID = """
			SELECT id_concepto, 
				   nombre, 
				   descripcion, 
				   valor_base
			FROM concepto_cobro
			WHERE id_concepto = ?
			""";

	/** Sentencia que inserta un registro en la tabla {@code concepto_cobro}. */
	private static final String INSERTAR = """
			INSERT INTO concepto_cobro (nombre, descripcion, valor_base)
			VALUES (?, ?, ?)
			RETURNING id_concepto
			""";

	/** Sentencia que actualiza un registro de la tabla {@code concepto_cobro}. */
	private static final String ACTUALIZAR = """
			UPDATE concepto_cobro
			SET nombre = ?, descripcion = ?, valor_base = ?
			WHERE id_concepto = ?
			""";

	/** Sentencia que elimina un registro de la tabla {@code concepto_cobro} por su llave primaria. */
	private static final String ELIMINAR = "DELETE FROM concepto_cobro WHERE id_concepto = ?";

	/**
	 * Consulta que indica si otro concepto ya usa ese nombre, sin distinguir mayúsculas ni espacios al
	 * inicio o al final.
	 */
	private static final String EXISTE_NOMBRE = """
			SELECT EXISTS (SELECT 1 FROM concepto_cobro
			               WHERE LOWER(TRIM(nombre)) = LOWER(TRIM(?)) AND id_concepto <> ?)
			""";

	/**
	 * Consulta los conceptos de cobro registrados.
	 *
	 * @return lista de conceptos de cobro; vacía si no hay registros
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 */
	public List<ConceptoCobro> listar() throws SQLException {
		List<ConceptoCobro> conceptos = new ArrayList<>();
		try (Connection c = ConexionBD.obtenerConexion();
		     PreparedStatement ps = c.prepareStatement(LISTAR);
		     ResultSet rs = ps.executeQuery()) {

			while (rs.next()) {
				conceptos.add(mapear(rs));
			}
		}
		return conceptos;
	}

	/**
	 * Busca un concepto de cobro por su llave primaria.
	 *
	 * @param id identificador del concepto de cobro
	 * @return concepto de cobro encontrado, o {@code null} si no existe
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 */
	public ConceptoCobro buscarPorId(int id) throws SQLException {
		try (Connection c = ConexionBD.obtenerConexion();
		     PreparedStatement ps = c.prepareStatement(BUSCAR_POR_ID)) {
			ps.setInt(1, id);
			try (ResultSet rs = ps.executeQuery()) {
				return rs.next() ? mapear(rs) : null;
			}
		}
	}

	/**
	 * Inserta un concepto de cobro.
	 *
	 * @param nombre nombre del concepto de cobro
	 * @param descripcion descripción del concepto de cobro
	 * @param valorBase valor base del concepto de cobro
	 * @return identificador del concepto generado
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 */
	public int insertar(String nombre, String descripcion, BigDecimal valorBase) throws SQLException {
		try (Connection c = ConexionBD.obtenerConexion();
		     PreparedStatement ps = c.prepareStatement(INSERTAR)) {
			ps.setString(1, nombre);
			ps.setString(2, descripcion);
			ps.setBigDecimal(3, valorBase);
			try (ResultSet rs = ps.executeQuery()) {
				rs.next();
				return rs.getInt(1);
			}
		}
	}

	/**
	 * Actualiza el nombre, la descripción y el valor base de un concepto.
	 *
	 * @param id identificador del concepto
	 * @param nombre nombre del concepto de cobro
	 * @param descripcion descripción del concepto de cobro
	 * @param valorBase valor base del concepto de cobro
	 * @return {@code true} si el concepto existía y se actualizó
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 */
	public boolean actualizar(int id, String nombre, String descripcion, BigDecimal valorBase)
			throws SQLException {
		try (Connection c = ConexionBD.obtenerConexion();
		     PreparedStatement ps = c.prepareStatement(ACTUALIZAR)) {
			ps.setString(1, nombre);
			ps.setString(2, descripcion);
			ps.setBigDecimal(3, valorBase);
			ps.setInt(4, id);
			return ps.executeUpdate() > 0;
		}
	}

	/**
	 * Elimina el concepto de cobro indicado por su llave primaria.
	 *
	 * @param id identificador del concepto de cobro
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
	 * Indica si otro concepto ya usa ese nombre, sin distinguir mayúsculas ni espacios al inicio o al
	 * final.
	 *
	 * @param nombre nombre del concepto de cobro
	 * @param excluirId identificador del registro que no se tiene en cuenta (el que se está
	 *         actualizando); {@code 0} al crear
	 * @return {@code true} si el nombre ya está en uso
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 */
	public boolean existeNombre(String nombre, int excluirId) throws SQLException {
		try (Connection c = ConexionBD.obtenerConexion();
		     PreparedStatement ps = c.prepareStatement(EXISTE_NOMBRE)) {
			ps.setString(1, nombre);
			ps.setInt(2, excluirId);
			try (ResultSet rs = ps.executeQuery()) {
				rs.next();
				return rs.getBoolean(1);
			}
		}
	}

	/**
	 * Convierte la fila actual del resultado en un concepto de cobro.
	 *
	 * @param rs resultado de la consulta, ubicado en la fila que se va a leer
	 * @return concepto de cobro leído
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 */
	private ConceptoCobro mapear(ResultSet rs) throws SQLException {
		return new ConceptoCobro(
				rs.getInt("id_concepto"),
				rs.getString("nombre"),
				rs.getString("descripcion"),
				rs.getBigDecimal("valor_base")
		);
	}
}