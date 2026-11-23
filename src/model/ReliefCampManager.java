package model;

public class ReliefCampManager extends User {
    private int campId = 0;

    public ReliefCampManager(int id, String name, String phone, String username, String password) {
        super(id, name, phone, username, password, "CAMP_MANAGER");
    }

    public int getCampId() { return campId; }
    public void setCampId(int campId) { this.campId = campId; }

    @Override
    public int[] getAllowedModules() { return new int[]{5, 6, 7, 9, 10}; }

    @Override
    public void showRoleMenu() { printModules("Relief Camp Manager"); }
}
