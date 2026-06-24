package gui;

import dao.UserDAO;
import model.User;
import org.mindrot.jbcrypt.BCrypt;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class UsersPanel extends JPanel {

    private static final Color BG_DARK = new Color(18, 18, 28);
    private static final Color BG_CARD = new Color(30, 30, 46);
    private static final Color TEXT_WHITE = new Color(240, 240, 255);
    private static final Color TEXT_MUTED = new Color(148, 148, 180);

    private final AdminDashboard parent;
    private final UserDAO dao = new UserDAO();

    private JTextField txtId, txtUsername, txtFullName, txtSearch;
    private JPasswordField txtPassword;
    private JComboBox<String> cmbRole;
    private JTable table;
    private DefaultTableModel tableModel;

    private JButton btnAdd, btnUpdate, btnDelete, btnClear;

    private int selectedId = -1;

    public UsersPanel(AdminDashboard parent) {
        this.parent = parent;
        setBackground(BG_DARK);
        setLayout(new BorderLayout(16, 0));
        setBorder(new EmptyBorder(28, 28, 28, 28));

        add(buildFormPanel(), BorderLayout.WEST);
        add(buildTablePanel(), BorderLayout.CENTER);

        // Force load data
        refreshTable();
        
        // Debug output
        System.out.println("UsersPanel loaded. Total users in DB: " + dao.getAllUsers().size());
    }

    private JPanel buildFormPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(BG_CARD);
        panel.setBorder(new EmptyBorder(24, 24, 24, 24));
        panel.setPreferredSize(new Dimension(320, 0));

        JLabel title = new JLabel("User Details");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(TEXT_WHITE);
        title.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(title);
        panel.add(Box.createVerticalStrut(20));

        panel.add(lbl("User ID"));
        txtId = field();
        txtId.setText("Auto-generated");
        txtId.setEditable(false);
        panel.add(txtId);
        panel.add(Box.createVerticalStrut(10));

        panel.add(lbl("Username *"));
        txtUsername = field();
        panel.add(txtUsername);
        panel.add(Box.createVerticalStrut(10));

        panel.add(lbl("Password *"));
        txtPassword = new JPasswordField();
        styleField(txtPassword);
        panel.add(txtPassword);
        panel.add(Box.createVerticalStrut(10));

        panel.add(lbl("Full Name"));
        txtFullName = field();
        panel.add(txtFullName);
        panel.add(Box.createVerticalStrut(10));

        panel.add(lbl("Role *"));
        cmbRole = new JComboBox<>(new String[]{"ADMIN", "STAFF", "USER"});
        styleCmb(cmbRole);
        panel.add(cmbRole);
        panel.add(Box.createVerticalStrut(24));

        JPanel btnGrid = new JPanel(new GridLayout(2, 2, 8, 8));
        btnGrid.setBackground(BG_CARD);
        btnGrid.setAlignmentX(LEFT_ALIGNMENT);
        btnGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));

        btnAdd    = actionBtn("Add",    new Color(34, 197, 94));
        btnUpdate = actionBtn("Update", new Color(59, 130, 246));
        btnDelete = actionBtn("Delete", new Color(239, 68, 68));
        btnClear  = actionBtn("Clear",  new Color(75, 85, 99));

        btnGrid.add(btnAdd);
        btnGrid.add(btnUpdate);
        btnGrid.add(btnDelete);
        btnGrid.add(btnClear);
        panel.add(btnGrid);

        btnAdd.addActionListener(e -> addUser());
        btnUpdate.addActionListener(e -> updateUser());
        btnDelete.addActionListener(e -> deleteUser());
        btnClear.addActionListener(e -> clearForm());

        return panel;
    }

    private JPanel buildTablePanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(BG_DARK);

        JPanel header = new JPanel(new BorderLayout(10, 0));
        header.setBackground(BG_DARK);

        JLabel title = new JLabel("System Users");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(TEXT_WHITE);
        header.add(title, BorderLayout.WEST);

        txtSearch = new JTextField();
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtSearch.setForeground(TEXT_WHITE);
        txtSearch.setBackground(new Color(40, 40, 60));
        txtSearch.setCaretColor(TEXT_WHITE);
        txtSearch.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(70, 70, 100), 1, true),
                new EmptyBorder(4, 10, 4, 10)));
        txtSearch.setPreferredSize(new Dimension(240, 32));
        txtSearch.setToolTipText("Search users...");
        txtSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { search(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { search(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { search(); }
        });

        header.add(txtSearch, BorderLayout.EAST);
        panel.add(header, BorderLayout.NORTH);

        String[] cols = {"ID", "Username", "Role", "Full Name"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(tableModel);
        styleTable(table);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) {
                populateFormFromTable();
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(50, 50, 70)));
        scroll.getViewport().setBackground(BG_CARD);
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    private void refreshTable() {
        loadTable(dao.getAllUsers());
    }

    private void loadTable(List<User> list) {
        tableModel.setRowCount(0);
        for (User u : list) {
            tableModel.addRow(new Object[]{
                u.getUserId(),
                u.getUsername(),
                u.getRole(),
                u.getFullName() != null ? u.getFullName() : ""
            });
        }
    }

    private void populateFormFromTable() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        selectedId = (int) tableModel.getValueAt(row, 0);
        txtId.setText(String.valueOf(selectedId));
        txtUsername.setText((String) tableModel.getValueAt(row, 1));
        cmbRole.setSelectedItem(tableModel.getValueAt(row, 2));
        txtFullName.setText((String) tableModel.getValueAt(row, 3));
    }

    private boolean validateFields() {
        if (txtUsername.getText().trim().isBlank()) {
            toast("Username is required.", true);
            return false;
        }
        return true;
    }

    private User buildFromFields() {
        User u = new User();
        u.setUsername(txtUsername.getText().trim());
        String pass = new String(txtPassword.getPassword());
        if (!pass.isEmpty()) {
            // Hash if not already a BCrypt hash
            u.setPassword(pass.startsWith("$2") ? pass : BCrypt.hashpw(pass, BCrypt.gensalt()));
        }
        u.setRole((String) cmbRole.getSelectedItem());
        u.setFullName(txtFullName.getText().trim());
        return u;
    }

    private void addUser() {
        if (!validateFields()) return;
        String pass = new String(txtPassword.getPassword());
        if (pass.isEmpty()) {
            toast("Password is required when creating a user.", true);
            return;
        }
        if (pass.length() < 4) {
            toast("Password must be at least 4 characters.", true);
            return;
        }
        if (dao.addUser(buildFromFields())) {
            toast("User added!", false);
            clearForm();
            refreshTable();
        } else {
            toast("Failed to add user — username may already exist.", true);
        }
    }

    private void updateUser() {
        if (selectedId < 0) return;
        if (dao.updateUser(buildFromFields())) {
            toast("User updated!", false);
            clearForm();
            refreshTable();
        }
    }

    private void deleteUser() {
        if (selectedId < 0) return;
        if (dao.deleteUser(selectedId)) {
            toast("User deleted!", false);
            clearForm();
            refreshTable();
        }
    }

    private void search() {
        String kw = txtSearch.getText().trim();
        loadTable(kw.isEmpty() ? dao.getAllUsers() : dao.searchUsers(kw));
    }

    private void clearForm() {
        txtId.setText("Auto-generated");
        txtUsername.setText("");
        txtPassword.setText("");
        txtFullName.setText("");
        cmbRole.setSelectedIndex(0);
        selectedId = -1;
        table.clearSelection();
    }

    private void toast(String msg, boolean error) {
        JOptionPane.showMessageDialog(this, msg, error ? "Error" : "Success",
                error ? JOptionPane.ERROR_MESSAGE : JOptionPane.INFORMATION_MESSAGE);
    }

    // UI Helpers (same as before)
    private JLabel lbl(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 11));
        l.setForeground(TEXT_MUTED);
        l.setAlignmentX(LEFT_ALIGNMENT);
        return l;
    }

    private JTextField field() {
        JTextField f = new JTextField();
        styleField(f);
        return f;
    }

    private void styleField(JTextField f) {
        f.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        f.setForeground(TEXT_WHITE);
        f.setBackground(new Color(40, 40, 60));
        f.setCaretColor(TEXT_WHITE);
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(70, 70, 100), 1, true),
                new EmptyBorder(8, 10, 8, 10)));
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        f.setAlignmentX(LEFT_ALIGNMENT);
    }

    private void styleCmb(JComboBox<String> cmb) {
        cmb.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cmb.setBackground(new Color(40, 40, 60));
        cmb.setForeground(TEXT_WHITE);
        cmb.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        cmb.setAlignmentX(LEFT_ALIGNMENT);
    }

    private JButton actionBtn(String text, Color color) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? color.brighter() : color);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setForeground(Color.WHITE);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void styleTable(JTable tbl) {
        tbl.setBackground(BG_CARD);
        tbl.setForeground(TEXT_WHITE);
        tbl.setGridColor(new Color(50, 50, 70));
        tbl.setRowHeight(38);
        tbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tbl.setSelectionBackground(new Color(99, 102, 241, 100));
        tbl.setSelectionForeground(TEXT_WHITE);
        tbl.setShowVerticalLines(true);
        tbl.setShowHorizontalLines(true);
        tbl.setIntercellSpacing(new Dimension(0, 0));
        tbl.setFillsViewportHeight(true);

        // Header styling — matches Requests panel
        javax.swing.table.JTableHeader header = tbl.getTableHeader();
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(
                        table, value, isSelected, hasFocus, row, column);
                lbl.setBackground(new Color(24, 24, 40));
                lbl.setForeground(new Color(180, 180, 210));
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
                lbl.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 2, 1, new Color(50, 50, 75)),
                        new EmptyBorder(0, 12, 0, 12)));
                lbl.setHorizontalAlignment(CENTER);
                lbl.setOpaque(true);
                return lbl;
            }
        });
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, 38));
        header.setBackground(new Color(24, 24, 40));
        header.setReorderingAllowed(false);

        // Cell renderer — centered text + alternating row colors matching Requests style
        DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(CENTER);
                setBorder(new EmptyBorder(0, 12, 0, 12));
                if (isSelected) {
                    setBackground(new Color(99, 102, 241, 100));
                    setForeground(TEXT_WHITE);
                } else {
                    setBackground(row % 2 == 0 ? BG_CARD : new Color(35, 35, 52));
                    setForeground(TEXT_WHITE);
                }
                return this;
            }
        };

        for (int i = 0; i < tbl.getColumnCount(); i++) {
            tbl.getColumnModel().getColumn(i).setCellRenderer(cellRenderer);
        }

        // Column widths — ID narrow, others proportional
        tbl.getColumnModel().getColumn(0).setPreferredWidth(50);   // ID
        tbl.getColumnModel().getColumn(1).setPreferredWidth(160);  // Username
        tbl.getColumnModel().getColumn(2).setPreferredWidth(100);  // Role
        tbl.getColumnModel().getColumn(3).setPreferredWidth(200);  // Full Name
    }
}