package model;

import java.time.LocalDateTime;

/**
 * ChatMessage — represents a single message in the AI Chatbot conversation.
 */
public class ChatMessage {

    private int id;
    private String role;          // "user" or "assistant"
    private String content;
    private String username;      // logged-in user who owns this session
    private String userRole;      // "Admin" or "Technician"
    private LocalDateTime createdAt;

    public ChatMessage() {}

    public ChatMessage(String role, String content, String username, String userRole) {
        this.role      = role;
        this.content   = content;
        this.username  = username;
        this.userRole  = userRole;
        this.createdAt = LocalDateTime.now();
    }

    public int getId()                      { return id; }
    public void setId(int id)               { this.id = id; }

    public String getRole()                 { return role; }
    public void setRole(String role)        { this.role = role; }

    public String getContent()              { return content; }
    public void setContent(String content)  { this.content = content; }

    public String getUsername()             { return username; }
    public void setUsername(String u)       { this.username = u; }

    public String getUserRole()             { return userRole; }
    public void setUserRole(String r)       { this.userRole = r; }

    public LocalDateTime getCreatedAt()           { return createdAt; }
    public void setCreatedAt(LocalDateTime dt)     { this.createdAt = dt; }
}
