package exception;

/** Unchecked exception: wrong user input or invalid incident data. */
public class InvalidInputException extends RuntimeException {
    public InvalidInputException(String message) { super(message); }
}
