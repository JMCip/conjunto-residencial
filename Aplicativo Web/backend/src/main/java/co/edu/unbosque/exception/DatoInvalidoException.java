package co.edu.unbosque.exception;

/**
 * Excepción lanzada cuando un dato ingresado en el sistema no es válido.
 * <p>
 * Esta excepción es verificada ({@code checked}) y se usa para indicar que
 * uno o más datos no cumplen las reglas de validación del conjunto residencial.
 * </p>
 */
public class DatoInvalidoException extends Exception {

    /** Constructor por defecto, con el mensaje "El dato ingresado no es válido". */
    public DatoInvalidoException() {
        super("El dato ingresado no es válido");
    }

    /** Constructor con un mensaje específico. */
    public DatoInvalidoException(String mensaje) {
        super(mensaje);
    }
}