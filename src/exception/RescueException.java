package exception;

/** Base checked exception for all RescueGrid business errors. */
public class RescueException extends Exception {
    public RescueException(String message) { super(message); }
    public RescueException(String message, Throwable cause) { super(message, cause); }
}
