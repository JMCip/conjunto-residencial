package co.edu.unbosque.dao;
import co.edu.unbosque.config.ConexionBD;
import co.edu.unbosque.model.CeldaParqueadero;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CeldaParqueaderoDAO {
	
	private static final String LISTAR = """
			SELECT id_celda, tipo, estado
			FROM celda_parqueadero
			ORDER BY id_celda
			""";
	
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
	
	private CeldaParqueadero mapear(ResultSet rs) throws SQLException {
		return new CeldaParqueadero(
				rs.getInt("id_celda"),
				rs.getString("tipo"),
				rs.getString("estado")
				);
	}
	
	private static final String BUSCAR = """
			SELECT id_celda, tipo, estado
			FROM celda_parqueadero
			WHERE id_celda = ?
			""";

	public CeldaParqueadero buscar(int id) throws SQLException {
		try (Connection c = ConexionBD.obtenerConexion();
				PreparedStatement ps = c.prepareStatement(BUSCAR)) {
			ps.setInt(1, id);
			try (ResultSet rs = ps.executeQuery()) {
				return rs.next() ? mapear(rs) : null;
			}
		}
	}

	private static final String INSERTAR = """
			INSERT INTO celda_parqueadero (tipo, estado)
			VALUES (?, ?)
			RETURNING id_celda
			""";

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

	private static final String ACTUALIZAR = """
			UPDATE celda_parqueadero
			SET tipo = ?,
			    estado = ?
			WHERE id_celda = ?
			""";

	public boolean actualizar(CeldaParqueadero c) throws SQLException {
		try (Connection con = ConexionBD.obtenerConexion();
				PreparedStatement ps = con.prepareStatement(ACTUALIZAR)) {
			ps.setString(1, c.getTipo());
			ps.setString(2, c.getEstado());
			ps.setInt(3, c.getIdCelda());
			return ps.executeUpdate() > 0;
		}
	}

	private static final String ELIMINAR = """
			DELETE FROM celda_parqueadero
			WHERE id_celda = ?
			""";

	public boolean eliminar(int id) throws SQLException {
		try (Connection c = ConexionBD.obtenerConexion();
				PreparedStatement ps = c.prepareStatement(ELIMINAR)) {
			ps.setInt(1, id);
			return ps.executeUpdate() > 0;
		}
	}
	
}
