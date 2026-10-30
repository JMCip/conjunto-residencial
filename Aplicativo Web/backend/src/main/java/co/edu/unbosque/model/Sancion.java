package co.edu.unbosque.model;

import java.math.BigDecimal;

public class Sancion {
	
	private int idSancion;
	private String fechaIncidente;
	private String fechaNotificacion;
	private String tipo;
	private String descripcion;
	private String descargos;
	private String estadoProceso;
	private BigDecimal valor;
	private int casaNumero;
	private int casaManzana;
	
	public Sancion() {
		
	}

	public Sancion(int idSancion, String fechaIncidente, String fechaNotificacion, String tipo, String descripcion, String descargos, String estadoProceso, BigDecimal valor, int casaNumero, int casaManzana) {
		super();
		this.idSancion = idSancion;
		this.fechaIncidente = fechaIncidente;
		this.fechaNotificacion = fechaNotificacion;
		this.tipo = tipo;
		this.descripcion = descripcion;
		this.descargos = descargos;
		this.estadoProceso = estadoProceso;
		this.valor = valor;
		this.casaNumero = casaNumero;
		this.casaManzana = casaManzana;
	}

	public int getIdSancion() {
		return idSancion;
	}

	public void setIdSancion(int idSancion) {
		this.idSancion = idSancion;
	}

	public String getFechaIncidente() {
		return fechaIncidente;
	}

	public void setFechaIncidente(String fechaIncidente) {
		this.fechaIncidente = fechaIncidente;
	}

	public String getFechaNotificacion() {
		return fechaNotificacion;
	}

	public void setFechaNotificacion(String fechaNotificacion) {
		this.fechaNotificacion = fechaNotificacion;
	}

	public String getTipo() {
		return tipo;
	}

	public void setTipo(String tipo) {
		this.tipo = tipo;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public String getDescargos() {
		return descargos;
	}

	public void setDescargos(String descargos) {
		this.descargos = descargos;
	}

	public String getEstadoProceso() {
		return estadoProceso;
	}

	public void setEstadoProceso(String estadoProceso) {
		this.estadoProceso = estadoProceso;
	}

	public BigDecimal getValor() {
		return valor;
	}

	public void setValor(BigDecimal valor) {
		this.valor = valor;
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

}
