package co.edu.unbosque.model;

import java.math.BigDecimal;

public class DetalleFactura {
	
	private int numeroFactura;
	private int numeroLinea;
	private int idConcepto;
	private BigDecimal valor;
	private Integer idAsignacion;
	private Integer idReserva;
	private Integer idSancion;
	
	public DetalleFactura() {
		
	}

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

	public int getNumeroFactura() {
		return numeroFactura;
	}

	public void setNumeroFactura(int numeroFactura) {
		this.numeroFactura = numeroFactura;
	}

	public int getNumeroLinea() {
		return numeroLinea;
	}

	public void setNumeroLinea(int numeroLinea) {
		this.numeroLinea = numeroLinea;
	}

	public int getIdConcepto() {
		return idConcepto;
	}

	public void setIdConcepto(int idConcepto) {
		this.idConcepto = idConcepto;
	}

	public BigDecimal getValor() {
		return valor;
	}

	public void setValor(BigDecimal valor) {
		this.valor = valor;
	}

	public Integer getIdAsignacion() {
		return idAsignacion;
	}

	public void setIdAsignacion(Integer idAsignacion) {
		this.idAsignacion = idAsignacion;
	}

	public Integer getIdReserva() {
		return idReserva;
	}

	public void setIdReserva(Integer idReserva) {
		this.idReserva = idReserva;
	}

	public Integer getIdSancion() {
		return idSancion;
	}

	public void setIdSancion(Integer idSancion) {
		this.idSancion = idSancion;
	}

}
