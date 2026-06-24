package gui;

import dao.MaintenanceRequestDAO;
import model.MaintenanceRequest;
import model.MaintenanceRequest.Priority;
import model.MaintenanceRequest.Status;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * UserPortalWindow — v3.1  (layout fix)
 */
public class UserPortalWindow extends JFrame {

    // ── Palette ───────────────────────────────────────────────────────────────
    private static final Color BG_DARK      = new Color(18, 18, 28);
    private static final Color BG_SIDEBAR   = new Color(22, 22, 36);
    private static final Color BG_CARD      = new Color(30, 30, 46);
    private static final Color ACCENT       = new Color(99, 102, 241);
    private static final Color ACCENT_HOVER = new Color(79, 82, 221);
    private static final Color TEXT_WHITE   = new Color(240, 240, 255);
    private static final Color TEXT_MUTED   = new Color(140, 140, 175);
    private static final Color DIVIDER      = new Color(40, 40, 60);
    private static final Color FIELD_BG     = new Color(40, 40, 60);
    private static final Color FIELD_BORDER = new Color(70, 70, 100);
    private static final Color FIELD_FOCUS  = new Color(99, 102, 241);

    private final String username;
    private final MaintenanceRequestDAO dao = new MaintenanceRequestDAO();

    // Form fields
    private JTextField        txtTitle;
    private JTextField        txtLocation;
    private JTextArea         txtDescription;
    private JComboBox<String> cmbPriority;
    private JButton           btnSubmit;
    private JButton           btnClear;
    private JLabel            lblStatus;

    // Table (My Requests view — kept but not shown in sidebar nav)
    private JTable            table;
    private DefaultTableModel tableModel;

    private static final String VIEW_SUBMIT  = "SUBMIT";
    private static final String VIEW_MY_REQS = "MY_REQUESTS";

    private CardLayout cardLayout;
    private JPanel     contentPanel;
    private JButton    btnNavSubmit;

    // ── Constructor ───────────────────────────────────────────────────────────
    public UserPortalWindow(String username) {
        this.username = username;
        buildUI();
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        showView(VIEW_SUBMIT);
    }

