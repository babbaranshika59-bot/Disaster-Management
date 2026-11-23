package model;

/** Flood, Earthquake, Landslide. */
public class NaturalIncident extends Incident {
    private final String naturalType;
    private final int intensityLevel;   // 1 to 10

    public NaturalIncident(String location, String description, int affected, int severity,
                           String naturalType, int intensityLevel) {
        super(location, description, affected, severity);
        this.naturalType = naturalType;
        this.intensityLevel = intensityLevel;
    }

    public NaturalIncident(int id, String location, String description, int affected, int severity,
                           String naturalType, int intensityLevel) {
        super(id, location, description, affected, severity);
        this.naturalType = naturalType;
        this.intensityLevel = intensityLevel;
    }

    @Override
    public String getDisasterType() { return naturalType; }

    @Override
    public int getPriorityScore() {
        return getSeverity() * 100000 + getAffectedPeople() / 10 + intensityLevel * 5;
    }

    @Override
    public String getExtraInfo() { return String.valueOf(intensityLevel); }
}
