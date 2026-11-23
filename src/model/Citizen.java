package model;


public class Citizen extends User {

    public Citizen(int id, String name, String phone, String username, String password) {
        super(id, name, phone, username, password, "CITIZEN");
    }

    /** A citizen has no access to the staff modules of the main menu. */
    @Override
    public int[] getAllowedModules() { return new int[]{}; }

    @Override
    public void showRoleMenu() { System.out.println("Logged in as Citizen (can report emergencies and track own reports)"); }
}