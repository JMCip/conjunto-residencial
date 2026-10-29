package co.edu.unbosque.dao;
import co.edu.unbosque.config.ConexionBD;
import co.edu.unbosque.model.Casa;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CasaDao {
	
	  private static final String LISTAR = """
	            SELECT numero, manzana, area, estado, numero_habitaciones
	            FROM casa
	            ORDER BY manzana, numero
	            """;
	  
	  public List<Casa> listar() throws SQLException {
		  List<Casa> casas = new ArrayList<>();
		  try (	Connection con = ConexionBD.obtenerConexion(); 
				PreparedStatement ps = con.prepareStatement(LISTAR);
				ResultSet rs = ps.executeQuery()) {
			  
			  while (rs.next()) {
				  casas.add(mapear(rs));
			  }		  
		  }
		  return casas;
	  }
	  
	  
	  private Casa mapear (ResultSet rs) throws SQLException {
		  return new Casa (
				  rs.getInt("numero"),
				  rs.getInt("manzana"),
				  rs.getBigDecimal("area"),
				  rs.getString("estado"),
				  rs.getObject("numero_habitaciones" ,  Integer.class)
				  );
		  
	  }	  
}
