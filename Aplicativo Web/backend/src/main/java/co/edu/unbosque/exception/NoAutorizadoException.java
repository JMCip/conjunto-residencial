package co.edu.unbosque.exception;

/**
 * Excepción lanzada cuando la petición no está autenticada: faltan las credenciales o el token, o
 * no son válidos.
 * <p>
 * Es verificada ({@code checked}) y se traduce a HTTP 401.
 */
public class NoAutorizadoException extends Exception {
	
	/** Crea la excepción con el mensaje por defecto. */
	public NoAutorizadoException() {
		super("No autorizado");
	}
	
	/**
	 * Crea la excepción con un mensaje específico.
	 *
	 * @param mensaje mensaje que describe el error
	 */
	public NoAutorizadoException(String mensaje) {
		super(mensaje);
	}
}
