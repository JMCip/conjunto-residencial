package co.edu.unbosque.model;

import java.math.BigDecimal;

/**
 * Concepto que se puede cobrar en una factura (administración, sanción, salón comunal, etc.).
 * <p>
 * Corresponde a la tabla {@code concepto_cobro}.
 */
public class ConceptoCobro {
	
	/** Identificador del concepto de cobro. */
	private int idConcepto;
	/** Nombre del concepto de cobro. */
	private String nombre;
	/** Descripción del concepto. */
	private String descripcion;
	/** Valor que se cobra por defecto con este concepto. */
	private BigDecimal valorBase;
	
	/**
	 * Crea un concepto de cobro vacío. Lo usa Jackson para convertir el JSON del cuerpo de la
	 * petición.
	 */
	public ConceptoCobro() {
		
	}

	/**
	 * Crea un concepto de cobro con todos sus datos.
	 *
	 * @param idConcepto identificador del concepto de cobro
	 * @param nombre nombre del concepto de cobro
	 * @param descripcion descripción del concepto
	 * @param valorBase valor que se cobra por defecto con este concepto
	 */
	public ConceptoCobro(int idConcepto, String nombre, String descripcion, BigDecimal valorBase) {
		super();
		this.idConcepto = idConcepto;
		this.nombre = nombre;
		this.descripcion = descripcion;
		this.valorBase = valorBase;
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
	 * Devuelve el nombre del concepto de cobro.
	 *
	 * @return nombre del concepto de cobro
	 */
	public String getNombre() {
		return nombre;
	}

	/**
	 * Asigna el nombre del concepto de cobro.
	 *
	 * @param nombre nombre del concepto de cobro
	 */
	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	/**
	 * Devuelve la descripción del concepto.
	 *
	 * @return descripción del concepto
	 */
	public String getDescripcion() {
		return descripcion;
	}

	/**
	 * Asigna la descripción del concepto.
	 *
	 * @param descripcion descripción del concepto
	 */
	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	/**
	 * Devuelve el valor que se cobra por defecto con este concepto.
	 *
	 * @return valor que se cobra por defecto con este concepto
	 */
	public BigDecimal getValorBase() {
		return valorBase;
	}

	/**
	 * Asigna el valor que se cobra por defecto con este concepto.
	 *
	 * @param valorBase valor que se cobra por defecto con este concepto
	 */
	public void setValorBase(BigDecimal valorBase) {
		this.valorBase = valorBase;
	}

}
