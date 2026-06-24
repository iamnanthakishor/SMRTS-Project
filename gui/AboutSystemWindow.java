package gui;

import database.DBConnection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.*;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * AboutSystemWindow — modal dialog showing system version, runtime, and DB info.
 * Accessible from the sidebar by any role.
 */
public class AboutSystemWindow extends JDialog {

    private static final Color BG_DARK    = AdminDashboard.BG_DARK;
    private static final Color BG_CARD    = AdminDashboard.BG_CARD;
    private static final Color ACCENT     = AdminDashboard.ACCENT;
    private static final Color TEXT_WHITE = AdminDashboard.TEXT_WHITE;
    private static final Color TEXT_MUTED = AdminDashboard.TEXT_MUTED;
    private static final Color DIVIDER    = AdminDashboard.DIVIDER;

    private final String loggedInUser;
    private final String role;

    public AboutSystemWindow(Frame owner, String loggedInUser, String role) {
        super(owner, "About SMRTS", true);
        this.loggedInUser = loggedInUser;
        this.role = role;
        buildUI();
        setSize(500, 640);   // taller so all rows + footer are visible
        setResizable(false);
        setLocationRelativeTo(owner);
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG_DARK);
        root.setBorder(new EmptyBorder(28, 30, 24, 30));
        setContentPane(root);

        // ── Logo + Title ─────────────────────────────────────────────────────
        JPanel topBlock = new JPanel();
        topBlock.setLayout(new BoxLayout(topBlock, BoxLayout.Y_AXIS));
        topBlock.setBackground(BG_DARK);
        topBlock.setBorder(new EmptyBorder(0, 0, 22, 0));

        // Logo — gradient circle with a clean centred gear icon (single, no clutter)
        JLabel logoCircle = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,  RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

                int w = getWidth(), h = getHeight();
                float cx = w / 2f, cy = h / 2f;

                // ── Gradient background circle ──────────────────────────────
                GradientPaint gp = new GradientPaint(0, 0,
                        new Color(99, 102, 241), w, h, new Color(139, 92, 246));
                g2.setPaint(gp);
                g2.fillOval(0, 0, w, h);

                // Subtle inner ring for depth
                g2.setColor(new Color(255, 255, 255, 22));
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawOval(4, 4, w - 9, h - 9);

                // ── Centred gear icon ───────────────────────────────────────
                // Parameters
                float outerR  = 18f;   // tip of teeth
                float innerR  = 13f;   // valley between teeth
                float coreR   =  8f;   // inner hub circle
                float holeR   =  3.2f; // centre hole
                int   teeth   =  8;

                // Build gear polygon path (alternating outer/inner vertices)
                GeneralPath gear = new GeneralPath();
                for (int i = 0; i < teeth * 2; i++) {
                    double angle = Math.toRadians(i * (360.0 / (teeth * 2)) - 90);
                    float r = (i % 2 == 0) ? outerR : innerR;
                    float px = cx + r * (float) Math.cos(angle);
                    float py = cy + r * (float) Math.sin(angle);
                    if (i == 0) gear.moveTo(px, py);
                    else        gear.lineTo(px, py);
                }
                gear.closePath();

                // Fill gear shape
                g2.setColor(new Color(255, 255, 255, 230));
                g2.fill(gear);

                // Punch out centre hole (hub) — fill with gradient colour
                g2.setPaint(gp);
                g2.fill(new Ellipse2D.Float(cx - coreR, cy - coreR, coreR * 2, coreR * 2));

                // Inner white hole dot
                g2.setColor(new Color(255, 255, 255, 200));
                g2.fill(new Ellipse2D.Float(cx - holeR, cy - holeR, holeR * 2, holeR * 2));

