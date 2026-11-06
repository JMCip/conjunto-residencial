package co.edu.unbosque.model;

import java.math.BigDecimal;

/**
 * Pago (total o parcial) de una factura, realizado por una persona.
 * <p>
 * Corresponde a la tabla {@code pago}.
 */
public class Pago {
	
	/** Identificador del pago. */
	private int idPago;
	/** Fecha del pago ({@code AAAA-MM-DD}). */
	private String fecha;
	/** Valor pagado. */
	private BigDecimal valor;
	/** Método de pago: {@code EFECTIVO}, {@code TRANSFERENCIA}, {@code PSE} o {@code TARJETA}. */
	private String metodoPago;
	/** Número de la factura que se paga. */
	private int numeroFactura;
	/** Tipo de documento de la persona que paga. */
	private String personaTipoDocumento;
	/** Número de documento de la persona que paga. */
	private String personaNumeroDocumento;
	
	/** Crea un pago vacío. Lo usa Jackson para convertir el JSON del cuerpo de la petición. */
	public Pago() {
		
	}

	/**
	 * Crea un pago con todos sus datos.
	 *
	 * @param idPago identificador del pago
	 * @param fecha fecha del pago ({@code AAAA-MM-DD})
	 * @param valor valor pagado
	 * @param metodoPago método de pago: {@code EFECTIVO}, {@code TRANSFERENCIA}, {@code PSE} o
	 *         {@code TARJETA}
	 * @param numeroFactura número de la factura que se paga
	 * @param personaTipoDocumento tipo de documento de la persona que paga
	 * @param personaNumeroDocumento número de documento de la persona que paga
	 */
	public Pago(int idPago, String fecha, BigDecimal valor, String metodoPago, int numeroFactura, String personaTipoDocumento, String personaNumeroDocumento) {
		super();
		this.idPago = idPago;
		this.fecha = fecha;
		this.valor = valor;
		this.metodoPago = metodoPago;
		this.numeroFactura = numeroFactura;
		this.personaTipoDocumento = personaTipoDocumento;
		this.personaNumeroDocumento = personaNumeroDocumento;
	}

	/**
	 * Devuelve el identificador del pago.
	 *
	 * @return identificador del pago
	 */
	public int getIdPago() {
		return idPago;
	}

	/**
	 * Asigna el identificador del pago.
	 *
	 * @param idPago identificador del pago
	 */
	public void setIdPago(int idPago) {
		this.idPago = idPago;
	}

	/**
	 * Devuelve la fecha del pago ({@code AAAA-MM-DD}).
	 *
	 * @return fecha del pago ({@code AAAA-MM-DD})
	 */
	public String getFecha() {
		return fecha;
	}

	/**
	 * Asigna la fecha del pago ({@code AAAA-MM-DD}).
	 *
	 * @param fecha fecha del pago ({@code AAAA-MM-DD})
	 */
	public void setFecha(String fecha) {
		this.fecha = fecha;
	}

	/**
	 * Devuelve el valor pagado.
	 *
	 * @return valor pagado
	 */
	public BigDecimal getValor() {
		return valor;
	}

	/**
	 * Asigna el valor pagado.
	 *
	 * @param valor valor pagado
	 */
	public void setValor(BigDecimal valor) {
		this.valor = valor;
	}

	/**
	 * Devuelve el método de pago: {@code EFECTIVO}, {@code TRANSFERENCIA}, {@code PSE} o
	 * {@code TARJETA}.
	 *
	 * @return método de pago: {@code EFECTIVO}, {@code TRANSFERENCIA}, {@code PSE} o {@code TARJETA}
	 */
	public String getMetodoPago() {
		return metodoPago;
	}

	/**
	 * Asigna el método de pago: {@code EFECTIVO}, {@code TRANSFERENCIA}, {@code PSE} o
	 * {@code TARJETA}.
	 *
	 * @param metodoPago método de pago: {@code EFECTIVO}, {@code TRANSFERENCIA}, {@code PSE} o
	 *         {@code TARJETA}
	 */
	public void setMetodoPago(String metodoPago) {
		this.metodoPago = metodoPago;
	}

	/**
	 * Devuelve el número de la factura que se paga.
	 *
	 * @return número de la factura que se paga
	 */
	public int getNumeroFactura() {
		return numeroFactura;
	}

	/**
	 * Asigna el número de la factura que se paga.
	 *
	 * @param numeroFactura número de la factura que se paga
	 */
	public void setNumeroFactura(int numeroFactura) {
		this.numeroFactura = numeroFactura;
	}

	/**
	 * Devuelve el tipo de documento de la persona que paga.
	 *
	 * @return tipo de documento de la persona que paga
	 */
	public String getPersonaTipoDocumento() {
		return personaTipoDocumento;
	}

	/**
	 * Asigna el tipo de documento de la persona que paga.
	 *
	 * @param personaTipoDocumento tipo de documento de la persona que paga
	 */
	public void setPersonaTipoDocumento(String personaTipoDocumento) {
		this.personaTipoDocumento = personaTipoDocumento;
	}

	/**
	 * Devuelve el número de documento de la persona que paga.
	 *
	 * @return número de documento de la persona que paga
	 */
	public String getPersonaNumeroDocumento() {
		return personaNumeroDocumento;
	}

	/**
	 * Asigna el número de documento de la persona que paga.
	 *
	 * @param personaNumeroDocumento número de documento de la persona que paga
	 */
	public void setPersonaNumeroDocumento(String personaNumeroDocumento) {
		this.personaNumeroDocumento = personaNumeroDocumento;
	}

}
