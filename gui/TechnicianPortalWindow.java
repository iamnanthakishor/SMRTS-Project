package gui;

import dao.MaintenanceRequestDAO;
import dao.TechnicianDAO;
import model.MaintenanceRequest;
import model.MaintenanceRequest.Status;
import model.Technician;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.awt.geom.*;
import java.awt.geom.GeneralPath;
import java.awt.geom.Arc2D;

/**
 * TechnicianPortalWindow — v1.0
 *
 * A dedicated portal for technicians (STAFF role) to:
 *   • See all open/pending maintenance requests
 *   • Self-assign a request (sets assigned_technician_id to their own record)
 *   • View full request details
 *   • Update the status of their assigned requests (IN_PROGRESS → COMPLETED)
 *
 * Inherits the same dark colour palette as AdminDashboard and UserPortalWindow
 * so the whole system looks consistent.
 *
 * Login routing: LoginForm already sends "Technician" role logins to
 * AdminDashboard — change the done() block to call:
 *   new TechnicianPortalWindow(username).setVisible(true);
 * when result.equalsIgnoreCase("Technician").
 */
public class TechnicianPortalWindow extends JFrame {

    // ── Shared palette (same values as AdminDashboard) ────────────────────────
    private static final Color BG_DARK      = new Color(18, 18, 28);
    private static final Color BG_SIDEBAR   = new Color(22, 22, 36);
    private static final Color BG_CARD      = new Color(30, 30, 46);
    private static final Color BG_CARD_HOVER= new Color(36, 36, 56);
    private static final Color ACCENT       = new Color(99, 102, 241);
    private static final Color ACCENT_HOVER = new Color(79, 82, 221);
    private static final Color TEXT_WHITE   = new Color(240, 240, 255);
    private static final Color TEXT_MUTED   = new Color(140, 140, 175);
    private static final Color DIVIDER      = new Color(40, 40, 60);
    private static final Color FIELD_BG     = new Color(40, 40, 60);
    private static final Color FIELD_BORDER = new Color(70, 70, 100);

    // Status colours
    private static final Color COL_PENDING    = new Color(245, 158, 11);
    private static final Color COL_IN_PROGRESS= new Color(59, 130, 246);
    private static final Color COL_COMPLETED  = new Color(34, 197, 94);

    // Priority colours
    private static final Color COL_HIGH  = new Color(239, 68, 68);
    private static final Color COL_MED   = new Color(245, 158, 11);
    private static final Color COL_LOW   = new Color(34, 197, 94);

    // ── State ─────────────────────────────────────────────────────────────────
    private static final Logger logger = Logger.getLogger(TechnicianPortalWindow.class.getName());

    private final String username;           // logged-in technician's username
    private Technician   selfTechnician;     // resolved from DB on load

    private final MaintenanceRequestDAO reqDAO  = new MaintenanceRequestDAO();
    private final TechnicianDAO         techDAO = new TechnicianDAO();

    // ── Views ─────────────────────────────────────────────────────────────────
    private static final String VIEW_OPEN    = "OPEN";    // all unassigned/open
    private static final String VIEW_MY      = "MY";      // assigned to me
    private static final String VIEW_DETAILS = "DETAILS"; // request detail pane

    private CardLayout cardLayout;
    private JPanel     contentPanel;

    // Open requests view
    private JTable         openTable;
    private DefaultTableModel openModel;
    private JLabel         lblOpenCount;

    // My requests view
    private JTable         myTable;
    private DefaultTableModel myModel;
    private JLabel         lblMyCount;

    // Detail view state
    private MaintenanceRequest selectedRequest;

    // Sidebar nav buttons
    private JButton btnOpen;
    private JButton btnMy;

    // ── Constructor ───────────────────────────────────────────────────────────
    public TechnicianPortalWindow(String username) {
        this.username = username;
        resolveSelfTechnician();
        buildUI();
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        showView(VIEW_OPEN);
    }

