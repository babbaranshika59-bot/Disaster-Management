package exception;

/** Wraps SQLException so upper layers do not depend on java.sql. */
public class DatabaseOperationException extends RescueException {
    public DatabaseOperationException(String message, Throwable cause) { super(message, cause); }
}
