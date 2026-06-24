package gui;

import dao.MaintenanceRequestDAO;
import dao.TechnicianDAO;
import model.MaintenanceRequest;
import model.MaintenanceRequest.Status;
import model.MaintenanceRequest.Priority;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Reports Window — shows summary statistics for admin users.
 * Available only to ADMIN role.
 */
public class ReportsWindow extends JPanel {

    private static final Color BG_DARK     = AdminDashboard.BG_DARK;
    private static final Color BG_CARD     = AdminDashboard.BG_CARD;
    private static final Color ACCENT      = AdminDashboard.ACCENT;
    private static final Color TEXT_WHITE  = AdminDashboard.TEXT_WHITE;
    private static final Color TEXT_MUTED  = AdminDashboard.TEXT_MUTED;
    private static final Color DIVIDER     = AdminDashboard.DIVIDER;

    private final AdminDashboard dashboard;
    private final String role;

    // summary labels
    private JLabel lblTotalRequests, lblPending, lblInProgress, lblCompleted;
    private JLabel lblHigh, lblMedium, lblLow;
    private JLabel lblGenerated;
    private JTable tblByTechnician;
    private JTable tblRecentCompleted;

    public ReportsWindow(AdminDashboard dashboard, String role) {
        this.dashboard = dashboard;
        this.role = role;
        setBackground(BG_DARK);
        setLayout(new BorderLayout(0, 0));
        setBorder(new EmptyBorder(30, 30, 24, 30));
        buildUI();
        loadData();
    }

    private void buildUI() {
        // ── Header ──────────────────────────────────────────────────────────
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BG_DARK);
        header.setBorder(new EmptyBorder(0, 0, 24, 0));

        JLabel title = new JLabel("Reports & Analytics");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(TEXT_WHITE);
        header.add(title, BorderLayout.WEST);

