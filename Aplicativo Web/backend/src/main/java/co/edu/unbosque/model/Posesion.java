package co.edu.unbosque.model;

import java.math.BigDecimal;

public class Posesion {
	
	private int idPosesion;
	private BigDecimal porcentaje;
	private String fechaCompra;
	private String fechaVenta;
	private int casaNumero;
	private int casaManzana;
	private String propietarioTipoDocumento;
	private String propietarioNumeroDocumento;
	
	public Posesion() {
		
	}

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

	public int getIdPosesion() {
		return idPosesion;
	}

	public void setIdPosesion(int idPosesion) {
		this.idPosesion = idPosesion;
	}

	public BigDecimal getPorcentaje() {
		return porcentaje;
	}

	public void setPorcentaje(BigDecimal porcentaje) {
		this.porcentaje = porcentaje;
	}

	public String getFechaCompra() {
		return fechaCompra;
	}

	public void setFechaCompra(String fechaCompra) {
		this.fechaCompra = fechaCompra;
	}

	public String getFechaVenta() {
		return fechaVenta;
	}

	public void setFechaVenta(String fechaVenta) {
		this.fechaVenta = fechaVenta;
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

	public String getPropietarioTipoDocumento() {
		return propietarioTipoDocumento;
	}

	public void setPropietarioTipoDocumento(String propietarioTipoDocumento) {
		this.propietarioTipoDocumento = propietarioTipoDocumento;
	}

	public String getPropietarioNumeroDocumento() {
		return propietarioNumeroDocumento;
	}

	public void setPropietarioNumeroDocumento(String propietarioNumeroDocumento) {
		this.propietarioNumeroDocumento = propietarioNumeroDocumento;
	}

}
