package model;

import exception.CampCapacityExceededException;
import exception.InvalidInputException;
import interfaces.Allocatable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ReliefCamp implements Allocatable {
    public static final int FOOD_PER_PERSON = 3;    // packets per day
    public static final int WATER_PER_PERSON = 4;   // litres per day
    private static int counter = 1;

    private final int campId;
    private final String name;
    private final String location;
    private final int capacity;
    private int occupied;
    private final List<DisplacedPerson> people = new ArrayList<>();
    private final Map<String, Integer> supplies = new LinkedHashMap<>();

    public ReliefCamp(String name, String location, int capacity) {
        this(nextId(), name, location, capacity, 0);
    }

    public ReliefCamp(int id, String name, String location, int capacity, int occupied) {
        if (name == null || name.trim().isEmpty() || location == null || location.trim().isEmpty())
            throw new InvalidInputException("Camp name and location cannot be empty.");
        if (capacity <= 0) throw new InvalidInputException("Capacity must be more than 0.");
        this.campId = id;
        synchronized (ReliefCamp.class) { if (id >= counter) counter = id + 1; }
        this.name = name.trim();
        this.location = location.trim();
        this.capacity = capacity;
        this.occupied = occupied;
    }

    private static synchronized int nextId() { return counter++; }

    public void registerPerson(DisplacedPerson p) throws CampCapacityExceededException {
        if (occupied >= capacity)
            throw new CampCapacityExceededException("Camp '" + name + "' is full (" + capacity + " beds).");
        p.setCampId(campId);
        people.add(p);
        occupied++;
    }

    /** Used while loading from the database (no capacity check needed). */
    public void addLoadedPerson(DisplacedPerson p) { p.setCampId(campId); people.add(p); }

    public void addSupply(String resourceName, int qty) {
        supplies.merge(resourceName, qty, Integer::sum);
    }

    public int getAvailableBeds() { return capacity - occupied; }
    public int getOccupancyPercent() { return capacity == 0 ? 0 : occupied * 100 / capacity; }
    public int getFoodNeeded() { return occupied * FOOD_PER_PERSON; }
    public int getWaterNeeded() { return occupied * WATER_PER_PERSON; }

    @Override
    public int getAvailableQuantity() { return getAvailableBeds(); }

    /** Reserve beds without registering names. */
    @Override
    public void allocate(int beds) throws CampCapacityExceededException {
        if (beds <= 0) throw new InvalidInputException("Beds must be more than 0.");
        if (beds > getAvailableBeds())
            throw new CampCapacityExceededException("Only " + getAvailableBeds() + " beds free in " + name);
        occupied += beds;
    }

    @Override
    public void release(int beds) { occupied -= Math.min(beds, occupied); }

    public int getCampId() { return campId; }
    public String getName() { return name; }
    public String getLocation() { return location; }
    public int getCapacity() { return capacity; }
    public int getOccupied() { return occupied; }
    public List<DisplacedPerson> getPeople() { return people; }
    public Map<String, Integer> getSupplies() { return supplies; }

    @Override
    public String toString() {
        return String.format("Camp #%-3d %-16s %-12s capacity=%-5d occupied=%-5d free=%-5d (%d%%)",
                campId, name, location, capacity, occupied, getAvailableBeds(), getOccupancyPercent());
    }
}
