package co.edu.unbosque.dao;

import co.edu.unbosque.config.ConexionBD;
import co.edu.unbosque.model.ConceptoCobro;
import co.edu.unbosque.model.DetalleFactura;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a datos de las líneas de factura con JDBC.
 * <p>
 * Cada método abre su propia conexión con {@link co.edu.unbosque.config.ConexionBD} y usa
 * {@link java.sql.PreparedStatement} para enviar los parámetros de forma segura.
 */
public class DetalleFacturaDAO {

	/**
	 * Datos del registro que origina el cobro de una línea de factura (sanción, reserva o asignación).
	 *
	 * @param casaNumero número de la casa a la que pertenece el origen
	 * @param casaManzana manzana de la casa a la que pertenece el origen
	 * @param estado estado del origen (de la sanción o de la reserva; {@code ASIGNADA} para una
	 *         asignación)
	 * @param valor valor que se cobra; {@code null} para una asignación, que se cobra con el valor
	 *         base del concepto
	 */
	public record Origen(int casaNumero, int casaManzana, String estado, BigDecimal valor) {
	}

	/** Consulta que lista todos los registros de la tabla {@code detalle_factura}. */
	private static final String LISTAR = """
			SELECT factura_numero_factura, 
				   numero_linea, 
				   concepto_cobro_id_concepto, 
				   valor,
			       asignacion_celda_id_asignacion, 
			       reserva_salon_id_reserva, 
			       sancion_id_sancion
			FROM detalle_factura
			ORDER BY factura_numero_factura, numero_linea
			""";

	/** Consulta que busca una línea por número de factura y número de línea. */
	private static final String BUSCAR = """
			SELECT factura_numero_factura,
			 	   numero_linea, 
			 	   concepto_cobro_id_concepto, 
			 	   valor,
			       asignacion_celda_id_asignacion, 
			       reserva_salon_id_reserva, 
			       sancion_id_sancion
			FROM detalle_factura
			WHERE factura_numero_factura = ? AND numero_linea = ?
			""";

	/**
	 * Sentencia que inserta una línea con el siguiente número de línea libre de la factura y devuelve
	 * ese número.
	 */
	private static final String INSERTAR = """
			INSERT INTO detalle_factura (factura_numero_factura, numero_linea, valor,
			       concepto_cobro_id_concepto, asignacion_celda_id_asignacion,
			       reserva_salon_id_reserva, sancion_id_sancion)
			SELECT ?, COALESCE(MAX(numero_linea), 0) + 1, ?, ?, ?, ?, ?
			FROM detalle_factura
			WHERE factura_numero_factura = ?
			RETURNING numero_linea
			""";

	/** Sentencia que cambia el valor de una línea. */
	private static final String ACTUALIZAR_VALOR = """
			UPDATE detalle_factura SET valor = ?
			WHERE factura_numero_factura = ? AND numero_linea = ?
			""";

	/** Sentencia que elimina una línea por número de factura y número de línea. */
	private static final String ELIMINAR = """
			DELETE FROM detalle_factura
			WHERE factura_numero_factura = ? AND numero_linea = ?
			""";

	/** Consulta que busca un concepto de cobro por su identificador. */
	private static final String BUSCAR_CONCEPTO = """
			SELECT id_concepto, nombre, descripcion, valor_base
			FROM concepto_cobro WHERE id_concepto = ?
			""";

	/** Consulta que obtiene la casa, el estado del proceso y el valor de una sanción. */
	private static final String BUSCAR_SANCION = """
			SELECT casa_numero, casa_manzana, estado_proceso, valor
			FROM sancion WHERE id_sancion = ?
			""";

	/** Consulta que indica si la factura ya tiene una línea con ese concepto. */
	private static final String CONCEPTO_EN_FACTURA = """
		    SELECT EXISTS (
		        SELECT 1
		        FROM detalle_factura
		        WHERE concepto_cobro_id_concepto = ?
		          AND factura_numero_factura = ?
		    )
		    """;

	/**
	 * Consulta que obtiene la casa, el estado y el valor (horas por valor por hora) de una reserva.
	 */
	private static final String BUSCAR_RESERVA = """
			SELECT casa_numero, casa_manzana, estado,
			       ROUND(EXTRACT(EPOCH FROM (hora_final - hora_inicio)) / 3600 * valor_por_hora, 2) AS valor
			FROM reserva_salon WHERE id_reserva = ?
			""";

	/** Consulta que obtiene la casa de una asignación de celda (sin valor propio). */
	private static final String BUSCAR_ASIGNACION = """
			SELECT casa_numero, casa_manzana, 'ASIGNADA' AS estado, NULL::numeric AS valor
			FROM asignacion_celda WHERE id_asignacion = ?
			""";

	/** Consulta que indica si la sanción ya está en alguna línea de factura. */
	private static final String SANCION_FACTURADA =
			"SELECT EXISTS (SELECT 1 FROM detalle_factura WHERE sancion_id_sancion = ?)";

