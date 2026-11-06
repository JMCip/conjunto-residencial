package co.edu.unbosque.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Acceso a la configuración de seguridad del backend.
 * <p>
 * Lee una sola vez, al cargar la clase, el archivo {@code seguridad.properties} del classpath con
 * la clave secreta del JWT, la duración del token y las credenciales del administrador (la clave se
 * guarda como hash BCrypt). El archivo no se sube al repositorio.
 */
public class ConfiguracionSeguridad {

	/** Propiedades leídas de {@code seguridad.properties}. */
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
	
	/**
	 * Devuelve la clave secreta con la que se firman los tokens ({@code jwt.secreto}).
	 *
	 * @return clave secreta del JWT
	 */
	public static String getJwtSecreto() {
		return props.getProperty("jwt.secreto");
	}
	
	/**
	 * Devuelve cuántas horas dura un token ({@code jwt.expiracion.horas}).
	 *
	 * @return duración del token en horas
	 */
	public static long getJwtExpiracionHoras() {
		return Long.parseLong(props.getProperty("jwt.expiracion.horas"));
	}
	
	/**
	 * Devuelve el nombre de usuario del administrador ({@code admin.usuario}).
	 *
	 * @return usuario del administrador
	 */
	public static String getAdminUsuario() {
		return props.getProperty("admin.usuario");
	}
	
	/**
	 * Devuelve el hash BCrypt de la clave del administrador ({@code admin.clave.hash}).
	 *
	 * @return hash BCrypt de la clave
	 */
	public static String getAdminClaveHash() {
		return props.getProperty("admin.clave.hash");
	}

}
