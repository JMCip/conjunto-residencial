package co.edu.unbosque.model;

public class CeldaParqueadero {
	
	private int idCelda;
	private String tipo;
	private String estado;
	
	public CeldaParqueadero() {
		
	}

	public CeldaParqueadero(int idCelda, String tipo, String estado) {
		super();
		this.idCelda = idCelda;
		this.tipo = tipo;
		this.estado = estado;
	}

	public int getIdCelda() {
		return idCelda;
	}

	public void setIdCelda(int idCelda) {
		this.idCelda = idCelda;
	}

	public String getTipo() {
		return tipo;
	}

	public void setTipo(String tipo) {
		this.tipo = tipo;
	}

	public String getEstado() {
		return estado;
	}

	public void setEstado(String estado) {
		this.estado = estado;
	}

}
