package co.edu.unbosque.service;

import org.mindrot.jbcrypt.BCrypt;

import co.edu.unbosque.config.ConfiguracionSeguridad;
import co.edu.unbosque.exception.DatoInvalidoException;
import co.edu.unbosque.exception.NoAutorizadoException;
import co.edu.unbosque.model.AuthResponse;
import co.edu.unbosque.model.LoginRequest;
import co.edu.unbosque.security.JwtUtil;


public class AuthService {
	
	private final JwtUtil jwtUtil = new JwtUtil();
	
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
