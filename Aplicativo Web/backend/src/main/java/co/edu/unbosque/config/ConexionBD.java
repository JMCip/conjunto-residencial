package co.edu.unbosque.config;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Fábrica de conexiones JDBC a la base de datos PostgreSQL del proyecto.
 * <p>
 * Lee una sola vez, al cargar la clase, el archivo {@code db.properties} del classpath con las
 * propiedades {@code db.url}, {@code db.usuario} y {@code db.clave}. El archivo no se sube al
 * repositorio.
 */
public class ConexionBD {
	 /** Propiedades leídas de {@code db.properties}. */
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
	 
	 /**
	  * Abre una conexión nueva con los datos de {@code db.properties}. Quien la usa debe cerrarla (por
	  * ejemplo, con try-with-resources).
	  *
	  * @return conexión abierta con la base de datos
	  * @throws SQLException si no se puede abrir la conexión
	  */
	 public static Connection obtenerConexion() throws SQLException {
		 return DriverManager.getConnection(props.getProperty("db.url"), props.getProperty("db.usuario"), props.getProperty("db.clave"));
	 }
	 
}
