package co.edu.unbosque.model;

public class Parqueo {
	
	private int idParqueo;
	private String fechaHoraEntrada;
	private String fechaHoraSalida;
	private String vehiculoPlaca;
	private int idCelda;
	
	public Parqueo() {
		
	}

	public Parqueo(int idParqueo, String fechaHoraEntrada, String fechaHoraSalida, String vehiculoPlaca, int idCelda) {
		super();
		this.idParqueo = idParqueo;
		this.fechaHoraEntrada = fechaHoraEntrada;
		this.fechaHoraSalida = fechaHoraSalida;
		this.vehiculoPlaca = vehiculoPlaca;
		this.idCelda = idCelda;
	}

	public int getIdParqueo() {
		return idParqueo;
	}

	public void setIdParqueo(int idParqueo) {
		this.idParqueo = idParqueo;
	}

	public String getFechaHoraEntrada() {
		return fechaHoraEntrada;
	}

	public void setFechaHoraEntrada(String fechaHoraEntrada) {
		this.fechaHoraEntrada = fechaHoraEntrada;
	}

	public String getFechaHoraSalida() {
		return fechaHoraSalida;
	}

	public void setFechaHoraSalida(String fechaHoraSalida) {
		this.fechaHoraSalida = fechaHoraSalida;
	}

	public String getVehiculoPlaca() {
		return vehiculoPlaca;
	}

	public void setVehiculoPlaca(String vehiculoPlaca) {
		this.vehiculoPlaca = vehiculoPlaca;
	}

	public int getIdCelda() {
		return idCelda;
	}

	public void setIdCelda(int idCelda) {
		this.idCelda = idCelda;
	}

}
