package gui;

import model.MaintenanceRequest;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * RequestDetailsWindow — modal dialog that displays full information
 * about a single maintenance request. Open from any table by double-clicking
 * a row or pressing the "View Details" button.
 */
public class RequestDetailsWindow extends JDialog {

    private static final Color BG_DARK    = AdminDashboard.BG_DARK;
    private static final Color BG_CARD    = AdminDashboard.BG_CARD;
    private static final Color BG_CARD_HOVER = AdminDashboard.BG_CARD_HOVER;
    private static final Color ACCENT     = AdminDashboard.ACCENT;
    private static final Color TEXT_WHITE = AdminDashboard.TEXT_WHITE;
    private static final Color TEXT_MUTED = AdminDashboard.TEXT_MUTED;
    private static final Color DIVIDER    = AdminDashboard.DIVIDER;

    public RequestDetailsWindow(Frame owner, MaintenanceRequest req) {
        super(owner, "Request Details — #" + req.getRequestId(), true);
        buildUI(req);
        setSize(560, 600);
        setResizable(false);
        setLocationRelativeTo(owner);
        setBackground(BG_DARK);
    }

    private void buildUI(MaintenanceRequest req) {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG_DARK);
        root.setBorder(new EmptyBorder(24, 28, 24, 28));
        setContentPane(root);

        // ── Title bar ────────────────────────────────────────────────────────
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(BG_DARK);
        topBar.setBorder(new EmptyBorder(0, 0, 18, 0));

        JLabel lblTitle = new JLabel("Request #" + req.getRequestId());
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(TEXT_WHITE);
        topBar.add(lblTitle, BorderLayout.WEST);

        // Status badge
        topBar.add(statusBadge(req.getStatus().name()), BorderLayout.EAST);
        root.add(topBar, BorderLayout.NORTH);

        // ── Content card ─────────────────────────────────────────────────────
        JPanel card = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                // Priority accent bar on left
                Color barColor = priorityColor(req.getPriority().name());
                g2.setColor(barColor);
                g2.fillRoundRect(0, 0, 4, getHeight(), 4, 4);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(22, 26, 22, 26));

        JPanel fields = new JPanel();
        fields.setLayout(new BoxLayout(fields, BoxLayout.Y_AXIS));
        fields.setOpaque(false);

        fields.add(fieldRow("Title", req.getTitle()));
        fields.add(sep());
        fields.add(fieldRow("Location", req.getLocation() != null ? req.getLocation() : "—"));
        fields.add(sep());
        fields.add(fieldRow("Priority", req.getPriority().name()));
        fields.add(sep());
        fields.add(fieldRow("Status", req.getStatus().name()));
        fields.add(sep());
        fields.add(fieldRow("Assigned Technician",
                req.getTechnicianName() != null ? req.getTechnicianName() : "Unassigned"));
        fields.add(sep());
        fields.add(fieldRow("Created At", req.getCreatedAtFormatted()));
        fields.add(sep());
        fields.add(fieldRow("Estimated Hours",
                req.getEstimatedHours() > 0 ? req.getEstimatedHours() + " hrs" : "—"));
        fields.add(sep());
        fields.add(fieldRow("Completion Date",
                req.getCompletionDate() != null ? req.getCompletionDateFormatted() : "—"));

        // Description — multiline
        fields.add(sep());
        JPanel descRow = new JPanel(new BorderLayout());
        descRow.setOpaque(false);
        descRow.setBorder(new EmptyBorder(10, 0, 10, 0));

        JLabel lKey = new JLabel("Description");
        lKey.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lKey.setForeground(TEXT_MUTED);
        lKey.setPreferredSize(new Dimension(150, 20));

        JTextArea area = new JTextArea(req.getDescription() != null ? req.getDescription() : "—");
        area.setBackground(BG_CARD_HOVER);
        area.setForeground(TEXT_WHITE);
        area.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBorder(new EmptyBorder(8, 10, 8, 10));
        area.setRows(4);

        descRow.add(lKey, BorderLayout.NORTH);
        descRow.add(area, BorderLayout.CENTER);
        fields.add(descRow);

        card.add(fields, BorderLayout.CENTER);
        root.add(card, BorderLayout.CENTER);

        // ── Footer button ─────────────────────────────────────────────────────
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        footer.setBackground(BG_DARK);
        footer.setBorder(new EmptyBorder(18, 0, 0, 0));

        JButton btnClose = new JButton("Close") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover()
                        ? new Color(55, 55, 80) : new Color(45, 45, 70));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnClose.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnClose.setForeground(TEXT_WHITE);
        btnClose.setOpaque(false);
        btnClose.setContentAreaFilled(false);
        btnClose.setBorderPainted(false);
        btnClose.setFocusPainted(false);
        btnClose.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnClose.setPreferredSize(new Dimension(110, 38));
        btnClose.addActionListener(e -> dispose());
        footer.add(btnClose);
        root.add(footer, BorderLayout.SOUTH);
    }

    private JPanel fieldRow(String key, String value) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(10, 0, 10, 0));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));

        JLabel lKey = new JLabel(key);
        lKey.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lKey.setForeground(TEXT_MUTED);
        lKey.setPreferredSize(new Dimension(160, 20));

        JLabel lVal = new JLabel(value);
        lVal.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lVal.setForeground(TEXT_WHITE);

        row.add(lKey, BorderLayout.WEST);
        row.add(lVal, BorderLayout.CENTER);
        return row;
    }

    private JPanel sep() {
        JPanel line = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(DIVIDER);
                g.fillRect(0, 0, getWidth(), 1);
            }
        };
        line.setOpaque(false);
        line.setPreferredSize(new Dimension(0, 1));
        line.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        return line;
    }

    private JLabel statusBadge(String status) {
        Color fg = switch (status) {
            case "PENDING"     -> new Color(245, 158, 11);
            case "IN_PROGRESS" -> new Color(59, 130, 246);
            case "COMPLETED"   -> new Color(34, 197, 94);
            default            -> TEXT_MUTED;
        };
        Color bg = new Color(fg.getRed(), fg.getGreen(), fg.getBlue(), 28);

        JLabel badge = new JLabel(status.replace("_", " ")) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        badge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        badge.setForeground(fg);
        badge.setHorizontalAlignment(SwingConstants.CENTER);
        badge.setOpaque(false);
        badge.setBorder(new EmptyBorder(5, 14, 5, 14));
        badge.setPreferredSize(new Dimension(120, 30));
        return badge;
    }

    private Color priorityColor(String priority) {
        return switch (priority) {
            case "HIGH"   -> new Color(239, 68, 68);
            case "MEDIUM" -> new Color(245, 158, 11);
            case "LOW"    -> new Color(34, 197, 94);
            default       -> ACCENT;
        };
    }
}
