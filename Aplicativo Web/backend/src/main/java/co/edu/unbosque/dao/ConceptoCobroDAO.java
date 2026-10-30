package co.edu.unbosque.dao;
import co.edu.unbosque.config.ConexionBD;
import co.edu.unbosque.model.ConceptoCobro;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ConceptoCobroDAO {
	
	private static final String LISTAR = """
			SELECT id_concepto, nombre, descripcion, valor_base
			FROM concepto_cobro
			ORDER BY id_concepto
			""";
	
	public List<ConceptoCobro> listar() throws SQLException {
		List<ConceptoCobro> conceptos = new ArrayList<>();
		try (	Connection c = ConexionBD.obtenerConexion();
				PreparedStatement ps = c.prepareStatement(LISTAR);
				ResultSet rs = ps.executeQuery()) {
			
			while (rs.next()) {
				conceptos.add(mapear(rs));
			}
		}
		return conceptos;
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
