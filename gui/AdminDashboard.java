package gui;

import dao.MaintenanceRequestDAO;
import dao.TechnicianDAO;
import dao.UserDAO;
import model.MaintenanceRequest.Status;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.geom.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Main application window — sidebar with SVG icons + CardLayout content area.
 * Starts maximised (full-screen) on every launch.
 */
public class AdminDashboard extends JFrame {

    // ── Palette ──────────────────────────────────────────────────────────────
    static final Color BG_DARK = new Color(18, 18, 28);
    static final Color BG_SIDEBAR = new Color(22, 22, 36);
    static final Color BG_CARD = new Color(30, 30, 46);
    static final Color BG_CARD_HOVER = new Color(36, 36, 56);
    static final Color ACCENT = new Color(99, 102, 241);
    static final Color ACCENT_HOVER = new Color(79, 82, 221);
    static final Color TEXT_WHITE = new Color(240, 240, 255);
    static final Color TEXT_MUTED = new Color(140, 140, 175);
    static final Color DIVIDER = new Color(40, 40, 60);

    private static final String VIEW_HOME    = "HOME";
    private static final String VIEW_TECH    = "TECHNICIANS";
    private static final String VIEW_REQ     = "REQUESTS";
    private static final String VIEW_USERS   = "USERS";
    private static final String VIEW_REPORTS = "REPORTS";
    private static final String VIEW_CHATBOT = "CHATBOT";

    private JPanel contentPanel;
    private CardLayout cardLayout;

    private JButton btnHome, btnTech, btnReq, btnUsers, btnReports, btnChatbot, btnAbout, btnLogout;

    private final String loggedInUser;
    private final String role;

    // Stat panels
    private JPanel pendingPanel, inProgressPanel, completedPanel, techPanel, usersStatPanel;

    // Dynamic labels
    private JTable recentTable;
    private JLabel lblServerTime, lblTotalRecords;

    public AdminDashboard(String username, String role) {
        this.loggedInUser = username;
        this.role = role;
        buildUI();
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setVisible(true);
        showView(VIEW_HOME);
        refreshStats();
        startClock();
    }

