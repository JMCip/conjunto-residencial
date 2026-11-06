package co.edu.unbosque.model;

/**
 * Vehículo registrado a nombre de una persona.
 * <p>
 * Corresponde a la tabla {@code vehiculo}.
 */
public class Vehiculo {
	
	/** Placa del vehículo. */
	private String placa;
	/** Marca del vehículo. */
	private String marca;
	/** Año del modelo; puede ser {@code null}. */
	private Integer modelo;
	/** Tipo de vehículo: {@code CARRO} o {@code MOTO}. */
	private String tipo;
	/** Tipo de documento del dueño del vehículo. */
	private String personaTipoDocumento;
	/** Número de documento del dueño del vehículo. */
	private String personaNumeroDocumento;
	
	/** Crea un vehículo vacío. Lo usa Jackson para convertir el JSON del cuerpo de la petición. */
	public Vehiculo() {
		
	}

	/**
	 * Crea un vehículo con todos sus datos.
	 *
	 * @param placa placa del vehículo
	 * @param marca marca del vehículo
	 * @param modelo año del modelo; puede ser {@code null}
	 * @param tipo tipo de vehículo: {@code CARRO} o {@code MOTO}
	 * @param personaTipoDocumento tipo de documento del dueño del vehículo
	 * @param personaNumeroDocumento número de documento del dueño del vehículo
	 */
	public Vehiculo(String placa, String marca, Integer modelo, String tipo, String personaTipoDocumento, String personaNumeroDocumento) {
		super();
		this.placa = placa;
		this.marca = marca;
		this.modelo = modelo;
		this.tipo = tipo;
		this.personaTipoDocumento = personaTipoDocumento;
		this.personaNumeroDocumento = personaNumeroDocumento;
	}

	/**
	 * Devuelve la placa del vehículo.
	 *
	 * @return placa del vehículo
	 */
	public String getPlaca() {
		return placa;
	}

	/**
	 * Asigna la placa del vehículo.
	 *
	 * @param placa placa del vehículo
	 */
	public void setPlaca(String placa) {
		this.placa = placa;
	}

	/**
	 * Devuelve la marca del vehículo.
	 *
	 * @return marca del vehículo
	 */
	public String getMarca() {
		return marca;
	}

	/**
	 * Asigna la marca del vehículo.
	 *
	 * @param marca marca del vehículo
	 */
	public void setMarca(String marca) {
		this.marca = marca;
	}

	/**
	 * Devuelve el año del modelo; puede ser {@code null}.
	 *
	 * @return año del modelo; puede ser {@code null}
	 */
	public Integer getModelo() {
		return modelo;
	}

	/**
	 * Asigna el año del modelo; puede ser {@code null}.
	 *
	 * @param modelo año del modelo; puede ser {@code null}
	 */
	public void setModelo(Integer modelo) {
		this.modelo = modelo;
	}

	/**
	 * Devuelve el tipo de vehículo: {@code CARRO} o {@code MOTO}.
	 *
	 * @return tipo de vehículo: {@code CARRO} o {@code MOTO}
	 */
	public String getTipo() {
		return tipo;
	}

	/**
	 * Asigna el tipo de vehículo: {@code CARRO} o {@code MOTO}.
	 *
	 * @param tipo tipo de vehículo: {@code CARRO} o {@code MOTO}
	 */
	public void setTipo(String tipo) {
		this.tipo = tipo;
	}

	/**
	 * Devuelve el tipo de documento del dueño del vehículo.
	 *
	 * @return tipo de documento del dueño del vehículo
	 */
	public String getPersonaTipoDocumento() {
		return personaTipoDocumento;
	}

	/**
	 * Asigna el tipo de documento del dueño del vehículo.
	 *
	 * @param personaTipoDocumento tipo de documento del dueño del vehículo
	 */
	public void setPersonaTipoDocumento(String personaTipoDocumento) {
		this.personaTipoDocumento = personaTipoDocumento;
	}

	/**
	 * Devuelve el número de documento del dueño del vehículo.
	 *
	 * @return número de documento del dueño del vehículo
	 */
	public String getPersonaNumeroDocumento() {
		return personaNumeroDocumento;
	}

	/**
	 * Asigna el número de documento del dueño del vehículo.
	 *
	 * @param personaNumeroDocumento número de documento del dueño del vehículo
	 */
	public void setPersonaNumeroDocumento(String personaNumeroDocumento) {
		this.personaNumeroDocumento = personaNumeroDocumento;
	}

}
