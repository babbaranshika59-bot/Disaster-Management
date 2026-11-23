package model;

/** Fire, Building Collapse. */
public class ManMadeIncident extends Incident {
    private final String manMadeType;
    private final String cause;

    public ManMadeIncident(String location, String description, int affected, int severity,
                           String manMadeType, String cause) {
        super(location, description, affected, severity);
        this.manMadeType = manMadeType;
        this.cause = cause;
    }

    public ManMadeIncident(int id, String location, String description, int affected, int severity,
                           String manMadeType, String cause) {
        super(id, location, description, affected, severity);
        this.manMadeType = manMadeType;
        this.cause = cause;
    }

    @Override
    public String getDisasterType() { return manMadeType; }

    /** Man-made incidents can spread fast, so affected people count a little more. */
    @Override
    public int getPriorityScore() {
        return getSeverity() * 100000 + getAffectedPeople() / 5;
    }

    @Override
    public String getExtraInfo() { return cause; }
}