                g2.dispose();
            }
            @Override public Dimension getPreferredSize() { return new Dimension(72, 72); }
            @Override public Dimension getMaximumSize()   { return new Dimension(72, 72); }
        };
        logoCircle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel appName = new JLabel("SMRTS");
        appName.setFont(new Font("Segoe UI", Font.BOLD, 26));
        appName.setForeground(TEXT_WHITE);
        appName.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel appSubtitle = new JLabel("Smart Maintenance & Repair Tracking System");
        appSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        appSubtitle.setForeground(TEXT_MUTED);
        appSubtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Version badge — accent-colored pill
        JLabel version = new JLabel("Version 3.2") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(99, 102, 241, 30));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                g2.setColor(new Color(99, 102, 241, 120));
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, getHeight(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        version.setFont(new Font("Segoe UI", Font.BOLD, 12));
        version.setForeground(ACCENT);
        version.setAlignmentX(Component.CENTER_ALIGNMENT);
        version.setOpaque(false);
        version.setBorder(new EmptyBorder(4, 14, 4, 14));
        version.setHorizontalAlignment(SwingConstants.CENTER);

        // Wrap version in a flow panel so it doesn't stretch full width
        JPanel versionWrap = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        versionWrap.setOpaque(false);
        versionWrap.setAlignmentX(Component.CENTER_ALIGNMENT);
        versionWrap.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        versionWrap.add(version);

        topBlock.add(logoCircle);
        topBlock.add(Box.createVerticalStrut(14));
        topBlock.add(appName);
        topBlock.add(Box.createVerticalStrut(4));
        topBlock.add(appSubtitle);
        topBlock.add(Box.createVerticalStrut(8));
        topBlock.add(versionWrap);
        root.add(topBlock, BorderLayout.NORTH);

        // ── Info card ─────────────────────────────────────────────────────────
        JPanel card = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(6, 22, 6, 22));

        JPanel infoRows = new JPanel();
        infoRows.setLayout(new BoxLayout(infoRows, BoxLayout.Y_AXIS));
        infoRows.setOpaque(false);

        // Static info rows
        infoRows.add(infoRow("Application",     "SMRTS v3.2"));
        infoRows.add(sep());
        infoRows.add(infoRow("Platform",        "Java Swing Desktop App"));
        infoRows.add(sep());
        infoRows.add(infoRow("Java Version",    System.getProperty("java.version")));
        infoRows.add(sep());
        infoRows.add(infoRow("OS",
                System.getProperty("os.name") + " " + System.getProperty("os.version")));
        infoRows.add(sep());
        infoRows.add(infoRow("Logged In As",    loggedInUser + "  ·  " + role));
        infoRows.add(sep());
        infoRows.add(infoRow("Session Started",
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd  HH:mm:ss"))));
        infoRows.add(sep());

        // DB info (async — updates after worker completes)
        JLabel dbLabel = new JLabel("Checking…");
        dbLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        dbLabel.setForeground(TEXT_MUTED);
        infoRows.add(infoRowWithLabel("Database", dbLabel));

        card.add(infoRows, BorderLayout.CENTER);
        root.add(card, BorderLayout.CENTER);

        // ── Footer ────────────────────────────────────────────────────────────
        JPanel footerRow = new JPanel(new BorderLayout());
        footerRow.setBackground(BG_DARK);
        footerRow.setBorder(new EmptyBorder(16, 0, 0, 0));

        JLabel copy = new JLabel("\u00A9 2026 SMRTS. Built with Java Swing + MySQL.");
        copy.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        copy.setForeground(TEXT_MUTED);

        JButton btnClose = buildCloseButton();
        btnClose.addActionListener(e -> dispose());

        footerRow.add(copy,     BorderLayout.WEST);
        footerRow.add(btnClose, BorderLayout.EAST);
        root.add(footerRow, BorderLayout.SOUTH);

        // ── Load DB info async ────────────────────────────────────────────────
        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() {
                try (Connection conn = DBConnection.getConnection()) {
                    DatabaseMetaData meta = conn.getMetaData();
                    return meta.getDatabaseProductName() + " " + meta.getDatabaseProductVersion();
                } catch (Exception e) {
                    return "Connection Failed";
                }
            }
            @Override
            protected void done() {
                try {
                    String db = get();
                    dbLabel.setText(db);
                    dbLabel.setForeground(db.contains("Failed")
                            ? new Color(239, 68, 68)
                            : new Color(74, 222, 128));
                } catch (Exception ex) {
                    dbLabel.setText("Error");
                    dbLabel.setForeground(new Color(239, 68, 68));
                }
            }
        }.execute();
    }

    // ── Row builders ─────────────────────────────────────────────────────────

    private JPanel infoRow(String key, String value) {
        JLabel valLbl = new JLabel(value);
        valLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        valLbl.setForeground(TEXT_WHITE);
        return infoRowWithLabel(key, valLbl);
    }

    private JPanel infoRowWithLabel(String key, JLabel valLbl) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(10, 0, 10, 0));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));

        JLabel lKey = new JLabel(key);
        lKey.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lKey.setForeground(TEXT_MUTED);
        lKey.setPreferredSize(new Dimension(150, 20));

        valLbl.setHorizontalAlignment(SwingConstants.RIGHT);
        row.add(lKey,    BorderLayout.WEST);
        row.add(valLbl,  BorderLayout.EAST);
        return row;
    }

    // ── Separator ────────────────────────────────────────────────────────────

    private JPanel sep() {
        JPanel line = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(DIVIDER);
                g.fillRect(0, 0, getWidth(), 1);
            }
        };
        line.setOpaque(false);
        line.setPreferredSize(new Dimension(0, 1));
        line.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        return line;
    }

    // ── Close button ─────────────────────────────────────────────────────────

    private JButton buildCloseButton() {
        final boolean[] pressed = {false};

        JButton btn = new JButton("Close") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = pressed[0]
                        ? new Color(40, 40, 65)
                        : getModel().isRollover()
                                ? new Color(65, 65, 95)
                                : new Color(50, 50, 78);
                g2.setColor(bg);
                g2.fillRoundRect(0, pressed[0] ? 1 : 0, getWidth(), getHeight(), 10, 10);
                // Top highlight
                if (!pressed[0]) {
                    g2.setColor(new Color(255, 255, 255, 18));
                    g2.setStroke(new BasicStroke(1f));
                    g2.drawRoundRect(1, 1, getWidth() - 2, 8, 6, 6);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setForeground(TEXT_WHITE);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(100, 36));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mousePressed(java.awt.event.MouseEvent e)  { pressed[0] = true;  btn.repaint(); }
            @Override public void mouseReleased(java.awt.event.MouseEvent e) { pressed[0] = false; btn.repaint(); }
            @Override public void mouseExited(java.awt.event.MouseEvent e)   { pressed[0] = false; btn.repaint(); }
        });

        return btn;
    }
}