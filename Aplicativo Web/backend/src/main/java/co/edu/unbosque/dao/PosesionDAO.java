package co.edu.unbosque.dao;
import co.edu.unbosque.config.ConexionBD;
import co.edu.unbosque.model.Posesion;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a datos de las posesiones con JDBC.
 * <p>
 * Cada método abre su propia conexión con {@link co.edu.unbosque.config.ConexionBD} y usa
 * {@link java.sql.PreparedStatement} para enviar los parámetros de forma segura.
 */
public class PosesionDAO {
	
	/** Consulta que lista todos los registros de la tabla {@code posesion}. */
	private static final String LISTAR = """
			SELECT id_posesion, porcentaje, fecha_compra, fecha_venta, casa_numero, casa_manzana,
			       propietario_tipo_documento, propietario_numero_documento
			FROM posesion
			ORDER BY casa_manzana, casa_numero, fecha_compra
			""";
	
	/**
	 * Consulta las posesiones registradas.
	 *
	 * @return lista de posesiones; vacía si no hay registros
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 */
	public List<Posesion> listar() throws SQLException {
		List<Posesion> posesiones = new ArrayList<>();
		try (	Connection c = ConexionBD.obtenerConexion();
				PreparedStatement ps = c.prepareStatement(LISTAR);
				ResultSet rs = ps.executeQuery()) {
			
			while (rs.next()) {
				posesiones.add(mapear(rs));
			}
		}
		return posesiones;
	}
	
	/**
	 * Convierte la fila actual del resultado en una posesión.
	 *
	 * @param rs resultado de la consulta, ubicado en la fila que se va a leer
	 * @return posesión leída
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 */
	private Posesion mapear(ResultSet rs) throws SQLException {
		return new Posesion(
				rs.getInt("id_posesion"),
				rs.getBigDecimal("porcentaje"),
				rs.getString("fecha_compra"),
				rs.getString("fecha_venta"),
				rs.getInt("casa_numero"),
				rs.getInt("casa_manzana"),
				rs.getString("propietario_tipo_documento"),
				rs.getString("propietario_numero_documento")
				);
	}
	
	/** Consulta que busca un registro de la tabla {@code posesion} por su llave primaria. */
	private static final String BUSCAR = """
			SELECT id_posesion, porcentaje, fecha_compra, fecha_venta, casa_numero, casa_manzana,
			       propietario_tipo_documento, propietario_numero_documento
			FROM posesion
			WHERE id_posesion = ?
			""";

	/**
	 * Busca una posesión por su llave primaria.
	 *
	 * @param id identificador de la posesión
	 * @return posesión encontrada, o {@code null} si no existe
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 */
	public Posesion buscar(int id) throws SQLException {
		try (Connection c = ConexionBD.obtenerConexion();
				PreparedStatement ps = c.prepareStatement(BUSCAR)) {
			ps.setInt(1, id);
			try (ResultSet rs = ps.executeQuery()) {
				return rs.next() ? mapear(rs) : null;
			}
		}
	}

	/**
	 * Consulta que suma los porcentajes de las posesiones vigentes de una casa, sin contar la posesión
	 * indicada.
	 */
	private static final String SUMA_VIGENTE = """
			SELECT COALESCE(SUM(porcentaje), 0)
			FROM posesion
			WHERE casa_numero = ?
			AND casa_manzana = ?
			AND fecha_venta IS NULL
			AND id_posesion <> ?
			""";

	/**
	 * Suma los porcentajes de las posesiones vigentes de la casa, sin contar la indicada.
	 *
	 * @param casaNumero número de la casa
	 * @param casaManzana manzana de la casa
	 * @param excluirId identificador del registro que no se tiene en cuenta (el que se está
	 *         actualizando); {@code 0} al crear
	 * @return suma de los porcentajes; {@code 0} si no hay posesiones vigentes
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 */
	public BigDecimal sumaPorcentajeVigente(int casaNumero, int casaManzana, int excluirId) throws SQLException {
		try (Connection c = ConexionBD.obtenerConexion();
				PreparedStatement ps = c.prepareStatement(SUMA_VIGENTE)) {
			ps.setInt(1, casaNumero);
			ps.setInt(2, casaManzana);
			ps.setInt(3, excluirId);
			try (ResultSet rs = ps.executeQuery()) {
				rs.next();
				return rs.getBigDecimal(1);
			}
		}
	}

	/** Sentencia que inserta un registro en la tabla {@code posesion}. */
	private static final String INSERTAR = """
			INSERT INTO posesion (porcentaje, fecha_compra, fecha_venta, casa_numero, casa_manzana,
			                      propietario_tipo_documento, propietario_numero_documento)
			VALUES (?, CAST(? AS DATE), CAST(? AS DATE), ?, ?, ?, ?)
			RETURNING id_posesion
			""";

	/**
	 * Inserta una posesión en la base de datos.
	 *
	 * @param p posesión que se va a insertara
	 * @return identificador generado por la base de datos
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 */
	public int insertar(Posesion p) throws SQLException {
		try (Connection c = ConexionBD.obtenerConexion();
				PreparedStatement ps = c.prepareStatement(INSERTAR)) {
			ps.setBigDecimal(1, p.getPorcentaje());
			ps.setString(2, p.getFechaCompra());
			ps.setString(3, p.getFechaVenta());
			ps.setInt(4, p.getCasaNumero());
			ps.setInt(5, p.getCasaManzana());
			ps.setString(6, p.getPropietarioTipoDocumento());
			ps.setString(7, p.getPropietarioNumeroDocumento());
			try (ResultSet rs = ps.executeQuery()) {
				rs.next();
				return rs.getInt(1);
			}
		}
	}

	/** Sentencia que actualiza un registro de la tabla {@code posesion}. */
	private static final String ACTUALIZAR = """
			UPDATE posesion
			SET porcentaje = ?,
			    fecha_compra = CAST(? AS DATE),
			    fecha_venta = CAST(? AS DATE)
			WHERE id_posesion = ?
			""";

	/**
	 * Actualiza la posesión indicada por su llave primaria.
	 *
	 * @param p posesión con su llave primaria y los datos nuevosa
	 * @return {@code true} si el registro existía y se actualizó
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 */
	public boolean actualizar(Posesion p) throws SQLException {
		try (Connection c = ConexionBD.obtenerConexion();
				PreparedStatement ps = c.prepareStatement(ACTUALIZAR)) {
			ps.setBigDecimal(1, p.getPorcentaje());
			ps.setString(2, p.getFechaCompra());
			ps.setString(3, p.getFechaVenta());
			ps.setInt(4, p.getIdPosesion());
			return ps.executeUpdate() > 0;
		}
	}

	/** Sentencia que elimina un registro de la tabla {@code posesion} por su llave primaria. */
	private static final String ELIMINAR = """
			DELETE FROM posesion
			WHERE id_posesion = ?
			""";

	/**
	 * Elimina la posesión indicada por su llave primaria.
	 *
	 * @param id identificador de la posesión
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
