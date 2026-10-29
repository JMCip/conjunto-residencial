package co.edu.unbosque.dao;
import co.edu.unbosque.config.ConexionBD;
import co.edu.unbosque.model.Persona;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PersonaDAO {
	
	private static final String LISTAR = """ 
			SELECT tipo_documento, numero_documento, correo, primer_nombre, segundo_nombre, primer_apellido, segundo_apellido
			FROM persona
			ORDER BY primer_nombre ASC, primer_apellido ASC;
			""";
	
	public List<Persona> listar () throws SQLException {
		List<Persona> personas = new ArrayList<>();
		try (	Connection c = ConexionBD.obtenerConexion();
				PreparedStatement ps = c.prepareStatement(LISTAR);
				ResultSet rs = ps.executeQuery()) {
			
			while (rs.next()) {
				personas.add(mapear(rs));
			}
		}
		return personas;
	}
	
	private Persona mapear (ResultSet rs) throws SQLException {
		return new Persona(
				rs.getString("tipo_documento"),
				rs.getString("numero_documento"),
				rs.getString("correo"),
				rs.getString("primer_nombre"),
				rs.getString("segundo_nombre"),
				rs.getString("primer_apellido"),
				rs.getString("segundo_apellido")			
				);
	}
}
