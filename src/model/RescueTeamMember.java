package model;

public class RescueTeamMember extends User {
    private int teamId = 0;          // 0 = not in any team
    private String skill = "General";
    private boolean available = true;

    public RescueTeamMember(int id, String name, String phone, String username, String password) {
        super(id, name, phone, username, password, "TEAM_MEMBER");
    }

    public int getTeamId() { return teamId; }
    public void setTeamId(int teamId) { this.teamId = teamId; }
    public String getSkill() { return skill; }
    public void setSkill(String skill) { this.skill = skill; }
    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }

    @Override
    public int[] getAllowedModules() { return new int[]{4, 8, 9}; }

    @Override
    public void showRoleMenu() { printModules("Rescue Team Member (skill: " + skill + ")"); }
}