    // ── Shell ────────────────────────────────────────────────────────────────
    private void buildUI() {
        setTitle("SMRTS — Dashboard");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1000, 620));

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG_DARK);
        setContentPane(root);

        root.add(buildSidebar(), BorderLayout.WEST);
        root.add(buildContentArea(), BorderLayout.CENTER);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // SIDEBAR
    // ══════════════════════════════════════════════════════════════════════════
    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(BG_SIDEBAR);
        sidebar.setPreferredSize(new Dimension(230, 0));
        sidebar.setBorder(new EmptyBorder(0, 0, 0, 0));

        // ── Brand header ─────────────────────────────────────────────────────
        JPanel brand = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 18));
        brand.setBackground(BG_SIDEBAR);
        brand.setAlignmentX(LEFT_ALIGNMENT);
        brand.setMaximumSize(new Dimension(Integer.MAX_VALUE, 72));

        // Avatar
        JLabel avatar = new JLabel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, ACCENT, 40, 40, new Color(139, 92, 246));
                g2.setPaint(gp);
                g2.fillOval(0, 0, 40, 40);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 17));
                FontMetrics fm = g2.getFontMetrics();
                String init = loggedInUser.isEmpty() ? "?" :
                              String.valueOf(Character.toUpperCase(loggedInUser.charAt(0)));
                g2.drawString(init, (40 - fm.stringWidth(init)) / 2,
                              (40 + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        avatar.setPreferredSize(new Dimension(40, 40));

        JPanel nameBlock = new JPanel();
        nameBlock.setLayout(new BoxLayout(nameBlock, BoxLayout.Y_AXIS));
        nameBlock.setBackground(BG_SIDEBAR);

        JLabel lName = new JLabel(loggedInUser);
        lName.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lName.setForeground(TEXT_WHITE);

        JLabel lRole = new JLabel(role);
        lRole.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lRole.setForeground(TEXT_MUTED);

        nameBlock.add(lName);
        nameBlock.add(lRole);

        brand.add(avatar);
        brand.add(nameBlock);
        sidebar.add(brand);

        // Divider
        sidebar.add(dividerLine());

        // ── App label with logo (fixed) ──────────────────────────────────────
        JPanel appLbl = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
        appLbl.setBackground(BG_SIDEBAR);
        appLbl.setAlignmentX(LEFT_ALIGNMENT);
        appLbl.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        JLabel logoLabel = new JLabel();
        try {
            ImageIcon logo = new ImageIcon(getClass().getResource("/logo.png"));
            if (logo.getIconWidth() > 0) {
                Image img = logo.getImage().getScaledInstance(28, 28, Image.SCALE_SMOOTH);
                logoLabel.setIcon(new ImageIcon(img));
            } else {
                logoLabel.setText("⚙");
            }
        } catch (Exception e) {
            logoLabel.setText("⚙");
        }
        logoLabel.setPreferredSize(new Dimension(28, 28));

        JLabel lApp = new JLabel("SMRTS");
        lApp.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lApp.setForeground(ACCENT);

        appLbl.add(logoLabel);
        appLbl.add(lApp);
        sidebar.add(appLbl);

        // ── Nav section label ─────────────────────────────────────────────────
        sidebar.add(sectionLabel("NAVIGATION"));

        // ── Nav buttons ───────────────────────────────────────────────────────
        btnHome = navButton("Dashboard", SvgIcon.HOME, VIEW_HOME);
        btnTech = navButton("Technicians", SvgIcon.USERS, VIEW_TECH);
        btnReq = navButton("Requests", SvgIcon.CLIPBOARD, VIEW_REQ);
        btnUsers = navButton("Users", SvgIcon.PEOPLE, VIEW_USERS);

        sidebar.add(btnHome);
        sidebar.add(btnTech);
        sidebar.add(btnReq);
        sidebar.add(btnUsers);

        // ── Tools section ─────────────────────────────────────────────────────
        sidebar.add(sectionLabel("TOOLS"));
        btnReports = navButton("Reports", SvgIcon.CHART, VIEW_REPORTS);
        btnChatbot = navButton("AI Chatbot", SvgIcon.AI, VIEW_CHATBOT);
        sidebar.add(btnReports);
        sidebar.add(btnChatbot);

        sidebar.add(Box.createVerticalGlue());
        sidebar.add(dividerLine());

        // About
        btnAbout = navButton("About System", SvgIcon.INFO, null);
        btnAbout.addActionListener(e -> new AboutSystemWindow(
                AdminDashboard.this, loggedInUser, role).setVisible(true));
        sidebar.add(btnAbout);

        // ── Logout ───────────────────────────────────────────────────────────
        btnLogout = navButton("Logout", SvgIcon.LOGOUT, null);
        btnLogout.setForeground(new Color(239, 68, 68));
        btnLogout.addActionListener(e -> { dispose(); new LoginForm().setVisible(true); });
        sidebar.add(btnLogout);
        sidebar.add(Box.createVerticalStrut(12));

        return sidebar;
    }

    private JComponent dividerLine() {
        JPanel line = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(DIVIDER);
                g.fillRect(0, 0, getWidth(), 1);
            }
        };
        line.setOpaque(false);
        line.setPreferredSize(new Dimension(0, 1));
        line.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        line.setAlignmentX(LEFT_ALIGNMENT);
        return line;
    }

    private JLabel sectionLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lbl.setForeground(new Color(90, 90, 120));
        lbl.setBorder(new EmptyBorder(14, 20, 6, 0));
        lbl.setAlignmentX(LEFT_ALIGNMENT);
        return lbl;
    }

    /** Nav button with inline SVG icon */
    private JButton navButton(String label, SvgIcon icon, String view) {
        JButton btn = new JButton() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                boolean active = getBackground().equals(new Color(40, 40, 65));
                boolean hover = getModel().isRollover();

                if (active) {
                    g2.setColor(new Color(99, 102, 241, 30));
                    g2.fillRoundRect(8, 4, getWidth() - 16, getHeight() - 8, 10, 10);
                    g2.setColor(ACCENT);
                    g2.fillRoundRect(0, 4, 3, getHeight() - 8, 3, 3);
                } else if (hover) {
                    g2.setColor(new Color(255, 255, 255, 12));
                    g2.fillRoundRect(8, 4, getWidth() - 16, getHeight() - 8, 10, 10);
                }

                Color iconCol = active ? ACCENT : (hover ? TEXT_WHITE : TEXT_MUTED);
                icon.draw(g2, 20, getHeight() / 2 - 9, 18, iconCol);

                g2.setFont(new Font("Segoe UI", active ? Font.BOLD : Font.PLAIN, 13));
                g2.setColor(active ? TEXT_WHITE : (hover ? TEXT_WHITE : TEXT_MUTED));
                g2.drawString(label, 46, getHeight() / 2 + 5);

                g2.dispose();
            }
        };
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        btn.setPreferredSize(new Dimension(230, 44));
        btn.setAlignmentX(LEFT_ALIGNMENT);
        btn.setBackground(BG_SIDEBAR);

        if (view != null) btn.addActionListener(e -> showView(view));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) { btn.repaint(); }
            public void mouseExited (java.awt.event.MouseEvent e) { btn.repaint(); }
        });
        return btn;
    }

    // ══════════════════════════════════════════════════════════════════════════
    // CONTENT AREA
    // ══════════════════════════════════════════════════════════════════════════
    private JPanel buildContentArea() {
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(BG_DARK);

        contentPanel.add(buildHomeView(), VIEW_HOME);
        contentPanel.add(new TechnicianForm(this), VIEW_TECH);
        contentPanel.add(new MaintenanceRequestForm(this), VIEW_REQ);
        contentPanel.add(new UsersPanel(this), VIEW_USERS);
        contentPanel.add(new ReportsWindow(this, role), VIEW_REPORTS);
        contentPanel.add(new AIChatbotWindow(this, loggedInUser, role), VIEW_CHATBOT);

        return contentPanel;
    }

    // ── HOME VIEW ────────────────────────────────────────────────────────────
    private JPanel buildHomeView() {
        JPanel home = new JPanel(new BorderLayout(0, 0));
        home.setBackground(BG_DARK);
        home.setBorder(new EmptyBorder(30, 30, 24, 30));

        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setBackground(BG_DARK);
        headerRow.setBorder(new EmptyBorder(0, 0, 24, 0));

        JLabel header = new JLabel("Dashboard Overview");
        header.setFont(new Font("Segoe UI", Font.BOLD, 24));
        header.setForeground(TEXT_WHITE);
        headerRow.add(header, BorderLayout.WEST);

        JLabel dateLbl = new JLabel(LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("EEEE, MMM dd yyyy")));
        dateLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        dateLbl.setForeground(TEXT_MUTED);
        headerRow.add(dateLbl, BorderLayout.EAST);

        home.add(headerRow, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(0, 20));
        center.setBackground(BG_DARK);

        JPanel cards = new JPanel(new GridLayout(1, 5, 14, 0));
        cards.setBackground(BG_DARK);

        pendingPanel = statCard("Pending", "—", new Color(245, 158, 11), SvgIcon.CLOCK);
        inProgressPanel = statCard("In Progress", "—", new Color(59, 130, 246), SvgIcon.SETTINGS);
        completedPanel = statCard("Completed", "—", new Color(34, 197, 94), SvgIcon.CHECK);
        techPanel = statCard("Technicians", "—", ACCENT, SvgIcon.USERS);
        usersStatPanel = statCard("Total Users", "—", new Color(168, 85, 247), SvgIcon.PEOPLE);

        cards.add(pendingPanel);
        cards.add(inProgressPanel);
        cards.add(completedPanel);
        cards.add(techPanel);
        cards.add(usersStatPanel);
        center.add(cards, BorderLayout.NORTH);

        JPanel lower = new JPanel(new GridLayout(1, 2, 16, 0));
        lower.setBackground(BG_DARK);
        lower.add(buildRecentRequestsPanel());
        lower.add(buildSystemInfoPanel());
        center.add(lower, BorderLayout.CENTER);

        home.add(center, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        actions.setBackground(BG_DARK);
        actions.setBorder(new EmptyBorder(16, 0, 0, 0));

        JButton newReq = accentButton("+ New Request", ACCENT);
        JButton addTech = accentButton("+ Add Technician", new Color(34, 197, 94));
        newReq.addActionListener(e -> showView(VIEW_REQ));
        addTech.addActionListener(e -> showView(VIEW_TECH));
        actions.add(newReq);
        actions.add(addTech);
        home.add(actions, BorderLayout.SOUTH);

        return home;
    }

    private JPanel statCard(String title, String value, Color accent, SvgIcon icon) {
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
        p.setBorder(new EmptyBorder(22, 22, 22, 22));

        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setOpaque(false);

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblTitle.setForeground(TEXT_MUTED);

        JLabel lblVal = new JLabel(value);
        lblVal.setFont(new Font("Segoe UI", Font.BOLD, 34));
        lblVal.setForeground(accent);

        left.add(lblTitle);
        left.add(Box.createVerticalStrut(4));
        left.add(lblVal);
        p.add(left, BorderLayout.CENTER);

        JLabel iconBadge = new JLabel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 25);
                g2.setColor(bg);
                g2.fillOval(0, 0, 48, 48);
                icon.draw(g2, 12, 12, 24, accent);
                g2.dispose();
            }
        };
        iconBadge.setPreferredSize(new Dimension(48, 48));
        p.add(iconBadge, BorderLayout.EAST);

        return p;
    }

    private void updateStatCard(JPanel card, int value) {
        Component center = ((BorderLayout) card.getLayout()).getLayoutComponent(BorderLayout.CENTER);
        if (center instanceof JPanel lp && lp.getComponentCount() >= 3) {
            ((JLabel) lp.getComponent(2)).setText(String.valueOf(value));
        }
    }

    private JPanel buildRecentRequestsPanel() {
        JPanel panel = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(BG_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
            }
        };
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);
        titleRow.setBorder(new EmptyBorder(0, 0, 14, 0));

        JLabel title = new JLabel("Recent Requests");
        title.setFont(new Font("Segoe UI", Font.BOLD, 14));
        title.setForeground(TEXT_WHITE);
        titleRow.add(title, BorderLayout.WEST);

        JLabel viewAll = new JLabel("View All →");
        viewAll.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        viewAll.setForeground(ACCENT);
        viewAll.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        viewAll.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) { showView(VIEW_REQ); }
        });
        titleRow.add(viewAll, BorderLayout.EAST);
        panel.add(titleRow, BorderLayout.NORTH);

        String[] cols = {"ID", "Title", "Status", "Technician"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        recentTable = new JTable(model);
        styleRecentTable(recentTable);

        JScrollPane scroll = new JScrollPane(recentTable);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getViewport().setBackground(BG_CARD);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    private void styleRecentTable(JTable table) {
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
        table.getColumnModel().getColumn(0).setMaxWidth(50);

        DefaultTableCellRenderer base = new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                setBackground(sel ? new Color(99,102,241,60) : (row%2==0 ? BG_CARD : new Color(26,26,40)));
                setForeground(TEXT_WHITE);
                setOpaque(true);
                setBorder(new EmptyBorder(0, 8, 0, 8));
                return this;
            }
        };
        table.setDefaultRenderer(Object.class, base);

        table.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel, boolean foc, int row, int col) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                lbl.setOpaque(true);
                lbl.setBackground(sel ? new Color(99,102,241,60) : (row%2==0 ? BG_CARD : new Color(26,26,40)));
                String s = val == null ? "" : val.toString();
                lbl.setForeground(!sel ? switch (s) {
                    case "PENDING"     -> new Color(245, 158, 11);
                    case "IN_PROGRESS" -> new Color(59, 130, 246);
                    case "COMPLETED"   -> new Color(34, 197, 94);
                    default            -> TEXT_MUTED;
                } : TEXT_WHITE);
                if (!sel) {
                    Color bg = switch (s) {
                        case "PENDING"     -> new Color(245, 158, 11, 20);
                        case "IN_PROGRESS" -> new Color(59, 130, 246, 20);
                        case "COMPLETED"   -> new Color(34, 197, 94, 20);
                        default            -> BG_CARD;
                    };
                    lbl.setBackground(bg);
                }
                return lbl;
            }
        });
    }

    private JPanel buildSystemInfoPanel() {
        JPanel panel = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(BG_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
            }
        };
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(20, 24, 20, 24));

        JLabel title = new JLabel("System Information");
        title.setFont(new Font("Segoe UI", Font.BOLD, 14));
        title.setForeground(TEXT_WHITE);
        title.setBorder(new EmptyBorder(0, 0, 18, 0));
        panel.add(title, BorderLayout.NORTH);

        JPanel rows = new JPanel();
        rows.setLayout(new BoxLayout(rows, BoxLayout.Y_AXIS));
        rows.setOpaque(false);

        lblServerTime = new JLabel("—");
        lblTotalRecords = new JLabel("—");

        rows.add(infoRow("Logged In User", loggedInUser + "  ·  " + role, null));
        rows.add(Box.createVerticalStrut(2));
        rows.add(infoRow("Server Time", null, lblServerTime));
        rows.add(Box.createVerticalStrut(2));
        rows.add(infoRow("Database", "MySQL (Connected)", null));
        rows.add(Box.createVerticalStrut(2));
        rows.add(infoRow("Total Records", null, lblTotalRecords));

        panel.add(rows, BorderLayout.CENTER);
        return panel;
    }

    private JPanel infoRow(String key, String staticVal, JLabel dynLbl) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        row.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, DIVIDER),
            new EmptyBorder(10, 0, 10, 0)));

        JLabel k = new JLabel(key);
        k.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        k.setForeground(TEXT_MUTED);

        JLabel v = dynLbl != null ? dynLbl : new JLabel(staticVal != null ? staticVal : "");
        v.setFont(new Font("Segoe UI", Font.BOLD, 12));
        v.setForeground(TEXT_WHITE);
        v.setHorizontalAlignment(SwingConstants.RIGHT);

        row.add(k, BorderLayout.WEST);
        row.add(v, BorderLayout.EAST);
        return row;
    }

    private void startClock() {
        Timer t = new Timer(1000, e -> {
            if (lblServerTime != null)
                lblServerTime.setText(LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        });
        t.setInitialDelay(0);
        t.start();
    }

    public void refreshStats() {
        new SwingWorker<int[], Void>() {
            @Override protected int[] doInBackground() {
                MaintenanceRequestDAO rDao = new MaintenanceRequestDAO();
                TechnicianDAO tDao = new TechnicianDAO();
                UserDAO uDao = new UserDAO();
                return new int[]{
                    rDao.countByStatus(Status.PENDING),
                    rDao.countByStatus(Status.IN_PROGRESS),
                    rDao.countByStatus(Status.COMPLETED),
                    tDao.countTechnicians(),
                    rDao.getTotalRecords(),
                    uDao.countUsers()
                };
            }
            @Override protected void done() {
                try {
                    int[] c = get();
                    updateStatCard(pendingPanel, c[0]);
                    updateStatCard(inProgressPanel, c[1]);
                    updateStatCard(completedPanel, c[2]);
                    updateStatCard(techPanel, c[3]);
                    if (lblTotalRecords != null) lblTotalRecords.setText(String.valueOf(c[4]));
                    if (usersStatPanel != null) updateStatCard(usersStatPanel, c[5]);
                    loadRecentRequests();
                } catch (Exception ignored) {}
            }
        }.execute();
    }

    private void loadRecentRequests() {
        new SwingWorker<java.util.List<Object[]>, Void>() {
            @Override protected java.util.List<Object[]> doInBackground() {
                return new MaintenanceRequestDAO().getRecentRequests(10);
            }
            @Override protected void done() {
                try {
                    DefaultTableModel m = (DefaultTableModel) recentTable.getModel();
                    m.setRowCount(0);
                    for (Object[] row : get()) m.addRow(row);
                } catch (Exception ignored) {}
            }
        }.execute();
    }

    public void showView(String view) {
        cardLayout.show(contentPanel, view);
        Color active = new Color(40, 40, 65);
        for (JButton b : new JButton[]{btnHome, btnTech, btnReq, btnUsers, btnReports, btnChatbot}) {
            b.setBackground(BG_SIDEBAR);
            b.repaint();
        }
        switch (view) {
            case VIEW_HOME    -> { btnHome.setBackground(active);    btnHome.repaint(); }
            case VIEW_TECH    -> { btnTech.setBackground(active);    btnTech.repaint(); }
            case VIEW_REQ     -> { btnReq.setBackground(active);     btnReq.repaint(); }
            case VIEW_USERS   -> { btnUsers.setBackground(active);   btnUsers.repaint(); }
            case VIEW_REPORTS -> { btnReports.setBackground(active); btnReports.repaint(); }
            case VIEW_CHATBOT -> { btnChatbot.setBackground(active); btnChatbot.repaint(); }
        }
        if (VIEW_HOME.equals(view)) refreshStats();
    }

    public JButton accentButton(String text, Color color) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color c = getModel().isRollover()
                        ? new Color(Math.max(0, color.getRed() - 20),
                                   Math.max(0, color.getGreen() - 20),
                                   Math.max(0, color.getBlue() - 20))
                        : color;
                g2.setColor(c);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                g.setColor(Color.WHITE);
                g.setFont(getFont());
                FontMetrics fm = g.getFontMetrics();
                g.drawString(getText(), (getWidth() - fm.stringWidth(getText())) / 2,
                             (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setForeground(Color.WHITE);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(10, 24, 10, 24));
        btn.setPreferredSize(new Dimension(170, 40));
        return btn;
    }

    public JButton accentButton(String text) {
        return accentButton(text, ACCENT);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // SVG ICONS
    // ══════════════════════════════════════════════════════════════════════════
    enum SvgIcon {
        HOME, USERS, PEOPLE, CLIPBOARD, LOGOUT, CLOCK, SETTINGS, CHECK, CHART, AI, INFO;

        void draw(Graphics2D g, int x, int y, int size, Color c) {
            g.setColor(c);
            BasicStroke defaultStroke = new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
            g.setStroke(defaultStroke);
            float s = size / 24f;

            switch (this) {
                case HOME -> {
                    // FIX: close the roof path and use float-precision door rect
                    GeneralPath roof = new GeneralPath();
                    roof.moveTo(x + 3*s,  y + 10.5f*s);
                    roof.lineTo(x + 12*s, y + 3*s);
                    roof.lineTo(x + 21*s, y + 10.5f*s);
                    g.draw(roof);
                    GeneralPath walls = new GeneralPath();
                    walls.moveTo(x + 5*s,  y + 9.5f*s);
                    walls.lineTo(x + 5*s,  y + 21*s);
                    walls.lineTo(x + 19*s, y + 21*s);
                    walls.lineTo(x + 19*s, y + 9.5f*s);
                    g.draw(walls);
                    // FIX: use float-aware RoundRectangle2D so door renders at sub-pixel sizes
                    g.draw(new java.awt.geom.RoundRectangle2D.Float(
                        x + 9*s, y + 14*s, 6*s, 7*s, 2, 2));
                }
                case USERS -> {
                    // FIX: secondary figure head moved right so it doesn't overlap primary;
                    //      body2 curve clamped to x+22*s (within grid)
                    g.draw(new Ellipse2D.Float(x+4*s, y+3*s, 8*s, 8*s));
                    GeneralPath body = new GeneralPath();
                    body.moveTo(x+1*s,  y+21*s);
                    body.curveTo(x+1*s, y+17*s, x+4*s, y+14*s, x+9*s, y+14*s);
                    body.curveTo(x+14*s,y+14*s, x+17*s,y+17*s, x+17*s,y+21*s);
                    g.draw(body);
                    // Secondary figure (offset right, slightly smaller)
                    g.draw(new Ellipse2D.Float(x+14*s, y+4*s, 7*s, 7*s));
                    GeneralPath body2 = new GeneralPath();
                    body2.moveTo(x+17*s,  y+14*s);
                    body2.curveTo(x+19.5f*s,y+14*s, x+22*s,y+17*s, x+22*s,y+21*s);
                    g.draw(body2);
                }
                case PEOPLE -> {
                    // FIX: keep both figures inside 0-24 grid; body2 clamped to x+22*s
                    g.draw(new Ellipse2D.Float(x+3*s, y+4*s, 7*s, 7*s));
                    GeneralPath b1 = new GeneralPath();
                    b1.moveTo(x+1*s,  y+20*s);
                    b1.curveTo(x+1*s, y+16*s, x+3*s, y+13*s, x+7*s, y+13*s);
                    b1.curveTo(x+11*s,y+13*s, x+13*s,y+16*s, x+13*s,y+20*s);
                    g.draw(b1);
                    g.draw(new Ellipse2D.Float(x+13*s, y+6*s, 7*s, 7*s));
                    GeneralPath b2 = new GeneralPath();
                    b2.moveTo(x+15*s, y+20*s);
                    b2.curveTo(x+15*s,y+17*s, x+17*s,y+15*s, x+19.5f*s,y+15*s);
                    b2.curveTo(x+22*s, y+15*s, x+22*s,y+17*s, x+22*s,y+20*s);
                    g.draw(b2);
                }
                case CLIPBOARD -> {
                    g.draw(new java.awt.geom.RoundRectangle2D.Float(
                        x+4*s, y+3*s, 16*s, 18*s, 4, 4));
                    g.draw(new java.awt.geom.RoundRectangle2D.Float(
                        x+8*s, y+1*s, 8*s, 4*s, 4, 4));
                    g.draw(new java.awt.geom.Line2D.Float(
                        x+8*s, y+11*s, x+16*s, y+11*s));
                    g.draw(new java.awt.geom.Line2D.Float(
                        x+8*s, y+15*s, x+16*s, y+15*s));
                }
                case LOGOUT -> {
                    GeneralPath door = new GeneralPath();
                    door.moveTo(x+9*s,  y+21*s);
                    door.lineTo(x+5*s,  y+21*s);
                    door.curveTo(x+4*s, y+21*s, x+3*s, y+20*s, x+3*s, y+19*s);
                    door.lineTo(x+3*s,  y+5*s);
                    door.curveTo(x+3*s, y+4*s, x+4*s, y+3*s, x+5*s, y+3*s);
                    door.lineTo(x+9*s,  y+3*s);
                    g.draw(door);
                    GeneralPath arrow = new GeneralPath();
                    arrow.moveTo(x+16*s, y+17*s);
                    arrow.lineTo(x+21*s, y+12*s);
                    arrow.lineTo(x+16*s, y+7*s);
                    g.draw(arrow);
                    g.draw(new java.awt.geom.Line2D.Float(
                        x+21*s, y+12*s, x+9*s, y+12*s));
                }
                case CLOCK -> {
                    g.draw(new Ellipse2D.Float(x+2*s, y+2*s, 20*s, 20*s));
                    g.draw(new java.awt.geom.Line2D.Float(
                        x+12*s, y+6*s, x+12*s, y+12*s));
                    g.draw(new java.awt.geom.Line2D.Float(
                        x+12*s, y+12*s, x+16*s, y+14*s));
                }
                case SETTINGS -> {
                    // FIX: use float centre + proper scaled radii so spokes align at all sizes
                    g.draw(new Ellipse2D.Float(x+9*s, y+9*s, 6*s, 6*s));
                    float cx = x + 12*s, cy = y + 12*s;
                    float r1 = 5.5f*s, r2 = 8*s;
                    for (int i = 0; i < 8; i++) {
                        double a = Math.PI / 4.0 * i;
                        g.draw(new java.awt.geom.Line2D.Float(
                            cx + r1 * (float)Math.cos(a), cy + r1 * (float)Math.sin(a),
                            cx + r2 * (float)Math.cos(a), cy + r2 * (float)Math.sin(a)));
                    }
                }
                case CHECK -> {
                    g.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g.draw(new Ellipse2D.Float(x+2*s, y+2*s, 20*s, 20*s));
                    GeneralPath check = new GeneralPath();
                    check.moveTo(x+7*s,  y+12*s);
                    check.lineTo(x+10*s, y+15*s);
                    check.lineTo(x+17*s, y+9*s);
                    g.draw(check);
                    // FIX: restore default stroke so subsequent icons aren't thickened
                    g.setStroke(defaultStroke);
                }
                case CHART -> {
                    // FIX: bars use float rects; baseline moved to y+21.5*s to sit below bars
                    g.draw(new java.awt.geom.Rectangle2D.Float(x+3*s,  y+12*s, 4*s, 9*s));
                    g.draw(new java.awt.geom.Rectangle2D.Float(x+10*s, y+7*s,  4*s, 14*s));
                    g.draw(new java.awt.geom.Rectangle2D.Float(x+17*s, y+4*s,  4*s, 17*s));
                    g.draw(new java.awt.geom.Line2D.Float(
                        x+2*s, y+21.5f*s, x+22*s, y+21.5f*s));
                }
                case AI -> {
                    // FIX: pins clamped to y+2.5*s / y+21.5*s and x+2.5*s / x+21.5*s
                    //      so they stay visible and within the paint region at size=18
                    g.draw(new java.awt.geom.RoundRectangle2D.Float(
                        x+5*s, y+5*s, 14*s, 14*s, 3*s, 3*s));
                    g.draw(new Ellipse2D.Float(x+9*s, y+9*s, 6*s, 6*s));
                    // top pins
                    g.draw(new java.awt.geom.Line2D.Float(x+9*s,  y+5*s, x+9*s,  y+2.5f*s));
                    g.draw(new java.awt.geom.Line2D.Float(x+15*s, y+5*s, x+15*s, y+2.5f*s));
                    // bottom pins
                    g.draw(new java.awt.geom.Line2D.Float(x+9*s,  y+19*s, x+9*s,  y+21.5f*s));
                    g.draw(new java.awt.geom.Line2D.Float(x+15*s, y+19*s, x+15*s, y+21.5f*s));
                    // left pins
                    g.draw(new java.awt.geom.Line2D.Float(x+5*s, y+9*s,  x+2.5f*s, y+9*s));
                    g.draw(new java.awt.geom.Line2D.Float(x+5*s, y+15*s, x+2.5f*s, y+15*s));
                    // right pins
                    g.draw(new java.awt.geom.Line2D.Float(x+19*s, y+9*s,  x+21.5f*s, y+9*s));
                    g.draw(new java.awt.geom.Line2D.Float(x+19*s, y+15*s, x+21.5f*s, y+15*s));
                }
                case INFO -> {
                    // FIX: dot uses max(2, 2*s) so it is always at least 2px at small sizes
                    g.draw(new Ellipse2D.Float(x+2*s, y+2*s, 20*s, 20*s));
                    g.draw(new java.awt.geom.Line2D.Float(
                        x+12*s, y+11*s, x+12*s, y+17*s));
                    float dotR = Math.max(1.5f, s * 1.5f);
                    g.fill(new Ellipse2D.Float(x+12*s - dotR, y+7.5f*s - dotR, dotR*2, dotR*2));
                }
            }
        }
    }
}