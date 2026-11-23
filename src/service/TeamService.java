package service;

import exception.InvalidInputException;
import exception.InvalidTeamAssignmentException;
import model.*;
import java.util.*;
import java.util.stream.Collectors;

public class TeamService {
    private final Map<Integer, RescueTeam> teams = new HashMap<>();
    private final Set<String> specializations = new HashSet<>();

    public synchronized RescueTeam registerTeam(String name, String specialization) {
        if (name.trim().isEmpty() || specialization.trim().isEmpty())
            throw new InvalidInputException("Team name and specialization cannot be empty.");
        RescueTeam t = new RescueTeam(name.trim(), specialization.trim());
        teams.put(t.getTeamId(), t);
        specializations.add(t.getSpecialization().toLowerCase());
        return t;
    }

    public synchronized RescueTeam findById(int id) { return teams.get(id); }

    private RescueTeam require(int id) {
        RescueTeam t = findById(id);
        if (t == null) throw new InvalidInputException("Team #" + id + " not found.");
        return t;
    }

    public synchronized void addMember(int teamId, RescueTeamMember m) {
        RescueTeam t = require(teamId);
        if (m.getTeamId() != 0) throw new InvalidInputException(m.getName() + " is already in team #" + m.getTeamId());
        t.addMember(m);
    }

    public synchronized void assignTeam(int teamId, Incident incident) throws InvalidTeamAssignmentException {
        RescueTeam t = findById(teamId);
        if (t == null) throw new InvalidTeamAssignmentException("Team #" + teamId + " does not exist.");
        if (incident == null || incident.isResolved())
            throw new InvalidTeamAssignmentException("Incident is missing or already resolved.");
        t.assignToIncident(incident.getIncidentId());
    }

    public synchronized void releaseTeam(int teamId) {
        require(teamId).release();
    }

    public synchronized List<RescueTeam> getAll() {
        List<RescueTeam> list = new ArrayList<>(teams.values());
        list.sort(Comparator.comparingInt(RescueTeam::getTeamId));
        return list;
    }

    public List<RescueTeam> findAvailable() {
        return getAll().stream().filter(RescueTeam::isAvailable).collect(Collectors.toList());
    }

    public Set<String> getSpecializations() { return specializations; }

    public synchronized void replaceAll(List<RescueTeam> loaded) {
        teams.clear();
        specializations.clear();
        for (RescueTeam t : loaded) {
            teams.put(t.getTeamId(), t);
            specializations.add(t.getSpecialization().toLowerCase());
        }
    }

    public void displayAll() { util.DataStore.printAll(getAll()); }
}
