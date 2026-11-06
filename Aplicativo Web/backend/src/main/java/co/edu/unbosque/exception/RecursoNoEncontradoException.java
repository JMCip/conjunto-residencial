package co.edu.unbosque.exception;

/**
 * Excepción lanzada cuando el registro buscado no existe en la base de datos.
 * <p>
 * Es verificada ({@code checked}) y se traduce a HTTP 404.
 */
public class RecursoNoEncontradoException extends Exception {

    /** Crea la excepción con el mensaje por defecto. */
    public RecursoNoEncontradoException() {
        super("El registro no fue encontrado");
    }

    /**
     * Crea la excepción con un mensaje específico.
     *
     * @param mensaje mensaje que describe el error
     */
    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}