package dao;

import exception.DatabaseOperationException;
import model.RescueOperation;
import model.RescueTeam;
import model.RescueTeamMember;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** Handles rescue_teams, team_members, rescue_operations and operation_teams. */
public class TeamDAO {

    public void saveTeam(RescueTeam t) throws DatabaseOperationException {
        String teamSql = "INSERT INTO rescue_teams(team_id, team_name, specialization, available, assigned_incident_id) "
                + "VALUES(?,?,?,?,?) ON DUPLICATE KEY UPDATE available=VALUES(available), "
                + "assigned_incident_id=VALUES(assigned_incident_id)";
        Connection con = null;
        PreparedStatement ps = null;
        try {
            con = DBConnection.getConnection();
            ps = con.prepareStatement(teamSql);
            ps.setInt(1, t.getTeamId());
            ps.setString(2, t.getTeamName());
            ps.setString(3, t.getSpecialization());
            ps.setBoolean(4, t.isAvailable());
            ps.setInt(5, t.getAssignedIncidentId());
            ps.executeUpdate();
            ps.close();

            ps = con.prepareStatement("DELETE FROM team_members WHERE team_id=?");
            ps.setInt(1, t.getTeamId());
            ps.executeUpdate();
            ps.close();

            ps = con.prepareStatement("INSERT INTO team_members(member_id, team_id, member_name, skill) VALUES(?,?,?,?)");
            for (RescueTeamMember m : t.getMembers()) {
                ps.setInt(1, m.getUserId());
                ps.setInt(2, t.getTeamId());
                ps.setString(3, m.getName());
                ps.setString(4, m.getSkill());
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Save team failed: " + e.getMessage(), e);
        } finally {
            DBConnection.closeQuietly(ps);
            DBConnection.closeQuietly(con);
        }
    }

    public List<RescueTeam> findAllTeams() throws DatabaseOperationException {
        List<RescueTeam> list = new ArrayList<>();
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            con = DBConnection.getConnection();
            ps = con.prepareStatement("SELECT * FROM rescue_teams ORDER BY team_id");
            rs = ps.executeQuery();
            while (rs.next()) {
                RescueTeam t = new RescueTeam(rs.getInt("team_id"), rs.getString("team_name"), rs.getString("specialization"));
                t.restoreState(rs.getBoolean("available"), rs.getInt("assigned_incident_id"));
                list.add(t);
            }
            DBConnection.closeQuietly(rs);
            DBConnection.closeQuietly(ps);
            ps = con.prepareStatement("SELECT * FROM team_members WHERE team_id=?");
            for (RescueTeam t : list) {
                ps.setInt(1, t.getTeamId());
                rs = ps.executeQuery();
                while (rs.next()) {
                    int id = rs.getInt("member_id");
                    RescueTeamMember m = new RescueTeamMember(id, rs.getString("member_name"), "0000000000", "member" + id, "NA");
                    m.setSkill(rs.getString("skill"));
                    t.addMember(m);
                }
                DBConnection.closeQuietly(rs);
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Load teams failed: " + e.getMessage(), e);
        } finally {
            DBConnection.closeQuietly(ps);
            DBConnection.closeQuietly(con);
        }
    }

    public boolean deleteTeam(int teamId) throws DatabaseOperationException {
        Connection con = null;
        PreparedStatement ps = null;
        try {
            con = DBConnection.getConnection();
            ps = con.prepareStatement("DELETE FROM team_members WHERE team_id=?");
            ps.setInt(1, teamId);
            ps.executeUpdate();
            ps.close();
            ps = con.prepareStatement("DELETE FROM rescue_teams WHERE team_id=?");
            ps.setInt(1, teamId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Delete team failed: " + e.getMessage(), e);
        } finally {
            DBConnection.closeQuietly(ps);
            DBConnection.closeQuietly(con);
        }
    }

    public void saveOperation(RescueOperation op) throws DatabaseOperationException {
        String sql = "INSERT INTO rescue_operations(operation_id, incident_id, status, start_time, end_time) VALUES(?,?,?,?,?) "
                + "ON DUPLICATE KEY UPDATE status=VALUES(status), start_time=VALUES(start_time), end_time=VALUES(end_time)";
        Connection con = null;
        PreparedStatement ps = null;
        try {
            con = DBConnection.getConnection();
            ps = con.prepareStatement(sql);
            ps.setInt(1, op.getOperationId());
            ps.setInt(2, op.getIncident().getIncidentId());
            ps.setString(3, op.getStatus());
            ps.setString(4, op.getStartTime());
            ps.setString(5, op.getEndTime());
            ps.executeUpdate();
            ps.close();

            ps = con.prepareStatement("DELETE FROM operation_teams WHERE operation_id=?");
            ps.setInt(1, op.getOperationId());
            ps.executeUpdate();
            ps.close();

            ps = con.prepareStatement("INSERT INTO operation_teams(operation_id, team_id) VALUES(?,?)");
            for (RescueTeam t : op.getTeams()) {
                ps.setInt(1, op.getOperationId());
                ps.setInt(2, t.getTeamId());
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Save operation failed: " + e.getMessage(), e);
        } finally {
            DBConnection.closeQuietly(ps);
            DBConnection.closeQuietly(con);
        }
    }

    public List<String> findAllOperations() throws DatabaseOperationException {
        List<String> rows = new ArrayList<>();
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            con = DBConnection.getConnection();
            ps = con.prepareStatement("SELECT o.operation_id, o.incident_id, o.status, "
                    + "(SELECT COUNT(*) FROM operation_teams t WHERE t.operation_id = o.operation_id) AS teams "
                    + "FROM rescue_operations o ORDER BY o.operation_id");
            rs = ps.executeQuery();
            while (rs.next()) {
                rows.add("Operation #" + rs.getInt("operation_id") + " incident=#" + rs.getInt("incident_id")
                        + " status=" + rs.getString("status") + " teams=" + rs.getInt("teams"));
            }
            return rows;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Load operations failed: " + e.getMessage(), e);
        } finally {
            DBConnection.closeQuietly(rs);
            DBConnection.closeQuietly(ps);
            DBConnection.closeQuietly(con);
        }
    }
}
