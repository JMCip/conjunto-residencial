package co.edu.unbosque.model;

/** Respuesta del login: el token JWT emitido y el rol del usuario autenticado. */
public class AuthResponse {

	/** Token JWT firmado que se envía en el encabezado {@code Authorization}. */
	private String token;
	/** Rol del usuario autenticado ({@code ADMIN}). */
	private String role;

	/** Crea un objeto vacío. Lo usa Jackson para convertir el JSON del cuerpo de la petición. */
	public AuthResponse() {

	}

	/**
	 * Crea el objeto con todos sus datos.
	 *
	 * @param token token JWT firmado que se envía en el encabezado {@code Authorization}
	 * @param role rol del usuario autenticado ({@code ADMIN})
	 */
	public AuthResponse(String token, String role) {
		super();
		this.token = token;
		this.role = role;
	}

	/**
	 * Devuelve el token JWT firmado que se envía en el encabezado {@code Authorization}.
	 *
	 * @return token JWT firmado que se envía en el encabezado {@code Authorization}
	 */
	public String getToken() {
		return token;
	}

	/**
	 * Asigna el token JWT firmado que se envía en el encabezado {@code Authorization}.
	 *
	 * @param token token JWT firmado que se envía en el encabezado {@code Authorization}
	 */
	public void setToken(String token) {
		this.token = token;
	}

	/**
	 * Devuelve el rol del usuario autenticado ({@code ADMIN}).
	 *
	 * @return rol del usuario autenticado ({@code ADMIN})
	 */
	public String getRole() {
		return role;
	}

	/**
	 * Asigna el rol del usuario autenticado ({@code ADMIN}).
	 *
	 * @param role rol del usuario autenticado ({@code ADMIN})
	 */
	public void setRole(String role) {
		this.role = role;
	}
}