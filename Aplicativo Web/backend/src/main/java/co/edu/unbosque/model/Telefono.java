package co.edu.unbosque.model;

/**
 * Número de teléfono de una persona.
 * <p>
 * Corresponde a la tabla {@code telefono}.
 */
public class Telefono {
	
	/** Número de teléfono (de 7 a 15 dígitos). */
	private String telefono;
	/** Tipo de documento del dueño del teléfono. */
	private String personaTipoDocumento;
	/** Número de documento del dueño del teléfono. */
	private String personaNumeroDocumento;
	
	/** Crea un teléfono vacío. Lo usa Jackson para convertir el JSON del cuerpo de la petición. */
	public Telefono() {
		
	}

	/**
	 * Crea un teléfono con todos sus datos.
	 *
	 * @param telefono número de teléfono (de 7 a 15 dígitos)
	 * @param personaTipoDocumento tipo de documento del dueño del teléfono
	 * @param personaNumeroDocumento número de documento del dueño del teléfono
	 */
	public Telefono(String telefono, String personaTipoDocumento, String personaNumeroDocumento) {
		super();
		this.telefono = telefono;
		this.personaTipoDocumento = personaTipoDocumento;
		this.personaNumeroDocumento = personaNumeroDocumento;
	}

	/**
	 * Devuelve el número de teléfono (de 7 a 15 dígitos).
	 *
	 * @return número de teléfono (de 7 a 15 dígitos)
	 */
	public String getTelefono() {
		return telefono;
	}

	/**
	 * Asigna el número de teléfono (de 7 a 15 dígitos).
	 *
	 * @param telefono número de teléfono (de 7 a 15 dígitos)
	 */
	public void setTelefono(String telefono) {
		this.telefono = telefono;
	}

	/**
	 * Devuelve el tipo de documento del dueño del teléfono.
	 *
	 * @return tipo de documento del dueño del teléfono
	 */
	public String getPersonaTipoDocumento() {
		return personaTipoDocumento;
	}

	/**
	 * Asigna el tipo de documento del dueño del teléfono.
	 *
	 * @param personaTipoDocumento tipo de documento del dueño del teléfono
	 */
	public void setPersonaTipoDocumento(String personaTipoDocumento) {
		this.personaTipoDocumento = personaTipoDocumento;
	}

	/**
	 * Devuelve el número de documento del dueño del teléfono.
	 *
	 * @return número de documento del dueño del teléfono
	 */
	public String getPersonaNumeroDocumento() {
		return personaNumeroDocumento;
	}

	/**
	 * Asigna el número de documento del dueño del teléfono.
	 *
	 * @param personaNumeroDocumento número de documento del dueño del teléfono
	 */
	public void setPersonaNumeroDocumento(String personaNumeroDocumento) {
		this.personaNumeroDocumento = personaNumeroDocumento;
	}

}