	/** Consulta que indica si la reserva ya está en alguna línea de factura. */
	private static final String RESERVA_FACTURADA =
			"SELECT EXISTS (SELECT 1 FROM detalle_factura WHERE reserva_salon_id_reserva = ?)";

	/** Consulta que indica si la asignación ya está cobrada en la factura indicada. */
	private static final String ASIGNACION_EN_FACTURA = """
			SELECT EXISTS (SELECT 1 FROM detalle_factura
			               WHERE asignacion_celda_id_asignacion = ? AND factura_numero_factura = ?)
			""";

	/**
	 * Consulta las líneas de factura registradas.
	 *
	 * @return lista de líneas de factura; vacía si no hay registros
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 */
	public List<DetalleFactura> listar() throws SQLException {
		List<DetalleFactura> detalles = new ArrayList<>();
		try (Connection c = ConexionBD.obtenerConexion();
		     PreparedStatement ps = c.prepareStatement(LISTAR);
		     ResultSet rs = ps.executeQuery()) {

			while (rs.next()) {
				detalles.add(mapear(rs));
			}
		}
		return detalles;
	}

	/**
	 * Busca una línea de factura por su llave primaria.
	 *
	 * @param factura número de la factura
	 * @param linea número de la línea dentro de la factura
	 * @return línea de factura encontrada, o {@code null} si no existe
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 */
	public DetalleFactura buscar(int factura, int linea) throws SQLException {
		try (Connection c = ConexionBD.obtenerConexion();
		     PreparedStatement ps = c.prepareStatement(BUSCAR)) {
			ps.setInt(1, factura);
			ps.setInt(2, linea);
			try (ResultSet rs = ps.executeQuery()) {
				return rs.next() ? mapear(rs) : null;
			}
		}
	}

	/**
	 * Inserta una línea en la factura con el siguiente número de línea libre.
	 *
	 * @param d línea con la factura, el concepto y el origen del cobro
	 * @param valor valor validado que se cobra
	 * @return número de línea asignado
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 */
	public int insertar(DetalleFactura d, BigDecimal valor) throws SQLException {
		try (Connection c = ConexionBD.obtenerConexion();
		     PreparedStatement ps = c.prepareStatement(INSERTAR)) {
			ps.setInt(1, d.getNumeroFactura());
			ps.setBigDecimal(2, valor);
			ps.setInt(3, d.getIdConcepto());
			ps.setObject(4, d.getIdAsignacion(), Types.INTEGER);
			ps.setObject(5, d.getIdReserva(), Types.INTEGER);
			ps.setObject(6, d.getIdSancion(), Types.INTEGER);
			ps.setInt(7, d.getNumeroFactura());
			try (ResultSet rs = ps.executeQuery()) {
				rs.next();
				return rs.getInt(1);
			}
		}
	}

	/**
	 * Cambia el valor de una línea de factura.
	 *
	 * @param factura número de la factura
	 * @param linea número de la línea dentro de la factura
	 * @param valor nuevo valor de la línea
	 * @return {@code true} si la línea existía y se actualizó
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 */
	public boolean actualizarValor(int factura, int linea, BigDecimal valor) throws SQLException {
		try (Connection c = ConexionBD.obtenerConexion();
		     PreparedStatement ps = c.prepareStatement(ACTUALIZAR_VALOR)) {
			ps.setBigDecimal(1, valor);
			ps.setInt(2, factura);
			ps.setInt(3, linea);
			return ps.executeUpdate() > 0;
		}
	}

	/**
	 * Elimina la línea de factura indicada por su llave primaria.
	 *
	 * @param factura número de la factura
	 * @param linea número de la línea dentro de la factura
	 * @return {@code true} si el registro existía y se eliminó
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 */
	public boolean eliminar(int factura, int linea) throws SQLException {
		try (Connection c = ConexionBD.obtenerConexion();
		     PreparedStatement ps = c.prepareStatement(ELIMINAR)) {
			ps.setInt(1, factura);
			ps.setInt(2, linea);
			return ps.executeUpdate() > 0;
		}
	}

	/**
	 * Busca un concepto de cobro por su identificador.
	 *
	 * @param id identificador del concepto
	 * @return concepto encontrado, o {@code null} si no existe
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 */
	public ConceptoCobro buscarConcepto(int id) throws SQLException {
		try (Connection c = ConexionBD.obtenerConexion();
		     PreparedStatement ps = c.prepareStatement(BUSCAR_CONCEPTO)) {
			ps.setInt(1, id);
			try (ResultSet rs = ps.executeQuery()) {
				if (!rs.next()) {
					return null;
				}
				return new ConceptoCobro(rs.getInt("id_concepto"), rs.getString("nombre"),
						rs.getString("descripcion"), rs.getBigDecimal("valor_base"));
			}
		}
	}

