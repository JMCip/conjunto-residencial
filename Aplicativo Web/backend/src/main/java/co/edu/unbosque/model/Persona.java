package co.edu.unbosque.model;

/**
 * Persona registrada en el conjunto (propietario, arrendatario, residente o visitante).
 * <p>
 * Corresponde a la tabla {@code persona}; se identifica por su tipo y número de documento.
 */
public class Persona {
	
	/** Tipo de documento. */
	private String tipoDocumento;
	/** Número de documento. */
	private String numeroDocumento;
	/** Correo electrónico; puede ser {@code null}. */
	private String Correo;
	/** Primer nombre. */
	private String primerNombre;
	/** Segundo nombre; puede ser {@code null}. */
	private String segundoNombre;
	/** Primer apellido. */
	private String primerApellido;
	/** Segundo apellido; puede ser {@code null}. */
	private String segundoApellido;
	
	/** Crea una persona vacía. Lo usa Jackson para convertir el JSON del cuerpo de la petición. */
	public Persona() {
		
	}

	/**
	 * Crea una persona con todos sus datos.
	 *
	 * @param tipoDocumento tipo de documento
	 * @param numeroDocumento número de documento
	 * @param correo correo electrónico; puede ser {@code null}
	 * @param primerNombre primer nombre
	 * @param segundoNombre segundo nombre; puede ser {@code null}
	 * @param primerApellido primer apellido
	 * @param segundoApellido segundo apellido; puede ser {@code null}
	 */
	public Persona(String tipoDocumento, String numeroDocumento, String correo, String primerNombre,
			String segundoNombre, String primerApellido, String segundoApellido) {
		super();
		this.tipoDocumento = tipoDocumento;
		this.numeroDocumento = numeroDocumento;
		this.Correo = correo;
		this.primerNombre = primerNombre;
		this.segundoNombre = segundoNombre;
		this.primerApellido = primerApellido;
		this.segundoApellido = segundoApellido;
	}

	/**
	 * Devuelve el tipo de documento.
	 *
	 * @return tipo de documento
	 */
	public String getTipoDocumento() {
		return tipoDocumento;
	}

	/**
	 * Asigna el tipo de documento.
	 *
	 * @param tipoDocumento tipo de documento
	 */
	public void setTipoDocumento(String tipoDocumento) {
		this.tipoDocumento = tipoDocumento;
	}

	/**
	 * Devuelve el número de documento.
	 *
	 * @return número de documento
	 */
	public String getNumeroDocumento() {
		return numeroDocumento;
	}

	/**
	 * Asigna el número de documento.
	 *
	 * @param numeroDocumento número de documento
	 */
	public void setNumeroDocumento(String numeroDocumento) {
		this.numeroDocumento = numeroDocumento;
	}

	/**
	 * Devuelve el correo electrónico; puede ser {@code null}.
	 *
	 * @return correo electrónico; puede ser {@code null}
	 */
	public String getCorreo() {
		return Correo;
	}

	/**
	 * Asigna el correo electrónico; puede ser {@code null}.
	 *
	 * @param correo correo electrónico; puede ser {@code null}
	 */
	public void setCorreo(String correo) {
		Correo = correo;
	}

	/**
	 * Devuelve el primer nombre.
	 *
	 * @return primer nombre
	 */
	public String getPrimerNombre() {
		return primerNombre;
	}

	/**
	 * Asigna el primer nombre.
	 *
	 * @param primerNombre primer nombre
	 */
	public void setPrimerNombre(String primerNombre) {
		this.primerNombre = primerNombre;
	}

	/**
	 * Devuelve el segundo nombre; puede ser {@code null}.
	 *
	 * @return segundo nombre; puede ser {@code null}
	 */
	public String getSegundoNombre() {
		return segundoNombre;
	}

	/**
	 * Asigna el segundo nombre; puede ser {@code null}.
	 *
	 * @param segundoNombre segundo nombre; puede ser {@code null}
	 */
	public void setSegundoNombre(String segundoNombre) {
		this.segundoNombre = segundoNombre;
	}

	/**
	 * Devuelve el primer apellido.
	 *
	 * @return primer apellido
	 */
	public String getPrimerApellido() {
		return primerApellido;
	}

	/**
	 * Asigna el primer apellido.
	 *
	 * @param primerApellido primer apellido
	 */
	public void setPrimerApellido(String primerApellido) {
		this.primerApellido = primerApellido;
	}

	/**
	 * Devuelve el segundo apellido; puede ser {@code null}.
	 *
	 * @return segundo apellido; puede ser {@code null}
	 */
	public String getSegundoApellido() {
		return segundoApellido;
	}

	/**
	 * Asigna el segundo apellido; puede ser {@code null}.
	 *
	 * @param segundoApellido segundo apellido; puede ser {@code null}
	 */
	public void setSegundoApellido(String segundoApellido) {
		this.segundoApellido = segundoApellido;
	}
	
}
