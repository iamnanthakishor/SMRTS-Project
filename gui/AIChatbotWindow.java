package gui;

import dao.ChatHistoryDAO;
import dao.MaintenanceRequestDAO;
import dao.TechnicianDAO;
import dao.UserDAO;
import model.ChatMessage;
import model.MaintenanceRequest;
import model.Technician;
import model.MaintenanceRequest.Status;
import model.MaintenanceRequest.Priority;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * OFFLINE AI Chatbot — Enhanced Intelligence
 */
public class AIChatbotWindow extends JPanel {

    private static final Color BG_DARK       = AdminDashboard.BG_DARK;
    private static final Color BG_CARD       = AdminDashboard.BG_CARD;
    private static final Color BG_CARD_HOVER = AdminDashboard.BG_CARD_HOVER;
    private static final Color ACCENT        = AdminDashboard.ACCENT;
    private static final Color TEXT_WHITE    = AdminDashboard.TEXT_WHITE;
    private static final Color TEXT_MUTED    = AdminDashboard.TEXT_MUTED;

    private final String loggedInUser;
    private final String role;
    private final AdminDashboard dashboard;

    private JPanel chatPanel;
    private JScrollPane chatScroll;
    private JTextArea inputArea;
    private JButton btnSend;
    private JButton btnClear;
    private JLabel lblStatus;

    private final List<ChatMessage> conversationHistory = new ArrayList<>();

    private final MaintenanceRequestDAO requestDAO = new MaintenanceRequestDAO();
    private final TechnicianDAO technicianDAO = new TechnicianDAO();
    private final ChatHistoryDAO chatDAO = new ChatHistoryDAO();

    public AIChatbotWindow(AdminDashboard dashboard, String loggedInUser, String role) {
        this.dashboard = dashboard;
        this.loggedInUser = loggedInUser;
        this.role = role;
        setBackground(BG_DARK);
        setLayout(new BorderLayout(0, 0));
        setBorder(new EmptyBorder(30, 30, 24, 30));
        buildUI();
        showWelcomeMessage();
    }

    private void buildUI() {
        // Header (same as before)
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BG_DARK);
        header.setBorder(new EmptyBorder(0, 0, 18, 0));

        JPanel titleBlock = new JPanel();
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.setBackground(BG_DARK);

        JLabel lblTitle = new JLabel("SMRTS AI Assistant");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setForeground(TEXT_WHITE);

        JLabel lblSubtitle = new JLabel("Offline • Enhanced Intelligence");
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSubtitle.setForeground(TEXT_MUTED);

        titleBlock.add(lblTitle);
        titleBlock.add(Box.createVerticalStrut(3));
        titleBlock.add(lblSubtitle);
        header.add(titleBlock, BorderLayout.WEST);

        JPanel headerBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        headerBtns.setBackground(BG_DARK);
        btnClear = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                // Background
                Color bg = new Color(55, 55, 80);
                g2.setColor(getModel().isRollover() ? bg.brighter() : bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);

                // ── Trash can icon ──────────────────────────────────────────
                Color iconColor = new Color(200, 200, 220);
                g2.setColor(iconColor);
                g2.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

                int iconW = 13, iconH = 16;
                int ix = 12;
                int iy = (getHeight() - iconH) / 2;

                // handle / lid top bar
                g2.drawLine(ix, iy + 4, ix + iconW, iy + 4);
                // handle left post
                g2.drawLine(ix + 3, iy + 4, ix + 3, iy + 1);
                // handle right post
                g2.drawLine(ix + 10, iy + 4, ix + 10, iy + 1);
                // handle top
                g2.drawLine(ix + 3, iy + 1, ix + 10, iy + 1);
                // body
                g2.drawRoundRect(ix + 1, iy + 5, 11, 10, 3, 3);
                // inner vertical lines
                g2.drawLine(ix + 4, iy + 7, ix + 4, iy + 12);
                g2.drawLine(ix + 7, iy + 7, ix + 7, iy + 12);
                g2.drawLine(ix + 10, iy + 7, ix + 10, iy + 12);

                // ── Label text ──────────────────────────────────────────────
                g2.setFont(getFont());
                g2.setColor(iconColor);
                FontMetrics fm = g2.getFontMetrics();
                String label = "Clear Chat";
                int tx = ix + iconW + 6;
                int ty = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(label, tx, ty);

