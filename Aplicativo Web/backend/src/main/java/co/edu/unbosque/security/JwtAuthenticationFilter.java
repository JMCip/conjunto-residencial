package co.edu.unbosque.security;

import java.util.List;

import co.edu.unbosque.exception.NoAutorizadoException;
import io.javalin.http.Context;
import io.javalin.http.HandlerType;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;

public class JwtAuthenticationFilter {

	private static final List<String> RUTAS_PUBLICAS = List.of(
			"/api/auth/login",
			"/api/estado",
			"/swagger",
			"/openapi",
			"/webjars");

	private final JwtUtil jwtUtil = new JwtUtil();

	public void filtrar(Context ctx) throws NoAutorizadoException {
		if (ctx.method() == HandlerType.OPTIONS || esPublica(ctx.path())) {
			return;
		}

		final String authorizationHeader = ctx.header("Authorization");

		if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
			throw new NoAutorizadoException("Falta ingresar el token. Envía el encabezado Authorization: Bearer <token>");
		}

		String jwt = authorizationHeader.substring(7);

		try {
			if (!jwtUtil.validateToken(jwt)) {
				throw new NoAutorizadoException("EL token no es valido");
			}
		} catch (ExpiredJwtException e) {
			throw new NoAutorizadoException("El token ya a expirado. Inicia sesión de nuevo");
		} catch (JwtException | IllegalArgumentException e) {
			throw new NoAutorizadoException("Token inva<lido");
		}
	}

	private boolean esPublica(String ruta) {
		return RUTAS_PUBLICAS.stream().anyMatch(ruta::startsWith);
	}
}