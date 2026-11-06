package co.edu.unbosque.model;

/** Datos que envía el cliente para iniciar sesión. */
public class LoginRequest {

	/** Nombre de usuario del administrador. */
	private String usuario;
	/** Clave en texto plano (no el hash). */
	private String clave;

	/** Crea un objeto vacío. Lo usa Jackson para convertir el JSON del cuerpo de la petición. */
	public LoginRequest() {

	}

	/**
	 * Crea el objeto con todos sus datos.
	 *
	 * @param usuario nombre de usuario del administrador
	 * @param clave clave en texto plano (no el hash)
	 */
	public LoginRequest(String usuario, String clave) {
		super();
		this.usuario = usuario;
		this.clave = clave;
	}

	/**
	 * Devuelve el nombre de usuario del administrador.
	 *
	 * @return nombre de usuario del administrador
	 */
	public String getUsuario() {
		return usuario;
	}

	/**
	 * Asigna el nombre de usuario del administrador.
	 *
	 * @param usuario nombre de usuario del administrador
	 */
	public void setUsuario(String usuario) {
		this.usuario = usuario;
	}

	/**
	 * Devuelve la clave en texto plano (no el hash).
	 *
	 * @return clave en texto plano (no el hash)
	 */
	public String getClave() {
		return clave;
	}

	/**
	 * Asigna la clave en texto plano (no el hash).
	 *
	 * @param clave clave en texto plano (no el hash)
	 */
	public void setClave(String clave) {
		this.clave = clave;
	}
}