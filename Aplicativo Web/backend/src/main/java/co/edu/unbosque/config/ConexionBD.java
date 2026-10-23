package co.edu.unbosque.config;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class ConexionBD {
	 private static final Properties props = new Properties();
	 
	 static {
		 try (InputStream in = ConexionBD.class.getResourceAsStream("/db.properties")) {
			 if (in == null) {
				 throw new IllegalStateException("No se encontro el archivo de db.properties");
			 }
			 props.load(in);
			 
		 } catch (IOException e) {
			throw new IllegalStateException("Error leyendo db.properties", e);
			
		}
	 }
	 
	 public static Connection obtenerConexion() throws SQLException {
		 return DriverManager.getConnection(props.getProperty("db.url"), props.getProperty("db.usuario"), props.getProperty("db.clave"));
	 }
	 
}
