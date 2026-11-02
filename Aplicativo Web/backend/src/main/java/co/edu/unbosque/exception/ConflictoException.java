package co.edu.unbosque.exception;

/**
 * Excepción lanzada cuando la operación choca con una regla de negocio
 * o con el estado actual de los datos (por ejemplo, una franja ya ocupada).
 * <p>
 * Es verificada ({@code checked}) y se traduce a HTTP 409.
 * </p>
 */
public class ConflictoException extends Exception {

    /** Constructor por defecto. */
    public ConflictoException() {
        super("La operación genera un conflicto con los datos existentes");
    }

    /** Constructor con un mensaje específico. */
    public ConflictoException(String mensaje) {
        super(mensaje);
    }
}
