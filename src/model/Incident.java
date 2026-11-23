package model;

import exception.InvalidInputException;
import interfaces.Exportable;

/** Abstract disaster record. Natural and man-made incidents extend it. */
public abstract class Incident implements Comparable<Incident>, Exportable {
    public static final int LOW = 1, MEDIUM = 2, HIGH = 3, CRITICAL = 4;
    public static final String CSV_HEADER = "id,type,location,description,affected,severity,status,extra";

    private static int counter = 1;

    private final int incidentId;
    private String location;
    private String description;
    private int affectedPeople;
    private int severity;
    private String status;
    private String reportedAt;

    /** New incident: id comes from the static counter. */
    public Incident(String location, String description, int affectedPeople, int severity) {
        this(nextId(), location, description, affectedPeople, severity);
    }

    /** Used when loading an existing incident (e.g. from the database). */
    public Incident(int id, String location, String description, int affectedPeople, int severity) {
        validate(location, description, affectedPeople, severity);
        this.incidentId = id;
        syncCounter(id);
        this.location = location.trim();
        this.description = description.trim();
        this.affectedPeople = affectedPeople;
        this.severity = severity;
        this.status = "REPORTED";
        this.reportedAt = new java.util.Date().toString();
    }

    private static synchronized int nextId() { return counter++; }

    private static synchronized void syncCounter(int id) {
        if (id >= counter) counter = id + 1;
    }

    private static void validate(String location, String description, int affected, int severity) {
        if (location == null || location.trim().isEmpty())
            throw new InvalidInputException("Location cannot be empty.");
        if (description == null || description.trim().isEmpty())
            throw new InvalidInputException("Description cannot be empty.");
        if (affected < 0)
            throw new InvalidInputException("Affected people cannot be negative.");
        if (severity < LOW || severity > CRITICAL)
            throw new InvalidInputException("Severity must be 1 (Low) to 4 (Critical).");
    }

    public static String severityName(int s) {
        switch (s) {
            case CRITICAL: return "Critical";
            case HIGH: return "High";
            case MEDIUM: return "Medium";
            default: return "Low";
        }
    }

    public abstract String getDisasterType();
    public abstract int getPriorityScore();
    public abstract String getExtraInfo();

    public int getIncidentId() { return incidentId; }
    public String getLocation() { return location; }
    public String getDescription() { return description; }
    public int getAffectedPeople() { return affectedPeople; }
    public int getSeverity() { return severity; }
    public String getStatus() { return status; }
    public String getReportedAt() { return reportedAt; }
    public boolean isResolved() { return "RESOLVED".equals(status); }

    public void setStatus(String status) { this.status = status; }
    public void setReportedAt(String reportedAt) { this.reportedAt = reportedAt; }

    /** Overloaded update #1: text details. */
    public void updateDetails(String newLocation, String newDescription) {
        validate(newLocation, newDescription, affectedPeople, severity);
        this.location = newLocation.trim();
        this.description = newDescription.trim();
    }

    /** Overloaded update #2: numbers. */
    public void updateDetails(int newAffected, int newSeverity) {
        validate(location, description, newAffected, newSeverity);
        this.affectedPeople = newAffected;
        this.severity = newSeverity;
    }

    public void resolve() { this.status = "RESOLVED"; }

    /** Most urgent incident comes first (natural ordering). */
    @Override
    public int compareTo(Incident other) {
        int cmp = Integer.compare(other.getPriorityScore(), this.getPriorityScore());
        if (cmp != 0) return cmp;
        return Integer.compare(this.incidentId, other.incidentId);
    }

    @Override
    public String getCsvHeader() { return CSV_HEADER; }

    @Override
    public String toCsvLine() {
        return incidentId + "," + getDisasterType() + "," + clean(location) + "," + clean(description) + ","
                + affectedPeople + "," + severity + "," + status + "," + clean(getExtraInfo());
    }

    private static String clean(String s) {
        return s.replace(',', ';').replace('\n', ' ');
    }

    @Override
    public String toString() {
        return String.format("#%-3d %-17s %-14s %-9s affected=%-6d %-8s | %s",
                incidentId, getDisasterType(), location, severityName(severity), affectedPeople, status, description);
    }
}
