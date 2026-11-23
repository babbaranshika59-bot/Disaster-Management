package exception;

/** Thrown when a team cannot be assigned (busy team, resolved incident, unknown id). */
public class InvalidTeamAssignmentException extends RescueException {
    public InvalidTeamAssignmentException(String message) { super(message); }
}