    /**
     * Resolves the Technician DB record for the logged-in user.
     *
     * Matching strategy (tried in order, stops at first hit):
     *   0. Direct username column match — requires patch_technician_accounts.sql
     *      to have been run (adds `username` column to technicians table).
     *   1. Exact name match:   technician.name == username  (case-insensitive)
     *   2. First-word match:   first word of technician.name == username
     *      e.g. username "kasun" matches "Kasun Perera"
     *   3. Name-contains:      technician.name contains username as a substring
     *      e.g. username "elan" matches "Elankeeran"
     *   4. Username-contains:  username contains first word of technician.name
     *      e.g. username "kasunp" contains "kasun"
     *
     * If no match is found the window still opens with a detached Technician
     * (id=0) and self-assign is gracefully blocked with an error message.
     */
    private void resolveSelfTechnician() {
        try {
            // Pass 0 — direct DB lookup by username column (most reliable)
            // Works after patch_technician_accounts.sql has been applied.
            selfTechnician = techDAO.getByUsername(username);
            if (selfTechnician != null) return;
        } catch (Exception ignored) {
            // Column may not exist yet — fall through to name-matching
        }

        try {
            List<Technician> all = techDAO.getAllTechnicians();
            String uLower = username.trim().toLowerCase();

            // Pass 1 — exact name match
            for (Technician t : all) {
                if (t.getName().trim().equalsIgnoreCase(uLower)) {
                    selfTechnician = t;
                    return;
                }
            }

            // Pass 2 — first word of technician name matches username
            // e.g. "Kasun Perera" → first word "kasun" matches username "kasun"
            for (Technician t : all) {
                String firstName = t.getName().trim().split("\\s+")[0].toLowerCase();
                if (firstName.equals(uLower)) {
                    selfTechnician = t;
                    return;
                }
            }

            // Pass 3 — technician name contains username as substring
            // e.g. "Elankeeran" contains "elan" or "elankeeran"
            for (Technician t : all) {
                if (t.getName().trim().toLowerCase().contains(uLower)) {
                    selfTechnician = t;
                    return;
                }
            }

            // Pass 4 — username contains first word of technician name
            // e.g. username "kasunp" contains "kasun"
            for (Technician t : all) {
                String firstName = t.getName().trim().split("\\s+")[0].toLowerCase();
                if (uLower.contains(firstName) && firstName.length() >= 4) {
                    selfTechnician = t;
                    return;
                }
            }

            logger.log(Level.WARNING,
                    "No technician record found for username ''{0}''. "
                    + "Self-assign will be disabled until a match is found.", username);

        } catch (Exception e) {
            logger.log(Level.WARNING, "Could not resolve technician for username: " + username, e);
        }

        // Fallback — window opens normally, self-assign shows a helpful message
        selfTechnician = new Technician(0, username, "", "", "", "Available");
    }

