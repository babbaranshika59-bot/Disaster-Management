package service;

import exception.InvalidInputException;
import model.*;
import java.util.*;
import java.util.stream.Collectors;

public class IncidentService {
    public static final String[] TYPES = {"Flood", "Earthquake", "Fire", "Landslide", "Building Collapse"};

    private final List<Incident> incidents = new ArrayList<>();
    private final PriorityManager priorityManager;

    public IncidentService(PriorityManager pm) { this.priorityManager = pm; }

    public static String normalizeType(String type) {
        for (String t : TYPES) {
            if (t.equalsIgnoreCase(type.trim())) return t;
        }
        return null;
    }

    /** Factory: id 0 means "give a new id". */
    public static Incident build(int id, String type, String location, String desc, int affected, int severity, String extra) {
        String t = normalizeType(type);
        if (t == null) throw new InvalidInputException("Unknown disaster type: " + type);
        boolean natural = t.equals("Flood") || t.equals("Earthquake") || t.equals("Landslide");
        if (natural) {
            int intensity = 1;
            try {
                intensity = Integer.parseInt(extra.trim());
            } catch (NumberFormatException e) {
                intensity = 1;
            }
            intensity = Math.max(1, Math.min(10, intensity));
            return id == 0 ? new NaturalIncident(location, desc, affected, severity, t, intensity)
                           : new NaturalIncident(id, location, desc, affected, severity, t, intensity);
        }
        String cause = (extra == null || extra.trim().isEmpty()) ? "Unknown" : extra.trim();
        return id == 0 ? new ManMadeIncident(location, desc, affected, severity, t, cause)
                       : new ManMadeIncident(id, location, desc, affected, severity, t, cause);
    }

    public synchronized Incident createIncident(String type, String location, String desc, int affected, int severity, String extra) {
        Incident i = build(0, type, location.intern(), desc, affected, severity, extra);  // intern(): String pool
        incidents.add(i);
        priorityManager.enqueue(i);
        return i;
    }

    public synchronized Incident findById(int id) {
        return incidents.stream().filter(i -> i.getIncidentId() == id).findFirst().orElse(null);
    }

    public synchronized List<Incident> getAll() { return new ArrayList<>(incidents); }

    public List<Incident> searchByType(String type) {
        return getAll().stream().filter(i -> i.getDisasterType().equalsIgnoreCase(type.trim())).collect(Collectors.toList());
    }

    public List<Incident> searchByLocation(String keyword) {
        String k = keyword.toLowerCase();
        return getAll().stream().filter(i -> i.getLocation().toLowerCase().contains(k)).collect(Collectors.toList());
    }

    public List<Incident> searchBySeverity(int severity) {
        return getAll().stream().filter(i -> i.getSeverity() == severity).collect(Collectors.toList());
    }

    public synchronized void updateText(int id, String location, String description) {
        Incident i = requireIncident(id);
        i.updateDetails(location, description);
    }

    public synchronized void updateNumbers(int id, int affected, int severity) {
        Incident i = requireIncident(id);
        priorityManager.remove(i);          // priority changes, so re-insert
        i.updateDetails(affected, severity);
        priorityManager.enqueue(i);
    }

    public synchronized void resolve(int id) {
        Incident i = requireIncident(id);
        if (i.isResolved()) throw new InvalidInputException("Incident #" + id + " is already resolved.");
        i.resolve();
        priorityManager.remove(i);
    }

    /** Only Administrator or Coordinator may delete. Uses an Iterator. */
    public synchronized void delete(User by, int id) {
        if (!(by instanceof Administrator) && !(by instanceof RescueCoordinator))
            throw new InvalidInputException("You are not permitted to delete incidents.");
        Iterator<Incident> it = incidents.iterator();
        while (it.hasNext()) {
            Incident i = it.next();
            if (i.getIncidentId() == id) {
                it.remove();
                priorityManager.remove(i);
                return;
            }
        }
        throw new InvalidInputException("Incident #" + id + " not found.");
    }

    private Incident requireIncident(int id) {
        Incident i = findById(id);
        if (i == null) throw new InvalidInputException("Incident #" + id + " not found.");
        return i;
    }

    public synchronized int countCritical() {
        return (int) incidents.stream().filter(i -> i.getSeverity() == Incident.CRITICAL && !i.isResolved()).count();
    }

    public synchronized void replaceAll(List<Incident> loaded) {
        incidents.clear();
        priorityManager.clear();
        for (Incident i : loaded) {
            incidents.add(i);
            priorityManager.enqueue(i);
        }
    }

    /** TreeMap groups incidents by severity (sorted by key). */
    public Map<Integer, List<Incident>> groupBySeverity() {
        Map<Integer, List<Incident>> map = new TreeMap<>(Collections.reverseOrder());
        for (Incident i : getAll()) {
            map.computeIfAbsent(i.getSeverity(), k -> new ArrayList<>()).add(i);
        }
        return map;
    }

    /** LinkedHashSet keeps unique locations in the order they were reported. */
    public Set<String> getUniqueLocations() {
        Set<String> set = new LinkedHashSet<>();
        for (Incident i : getAll()) set.add(i.getLocation());
        return set;
    }

    public void displayAll() { util.DataStore.printAll(getAll()); }

    /** ListIterator walks the list backwards (latest first). */
    public void displayReverse() {
        List<Incident> list = getAll();
        ListIterator<Incident> it = list.listIterator(list.size());
        if (!it.hasPrevious()) System.out.println("  (no records)");
        while (it.hasPrevious()) System.out.println("  " + it.previous());
    }
}
