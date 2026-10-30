package co.edu.unbosque.model;

public class Visita {
	
	private int idVisita;
	private String fechaEntrada;
	private String fechaSalida;
	private String visitanteTipoDocumento;
	private String visitanteNumeroDocumento;
	private int casaNumero;
	private int casaManzana;
	
	public Visita() {
		
	}

	public Visita(int idVisita, String fechaEntrada, String fechaSalida, String visitanteTipoDocumento, String visitanteNumeroDocumento, int casaNumero, int casaManzana) {
		super();
		this.idVisita = idVisita;
		this.fechaEntrada = fechaEntrada;
		this.fechaSalida = fechaSalida;
		this.visitanteTipoDocumento = visitanteTipoDocumento;
		this.visitanteNumeroDocumento = visitanteNumeroDocumento;
		this.casaNumero = casaNumero;
		this.casaManzana = casaManzana;
	}

	public int getIdVisita() {
		return idVisita;
	}

	public void setIdVisita(int idVisita) {
		this.idVisita = idVisita;
	}

	public String getFechaEntrada() {
		return fechaEntrada;
	}

	public void setFechaEntrada(String fechaEntrada) {
		this.fechaEntrada = fechaEntrada;
	}

	public String getFechaSalida() {
		return fechaSalida;
	}

	public void setFechaSalida(String fechaSalida) {
		this.fechaSalida = fechaSalida;
	}

	public String getVisitanteTipoDocumento() {
		return visitanteTipoDocumento;
	}

	public void setVisitanteTipoDocumento(String visitanteTipoDocumento) {
		this.visitanteTipoDocumento = visitanteTipoDocumento;
	}

	public String getVisitanteNumeroDocumento() {
		return visitanteNumeroDocumento;
	}

	public void setVisitanteNumeroDocumento(String visitanteNumeroDocumento) {
		this.visitanteNumeroDocumento = visitanteNumeroDocumento;
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
