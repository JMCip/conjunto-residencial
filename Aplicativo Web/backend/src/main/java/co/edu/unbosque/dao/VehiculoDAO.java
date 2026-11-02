package co.edu.unbosque.dao;
import co.edu.unbosque.config.ConexionBD;
import co.edu.unbosque.model.Vehiculo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class VehiculoDAO {
	
	private static final String LISTAR = """
			SELECT placa, marca, modelo, tipo, persona_tipo_documento, persona_numero_documento
			FROM vehiculo
			ORDER BY placa
			""";
	
	public List<Vehiculo> listar() throws SQLException {
		List<Vehiculo> vehiculos = new ArrayList<>();
		try (	Connection c = ConexionBD.obtenerConexion();
				PreparedStatement ps = c.prepareStatement(LISTAR);
				ResultSet rs = ps.executeQuery()) {
			
			while (rs.next()) {
				vehiculos.add(mapear(rs));
			}
		}
		return vehiculos;
	}
	
	private Vehiculo mapear(ResultSet rs) throws SQLException {
		return new Vehiculo(
				rs.getString("placa"),
				rs.getString("marca"),
				rs.getObject("modelo", Integer.class),
				rs.getString("tipo"),
				rs.getString("persona_tipo_documento"),
				rs.getString("persona_numero_documento")
				);
	}
	
	private static final String BUSCAR = """
			SELECT placa, marca, modelo, tipo, persona_tipo_documento, persona_numero_documento
			FROM vehiculo
			WHERE placa = ?
			""";

	public Vehiculo buscar(String placa) throws SQLException {
		try (Connection c = ConexionBD.obtenerConexion();
				PreparedStatement ps = c.prepareStatement(BUSCAR)) {
			ps.setString(1, placa);
			try (ResultSet rs = ps.executeQuery()) {
				return rs.next() ? mapear(rs) : null;
			}
		}
	}

	private static final String INSERTAR = """
			INSERT INTO vehiculo (placa, marca, modelo, tipo, persona_tipo_documento, persona_numero_documento)
			VALUES (?, ?, ?, ?, ?, ?)
			""";

	public void insertar(Vehiculo v) throws SQLException {
		try (Connection c = ConexionBD.obtenerConexion();
				PreparedStatement ps = c.prepareStatement(INSERTAR)) {
			ps.setString(1, v.getPlaca());
			ps.setString(2, v.getMarca());
			ps.setObject(3, v.getModelo());
			ps.setString(4, v.getTipo());
			ps.setString(5, v.getPersonaTipoDocumento());
			ps.setString(6, v.getPersonaNumeroDocumento());
			ps.executeUpdate();
		}
	}

	private static final String ACTUALIZAR = """
			UPDATE vehiculo
			SET marca = ?,
			    modelo = ?,
			    tipo = ?,
			    persona_tipo_documento = ?,
			    persona_numero_documento = ?
			WHERE placa = ?
			""";

	public boolean actualizar(Vehiculo v) throws SQLException {
		try (Connection c = ConexionBD.obtenerConexion();
				PreparedStatement ps = c.prepareStatement(ACTUALIZAR)) {
			ps.setString(1, v.getMarca());
			ps.setObject(2, v.getModelo());
			ps.setString(3, v.getTipo());
			ps.setString(4, v.getPersonaTipoDocumento());
			ps.setString(5, v.getPersonaNumeroDocumento());
			ps.setString(6, v.getPlaca());
			return ps.executeUpdate() > 0;
		}
	}

	private static final String ELIMINAR = """
			DELETE FROM vehiculo
			WHERE placa = ?
			""";

	public boolean eliminar(String placa) throws SQLException {
		try (Connection c = ConexionBD.obtenerConexion();
				PreparedStatement ps = c.prepareStatement(ELIMINAR)) {
			ps.setString(1, placa);
			return ps.executeUpdate() > 0;
		}
	}
	
}
