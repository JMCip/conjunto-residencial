package co.edu.unbosque.model;

/**
 * Visita de un visitante a una casa del conjunto.
 * <p>
 * Corresponde a la tabla {@code visita}.
 */
public class Visita {
	
	/** Identificador de la visita. */
	private int idVisita;
	/** Fecha de entrada ({@code AAAA-MM-DD}). */
	private String fechaEntrada;
	/** Fecha de salida ({@code AAAA-MM-DD}); puede ser {@code null}. */
	private String fechaSalida;
	/** Tipo de documento del visitante. */
	private String visitanteTipoDocumento;
	/** Número de documento del visitante. */
	private String visitanteNumeroDocumento;
	/** Número de la casa. */
	private int casaNumero;
	/** Manzana de la casa. */
	private int casaManzana;
	
	/** Crea una visita vacía. Lo usa Jackson para convertir el JSON del cuerpo de la petición. */
	public Visita() {
		
	}

	/**
	 * Crea una visita con todos sus datos.
	 *
	 * @param idVisita identificador de la visita
	 * @param fechaEntrada fecha de entrada ({@code AAAA-MM-DD})
	 * @param fechaSalida fecha de salida ({@code AAAA-MM-DD}); puede ser {@code null}
	 * @param visitanteTipoDocumento tipo de documento del visitante
	 * @param visitanteNumeroDocumento número de documento del visitante
	 * @param casaNumero número de la casa
	 * @param casaManzana manzana de la casa
	 */
	public Visita(int idVisita, String fechaEntrada, String fechaSalida, String visitanteTipoDocumento, String visitanteNumeroDocumento, int casaNumero, int casaManzana) {
		super();
		this.idVisita = idVisita;
		this.fechaEntrada = fechaEntrada;
		this.fechaSalida = fechaSalida;
		this.visitanteTipoDocumento = visitanteTipoDocumento;
		this.visitanteNumeroDocumento = visitanteNumeroDocumento;
		this.casaNumero = casaNumero;
		this.casaManzana = casaManzana;
	}

	/**
	 * Devuelve el identificador de la visita.
	 *
	 * @return identificador de la visita
	 */
	public int getIdVisita() {
		return idVisita;
	}

	/**
	 * Asigna el identificador de la visita.
	 *
	 * @param idVisita identificador de la visita
	 */
	public void setIdVisita(int idVisita) {
		this.idVisita = idVisita;
	}

	/**
	 * Devuelve la fecha de entrada ({@code AAAA-MM-DD}).
	 *
	 * @return fecha de entrada ({@code AAAA-MM-DD})
	 */
	public String getFechaEntrada() {
		return fechaEntrada;
	}

	/**
	 * Asigna la fecha de entrada ({@code AAAA-MM-DD}).
	 *
	 * @param fechaEntrada fecha de entrada ({@code AAAA-MM-DD})
	 */
	public void setFechaEntrada(String fechaEntrada) {
		this.fechaEntrada = fechaEntrada;
	}

	/**
	 * Devuelve la fecha de salida ({@code AAAA-MM-DD}); puede ser {@code null}.
	 *
	 * @return fecha de salida ({@code AAAA-MM-DD}); puede ser {@code null}
	 */
	public String getFechaSalida() {
		return fechaSalida;
	}

	/**
	 * Asigna la fecha de salida ({@code AAAA-MM-DD}); puede ser {@code null}.
	 *
	 * @param fechaSalida fecha de salida ({@code AAAA-MM-DD}); puede ser {@code null}
	 */
	public void setFechaSalida(String fechaSalida) {
		this.fechaSalida = fechaSalida;
	}

	/**
	 * Devuelve el tipo de documento del visitante.
	 *
	 * @return tipo de documento del visitante
	 */
	public String getVisitanteTipoDocumento() {
		return visitanteTipoDocumento;
	}

	/**
	 * Asigna el tipo de documento del visitante.
	 *
	 * @param visitanteTipoDocumento tipo de documento del visitante
	 */
	public void setVisitanteTipoDocumento(String visitanteTipoDocumento) {
		this.visitanteTipoDocumento = visitanteTipoDocumento;
	}

	/**
	 * Devuelve el número de documento del visitante.
	 *
	 * @return número de documento del visitante
	 */
	public String getVisitanteNumeroDocumento() {
		return visitanteNumeroDocumento;
	}

	/**
	 * Asigna el número de documento del visitante.
	 *
	 * @param visitanteNumeroDocumento número de documento del visitante
	 */
	public void setVisitanteNumeroDocumento(String visitanteNumeroDocumento) {
		this.visitanteNumeroDocumento = visitanteNumeroDocumento;
	}

	/**
	 * Devuelve el número de la casa.
	 *
	 * @return número de la casa
	 */
	public int getCasaNumero() {
		return casaNumero;
	}

	/**
	 * Asigna el número de la casa.
	 *
	 * @param casaNumero número de la casa
	 */
	public void setCasaNumero(int casaNumero) {
		this.casaNumero = casaNumero;
	}

	/**
	 * Devuelve la manzana de la casa.
	 *
	 * @return manzana de la casa
	 */
	public int getCasaManzana() {
		return casaManzana;
	}

	/**
	 * Asigna la manzana de la casa.
	 *
	 * @param casaManzana manzana de la casa
	 */
	public void setCasaManzana(int casaManzana) {
		this.casaManzana = casaManzana;
	}

}
