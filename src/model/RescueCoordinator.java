package model;

public class RescueCoordinator extends User {
    private String region = "General";

    public RescueCoordinator(int id, String name, String phone, String username, String password) {
        super(id, name, phone, username, password, "COORDINATOR");
    }

    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }

    @Override
    public int[] getAllowedModules() { return new int[]{2, 3, 4, 6, 8, 9, 10}; }

    @Override
    public void showRoleMenu() { printModules("Rescue Coordinator (" + region + ")"); }
}
