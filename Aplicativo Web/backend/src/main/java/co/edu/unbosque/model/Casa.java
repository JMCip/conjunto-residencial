package co.edu.unbosque.model;

import java.math.BigDecimal;

public class Casa {
	
	private int numero;
	private int manzana;
	private BigDecimal area;
	private String estado;
	private Integer numeroHabitaciones;
	
	public Casa () {
		
	}

	public Casa(int numero, int manzana, BigDecimal area, String estado, Integer numeroHabitaciones) {
		super();
		this.numero = numero;
		this.manzana = manzana;
		this.area = area;
		this.estado = estado;
		this.numeroHabitaciones = numeroHabitaciones;
	}

	public int getNumero() {
		return numero;
	}

	public void setNumero(int numero) {
		this.numero = numero;
	}

	public int getManzana() {
		return manzana;
	}

	public void setManzana(int manzana) {
		this.manzana = manzana;
	}

	public BigDecimal getArea() {
		return area;
	}

	public void setArea(BigDecimal area) {
		this.area = area;
	}

	public String getEstado() {
		return estado;
	}

	public void setEstado(String estado) {
		this.estado = estado;
	}

	public Integer getNumeroHabitaciones() {
		return numeroHabitaciones;
	}

	public void setNumeroHabitaciones(Integer numeroHabitaciones) {
		this.numeroHabitaciones = numeroHabitaciones;
	}
	


}
