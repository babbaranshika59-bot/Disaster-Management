package exception;

/** Thrown when more quantity is requested than is available. */
public class ResourceShortageException extends RescueException {
    public ResourceShortageException(String message) { super(message); }
}
