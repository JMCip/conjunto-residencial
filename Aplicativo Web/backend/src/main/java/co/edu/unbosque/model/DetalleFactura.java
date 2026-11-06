package co.edu.unbosque.model;

import java.math.BigDecimal;

/**
 * Línea de una factura: un concepto cobrado con su valor.
 * <p>
 * Corresponde a la tabla {@code detalle_factura}. Una línea puede estar ligada a lo sumo a una
 * sanción, una reserva del salón o una asignación de celda, que es el origen del cobro.
 */
public class DetalleFactura {
	
	/** Número de la factura. */
	private int numeroFactura;
	/** Número de la línea dentro de la factura. */
	private int numeroLinea;
	/** Identificador del concepto de cobro. */
	private int idConcepto;
	/** Valor cobrado en la línea. */
	private BigDecimal valor;
	/** Asignación de celda que origina el cobro; puede ser {@code null}. */
	private Integer idAsignacion;
	/** Reserva del salón que origina el cobro; puede ser {@code null}. */
	private Integer idReserva;
	/** Sanción que origina el cobro; puede ser {@code null}. */
	private Integer idSancion;
	
	/**
	 * Crea una línea de factura vacía. Lo usa Jackson para convertir el JSON del cuerpo de la
	 * petición.
	 */
	public DetalleFactura() {
		
	}

	/**
	 * Crea una línea de factura con todos sus datos.
	 *
	 * @param numeroFactura número de la factura
	 * @param numeroLinea número de la línea dentro de la factura
	 * @param idConcepto identificador del concepto de cobro
	 * @param valor valor cobrado en la línea
	 * @param idAsignacion asignación de celda que origina el cobro; puede ser {@code null}
	 * @param idReserva reserva del salón que origina el cobro; puede ser {@code null}
	 * @param idSancion sanción que origina el cobro; puede ser {@code null}
	 */
	public DetalleFactura(int numeroFactura, int numeroLinea, int idConcepto, BigDecimal valor, Integer idAsignacion, Integer idReserva, Integer idSancion) {
		super();
		this.numeroFactura = numeroFactura;
		this.numeroLinea = numeroLinea;
		this.idConcepto = idConcepto;
		this.valor = valor;
		this.idAsignacion = idAsignacion;
		this.idReserva = idReserva;
		this.idSancion = idSancion;
	}

	/**
	 * Devuelve el número de la factura.
	 *
	 * @return número de la factura
	 */
	public int getNumeroFactura() {
		return numeroFactura;
	}

	/**
	 * Asigna el número de la factura.
	 *
	 * @param numeroFactura número de la factura
	 */
	public void setNumeroFactura(int numeroFactura) {
		this.numeroFactura = numeroFactura;
	}

	/**
	 * Devuelve el número de la línea dentro de la factura.
	 *
	 * @return número de la línea dentro de la factura
	 */
	public int getNumeroLinea() {
		return numeroLinea;
	}

	/**
	 * Asigna el número de la línea dentro de la factura.
	 *
	 * @param numeroLinea número de la línea dentro de la factura
	 */
	public void setNumeroLinea(int numeroLinea) {
		this.numeroLinea = numeroLinea;
	}

	/**
	 * Devuelve el identificador del concepto de cobro.
	 *
	 * @return identificador del concepto de cobro
	 */
	public int getIdConcepto() {
		return idConcepto;
	}

	/**
	 * Asigna el identificador del concepto de cobro.
	 *
	 * @param idConcepto identificador del concepto de cobro
	 */
	public void setIdConcepto(int idConcepto) {
		this.idConcepto = idConcepto;
	}

	/**
	 * Devuelve el valor cobrado en la línea.
	 *
	 * @return valor cobrado en la línea
	 */
	public BigDecimal getValor() {
		return valor;
	}

	/**
	 * Asigna el valor cobrado en la línea.
	 *
	 * @param valor valor cobrado en la línea
	 */
	public void setValor(BigDecimal valor) {
		this.valor = valor;
	}

	/**
	 * Devuelve la asignación de celda que origina el cobro; puede ser {@code null}.
	 *
	 * @return asignación de celda que origina el cobro; puede ser {@code null}
	 */
	public Integer getIdAsignacion() {
		return idAsignacion;
	}

	/**
	 * Asigna la asignación de celda que origina el cobro; puede ser {@code null}.
	 *
	 * @param idAsignacion asignación de celda que origina el cobro; puede ser {@code null}
	 */
	public void setIdAsignacion(Integer idAsignacion) {
		this.idAsignacion = idAsignacion;
	}

	/**
	 * Devuelve el reserva del salón que origina el cobro; puede ser {@code null}.
	 *
	 * @return reserva del salón que origina el cobro; puede ser {@code null}
	 */
	public Integer getIdReserva() {
		return idReserva;
	}

	/**
	 * Asigna el reserva del salón que origina el cobro; puede ser {@code null}.
	 *
	 * @param idReserva reserva del salón que origina el cobro; puede ser {@code null}
	 */
	public void setIdReserva(Integer idReserva) {
		this.idReserva = idReserva;
	}

	/**
	 * Devuelve la sanción que origina el cobro; puede ser {@code null}.
	 *
	 * @return sanción que origina el cobro; puede ser {@code null}
	 */
	public Integer getIdSancion() {
		return idSancion;
	}

	/**
	 * Asigna la sanción que origina el cobro; puede ser {@code null}.
	 *
	 * @param idSancion sanción que origina el cobro; puede ser {@code null}
	 */
	public void setIdSancion(Integer idSancion) {
		this.idSancion = idSancion;
	}

}
