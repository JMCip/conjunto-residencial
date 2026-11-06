package co.edu.unbosque.model;

/**
 * Asignación de una celda de parqueadero a una casa durante un periodo.
 * <p>
 * Corresponde a la tabla {@code asignacion_celda}. Una asignación sin fecha de fin está vigente.
 */
public class AsignacionCelda {
	
	/** Identificador de la asignación de celda. */
	private int idAsignacion;
	/** Fecha de inicio ({@code AAAA-MM-DD}). */
	private String fechaInicio;
	/** Fecha de fin ({@code AAAA-MM-DD}); {@code null} mientras esté vigente. */
	private String fechaFin;
	/** Número de la casa. */
	private int casaNumero;
	/** Manzana de la casa. */
	private int casaManzana;
	/** Identificador de la celda de parqueadero. */
	private int idCelda;
	
	/**
	 * Crea una asignación de celda vacía. Lo usa Jackson para convertir el JSON del cuerpo de la
	 * petición.
	 */
	public AsignacionCelda() {
		
	}

	/**
	 * Crea una asignación de celda con todos sus datos.
	 *
	 * @param idAsignacion identificador de la asignación de celda
	 * @param fechaInicio fecha de inicio ({@code AAAA-MM-DD})
	 * @param fechaFin fecha de fin ({@code AAAA-MM-DD}); {@code null} mientras esté vigente
	 * @param casaNumero número de la casa
	 * @param casaManzana manzana de la casa
	 * @param idCelda identificador de la celda de parqueadero
	 */
	public AsignacionCelda(int idAsignacion, String fechaInicio, String fechaFin, int casaNumero, int casaManzana, int idCelda) {
		super();
		this.idAsignacion = idAsignacion;
		this.fechaInicio = fechaInicio;
		this.fechaFin = fechaFin;
		this.casaNumero = casaNumero;
		this.casaManzana = casaManzana;
		this.idCelda = idCelda;
	}

	/**
	 * Devuelve el identificador de la asignación de celda.
	 *
	 * @return identificador de la asignación de celda
	 */
	public int getIdAsignacion() {
		return idAsignacion;
	}

	/**
	 * Asigna el identificador de la asignación de celda.
	 *
	 * @param idAsignacion identificador de la asignación de celda
	 */
	public void setIdAsignacion(int idAsignacion) {
		this.idAsignacion = idAsignacion;
	}

	/**
	 * Devuelve la fecha de inicio ({@code AAAA-MM-DD}).
	 *
	 * @return fecha de inicio ({@code AAAA-MM-DD})
	 */
	public String getFechaInicio() {
		return fechaInicio;
	}

	/**
	 * Asigna la fecha de inicio ({@code AAAA-MM-DD}).
	 *
	 * @param fechaInicio fecha de inicio ({@code AAAA-MM-DD})
	 */
	public void setFechaInicio(String fechaInicio) {
		this.fechaInicio = fechaInicio;
	}

	/**
	 * Devuelve la fecha de fin ({@code AAAA-MM-DD}); {@code null} mientras esté vigente.
	 *
	 * @return fecha de fin ({@code AAAA-MM-DD}); {@code null} mientras esté vigente
	 */
	public String getFechaFin() {
		return fechaFin;
	}

	/**
	 * Asigna la fecha de fin ({@code AAAA-MM-DD}); {@code null} mientras esté vigente.
	 *
	 * @param fechaFin fecha de fin ({@code AAAA-MM-DD}); {@code null} mientras esté vigente
	 */
	public void setFechaFin(String fechaFin) {
		this.fechaFin = fechaFin;
	}

	/**
	 * Devuelve el número de la casa.
	 *
	 * @return número de la casa
	 */
	public int getCasaNumero() {
		return casaNumero;
	}

	/**
	 * Asigna el número de la casa.
	 *
	 * @param casaNumero número de la casa
	 */
	public void setCasaNumero(int casaNumero) {
		this.casaNumero = casaNumero;
	}

	/**
	 * Devuelve la manzana de la casa.
	 *
	 * @return manzana de la casa
	 */
	public int getCasaManzana() {
		return casaManzana;
	}

	/**
	 * Asigna la manzana de la casa.
	 *
	 * @param casaManzana manzana de la casa
	 */
	public void setCasaManzana(int casaManzana) {
		this.casaManzana = casaManzana;
	}

	/**
	 * Devuelve el identificador de la celda de parqueadero.
	 *
	 * @return identificador de la celda de parqueadero
	 */
	public int getIdCelda() {
		return idCelda;
	}

	/**
	 * Asigna el identificador de la celda de parqueadero.
	 *
	 * @param idCelda identificador de la celda de parqueadero
	 */
	public void setIdCelda(int idCelda) {
		this.idCelda = idCelda;
	}

}
