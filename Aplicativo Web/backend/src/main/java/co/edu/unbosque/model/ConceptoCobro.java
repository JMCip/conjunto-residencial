package co.edu.unbosque.model;

import java.math.BigDecimal;

public class ConceptoCobro {
	
	private int idConcepto;
	private String nombre;
	private String descripcion;
	private BigDecimal valorBase;
	
	public ConceptoCobro() {
		
	}

	public ConceptoCobro(int idConcepto, String nombre, String descripcion, BigDecimal valorBase) {
		super();
		this.idConcepto = idConcepto;
		this.nombre = nombre;
		this.descripcion = descripcion;
		this.valorBase = valorBase;
	}

	public int getIdConcepto() {
		return idConcepto;
	}

	public void setIdConcepto(int idConcepto) {
		this.idConcepto = idConcepto;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public BigDecimal getValorBase() {
		return valorBase;
	}

	public void setValorBase(BigDecimal valorBase) {
		this.valorBase = valorBase;
	}

}
