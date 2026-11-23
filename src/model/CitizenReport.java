package model;

/** A report sent by a logged-in citizen. It becomes a real Incident only after the Administrator approves it. */
public class CitizenReport {
    private static int counter = 1;

    private final int reportId;
    private final int reporterId;
    private final String reporterName;
    private final String phone;
    private final String type;
    private final String location;
    private final String description;
    private final int affectedPeople;
    private String status = "PENDING";   // PENDING, APPROVED, REJECTED
    private String remark = "-";

    public CitizenReport(int reporterId, String reporterName, String phone, String type,
                         String location, String description, int affectedPeople) {
        synchronized (CitizenReport.class) { this.reportId = counter++; }
        this.reporterId = reporterId;
        this.reporterName = reporterName;
        this.phone = phone;
        this.type = type;
        this.location = location;
        this.description = description;
        this.affectedPeople = affectedPeople;
    }

    public int getReportId() { return reportId; }
    public int getReporterId() { return reporterId; }
    public String getReporterName() { return reporterName; }
    public String getPhone() { return phone; }
    public String getType() { return type; }
    public String getLocation() { return location; }
    public String getDescription() { return description; }
    public int getAffectedPeople() { return affectedPeople; }
    public String getStatus() { return status; }
    public String getRemark() { return remark; }

    public void approve() { status = "APPROVED"; }
    public void reject(String reason) { status = "REJECTED"; remark = reason; }

    @Override
    public String toString() {
        String note = status.equals("REJECTED") ? " (reason: " + remark + ")" : "";
        return String.format("Report #%-3d %-17s %-12s affected=%-5d by %s (%s) [%s]%s | %s",
                reportId, type, location, affectedPeople, reporterName, phone, status, note, description);
    }
}