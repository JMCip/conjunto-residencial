package co.edu.unbosque.exception;

public class NoAutorizadoException extends Exception {
	
	public NoAutorizadoException() {
		super("No autorizado");
	}
	
	public NoAutorizadoException(String mensaje) {
		super(mensaje);
	}
}
