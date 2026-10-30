package co.edu.unbosque.model;

import java.math.BigDecimal;

public class Pago {
	
	private int idPago;
	private String fecha;
	private BigDecimal valor;
	private String metodoPago;
	private int numeroFactura;
	private String personaTipoDocumento;
	private String personaNumeroDocumento;
	
	public Pago() {
		
	}

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

	public int getIdPago() {
		return idPago;
	}

	public void setIdPago(int idPago) {
		this.idPago = idPago;
	}

	public String getFecha() {
		return fecha;
	}

	public void setFecha(String fecha) {
		this.fecha = fecha;
	}

	public BigDecimal getValor() {
		return valor;
	}

	public void setValor(BigDecimal valor) {
		this.valor = valor;
	}

	public String getMetodoPago() {
		return metodoPago;
	}

	public void setMetodoPago(String metodoPago) {
		this.metodoPago = metodoPago;
	}

	public int getNumeroFactura() {
		return numeroFactura;
	}

	public void setNumeroFactura(int numeroFactura) {
		this.numeroFactura = numeroFactura;
	}

	public String getPersonaTipoDocumento() {
		return personaTipoDocumento;
	}

	public void setPersonaTipoDocumento(String personaTipoDocumento) {
		this.personaTipoDocumento = personaTipoDocumento;
	}

	public String getPersonaNumeroDocumento() {
		return personaNumeroDocumento;
	}

	public void setPersonaNumeroDocumento(String personaNumeroDocumento) {
		this.personaNumeroDocumento = personaNumeroDocumento;
	}

}