                g2.dispose();
            }
            @Override public Dimension getPreferredSize() { return new Dimension(135, 36); }
        };
        btnClear.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnClear.setOpaque(false);
        btnClear.setContentAreaFilled(false);
        btnClear.setBorderPainted(false);
        btnClear.setFocusPainted(false);
        btnClear.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnClear.addActionListener(e -> clearChat());
        headerBtns.add(btnClear);
        header.add(headerBtns, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);

        chatPanel = new JPanel();
        chatPanel.setLayout(new BoxLayout(chatPanel, BoxLayout.Y_AXIS));
        chatPanel.setBackground(BG_DARK);
        chatPanel.setBorder(new EmptyBorder(0, 0, 12, 0));

        chatScroll = new JScrollPane(chatPanel);
        chatScroll.setOpaque(false);
        chatScroll.getViewport().setOpaque(false);
        chatScroll.getViewport().setBackground(BG_DARK);
        chatScroll.setBorder(BorderFactory.createEmptyBorder());
        add(chatScroll, BorderLayout.CENTER);

        // Input Bar (same)
        JPanel inputBar = new JPanel(new BorderLayout(10, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.dispose();
            }
        };
        inputBar.setOpaque(false);
        inputBar.setBorder(new EmptyBorder(12, 14, 12, 14));

        inputArea = new JTextArea(3, 10);
        inputArea.setBackground(BG_CARD);
        inputArea.setForeground(TEXT_WHITE);
        inputArea.setCaretColor(ACCENT);
        inputArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        inputArea.setLineWrap(true);
        inputArea.setWrapStyleWord(true);
        inputArea.setBorder(BorderFactory.createEmptyBorder());

        inputArea.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER && e.isControlDown()) {
                    e.consume();
                    sendMessage();
                }
            }
        });

        JScrollPane inputScroll = new JScrollPane(inputArea);
        inputScroll.setOpaque(false);
        inputScroll.getViewport().setOpaque(false);
        inputScroll.getViewport().setBackground(BG_CARD);
        inputScroll.setBorder(BorderFactory.createEmptyBorder());

        btnSend = new JButton("Send") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color c = isEnabled() ? (getModel().isRollover() ? new Color(79, 82, 221) : ACCENT) : new Color(60, 60, 90);
                g2.setColor(c);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnSend.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnSend.setForeground(Color.WHITE);
        btnSend.setOpaque(false);
        btnSend.setContentAreaFilled(false);
        btnSend.setBorderPainted(false);
        btnSend.setFocusPainted(false);
        btnSend.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnSend.setPreferredSize(new Dimension(90, 60));
        btnSend.addActionListener(e -> sendMessage());

        JLabel hint = new JLabel("Ctrl+Enter to send");
        hint.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        hint.setForeground(TEXT_MUTED);

        JPanel inputRight = new JPanel(new BorderLayout(0, 4));
        inputRight.setOpaque(false);
        inputRight.add(btnSend, BorderLayout.CENTER);
        inputRight.add(hint, BorderLayout.SOUTH);

        inputBar.add(inputScroll, BorderLayout.CENTER);
        inputBar.add(inputRight, BorderLayout.EAST);

        lblStatus = new JLabel(" ");
        lblStatus.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblStatus.setForeground(TEXT_MUTED);

        JPanel south = new JPanel(new BorderLayout(0, 6));
        south.setBackground(BG_DARK);
        south.add(inputBar, BorderLayout.CENTER);
        south.add(lblStatus, BorderLayout.SOUTH);
        add(south, BorderLayout.SOUTH);
    }

    private void sendMessage() {
        String text = inputArea.getText().trim();
        if (text.isEmpty()) return;

        inputArea.setText("");
        appendBubble(loggedInUser, text, true, false);

        ChatMessage userMsg = new ChatMessage("user", text, loggedInUser, role);
        conversationHistory.add(userMsg);
        saveChatMessage(userMsg);

        btnSend.setEnabled(false);
        lblStatus.setText("Thinking...");

        new SwingWorker<String, Void>() {
            @Override protected String doInBackground() {
                return generateOfflineResponse(text.toLowerCase());
            }
            @Override protected void done() {
                try {
                    String reply = get();
                    appendBubble("SMRTS AI", reply, false, false);
                    saveChatMessage(new ChatMessage("assistant", reply, "AI", "SYSTEM"));
                } catch (Exception ex) {
                    appendBubble("SMRTS AI", "Sorry, I couldn't understand that.", false, true);
                } finally {
                    btnSend.setEnabled(true);
                    lblStatus.setText(" ");
                }
            }
        }.execute();
    }

    /** ─────── ENHANCED INTELLIGENCE ─────── */
    private String generateOfflineResponse(String q) {
        boolean isAdmin = isAdmin();

        // 1. Statistics
        if (containsAny(q, "pending", "how many pending")) {
            return "There are **" + requestDAO.countByStatus(Status.PENDING) + "** pending requests.";
        }
        if (containsAny(q, "in progress", "ongoing", "progress")) {
            return "There are **" + requestDAO.countByStatus(Status.IN_PROGRESS) + "** requests in progress.";
        }
        if (containsAny(q, "completed", "done")) {
            return "Total completed requests: **" + requestDAO.countByStatus(Status.COMPLETED) + "**.";
        }
        if (containsAny(q, "total request", "how many request", "all request")) {
            return "Total maintenance requests in the system: **" + requestDAO.getTotalRecords() + "**.";
        }
        if (containsAny(q, "high priority", "priority high")) {
            return "High priority requests: **" + requestDAO.countByPriority(Priority.HIGH) + "**.";
        }

        // 2. Technicians
        if (containsAny(q, "technician", "tech", "staff")) {
            List<Technician> techs = technicianDAO.getAllTechnicians();
            if (techs.isEmpty()) return "No technicians found.";

            long available = techs.stream().filter(t -> "Available".equalsIgnoreCase(t.getStatus())).count();
            StringBuilder sb = new StringBuilder("**Technicians Overview**\n");
            sb.append("Total: ").append(techs.size()).append(" | Available: ").append(available).append("\n\n");
            techs.stream().limit(10).forEach(t ->
                sb.append("• ").append(t.getName()).append(" (").append(t.getStatus()).append(" - ").append(t.getDepartment()).append(")\n"));
            return sb.toString();
        }

        // 3. Requests
        if (containsAny(q, "request", "work order", "job")) {
            List<MaintenanceRequest> requests = requestDAO.getAllRequests();
            if (requests.isEmpty()) return "No maintenance requests found.";

            StringBuilder sb = new StringBuilder("**Recent Requests**\n");
            requests.stream().limit(8).forEach(r -> 
                sb.append("• #").append(r.getRequestId())
                  .append(" | ").append(r.getTitle())
                  .append(" [").append(r.getStatus()).append("]\n"));
            return sb.toString();
        }

        if (containsAny(q, "my request", "assigned", "my job")) {
            List<MaintenanceRequest> all = requestDAO.getAllRequests();
            List<MaintenanceRequest> mine = all.stream()
                    .filter(r -> r.getTechnicianName() != null && 
                           r.getTechnicianName().toLowerCase().contains(loggedInUser.toLowerCase()))
                    .collect(Collectors.toList());

            if (mine.isEmpty()) return "You have no assigned requests at the moment.";
            
            StringBuilder sb = new StringBuilder("**Your Assigned Requests**\n");
            mine.forEach(r -> sb.append("• #").append(r.getRequestId())
                                .append(" - ").append(r.getTitle())
                                .append(" [").append(r.getStatus()).append("]\n"));
            return sb.toString();
        }

        // Default Help
        return "Hi! You can ask me:\n" +
               "• How many pending requests?\n" +
               "• Show technicians / List techs\n" +
               "• Recent requests\n" +
               "• My requests (for technicians)\n" +
               "• High priority requests\n" +
               "• Total requests";
    }

    private boolean containsAny(String text, String... keywords) {
        return java.util.Arrays.stream(keywords).anyMatch(text::contains);
    }

    private void appendBubble(String sender, String text, boolean isUser, boolean isError) {
        SwingUtilities.invokeLater(() -> {
            JPanel bubble = new JPanel(new BorderLayout(0, 4)) {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    Color bg = isUser ? new Color(ACCENT.getRed(), ACCENT.getGreen(), ACCENT.getBlue(), 40)
                            : (isError ? new Color(239, 68, 68, 25) : BG_CARD);
                    g2.setColor(bg);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                    if (isUser) {
                        g2.setColor(ACCENT);
                        g2.fillRoundRect(getWidth() - 4, 0, 4, getHeight(), 4, 4);
                    } else {
                        g2.setColor(isError ? new Color(239, 68, 68) : new Color(99, 102, 241));
                        g2.fillRoundRect(0, 0, 4, getHeight(), 4, 4);
                    }
                    g2.dispose();
                }
            };
            bubble.setOpaque(false);
            bubble.setAlignmentX(Component.LEFT_ALIGNMENT);
            bubble.setBorder(new EmptyBorder(10, 14, 10, 14));
            bubble.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

            // Sender row: drawn bot icon + name + timestamp
            JPanel senderRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
            senderRow.setOpaque(false);

            if (!isUser) {
                // Small robot/AI icon drawn with Java2D
                JPanel botIcon = new JPanel() {
                    @Override protected void paintComponent(Graphics g) {
                        super.paintComponent(g);
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        Color ic = isError ? new Color(239, 68, 68) : new Color(99, 102, 241);
                        g2.setColor(ic);
                        g2.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                        // head
                        g2.drawRoundRect(2, 4, 10, 8, 3, 3);
                        // antenna
                        g2.drawLine(7, 4, 7, 1);
                        g2.fillOval(6, 0, 3, 3);
                        // eyes
                        g2.fillOval(4, 6, 2, 2);
                        g2.fillOval(8, 6, 2, 2);
                        // mouth
                        g2.drawLine(5, 10, 9, 10);
                        g2.dispose();
                    }
                    @Override public Dimension getPreferredSize() { return new Dimension(14, 14); }
                };
                botIcon.setOpaque(false);
                senderRow.add(botIcon);
            }

            JLabel lblSender = new JLabel(sender + "  ·  " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));
            lblSender.setFont(new Font("Segoe UI", Font.BOLD, 10));
            lblSender.setForeground(isUser ? ACCENT : TEXT_MUTED);
            senderRow.add(lblSender);
            bubble.add(senderRow, BorderLayout.NORTH);

            JTextArea msgArea = new JTextArea(text);
            msgArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            msgArea.setForeground(TEXT_WHITE);
            msgArea.setBackground(new Color(0,0,0,0));
            msgArea.setOpaque(false);
            msgArea.setEditable(false);
            msgArea.setLineWrap(true);
            msgArea.setWrapStyleWord(true);
            bubble.add(msgArea, BorderLayout.CENTER);

            chatPanel.add(bubble);
            chatPanel.add(Box.createVerticalStrut(8));
            chatPanel.revalidate();
            chatPanel.repaint();

            SwingUtilities.invokeLater(() -> chatScroll.getVerticalScrollBar().setValue(chatScroll.getVerticalScrollBar().getMaximum()));
        });
    }

    private void showWelcomeMessage() {
        String welcome = isAdmin() ?
            "Hello " + loggedInUser + "! I'm your enhanced offline AI Assistant.\n\nTry asking:\n• How many pending requests?\n• Show technicians\n• Recent requests" :
            "Hello " + loggedInUser + "! I'm your offline assistant.\nI can help with your assigned work.";
        appendBubble("SMRTS AI", welcome, false, false);
    }

    private void clearChat() {
        if (JOptionPane.showConfirmDialog(this, "Clear chat history?", "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            chatPanel.removeAll();
            chatPanel.revalidate();
            chatPanel.repaint();
            conversationHistory.clear();
            showWelcomeMessage();
        }
    }

    private void saveChatMessage(ChatMessage msg) {
        new SwingWorker<Void, Void>() {
            @Override protected Void doInBackground() {
                chatDAO.save(msg);
                return null;
            }
        }.execute();
    }

    private void loadChatHistory() {
        new SwingWorker<List<ChatMessage>, Void>() {
            @Override protected List<ChatMessage> doInBackground() {
                return chatDAO.getRecentByUser(loggedInUser, 15);
            }
            @Override protected void done() {
                try {
                    List<ChatMessage> history = get();
                    if (!history.isEmpty()) {
                        chatPanel.removeAll();
                        for (ChatMessage m : history) {
                            boolean isUserMsg = "user".equals(m.getRole());
                            appendBubble(isUserMsg ? loggedInUser : "SMRTS AI", m.getContent(), isUserMsg, false);
                        }
                        conversationHistory.addAll(history);
                    }
                } catch (Exception ignored) {}
            }
        }.execute();
    }

    private boolean isAdmin() {
        return "Admin".equalsIgnoreCase(role) || "ADMIN".equalsIgnoreCase(role);
    }
}