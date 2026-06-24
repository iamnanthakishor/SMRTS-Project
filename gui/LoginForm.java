package gui;

import database.DBConnection;
import org.mindrot.jbcrypt.BCrypt;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

/**
 * SMRTS Login Form — Premium Redesign v3.4
 * Changes: SVG-style icons, press animations, alignment fixes
 */
public class LoginForm extends JFrame {

    // ── Palette ──────────────────────────────────────────────────────────────
    private static final Color BG_LEFT        = new Color(26, 26, 46);
    private static final Color BG_RIGHT       = new Color(17, 17, 27);
    private static final Color ACCENT         = new Color(99, 102, 241);
    private static final Color ACCENT_HOVER   = new Color(79, 82, 212);
    private static final Color ACCENT_PRESS   = new Color(60, 63, 190);
    private static final Color TEXT_PRIMARY   = new Color(240, 240, 255);
    private static final Color TEXT_SECONDARY = new Color(136, 136, 170);
    private static final Color TEXT_MUTED     = new Color(85, 85, 115);
    private static final Color FIELD_BG       = new Color(30, 30, 48);
    private static final Color FIELD_BORDER   = new Color(55, 55, 75);
    private static final Color FIELD_FOCUS    = new Color(99, 102, 241);
    private static final Color CHIP_BG        = new Color(45, 45, 65);
    private static final Color CHIP_ACTIVE_BG = new Color(99, 102, 241, 40);
    private static final Color ERROR_COLOR    = new Color(239, 68, 68);

    private CardLayout cardLayout;
    private JPanel mainContent;
    private JLabel animatedTitle;

    // Login fields
    private JTextField     loginUsername;
    private JPasswordField loginPassword;
    private JLabel         loginErrorLabel;
    private Map<String, JButton> loginRoleChips = new HashMap<>();
    private String selectedLoginRole = "Admin";
    private JButton btnLogin;

    // Register fields
    private JTextField     regFullName;
    private JTextField     regUsername;
    private JPasswordField regPassword;
    private JPasswordField regConfirmPw;
    private JLabel         regErrorLabel;
    private Map<String, JButton> regRoleChips = new HashMap<>();
    private String selectedRegRole = "User";
    private JButton btnRegister;

    private JLabel lblDBStatus;

    private Timer wordCycleTimer;
    private final String[] words = {"simplified.", "streamlined.", "modernised.", "organised."};
    private int wordIndex = 0;

    public LoginForm() {
        buildUI();
        checkDatabase();
        startAnimations();
    }

    // ── Root UI ───────────────────────────────────────────────────────────────

