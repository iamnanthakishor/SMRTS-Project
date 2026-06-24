package dao;

import database.DBConnection;
import model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UserDAO {

    private static final Logger logger = Logger.getLogger(UserDAO.class.getName());

    public boolean addUser(User u) {
        // Ensure full_name column exists (safe for existing DBs)
        ensureFullNameColumn();

        String sql = """
            INSERT INTO users (username, password, role, full_name)
            VALUES(?,?,?,?)
            """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setString(1, u.getUsername());
            // Hash password if not already hashed
            String pw = u.getPassword();
            if (pw == null || pw.isEmpty()) {
                logger.warning("addUser: password is empty");
                return false;
            }
            pst.setString(2, pw);
            pst.setString(3, u.getRole() != null ? u.getRole() : "USER");
            pst.setString(4, u.getFullName() != null ? u.getFullName() : "");

            pst.executeUpdate();
            return true;
        } catch (SQLIntegrityConstraintViolationException e) {
            logger.log(Level.WARNING, "addUser: duplicate username — " + u.getUsername());
            return false;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "addUser failed", e);
            return false;
        }
    }

    /** Adds full_name column if the table was created without it. */
    private void ensureFullNameColumn() {
        try (Connection conn = DBConnection.getConnection()) {
            DatabaseMetaData meta = conn.getMetaData();
            ResultSet cols = meta.getColumns(null, null, "users", "full_name");
            if (!cols.next()) {
                try (Statement st = conn.createStatement()) {
                    st.execute("ALTER TABLE users ADD COLUMN full_name VARCHAR(200) DEFAULT ''");
                    logger.info("Added full_name column to users table.");
                }
            }
        } catch (SQLException e) {
            logger.log(Level.WARNING, "ensureFullNameColumn check skipped", e);
        }
    }

    public List<User> getAllUsers() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY username";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "getAllUsers failed", e);
        }
        return list;
    }

    /** Returns total number of registered users. */
    public int countUsers() {
        String sql = "SELECT COUNT(*) FROM users";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "countUsers failed", e);
        }
        return 0;
    }

    public List<User> searchUsers(String keyword) {
        List<User> list = new ArrayList<>();
        String sql = """
            SELECT * FROM users
            WHERE username LIKE ? OR role LIKE ? OR full_name LIKE ?
            ORDER BY username
            """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            String kw = "%" + keyword + "%";
            pst.setString(1, kw);
            pst.setString(2, kw);
            pst.setString(3, kw);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "searchUsers failed", e);
        }
        return list;
    }

    public boolean updateUser(User u) {
        String sql = """
            UPDATE users SET
            username=?,
            password=?,
            role=?,
            full_name=?
            WHERE user_id=?
            """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, u.getUsername());
            pst.setString(2, u.getPassword());
            pst.setString(3, u.getRole());
            pst.setString(4, u.getFullName() != null ? u.getFullName() : "");
            pst.setInt(5, u.getUserId());
            pst.executeUpdate();
            return true;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "updateUser failed", e);
            return false;
        }
    }

    public boolean deleteUser(int id) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(
                     "DELETE FROM users WHERE user_id=?")) {
            pst.setInt(1, id);
            pst.executeUpdate();
            return true;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "deleteUser failed", e);
            return false;
        }
    }

    private User mapRow(ResultSet rs) throws SQLException {
        // full_name may not exist in very old DBs — fall back gracefully
        String fullName = "";
        try { fullName = rs.getString("full_name"); } catch (SQLException ignored) {}
        return new User(
            rs.getInt("user_id"),
            rs.getString("username"),
            rs.getString("password"),
            rs.getString("role"),
            fullName
        );
    }
}
