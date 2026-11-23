package model;

/** Abstract base class for every system user. */
public abstract class User {
    private final int userId;
    private String name;
    private String phone;
    private final String username;
    private String password;
    private final String role;

    public User(int userId, String name, String phone, String username, String password, String role) {
        this.userId = userId;
        this.name = name;
        this.phone = phone;
        this.username = username;
        this.password = password;
        this.role = role;
    }

    public int getUserId() { return userId; }
    public String getName() { return name; }
    public String getPhone() { return phone; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getRole() { return role; }
    public void setName(String name) { this.name = name; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setPassword(String password) { this.password = password; }

    public boolean checkPassword(String input) { return this.password.equals(input); }

    /** Main-menu numbers this role may open. */
    public abstract int[] getAllowedModules();

    public abstract void showRoleMenu();

    public boolean canAccess(int module) {
        for (int m : getAllowedModules()) {
            if (m == module) return true;
        }
        return false;
    }

    protected void printModules(String title) {
        StringBuilder sb = new StringBuilder("Logged in as " + title + " | Allowed modules: ");
        int[] mods = getAllowedModules();
        for (int i = 0; i < mods.length; i++) {
            sb.append(mods[i]);
            if (i < mods.length - 1) sb.append(", ");
        }
        System.out.println(sb);
    }

    @Override
    public String toString() {
        return String.format("%-4d %-20s %-12s %-14s %s", userId, name, phone, username, role);
    }
}
