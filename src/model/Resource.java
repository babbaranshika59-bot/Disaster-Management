package model;

import exception.InvalidInputException;
import exception.ResourceShortageException;
import interfaces.Allocatable;
import interfaces.Exportable;

public class Resource implements Allocatable, Exportable, Comparable<Resource> {
    public static final String CSV_HEADER = "name,category,total,allocated";
    private static int counter = 1;

    private final int resourceId;
    private final String name;
    private final String category;
    private int totalQty;
    private int allocatedQty;

    public Resource(String name, String category, int totalQty) {
        this(nextId(), name, category, totalQty, 0);
    }

    public Resource(int id, String name, String category, int totalQty, int allocatedQty) {
        if (name == null || name.trim().isEmpty()) throw new InvalidInputException("Resource name cannot be empty.");
        if (totalQty < 0) throw new InvalidInputException("Quantity cannot be negative.");
        if (allocatedQty < 0 || allocatedQty > totalQty) throw new InvalidInputException("Allocated quantity is invalid.");
        this.resourceId = id;
        synchronized (Resource.class) { if (id >= counter) counter = id + 1; }
        this.name = name.trim();
        this.category = category;
        this.totalQty = totalQty;
        this.allocatedQty = allocatedQty;
    }

    private static synchronized int nextId() { return counter++; }

    public int getRemaining() { return totalQty - allocatedQty; }

    @Override
    public int getAvailableQuantity() { return getRemaining(); }

    @Override
    public synchronized void allocate(int qty) throws ResourceShortageException {
        if (qty <= 0) throw new InvalidInputException("Quantity must be more than 0.");
        if (qty > getRemaining())
            throw new ResourceShortageException("Shortage of " + name + ": requested " + qty + ", available " + getRemaining());
        allocatedQty += qty;
    }

    @Override
    public synchronized void release(int qty) {
        if (qty <= 0) throw new InvalidInputException("Quantity must be more than 0.");
        allocatedQty -= Math.min(qty, allocatedQty);
    }

    public synchronized void replenish(int qty) {
        if (qty <= 0) throw new InvalidInputException("Quantity must be more than 0.");
        totalQty += qty;
    }

    public synchronized void setTotalQty(int newTotal) {
        if (newTotal < allocatedQty)
            throw new InvalidInputException("Total cannot be less than already allocated (" + allocatedQty + ").");
        this.totalQty = newTotal;
    }

    public int getResourceId() { return resourceId; }
    public String getName() { return name; }
    public String getCategory() { return category; }
    public int getTotalQty() { return totalQty; }
    public int getAllocatedQty() { return allocatedQty; }

    @Override
    public int compareTo(Resource other) { return this.name.compareToIgnoreCase(other.name); }

    @Override
    public String getCsvHeader() { return CSV_HEADER; }

    @Override
    public String toCsvLine() {
        return name.replace(',', ';') + "," + category.replace(',', ';') + "," + totalQty + "," + allocatedQty;
    }

    @Override
    public String toString() {
        return String.format("#%-3d %-18s %-12s total=%-6d allocated=%-6d remaining=%d",
                resourceId, name, category, totalQty, allocatedQty, getRemaining());
    }
}
