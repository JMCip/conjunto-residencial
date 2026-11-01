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
	            SELECT numero, 
	            manzana, 
	            area, estado,
	            numero_habitaciones
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
	  
	  private static final String INSERTAR = """
		        INSERT INTO casa (numero, 
		        manzana, 
		        area, 
		        estado, 
		        numero_habitaciones)
		        VALUES (?, ?, ?, ?, ?)
		        """;

		public void insertar(Casa c) throws SQLException {
		    try (Connection con = ConexionBD.obtenerConexion();
		         PreparedStatement ps = con.prepareStatement(INSERTAR)) {
		        ps.setInt(1, c.getNumero());
		        ps.setInt(2, c.getManzana());
		        ps.setBigDecimal(3, c.getArea());
		        ps.setString(4, c.getEstado());
		        ps.setObject(5, c.getNumeroHabitaciones());
		        ps.executeUpdate();
		    }
		}
		
		
		private static final String ACTUALIZAR = """
		        UPDATE casa
		        SET area = ?,
		            estado = ?,
		            numero_habitaciones = ?
		        WHERE numero = ?
		        AND manzana = ?
		        """;

		public boolean actualizar(Casa c) throws SQLException {
		    try (Connection con = ConexionBD.obtenerConexion();
		         PreparedStatement ps = con.prepareStatement(ACTUALIZAR)) {
		        ps.setBigDecimal(1, c.getArea());
		        ps.setString(2, c.getEstado());
		        ps.setObject(3, c.getNumeroHabitaciones());
		        ps.setInt(4, c.getNumero());
		        ps.setInt(5, c.getManzana());
		        return ps.executeUpdate() > 0;
		    }
		}
		
		private static final String ELIMINAR = """
		        DELETE FROM casa 
		        WHERE numero = ? 
		        AND manzana = ?
		        """;

		public boolean eliminar(int numero, int manzana) throws SQLException {
		    try (Connection con = ConexionBD.obtenerConexion();
		         PreparedStatement ps = con.prepareStatement(ELIMINAR)) {
		        ps.setInt(1, numero);
		        ps.setInt(2, manzana);
		        return ps.executeUpdate() > 0;
		    }
		}
		
}
