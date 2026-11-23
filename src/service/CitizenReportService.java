package service;

import exception.InvalidInputException;
import model.CitizenReport;
import model.Incident;
import model.User;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/** Holds citizen reports until the Administrator approves or rejects them. */
public class CitizenReportService {
    private final List<CitizenReport> reports = new ArrayList<>();
    private final IncidentService incidentService;

    public CitizenReportService(IncidentService incidentService) {
        this.incidentService = incidentService;
    }

    /** synchronized: many citizens can report at the same time. Name and phone come from the logged-in user. */
    public synchronized CitizenReport submit(User reporter, String type, String location,
                                             String description, int affected) {
        String t = IncidentService.normalizeType(type);
        if (t == null) throw new InvalidInputException("Unknown disaster type.");
        if (affected < 0) throw new InvalidInputException("Affected people cannot be negative.");
        if (location.trim().isEmpty() || description.trim().isEmpty())
            throw new InvalidInputException("Location and description are required.");
        CitizenReport r = new CitizenReport(reporter.getUserId(), reporter.getName(), reporter.getPhone(),
                t, location.trim(), description.trim(), affected);
        reports.add(r);
        return r;
    }

    public synchronized List<CitizenReport> getPending() {
        return reports.stream().filter(r -> r.getStatus().equals("PENDING")).collect(Collectors.toList());
    }

    /** Only the reports of one user (so a citizen cannot see other people's reports). */
    public synchronized List<CitizenReport> getByReporter(int userId) {
        return reports.stream().filter(r -> r.getReporterId() == userId).collect(Collectors.toList());
    }

    private CitizenReport requirePending(int id) {
        for (CitizenReport r : reports) {
            if (r.getReportId() == id) {
                if (!r.getStatus().equals("PENDING"))
                    throw new InvalidInputException("Report #" + id + " is already " + r.getStatus() + ".");
                return r;
            }
        }
        throw new InvalidInputException("Report #" + id + " not found.");
    }

    /** The administrator decides the severity; the report becomes a real Incident in the priority queue. */
    public synchronized Incident approve(int id, int severity) {
        CitizenReport r = requirePending(id);
        Incident i = incidentService.createIncident(r.getType(), r.getLocation(),
                r.getDescription(), r.getAffectedPeople(), severity, "");
        r.approve();
        return i;
    }

    public synchronized void reject(int id, String reason) {
        requirePending(id).reject(reason);
    }
}