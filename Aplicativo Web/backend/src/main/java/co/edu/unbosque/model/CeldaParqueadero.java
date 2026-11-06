package co.edu.unbosque.model;

/**
 * Celda del parqueadero del conjunto.
 * <p>
 * Corresponde a la tabla {@code celda_parqueadero}.
 */
public class CeldaParqueadero {
	
	/** Identificador de la celda de parqueadero. */
	private int idCelda;
	/** Tipo de celda: {@code PROPIA}, {@code ADICIONAL} o {@code VISITANTES}. */
	private String tipo;
	/** Estado de la celda: {@code OCUPADA} o {@code DESOCUPADA}. */
	private String estado;
	
	/**
	 * Crea una celda de parqueadero vacía. Lo usa Jackson para convertir el JSON del cuerpo de la
	 * petición.
	 */
	public CeldaParqueadero() {
		
	}

	/**
	 * Crea una celda de parqueadero con todos sus datos.
	 *
	 * @param idCelda identificador de la celda de parqueadero
	 * @param tipo tipo de celda: {@code PROPIA}, {@code ADICIONAL} o {@code VISITANTES}
	 * @param estado estado de la celda: {@code OCUPADA} o {@code DESOCUPADA}
	 */
	public CeldaParqueadero(int idCelda, String tipo, String estado) {
		super();
		this.idCelda = idCelda;
		this.tipo = tipo;
		this.estado = estado;
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

	/**
	 * Devuelve el tipo de celda: {@code PROPIA}, {@code ADICIONAL} o {@code VISITANTES}.
	 *
	 * @return tipo de celda: {@code PROPIA}, {@code ADICIONAL} o {@code VISITANTES}
	 */
	public String getTipo() {
		return tipo;
	}

	/**
	 * Asigna el tipo de celda: {@code PROPIA}, {@code ADICIONAL} o {@code VISITANTES}.
	 *
	 * @param tipo tipo de celda: {@code PROPIA}, {@code ADICIONAL} o {@code VISITANTES}
	 */
	public void setTipo(String tipo) {
		this.tipo = tipo;
	}

	/**
	 * Devuelve el estado de la celda: {@code OCUPADA} o {@code DESOCUPADA}.
	 *
	 * @return estado de la celda: {@code OCUPADA} o {@code DESOCUPADA}
	 */
	public String getEstado() {
		return estado;
	}

	/**
	 * Asigna el estado de la celda: {@code OCUPADA} o {@code DESOCUPADA}.
	 *
	 * @param estado estado de la celda: {@code OCUPADA} o {@code DESOCUPADA}
	 */
	public void setEstado(String estado) {
		this.estado = estado;
	}

}