        // Refresh button with a Java2D painted SVG-style rotate/refresh icon
        JButton btnRefresh = new JButton("Refresh") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Button background
                Color bg = getModel().isRollover()
                        ? ACCENT.brighter()
                        : ACCENT;
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);

                // ── Draw refresh icon (circular arrows) ──────────────────────
                int iconSize = 16;
                int iconX    = 16;               // left padding
                int iconY    = (getHeight() - iconSize) / 2;
                int cx       = iconX + iconSize / 2;
                int cy       = iconY + iconSize / 2;
                int r        = iconSize / 2 - 1;

                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

                // Arc: ~300° circle (leaving a gap for the arrowhead)
                g2.drawArc(cx - r, cy - r, r * 2, r * 2, 60, -300);

                // Arrowhead at the end of the arc (at ~60° top-right)
                // The arc ends at 60° (measuring from 3-o-clock, CCW), so tip is at top-right
                double angle = Math.toRadians(60);
                int tipX = cx + (int) Math.round(r * Math.cos(angle));
                int tipY = cy - (int) Math.round(r * Math.sin(angle));

                // Two short lines forming the arrowhead
                int[] arrowXs = { tipX, tipX - 5, tipX + 1 };
                int[] arrowYs = { tipY, tipY - 1, tipY + 5 };
                g2.fillPolygon(arrowXs, arrowYs, 3);

                g2.dispose();

                // Draw label text to the right of the icon
                FontMetrics fm = getFontMetrics(getFont());
                String text = "Refresh";
                int textX = iconX + iconSize + 8;
                int textY = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g.setColor(Color.WHITE);
                g.setFont(getFont());
                g.drawString(text, textX, textY);
            }

            @Override public Dimension getPreferredSize() {
                return new Dimension(120, 36);
            }
        };
        btnRefresh.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnRefresh.setOpaque(false);
        btnRefresh.setContentAreaFilled(false);
        btnRefresh.setBorderPainted(false);
        btnRefresh.setFocusPainted(false);
        btnRefresh.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnRefresh.addActionListener(e -> loadData());
        
        header.add(btnRefresh, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);

        // ── Scrollable body ─────────────────────────────────────────────────
        JPanel body = new JPanel();
        body.setBackground(BG_DARK);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

        // Row 1: Summary cards
        body.add(buildSummarySection());
        body.add(Box.createVerticalStrut(20));

        // Row 2: Priority breakdown + Generated info
        body.add(buildPrioritySection());
        body.add(Box.createVerticalStrut(20));

        // Row 3: By-Technician table
        body.add(buildByTechSection());
        body.add(Box.createVerticalStrut(20));

        // Row 4: Recently completed
        body.add(buildRecentCompletedSection());
        body.add(Box.createVerticalStrut(20));

        // Generated at label
        lblGenerated = new JLabel("Report generated: —");
        lblGenerated.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblGenerated.setForeground(TEXT_MUTED);
        lblGenerated.setAlignmentX(LEFT_ALIGNMENT);
        body.add(lblGenerated);

        JScrollPane scroll = new JScrollPane(body);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getViewport().setBackground(BG_DARK);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);
    }

    // ── Summary Cards ────────────────────────────────────────────────────────
    private JPanel buildSummarySection() {
        JPanel section = new JPanel(new BorderLayout());
        section.setBackground(BG_DARK);
        section.setAlignmentX(LEFT_ALIGNMENT);
        section.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));

        JLabel lbl = sectionTitle("Request Summary");
        section.add(lbl, BorderLayout.NORTH);

        JPanel cards = new JPanel(new GridLayout(1, 4, 14, 0));
        cards.setBackground(BG_DARK);
        cards.setBorder(new EmptyBorder(8, 0, 0, 0));

        lblTotalRequests = new JLabel("—");
        lblPending       = new JLabel("—");
        lblInProgress    = new JLabel("—");
        lblCompleted     = new JLabel("—");

        cards.add(miniStatCard("Total Requests", lblTotalRequests, new Color(139, 92, 246)));
        cards.add(miniStatCard("Pending",         lblPending,       new Color(245, 158, 11)));
        cards.add(miniStatCard("In Progress",     lblInProgress,    new Color(59,  130, 246)));
        cards.add(miniStatCard("Completed",       lblCompleted,     new Color(34,  197, 94)));
        section.add(cards, BorderLayout.CENTER);
        return section;
    }

    // ── Priority Breakdown ───────────────────────────────────────────────────
    private JPanel buildPrioritySection() {
        JPanel section = new JPanel(new BorderLayout());
        section.setBackground(BG_DARK);
        section.setAlignmentX(LEFT_ALIGNMENT);
        section.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));

        section.add(sectionTitle("Priority Breakdown"), BorderLayout.NORTH);

        JPanel cards = new JPanel(new GridLayout(1, 3, 14, 0));
        cards.setBackground(BG_DARK);
        cards.setBorder(new EmptyBorder(8, 0, 0, 0));

        lblHigh   = new JLabel("—");
        lblMedium = new JLabel("—");
        lblLow    = new JLabel("—");

        cards.add(miniStatCard("High Priority",   lblHigh,   new Color(239, 68, 68)));
        cards.add(miniStatCard("Medium Priority", lblMedium, new Color(245, 158, 11)));
        cards.add(miniStatCard("Low Priority",    lblLow,    new Color(34,  197, 94)));
        section.add(cards, BorderLayout.CENTER);
        return section;
    }

    // ── By Technician Table ──────────────────────────────────────────────────
    private JPanel buildByTechSection() {
        JPanel section = cardPanel();
        section.setAlignmentX(LEFT_ALIGNMENT);
        section.setMaximumSize(new Dimension(Integer.MAX_VALUE, 260));
        section.add(sectionTitle("Requests per Technician"), BorderLayout.NORTH);

        String[] cols = {"Technician", "Pending", "In Progress", "Completed", "Total"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblByTechnician = new JTable(model);
        styleTable(tblByTechnician);

        JScrollPane sp = tableScroll(tblByTechnician);
        section.add(sp, BorderLayout.CENTER);
        return section;
    }

    // ── Recently Completed ───────────────────────────────────────────────────
    private JPanel buildRecentCompletedSection() {
        JPanel section = cardPanel();
        section.setAlignmentX(LEFT_ALIGNMENT);
        section.setMaximumSize(new Dimension(Integer.MAX_VALUE, 260));
        section.add(sectionTitle("Recently Completed Requests"), BorderLayout.NORTH);

        String[] cols = {"ID", "Title", "Location", "Priority", "Technician", "Created At"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblRecentCompleted = new JTable(model);
        styleTable(tblRecentCompleted);
        tblRecentCompleted.getColumnModel().getColumn(0).setMaxWidth(50);

        JScrollPane sp = tableScroll(tblRecentCompleted);
        section.add(sp, BorderLayout.CENTER);
        return section;
    }

    // ── Data Loading ─────────────────────────────────────────────────────────
    private void loadData() {
        new SwingWorker<ReportData, Void>() {
            @Override
            protected ReportData doInBackground() {
                MaintenanceRequestDAO rDao = new MaintenanceRequestDAO();
                int total     = rDao.getTotalRecords();
                int pending   = rDao.countByStatus(Status.PENDING);
                int inProg    = rDao.countByStatus(Status.IN_PROGRESS);
                int completed = rDao.countByStatus(Status.COMPLETED);
                int high      = rDao.countByPriority(Priority.HIGH);
                int medium    = rDao.countByPriority(Priority.MEDIUM);
                int low       = rDao.countByPriority(Priority.LOW);
                List<Object[]> byTech         = rDao.getCountsByTechnician();
                List<MaintenanceRequest> done = rDao.getRequestsByStatus(Status.COMPLETED);
                return new ReportData(total, pending, inProg, completed,
                        high, medium, low, byTech, done);
            }

            @Override
            protected void done() {
                try {
                    ReportData d = get();
                    lblTotalRequests.setText(String.valueOf(d.total));
                    lblPending.setText(String.valueOf(d.pending));
                    lblInProgress.setText(String.valueOf(d.inProgress));
                    lblCompleted.setText(String.valueOf(d.completed));
                    lblHigh.setText(String.valueOf(d.high));
                    lblMedium.setText(String.valueOf(d.medium));
                    lblLow.setText(String.valueOf(d.low));

                    DefaultTableModel m1 = (DefaultTableModel) tblByTechnician.getModel();
                    m1.setRowCount(0);
                    for (Object[] row : d.byTechnician) m1.addRow(row);

                    DefaultTableModel m2 = (DefaultTableModel) tblRecentCompleted.getModel();
                    m2.setRowCount(0);
                    for (MaintenanceRequest r : d.recentCompleted) {
                        m2.addRow(new Object[]{
                            r.getRequestId(),
                            r.getTitle(),
                            r.getLocation(),
                            r.getPriority().name(),
                            r.getTechnicianName() != null ? r.getTechnicianName() : "Unassigned",
                            r.getCreatedAtFormatted()
                        });
                    }

                    lblGenerated.setText("Report generated: " +
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }.execute();
    }

    // ── UI Helpers ───────────────────────────────────────────────────────────
    private JPanel miniStatCard(String title, JLabel valLabel, Color accent) {
        JPanel p = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(accent);
                g2.fillRoundRect(0, 0, getWidth(), 4, 4, 4);
                g2.dispose();
            }
        };
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(16, 20, 16, 20));

        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setOpaque(false);

        JLabel lTitle = new JLabel(title);
        lTitle.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lTitle.setForeground(TEXT_MUTED);

        valLabel.setFont(new Font("Segoe UI", Font.BOLD, 30));
        valLabel.setForeground(accent);

        left.add(lTitle);
        left.add(Box.createVerticalStrut(4));
        left.add(valLabel);
        p.add(left, BorderLayout.CENTER);
        return p;
    }

    private JPanel cardPanel() {
        JPanel p = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
            }
        };
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(18, 20, 18, 20));
        return p;
    }

    private JLabel sectionTitle(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lbl.setForeground(TEXT_WHITE);
        lbl.setBorder(new EmptyBorder(0, 0, 10, 0));
        return lbl;
    }

    private void styleTable(JTable table) {
        table.setBackground(BG_CARD);
        table.setForeground(TEXT_WHITE);
        table.setGridColor(DIVIDER);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setRowHeight(34);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.setFocusable(false);
        table.setSelectionBackground(new Color(99, 102, 241, 60));
        table.setSelectionForeground(TEXT_WHITE);
        table.getTableHeader().setBackground(new Color(22, 22, 36));
        table.getTableHeader().setForeground(TEXT_MUTED);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 11));
        table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, DIVIDER));

        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                setBackground(sel ? new Color(99, 102, 241, 60)
                        : (row % 2 == 0 ? BG_CARD : new Color(26, 26, 40)));
                setForeground(TEXT_WHITE);
                setOpaque(true);
                setBorder(new EmptyBorder(0, 10, 0, 10));
                return this;
            }
        };
        table.setDefaultRenderer(Object.class, renderer);
    }

    private JScrollPane tableScroll(JTable table) {
        JScrollPane sp = new JScrollPane(table);
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        sp.getViewport().setBackground(BG_CARD);
        sp.setBorder(BorderFactory.createEmptyBorder());
        return sp;
    }

    // ── Data holder ──────────────────────────────────────────────────────────
    private record ReportData(
        int total, int pending, int inProgress, int completed,
        int high, int medium, int low,
        List<Object[]> byTechnician,
        List<MaintenanceRequest> recentCompleted
    ) {}
}