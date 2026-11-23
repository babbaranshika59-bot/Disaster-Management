package model;

import exception.InvalidTeamAssignmentException;
import java.util.ArrayList;
import java.util.List;

public class RescueTeam {
    private static int counter = 1;

    private final int teamId;
    private final String teamName;
    private final String specialization;
    private final List<RescueTeamMember> members = new ArrayList<>();
    private boolean available = true;
    private int assignedIncidentId = 0;

    public RescueTeam(String teamName, String specialization) {
        this(nextId(), teamName, specialization);
    }

    public RescueTeam(int id, String teamName, String specialization) {
        this.teamId = id;
        synchronized (RescueTeam.class) { if (id >= counter) counter = id + 1; }
        this.teamName = teamName;
        this.specialization = specialization;
    }

    private static synchronized int nextId() { return counter++; }

    public void addMember(RescueTeamMember m) {
        m.setTeamId(teamId);
        members.add(m);
    }

    public boolean removeMember(int userId) {
        return members.removeIf(m -> m.getUserId() == userId);
    }

    public void assignToIncident(int incidentId) throws InvalidTeamAssignmentException {
        if (!available)
            throw new InvalidTeamAssignmentException("Team '" + teamName + "' is already busy on incident #" + assignedIncidentId);
        this.available = false;
        this.assignedIncidentId = incidentId;
    }

    public void release() {
        this.available = true;
        this.assignedIncidentId = 0;
    }

    public void restoreState(boolean available, int assignedIncidentId) {
        this.available = available;
        this.assignedIncidentId = assignedIncidentId;
    }

    public int getTeamId() { return teamId; }
    public String getTeamName() { return teamName; }
    public String getSpecialization() { return specialization; }
    public List<RescueTeamMember> getMembers() { return members; }
    public boolean isAvailable() { return available; }
    public int getAssignedIncidentId() { return assignedIncidentId; }

    @Override
    public String toString() {
        return String.format("Team #%-3d %-18s %-12s members=%-2d %s",
                teamId, teamName, specialization, members.size(),
                available ? "AVAILABLE" : "BUSY (incident #" + assignedIncidentId + ")");
    }
}
