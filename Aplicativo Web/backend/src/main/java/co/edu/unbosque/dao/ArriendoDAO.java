package co.edu.unbosque.dao;
import co.edu.unbosque.config.ConexionBD;
import co.edu.unbosque.model.Arriendo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a datos de los contratos de arriendo con JDBC.
 * <p>
 * Cada método abre su propia conexión con {@link co.edu.unbosque.config.ConexionBD} y usa
 * {@link java.sql.PreparedStatement} para enviar los parámetros de forma segura.
 */
public class ArriendoDAO {
	
	/** Consulta que lista todos los registros de la tabla {@code arriendo}. */
	private static final String LISTAR = """
			SELECT id_contrato, cuota_mensual, fecha_inicio, fecha_fin, estado_contrato,
			       arrendatario_tipo_documento, arrendatario_numero_documento, casa_numero, casa_manzana,
			       propietario_tipo_documento, propietario_numero_documento
			FROM arriendo
			ORDER BY id_contrato
			""";
	
	/**
	 * Consulta los contratos de arriendo registrados.
	 *
	 * @return lista de contratos de arriendo; vacía si no hay registros
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 */
	public List<Arriendo> listar() throws SQLException {
		List<Arriendo> arriendos = new ArrayList<>();
		try (	Connection c = ConexionBD.obtenerConexion();
				PreparedStatement ps = c.prepareStatement(LISTAR);
				ResultSet rs = ps.executeQuery()) {
			
			while (rs.next()) {
				arriendos.add(mapear(rs));
			}
		}
		return arriendos;
	}
	
	/**
	 * Convierte la fila actual del resultado en un contrato de arriendo.
	 *
	 * @param rs resultado de la consulta, ubicado en la fila que se va a leer
	 * @return contrato de arriendo leído
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 */
	private Arriendo mapear(ResultSet rs) throws SQLException {
		return new Arriendo(
				rs.getInt("id_contrato"),
				rs.getBigDecimal("cuota_mensual"),
				rs.getString("fecha_inicio"),
				rs.getString("fecha_fin"),
				rs.getString("estado_contrato"),
				rs.getString("arrendatario_tipo_documento"),
				rs.getString("arrendatario_numero_documento"),
				rs.getInt("casa_numero"),
				rs.getInt("casa_manzana"),
				rs.getString("propietario_tipo_documento"),
				rs.getString("propietario_numero_documento")
				);
	}
	
	/** Consulta que busca un registro de la tabla {@code arriendo} por su llave primaria. */
	private static final String BUSCAR = """
			SELECT id_contrato, cuota_mensual, fecha_inicio, fecha_fin, estado_contrato,
			       arrendatario_tipo_documento, arrendatario_numero_documento, casa_numero, casa_manzana,
			       propietario_tipo_documento, propietario_numero_documento
			FROM arriendo
			WHERE id_contrato = ?
			""";

	/**
	 * Busca un contrato de arriendo por su llave primaria.
	 *
	 * @param id identificador del contrato de arriendo
	 * @return contrato de arriendo encontrado, o {@code null} si no existe
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 */
	public Arriendo buscar(int id) throws SQLException {
		try (Connection c = ConexionBD.obtenerConexion();
				PreparedStatement ps = c.prepareStatement(BUSCAR)) {
			ps.setInt(1, id);
			try (ResultSet rs = ps.executeQuery()) {
				return rs.next() ? mapear(rs) : null;
			}
		}
	}

	/**
	 * Consulta que cuenta los contratos {@code VIGENTE} de una casa, sin contar el contrato indicado.
	 */
	private static final String EXISTE_VIGENTE = """
			SELECT COUNT(*)
			FROM arriendo
			WHERE casa_numero = ?
			AND casa_manzana = ?
			AND UPPER(estado_contrato) = 'VIGENTE'
			AND id_contrato <> ?
			""";

	/**
	 * Indica si la casa ya tiene un contrato {@code VIGENTE} distinto del indicado.
	 *
	 * @param casaNumero número de la casa
	 * @param casaManzana manzana de la casa
	 * @param excluirId identificador del registro que no se tiene en cuenta (el que se está
	 *         actualizando); {@code 0} al crear
	 * @return {@code true} si existe otro contrato vigente
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 */
	public boolean existeVigente(int casaNumero, int casaManzana, int excluirId) throws SQLException {
		try (Connection c = ConexionBD.obtenerConexion();
				PreparedStatement ps = c.prepareStatement(EXISTE_VIGENTE)) {
			ps.setInt(1, casaNumero);
			ps.setInt(2, casaManzana);
			ps.setInt(3, excluirId);
			try (ResultSet rs = ps.executeQuery()) {
				rs.next();
				return rs.getInt(1) > 0;
			}
		}
	}

