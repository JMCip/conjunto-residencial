package co.edu.unbosque.model;

/**
 * Periodo en que una persona reside en una casa.
 * <p>
 * Corresponde a la tabla {@code residencia}. Una residencia sin fecha de salida está vigente.
 */
public class Residencia {
	
	/** Identificador de la residencia. */
	private int idResidencia;
	/** Fecha de ingreso ({@code AAAA-MM-DD}). */
	private String fechaIngreso;
	/** Fecha de salida ({@code AAAA-MM-DD}); {@code null} mientras siga vigente. */
	private String fechaSalida;
	/** Tipo de documento del residente. */
	private String residenteTipoDocumento;
	/** Número de documento del residente. */
	private String residenteNumeroDocumento;
	/** Número de la casa. */
	private int casaNumero;
	/** Manzana de la casa. */
	private int casaManzana;
	
	/** Crea una residencia vacía. Lo usa Jackson para convertir el JSON del cuerpo de la petición. */
	public Residencia() {
		
	}

	/**
	 * Crea una residencia con todos sus datos.
	 *
	 * @param idResidencia identificador de la residencia
	 * @param fechaIngreso fecha de ingreso ({@code AAAA-MM-DD})
	 * @param fechaSalida fecha de salida ({@code AAAA-MM-DD}); {@code null} mientras siga vigente
	 * @param residenteTipoDocumento tipo de documento del residente
	 * @param residenteNumeroDocumento número de documento del residente
	 * @param casaNumero número de la casa
	 * @param casaManzana manzana de la casa
	 */
	public Residencia(int idResidencia, String fechaIngreso, String fechaSalida, String residenteTipoDocumento, String residenteNumeroDocumento, int casaNumero, int casaManzana) {
		super();
		this.idResidencia = idResidencia;
		this.fechaIngreso = fechaIngreso;
		this.fechaSalida = fechaSalida;
		this.residenteTipoDocumento = residenteTipoDocumento;
		this.residenteNumeroDocumento = residenteNumeroDocumento;
		this.casaNumero = casaNumero;
		this.casaManzana = casaManzana;
	}

	/**
	 * Devuelve el identificador de la residencia.
	 *
	 * @return identificador de la residencia
	 */
	public int getIdResidencia() {
		return idResidencia;
	}

	/**
	 * Asigna el identificador de la residencia.
	 *
	 * @param idResidencia identificador de la residencia
	 */
	public void setIdResidencia(int idResidencia) {
		this.idResidencia = idResidencia;
	}

	/**
	 * Devuelve la fecha de ingreso ({@code AAAA-MM-DD}).
	 *
	 * @return fecha de ingreso ({@code AAAA-MM-DD})
	 */
	public String getFechaIngreso() {
		return fechaIngreso;
	}

	/**
	 * Asigna la fecha de ingreso ({@code AAAA-MM-DD}).
	 *
	 * @param fechaIngreso fecha de ingreso ({@code AAAA-MM-DD})
	 */
	public void setFechaIngreso(String fechaIngreso) {
		this.fechaIngreso = fechaIngreso;
	}

	/**
	 * Devuelve la fecha de salida ({@code AAAA-MM-DD}); {@code null} mientras siga vigente.
	 *
	 * @return fecha de salida ({@code AAAA-MM-DD}); {@code null} mientras siga vigente
	 */
	public String getFechaSalida() {
		return fechaSalida;
	}

	/**
	 * Asigna la fecha de salida ({@code AAAA-MM-DD}); {@code null} mientras siga vigente.
	 *
	 * @param fechaSalida fecha de salida ({@code AAAA-MM-DD}); {@code null} mientras siga vigente
	 */
	public void setFechaSalida(String fechaSalida) {
		this.fechaSalida = fechaSalida;
	}

	/**
	 * Devuelve el tipo de documento del residente.
	 *
	 * @return tipo de documento del residente
	 */
	public String getResidenteTipoDocumento() {
		return residenteTipoDocumento;
	}

	/**
	 * Asigna el tipo de documento del residente.
	 *
	 * @param residenteTipoDocumento tipo de documento del residente
	 */
	public void setResidenteTipoDocumento(String residenteTipoDocumento) {
		this.residenteTipoDocumento = residenteTipoDocumento;
	}

	/**
	 * Devuelve el número de documento del residente.
	 *
	 * @return número de documento del residente
	 */
	public String getResidenteNumeroDocumento() {
		return residenteNumeroDocumento;
	}

	/**
	 * Asigna el número de documento del residente.
	 *
	 * @param residenteNumeroDocumento número de documento del residente
	 */
	public void setResidenteNumeroDocumento(String residenteNumeroDocumento) {
		this.residenteNumeroDocumento = residenteNumeroDocumento;
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

}