    // ── Root UI ───────────────────────────────────────────────────────────────
    private void buildUI() {
        setTitle("SMRTS — User Portal");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(900, 600));

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG_DARK);
        setContentPane(root);

        root.add(buildSidebar(),     BorderLayout.WEST);
        root.add(buildContentArea(), BorderLayout.CENTER);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // SIDEBAR — fixed width 230 px, proper BoxLayout stacking
    // ══════════════════════════════════════════════════════════════════════════
    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(BG_SIDEBAR);
        sidebar.setPreferredSize(new Dimension(230, 0));

        // ── Avatar + name block ───────────────────────────────────────────────
        JPanel brand = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 18));
        brand.setBackground(BG_SIDEBAR);
        brand.setAlignmentX(LEFT_ALIGNMENT);
        brand.setMaximumSize(new Dimension(Integer.MAX_VALUE, 72));

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
                String init = username.isEmpty() ? "?" : String.valueOf(Character.toUpperCase(username.charAt(0)));
                g2.drawString(init, (40 - fm.stringWidth(init)) / 2,
                        (40 + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        avatar.setPreferredSize(new Dimension(40, 40));

        JPanel nameBlock = new JPanel();
        nameBlock.setLayout(new BoxLayout(nameBlock, BoxLayout.Y_AXIS));
        nameBlock.setBackground(BG_SIDEBAR);

        JLabel lName = new JLabel(username);
        lName.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lName.setForeground(TEXT_WHITE);

        JLabel lRole = new JLabel("User");
        lRole.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lRole.setForeground(TEXT_MUTED);

        nameBlock.add(lName);
        nameBlock.add(lRole);
        brand.add(avatar);
        brand.add(nameBlock);
        sidebar.add(brand);
        sidebar.add(sidebarDivider());

        // ── Logo + App name ───────────────────────────────────────────────────
        JPanel appLbl = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
        appLbl.setBackground(BG_SIDEBAR);
        appLbl.setAlignmentX(LEFT_ALIGNMENT);
        appLbl.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));

        JLabel logoLabel = new JLabel();
        try {
            java.net.URL url = getClass().getResource("/logo.png");
            if (url != null) {
                Image img = new ImageIcon(url).getImage().getScaledInstance(28, 28, Image.SCALE_SMOOTH);
                logoLabel.setIcon(new ImageIcon(img));
            } else {
                logoLabel.setText("⚙");
                logoLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
                logoLabel.setForeground(ACCENT);
            }
        } catch (Exception ex) {
            logoLabel.setText("⚙");
            logoLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
            logoLabel.setForeground(ACCENT);
        }
        logoLabel.setPreferredSize(new Dimension(28, 28));

        JLabel lApp = new JLabel("SMRTS");
        lApp.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lApp.setForeground(ACCENT);
        appLbl.add(logoLabel);
        appLbl.add(lApp);
        sidebar.add(appLbl);

        // ── Nav section ───────────────────────────────────────────────────────
        sidebar.add(sectionLabel("USER PORTAL"));

        btnNavSubmit = navBtn("Submit Request", PortalIcon.CLIPBOARD, VIEW_SUBMIT);
        sidebar.add(btnNavSubmit);

        // ── Push everything below to bottom ──────────────────────────────────
        sidebar.add(Box.createVerticalGlue());
        sidebar.add(sidebarDivider());

        // ── Session clock ─────────────────────────────────────────────────────
        sidebar.add(sectionLabel("SESSION"));

        JPanel timeRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 6));
        timeRow.setBackground(BG_SIDEBAR);
        timeRow.setAlignmentX(LEFT_ALIGNMENT);
        timeRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        JLabel lblTime = new JLabel();
        lblTime.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblTime.setForeground(TEXT_MUTED);
        timeRow.add(lblTime);
        sidebar.add(timeRow);

        Timer clock = new Timer(1000, e ->
                lblTime.setText(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))));
        clock.setInitialDelay(0);
        clock.start();

        // ── Logout button ─────────────────────────────────────────────────────
        JButton btnLogout = navBtn("Logout", PortalIcon.LOGOUT, null);
        btnLogout.setForeground(new Color(239, 68, 68));
        btnLogout.addActionListener(e -> { dispose(); new LoginForm().setVisible(true); });
        sidebar.add(btnLogout);
        sidebar.add(Box.createVerticalStrut(12));

        return sidebar;
    }

    // ══════════════════════════════════════════════════════════════════════════
    // CONTENT AREA
    // ══════════════════════════════════════════════════════════════════════════
    private JPanel buildContentArea() {
        cardLayout  = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(BG_DARK);
        contentPanel.add(buildSubmitView(), VIEW_SUBMIT);
        contentPanel.add(buildMyReqsView(), VIEW_MY_REQS);
        return contentPanel;
    }

    // ── VIEW: Submit Request ──────────────────────────────────────────────────
    private JPanel buildSubmitView() {
        // Outer panel — full height, padding around edges
        JPanel outer = new JPanel(new BorderLayout());
        outer.setBackground(BG_DARK);
        outer.setBorder(new EmptyBorder(32, 36, 24, 36));

        // ── Page header ───────────────────────────────────────────────────────
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BG_DARK);
        header.setBorder(new EmptyBorder(0, 0, 28, 0));

        JLabel h = new JLabel("Submit a Maintenance Request");
        h.setFont(new Font("Segoe UI", Font.BOLD, 26));
        h.setForeground(TEXT_WHITE);
        header.add(h, BorderLayout.WEST);

        JLabel sub = new JLabel("Fill in the details below and we'll handle the rest.");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        sub.setForeground(TEXT_MUTED);
        header.add(sub, BorderLayout.EAST);
        outer.add(header, BorderLayout.NORTH);

        // ── Card — fills the remaining space ──────────────────────────────────
        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.setColor(ACCENT);
                g2.fillRoundRect(0, 0, getWidth(), 4, 4, 4);
                g2.dispose();
            }
        };
        // Use GridBagLayout so the form sits neatly left-aligned with proper widths
        card.setLayout(new GridBagLayout());
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(36, 44, 36, 44));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill      = GridBagConstraints.HORIZONTAL;
        gbc.weightx   = 1.0;
        gbc.gridx     = 0;
        gbc.insets    = new Insets(0, 0, 6, 0);

        // ── Row: Request Title ────────────────────────────────────────────────
        gbc.gridy = 0;
        card.add(fieldLabel("Request Title *"), gbc);

        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 20, 0);
        txtTitle = styledField("Brief summary of the issue");
        card.add(txtTitle, gbc);

        // ── Row: Location ─────────────────────────────────────────────────────
        gbc.gridy = 2;
        gbc.insets = new Insets(0, 0, 6, 0);
        card.add(fieldLabel("Location / Room *"), gbc);

        gbc.gridy = 3;
        gbc.insets = new Insets(0, 0, 20, 0);
        txtLocation = styledField("e.g. Room 201, Lab 3, Basement");
        card.add(txtLocation, gbc);

        // ── Row: Description ──────────────────────────────────────────────────
        gbc.gridy = 4;
        gbc.insets = new Insets(0, 0, 6, 0);
        card.add(fieldLabel("Description (optional)"), gbc);

        gbc.gridy = 5;
        gbc.weighty = 1.0;
        gbc.fill    = GridBagConstraints.BOTH;
        gbc.insets  = new Insets(0, 0, 20, 0);
        txtDescription = new JTextArea();
        txtDescription.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtDescription.setForeground(TEXT_WHITE);
        txtDescription.setBackground(FIELD_BG);
        txtDescription.setCaretColor(TEXT_WHITE);
        txtDescription.setLineWrap(true);
        txtDescription.setWrapStyleWord(true);
        txtDescription.setBorder(new EmptyBorder(10, 12, 10, 12));
        JScrollPane descScroll = new JScrollPane(txtDescription);
        descScroll.setBorder(BorderFactory.createLineBorder(FIELD_BORDER, 1, true));
        descScroll.setBackground(FIELD_BG);
        descScroll.getViewport().setBackground(FIELD_BG);
        applyFocusBorder(txtDescription, descScroll);
        card.add(descScroll, gbc);

        // Reset vertical fill for remaining rows
        gbc.weighty = 0;
        gbc.fill    = GridBagConstraints.HORIZONTAL;

        // ── Row: Priority ─────────────────────────────────────────────────────
        gbc.gridy = 6;
        gbc.insets = new Insets(0, 0, 6, 0);
        card.add(fieldLabel("Priority Level"), gbc);

        gbc.gridy = 7;
        gbc.insets = new Insets(0, 0, 28, 0);
        cmbPriority = new JComboBox<>(new String[]{"LOW", "MEDIUM", "HIGH"});
        cmbPriority.setSelectedItem("MEDIUM");
        cmbPriority.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cmbPriority.setBackground(FIELD_BG);
        cmbPriority.setForeground(TEXT_WHITE);
        cmbPriority.setBorder(BorderFactory.createLineBorder(FIELD_BORDER, 1, true));
        card.add(cmbPriority, gbc);

        // ── Row: Buttons ──────────────────────────────────────────────────────
        gbc.gridy  = 8;
        gbc.insets = new Insets(0, 0, 10, 0);
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        btnRow.setOpaque(false);
        btnSubmit = portalButton("Submit Request", ACCENT, ACCENT_HOVER);
        btnClear  = portalButton("Clear", new Color(55, 55, 80), new Color(70, 70, 100));
        btnSubmit.addActionListener(e -> submitRequest());
        btnClear.addActionListener(e -> clearForm());
        btnRow.add(btnSubmit);
        btnRow.add(btnClear);
        card.add(btnRow, gbc);

        // ── Row: Status label ─────────────────────────────────────────────────
        gbc.gridy  = 9;
        gbc.insets = new Insets(0, 0, 0, 0);
        lblStatus = new JLabel(" ");
        lblStatus.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblStatus.setForeground(new Color(34, 197, 94));
        card.add(lblStatus, gbc);

        outer.add(card, BorderLayout.CENTER);

        // ── Footer tip ────────────────────────────────────────────────────────
        JPanel tip = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        tip.setBackground(BG_DARK);
        tip.setBorder(new EmptyBorder(12, 0, 0, 0));
        JLabel tipLbl = new JLabel("\u2139  Your request will be reviewed and assigned to a technician by an administrator.");
        tipLbl.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        tipLbl.setForeground(TEXT_MUTED);
        tip.add(tipLbl);
        outer.add(tip, BorderLayout.SOUTH);

        return outer;
    }

    // ── VIEW: My Requests (admin-only nav, hidden from sidebar) ───────────────
    private JPanel buildMyReqsView() {
        JPanel outer = new JPanel(new BorderLayout(0, 16));
        outer.setBackground(BG_DARK);
        outer.setBorder(new EmptyBorder(30, 30, 30, 30));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BG_DARK);
        header.setBorder(new EmptyBorder(0, 0, 8, 0));

        JLabel h = new JLabel("My Submitted Requests");
        h.setFont(new Font("Segoe UI", Font.BOLD, 24));
        h.setForeground(TEXT_WHITE);
        header.add(h, BorderLayout.WEST);

        JButton btnRefresh = new JButton("\u21bb  Refresh") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? new Color(55, 55, 85) : new Color(40, 40, 65));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnRefresh.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnRefresh.setForeground(TEXT_WHITE);
        btnRefresh.setOpaque(false);
        btnRefresh.setContentAreaFilled(false);
        btnRefresh.setBorderPainted(false);
        btnRefresh.setFocusPainted(false);
        btnRefresh.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnRefresh.setPreferredSize(new Dimension(110, 34));
        btnRefresh.addActionListener(e -> loadMyRequests());
        header.add(btnRefresh, BorderLayout.EAST);
        outer.add(header, BorderLayout.NORTH);

        JPanel tableCard = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(BG_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
            }
        };
        tableCard.setOpaque(false);
        tableCard.setBorder(new EmptyBorder(20, 20, 20, 20));

        String[] cols = {"ID", "Title", "Location", "Priority", "Status", "Assigned To", "Submitted"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        styleTable(table);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getViewport().setBackground(BG_CARD);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        tableCard.add(scroll, BorderLayout.CENTER);
        outer.add(tableCard, BorderLayout.CENTER);
        return outer;
    }

    // ── Submit logic ──────────────────────────────────────────────────────────
    private void submitRequest() {
        String title    = txtTitle.getText().trim();
        String location = txtLocation.getText().trim();

        if (title.isBlank())    { showStatus("Title is required.", true);    return; }
        if (location.isBlank()) { showStatus("Location is required.", true); return; }

        btnSubmit.setEnabled(false);
        btnSubmit.setText("Submitting...");

        new SwingWorker<Boolean, Void>() {
            @Override protected Boolean doInBackground() {
                MaintenanceRequest r = new MaintenanceRequest();
                r.setTitle(title);
                r.setLocation(location);
                r.setDescription(txtDescription.getText().trim());
                r.setPriority(Priority.valueOf((String) cmbPriority.getSelectedItem()));
                r.setStatus(Status.PENDING);
                r.setSubmittedByUsername(username);
                return dao.addRequest(r);
            }
            @Override protected void done() {
                btnSubmit.setEnabled(true);
                btnSubmit.setText("Submit Request");
                try {
                    if (get()) { showStatus("\u2713 Request submitted successfully!", false); clearForm(); }
                    else         showStatus("Failed to submit. Please try again.", true);
                } catch (Exception ex) {
                    showStatus("Error: " + ex.getMessage(), true);
                }
            }
        }.execute();
    }

    private void clearForm() {
        txtTitle.setText("");
        txtLocation.setText("");
        txtDescription.setText("");
        cmbPriority.setSelectedItem("MEDIUM");
    }

    private void showStatus(String msg, boolean error) {
        lblStatus.setText(msg);
        lblStatus.setForeground(error ? new Color(239, 68, 68) : new Color(34, 197, 94));
        new Timer(4000, e -> lblStatus.setText(" ")).start();
    }

    private void loadMyRequests() {
        new SwingWorker<List<MaintenanceRequest>, Void>() {
            @Override protected List<MaintenanceRequest> doInBackground() {
                return dao.getRequestsByUser(username);
            }
            @Override protected void done() {
                try {
                    tableModel.setRowCount(0);
                    for (MaintenanceRequest r : get()) {
                        tableModel.addRow(new Object[]{
                            r.getRequestId(), r.getTitle(), r.getLocation(),
                            r.getPriority().name(), r.getStatus().name(),
                            r.getTechnicianName() != null ? r.getTechnicianName() : "— Unassigned —",
                            r.getCreatedAtFormatted()
                        });
                    }
                } catch (Exception ignored) {}
            }
        }.execute();
    }

    private void showView(String view) {
        cardLayout.show(contentPanel, view);
        Color active = new Color(40, 40, 65);
        btnNavSubmit.setBackground(BG_SIDEBAR);
        if (VIEW_SUBMIT.equals(view)) btnNavSubmit.setBackground(active);
        btnNavSubmit.repaint();
    }

    // ══════════════════════════════════════════════════════════════════════════
    // WIDGET HELPERS
    // ══════════════════════════════════════════════════════════════════════════

    private JComponent sidebarDivider() {
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

    private JLabel sectionLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lbl.setForeground(new Color(90, 90, 120));
        lbl.setBorder(new EmptyBorder(14, 20, 6, 0));
        lbl.setAlignmentX(LEFT_ALIGNMENT);
        return lbl;
    }

    private JButton navBtn(String label, PortalIcon icon, String view) {
        JButton btn = new JButton() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                boolean active = getBackground().equals(new Color(40, 40, 65));
                boolean hover  = getModel().isRollover();

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
                // Draw at size 20 (was 18) — bigger icon is more visible at this nav height
                icon.draw(g2, 16, (getHeight() - 20) / 2, 20, iconCol);

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
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.repaint(); }
            public void mouseExited(MouseEvent e)  { btn.repaint(); }
        });
        return btn;
    }

    private JLabel fieldLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(TEXT_MUTED);
        return lbl;
    }

    /** Styled text field — fills full width via GridBagLayout, fixed height 46px. */
    private JTextField styledField(String tooltip) {
        JTextField f = new JTextField();
        f.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        f.setForeground(TEXT_WHITE);
        f.setBackground(FIELD_BG);
        f.setCaretColor(TEXT_WHITE);
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(FIELD_BORDER, 1, true),
                new EmptyBorder(10, 12, 10, 12)));
        f.setPreferredSize(new Dimension(0, 46));   // height fixed, width from layout
        if (tooltip != null) f.setToolTipText(tooltip);

        f.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                f.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(FIELD_FOCUS, 1, true),
                        new EmptyBorder(10, 12, 10, 12)));
            }
            @Override public void focusLost(FocusEvent e) {
                f.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(FIELD_BORDER, 1, true),
                        new EmptyBorder(10, 12, 10, 12)));
            }
        });
        return f;
    }

    /** Applies focus border highlight to the JScrollPane wrapping a JTextArea. */
    private void applyFocusBorder(JTextArea area, JScrollPane scroll) {
        area.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                scroll.setBorder(BorderFactory.createLineBorder(FIELD_FOCUS, 1, true));
            }
            @Override public void focusLost(FocusEvent e) {
                scroll.setBorder(BorderFactory.createLineBorder(FIELD_BORDER, 1, true));
            }
        });
    }

    private JButton portalButton(String text, Color color, Color hover) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? hover : color);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(Color.WHITE);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(180, 44));
        return btn;
    }

    private void styleTable(JTable tbl) {
        tbl.setBackground(BG_CARD);
        tbl.setForeground(TEXT_WHITE);
        tbl.setGridColor(DIVIDER);
        tbl.setRowHeight(36);
        tbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tbl.setSelectionBackground(new Color(99, 102, 241, 70));
        tbl.setSelectionForeground(TEXT_WHITE);
        tbl.setShowHorizontalLines(true);
        tbl.setShowVerticalLines(false);
        tbl.setFillsViewportHeight(true);
        tbl.getTableHeader().setBackground(new Color(22, 22, 36));
        tbl.getTableHeader().setForeground(TEXT_MUTED);
        tbl.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tbl.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, DIVIDER));
        tbl.getColumnModel().getColumn(0).setMaxWidth(55);

        DefaultTableCellRenderer base = new DefaultTableCellRenderer() {
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
        tbl.setDefaultRenderer(Object.class, base);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // ICON ENUM
    // ══════════════════════════════════════════════════════════════════════════
    enum PortalIcon {
        CLIPBOARD, LIST, LOGOUT;

        void draw(Graphics2D g, int x, int y, int size, Color c) {
            g.setColor(c);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            // Use double + Math.round to avoid int-cast truncation producing zero coords
            double s = size / 24.0;
            switch (this) {
                case CLIPBOARD -> {
                    g.drawRoundRect(x+r(4*s), y+r(3*s), r(16*s), r(18*s), 4, 4);
                    g.drawRoundRect(x+r(8*s), y+r(1*s), r(8*s),  r(4*s),  4, 4);
                    g.drawLine(x+r(8*s),  y+r(11*s), x+r(16*s), y+r(11*s));
                    g.drawLine(x+r(8*s),  y+r(15*s), x+r(16*s), y+r(15*s));
                }
                case LIST -> {
                    g.drawLine(x+r(9*s),  y+r(6*s),  x+r(20*s), y+r(6*s));
                    g.drawLine(x+r(9*s),  y+r(12*s), x+r(20*s), y+r(12*s));
                    g.drawLine(x+r(9*s),  y+r(18*s), x+r(20*s), y+r(18*s));
                    int dot = Math.max(3, r(3*s));   // ensure dots are always visible
                    g.fillOval(x+r(4*s), y+r(5*s),  dot, dot);
                    g.fillOval(x+r(4*s), y+r(11*s), dot, dot);
                    g.fillOval(x+r(4*s), y+r(17*s), dot, dot);
                }
                case LOGOUT -> {
                    java.awt.geom.GeneralPath door = new java.awt.geom.GeneralPath();
                    door.moveTo(x+9*s, y+21*s);
                    door.lineTo(x+5*s, y+21*s);
                    door.curveTo(x+4*s, y+21*s, x+3*s, y+20*s, x+3*s, y+19*s);
                    door.lineTo(x+3*s, y+5*s);
                    door.curveTo(x+3*s, y+4*s, x+4*s, y+3*s, x+5*s, y+3*s);
                    door.lineTo(x+9*s, y+3*s);
                    g.draw(door);
                    java.awt.geom.GeneralPath arrow = new java.awt.geom.GeneralPath();
                    arrow.moveTo(x+16*s, y+17*s);
                    arrow.lineTo(x+21*s, y+12*s);
                    arrow.lineTo(x+16*s, y+7*s);
                    g.draw(arrow);
                    g.drawLine(x+r(21*s), y+r(12*s), x+r(9*s), y+r(12*s));
                }
            }
        }

        /** Round double to nearest int for pixel-accurate icon coordinates. */
        private static int r(double v) { return (int) Math.round(v); }
    }
}