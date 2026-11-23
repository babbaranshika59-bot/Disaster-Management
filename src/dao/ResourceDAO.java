package dao;

import exception.DatabaseOperationException;
import model.Resource;
import model.ResourceAllocation;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ResourceDAO {

    /** Create or update a resource row (MySQL upsert). */
    public void save(Resource r) throws DatabaseOperationException {
        String sql = "INSERT INTO resources(resource_id, name, category, total_qty, allocated_qty) VALUES(?,?,?,?,?) "
                + "ON DUPLICATE KEY UPDATE name=VALUES(name), category=VALUES(category), "
                + "total_qty=VALUES(total_qty), allocated_qty=VALUES(allocated_qty)";
        Connection con = null;
        PreparedStatement ps = null;
        try {
            con = DBConnection.getConnection();
            ps = con.prepareStatement(sql);
            ps.setInt(1, r.getResourceId());
            ps.setString(2, r.getName());
            ps.setString(3, r.getCategory());
            ps.setInt(4, r.getTotalQty());
            ps.setInt(5, r.getAllocatedQty());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseOperationException("Save resource failed: " + e.getMessage(), e);
        } finally {
            DBConnection.closeQuietly(ps);
            DBConnection.closeQuietly(con);
        }
    }

    public void saveAllocation(ResourceAllocation a) throws DatabaseOperationException {
        String sql = "INSERT INTO resource_allocations(allocation_id, resource_id, target_type, target_id, quantity, allocated_at) "
                + "VALUES(?,?,?,?,?,?) ON DUPLICATE KEY UPDATE quantity=VALUES(quantity)";
        Connection con = null;
        PreparedStatement ps = null;
        try {
            con = DBConnection.getConnection();
            ps = con.prepareStatement(sql);
            ps.setInt(1, a.getAllocationId());
            ps.setInt(2, a.getResourceId());
            ps.setString(3, a.getTargetType());
            ps.setInt(4, a.getTargetId());
            ps.setInt(5, a.getQuantity());
            ps.setString(6, a.getAllocatedAt());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseOperationException("Save allocation failed: " + e.getMessage(), e);
        } finally {
            DBConnection.closeQuietly(ps);
            DBConnection.closeQuietly(con);
        }
    }

    public boolean delete(int resourceId) throws DatabaseOperationException {
        Connection con = null;
        PreparedStatement ps = null;
        try {
            con = DBConnection.getConnection();
            ps = con.prepareStatement("DELETE FROM resources WHERE resource_id=?");
            ps.setInt(1, resourceId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Delete resource failed: " + e.getMessage(), e);
        } finally {
            DBConnection.closeQuietly(ps);
            DBConnection.closeQuietly(con);
        }
    }

    public List<Resource> findAll() throws DatabaseOperationException {
        List<Resource> list = new ArrayList<>();
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            con = DBConnection.getConnection();
            ps = con.prepareStatement("SELECT * FROM resources ORDER BY resource_id");
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new Resource(rs.getInt("resource_id"), rs.getString("name"), rs.getString("category"),
                        rs.getInt("total_qty"), rs.getInt("allocated_qty")));
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Load resources failed: " + e.getMessage(), e);
        } finally {
            DBConnection.closeQuietly(rs);
            DBConnection.closeQuietly(ps);
            DBConnection.closeQuietly(con);
        }
    }

    public List<String> findAllAllocations() throws DatabaseOperationException {
        List<String> rows = new ArrayList<>();
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            con = DBConnection.getConnection();
            ps = con.prepareStatement("SELECT a.allocation_id, r.name, a.quantity, a.target_type, a.target_id "
                    + "FROM resource_allocations a JOIN resources r ON a.resource_id = r.resource_id ORDER BY a.allocation_id");
            rs = ps.executeQuery();
            while (rs.next()) {
                rows.add("Alloc #" + rs.getInt(1) + " " + rs.getString(2) + " qty=" + rs.getInt(3)
                        + " -> " + rs.getString(4) + " #" + rs.getInt(5));
            }
            return rows;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Load allocations failed: " + e.getMessage(), e);
        } finally {
            DBConnection.closeQuietly(rs);
            DBConnection.closeQuietly(ps);
            DBConnection.closeQuietly(con);
        }
    }
}
