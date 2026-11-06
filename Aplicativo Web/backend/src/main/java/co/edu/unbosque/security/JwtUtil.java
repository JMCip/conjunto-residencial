package co.edu.unbosque.security;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import co.edu.unbosque.config.ConfiguracionSeguridad;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

/**
 * Utilidades para generar, leer y validar tokens JWT firmados con HS256.
 * <p>
 * La clave de firma y la duración del token se toman de {@link ConfiguracionSeguridad}. Usa la
 * librería jjwt 0.11.5.
 */
public class JwtUtil {
	
	/** Duración del token en milisegundos, calculada a partir de {@code jwt.expiracion.horas}. */
	private final long JWT_TOKEN_VALIDITY = ConfiguracionSeguridad.getJwtExpiracionHoras() * 60 * 60 * 1000;
	
	/** Clave secreta con la que se firman los tokens. */
	private final String secret = ConfiguracionSeguridad.getJwtSecreto();
	
	/**
	 * Construye la llave HMAC de firma a partir de la clave secreta (en UTF-8).
	 *
	 * @return llave para firmar y verificar tokens
	 */
	private Key getSigningKey() {
		byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
		return Keys.hmacShaKeyFor(keyBytes);	
	}
	
	/**
	 * Verifica la firma del token y devuelve todos sus claims.
	 *
	 * @param token token JWT
	 * @return claims del token
	 */
	private Claims extractAllClaims(String token) {
		return Jwts.parserBuilder()
				.setSigningKey(getSigningKey())
				.build()
				.parseClaimsJws(token)
				.getBody();
	}
	
	/**
	 * Extrae un claim del token con la función indicada.
	 *
	 * @param <T> tipo del valor del claim
	 * @param token token JWT
	 * @param claimsResolver función que obtiene el claim a partir de los claims del token
	 * @return valor del claim
	 */
	public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
		final Claims claims = extractAllClaims(token);
		return claimsResolver.apply(claims);
	}
	
	/**
	 * Extrae el usuario (claim {@code sub}) del token.
	 *
	 * @param token token JWT
	 * @return usuario dueño del token
	 */
	public String extractUsername(String token) {
		return extractClaim(token, Claims::getSubject);
	}
	
	/**
	 * Extrae la fecha de expiración (claim {@code exp}) del token.
	 *
	 * @param token token JWT
	 * @return fecha de expiración
	 */
	public Date extractExpiration(String token) {
		return extractClaim(token, Claims::getExpiration);
	}
	
	/**
	 * Extrae el rol (claim {@code role}) del token.
	 *
	 * @param token token JWT
	 * @return rol del usuario
	 */
	public String extractRole(String token) {
		return extractClaim(token, claims -> claims.get("role", String.class));
	}
	
	/**
	 * Indica si el token ya expiró.
	 *
	 * @param token token JWT
	 * @return {@code true} si la fecha de expiración ya pasó
	 */
	private Boolean isTokenExpired(String token) {
		return extractExpiration(token).before(new Date());
	}
	
	/**
	 * Genera un token para el usuario con el rol {@code ADMIN}.
	 *
	 * @param username usuario que inicia sesión
	 * @return token JWT firmado
	 */
	public String generateToken(String username) {
		Map<String, Object> claims = new HashMap<>();
		claims.put("role", "ADMIN");
		return createToken(claims, username);
	}
	
	/**
	 * Construye y firma el token con los claims, el usuario, la fecha de emisión y la de expiración.
	 *
	 * @param claims claims adicionales del token
	 * @param subject usuario dueño del token (claim {@code sub})
	 * @return token JWT compacto
	 */
	private String createToken(Map<String, Object> claims, String subject) {
		return Jwts.builder()
				.setClaims(claims)
				.setSubject(subject)
				.setIssuedAt(new Date(System.currentTimeMillis()))
				.setExpiration(new Date(System.currentTimeMillis() + JWT_TOKEN_VALIDITY))
				.signWith(getSigningKey(), SignatureAlgorithm.HS256)
				.compact();
	}

	/**
	 * Valida el token: la firma debe ser correcta, el usuario debe ser el administrador configurado y
	 * no debe haber expirado. Si la firma no es válida o el token expiró, jjwt lanza una excepción no
	 * verificada que atiende el filtro.
	 *
	 * @param token token JWT
	 * @return {@code true} si el token es válido
	 */
	public Boolean validateToken(String token) {
		final String username = extractUsername(token);
		return username.equals(ConfiguracionSeguridad.getAdminUsuario()) && !isTokenExpired(token);
	}
}
