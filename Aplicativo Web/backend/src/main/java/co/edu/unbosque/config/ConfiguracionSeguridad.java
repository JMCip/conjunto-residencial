package co.edu.unbosque.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfiguracionSeguridad {

	private static final Properties props = new Properties();
	
	static {	
		try (InputStream in = ConfiguracionSeguridad.class.getResourceAsStream("/seguridad.properties")) {
			if(in == null) {
				throw new IllegalStateException("No se encontro seguridad.properties");
			}
			props.load(in);
			
		} catch (IOException e) {
			throw new IllegalStateException("Error al leer seguridad.properties", e);
		}
	}
	
	public static String getJwtSecreto() {
		return props.getProperty("jwt.secreto");
	}
	
	public static long getJwtExpiracionHoras() {
		return Long.parseLong(props.getProperty("jwt.expiracion.horas"));
	}
	
	public static String getAdminUsuario() {
		return props.getProperty("admin.usuario");
	}
	
	public static String getAdminClaveHash() {
		return props.getProperty("admin.clave.hash");
	}

}
