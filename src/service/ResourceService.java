package service;

import exception.InvalidInputException;
import exception.ResourceShortageException;
import model.Resource;
import model.ResourceAllocation;
import java.util.*;
import java.util.stream.Collectors;

public class ResourceService {
    public static final String[] CATEGORIES = {"Medical", "Food", "Water", "Shelter", "Equipment", "Transport"};

    private final TreeMap<String, Resource> resources = new TreeMap<>();        // sorted by name
    private final LinkedList<ResourceAllocation> allocations = new LinkedList<>();
    private final Deque<ResourceAllocation> undoStack = new ArrayDeque<>();

    private String key(String name) { return name.trim().toLowerCase(); }

    public synchronized Resource addResource(String name, String category, int qty) {
        Resource existing = resources.get(key(name));
        if (existing != null) {
            existing.replenish(qty);
            return existing;
        }
        Resource r = new Resource(name, category, qty);
        resources.put(key(name), r);
        return r;
    }

    public synchronized Resource findResource(String name) { return resources.get(key(name)); }

    private Resource require(String name) {
        Resource r = findResource(name);
        if (r == null) throw new InvalidInputException("Resource not found: " + name);
        return r;
    }

    public synchronized void updateQuantity(String name, int newTotal) { require(name).setTotalQty(newTotal); }
    public synchronized void replenish(String name, int qty) { require(name).replenish(qty); }
    public synchronized void returnResource(String name, int qty) { require(name).release(qty); }

    /** synchronized: two threads can never over-allocate the same stock. */
    public synchronized ResourceAllocation allocate(String name, int qty, String targetType, int targetId)
            throws ResourceShortageException {
        Resource r = require(name);
        r.allocate(qty);                        // throws ResourceShortageException
        ResourceAllocation a = new ResourceAllocation(r.getResourceId(), r.getName(), targetType, targetId, qty);
        allocations.add(a);
        undoStack.push(a);
        return a;
    }

    public synchronized ResourceAllocation undoLastAllocation() {
        if (undoStack.isEmpty()) throw new InvalidInputException("No allocation to undo.");
        ResourceAllocation a = undoStack.pop();
        require(a.getResourceName()).release(a.getQuantity());
        allocations.remove(a);
        return a;
    }

    public synchronized List<Resource> getAll() { return new ArrayList<>(resources.values()); }
    public synchronized List<ResourceAllocation> getAllocations() { return new ArrayList<>(allocations); }

    public List<Resource> search(String keyword) {
        String k = keyword.toLowerCase();
        return getAll().stream()
                .filter(r -> r.getName().toLowerCase().contains(k) || r.getCategory().toLowerCase().contains(k))
                .collect(Collectors.toList());
    }

    public synchronized void replaceAll(List<Resource> loaded) {
        resources.clear();
        for (Resource r : loaded) resources.put(key(r.getName()), r);
    }

    public void displayAll() { util.DataStore.printAll(getAll()); }
}
