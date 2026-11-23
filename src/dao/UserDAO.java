package dao;

import exception.DatabaseOperationException;
import model.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    public void insert(User u) throws DatabaseOperationException {
        String sql = "INSERT INTO users(user_id, name, phone, username, password, role) VALUES(?,?,?,?,?,?)";
        Connection con = null;
        PreparedStatement ps = null;
        try {
            con = DBConnection.getConnection();
            ps = con.prepareStatement(sql);
            ps.setInt(1, u.getUserId());
            ps.setString(2, u.getName());
            ps.setString(3, u.getPhone());
            ps.setString(4, u.getUsername());
            ps.setString(5, u.getPassword());
            ps.setString(6, u.getRole());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseOperationException("Insert user failed: " + e.getMessage(), e);
        } finally {
            DBConnection.closeQuietly(ps);
            DBConnection.closeQuietly(con);
        }
    }

    public void update(User u) throws DatabaseOperationException {
        String sql = "UPDATE users SET name=?, phone=?, password=? WHERE user_id=?";
        Connection con = null;
        PreparedStatement ps = null;
        try {
            con = DBConnection.getConnection();
            ps = con.prepareStatement(sql);
            ps.setString(1, u.getName());
            ps.setString(2, u.getPhone());
            ps.setString(3, u.getPassword());
            ps.setInt(4, u.getUserId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseOperationException("Update user failed: " + e.getMessage(), e);
        } finally {
            DBConnection.closeQuietly(ps);
            DBConnection.closeQuietly(con);
        }
    }

    public void delete(int userId) throws DatabaseOperationException {
        String sql = "DELETE FROM users WHERE user_id=?";
        Connection con = null;
        PreparedStatement ps = null;
        try {
            con = DBConnection.getConnection();
            ps = con.prepareStatement(sql);
            ps.setInt(1, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseOperationException("Delete user failed: " + e.getMessage(), e);
        } finally {
            DBConnection.closeQuietly(ps);
            DBConnection.closeQuietly(con);
        }
    }

    public User findById(int userId) throws DatabaseOperationException {
        String sql = "SELECT * FROM users WHERE user_id=?";
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            con = DBConnection.getConnection();
            ps = con.prepareStatement(sql);
            ps.setInt(1, userId);
            rs = ps.executeQuery();
            return rs.next() ? rowToUser(rs) : null;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Find user failed: " + e.getMessage(), e);
        } finally {
            DBConnection.closeQuietly(rs);
            DBConnection.closeQuietly(ps);
            DBConnection.closeQuietly(con);
        }
    }

    public List<User> findAll() throws DatabaseOperationException {
        String sql = "SELECT * FROM users ORDER BY user_id";
        List<User> list = new ArrayList<>();
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            con = DBConnection.getConnection();
            ps = con.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) list.add(rowToUser(rs));
            return list;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Load users failed: " + e.getMessage(), e);
        } finally {
            DBConnection.closeQuietly(rs);
            DBConnection.closeQuietly(ps);
            DBConnection.closeQuietly(con);
        }
    }

    /** Insert if new, otherwise update. */
    public void save(User u) throws DatabaseOperationException {
        if (findById(u.getUserId()) == null) insert(u);
        else update(u);
    }

    private User rowToUser(ResultSet rs) throws SQLException {
        int id = rs.getInt("user_id");
        String name = rs.getString("name");
        String phone = rs.getString("phone");
        String username = rs.getString("username");
        String password = rs.getString("password");
        String role = rs.getString("role");
        switch (role) {
    case "ADMIN": return new Administrator(id, name, phone, username, password);
    case "COORDINATOR": return new RescueCoordinator(id, name, phone, username, password);
    case "TEAM_MEMBER": return new RescueTeamMember(id, name, phone, username, password);
    case "CITIZEN": return new Citizen(id, name, phone, username, password);
    default: return new ReliefCampManager(id, name, phone, username, password);
}
    }
}
