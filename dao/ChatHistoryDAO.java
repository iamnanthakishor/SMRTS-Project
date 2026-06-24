package dao;

import database.DBConnection;
import model.ChatMessage;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ChatHistoryDAO — handles persistence of AI chatbot messages.
 * Requires the chat_history table (see sql/chat_history.sql).
 */
public class ChatHistoryDAO {

    private static final Logger logger = Logger.getLogger(ChatHistoryDAO.class.getName());

    public boolean save(ChatMessage msg) {
        String sql = """
            INSERT INTO chat_history (role, content, username, user_role, created_at)
            VALUES (?, ?, ?, ?, ?)
            """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, msg.getRole());
            pst.setString(2, msg.getContent());
            pst.setString(3, msg.getUsername());
            pst.setString(4, msg.getUserRole());
            pst.setTimestamp(5, Timestamp.valueOf(
                    msg.getCreatedAt() != null ? msg.getCreatedAt()
                            : java.time.LocalDateTime.now()));
            pst.executeUpdate();
            return true;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "ChatHistoryDAO.save failed", e);
            return false;
        }
    }

    /** Returns the most recent N messages for a specific user, oldest first */
    public List<ChatMessage> getRecentByUser(String username, int limit) {
        List<ChatMessage> list = new ArrayList<>();
        String sql = """
            SELECT * FROM (
                SELECT * FROM chat_history
                WHERE username = ?
                ORDER BY created_at DESC
                LIMIT ?
            ) sub
            ORDER BY created_at ASC
            """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, username);
            pst.setInt(2, limit);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "ChatHistoryDAO.getRecentByUser failed", e);
        }
        return list;
    }

    /** Returns all messages (admin overview) */
    public List<ChatMessage> getAll(int limit) {
        List<ChatMessage> list = new ArrayList<>();
        String sql = "SELECT * FROM chat_history ORDER BY created_at DESC LIMIT ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setInt(1, limit);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "ChatHistoryDAO.getAll failed", e);
        }
        return list;
    }

    /** Delete all messages for a user */
    public boolean clearByUser(String username) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(
                     "DELETE FROM chat_history WHERE username = ?")) {
            pst.setString(1, username);
            pst.executeUpdate();
            return true;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "ChatHistoryDAO.clearByUser failed", e);
            return false;
        }
    }

    private ChatMessage mapRow(ResultSet rs) throws SQLException {
        ChatMessage m = new ChatMessage();
        m.setId(rs.getInt("id"));
        m.setRole(rs.getString("role"));
        m.setContent(rs.getString("content"));
        m.setUsername(rs.getString("username"));
        m.setUserRole(rs.getString("user_role"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) m.setCreatedAt(ts.toLocalDateTime());
        return m;
    }
}
