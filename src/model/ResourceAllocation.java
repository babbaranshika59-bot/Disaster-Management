package model;

public class ResourceAllocation {
    private static int counter = 1;

    private final int allocationId;
    private final int resourceId;
    private final String resourceName;
    private final String targetType;   // INCIDENT or CAMP
    private final int targetId;
    private final int quantity;
    private final String allocatedAt;

    public ResourceAllocation(int resourceId, String resourceName, String targetType, int targetId, int quantity) {
        synchronized (ResourceAllocation.class) { this.allocationId = counter++; }
        this.resourceId = resourceId;
        this.resourceName = resourceName;
        this.targetType = targetType;
        this.targetId = targetId;
        this.quantity = quantity;
        this.allocatedAt = new java.util.Date().toString();
    }

    public int getAllocationId() { return allocationId; }
    public int getResourceId() { return resourceId; }
    public String getResourceName() { return resourceName; }
    public String getTargetType() { return targetType; }
    public int getTargetId() { return targetId; }
    public int getQuantity() { return quantity; }
    public String getAllocatedAt() { return allocatedAt; }

    @Override
    public String toString() {
        return String.format("Alloc #%-3d %-16s qty=%-5d -> %s #%d  (%s)",
                allocationId, resourceName, quantity, targetType, targetId, allocatedAt);
    }
}
