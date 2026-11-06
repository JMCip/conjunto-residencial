package co.edu.unbosque.exception;

/**
 * Excepción lanzada cuando la operación choca con una regla de negocio o con el estado actual de
 * los datos (por ejemplo, una franja del salón ya ocupada).
 * <p>
 * Es verificada ({@code checked}) y se traduce a HTTP 409.
 */
public class ConflictoException extends Exception {

    /** Crea la excepción con el mensaje por defecto. */
    public ConflictoException() {
        super("La operación genera un conflicto con los datos existentes");
    }

    /**
     * Crea la excepción con un mensaje específico.
     *
     * @param mensaje mensaje que describe el error
     */
    public ConflictoException(String mensaje) {
        super(mensaje);
    }
}
