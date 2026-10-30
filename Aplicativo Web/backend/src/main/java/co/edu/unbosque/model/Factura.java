package co.edu.unbosque.model;

import java.math.BigDecimal;

public class Factura {
	
	private int numeroFactura;
	private String periodo;
	private String fechaEmision;
	private String fechaVencimiento;
	private String estado;
	private BigDecimal total;
	private int casaNumero;
	private int casaManzana;
	private BigDecimal totalPagado;
	private BigDecimal saldo;
	
	public Factura() {
		
	}

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

	public int getNumeroFactura() {
		return numeroFactura;
	}

	public void setNumeroFactura(int numeroFactura) {
		this.numeroFactura = numeroFactura;
	}

	public String getPeriodo() {
		return periodo;
	}

	public void setPeriodo(String periodo) {
		this.periodo = periodo;
	}

	public String getFechaEmision() {
		return fechaEmision;
	}

	public void setFechaEmision(String fechaEmision) {
		this.fechaEmision = fechaEmision;
	}

	public String getFechaVencimiento() {
		return fechaVencimiento;
	}

	public void setFechaVencimiento(String fechaVencimiento) {
		this.fechaVencimiento = fechaVencimiento;
	}

	public String getEstado() {
		return estado;
	}

	public void setEstado(String estado) {
		this.estado = estado;
	}

	public BigDecimal getTotal() {
		return total;
	}

	public void setTotal(BigDecimal total) {
		this.total = total;
	}

	public int getCasaNumero() {
		return casaNumero;
	}

	public void setCasaNumero(int casaNumero) {
		this.casaNumero = casaNumero;
	}

	public int getCasaManzana() {
		return casaManzana;
	}

	public void setCasaManzana(int casaManzana) {
		this.casaManzana = casaManzana;
	}

	public BigDecimal getTotalPagado() {
		return totalPagado;
	}

	public void setTotalPagado(BigDecimal totalPagado) {
		this.totalPagado = totalPagado;
	}

	public BigDecimal getSaldo() {
		return saldo;
	}

	public void setSaldo(BigDecimal saldo) {
		this.saldo = saldo;
	}

}
