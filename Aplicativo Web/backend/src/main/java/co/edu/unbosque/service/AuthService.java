package co.edu.unbosque.service;

import org.mindrot.jbcrypt.BCrypt;

import co.edu.unbosque.config.ConfiguracionSeguridad;
import co.edu.unbosque.exception.DatoInvalidoException;
import co.edu.unbosque.exception.NoAutorizadoException;
import co.edu.unbosque.model.AuthResponse;
import co.edu.unbosque.model.LoginRequest;
import co.edu.unbosque.security.JwtUtil;

/**
 * Lógica del inicio de sesión del administrador general.
 * <p>
 * Compara el usuario y la clave (con BCrypt) contra
 * {@link co.edu.unbosque.config.ConfiguracionSeguridad} y, si son correctos, emite un token JWT con
 * el rol {@code ADMIN}.
 */
public class AuthService {
	
	/** Utilidad para generar el token. */
	private final JwtUtil jwtUtil = new JwtUtil();
	
	/**
	 * Valida las credenciales del administrador y emite un token. Siempre revisa el usuario y la clave
	 * antes de decidir y responde el mismo mensaje en ambos casos, para no revelar cuál de los dos
	 * falló.
	 *
	 * @param login usuario y clave enviados por el cliente
	 * @return token JWT y rol {@code ADMIN}
	 * @throws DatoInvalidoException si falta el usuario o la clave
	 * @throws NoAutorizadoException si el usuario o la clave no son correctos
	 */
	public AuthResponse login(LoginRequest login) throws DatoInvalidoException, NoAutorizadoException {
		if (login.getUsuario() == null || login.getUsuario().isBlank()
				|| login.getClave() == null || login.getClave().isBlank()) {
			throw new DatoInvalidoException("El usuario y clave son obligatorios");
		}

		boolean usuarioCorrecto = login.getUsuario().equals(ConfiguracionSeguridad.getAdminUsuario());
		boolean claveCorrecta = BCrypt.checkpw(login.getClave(), ConfiguracionSeguridad.getAdminClaveHash());

		if (!usuarioCorrecto || !claveCorrecta) {
			throw new NoAutorizadoException("Usuario o contraseña son incorrectos, intenta de nuevo");
		}

		String token = jwtUtil.generateToken(login.getUsuario());
		return new AuthResponse(token, "ADMIN");
	}

}
