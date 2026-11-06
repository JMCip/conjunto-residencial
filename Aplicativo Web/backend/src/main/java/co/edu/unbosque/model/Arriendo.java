package co.edu.unbosque.model;

import java.math.BigDecimal;

/**
 * Contrato de arriendo de una casa, firmado entre un propietario vigente y un arrendatario.
 * <p>
 * Corresponde a la tabla {@code arriendo}. Las fechas viajan como texto en formato
 * {@code AAAA-MM-DD}.
 */
public class Arriendo {
	
	/** Identificador del contrato. */
	private int idContrato;
	/** Valor de la cuota mensual del arriendo. */
	private BigDecimal cuotaMensual;
	/** Fecha de inicio ({@code AAAA-MM-DD}). */
	private String fechaInicio;
	/** Fecha de fin ({@code AAAA-MM-DD}); {@code null} mientras esté vigente. */
	private String fechaFin;
	/** Estado del contrato: {@code VIGENTE} o {@code FINALIZADO}. */
	private String estadoContrato;
	/** Tipo de documento del arrendatario. */
	private String arrendatarioTipoDocumento;
	/** Número de documento del arrendatario. */
	private String arrendatarioNumeroDocumento;
	/** Número de la casa. */
	private int casaNumero;
	/** Manzana de la casa. */
	private int casaManzana;
	/** Tipo de documento del propietario. */
	private String propietarioTipoDocumento;
	/** Número de documento del propietario. */
	private String propietarioNumeroDocumento;
	
	/**
	 * Crea un contrato de arriendo vacío. Lo usa Jackson para convertir el JSON del cuerpo de la
	 * petición.
	 */
	public Arriendo() {
		
	}

	/**
	 * Crea un contrato de arriendo con todos sus datos.
	 *
	 * @param idContrato identificador del contrato
	 * @param cuotaMensual valor de la cuota mensual del arriendo
	 * @param fechaInicio fecha de inicio ({@code AAAA-MM-DD})
	 * @param fechaFin fecha de fin ({@code AAAA-MM-DD}); {@code null} mientras esté vigente
	 * @param estadoContrato estado del contrato: {@code VIGENTE} o {@code FINALIZADO}
	 * @param arrendatarioTipoDocumento tipo de documento del arrendatario
	 * @param arrendatarioNumeroDocumento número de documento del arrendatario
	 * @param casaNumero número de la casa
	 * @param casaManzana manzana de la casa
	 * @param propietarioTipoDocumento tipo de documento del propietario
	 * @param propietarioNumeroDocumento número de documento del propietario
	 */
	public Arriendo(int idContrato, BigDecimal cuotaMensual, String fechaInicio, String fechaFin, String estadoContrato, String arrendatarioTipoDocumento, String arrendatarioNumeroDocumento, int casaNumero, int casaManzana, String propietarioTipoDocumento, String propietarioNumeroDocumento) {
		super();
		this.idContrato = idContrato;
		this.cuotaMensual = cuotaMensual;
		this.fechaInicio = fechaInicio;
		this.fechaFin = fechaFin;
		this.estadoContrato = estadoContrato;
		this.arrendatarioTipoDocumento = arrendatarioTipoDocumento;
		this.arrendatarioNumeroDocumento = arrendatarioNumeroDocumento;
		this.casaNumero = casaNumero;
		this.casaManzana = casaManzana;
		this.propietarioTipoDocumento = propietarioTipoDocumento;
		this.propietarioNumeroDocumento = propietarioNumeroDocumento;
	}

	/**
	 * Devuelve el identificador del contrato.
	 *
	 * @return identificador del contrato
	 */
	public int getIdContrato() {
		return idContrato;
	}

	/**
	 * Asigna el identificador del contrato.
	 *
	 * @param idContrato identificador del contrato
	 */
	public void setIdContrato(int idContrato) {
		this.idContrato = idContrato;
	}

	/**
	 * Devuelve el valor de la cuota mensual del arriendo.
	 *
	 * @return valor de la cuota mensual del arriendo
	 */
	public BigDecimal getCuotaMensual() {
		return cuotaMensual;
	}

	/**
	 * Asigna el valor de la cuota mensual del arriendo.
	 *
	 * @param cuotaMensual valor de la cuota mensual del arriendo
	 */
	public void setCuotaMensual(BigDecimal cuotaMensual) {
		this.cuotaMensual = cuotaMensual;
	}

	/**
	 * Devuelve la fecha de inicio ({@code AAAA-MM-DD}).
	 *
	 * @return fecha de inicio ({@code AAAA-MM-DD})
	 */
	public String getFechaInicio() {
		return fechaInicio;
	}

	/**
	 * Asigna la fecha de inicio ({@code AAAA-MM-DD}).
	 *
	 * @param fechaInicio fecha de inicio ({@code AAAA-MM-DD})
	 */
	public void setFechaInicio(String fechaInicio) {
		this.fechaInicio = fechaInicio;
	}

	/**
	 * Devuelve la fecha de fin ({@code AAAA-MM-DD}); {@code null} mientras esté vigente.
	 *
	 * @return fecha de fin ({@code AAAA-MM-DD}); {@code null} mientras esté vigente
	 */
	public String getFechaFin() {
		return fechaFin;
	}

	/**
	 * Asigna la fecha de fin ({@code AAAA-MM-DD}); {@code null} mientras esté vigente.
	 *
	 * @param fechaFin fecha de fin ({@code AAAA-MM-DD}); {@code null} mientras esté vigente
	 */
	public void setFechaFin(String fechaFin) {
		this.fechaFin = fechaFin;
	}

	/**
	 * Devuelve el estado del contrato: {@code VIGENTE} o {@code FINALIZADO}.
	 *
	 * @return estado del contrato: {@code VIGENTE} o {@code FINALIZADO}
	 */
	public String getEstadoContrato() {
		return estadoContrato;
	}

	/**
	 * Asigna el estado del contrato: {@code VIGENTE} o {@code FINALIZADO}.
	 *
	 * @param estadoContrato estado del contrato: {@code VIGENTE} o {@code FINALIZADO}
	 */
	public void setEstadoContrato(String estadoContrato) {
		this.estadoContrato = estadoContrato;
	}

	/**
	 * Devuelve el tipo de documento del arrendatario.
	 *
	 * @return tipo de documento del arrendatario
	 */
	public String getArrendatarioTipoDocumento() {
		return arrendatarioTipoDocumento;
	}

	/**
	 * Asigna el tipo de documento del arrendatario.
	 *
	 * @param arrendatarioTipoDocumento tipo de documento del arrendatario
	 */
	public void setArrendatarioTipoDocumento(String arrendatarioTipoDocumento) {
		this.arrendatarioTipoDocumento = arrendatarioTipoDocumento;
	}

	/**
	 * Devuelve el número de documento del arrendatario.
	 *
	 * @return número de documento del arrendatario
	 */
	public String getArrendatarioNumeroDocumento() {
		return arrendatarioNumeroDocumento;
	}

	/**
	 * Asigna el número de documento del arrendatario.
	 *
	 * @param arrendatarioNumeroDocumento número de documento del arrendatario
	 */
	public void setArrendatarioNumeroDocumento(String arrendatarioNumeroDocumento) {
		this.arrendatarioNumeroDocumento = arrendatarioNumeroDocumento;
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
