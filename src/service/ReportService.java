package service;

import model.*;
import util.DataStore;
import java.util.*;
import java.util.stream.Collectors;

/** All reports use lambda expressions and the Stream API. */
public class ReportService {
    private final IncidentService incidents;
    private final TeamService teams;
    private final ResourceService resources;
    private final CampService camps;

    public ReportService(IncidentService i, TeamService t, ResourceService r, CampService c) {
        this.incidents = i;
        this.teams = t;
        this.resources = r;
        this.camps = c;
    }

    public List<Incident> criticalIncidents() {
        return incidents.getAll().stream()
                .filter(i -> i.getSeverity() == Incident.CRITICAL && !i.isResolved())
                .sorted()
                .collect(Collectors.toList());
    }

    public List<RescueTeam> availableTeams() { return teams.findAvailable(); }

    public List<ReliefCamp> lowCapacityCamps(int minFreeBeds) {
        return camps.getAll().stream().filter(c -> c.getAvailableBeds() < minFreeBeds).collect(Collectors.toList());
    }

    public List<Resource> sortedResources() {
        return resources.getAll().stream()
                .sorted(Comparator.comparingInt(Resource::getRemaining))
                .collect(Collectors.toList());
    }

    public List<Incident> topUrgent(int n) {
        return incidents.getAll().stream().filter(i -> !i.isResolved()).sorted().limit(n).collect(Collectors.toList());
    }

    /** Pagination with skip() and limit(). */
    public List<Incident> incidentPage(int page, int pageSize) {
        return incidents.getAll().stream().skip((long) (page - 1) * pageSize).limit(pageSize).collect(Collectors.toList());
    }

    public String incidentStats() {
        List<Incident> all = incidents.getAll();
        StringBuilder sb = new StringBuilder();
        sb.append("Total incidents      : ").append(all.size()).append("\n");
        sb.append("Resolved             : ").append(all.stream().filter(Incident::isResolved).count()).append("\n");
        int totalAffected = all.stream().mapToInt(Incident::getAffectedPeople).sum();
        sb.append("Total people affected: ").append(totalAffected).append("\n");
        List<Integer> severities = all.stream().map(Incident::getSeverity).collect(Collectors.toList());
        Integer maxSeverity = DataStore.maxOf(severities);
        sb.append("Highest severity     : ").append(maxSeverity == null ? "-" : Incident.severityName(maxSeverity)).append("\n");
        sb.append("By disaster type     :\n");
        List<String> types = all.stream().map(Incident::getDisasterType).distinct().sorted().collect(Collectors.toList());
        for (String t : types) {
            long count = all.stream().filter(i -> i.getDisasterType().equals(t)).count();
            sb.append(String.format("   %-18s %d%n", t, count));
        }
        return sb.toString();
    }

    /** Two-dimensional array: rows = severity (Low..Critical), columns = open / resolved. */
    public String severityMatrix() {
        int[][] matrix = new int[4][2];
        for (Incident i : incidents.getAll()) {
            matrix[i.getSeverity() - 1][i.isResolved() ? 1 : 0]++;
        }
        StringBuilder sb = new StringBuilder(String.format("%-10s %-6s %-9s%n", "Severity", "Open", "Resolved"));
        for (int s = 0; s < matrix.length; s++) {
            sb.append(String.format("%-10s %-6d %-9d%n", Incident.severityName(s + 1), matrix[s][0], matrix[s][1]));
        }
        return sb.toString();
    }
}
