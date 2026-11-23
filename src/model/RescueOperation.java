package model;

import exception.InvalidInputException;
import java.util.ArrayList;
import java.util.List;

/** One rescue operation: one incident, many teams, many resource allocations. */
public class RescueOperation {
    private static int counter = 1;

    private final int operationId;
    private final Incident incident;
    private final List<RescueTeam> teams = new ArrayList<>();
    private final List<ResourceAllocation> allocations = new ArrayList<>();
    private String status = "PLANNED";   // PLANNED -> ONGOING -> COMPLETED
    private String startTime = "-";
    private String endTime = "-";

    public RescueOperation(Incident incident) {
        synchronized (RescueOperation.class) { this.operationId = counter++; }
        this.incident = incident;
    }

    public void addTeam(RescueTeam t) { teams.add(t); }
    public void addAllocation(ResourceAllocation a) { allocations.add(a); }

    public void start() {
        if (!"PLANNED".equals(status)) throw new InvalidInputException("Operation is already " + status + ".");
        if (teams.isEmpty()) throw new InvalidInputException("Assign at least one team before starting.");
        status = "ONGOING";
        startTime = new java.util.Date().toString();
    }

    public void complete() {
        if (!"ONGOING".equals(status)) throw new InvalidInputException("Only an ONGOING operation can be completed.");
        status = "COMPLETED";
        endTime = new java.util.Date().toString();
    }

    public int getOperationId() { return operationId; }
    public Incident getIncident() { return incident; }
    public List<RescueTeam> getTeams() { return teams; }
    public List<ResourceAllocation> getAllocations() { return allocations; }
    public String getStatus() { return status; }
    public String getStartTime() { return startTime; }
    public String getEndTime() { return endTime; }

    @Override
    public String toString() {
        return String.format("Operation #%-3d incident=#%-3d (%s, %s) teams=%d allocations=%d status=%s",
                operationId, incident.getIncidentId(), incident.getDisasterType(), incident.getLocation(),
                teams.size(), allocations.size(), status);
    }
}
