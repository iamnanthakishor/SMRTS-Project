package dao;

import database.DBConnection;
import model.MaintenanceRequest;
import model.MaintenanceRequest.Priority;
import model.MaintenanceRequest.Status;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * MaintenanceRequestDAO — v3.0
 *
 * Changes vs v2.0:
 *   • addRequest() now also stores submitted_by_username when set
 *   • getRequestsByUser(username) — fetch only the requests submitted by a user
 *
 * DB NOTE: To support user submissions, add this column to your table once:
 *   ALTER TABLE maintenance_requests
 *     ADD COLUMN submitted_by_username VARCHAR(100) DEFAULT NULL;
 *
 * The DAO is backwards-compatible: if the column doesn't exist yet the
 * insert falls back to the original 6-column form (submitted_by will be null).
 */
public class MaintenanceRequestDAO {

    private static final Logger logger = Logger.getLogger(MaintenanceRequestDAO.class.getName());

    // ── CREATE ───────────────────────────────────────────────────────────────
    public boolean addRequest(MaintenanceRequest r) {
        // Try extended insert (with submitted_by_username). Fall back if column missing.
        if (r.getSubmittedByUsername() != null && !r.getSubmittedByUsername().isBlank()) {
            String sql = """
                INSERT INTO maintenance_requests
                  (title, description, location, priority, status,
                   assigned_technician_id, submitted_by_username)
                VALUES (?,?,?,?,?,?,?)
                """;
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement pst = conn.prepareStatement(sql)) {
                pst.setString(1, r.getTitle());
                pst.setString(2, r.getDescription());
                pst.setString(3, r.getLocation());
                pst.setString(4, r.getPriority().name());
                pst.setString(5, r.getStatus().name());
                if (r.getAssignedTechnicianId() > 0)
                    pst.setInt(6, r.getAssignedTechnicianId());
                else
                    pst.setNull(6, Types.INTEGER);
                pst.setString(7, r.getSubmittedByUsername());
                pst.executeUpdate();
                return true;
            } catch (SQLException e) {
                // If column doesn't exist yet, fall through to legacy insert
                if (!e.getMessage().toLowerCase().contains("unknown column")) {
                    logger.log(Level.SEVERE, "addRequest (extended) failed", e);
                    return false;
                }
                logger.warning("submitted_by_username column not found — using legacy insert. " +
                               "Run: ALTER TABLE maintenance_requests ADD COLUMN submitted_by_username VARCHAR(100) DEFAULT NULL;");
            }
        }

