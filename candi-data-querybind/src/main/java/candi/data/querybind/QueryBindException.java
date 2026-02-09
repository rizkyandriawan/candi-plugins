package candi.data.querybind;

/**
 * Exception thrown when a QueryBind operation fails.
 */
public class QueryBindException extends RuntimeException {

    public QueryBindException(String message) {
        super(message);
    }

    public QueryBindException(String message, Throwable cause) {
        super(message, cause);
    }
}
