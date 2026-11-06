package co.edu.unbosque.exception;

/**
 * Excepción lanzada cuando un dato ingresado en el sistema no es válido.
 * <p>
 * Es verificada ({@code checked}), indica que uno o más datos no cumplen las reglas de validación
 * del conjunto residencial y se traduce a HTTP 400.
 */
public class DatoInvalidoException extends Exception {

    /** Crea la excepción con el mensaje por defecto. */
    public DatoInvalidoException() {
        super("El dato ingresado no es válido");
    }

    /**
     * Crea la excepción con un mensaje específico.
     *
     * @param mensaje mensaje que describe el error
     */
    public DatoInvalidoException(String mensaje) {
        super(mensaje);
    }
}