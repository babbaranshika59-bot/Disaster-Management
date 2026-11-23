package dao;

import exception.DatabaseOperationException;
import model.DisplacedPerson;
import model.ReliefCamp;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CampDAO {

    public void saveCamp(ReliefCamp c) throws DatabaseOperationException {
        String campSql = "INSERT INTO relief_camps(camp_id, name, location, capacity, occupied, food_needed, water_needed) "
                + "VALUES(?,?,?,?,?,?,?) ON DUPLICATE KEY UPDATE occupied=VALUES(occupied), "
                + "food_needed=VALUES(food_needed), water_needed=VALUES(water_needed)";
        String personSql = "INSERT INTO displaced_people(person_id, name, age, gender, camp_id) VALUES(?,?,?,?,?) "
                + "ON DUPLICATE KEY UPDATE name=VALUES(name)";
        Connection con = null;
        PreparedStatement ps = null;
        try {
            con = DBConnection.getConnection();
            ps = con.prepareStatement(campSql);
            ps.setInt(1, c.getCampId());
            ps.setString(2, c.getName());
            ps.setString(3, c.getLocation());
            ps.setInt(4, c.getCapacity());
            ps.setInt(5, c.getOccupied());
            ps.setInt(6, c.getFoodNeeded());
            ps.setInt(7, c.getWaterNeeded());
            ps.executeUpdate();
            ps.close();

            ps = con.prepareStatement(personSql);
            for (DisplacedPerson p : c.getPeople()) {
                ps.setInt(1, p.getPersonId());
                ps.setString(2, p.getName());
                ps.setInt(3, p.getAge());
                ps.setString(4, p.getGender());
                ps.setInt(5, c.getCampId());
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Save camp failed: " + e.getMessage(), e);
        } finally {
            DBConnection.closeQuietly(ps);
            DBConnection.closeQuietly(con);
        }
    }

    public List<ReliefCamp> findAll() throws DatabaseOperationException {
        List<ReliefCamp> list = new ArrayList<>();
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            con = DBConnection.getConnection();
            ps = con.prepareStatement("SELECT * FROM relief_camps ORDER BY camp_id");
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new ReliefCamp(rs.getInt("camp_id"), rs.getString("name"), rs.getString("location"),
                        rs.getInt("capacity"), rs.getInt("occupied")));
            }
            DBConnection.closeQuietly(rs);
            DBConnection.closeQuietly(ps);
            ps = con.prepareStatement("SELECT * FROM displaced_people WHERE camp_id=?");
            for (ReliefCamp c : list) {
                ps.setInt(1, c.getCampId());
                rs = ps.executeQuery();
                while (rs.next()) {
                    c.addLoadedPerson(new DisplacedPerson(rs.getInt("person_id"), rs.getString("name"),
                            rs.getInt("age"), rs.getString("gender")));
                }
                DBConnection.closeQuietly(rs);
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Load camps failed: " + e.getMessage(), e);
        } finally {
            DBConnection.closeQuietly(ps);
            DBConnection.closeQuietly(con);
        }
    }

    public boolean deleteCamp(int campId) throws DatabaseOperationException {
        Connection con = null;
        PreparedStatement ps = null;
        try {
            con = DBConnection.getConnection();
            ps = con.prepareStatement("DELETE FROM displaced_people WHERE camp_id=?");
            ps.setInt(1, campId);
            ps.executeUpdate();
            ps.close();
            ps = con.prepareStatement("DELETE FROM relief_camps WHERE camp_id=?");
            ps.setInt(1, campId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Delete camp failed: " + e.getMessage(), e);
        } finally {
            DBConnection.closeQuietly(ps);
            DBConnection.closeQuietly(con);
        }
    }
}