    private void buildUI() {
        setTitle("SMRTS - Login");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(920, 580);
        setMinimumSize(new Dimension(920, 580));
        setResizable(false);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                createLeftBrandingPanel(), createRightFormPanel());
        split.setDividerSize(0);
        split.setDividerLocation(440);
        split.setEnabled(false);
        setContentPane(split);
    }

    // ── Left Branding Panel ───────────────────────────────────────────────────

    private JPanel createLeftBrandingPanel() {
        JPanel panel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(99, 102, 241, 20));
                g2.fillOval(getWidth() - 150, -90, 280, 280);
                g2.setColor(new Color(99, 102, 241, 14));
                g2.fillOval(-55, getHeight() - 110, 160, 160);
                g2.dispose();
            }
        };
        panel.setBackground(BG_LEFT);
        panel.setBorder(new EmptyBorder(48, 40, 48, 36));

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);

        // Badge — fixed-size pill, wrapped in a flow row so BoxLayout doesn't stretch it
        JPanel badgeFlow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        badgeFlow.setOpaque(false);
        badgeFlow.setAlignmentX(Component.LEFT_ALIGNMENT);
        badgeFlow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));

        JPanel badgePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        badgePanel.setOpaque(false);
        badgePanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(99, 102, 241, 70), 1, true),
                new EmptyBorder(4, 10, 4, 12)));

        // Gear/settings SVG icon
        JLabel gearIcon = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(165, 180, 252));
                g2.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                int cx = 7, cy = 7, r = 3;
                // Center circle
                g2.draw(new Ellipse2D.Float(cx - r, cy - r, r * 2, r * 2));
                // 8 gear teeth
                for (int i = 0; i < 8; i++) {
                    double angle = Math.toRadians(i * 45);
                    int x1 = (int)(cx + 4.5 * Math.cos(angle));
                    int y1 = (int)(cy + 4.5 * Math.sin(angle));
                    int x2 = (int)(cx + 6.2 * Math.cos(angle));
                    int y2 = (int)(cy + 6.2 * Math.sin(angle));
                    g2.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.drawLine(x1, y1, x2, y2);
                }
                g2.dispose();
            }
            @Override public Dimension getPreferredSize() { return new Dimension(14, 14); }
        };

        JLabel badgeText = new JLabel("SMRTS v3.2");
        badgeText.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        badgeText.setForeground(new Color(165, 180, 252));
        badgePanel.add(gearIcon);
        badgePanel.add(badgeText);
        badgeFlow.add(badgePanel);
        content.add(badgeFlow);
        content.add(Box.createVerticalStrut(24));

        JLabel title1 = new JLabel("Your maintenance");
        title1.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title1.setForeground(TEXT_PRIMARY);
        title1.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(title1);

        animatedTitle = new JLabel("simplified.");
        animatedTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
        animatedTitle.setForeground(ACCENT);
        animatedTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        animatedTitle.setMaximumSize(new Dimension(360, 42));
        animatedTitle.setPreferredSize(new Dimension(360, 42));
        content.add(animatedTitle);
        content.add(Box.createVerticalStrut(14));

        JLabel desc = new JLabel(
                "<html><body style='width:240px'>Smart Maintenance &amp; Request Tracking System "
                + "&mdash; built for facilities teams that move fast.</body></html>");
        desc.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        desc.setForeground(TEXT_SECONDARY);
        desc.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(desc);
        content.add(Box.createVerticalStrut(28));

        // Feature rows with custom SVG-style icons
        addFeature(content, "Real-time request tracking",          FeatureIcon.CLOCK);
        addFeature(content, "Technician assignment & scheduling",  FeatureIcon.CALENDAR);
        addFeature(content, "AI-powered chatbot assistance",       FeatureIcon.CHAT);
        addFeature(content, "Role-based access \u2014 Admin, Staff, User", FeatureIcon.SHIELD);

        content.add(Box.createVerticalGlue());

        JLabel footer = new JLabel("Smart Maintenance & Tracking System");
        footer.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        footer.setForeground(TEXT_MUTED);
        footer.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(footer);

        panel.add(content, BorderLayout.CENTER);
        return panel;
    }

    // ── Feature Icon Enum ─────────────────────────────────────────────────────

    enum FeatureIcon { CLOCK, CALENDAR, CHAT, SHIELD }

    private void addFeature(JPanel parent, String text, FeatureIcon icon) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel iconLabel = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ACCENT);
                g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                int cx = 8, cy = 8;
                switch (icon) {
                    case CLOCK:
                        g2.draw(new Ellipse2D.Float(1, 1, 14, 14));
                        g2.drawLine(cx, cy, cx, cy - 4);
                        g2.drawLine(cx, cy, cx + 3, cy + 2);
                        break;
                    case CALENDAR:
                        g2.draw(new RoundRectangle2D.Float(1, 3, 14, 12, 2, 2));
                        g2.drawLine(5, 1, 5, 5);
                        g2.drawLine(11, 1, 11, 5);
                        g2.drawLine(1, 7, 15, 7);
                        break;
                    case CHAT:
                        g2.draw(new RoundRectangle2D.Float(1, 1, 14, 11, 3, 3));
                        g2.drawLine(4, 15, 6, 12);
                        g2.drawLine(4, 5, 12, 5);
                        g2.drawLine(4, 8, 9,  8);
                        break;
                    case SHIELD:
                        int[] sx = {8, 15, 15, 8, 1, 1};
                        int[] sy = {1,  4, 10, 15, 10, 4};
                        g2.drawPolygon(sx, sy, 6);
                        g2.drawLine(5, 8, 7, 10);
                        g2.drawLine(7, 10, 11, 6);
                        break;
                }
                g2.dispose();
            }
            @Override public Dimension getPreferredSize() { return new Dimension(16, 16); }
        };

        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lbl.setForeground(new Color(140, 140, 180));
        row.add(iconLabel);
        row.add(lbl);
        parent.add(row);
        parent.add(Box.createVerticalStrut(4));
    }

    // ── Right Form Panel ──────────────────────────────────────────────────────

    private JPanel createRightFormPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_RIGHT);
        panel.setBorder(new EmptyBorder(40, 40, 40, 40));

        // Tab buttons: fixed pixel widths, placed in a FlowLayout so they never stretch
        JButton tabLogin    = createTabButton("Sign in",        true,  110);
        JButton tabRegister = createTabButton("Create account", false, 145);

        tabLogin.addActionListener(e -> {
            setTabActive(tabLogin, tabRegister);
            cardLayout.show(mainContent, "login");
        });
        tabRegister.addActionListener(e -> {
            setTabActive(tabRegister, tabLogin);
            cardLayout.show(mainContent, "register");
        });

        // FlowLayout with 0 gap keeps buttons flush; LEFT avoids centering
        JPanel tabRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        tabRow.setOpaque(false);
        tabRow.add(tabLogin);
        tabRow.add(tabRegister);

        // Outer wrapper with bottom gap so tabs sit above the card content
        JPanel tabWrapper = new JPanel(new BorderLayout());
        tabWrapper.setOpaque(false);
        tabWrapper.setBorder(new EmptyBorder(0, 0, 20, 0));
        tabWrapper.add(tabRow, BorderLayout.WEST);

        cardLayout  = new CardLayout();
        mainContent = new JPanel(cardLayout);
        mainContent.setOpaque(true);
        mainContent.setBackground(BG_RIGHT);
        mainContent.add(createLoginPanel(),    "login");
        mainContent.add(createRegisterPanel(), "register");

        panel.add(tabWrapper,  BorderLayout.NORTH);
        panel.add(mainContent, BorderLayout.CENTER);
        return panel;
    }

    private void setTabActive(JButton on, JButton off) {
        on.setBackground(new Color(45, 45, 65));
        on.setForeground(TEXT_PRIMARY);
        off.setBackground(new Color(28, 28, 42));
        off.setForeground(TEXT_SECONDARY);
    }

    private JButton createTabButton(String text, boolean active, int width) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btn.setForeground(active ? TEXT_PRIMARY : TEXT_SECONDARY);
        btn.setBackground(active ? new Color(45, 45, 65) : new Color(28, 28, 42));
        // Fixed size — both width and height locked so they never grow
        Dimension sz = new Dimension(width, 36);
        btn.setPreferredSize(sz);
        btn.setMinimumSize(sz);
        btn.setMaximumSize(sz);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(70, 70, 90), 1, true),
                new EmptyBorder(6, 0, 6, 0)));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // ── Login Panel ───────────────────────────────────────────────────────────

    private JPanel createLoginPanel() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(true);
        p.setBackground(BG_RIGHT);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);

        addFormHeader(p, "Welcome back", "Sign in to your SMRTS account");

        loginUsername = createStyledTextField("Enter your username");
        addLabeledField(p, "USERNAME", loginUsername);

        loginPassword = createStyledPasswordField();
        addPasswordField(p, "PASSWORD", loginPassword, () -> {
            boolean show = loginPassword.getEchoChar() == 0;
            loginPassword.setEchoChar(show ? '\u2022' : (char) 0);
        });

        loginErrorLabel = createErrorLabel();
        p.add(loginErrorLabel);
        p.add(Box.createVerticalStrut(6));

        addRoleChips(p, loginRoleChips, true, "login");
        p.add(Box.createVerticalStrut(8));

        btnLogin = createModernButton("Login in");
        btnLogin.addActionListener(e -> doLogin());
        p.add(btnLogin);
        p.add(Box.createVerticalStrut(14));

        // DB status with animated dot icon
        JPanel dbRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        dbRow.setOpaque(true);
        dbRow.setBackground(BG_RIGHT);
        dbRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        dbRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));

        JLabel dotIcon = new JLabel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color c = lblDBStatus != null && lblDBStatus.getForeground().equals(new Color(74, 222, 128))
                        ? new Color(74, 222, 128) : new Color(239, 68, 68);
                g2.setColor(c);
                g2.fillOval(2, 4, 8, 8);
                g2.dispose();
            }
            @Override public Dimension getPreferredSize() { return new Dimension(12, 16); }
        };

        lblDBStatus = new JLabel("Connecting...");
        lblDBStatus.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblDBStatus.setForeground(TEXT_SECONDARY);
        dbRow.add(dotIcon);
        dbRow.add(lblDBStatus);
        p.add(dbRow);
        p.add(Box.createVerticalGlue());

        return p;
    }

    // ── Register Panel ────────────────────────────────────────────────────────

    private JPanel createRegisterPanel() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(true);
        p.setBackground(BG_RIGHT);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);

        addFormHeader(p, "Create account", "Register a new SMRTS account");

        JPanel nameRow = new JPanel(new GridLayout(1, 2, 12, 0));
        nameRow.setOpaque(false);
        nameRow.setMaximumSize(new Dimension(400, 72));
        nameRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        regFullName = createStyledTextField("John Smith");
        addLabeledField(nameRow, "FULL NAME", regFullName, false);
        regUsername = createStyledTextField("johnsmith");
        addLabeledField(nameRow, "USERNAME",  regUsername, false);
        p.add(nameRow);
        p.add(Box.createVerticalStrut(12));

        regPassword = createStyledPasswordField();
        addPasswordField(p, "PASSWORD", regPassword, () -> {
            boolean show = regPassword.getEchoChar() == 0;
            char echo = show ? '\u2022' : (char) 0;
            regPassword.setEchoChar(echo);
            regConfirmPw.setEchoChar(echo);
        });

        regConfirmPw = createStyledPasswordField();
        addPasswordField(p, "CONFIRM PASSWORD", regConfirmPw, () -> {
            boolean show = regConfirmPw.getEchoChar() == 0;
            char echo = show ? '\u2022' : (char) 0;
            regPassword.setEchoChar(echo);
            regConfirmPw.setEchoChar(echo);
        });

        regErrorLabel = createErrorLabel();
        p.add(regErrorLabel);
        p.add(Box.createVerticalStrut(6));

        addRoleChips(p, regRoleChips, false, "reg");
        p.add(Box.createVerticalStrut(8));

        btnRegister = createModernButton("Create account");
        btnRegister.addActionListener(e -> doRegister());
        p.add(btnRegister);
        p.add(Box.createVerticalGlue());

        return p;
    }

    // ── Password Field with Eye Toggle ────────────────────────────────────────

    private void addPasswordField(JPanel parent, String labelText, JPasswordField pwField, Runnable toggleAction) {
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setOpaque(false);
        container.setMaximumSize(new Dimension(Integer.MAX_VALUE, 72));
        container.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel label = makeFieldLabel(labelText);
        container.add(label);
        container.add(Box.createVerticalStrut(5));

        // Use a wrapper panel that stretches to full width, then put layered pane inside
        JPanel lpWrapper = new JPanel(new BorderLayout()) {
            @Override public Dimension getMaximumSize() {
                return new Dimension(Integer.MAX_VALUE, 40);
            }
        };
        lpWrapper.setOpaque(false);
        lpWrapper.setAlignmentX(Component.LEFT_ALIGNMENT);

        // JLayeredPane inside a BorderLayout wrapper so it fills width automatically
        JLayeredPane lp = new JLayeredPane() {
            @Override public Dimension getPreferredSize() { return new Dimension(400, 40); }
            @Override public void doLayout() {
                // Re-position children when parent resizes
                int w = getWidth();
                for (Component c : getComponents()) {
                    if (c instanceof JPasswordField) c.setBounds(0, 0, w, 40);
                    else if (c instanceof JButton)   c.setBounds(w - 32, 8, 24, 24);
                }
            }
        };

        pwField.setBounds(0, 0, 400, 40);
        pwField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(FIELD_BORDER, 1, true),
                new EmptyBorder(8, 12, 8, 38)));
        lp.add(pwField, JLayeredPane.DEFAULT_LAYER);

        JButton eye = buildEyeButton(toggleAction);
        eye.setBounds(368, 8, 24, 24);
        lp.add(eye, JLayeredPane.PALETTE_LAYER);

        lpWrapper.add(lp, BorderLayout.CENTER);
        container.add(lpWrapper);
        parent.add(container);
        parent.add(Box.createVerticalStrut(12));
    }

    /** Paints an open-eye or closed-eye using pure Graphics2D — no image files. */
    private JButton buildEyeButton(Runnable toggleAction) {
        final boolean[] showing = {false};

        JButton btn = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color iconColor = getModel().isRollover()
                        ? new Color(200, 200, 230)
                        : new Color(110, 110, 150);
                g2.setColor(iconColor);
                g2.setStroke(new BasicStroke(1.7f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

                int cx = getWidth() / 2;
                int cy = getHeight() / 2;

                if (!showing[0]) {
                    // Eye OPEN
                    g2.draw(new Arc2D.Float(cx - 8, cy - 5, 16, 10, 0,  180, Arc2D.OPEN));
                    g2.draw(new Arc2D.Float(cx - 8, cy - 5, 16, 10, 0, -180, Arc2D.OPEN));
                    g2.draw(new Ellipse2D.Float(cx - 2.5f, cy - 2.5f, 5, 5));
                } else {
                    // Eye CLOSED
                    g2.draw(new Arc2D.Float(cx - 8, cy - 2, 16, 9, 0, 180, Arc2D.OPEN));
                    g2.drawLine(cx - 8, cy + 5, cx + 8, cy - 4);
                }
                g2.dispose();
            }
        };

        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setToolTipText("Show / hide password");

        btn.addActionListener(e -> {
            showing[0] = !showing[0];
            toggleAction.run();
            btn.repaint();
        });
        return btn;
    }

    // ── Field Helpers ─────────────────────────────────────────────────────────

    private void addFormHeader(JPanel parent, String title, String subtitle) {
        JLabel t = new JLabel(title);
        t.setFont(new Font("Segoe UI", Font.BOLD, 22));
        t.setForeground(TEXT_PRIMARY);
        t.setAlignmentX(Component.LEFT_ALIGNMENT);
        t.setHorizontalAlignment(SwingConstants.LEFT);
        t.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        parent.add(t);

        JLabel s = new JLabel(subtitle);
        s.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        s.setForeground(TEXT_SECONDARY);
        s.setAlignmentX(Component.LEFT_ALIGNMENT);
        s.setHorizontalAlignment(SwingConstants.LEFT);
        s.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));
        parent.add(s);
        parent.add(Box.createVerticalStrut(18));
    }

    private void addLabeledField(JPanel parent, String labelText, JComponent field) {
        addLabeledField(parent, labelText, field, true);
    }

    private void addLabeledField(JPanel parent, String labelText, JComponent field, boolean fullWidth) {
        JPanel c = new JPanel();
        c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS));
        c.setOpaque(false);
        // Force LEFT alignment on the field itself — critical for BoxLayout
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        if (fullWidth) {
            c.setMaximumSize(new Dimension(400, 72));
            c.setAlignmentX(Component.LEFT_ALIGNMENT);
            field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        }
        c.add(makeFieldLabel(labelText));
        c.add(Box.createVerticalStrut(5));
        c.add(field);
        parent.add(c);
        if (fullWidth) parent.add(Box.createVerticalStrut(12));
    }

    private JLabel makeFieldLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lbl.setForeground(TEXT_SECONDARY);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    private JTextField createStyledTextField(String placeholder) {
        JTextField f = new JTextField();
        f.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        f.setBackground(FIELD_BG);
        f.setCaretColor(ACCENT);
        f.setAlignmentX(Component.LEFT_ALIGNMENT);
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        f.setPreferredSize(new Dimension(400, 40));
        applyPlaceholder(f, placeholder);
        applyFocusBorder(f);
        return f;
    }

    private JPasswordField createStyledPasswordField() {
        JPasswordField f = new JPasswordField();
        f.setEchoChar('\u2022');
        f.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        f.setForeground(TEXT_PRIMARY);
        f.setBackground(FIELD_BG);
        f.setCaretColor(ACCENT);
        f.setAlignmentX(Component.LEFT_ALIGNMENT);
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        f.setPreferredSize(new Dimension(400, 40));
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(FIELD_BORDER, 1, true),
                new EmptyBorder(8, 12, 8, 38)));
        f.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                f.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(FIELD_FOCUS, 1, true),
                        new EmptyBorder(8, 12, 8, 38)));
            }
            @Override public void focusLost(FocusEvent e) {
                f.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(FIELD_BORDER, 1, true),
                        new EmptyBorder(8, 12, 8, 38)));
            }
        });
        return f;
    }

    private void applyPlaceholder(JTextField field, String placeholder) {
        field.setForeground(TEXT_SECONDARY);
        field.setText(placeholder);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(FIELD_BORDER, 1, true),
                new EmptyBorder(8, 12, 8, 12)));
        field.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                if (field.getText().equals(placeholder)) {
                    field.setText("");
                    field.setForeground(TEXT_PRIMARY);
                }
            }
            @Override public void focusLost(FocusEvent e) {
                if (field.getText().isEmpty()) {
                    field.setForeground(TEXT_SECONDARY);
                    field.setText(placeholder);
                }
            }
        });
    }

    private void applyFocusBorder(JTextField field) {
        field.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(FIELD_FOCUS, 1, true),
                        new EmptyBorder(8, 12, 8, 12)));
            }
            @Override public void focusLost(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(FIELD_BORDER, 1, true),
                        new EmptyBorder(8, 12, 8, 12)));
            }
        });
    }

    private JLabel createErrorLabel() {
        JLabel lbl = new JLabel(" ");
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(ERROR_COLOR);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        lbl.setHorizontalAlignment(SwingConstants.LEFT);
        lbl.setMaximumSize(new Dimension(Integer.MAX_VALUE, 18));
        return lbl;
    }

    // ── Role Chips ────────────────────────────────────────────────────────────

    private void addRoleChips(JPanel parent, Map<String, JButton> map, boolean defaultAdmin, String group) {
        parent.add(makeFieldLabel("ROLE"));
        parent.add(Box.createVerticalStrut(7));

        JPanel chips = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        chips.setOpaque(true);
        chips.setBackground(BG_RIGHT);
        chips.setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));
        chips.setAlignmentX(Component.LEFT_ALIGNMENT);

        String[] roles = {"Admin", "Technician", "User"};
        for (String role : roles) {
            boolean active = defaultAdmin ? role.equals("Admin") : role.equals("User");
            JButton chip = createRoleChip(role, active);
            String r = role;
            chip.addActionListener(e -> {
                map.forEach((k, c) -> updateChipStyle(c, k.equals(r)));
                if ("login".equals(group)) selectedLoginRole = r;
                else selectedRegRole = r;
            });
            map.put(role, chip);
            chips.add(chip);
        }
        parent.add(chips);
    }

    private JButton createRoleChip(String text, boolean active) {
        JButton chip = new JButton(text);
        chip.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        updateChipStyle(chip, active);
        chip.setFocusPainted(false);
        chip.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        // Hover effect
        chip.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                if (!chip.getForeground().equals(ACCENT)) {
                    chip.setForeground(TEXT_PRIMARY);
                }
            }
            @Override public void mouseExited(MouseEvent e) {
                if (!chip.getForeground().equals(ACCENT)) {
                    chip.setForeground(TEXT_SECONDARY);
                }
            }
        });
        return chip;
    }

    private void updateChipStyle(JButton chip, boolean active) {
        chip.setBackground(active ? CHIP_ACTIVE_BG : CHIP_BG);
        chip.setForeground(active ? ACCENT : TEXT_SECONDARY);
        chip.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(active ? ACCENT : FIELD_BORDER, 1, true),
                new EmptyBorder(5, 14, 5, 14)));
    }

    // ── Modern Button with Press Animation ────────────────────────────────────

    private JButton createModernButton(String text) {
        final boolean[] pressed = {false};

        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Pressed: sink + darken. Hover: lighten. Default: ACCENT.
                Color bg;
                if (!isEnabled()) {
                    bg = new Color(60, 62, 130);
                } else if (pressed[0]) {
                    bg = ACCENT_PRESS;
                } else if (getModel().isRollover()) {
                    bg = ACCENT_HOVER;
                } else {
                    bg = ACCENT;
                }

                // Shadow glow on hover
                if (isEnabled() && getModel().isRollover() && !pressed[0]) {
                    g2.setColor(new Color(99, 102, 241, 55));
                    g2.fill(new RoundRectangle2D.Float(-2, 2, getWidth() + 4, getHeight() + 2, 14, 14));
                }

                // Button body — shifts down 2px when pressed
                int yOffset = pressed[0] ? 2 : 0;
                g2.setColor(bg);
                g2.fill(new RoundRectangle2D.Float(0, yOffset, getWidth(), getHeight() - yOffset, 10, 10));

                // Top highlight line
                if (!pressed[0] && isEnabled()) {
                    g2.setColor(new Color(255, 255, 255, 30));
                    g2.setStroke(new BasicStroke(1f));
                    g2.draw(new RoundRectangle2D.Float(1, 1, getWidth() - 2, 10, 8, 8));
                }

                g2.dispose();

                // Translate text down when pressed
                if (pressed[0]) {
                    Graphics gShifted = g.create(0, 2, getWidth(), getHeight());
                    super.paintComponent(gShifted);
                    gShifted.dispose();
                } else {
                    super.paintComponent(g);
                }
            }
        };

        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        btn.setPreferredSize(new Dimension(400, 44));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);

        btn.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                pressed[0] = true;
                btn.repaint();
            }
            @Override public void mouseReleased(MouseEvent e) {
                pressed[0] = false;
                btn.repaint();
            }
            @Override public void mouseExited(MouseEvent e) {
                pressed[0] = false;
                btn.repaint();
            }
        });

        return btn;
    }

    // ── Animations ────────────────────────────────────────────────────────────

    private void startAnimations() {
        wordCycleTimer = new Timer(2800, e -> {
            wordIndex = (wordIndex + 1) % words.length;
            animatedTitle.setText(words[wordIndex]);
        });
        wordCycleTimer.start();
    }

    // ── Database Check ────────────────────────────────────────────────────────

    private void checkDatabase() {
        new SwingWorker<Boolean, Void>() {
            @Override protected Boolean doInBackground() {
                try (Connection c = DBConnection.getConnection()) { return true; }
                catch (Exception e) { return false; }
            }
            @Override protected void done() {
                try {
                    boolean ok = get();
                    lblDBStatus.setText(ok ? "Database connected" : "Database disconnected");
                    lblDBStatus.setForeground(ok ? new Color(74, 222, 128) : new Color(239, 68, 68));
                    lblDBStatus.getParent().repaint(); // repaint dot icon
                } catch (Exception ignored) {}
            }
        }.execute();
    }

    // ── Login Logic ───────────────────────────────────────────────────────────

    private void doLogin() {
        String username = getRealText(loginUsername);
        String password = new String(loginPassword.getPassword());

        loginErrorLabel.setText(" ");
        if (username.isEmpty()) { loginErrorLabel.setText("Username is required."); return; }
        if (password.isEmpty()) { loginErrorLabel.setText("Password is required.");  return; }

        final String chosenRole = selectedLoginRole;
        setLoading(btnLogin, true, "Signing in...");

        new SwingWorker<String, Void>() {
            private String errorDetail = null;

            @Override protected String doInBackground() {
                try (Connection conn = DBConnection.getConnection();
                     PreparedStatement pst = conn.prepareStatement(
                             "SELECT password, role FROM users WHERE BINARY username = ?")) {
                    pst.setString(1, username);
                    try (ResultSet rs = pst.executeQuery()) {
                        if (rs.next()) {
                            String stored = rs.getString("password");
                            String dbRole = rs.getString("role");

                            boolean valid;
                            try {
                                valid = stored.startsWith("$2")
                                        ? BCrypt.checkpw(password, stored)
                                        : stored.equals(password);
                            } catch (Exception bcryptEx) {
                                valid = stored.equals(password);
                            }

                            boolean roleMatch =
                                    (dbRole.equalsIgnoreCase("ADMIN")  && "Admin".equalsIgnoreCase(chosenRole)) ||
                                    (dbRole.equalsIgnoreCase("STAFF")  && "Technician".equalsIgnoreCase(chosenRole)) ||
                                    (dbRole.equalsIgnoreCase("USER")   && "User".equalsIgnoreCase(chosenRole));

                            if (valid && roleMatch) return chosenRole;
                            if (!valid)     errorDetail = "Wrong password.";
                            else            errorDetail = "Role mismatch \u2014 your account role is \"" + dbRole + "\".";
                        } else {
                            errorDetail = "No account found with that username.";
                        }
                    }
                } catch (Exception e) {
                    errorDetail = "DB error: " + e.getMessage();
                    e.printStackTrace();
                }
                return null;
            }

            @Override protected void done() {
                setLoading(btnLogin, false, "Login in");
                try {
                    String result = get();
                    if (result != null) {
                        dispose();
                        if ("User".equalsIgnoreCase(result))
                            new UserPortalWindow(username).setVisible(true);
                        else
                            new AdminDashboard(username, result).setVisible(true);
                    } else {
                        String msg = errorDetail != null
                                ? errorDetail
                                : "Invalid username, password or role.";
                        loginErrorLabel.setText(msg);
                        new Timer(5000, ev -> loginErrorLabel.setText(" ")).start();
                    }
                } catch (Exception ex) {
                    loginErrorLabel.setText("Login failed: " + ex.getMessage());
                }
            }
        }.execute();
    }

    // ── Register Logic ────────────────────────────────────────────────────────

    private void doRegister() {
        String name    = getRealText(regFullName);
        String user    = getRealText(regUsername);
        String pw      = new String(regPassword.getPassword());
        String confirm = new String(regConfirmPw.getPassword());

        regErrorLabel.setText(" ");
        if (name.isEmpty() || user.isEmpty() || pw.isEmpty()) {
            regErrorLabel.setText("All fields are required."); return;
        }
        if (!pw.equals(confirm)) {
            regErrorLabel.setText("Passwords do not match."); return;
        }
        if (pw.length() < 8) {
            regErrorLabel.setText("Password must be at least 8 characters."); return;
        }

        setLoading(btnRegister, true, "Creating account...");

        new SwingWorker<Boolean, Void>() {
            @Override protected Boolean doInBackground() {
                String hash = BCrypt.hashpw(pw, BCrypt.gensalt());
                try (Connection conn = DBConnection.getConnection();
                     PreparedStatement pst = conn.prepareStatement(
                             "INSERT INTO users (full_name, username, password, role) VALUES (?,?,?,?)")) {
                    pst.setString(1, name);
                    pst.setString(2, user);
                    pst.setString(3, hash);
                    pst.setString(4, selectedRegRole.toUpperCase());
                    return pst.executeUpdate() > 0;
                } catch (Exception e) { e.printStackTrace(); return false; }
            }
            @Override protected void done() {
                setLoading(btnRegister, false, "Create account");
                try {
                    if (get()) {
                        JOptionPane.showMessageDialog(null,
                                "Account created successfully!\nYou can now sign in.",
                                "Success", JOptionPane.INFORMATION_MESSAGE);
                        cardLayout.show(mainContent, "login");
                    } else {
                        regErrorLabel.setText("Registration failed \u2014 username may already exist.");
                        new Timer(4000, ev -> regErrorLabel.setText(" ")).start();
                    }
                } catch (Exception ex) {
                    regErrorLabel.setText("Registration error. Please try again.");
                }
            }
        }.execute();
    }

    // ── Utility ───────────────────────────────────────────────────────────────

    private String getRealText(JTextField field) {
        Color fg = field.getForeground();
        boolean isPlaceholder = fg.getRed()   == TEXT_SECONDARY.getRed()
                             && fg.getGreen() == TEXT_SECONDARY.getGreen()
                             && fg.getBlue()  == TEXT_SECONDARY.getBlue();
        if (isPlaceholder) return "";
        return field.getText().trim();
    }

    private void setLoading(JButton btn, boolean loading, String label) {
        btn.setEnabled(!loading);
        btn.setText(label);
    }

    // ── Entry Point ───────────────────────────────────────────────────────────

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginForm().setVisible(true));
    }
}