	/**
	 * Obtiene los datos de cobro de una sanción.
	 *
	 * @param id identificador de la sanción
	 * @return casa, estado del proceso y valor de la multa, o {@code null} si no existe
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 */
	public Origen buscarSancion(int id) throws SQLException {
		return buscarOrigen(BUSCAR_SANCION, id);
	}

	/**
	 * Obtiene los datos de cobro de una reserva del salón; el valor son las horas reservadas por el
	 * valor por hora.
	 *
	 * @param id identificador de la reserva
	 * @return casa, estado y valor de la reserva, o {@code null} si no existe
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 */
	public Origen buscarReserva(int id) throws SQLException {
		return buscarOrigen(BUSCAR_RESERVA, id);
	}

	/**
	 * Obtiene la casa de una asignación de celda.
	 *
	 * @param id identificador de la asignación
	 * @return casa de la asignación (sin valor), o {@code null} si no existe
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 */
	public Origen buscarAsignacion(int id) throws SQLException {
		return buscarOrigen(BUSCAR_ASIGNACION, id);
	}

	/**
	 * Indica si la sanción ya fue cobrada en alguna factura.
	 *
	 * @param id identificador de la sanción
	 * @return {@code true} si ya está en una línea de factura
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 */
	public boolean sancionFacturada(int id) throws SQLException {
		return existe(SANCION_FACTURADA, id, null);
	}

	/**
	 * Indica si la reserva ya fue cobrada en alguna factura.
	 *
	 * @param id identificador de la reserva
	 * @return {@code true} si ya está en una línea de factura
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 */
	public boolean reservaFacturada(int id) throws SQLException {
		return existe(RESERVA_FACTURADA, id, null);
	}

	/**
	 * Indica si la asignación ya está cobrada en la factura indicada.
	 *
	 * @param idAsignacion identificador de la asignación de celda
	 * @param factura número de la factura
	 * @return {@code true} si la factura ya tiene una línea con esa asignación
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 */
	public boolean asignacionEnFactura(int idAsignacion, int factura) throws SQLException {
		return existe(ASIGNACION_EN_FACTURA, idAsignacion, factura);
	}

	/**
	 * Ejecuta una consulta de origen (sanción, reserva o asignación) y la convierte en {@link Origen}.
	 *
	 * @param sql consulta que se ejecuta
	 * @param id identificador del origen
	 * @return datos del origen, o {@code null} si no existe
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 */
	private Origen buscarOrigen(String sql, int id) throws SQLException {
		try (Connection c = ConexionBD.obtenerConexion();
		     PreparedStatement ps = c.prepareStatement(sql)) {
			ps.setInt(1, id);
			try (ResultSet rs = ps.executeQuery()) {
				if (!rs.next()) {
					return null;
				}
				return new Origen(rs.getInt(1), rs.getInt(2), rs.getString(3), rs.getBigDecimal(4));
			}
		}
	}

	/**
	 * Ejecuta una consulta {@code SELECT EXISTS} con uno o dos parámetros enteros.
	 *
	 * @param sql consulta que se ejecuta
	 * @param primero primer parámetro
	 * @param segundo segundo parámetro; {@code null} si la consulta solo tiene uno
	 * @return resultado de la consulta
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 */
	private boolean existe(String sql, int primero, Integer segundo) throws SQLException {
		try (Connection c = ConexionBD.obtenerConexion();
		     PreparedStatement ps = c.prepareStatement(sql)) {
			ps.setInt(1, primero);
			if (segundo != null) {
				ps.setInt(2, segundo);
			}
			try (ResultSet rs = ps.executeQuery()) {
				rs.next();
				return rs.getBoolean(1);
			}
		}
	}
	
	/**
	 * Indica si la factura ya tiene una línea con ese concepto.
	 *
	 * @param idConcepto identificador del concepto de cobro
	 * @param numeroFactura número de la factura
	 * @return {@code true} si el concepto ya está en la factura
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 */
	public boolean conceptoEnFactura(int idConcepto, int numeroFactura)
	        throws SQLException {
	    return existe(CONCEPTO_EN_FACTURA, idConcepto, numeroFactura);
	}

	/**
	 * Convierte la fila actual del resultado en una línea de factura.
	 *
	 * @param rs resultado de la consulta, ubicado en la fila que se va a leer
	 * @return línea de factura leída
	 * @throws SQLException si ocurre un error al acceder a la base de datos
	 */
	private DetalleFactura mapear(ResultSet rs) throws SQLException {
		return new DetalleFactura(
				rs.getInt("factura_numero_factura"),
				rs.getInt("numero_linea"),
				rs.getInt("concepto_cobro_id_concepto"),
				rs.getBigDecimal("valor"),
				rs.getObject("asignacion_celda_id_asignacion", Integer.class),
				rs.getObject("reserva_salon_id_reserva", Integer.class),
				rs.getObject("sancion_id_sancion", Integer.class)
		);
	}
}