package co.edu.unbosque.security;

import java.util.List;

import co.edu.unbosque.exception.NoAutorizadoException;
import io.javalin.http.Context;
import io.javalin.http.HandlerType;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;

/**
 * Filtro que protege las rutas de la API con el token JWT.
 * <p>
 * Se ejecuta antes de cada ruta ({@code beforeMatched} en {@link co.edu.unbosque.App}). Deja pasar
 * las peticiones {@code OPTIONS} y las rutas públicas; en las demás exige el encabezado
 * {@code Authorization: Bearer <token>} con un token válido y vigente.
 */
public class JwtAuthenticationFilter {

	/** Prefijos de las rutas que no exigen token. */
	private static final List<String> RUTAS_PUBLICAS = List.of(
			"/api/auth/login",
			"/api/estado",
			"/swagger",
			"/openapi",
			"/webjars");

	/** Utilidad para validar los tokens. */
	private final JwtUtil jwtUtil = new JwtUtil();

	/**
	 * Revisa la petición antes de que llegue a la ruta. Deja pasar las peticiones {@code OPTIONS}
	 * (preflight de CORS) y las rutas públicas; en las demás exige un token Bearer válido.
	 *
	 * @param ctx contexto de la petición HTTP
	 * @throws NoAutorizadoException si falta el token, es inválido o expiró (HTTP 401)
	 */
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
			throw new NoAutorizadoException("El token ya expirado. Inicia sesión de nuevo");
		} catch (JwtException | IllegalArgumentException e) {
			throw new NoAutorizadoException("Token invalido");
		}
	}

	/**
	 * Indica si la ruta empieza por alguno de los prefijos públicos.
	 *
	 * @param ruta ruta de la petición
	 * @return {@code true} si la ruta no exige token
	 */
	private boolean esPublica(String ruta) {
		return RUTAS_PUBLICAS.stream().anyMatch(ruta::startsWith);
	}
}