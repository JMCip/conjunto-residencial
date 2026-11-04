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

public class JwtUtil {
	
	private final long JWT_TOKEN_VALIDITY = ConfiguracionSeguridad.getJwtExpiracionHoras() * 60 * 60 * 1000;
	
	private final String secret = ConfiguracionSeguridad.getJwtSecreto();
	
	private Key getSigningKey() {
		byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
		return Keys.hmacShaKeyFor(keyBytes);	
	}
	
	private Claims extractAllClaims(String token) {
		return Jwts.parserBuilder()
				.setSigningKey(getSigningKey())
				.build()
				.parseClaimsJws(token)
				.getBody();
	}
	
	public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
		final Claims claims = extractAllClaims(token);
		return claimsResolver.apply(claims);
	}
	
	public String extractUsername(String token) {
		return extractClaim(token, Claims::getSubject);
	}
	
	public Date extractExpiration(String token) {
		return extractClaim(token, Claims::getExpiration);
	}
	
	public String extractRole(String token) {
		return extractClaim(token, claims -> claims.get("role", String.class));
	}
	
	private Boolean isTokenExpired(String token) {
		return extractExpiration(token).before(new Date());
	}
	
	public String generateToken(String username) {
		Map<String, Object> claims = new HashMap<>();
		claims.put("role", "ADMIN");
		return createToken(claims, username);
	}
	
	private String createToken(Map<String, Object> claims, String subject) {
		return Jwts.builder()
				.setClaims(claims)
				.setSubject(subject)
				.setIssuedAt(new Date(System.currentTimeMillis()))
				.setExpiration(new Date(System.currentTimeMillis() + JWT_TOKEN_VALIDITY))
				.signWith(getSigningKey(), SignatureAlgorithm.HS256)
				.compact();
	}
	

	public Boolean validateToken(String token) {
		final String username = extractUsername(token);
		return username.equals(ConfiguracionSeguridad.getAdminUsuario()) && !isTokenExpired(token);
	}
}
