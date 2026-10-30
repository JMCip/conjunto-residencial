package co.edu.unbosque.model;

public class AsignacionCelda {
	
	private int idAsignacion;
	private String fechaInicio;
	private String fechaFin;
	private int casaNumero;
	private int casaManzana;
	private int idCelda;
	
	public AsignacionCelda() {
		
	}

	public AsignacionCelda(int idAsignacion, String fechaInicio, String fechaFin, int casaNumero, int casaManzana, int idCelda) {
		super();
		this.idAsignacion = idAsignacion;
		this.fechaInicio = fechaInicio;
		this.fechaFin = fechaFin;
		this.casaNumero = casaNumero;
		this.casaManzana = casaManzana;
		this.idCelda = idCelda;
	}

	public int getIdAsignacion() {
		return idAsignacion;
	}

	public void setIdAsignacion(int idAsignacion) {
		this.idAsignacion = idAsignacion;
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

	public int getIdCelda() {
		return idCelda;
	}

	public void setIdCelda(int idCelda) {
		this.idCelda = idCelda;
	}

}
