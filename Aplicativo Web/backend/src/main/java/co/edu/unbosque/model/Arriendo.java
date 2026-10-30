package co.edu.unbosque.model;

import java.math.BigDecimal;

public class Arriendo {
	
	private int idContrato;
	private BigDecimal cuotaMensual;
	private String fechaInicio;
	private String fechaFin;
	private String estadoContrato;
	private String arrendatarioTipoDocumento;
	private String arrendatarioNumeroDocumento;
	private int casaNumero;
	private int casaManzana;
	private String propietarioTipoDocumento;
	private String propietarioNumeroDocumento;
	
	public Arriendo() {
		
	}

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

	public int getIdContrato() {
		return idContrato;
	}

	public void setIdContrato(int idContrato) {
		this.idContrato = idContrato;
	}

	public BigDecimal getCuotaMensual() {
		return cuotaMensual;
	}

	public void setCuotaMensual(BigDecimal cuotaMensual) {
		this.cuotaMensual = cuotaMensual;
	}

	public String getFechaInicio() {
		return fechaInicio;
	}

	public void setFechaInicio(String fechaInicio) {
		this.fechaInicio = fechaInicio;
	}

	public String getFechaFin() {
		return fechaFin;
	}

	public void setFechaFin(String fechaFin) {
		this.fechaFin = fechaFin;
	}

	public String getEstadoContrato() {
		return estadoContrato;
	}

	public void setEstadoContrato(String estadoContrato) {
		this.estadoContrato = estadoContrato;
	}

	public String getArrendatarioTipoDocumento() {
		return arrendatarioTipoDocumento;
	}

	public void setArrendatarioTipoDocumento(String arrendatarioTipoDocumento) {
		this.arrendatarioTipoDocumento = arrendatarioTipoDocumento;
	}

	public String getArrendatarioNumeroDocumento() {
		return arrendatarioNumeroDocumento;
	}

	public void setArrendatarioNumeroDocumento(String arrendatarioNumeroDocumento) {
		this.arrendatarioNumeroDocumento = arrendatarioNumeroDocumento;
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
