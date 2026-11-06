package co.edu.unbosque.dao;
import co.edu.unbosque.config.ConexionBD;
import co.edu.unbosque.model.Residencia;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a datos de las residencias con JDBC.
 * <p>
 * Cada método abre su propia conexión con {@link co.edu.unbosque.config.ConexionBD} y usa
 * {@link java.sql.PreparedStatement} para enviar los parámetros de forma segura.
 */
public class ResidenciaDAO {
	
	/** Consulta que lista todos los registros de la tabla {@code residencia}. */
	private static final String LISTAR = """
			SELECT id_residencia, fecha_ingreso, fecha_salida, residente_tipo_documento, residente_numero_documento,
			       casa_numero, casa_manzana
			FROM residencia
			ORDER BY casa_manzana, casa_numero, fecha_ingreso
			""";
	
	/**
	 * Consulta las residencias registradas.
	 *
	 * @return lista de residencias; vacía si no hay registros
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 */
	public List<Residencia> listar() throws SQLException {
		List<Residencia> residencias = new ArrayList<>();
		try (	Connection c = ConexionBD.obtenerConexion();
				PreparedStatement ps = c.prepareStatement(LISTAR);
				ResultSet rs = ps.executeQuery()) {
			
			while (rs.next()) {
				residencias.add(mapear(rs));
			}
		}
		return residencias;
	}
	
	/**
	 * Convierte la fila actual del resultado en una residencia.
	 *
	 * @param rs resultado de la consulta, ubicado en la fila que se va a leer
	 * @return residencia leída
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 */
	private Residencia mapear(ResultSet rs) throws SQLException {
		return new Residencia(
				rs.getInt("id_residencia"),
				rs.getString("fecha_ingreso"),
				rs.getString("fecha_salida"),
				rs.getString("residente_tipo_documento"),
				rs.getString("residente_numero_documento"),
				rs.getInt("casa_numero"),
				rs.getInt("casa_manzana")
				);
	}
	
	/** Consulta que busca un registro de la tabla {@code residencia} por su llave primaria. */
	private static final String BUSCAR = """
			SELECT id_residencia, fecha_ingreso, fecha_salida, residente_tipo_documento, residente_numero_documento,
			       casa_numero, casa_manzana
			FROM residencia
			WHERE id_residencia = ?
			""";

	/**
	 * Busca una residencia por su llave primaria.
	 *
	 * @param id identificador de la residencia
	 * @return residencia encontrada, o {@code null} si no existe
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 */
	public Residencia buscar(int id) throws SQLException {
		try (Connection c = ConexionBD.obtenerConexion();
				PreparedStatement ps = c.prepareStatement(BUSCAR)) {
			ps.setInt(1, id);
			try (ResultSet rs = ps.executeQuery()) {
				return rs.next() ? mapear(rs) : null;
			}
		}
	}

	/**
	 * Consulta que cuenta las residencias vigentes de una persona en una casa, sin contar la
	 * residencia indicada.
	 */
	private static final String EXISTE_VIGENTE = """
			SELECT COUNT(*)
			FROM residencia
			WHERE residente_tipo_documento = ?
			AND residente_numero_documento = ?
			AND casa_numero = ?
			AND casa_manzana = ?
			AND fecha_salida IS NULL
			AND id_residencia <> ?
			""";

	/**
	 * Indica si la persona ya tiene una residencia vigente en la casa, distinta de la indicada.
	 *
	 * @param tipoDoc tipo de documento de la persona
	 * @param numeroDoc número de documento de la persona
	 * @param casaNumero número de la casa
	 * @param casaManzana manzana de la casa
	 * @param excluirId identificador del registro que no se tiene en cuenta (el que se está
	 *         actualizando); {@code 0} al crear
	 * @return {@code true} si ya es residente vigente
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 */
	public boolean existeVigente(String tipoDoc, String numeroDoc, int casaNumero, int casaManzana, int excluirId)
			throws SQLException {
		try (Connection c = ConexionBD.obtenerConexion();
				PreparedStatement ps = c.prepareStatement(EXISTE_VIGENTE)) {
			ps.setString(1, tipoDoc);
			ps.setString(2, numeroDoc);
			ps.setInt(3, casaNumero);
			ps.setInt(4, casaManzana);
			ps.setInt(5, excluirId);
			try (ResultSet rs = ps.executeQuery()) {
				rs.next();
				return rs.getInt(1) > 0;
			}
		}
	}

	/** Sentencia que inserta un registro en la tabla {@code residencia}. */
	private static final String INSERTAR = """
			INSERT INTO residencia (fecha_ingreso, fecha_salida, residente_tipo_documento, residente_numero_documento,
			                        casa_numero, casa_manzana)
			VALUES (CAST(? AS DATE), CAST(? AS DATE), ?, ?, ?, ?)
			RETURNING id_residencia
			""";

	/**
	 * Inserta una residencia en la base de datos.
	 *
	 * @param r residencia que se va a insertara
	 * @return identificador generado por la base de datos
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 */
	public int insertar(Residencia r) throws SQLException {
		try (Connection c = ConexionBD.obtenerConexion();
				PreparedStatement ps = c.prepareStatement(INSERTAR)) {
			ps.setString(1, r.getFechaIngreso());
			ps.setString(2, r.getFechaSalida());
			ps.setString(3, r.getResidenteTipoDocumento());
			ps.setString(4, r.getResidenteNumeroDocumento());
			ps.setInt(5, r.getCasaNumero());
			ps.setInt(6, r.getCasaManzana());
			try (ResultSet rs = ps.executeQuery()) {
				rs.next();
				return rs.getInt(1);
			}
		}
	}

	/** Sentencia que actualiza un registro de la tabla {@code residencia}. */
	private static final String ACTUALIZAR = """
			UPDATE residencia
			SET fecha_ingreso = CAST(? AS DATE),
			    fecha_salida = CAST(? AS DATE)
			WHERE id_residencia = ?
			""";

	/**
	 * Actualiza la residencia indicada por su llave primaria.
	 *
	 * @param r residencia con su llave primaria y los datos nuevosa
	 * @return {@code true} si el registro existía y se actualizó
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 */
	public boolean actualizar(Residencia r) throws SQLException {
		try (Connection c = ConexionBD.obtenerConexion();
				PreparedStatement ps = c.prepareStatement(ACTUALIZAR)) {
			ps.setString(1, r.getFechaIngreso());
			ps.setString(2, r.getFechaSalida());
			ps.setInt(3, r.getIdResidencia());
			return ps.executeUpdate() > 0;
		}
	}

	/** Sentencia que elimina un registro de la tabla {@code residencia} por su llave primaria. */
	private static final String ELIMINAR = """
			DELETE FROM residencia
			WHERE id_residencia = ?
			""";

	/**
	 * Elimina la residencia indicada por su llave primaria.
	 *
	 * @param id identificador de la residencia
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
