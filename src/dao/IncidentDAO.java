package dao;

import exception.DatabaseOperationException;
import exception.InvalidInputException;
import model.Incident;
import service.IncidentService;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class IncidentDAO {

    public void insert(Incident i) throws DatabaseOperationException {
        String sql = "INSERT INTO incidents(incident_id, disaster_type, location, description, affected_people, "
                + "severity, status, reported_at, extra) VALUES(?,?,?,?,?,?,?,?,?)";
        Connection con = null;
        PreparedStatement ps = null;
        try {
            con = DBConnection.getConnection();
            ps = con.prepareStatement(sql);
            ps.setInt(1, i.getIncidentId());
            ps.setString(2, i.getDisasterType());
            ps.setString(3, i.getLocation());
            ps.setString(4, i.getDescription());
            ps.setInt(5, i.getAffectedPeople());
            ps.setInt(6, i.getSeverity());
            ps.setString(7, i.getStatus());
            ps.setString(8, i.getReportedAt());
            ps.setString(9, i.getExtraInfo());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseOperationException("Insert incident failed: " + e.getMessage(), e);
        } finally {
            DBConnection.closeQuietly(ps);
            DBConnection.closeQuietly(con);
        }
    }

    public void update(Incident i) throws DatabaseOperationException {
        String sql = "UPDATE incidents SET location=?, description=?, affected_people=?, severity=?, status=? WHERE incident_id=?";
        Connection con = null;
        PreparedStatement ps = null;
        try {
            con = DBConnection.getConnection();
            ps = con.prepareStatement(sql);
            ps.setString(1, i.getLocation());
            ps.setString(2, i.getDescription());
            ps.setInt(3, i.getAffectedPeople());
            ps.setInt(4, i.getSeverity());
            ps.setString(5, i.getStatus());
            ps.setInt(6, i.getIncidentId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseOperationException("Update incident failed: " + e.getMessage(), e);
        } finally {
            DBConnection.closeQuietly(ps);
            DBConnection.closeQuietly(con);
        }
    }

    public boolean delete(int incidentId) throws DatabaseOperationException {
        String sql = "DELETE FROM incidents WHERE incident_id=?";
        Connection con = null;
        PreparedStatement ps = null;
        try {
            con = DBConnection.getConnection();
            ps = con.prepareStatement(sql);
            ps.setInt(1, incidentId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Delete incident failed: " + e.getMessage(), e);
        } finally {
            DBConnection.closeQuietly(ps);
            DBConnection.closeQuietly(con);
        }
    }

    public Incident findById(int id) throws DatabaseOperationException {
        String sql = "SELECT * FROM incidents WHERE incident_id=?";
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            con = DBConnection.getConnection();
            ps = con.prepareStatement(sql);
            ps.setInt(1, id);
            rs = ps.executeQuery();
            return rs.next() ? rowToIncident(rs) : null;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Find incident failed: " + e.getMessage(), e);
        } finally {
            DBConnection.closeQuietly(rs);
            DBConnection.closeQuietly(ps);
            DBConnection.closeQuietly(con);
        }
    }

    public List<Incident> findAll() throws DatabaseOperationException {
        String sql = "SELECT * FROM incidents ORDER BY incident_id";
        List<Incident> list = new ArrayList<>();
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            con = DBConnection.getConnection();
            ps = con.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) list.add(rowToIncident(rs));
            return list;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Load incidents failed: " + e.getMessage(), e);
        } finally {
            DBConnection.closeQuietly(rs);
            DBConnection.closeQuietly(ps);
            DBConnection.closeQuietly(con);
        }
    }

    public void save(Incident i) throws DatabaseOperationException {
        if (findById(i.getIncidentId()) == null) insert(i);
        else update(i);
    }

    private Incident rowToIncident(ResultSet rs) throws SQLException {
        Incident inc;
        try {
            inc = IncidentService.build(rs.getInt("incident_id"), rs.getString("disaster_type"), rs.getString("location"),
                    rs.getString("description"), rs.getInt("affected_people"), rs.getInt("severity"), rs.getString("extra"));
        } catch (InvalidInputException e) {
            throw new SQLException("Bad incident row: " + e.getMessage());
        }
        inc.setStatus(rs.getString("status"));
        inc.setReportedAt(rs.getString("reported_at"));
        return inc;
    }
}