	/**
	 * Consulta que cuenta las posesiones vigentes (sin fecha de venta) de una persona sobre una casa.
	 */
	private static final String ES_PROPIETARIO_VIGENTE = """
			SELECT COUNT(*)
			FROM posesion
			WHERE casa_numero = ?
			AND casa_manzana = ?
			AND propietario_tipo_documento = ?
			AND propietario_numero_documento = ?
			AND fecha_venta IS NULL
			""";

	/**
	 * Indica si la persona es propietaria vigente de la casa (tiene una posesión sin fecha de venta).
	 *
	 * @param casaNumero número de la casa
	 * @param casaManzana manzana de la casa
	 * @param tipoDoc tipo de documento de la persona
	 * @param numeroDoc número de documento de la persona
	 * @return {@code true} si es propietaria vigente
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 */
	public boolean esPropietarioVigente(int casaNumero, int casaManzana, String tipoDoc, String numeroDoc)
			throws SQLException {
		try (Connection c = ConexionBD.obtenerConexion();
				PreparedStatement ps = c.prepareStatement(ES_PROPIETARIO_VIGENTE)) {
			ps.setInt(1, casaNumero);
			ps.setInt(2, casaManzana);
			ps.setString(3, tipoDoc);
			ps.setString(4, numeroDoc);
			try (ResultSet rs = ps.executeQuery()) {
				rs.next();
				return rs.getInt(1) > 0;
			}
		}
	}

	/** Sentencia que inserta un registro en la tabla {@code arriendo}. */
	private static final String INSERTAR = """
			INSERT INTO arriendo (cuota_mensual, fecha_inicio, fecha_fin, estado_contrato,
			                      arrendatario_tipo_documento, arrendatario_numero_documento,
			                      casa_numero, casa_manzana,
			                      propietario_tipo_documento, propietario_numero_documento)
			VALUES (?, CAST(? AS DATE), CAST(? AS DATE), ?, ?, ?, ?, ?, ?, ?)
			RETURNING id_contrato
			""";

	/**
	 * Inserta un contrato de arriendo en la base de datos.
	 *
	 * @param a contrato de arriendo que se va a insertaro
	 * @return identificador generado por la base de datos
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 */
	public int insertar(Arriendo a) throws SQLException {
		try (Connection c = ConexionBD.obtenerConexion();
				PreparedStatement ps = c.prepareStatement(INSERTAR)) {
			ps.setBigDecimal(1, a.getCuotaMensual());
			ps.setString(2, a.getFechaInicio());
			ps.setString(3, a.getFechaFin());
			ps.setString(4, a.getEstadoContrato());
			ps.setString(5, a.getArrendatarioTipoDocumento());
			ps.setString(6, a.getArrendatarioNumeroDocumento());
			ps.setInt(7, a.getCasaNumero());
			ps.setInt(8, a.getCasaManzana());
			ps.setString(9, a.getPropietarioTipoDocumento());
			ps.setString(10, a.getPropietarioNumeroDocumento());
			try (ResultSet rs = ps.executeQuery()) {
				rs.next();
				return rs.getInt(1);
			}
		}
	}

	/** Sentencia que actualiza un registro de la tabla {@code arriendo}. */
	private static final String ACTUALIZAR = """
			UPDATE arriendo
			SET cuota_mensual = ?,
			    fecha_inicio = CAST(? AS DATE),
			    fecha_fin = CAST(? AS DATE),
			    estado_contrato = ?
			WHERE id_contrato = ?
			""";

	/**
	 * Actualiza el contrato de arriendo indicado por su llave primaria.
	 *
	 * @param a contrato de arriendo con su llave primaria y los datos nuevoso
	 * @return {@code true} si el registro existía y se actualizó
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 */
	public boolean actualizar(Arriendo a) throws SQLException {
		try (Connection c = ConexionBD.obtenerConexion();
				PreparedStatement ps = c.prepareStatement(ACTUALIZAR)) {
			ps.setBigDecimal(1, a.getCuotaMensual());
			ps.setString(2, a.getFechaInicio());
			ps.setString(3, a.getFechaFin());
			ps.setString(4, a.getEstadoContrato());
			ps.setInt(5, a.getIdContrato());
			return ps.executeUpdate() > 0;
		}
	}

	/** Sentencia que elimina un registro de la tabla {@code arriendo} por su llave primaria. */
	private static final String ELIMINAR = """
			DELETE FROM arriendo
			WHERE id_contrato = ?
			""";

	/**
	 * Elimina el contrato de arriendo indicado por su llave primaria.
	 *
	 * @param id identificador del contrato de arriendo
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
