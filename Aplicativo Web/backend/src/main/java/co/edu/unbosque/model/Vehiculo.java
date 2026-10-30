package co.edu.unbosque.model;

public class Vehiculo {
	
	private String placa;
	private String marca;
	private Integer modelo;
	private String tipo;
	private String personaTipoDocumento;
	private String personaNumeroDocumento;
	
	public Vehiculo() {
		
	}

	public Vehiculo(String placa, String marca, Integer modelo, String tipo, String personaTipoDocumento, String personaNumeroDocumento) {
		super();
		this.placa = placa;
		this.marca = marca;
		this.modelo = modelo;
		this.tipo = tipo;
		this.personaTipoDocumento = personaTipoDocumento;
		this.personaNumeroDocumento = personaNumeroDocumento;
	}

	public String getPlaca() {
		return placa;
	}

	public void setPlaca(String placa) {
		this.placa = placa;
	}

	public String getMarca() {
		return marca;
	}

	public void setMarca(String marca) {
		this.marca = marca;
	}

	public Integer getModelo() {
		return modelo;
	}

	public void setModelo(Integer modelo) {
		this.modelo = modelo;
	}

	public String getTipo() {
		return tipo;
	}

	public void setTipo(String tipo) {
		this.tipo = tipo;
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
