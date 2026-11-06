package co.edu.unbosque.model;

import java.math.BigDecimal;

/**
 * Factura mensual de una casa.
 * <p>
 * Corresponde a la tabla {@code factura}. El total, lo pagado y el saldo no se guardan en la tabla:
 * se calculan en la consulta a partir de las líneas de detalle y de los pagos.
 */
public class Factura {
	
	/** Número de la factura. */
	private int numeroFactura;
	/** Periodo facturado ({@code AAAA-MM}). */
	private String periodo;
	/** Fecha de emisión ({@code AAAA-MM-DD}). */
	private String fechaEmision;
	/** Fecha de vencimiento ({@code AAAA-MM-DD}). */
	private String fechaVencimiento;
	/** Estado de la factura: {@code PENDIENTE}, {@code ABONADA}, {@code PAGADA} o {@code VENCIDA}. */
	private String estado;
	/** Total de la factura (suma de sus líneas). */
	private BigDecimal total;
	/** Número de la casa. */
	private int casaNumero;
	/** Manzana de la casa. */
	private int casaManzana;
	/** Suma de los pagos de la factura. */
	private BigDecimal totalPagado;
	/** Saldo pendiente (total menos lo pagado). */
	private BigDecimal saldo;
	
	/** Crea una factura vacía. Lo usa Jackson para convertir el JSON del cuerpo de la petición. */
	public Factura() {
		
	}

	/**
	 * Crea una factura con todos sus datos.
	 *
	 * @param numeroFactura número de la factura
	 * @param periodo periodo facturado ({@code AAAA-MM})
	 * @param fechaEmision fecha de emisión ({@code AAAA-MM-DD})
	 * @param fechaVencimiento fecha de vencimiento ({@code AAAA-MM-DD})
	 * @param estado estado de la factura: {@code PENDIENTE}, {@code ABONADA}, {@code PAGADA} o
	 *         {@code VENCIDA}
	 * @param total total de la factura (suma de sus líneas)
	 * @param casaNumero número de la casa
	 * @param casaManzana manzana de la casa
	 * @param totalPagado suma de los pagos de la factura
	 * @param saldo saldo pendiente (total menos lo pagado)
	 */
	public Factura(int numeroFactura, String periodo, String fechaEmision, String fechaVencimiento, String estado, BigDecimal total, int casaNumero, int casaManzana, BigDecimal totalPagado, BigDecimal saldo) {
		super();
		this.numeroFactura = numeroFactura;
		this.periodo = periodo;
		this.fechaEmision = fechaEmision;
		this.fechaVencimiento = fechaVencimiento;
		this.estado = estado;
		this.total = total;
		this.casaNumero = casaNumero;
		this.casaManzana = casaManzana;
		this.totalPagado = totalPagado;
		this.saldo = saldo;
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
	 * Devuelve el periodo facturado ({@code AAAA-MM}).
	 *
	 * @return periodo facturado ({@code AAAA-MM})
	 */
	public String getPeriodo() {
		return periodo;
	}

	/**
	 * Asigna el periodo facturado ({@code AAAA-MM}).
	 *
	 * @param periodo periodo facturado ({@code AAAA-MM})
	 */
	public void setPeriodo(String periodo) {
		this.periodo = periodo;
	}

	/**
	 * Devuelve la fecha de emisión ({@code AAAA-MM-DD}).
	 *
	 * @return fecha de emisión ({@code AAAA-MM-DD})
	 */
	public String getFechaEmision() {
		return fechaEmision;
	}

	/**
	 * Asigna la fecha de emisión ({@code AAAA-MM-DD}).
	 *
	 * @param fechaEmision fecha de emisión ({@code AAAA-MM-DD})
	 */
	public void setFechaEmision(String fechaEmision) {
		this.fechaEmision = fechaEmision;
	}

	/**
	 * Devuelve la fecha de vencimiento ({@code AAAA-MM-DD}).
	 *
	 * @return fecha de vencimiento ({@code AAAA-MM-DD})
	 */
	public String getFechaVencimiento() {
		return fechaVencimiento;
	}

	/**
	 * Asigna la fecha de vencimiento ({@code AAAA-MM-DD}).
	 *
	 * @param fechaVencimiento fecha de vencimiento ({@code AAAA-MM-DD})
	 */
	public void setFechaVencimiento(String fechaVencimiento) {
		this.fechaVencimiento = fechaVencimiento;
	}

	/**
	 * Devuelve el estado de la factura: {@code PENDIENTE}, {@code ABONADA}, {@code PAGADA} o
	 * {@code VENCIDA}.
	 *
	 * @return estado de la factura: {@code PENDIENTE}, {@code ABONADA}, {@code PAGADA} o
	 *         {@code VENCIDA}
	 */
	public String getEstado() {
		return estado;
	}

	/**
	 * Asigna el estado de la factura: {@code PENDIENTE}, {@code ABONADA}, {@code PAGADA} o
	 * {@code VENCIDA}.
	 *
	 * @param estado estado de la factura: {@code PENDIENTE}, {@code ABONADA}, {@code PAGADA} o
	 *         {@code VENCIDA}
	 */
	public void setEstado(String estado) {
		this.estado = estado;
	}

	/**
	 * Devuelve el total de la factura (suma de sus líneas).
	 *
	 * @return total de la factura (suma de sus líneas)
	 */
	public BigDecimal getTotal() {
		return total;
	}

	/**
	 * Asigna el total de la factura (suma de sus líneas).
	 *
	 * @param total total de la factura (suma de sus líneas)
	 */
	public void setTotal(BigDecimal total) {
		this.total = total;
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
	 * Devuelve el suma de los pagos de la factura.
	 *
	 * @return suma de los pagos de la factura
	 */
	public BigDecimal getTotalPagado() {
		return totalPagado;
	}

	/**
	 * Asigna el suma de los pagos de la factura.
	 *
	 * @param totalPagado suma de los pagos de la factura
	 */
	public void setTotalPagado(BigDecimal totalPagado) {
		this.totalPagado = totalPagado;
	}

	/**
	 * Devuelve el saldo pendiente (total menos lo pagado).
	 *
	 * @return saldo pendiente (total menos lo pagado)
	 */
	public BigDecimal getSaldo() {
		return saldo;
	}

	/**
	 * Asigna el saldo pendiente (total menos lo pagado).
	 *
	 * @param saldo saldo pendiente (total menos lo pagado)
	 */
	public void setSaldo(BigDecimal saldo) {
		this.saldo = saldo;
	}

}