    // ── Root UI ───────────────────────────────────────────────────────────────
    private void buildUI() {
        setTitle("SMRTS — Technician Portal  (" + username + ")");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(960, 620));

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG_DARK);
        setContentPane(root);

        root.add(buildSidebar(),     BorderLayout.WEST);
        root.add(buildContentArea(), BorderLayout.CENTER);
    }

    // ── Sidebar ───────────────────────────────────────────────────────────────
    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(BG_SIDEBAR);
        sidebar.setPreferredSize(new Dimension(230, 0));

        // Brand / avatar block
        JPanel brand = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 18));
        brand.setBackground(BG_SIDEBAR);
        brand.setAlignmentX(LEFT_ALIGNMENT);
        brand.setMaximumSize(new Dimension(Integer.MAX_VALUE, 72));

        // Circle avatar with initial
        JLabel avatar = new JLabel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, ACCENT, 40, 40, new Color(139, 92, 246));
                g2.setPaint(gp);
                g2.fillOval(0, 0, 40, 40);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 16));
                FontMetrics fm = g2.getFontMetrics();
                String initial = username.isEmpty() ? "T" : String.valueOf(username.charAt(0)).toUpperCase();
                g2.drawString(initial,
                        (40 - fm.stringWidth(initial)) / 2,
                        (40 - fm.getHeight()) / 2 + fm.getAscent());
                g2.dispose();
            }
        };
        avatar.setPreferredSize(new Dimension(40, 40));

        JPanel nameBlock = new JPanel();
        nameBlock.setLayout(new BoxLayout(nameBlock, BoxLayout.Y_AXIS));
        nameBlock.setBackground(BG_SIDEBAR);

        JLabel lblName = new JLabel(username);
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblName.setForeground(TEXT_WHITE);

        JLabel lblRole = new JLabel("Technician");
        lblRole.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblRole.setForeground(ACCENT);

        nameBlock.add(lblName);
        nameBlock.add(Box.createVerticalStrut(2));
        nameBlock.add(lblRole);

        brand.add(avatar);
        brand.add(nameBlock);
        sidebar.add(brand);

        // Divider
        sidebar.add(sidebarDivider());

        // Nav buttons
        btnOpen = navButton("Open Requests",  SvgIcon.CLIPBOARD, VIEW_OPEN);
        btnMy   = navButton("My Assignments", SvgIcon.WRENCH,    VIEW_MY);

        sidebar.add(btnOpen);
        sidebar.add(btnMy);

        // Spacer
        sidebar.add(Box.createVerticalGlue());
        sidebar.add(sidebarDivider());

        // Technician info card (name from DB)
        if (selfTechnician != null && selfTechnician.getTechnicianId() > 0) {
            JPanel infoCard = buildTechInfoCard();
            sidebar.add(infoCard);
            sidebar.add(Box.createVerticalStrut(8));
        }

        // Logout
        JButton btnLogout = navButton("Logout", SvgIcon.LOGOUT, null);
        btnLogout.addActionListener(e -> { dispose(); new LoginForm().setVisible(true); });
        sidebar.add(btnLogout);
        sidebar.add(Box.createVerticalStrut(16));

        return sidebar;
    }

    private JPanel buildTechInfoCard() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(new Color(28, 28, 45));
        card.setBorder(new EmptyBorder(10, 16, 10, 16));
        card.setAlignmentX(LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

        JLabel lDept = new JLabel(selfTechnician.getDepartment().isEmpty()
                ? "No department" : selfTechnician.getDepartment());
        lDept.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lDept.setForeground(TEXT_MUTED);

        JLabel lStatus = new JLabel("● " + selfTechnician.getStatus());
        lStatus.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lStatus.setForeground(selfTechnician.getStatus().equalsIgnoreCase("Available")
                ? COL_COMPLETED : COL_PENDING);

        card.add(lDept);
        card.add(Box.createVerticalStrut(4));
        card.add(lStatus);
        return card;
    }

    /** Thin horizontal divider line for sidebar */
    private JPanel sidebarDivider() {
        JPanel line = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(DIVIDER);
                g.fillRect(0, 0, getWidth(), 1);
            }
        };
        line.setOpaque(false);
        line.setAlignmentX(LEFT_ALIGNMENT);
        line.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        line.setPreferredSize(new Dimension(0, 1));
        return line;
    }

    /** Creates a styled sidebar nav button with a vector SVG icon */
    private JButton navButton(String label, SvgIcon icon, String view) {
        JButton btn = new JButton(label) {
            boolean hover = false;
            { // instance init
                addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                    @Override public void mouseExited(MouseEvent e)  { hover = false; repaint(); }
                });
            }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean isActive = view != null && view.equals(getClientProperty("activeView"));
                Color bg = isActive ? new Color(99, 102, 241, 40)
                         : hover    ? new Color(255, 255, 255, 8)
                         : new Color(0, 0, 0, 0);
                g2.setColor(bg);
                g2.fillRoundRect(8, 2, getWidth()-16, getHeight()-4, 10, 10);
                if (isActive) {
                    g2.setColor(ACCENT);
                    g2.fillRoundRect(0, 8, 4, getHeight()-16, 4, 4);
                }
                // Draw SVG icon at 18×18, vertically centred, 16px from left edge
                int iconSize = 18;
                int ix = 16;
                int iy = (getHeight() - iconSize) / 2;
                Color iconColor = isActive ? ACCENT : TEXT_MUTED;
                icon.draw(g2, ix, iy, iconSize, iconColor);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btn.setForeground(TEXT_MUTED);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        // Left padding = icon_x(16) + icon_size(18) + gap(10) = 44
        btn.setBorder(new EmptyBorder(0, 44, 0, 16));
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setAlignmentX(LEFT_ALIGNMENT);
        btn.setPreferredSize(new Dimension(230, 48));
        btn.setMinimumSize(new Dimension(160, 48));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        if (view != null) {
            btn.addActionListener(e -> showView(view));
        }
        return btn;
    }

    // ── Content area with CardLayout ──────────────────────────────────────────
    private JPanel buildContentArea() {
        cardLayout   = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(BG_DARK);

        contentPanel.add(buildOpenRequestsView(), VIEW_OPEN);
        contentPanel.add(buildMyRequestsView(),   VIEW_MY);

        return contentPanel;
    }

    private void showView(String view) {
        // Update active state on nav buttons
        for (JButton b : new JButton[]{btnOpen, btnMy}) {
            if (b != null) {
                b.putClientProperty("activeView", view);
                b.setForeground(view.equals(b.getClientProperty("activeView"))
                        ? TEXT_WHITE : TEXT_MUTED);
                b.repaint();
            }
        }
        cardLayout.show(contentPanel, view);

        // Refresh data on switch
        if (VIEW_OPEN.equals(view)) loadOpenRequests();
        if (VIEW_MY.equals(view))   loadMyRequests();
    }

    // ══════════════════════════════════════════════════════════════════════════
    // VIEW 1 — Open Requests (unassigned / pending)
    // ══════════════════════════════════════════════════════════════════════════
    private JPanel buildOpenRequestsView() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_DARK);
        panel.setBorder(new EmptyBorder(28, 28, 28, 28));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BG_DARK);
        header.setBorder(new EmptyBorder(0, 0, 20, 0));

        JPanel titleBlock = new JPanel();
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.setBackground(BG_DARK);

        JLabel lblTitle = new JLabel("Open Requests");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(TEXT_WHITE);

        lblOpenCount = new JLabel("Loading...");
        lblOpenCount.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblOpenCount.setForeground(TEXT_MUTED);

        titleBlock.add(lblTitle);
        titleBlock.add(Box.createVerticalStrut(4));
        titleBlock.add(lblOpenCount);
        header.add(titleBlock, BorderLayout.WEST);

        // Refresh button
        JButton btnRefresh = accentButton("Refresh", SvgIcon.REFRESH);
        btnRefresh.addActionListener(e -> loadOpenRequests());
        header.add(btnRefresh, BorderLayout.EAST);

        panel.add(header, BorderLayout.NORTH);

        // Table
        String[] cols = {"#", "Title", "Location", "Priority", "Status", "Submitted", "Created"};
        openModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        openTable = buildTable(openModel);
        openTable.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) openDetailDialog(openTable);
            }
        });

        // Column widths
        setColumnWidths(openTable, new int[]{45, 200, 130, 80, 100, 110, 110});

        JScrollPane scroll = styledScroll(openTable);
        panel.add(scroll, BorderLayout.CENTER);

        // Bottom action bar
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setBackground(BG_DARK);
        actions.setBorder(new EmptyBorder(14, 0, 0, 0));

        JButton btnView   = ghostButton("View Details");
        JButton btnAssign = accentButton("Self-Assign", SvgIcon.CHECK);

        btnView.addActionListener(e -> openDetailDialog(openTable));
        btnAssign.addActionListener(e -> selfAssign(openTable));

        actions.add(btnView);
        actions.add(btnAssign);
        panel.add(actions, BorderLayout.SOUTH);

        return panel;
    }

    private void loadOpenRequests() {
        new SwingWorker<List<MaintenanceRequest>, Void>() {
            @Override protected List<MaintenanceRequest> doInBackground() {
                // Show all PENDING requests (unassigned ones are natural targets for self-assign)
                return reqDAO.getRequestsByStatus(Status.PENDING);
            }
            @Override protected void done() {
                try {
                    List<MaintenanceRequest> list = get();
                    openModel.setRowCount(0);
                    for (MaintenanceRequest r : list) {
                        openModel.addRow(new Object[]{
                            r.getRequestId(),
                            r.getTitle(),
                            r.getLocation() != null ? r.getLocation() : "—",
                            r.getPriority().name(),
                            r.getStatus().name(),
                            r.getSubmittedByUsername() != null ? r.getSubmittedByUsername() : "Admin",
                            r.getCreatedAtFormatted()
                        });
                    }
                    int total = list.size();
                    lblOpenCount.setText(total + " pending request" + (total == 1 ? "" : "s") + " awaiting assignment");
                } catch (Exception ex) {
                    logger.log(Level.SEVERE, "loadOpenRequests failed", ex);
                }
            }
        }.execute();
    }

    // ══════════════════════════════════════════════════════════════════════════
    // VIEW 2 — My Assignments (assigned to this technician)
    // ══════════════════════════════════════════════════════════════════════════
    private JPanel buildMyRequestsView() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_DARK);
        panel.setBorder(new EmptyBorder(28, 28, 28, 28));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BG_DARK);
        header.setBorder(new EmptyBorder(0, 0, 20, 0));

        JPanel titleBlock = new JPanel();
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.setBackground(BG_DARK);

        JLabel lblTitle = new JLabel("My Assignments");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(TEXT_WHITE);

        lblMyCount = new JLabel("Loading...");
        lblMyCount.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblMyCount.setForeground(TEXT_MUTED);

        titleBlock.add(lblTitle);
        titleBlock.add(Box.createVerticalStrut(4));
        titleBlock.add(lblMyCount);
        header.add(titleBlock, BorderLayout.WEST);

        JButton btnRefresh = accentButton("Refresh", SvgIcon.REFRESH);
        btnRefresh.addActionListener(e -> loadMyRequests());
        header.add(btnRefresh, BorderLayout.EAST);

        panel.add(header, BorderLayout.NORTH);

        // Table
        String[] cols = {"#", "Title", "Location", "Priority", "Status", "Submitted By", "Created"};
        myModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        myTable = buildTable(myModel);
        myTable.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) openDetailDialog(myTable);
            }
        });

        setColumnWidths(myTable, new int[]{45, 200, 130, 80, 110, 110, 110});

        JScrollPane scroll = styledScroll(myTable);
        panel.add(scroll, BorderLayout.CENTER);

        // Bottom action bar
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setBackground(BG_DARK);
        actions.setBorder(new EmptyBorder(14, 0, 0, 0));

        JButton btnView       = ghostButton("View Details");
        JButton btnInProgress = ghostButton("Mark In Progress");
        JButton btnComplete   = accentButton("Mark Completed", SvgIcon.CHECK);

        btnView.addActionListener(e -> openDetailDialog(myTable));
        btnInProgress.addActionListener(e -> updateStatus(myTable, Status.IN_PROGRESS));
        btnComplete.addActionListener(e   -> updateStatus(myTable, Status.COMPLETED));

        actions.add(btnView);
        actions.add(btnInProgress);
        actions.add(btnComplete);
        panel.add(actions, BorderLayout.SOUTH);

        return panel;
    }

    private void loadMyRequests() {
        if (selfTechnician == null || selfTechnician.getTechnicianId() == 0) {
            myModel.setRowCount(0);
            lblMyCount.setText("Could not resolve technician record — contact admin.");
            return;
        }
        new SwingWorker<List<MaintenanceRequest>, Void>() {
            @Override protected List<MaintenanceRequest> doInBackground() {
                return reqDAO.getAllRequests().stream()
                        .filter(r -> r.getAssignedTechnicianId() == selfTechnician.getTechnicianId())
                        .toList();
            }
            @Override protected void done() {
                try {
                    List<MaintenanceRequest> list = get();
                    myModel.setRowCount(0);
                    for (MaintenanceRequest r : list) {
                        myModel.addRow(new Object[]{
                            r.getRequestId(),
                            r.getTitle(),
                            r.getLocation() != null ? r.getLocation() : "—",
                            r.getPriority().name(),
                            r.getStatus().name(),
                            r.getSubmittedByUsername() != null ? r.getSubmittedByUsername() : "Admin",
                            r.getCreatedAtFormatted()
                        });
                    }
                    long active = list.stream()
                            .filter(r -> r.getStatus() != Status.COMPLETED).count();
                    lblMyCount.setText(list.size() + " total  ·  " + active + " active");
                } catch (Exception ex) {
                    logger.log(Level.SEVERE, "loadMyRequests failed", ex);
                }
            }
        }.execute();
    }

    // ══════════════════════════════════════════════════════════════════════════
    // ACTIONS
    // ══════════════════════════════════════════════════════════════════════════

    /** Self-assign: sets this technician as the assignee of the selected request */
    private void selfAssign(JTable table) {
        int row = table.getSelectedRow();
        if (row < 0) {
            showInfo("Select a request from the table first.");
            return;
        }
        if (selfTechnician == null || selfTechnician.getTechnicianId() == 0) {
            showError("Your technician record could not be found in the database.\n"
                    + "Please ask an admin to make sure your username matches your technician profile.");
            return;
        }

        int reqId = (int) table.getValueAt(row, 0);

        // Confirm
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "<html>Assign <b>Request #" + reqId + "</b> to yourself (" + selfTechnician.getName() + ")?<br>"
                        + "The status will be changed to <b>IN PROGRESS</b>.</html>",
                "Confirm Self-Assign",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION) return;

        // Fetch full request, update fields, save
        new SwingWorker<Boolean, Void>() {
            @Override protected Boolean doInBackground() {
                List<MaintenanceRequest> all = reqDAO.getAllRequests();
                for (MaintenanceRequest r : all) {
                    if (r.getRequestId() == reqId) {
                        r.setAssignedTechnicianId(selfTechnician.getTechnicianId());
                        r.setStatus(Status.IN_PROGRESS);
                        return reqDAO.updateRequest(r);
                    }
                }
                return false;
            }
            @Override protected void done() {
                try {
                    if (get()) {
                        showInfo("Request #" + reqId + " assigned to you and set to IN PROGRESS.\n"
                                + "Switch to 'My Assignments' to manage it.");
                        loadOpenRequests(); // refresh open list
                    } else {
                        showError("Could not assign the request. Please try again.");
                    }
                } catch (Exception ex) {
                    showError("Error: " + ex.getMessage());
                }
            }
        }.execute();
    }

    /** Update status of the selected request in My Assignments */
    private void updateStatus(JTable table, Status newStatus) {
        int row = table.getSelectedRow();
        if (row < 0) {
            showInfo("Select a request from the table first.");
            return;
        }
        int reqId = (int) table.getValueAt(row, 0);
        String currentStatus = (String) table.getValueAt(row, 4);

        if (currentStatus.equals(newStatus.name())) {
            showInfo("Request #" + reqId + " is already " + newStatus.name().replace("_", " ") + ".");
            return;
        }

        new SwingWorker<Boolean, Void>() {
            @Override protected Boolean doInBackground() {
                List<MaintenanceRequest> all = reqDAO.getAllRequests();
                for (MaintenanceRequest r : all) {
                    if (r.getRequestId() == reqId) {
                        r.setStatus(newStatus);
                        if (newStatus == Status.COMPLETED) {
                            r.setCompletionDate(java.time.LocalDate.now());
                        }
                        return reqDAO.updateRequest(r);
                    }
                }
                return false;
            }
            @Override protected void done() {
                try {
                    if (get()) {
                        loadMyRequests();
                    } else {
                        showError("Could not update status. Please try again.");
                    }
                } catch (Exception ex) {
                    showError("Error: " + ex.getMessage());
                }
            }
        }.execute();
    }

    /** Opens the details dialog for the selected row in any table */
    private void openDetailDialog(JTable table) {
        int row = table.getSelectedRow();
        if (row < 0) {
            showInfo("Select a request to view its details.");
            return;
        }
        int reqId = (int) table.getValueAt(row, 0);

        new SwingWorker<MaintenanceRequest, Void>() {
            @Override protected MaintenanceRequest doInBackground() {
                for (MaintenanceRequest r : reqDAO.getAllRequests()) {
                    if (r.getRequestId() == reqId) return r;
                }
                return null;
            }
            @Override protected void done() {
                try {
                    MaintenanceRequest r = get();
                    if (r != null) {
                        new RequestDetailsWindow(TechnicianPortalWindow.this, r).setVisible(true);
                    } else {
                        showError("Could not load request details.");
                    }
                } catch (Exception ex) {
                    showError("Error: " + ex.getMessage());
                }
            }
        }.execute();
    }

    // ══════════════════════════════════════════════════════════════════════════
    // UI HELPERS
    // ══════════════════════════════════════════════════════════════════════════

    private JTable buildTable(DefaultTableModel model) {
        JTable table = new JTable(model) {
            @Override public Component prepareRenderer(
                    javax.swing.table.TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                c.setBackground(isRowSelected(row) ? new Color(99, 102, 241, 60) : BG_CARD);
                c.setForeground(TEXT_WHITE);
                return c;
            }
        };
        table.setBackground(BG_CARD);
        table.setForeground(TEXT_WHITE);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setRowHeight(38);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 2));
        table.setSelectionBackground(new Color(99, 102, 241, 60));
        table.setSelectionForeground(TEXT_WHITE);
        table.setFillsViewportHeight(true);

        // Header
        table.getTableHeader().setBackground(BG_CARD_HOVER);
        table.getTableHeader().setForeground(TEXT_MUTED);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 11));
        table.getTableHeader().setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        ((DefaultTableCellRenderer) table.getTableHeader().getDefaultRenderer())
                .setHorizontalAlignment(SwingConstants.LEFT);

        // Custom cell renderer for Priority and Status columns
        DefaultTableCellRenderer badgeRenderer = new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel, boolean focus, int r, int c) {
                super.getTableCellRendererComponent(t, val, sel, focus, r, c);
                setBackground(sel ? new Color(99, 102, 241, 60) : BG_CARD);
                setOpaque(true);
                String s = val == null ? "" : val.toString();
                switch (s) {
                    case "HIGH"        -> setForeground(COL_HIGH);
                    case "MEDIUM"      -> setForeground(COL_MED);
                    case "LOW"         -> setForeground(COL_LOW);
                    case "PENDING"     -> setForeground(COL_PENDING);
                    case "IN_PROGRESS" -> { setForeground(COL_IN_PROGRESS); setText("IN PROGRESS"); }
                    case "COMPLETED"   -> setForeground(COL_COMPLETED);
                    default            -> setForeground(TEXT_MUTED);
                }
                setFont(new Font("Segoe UI", Font.BOLD, 12));
                setBorder(new EmptyBorder(0, 10, 0, 10));
                return this;
            }
        };

        // Apply badge renderer to Priority (col 3) and Status (col 4)
        table.getColumnModel().getColumn(3).setCellRenderer(badgeRenderer);
        table.getColumnModel().getColumn(4).setCellRenderer(badgeRenderer);

        // Default renderer for other columns
        DefaultTableCellRenderer baseRenderer = new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel, boolean focus, int r, int c) {
                super.getTableCellRendererComponent(t, val, sel, focus, r, c);
                setBackground(sel ? new Color(99, 102, 241, 60) : BG_CARD);
                setForeground(c == 0 ? TEXT_MUTED : TEXT_WHITE);
                setFont(new Font("Segoe UI", c == 0 ? Font.PLAIN : Font.PLAIN, 13));
                setBorder(new EmptyBorder(0, 10, 0, 10));
                setOpaque(true);
                return this;
            }
        };
        for (int i = 0; i < model.getColumnCount(); i++) {
            if (i != 3 && i != 4) table.getColumnModel().getColumn(i).setCellRenderer(baseRenderer);
        }

        return table;
    }

    private JScrollPane styledScroll(JTable table) {
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(BG_CARD);
        scroll.setBackground(BG_DARK);
        scroll.getVerticalScrollBar().setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            @Override protected void configureScrollBarColors() {
                thumbColor = new Color(70, 70, 100);
                trackColor = BG_CARD;
            }
            @Override protected JButton createDecreaseButton(int o) { return zeroButton(); }
            @Override protected JButton createIncreaseButton(int o) { return zeroButton(); }
            private JButton zeroButton() {
                JButton b = new JButton(); b.setPreferredSize(new Dimension(0,0)); return b;
            }
        });
        return scroll;
    }

    private void setColumnWidths(JTable table, int[] widths) {
        for (int i = 0; i < widths.length && i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
    }

    /** Filled accent button */
    private JButton accentButton(String text, SvgIcon icon) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? ACCENT_HOVER : ACCENT);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                // Draw icon left of text
                int iconSize = 16;
                int ix = 14;
                int iy = (getHeight() - iconSize) / 2;
                icon.draw(g2, ix, iy, iconSize, Color.WHITE);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setForeground(Color.WHITE);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        // Left padding leaves room for the painted icon
        btn.setBorder(new EmptyBorder(0, 36, 0, 14));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setPreferredSize(new Dimension(160, 38));
        return btn;
    }

    /** Ghost (outline) button */
    private JButton ghostButton(String text) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover()
                        ? new Color(55, 55, 80) : new Color(40, 40, 62));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(FIELD_BORDER);
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btn.setForeground(TEXT_WHITE);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(150, 38));
        return btn;
    }

    private void showInfo(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Info", JOptionPane.INFORMATION_MESSAGE);
    }
    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    // ── Vector icon renderer (font-independent, always crisp) ─────────────────
    enum SvgIcon {
        CLIPBOARD, WRENCH, LOGOUT, REFRESH, CHECK;

        void draw(Graphics2D g, int x, int y, int size, Color c) {
            g.setColor(c);
            g.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            float s = size / 24f;
            switch (this) {
                case CLIPBOARD -> {
                    g.draw(new RoundRectangle2D.Float(x+4*s, y+3*s, 16*s, 18*s, 4, 4));
                    g.draw(new RoundRectangle2D.Float(x+8*s, y+1*s, 8*s,  4*s,  4, 4));
                    g.draw(new Line2D.Float(x+8*s, y+11*s, x+16*s, y+11*s));
                    g.draw(new Line2D.Float(x+8*s, y+15*s, x+16*s, y+15*s));
                }
                case WRENCH -> {
                    // wrench handle diagonal
                    GeneralPath handle = new GeneralPath();
                    handle.moveTo(x+14.5f*s, y+9.5f*s);
                    handle.lineTo(x+21*s,    y+16*s);
                    handle.curveTo(x+22*s, y+17*s, x+22*s, y+19*s, x+21*s, y+20*s);
                    handle.curveTo(x+20*s, y+21*s, x+18*s, y+21*s, x+17*s, y+20*s);
                    handle.lineTo(x+10.5f*s, y+13.5f*s);
                    g.draw(handle);
                    // wrench head (circle at top-left)
                    g.draw(new Ellipse2D.Float(x+2*s, y+2*s, 10*s, 10*s));
                    g.draw(new Line2D.Float(x+5*s, y+2*s, x+5*s, y+5*s));
                    g.draw(new Line2D.Float(x+9*s, y+2*s, x+9*s, y+5*s));
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
                    g.draw(new Line2D.Float(x+21*s, y+12*s, x+9*s, y+12*s));
                }
                case REFRESH -> {
                    // Circular arrow (clockwise arc, open at bottom-right, with arrowhead)
                    g.draw(new Arc2D.Float(x+3*s, y+3*s, 18*s, 18*s, 50, 270, Arc2D.OPEN));
                    GeneralPath head = new GeneralPath();
                    head.moveTo(x+18*s, y+4*s);
                    head.lineTo(x+21*s, y+8*s);
                    head.lineTo(x+14*s, y+8*s);
                    head.closePath();
                    g.fill(head);
                }
                case CHECK -> {
                    g.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g.draw(new Ellipse2D.Float(x+2*s, y+2*s, 20*s, 20*s));
                    GeneralPath tick = new GeneralPath();
                    tick.moveTo(x+7*s,  y+12*s);
                    tick.lineTo(x+10*s, y+15*s);
                    tick.lineTo(x+17*s, y+9*s);
                    g.draw(tick);
                }
            }
        }
    }
}