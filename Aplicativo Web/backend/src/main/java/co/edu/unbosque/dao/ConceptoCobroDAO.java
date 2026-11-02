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

public class ConceptoCobroDAO {

	private static final String LISTAR = """
			SELECT id_concepto, 
				   nombre, 
				   descripcion, 
				   valor_base
			FROM concepto_cobro
			ORDER BY id_concepto
			""";

	private static final String BUSCAR_POR_ID = """
			SELECT id_concepto, 
				   nombre, 
				   descripcion, 
				   valor_base
			FROM concepto_cobro
			WHERE id_concepto = ?
			""";

	private static final String INSERTAR = """
			INSERT INTO concepto_cobro (nombre, descripcion, valor_base)
			VALUES (?, ?, ?)
			RETURNING id_concepto
			""";

	private static final String ACTUALIZAR = """
			UPDATE concepto_cobro
			SET nombre = ?, descripcion = ?, valor_base = ?
			WHERE id_concepto = ?
			""";

	private static final String ELIMINAR = "DELETE FROM concepto_cobro WHERE id_concepto = ?";

	
	private static final String EXISTE_NOMBRE = """
			SELECT EXISTS (SELECT 1 FROM concepto_cobro
			               WHERE LOWER(TRIM(nombre)) = LOWER(TRIM(?)) AND id_concepto <> ?)
			""";

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

	
	public ConceptoCobro buscarPorId(int id) throws SQLException {
		try (Connection c = ConexionBD.obtenerConexion();
		     PreparedStatement ps = c.prepareStatement(BUSCAR_POR_ID)) {
			ps.setInt(1, id);
			try (ResultSet rs = ps.executeQuery()) {
				return rs.next() ? mapear(rs) : null;
			}
		}
	}

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

	public boolean eliminar(int id) throws SQLException {
		try (Connection c = ConexionBD.obtenerConexion();
		     PreparedStatement ps = c.prepareStatement(ELIMINAR)) {
			ps.setInt(1, id);
			return ps.executeUpdate() > 0;
		}
	}

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

	private ConceptoCobro mapear(ResultSet rs) throws SQLException {
		return new ConceptoCobro(
				rs.getInt("id_concepto"),
				rs.getString("nombre"),
				rs.getString("descripcion"),
				rs.getBigDecimal("valor_base")
		);
	}
}