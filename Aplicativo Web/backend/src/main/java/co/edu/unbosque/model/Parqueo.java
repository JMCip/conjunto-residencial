package co.edu.unbosque.model;

/**
 * Registro de entrada y salida de un vehículo en una celda del parqueadero.
 * <p>
 * Corresponde a la tabla {@code parqueo}. Un parqueo sin fecha y hora de salida está abierto.
 */
public class Parqueo {
	
	/** Identificador del parqueo. */
	private int idParqueo;
	/** Fecha y hora de entrada ({@code AAAA-MM-DD HH:MM:SS}). */
	private String fechaHoraEntrada;
	/**
	 * Fecha y hora de salida ({@code AAAA-MM-DD HH:MM:SS}); {@code null} mientras el parqueo esté
	 * abierto.
	 */
	private String fechaHoraSalida;
	/** Placa del vehículo. */
	private String vehiculoPlaca;
	/** Identificador de la celda de parqueadero. */
	private int idCelda;
	
	/** Crea un parqueo vacío. Lo usa Jackson para convertir el JSON del cuerpo de la petición. */
	public Parqueo() {
		
	}

	/**
	 * Crea un parqueo con todos sus datos.
	 *
	 * @param idParqueo identificador del parqueo
	 * @param fechaHoraEntrada fecha y hora de entrada ({@code AAAA-MM-DD HH:MM:SS})
	 * @param fechaHoraSalida fecha y hora de salida ({@code AAAA-MM-DD HH:MM:SS}); {@code null}
	 *         mientras el parqueo esté abierto
	 * @param vehiculoPlaca placa del vehículo
	 * @param idCelda identificador de la celda de parqueadero
	 */
	public Parqueo(int idParqueo, String fechaHoraEntrada, String fechaHoraSalida, String vehiculoPlaca, int idCelda) {
		super();
		this.idParqueo = idParqueo;
		this.fechaHoraEntrada = fechaHoraEntrada;
		this.fechaHoraSalida = fechaHoraSalida;
		this.vehiculoPlaca = vehiculoPlaca;
		this.idCelda = idCelda;
	}

	/**
	 * Devuelve el identificador del parqueo.
	 *
	 * @return identificador del parqueo
	 */
	public int getIdParqueo() {
		return idParqueo;
	}

	/**
	 * Asigna el identificador del parqueo.
	 *
	 * @param idParqueo identificador del parqueo
	 */
	public void setIdParqueo(int idParqueo) {
		this.idParqueo = idParqueo;
	}

	/**
	 * Devuelve la fecha y hora de entrada ({@code AAAA-MM-DD HH:MM:SS}).
	 *
	 * @return fecha y hora de entrada ({@code AAAA-MM-DD HH:MM:SS})
	 */
	public String getFechaHoraEntrada() {
		return fechaHoraEntrada;
	}

	/**
	 * Asigna la fecha y hora de entrada ({@code AAAA-MM-DD HH:MM:SS}).
	 *
	 * @param fechaHoraEntrada fecha y hora de entrada ({@code AAAA-MM-DD HH:MM:SS})
	 */
	public void setFechaHoraEntrada(String fechaHoraEntrada) {
		this.fechaHoraEntrada = fechaHoraEntrada;
	}

	/**
	 * Devuelve la fecha y hora de salida ({@code AAAA-MM-DD HH:MM:SS}); {@code null} mientras el
	 * parqueo esté abierto.
	 *
	 * @return fecha y hora de salida ({@code AAAA-MM-DD HH:MM:SS}); {@code null} mientras el parqueo
	 *         esté abierto
	 */
	public String getFechaHoraSalida() {
		return fechaHoraSalida;
	}

	/**
	 * Asigna la fecha y hora de salida ({@code AAAA-MM-DD HH:MM:SS}); {@code null} mientras el parqueo
	 * esté abierto.
	 *
	 * @param fechaHoraSalida fecha y hora de salida ({@code AAAA-MM-DD HH:MM:SS}); {@code null}
	 *         mientras el parqueo esté abierto
	 */
	public void setFechaHoraSalida(String fechaHoraSalida) {
		this.fechaHoraSalida = fechaHoraSalida;
	}

	/**
	 * Devuelve la placa del vehículo.
	 *
	 * @return placa del vehículo
	 */
	public String getVehiculoPlaca() {
		return vehiculoPlaca;
	}

	/**
	 * Asigna la placa del vehículo.
	 *
	 * @param vehiculoPlaca placa del vehículo
	 */
	public void setVehiculoPlaca(String vehiculoPlaca) {
		this.vehiculoPlaca = vehiculoPlaca;
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
