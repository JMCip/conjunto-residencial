package co.edu.unbosque.model;

import java.math.BigDecimal;

/**
 * Sanción impuesta a una casa por un incidente, con su proceso de descargos.
 * <p>
 * Corresponde a la tabla {@code sancion}. El proceso empieza {@code EN_DESCARGOS} y termina
 * {@code CONFIRMADA} (con valor de multa) o {@code ANULADA}.
 */
public class Sancion {
	
	/** Identificador de la sanción. */
	private int idSancion;
	/** Fecha del incidente ({@code AAAA-MM-DD}). */
	private String fechaIncidente;
	/** Fecha de notificación a la casa ({@code AAAA-MM-DD}). */
	private String fechaNotificacion;
	/** Tipo de sanción. */
	private String tipo;
	/** Descripción del incidente. */
	private String descripcion;
	/** Descargos presentados por la casa. */
	private String descargos;
	/** Estado del proceso: {@code EN_DESCARGOS}, {@code CONFIRMADA} o {@code ANULADA}. */
	private String estadoProceso;
	/** Valor de la multa; solo se define al confirmar la sanción. */
	private BigDecimal valor;
	/** Número de la casa. */
	private int casaNumero;
	/** Manzana de la casa. */
	private int casaManzana;
	
	/** Crea una sanción vacía. Lo usa Jackson para convertir el JSON del cuerpo de la petición. */
	public Sancion() {
		
	}

	/**
	 * Crea una sanción con todos sus datos.
	 *
	 * @param idSancion identificador de la sanción
	 * @param fechaIncidente fecha del incidente ({@code AAAA-MM-DD})
	 * @param fechaNotificacion fecha de notificación a la casa ({@code AAAA-MM-DD})
	 * @param tipo tipo de sanción
	 * @param descripcion descripción del incidente
	 * @param descargos descargos presentados por la casa
	 * @param estadoProceso estado del proceso: {@code EN_DESCARGOS}, {@code CONFIRMADA} o
	 *         {@code ANULADA}
	 * @param valor valor de la multa; solo se define al confirmar la sanción
	 * @param casaNumero número de la casa
	 * @param casaManzana manzana de la casa
	 */
	public Sancion(int idSancion, String fechaIncidente, String fechaNotificacion, String tipo, String descripcion, String descargos, String estadoProceso, BigDecimal valor, int casaNumero, int casaManzana) {
		super();
		this.idSancion = idSancion;
		this.fechaIncidente = fechaIncidente;
		this.fechaNotificacion = fechaNotificacion;
		this.tipo = tipo;
		this.descripcion = descripcion;
		this.descargos = descargos;
		this.estadoProceso = estadoProceso;
		this.valor = valor;
		this.casaNumero = casaNumero;
		this.casaManzana = casaManzana;
	}

	/**
	 * Devuelve el identificador de la sanción.
	 *
	 * @return identificador de la sanción
	 */
	public int getIdSancion() {
		return idSancion;
	}

	/**
	 * Asigna el identificador de la sanción.
	 *
	 * @param idSancion identificador de la sanción
	 */
	public void setIdSancion(int idSancion) {
		this.idSancion = idSancion;
	}

	/**
	 * Devuelve la fecha del incidente ({@code AAAA-MM-DD}).
	 *
	 * @return fecha del incidente ({@code AAAA-MM-DD})
	 */
	public String getFechaIncidente() {
		return fechaIncidente;
	}

	/**
	 * Asigna la fecha del incidente ({@code AAAA-MM-DD}).
	 *
	 * @param fechaIncidente fecha del incidente ({@code AAAA-MM-DD})
	 */
	public void setFechaIncidente(String fechaIncidente) {
		this.fechaIncidente = fechaIncidente;
	}

	/**
	 * Devuelve la fecha de notificación a la casa ({@code AAAA-MM-DD}).
	 *
	 * @return fecha de notificación a la casa ({@code AAAA-MM-DD})
	 */
	public String getFechaNotificacion() {
		return fechaNotificacion;
	}

	/**
	 * Asigna la fecha de notificación a la casa ({@code AAAA-MM-DD}).
	 *
	 * @param fechaNotificacion fecha de notificación a la casa ({@code AAAA-MM-DD})
	 */
	public void setFechaNotificacion(String fechaNotificacion) {
		this.fechaNotificacion = fechaNotificacion;
	}

	/**
	 * Devuelve el tipo de sanción.
	 *
	 * @return tipo de sanción
	 */
	public String getTipo() {
		return tipo;
	}

	/**
	 * Asigna el tipo de sanción.
	 *
	 * @param tipo tipo de sanción
	 */
	public void setTipo(String tipo) {
		this.tipo = tipo;
	}

	/**
	 * Devuelve la descripción del incidente.
	 *
	 * @return descripción del incidente
	 */
	public String getDescripcion() {
		return descripcion;
	}

	/**
	 * Asigna la descripción del incidente.
	 *
	 * @param descripcion descripción del incidente
	 */
	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	/**
	 * Devuelve el descargos presentados por la casa.
	 *
	 * @return descargos presentados por la casa
	 */
	public String getDescargos() {
		return descargos;
	}

	/**
	 * Asigna el descargos presentados por la casa.
	 *
	 * @param descargos descargos presentados por la casa
	 */
	public void setDescargos(String descargos) {
		this.descargos = descargos;
	}

	/**
	 * Devuelve el estado del proceso: {@code EN_DESCARGOS}, {@code CONFIRMADA} o {@code ANULADA}.
	 *
	 * @return estado del proceso: {@code EN_DESCARGOS}, {@code CONFIRMADA} o {@code ANULADA}
	 */
	public String getEstadoProceso() {
		return estadoProceso;
	}

	/**
	 * Asigna el estado del proceso: {@code EN_DESCARGOS}, {@code CONFIRMADA} o {@code ANULADA}.
	 *
	 * @param estadoProceso estado del proceso: {@code EN_DESCARGOS}, {@code CONFIRMADA} o
	 *         {@code ANULADA}
	 */
	public void setEstadoProceso(String estadoProceso) {
		this.estadoProceso = estadoProceso;
	}

	/**
	 * Devuelve el valor de la multa; solo se define al confirmar la sanción.
	 *
	 * @return valor de la multa; solo se define al confirmar la sanción
	 */
	public BigDecimal getValor() {
		return valor;
	}

	/**
	 * Asigna el valor de la multa; solo se define al confirmar la sanción.
	 *
	 * @param valor valor de la multa; solo se define al confirmar la sanción
	 */
	public void setValor(BigDecimal valor) {
		this.valor = valor;
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