        // Legacy insert (no submitted_by_username)
        String sql = """
            INSERT INTO maintenance_requests
              (title, description, location, priority, status, assigned_technician_id)
            VALUES (?,?,?,?,?,?)
            """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, r.getTitle());
            pst.setString(2, r.getDescription());
            pst.setString(3, r.getLocation());
            pst.setString(4, r.getPriority().name());
            pst.setString(5, r.getStatus().name());
            if (r.getAssignedTechnicianId() > 0)
                pst.setInt(6, r.getAssignedTechnicianId());
            else
                pst.setNull(6, Types.INTEGER);
            pst.executeUpdate();
            return true;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "addRequest failed", e);
            return false;
        }
    }

    // ── READ ALL ─────────────────────────────────────────────────────────────
    public List<MaintenanceRequest> getAllRequests() {
        List<MaintenanceRequest> list = new ArrayList<>();
        String sql = """
            SELECT r.*, t.name AS tech_name
            FROM maintenance_requests r
            LEFT JOIN technicians t ON r.assigned_technician_id = t.technician_id
            ORDER BY r.created_at DESC
            """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "getAllRequests failed", e);
        }
        return list;
    }

    // ── READ BY SUBMITTED USER ───────────────────────────────────────────────
    /**
     * Returns all requests submitted by the given username.
     * Used by UserPortalWindow to show a user only their own requests.
     *
     * Falls back to getAllRequests() if the column doesn't exist yet.
     */
    public List<MaintenanceRequest> getRequestsByUser(String username) {
        List<MaintenanceRequest> list = new ArrayList<>();
        String sql = """
            SELECT r.*, t.name AS tech_name
            FROM maintenance_requests r
            LEFT JOIN technicians t ON r.assigned_technician_id = t.technician_id
            WHERE r.submitted_by_username = ?
            ORDER BY r.created_at DESC
            """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, username);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            if (e.getMessage().toLowerCase().contains("unknown column")) {
                logger.warning("submitted_by_username column missing — showing all requests for user view. " +
                               "Run: ALTER TABLE maintenance_requests ADD COLUMN submitted_by_username VARCHAR(100) DEFAULT NULL;");
                return getAllRequests(); // graceful fallback
            }
            logger.log(Level.SEVERE, "getRequestsByUser failed", e);
        }
        return list;
    }

    // ── READ BY STATUS ───────────────────────────────────────────────────────
    public List<MaintenanceRequest> getRequestsByStatus(Status status) {
        List<MaintenanceRequest> list = new ArrayList<>();
        String sql = """
            SELECT r.*, t.name AS tech_name
            FROM maintenance_requests r
            LEFT JOIN technicians t ON r.assigned_technician_id = t.technician_id
            WHERE r.status = ?
            ORDER BY r.created_at DESC
            """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, status.name());
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "getRequestsByStatus failed", e);
        }
        return list;
    }

    // ── SEARCH ───────────────────────────────────────────────────────────────
    public List<MaintenanceRequest> searchRequests(String keyword) {
        List<MaintenanceRequest> list = new ArrayList<>();
        String sql = """
            SELECT r.*, t.name AS tech_name
            FROM maintenance_requests r
            LEFT JOIN technicians t ON r.assigned_technician_id = t.technician_id
            WHERE r.title LIKE ? OR r.location LIKE ? OR r.description LIKE ?
            ORDER BY r.created_at DESC
            """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            String kw = "%" + keyword + "%";
            pst.setString(1, kw);
            pst.setString(2, kw);
            pst.setString(3, kw);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "searchRequests failed", e);
        }
        return list;
    }

    // ── UPDATE ───────────────────────────────────────────────────────────────
    public boolean updateRequest(MaintenanceRequest r) {
        String sql = """
            UPDATE maintenance_requests
            SET title=?, description=?, location=?, priority=?, status=?, assigned_technician_id=?
            WHERE request_id=?
            """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, r.getTitle());
            pst.setString(2, r.getDescription());
            pst.setString(3, r.getLocation());
            pst.setString(4, r.getPriority().name());
            pst.setString(5, r.getStatus().name());
            if (r.getAssignedTechnicianId() > 0)
                pst.setInt(6, r.getAssignedTechnicianId());
            else
                pst.setNull(6, Types.INTEGER);
            pst.setInt(7, r.getRequestId());
            pst.executeUpdate();
            return true;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "updateRequest failed", e);
            return false;
        }
    }

    // ── DELETE ───────────────────────────────────────────────────────────────
    public boolean deleteRequest(int id) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(
                     "DELETE FROM maintenance_requests WHERE request_id=?")) {
            pst.setInt(1, id);
            pst.executeUpdate();
            return true;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "deleteRequest failed", e);
            return false;
        }
    }

    // ── COUNTS ───────────────────────────────────────────────────────────────
    public int countByStatus(Status status) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(
                     "SELECT COUNT(*) FROM maintenance_requests WHERE status=?")) {
            pst.setString(1, status.name());
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "countByStatus failed", e);
        }
        return 0;
    }

    // ── RECENT REQUESTS (for dashboard table) ────────────────────────────────
    public List<Object[]> getRecentRequests(int limit) {
        List<Object[]> list = new ArrayList<>();
        String sql = """
            SELECT r.request_id, r.title, r.status, t.name AS tech_name
            FROM maintenance_requests r
            LEFT JOIN technicians t ON r.assigned_technician_id = t.technician_id
            ORDER BY r.created_at DESC
            LIMIT ?
            """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setInt(1, limit);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    list.add(new Object[]{
                        rs.getInt("request_id"),
                        rs.getString("title"),
                        rs.getString("status"),
                        rs.getString("tech_name") != null ? rs.getString("tech_name") : "Unassigned"
                    });
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "getRecentRequests failed", e);
        }
        return list;
    }

    // ── TOTAL RECORD COUNT ───────────────────────────────────────────────────
    public int getTotalRecords() {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(
                     "SELECT COUNT(*) FROM maintenance_requests");
             ResultSet rs = pst.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "getTotalRecords failed", e);
        }
        return 0;
    }

    // ── COUNT BY PRIORITY ────────────────────────────────────────────────────
    public int countByPriority(Priority priority) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(
                     "SELECT COUNT(*) FROM maintenance_requests WHERE priority=?")) {
            pst.setString(1, priority.name());
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "countByPriority failed", e);
        }
        return 0;
    }

    // ── COUNT BY TECHNICIAN ──────────────────────────────────────────────────
    public List<Object[]> getCountsByTechnician() {
        List<Object[]> list = new ArrayList<>();
        String sql = """
            SELECT
                COALESCE(t.name, 'Unassigned') AS tech_name,
                SUM(CASE WHEN r.status = 'PENDING'     THEN 1 ELSE 0 END) AS pending,
                SUM(CASE WHEN r.status = 'IN_PROGRESS' THEN 1 ELSE 0 END) AS in_progress,
                SUM(CASE WHEN r.status = 'COMPLETED'   THEN 1 ELSE 0 END) AS completed,
                COUNT(*) AS total
            FROM maintenance_requests r
            LEFT JOIN technicians t ON r.assigned_technician_id = t.technician_id
            GROUP BY tech_name
            ORDER BY total DESC
            """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                list.add(new Object[]{
                    rs.getString("tech_name"),
                    rs.getInt("pending"),
                    rs.getInt("in_progress"),
                    rs.getInt("completed"),
                    rs.getInt("total")
                });
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "getCountsByTechnician failed", e);
        }
        return list;
    }

    // ── HELPER ───────────────────────────────────────────────────────────────
    private MaintenanceRequest mapRow(ResultSet rs) throws SQLException {
        MaintenanceRequest r = new MaintenanceRequest();
        r.setRequestId(rs.getInt("request_id"));
        r.setTitle(rs.getString("title"));
        r.setDescription(rs.getString("description"));
        r.setLocation(rs.getString("location"));
        r.setPriority(Priority.valueOf(rs.getString("priority")));
        r.setStatus(Status.valueOf(rs.getString("status")));
        r.setAssignedTechnicianId(rs.getInt("assigned_technician_id"));
        r.setTechnicianName(rs.getString("tech_name"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) r.setCreatedAt(ts.toLocalDateTime());

        // Try to read submitted_by_username — may not exist in older schemas
        try {
            r.setSubmittedByUsername(rs.getString("submitted_by_username"));
        } catch (SQLException ignored) { /* column not yet added */ }

        return r;
    }
}
