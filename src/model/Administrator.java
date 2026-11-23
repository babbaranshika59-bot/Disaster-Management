package model;

public class Administrator extends User {
    private final String adminCode;

    public Administrator(int id, String name, String phone, String username, String password) {
        super(id, name, phone, username, password, "ADMIN");
        this.adminCode = "ADM-" + id;
    }

    public String getAdminCode() { return adminCode; }

    @Override
    public int[] getAllowedModules() { return new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11}; }

    @Override
    public void showRoleMenu() { printModules("Administrator (" + adminCode + ")"); }
}
