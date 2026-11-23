package exception;

/** Thrown when a relief camp has no free beds. */
public class CampCapacityExceededException extends RescueException {
    public CampCapacityExceededException(String message) { super(message); }
}
