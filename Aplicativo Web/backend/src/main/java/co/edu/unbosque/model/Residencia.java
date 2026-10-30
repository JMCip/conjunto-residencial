package co.edu.unbosque.model;

public class Residencia {
	
	private int idResidencia;
	private String fechaIngreso;
	private String fechaSalida;
	private String residenteTipoDocumento;
	private String residenteNumeroDocumento;
	private int casaNumero;
	private int casaManzana;
	
	public Residencia() {
		
	}

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

	public int getIdResidencia() {
		return idResidencia;
	}

	public void setIdResidencia(int idResidencia) {
		this.idResidencia = idResidencia;
	}

	public String getFechaIngreso() {
		return fechaIngreso;
	}

	public void setFechaIngreso(String fechaIngreso) {
		this.fechaIngreso = fechaIngreso;
	}

	public String getFechaSalida() {
		return fechaSalida;
	}

	public void setFechaSalida(String fechaSalida) {
		this.fechaSalida = fechaSalida;
	}

	public String getResidenteTipoDocumento() {
		return residenteTipoDocumento;
	}

	public void setResidenteTipoDocumento(String residenteTipoDocumento) {
		this.residenteTipoDocumento = residenteTipoDocumento;
	}

	public String getResidenteNumeroDocumento() {
		return residenteNumeroDocumento;
	}

	public void setResidenteNumeroDocumento(String residenteNumeroDocumento) {
		this.residenteNumeroDocumento = residenteNumeroDocumento;
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
