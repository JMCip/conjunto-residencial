package co.edu.unbosque.model;

public class Telefono {
	
	private String telefono;
	private String personaTipoDocumento;
	private String personaNumeroDocumento;
	
	public Telefono() {
		
	}

	public Telefono(String telefono, String personaTipoDocumento, String personaNumeroDocumento) {
		super();
		this.telefono = telefono;
		this.personaTipoDocumento = personaTipoDocumento;
		this.personaNumeroDocumento = personaNumeroDocumento;
	}

	public String getTelefono() {
		return telefono;
	}

	public void setTelefono(String telefono) {
		this.telefono = telefono;
	}

	public String getPersonaTipoDocumento() {
		return personaTipoDocumento;
	}

	public void setPersonaTipoDocumento(String personaTipoDocumento) {
		this.personaTipoDocumento = personaTipoDocumento;
	}

	public String getPersonaNumeroDocumento() {
		return personaNumeroDocumento;
	}

	public void setPersonaNumeroDocumento(String personaNumeroDocumento) {
		this.personaNumeroDocumento = personaNumeroDocumento;
	}

}
