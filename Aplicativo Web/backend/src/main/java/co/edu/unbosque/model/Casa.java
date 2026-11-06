package co.edu.unbosque.model;

import java.math.BigDecimal;

/**
 * Casa del conjunto residencial, identificada por su número y su manzana.
 * <p>
 * Corresponde a la tabla {@code casa}.
 */
public class Casa {

	/** Número de la casa dentro de su manzana. */
	private int numero;
	/** Número de la manzana. */
	private int manzana;
	/** Área de la casa en metros cuadrados. */
	private BigDecimal area;
	/** Número de habitaciones; puede ser {@code null}. */
	private Integer numeroHabitaciones;

	/** Crea una casa vacía. Lo usa Jackson para convertir el JSON del cuerpo de la petición. */
	public Casa () {

	}

	/**
	 * Crea una casa con todos sus datos.
	 *
	 * @param numero número de la casa dentro de su manzana
	 * @param manzana número de la manzana
	 * @param area área de la casa en metros cuadrados
	 * @param numeroHabitaciones número de habitaciones; puede ser {@code null}
	 */
	public Casa(int numero, int manzana, BigDecimal area, Integer numeroHabitaciones) {
		super();
		this.numero = numero;
		this.manzana = manzana;
		this.area = area;
		this.numeroHabitaciones = numeroHabitaciones;
	}

	/**
	 * Devuelve el número de la casa dentro de su manzana.
	 *
	 * @return número de la casa dentro de su manzana
	 */
	public int getNumero() {
		return numero;
	}

	/**
	 * Asigna el número de la casa dentro de su manzana.
	 *
	 * @param numero número de la casa dentro de su manzana
	 */
	public void setNumero(int numero) {
		this.numero = numero;
	}

	/**
	 * Devuelve el número de la manzana.
	 *
	 * @return número de la manzana
	 */
	public int getManzana() {
		return manzana;
	}

	/**
	 * Asigna el número de la manzana.
	 *
	 * @param manzana número de la manzana
	 */
	public void setManzana(int manzana) {
		this.manzana = manzana;
	}

	/**
	 * Devuelve el área de la casa en metros cuadrados.
	 *
	 * @return área de la casa en metros cuadrados
	 */
	public BigDecimal getArea() {
		return area;
	}

	/**
	 * Asigna el área de la casa en metros cuadrados.
	 *
	 * @param area área de la casa en metros cuadrados
	 */
	public void setArea(BigDecimal area) {
		this.area = area;
	}

	/**
	 * Devuelve el número de habitaciones; puede ser {@code null}.
	 *
	 * @return número de habitaciones; puede ser {@code null}
	 */
	public Integer getNumeroHabitaciones() {
		return numeroHabitaciones;
	}

	/**
	 * Asigna el número de habitaciones; puede ser {@code null}.
	 *
	 * @param numeroHabitaciones número de habitaciones; puede ser {@code null}
	 */
	public void setNumeroHabitaciones(Integer numeroHabitaciones) {
		this.numeroHabitaciones = numeroHabitaciones;
	}

}