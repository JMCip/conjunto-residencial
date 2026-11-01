package co.edu.unbosque.exception;

/**
 * Excepción lanzada cuando el registro buscado no existe en la base de datos.
 * <p>
 * Es verificada ({@code checked}).
 * </p>
 */
public class RecursoNoEncontradoException extends Exception {

    /** Constructor por defecto, con el mensaje "El registro no fue encontrado". */
    public RecursoNoEncontradoException() {
        super("El registro no fue encontrado");
    }

    /** Constructor con un mensaje específico. */
    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}