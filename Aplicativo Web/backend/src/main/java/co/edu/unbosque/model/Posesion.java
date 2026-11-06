package co.edu.unbosque.model;

import java.math.BigDecimal;

/**
 * Porcentaje de propiedad que un propietario tiene sobre una casa.
 * <p>
 * Corresponde a la tabla {@code posesion}. Una posesión sin fecha de venta está vigente.
 */
public class Posesion {
	
	/** Identificador de la posesión. */
	private int idPosesion;
	/** Porcentaje de propiedad sobre la casa (mayor que 0 y máximo 100). */
	private BigDecimal porcentaje;
	/** Fecha de compra ({@code AAAA-MM-DD}). */
	private String fechaCompra;
	/** Fecha de venta ({@code AAAA-MM-DD}); {@code null} mientras la posesión esté vigente. */
	private String fechaVenta;
	/** Número de la casa. */
	private int casaNumero;
	/** Manzana de la casa. */
	private int casaManzana;
	/** Tipo de documento del propietario. */
	private String propietarioTipoDocumento;
	/** Número de documento del propietario. */
	private String propietarioNumeroDocumento;
	
	/** Crea una posesión vacía. Lo usa Jackson para convertir el JSON del cuerpo de la petición. */
	public Posesion() {
		
	}

	/**
	 * Crea una posesión con todos sus datos.
	 *
	 * @param idPosesion identificador de la posesión
	 * @param porcentaje porcentaje de propiedad sobre la casa (mayor que 0 y máximo 100)
	 * @param fechaCompra fecha de compra ({@code AAAA-MM-DD})
	 * @param fechaVenta fecha de venta ({@code AAAA-MM-DD}); {@code null} mientras la posesión esté
	 *         vigente
	 * @param casaNumero número de la casa
	 * @param casaManzana manzana de la casa
	 * @param propietarioTipoDocumento tipo de documento del propietario
	 * @param propietarioNumeroDocumento número de documento del propietario
	 */
	public Posesion(int idPosesion, BigDecimal porcentaje, String fechaCompra, String fechaVenta, int casaNumero, int casaManzana, String propietarioTipoDocumento, String propietarioNumeroDocumento) {
		super();
		this.idPosesion = idPosesion;
		this.porcentaje = porcentaje;
		this.fechaCompra = fechaCompra;
		this.fechaVenta = fechaVenta;
		this.casaNumero = casaNumero;
		this.casaManzana = casaManzana;
		this.propietarioTipoDocumento = propietarioTipoDocumento;
		this.propietarioNumeroDocumento = propietarioNumeroDocumento;
	}

	/**
	 * Devuelve el identificador de la posesión.
	 *
	 * @return identificador de la posesión
	 */
	public int getIdPosesion() {
		return idPosesion;
	}

	/**
	 * Asigna el identificador de la posesión.
	 *
	 * @param idPosesion identificador de la posesión
	 */
	public void setIdPosesion(int idPosesion) {
		this.idPosesion = idPosesion;
	}

	/**
	 * Devuelve el porcentaje de propiedad sobre la casa (mayor que 0 y máximo 100).
	 *
	 * @return porcentaje de propiedad sobre la casa (mayor que 0 y máximo 100)
	 */
	public BigDecimal getPorcentaje() {
		return porcentaje;
	}

	/**
	 * Asigna el porcentaje de propiedad sobre la casa (mayor que 0 y máximo 100).
	 *
	 * @param porcentaje porcentaje de propiedad sobre la casa (mayor que 0 y máximo 100)
	 */
	public void setPorcentaje(BigDecimal porcentaje) {
		this.porcentaje = porcentaje;
	}

	/**
	 * Devuelve la fecha de compra ({@code AAAA-MM-DD}).
	 *
	 * @return fecha de compra ({@code AAAA-MM-DD})
	 */
	public String getFechaCompra() {
		return fechaCompra;
	}

	/**
	 * Asigna la fecha de compra ({@code AAAA-MM-DD}).
	 *
	 * @param fechaCompra fecha de compra ({@code AAAA-MM-DD})
	 */
	public void setFechaCompra(String fechaCompra) {
		this.fechaCompra = fechaCompra;
	}

	/**
	 * Devuelve la fecha de venta ({@code AAAA-MM-DD}); {@code null} mientras la posesión esté vigente.
	 *
	 * @return fecha de venta ({@code AAAA-MM-DD}); {@code null} mientras la posesión esté vigente
	 */
	public String getFechaVenta() {
		return fechaVenta;
	}

	/**
	 * Asigna la fecha de venta ({@code AAAA-MM-DD}); {@code null} mientras la posesión esté vigente.
	 *
	 * @param fechaVenta fecha de venta ({@code AAAA-MM-DD}); {@code null} mientras la posesión esté
	 *         vigente
	 */
	public void setFechaVenta(String fechaVenta) {
		this.fechaVenta = fechaVenta;
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

	/**
	 * Devuelve el tipo de documento del propietario.
	 *
	 * @return tipo de documento del propietario
	 */
	public String getPropietarioTipoDocumento() {
		return propietarioTipoDocumento;
	}

	/**
	 * Asigna el tipo de documento del propietario.
	 *
	 * @param propietarioTipoDocumento tipo de documento del propietario
	 */
	public void setPropietarioTipoDocumento(String propietarioTipoDocumento) {
		this.propietarioTipoDocumento = propietarioTipoDocumento;
	}

	/**
	 * Devuelve el número de documento del propietario.
	 *
	 * @return número de documento del propietario
	 */
	public String getPropietarioNumeroDocumento() {
		return propietarioNumeroDocumento;
	}

	/**
	 * Asigna el número de documento del propietario.
	 *
	 * @param propietarioNumeroDocumento número de documento del propietario
	 */
	public void setPropietarioNumeroDocumento(String propietarioNumeroDocumento) {
		this.propietarioNumeroDocumento = propietarioNumeroDocumento;
	}

}
