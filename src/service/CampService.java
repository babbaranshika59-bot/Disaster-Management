package service;

import exception.CampCapacityExceededException;
import exception.InvalidInputException;
import exception.RescueException;
import model.*;
import java.util.*;

public class CampService {
    private final Map<Integer, ReliefCamp> camps = new TreeMap<>();
    private final Set<String> locations = new TreeSet<>();

    public synchronized ReliefCamp registerCamp(String name, String location, int capacity) {
        ReliefCamp c = new ReliefCamp(name, location, capacity);
        camps.put(c.getCampId(), c);
        locations.add(c.getLocation());
        return c;
    }

    public synchronized ReliefCamp findById(int id) { return camps.get(id); }

    private ReliefCamp require(int id) {
        ReliefCamp c = findById(id);
        if (c == null) throw new InvalidInputException("Camp #" + id + " not found.");
        return c;
    }

    public synchronized DisplacedPerson registerPerson(int campId, String name, int age, String gender)
            throws CampCapacityExceededException {
        if (name.trim().isEmpty()) throw new InvalidInputException("Name cannot be empty.");
        if (age < 0 || age > 120) throw new InvalidInputException("Age must be between 0 and 120.");
        ReliefCamp camp = require(campId);
        DisplacedPerson p = new DisplacedPerson(name.trim(), age, gender);
        camp.registerPerson(p);
        return p;
    }

    public synchronized void allocateSupplies(int campId, String resourceName, int qty, ResourceService rs)
            throws RescueException {
        ReliefCamp camp = require(campId);
        rs.allocate(resourceName, qty, "CAMP", campId);
        camp.addSupply(resourceName, qty);
    }

    public synchronized List<ReliefCamp> getAll() { return new ArrayList<>(camps.values()); }
    public Set<String> getLocations() { return locations; }

    public synchronized void replaceAll(List<ReliefCamp> loaded) {
        camps.clear();
        locations.clear();
        for (ReliefCamp c : loaded) {
            camps.put(c.getCampId(), c);
            locations.add(c.getLocation());
        }
    }

    public void showOccupancy() { util.DataStore.printAll(getAll()); }

    public void showRequirements() {
        for (ReliefCamp c : getAll()) {
            System.out.printf("  Camp #%d %-16s people=%-4d daily food=%-5d packets, water=%-5d litres%n",
                    c.getCampId(), c.getName(), c.getOccupied(), c.getFoodNeeded(), c.getWaterNeeded());
        }
        if (getAll().isEmpty()) System.out.println("  (no camps)");
    }
}
