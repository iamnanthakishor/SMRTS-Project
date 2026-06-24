package gui;

import dao.TechnicianDAO;
import model.Technician;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Technician management panel - Fully aligned with MaintenanceRequestForm styling.
 */
public class TechnicianForm extends JPanel {

    private static final Color BG_DARK = new Color(18, 18, 28);
    private static final Color BG_CARD = new Color(30, 30, 46);
    private static final Color TEXT_WHITE = new Color(240, 240, 255);
    private static final Color TEXT_MUTED = new Color(148, 148, 180);

    private final AdminDashboard parent;
    private final TechnicianDAO dao = new TechnicianDAO();

    private JTextField txtId, txtName, txtContact, txtEmail, txtDepartment, txtSearch;
    private JTable table;
    private DefaultTableModel tableModel;

    private JButton btnAdd, btnUpdate, btnDelete, btnClear;

    private int selectedId = -1;

    public TechnicianForm(AdminDashboard parent) {
        this.parent = parent;
        setBackground(BG_DARK);
        setLayout(new BorderLayout(16, 0));
        setBorder(new EmptyBorder(28, 28, 28, 28));

        add(buildFormPanel(), BorderLayout.WEST);
        add(buildTablePanel(), BorderLayout.CENTER);

        loadTable(dao.getAllTechnicians());
    }

    private JPanel buildFormPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(BG_CARD);
        panel.setBorder(new EmptyBorder(24, 24, 24, 24));
        panel.setPreferredSize(new Dimension(320, 0));

        JLabel title = new JLabel("Technician Details");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(TEXT_WHITE);
        title.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(title);
        panel.add(Box.createVerticalStrut(20));

        panel.add(lbl("Technician ID"));
        txtId = field();
        txtId.setText("Auto-generated");
        txtId.setEditable(false);
        panel.add(txtId);
        panel.add(Box.createVerticalStrut(10));

        panel.add(lbl("Full Name *"));
        txtName = field();
        panel.add(txtName);
        panel.add(Box.createVerticalStrut(10));

        panel.add(lbl("Contact Number *"));
        txtContact = field();
        panel.add(txtContact);
        panel.add(Box.createVerticalStrut(10));

        panel.add(lbl("Email Address *"));
        txtEmail = field();
        panel.add(txtEmail);
        panel.add(Box.createVerticalStrut(10));

        panel.add(lbl("Department *"));
        txtDepartment = field();
        panel.add(txtDepartment);
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

        btnAdd.addActionListener(e -> addTechnician());
        btnUpdate.addActionListener(e -> updateTechnician());
        btnDelete.addActionListener(e -> deleteTechnician());
        btnClear.addActionListener(e -> clearForm());

        return panel;
    }

    private JPanel buildTablePanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(BG_DARK);

        JPanel header = new JPanel(new BorderLayout(10, 0));
        header.setBackground(BG_DARK);

        JLabel title = new JLabel("All Technicians");
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
        txtSearch.setToolTipText("Search technicians...");
        txtSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { search(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { search(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { search(); }
        });

        header.add(txtSearch, BorderLayout.EAST);
        panel.add(header, BorderLayout.NORTH);

        String[] cols = {"ID", "Name", "Contact", "Email", "Department"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(tableModel);
        styleTable(table);

        // Fixed selection listener
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

    // ==================== CRUD ====================
    private void addTechnician() {
        if (!validateFields()) return;
        Technician t = buildFromFields();
        if (dao.addTechnician(t)) {
            toast("Technician added successfully!", false);
            clearForm();
            refreshTable();
        } else {
            toast("Failed to add technician.", true);
        }
    }

    private void updateTechnician() {
        if (selectedId < 0) {
            toast("Select a technician first.", true);
            return;
        }
        if (!validateFields()) return;
        Technician t = buildFromFields();
        t.setTechnicianId(selectedId);
        if (dao.updateTechnician(t)) {
            toast("Technician updated successfully!", false);
            clearForm();
            refreshTable();
        } else {
            toast("Failed to update technician.", true);
        }
    }

    private void deleteTechnician() {
        if (selectedId < 0) {
            toast("Select a technician to delete.", true);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete technician ID " + selectedId + "?", "Confirm Delete",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            if (dao.deleteTechnician(selectedId)) {
                toast("Technician deleted successfully.", false);
                clearForm();
                refreshTable();
                if (parent != null) parent.refreshStats();
            } else {
                toast("Failed to delete technician.", true);
            }
        }
    }

    private void refreshTable() {
        loadTable(dao.getAllTechnicians());
        if (parent != null) parent.refreshStats();
    }

    private void clearForm() {
        txtId.setText("Auto-generated");
        txtName.setText("");
        txtContact.setText("");
        txtEmail.setText("");
        txtDepartment.setText("");
        table.clearSelection();
        selectedId = -1;
    }

    private void search() {
        String kw = txtSearch.getText().trim();
        List<Technician> results = kw.isEmpty() ? dao.getAllTechnicians() : dao.searchTechnicians(kw);
        loadTable(results);
    }

    private void loadTable(List<Technician> list) {
        // Sort by ID ascending so newest/oldest order is correct
        list.sort((t1, t2) -> Integer.compare(t1.getTechnicianId(), t2.getTechnicianId()));
        
        tableModel.setRowCount(0);
        for (Technician t : list) {
            tableModel.addRow(new Object[]{
                t.getTechnicianId(), 
                t.getName(),
                t.getContactNo(), 
                t.getEmail(), 
                t.getDepartment()
            });
        }
    }

    private void populateFormFromTable() {
        int row = table.getSelectedRow();
        if (row < 0) return;

        selectedId = (int) tableModel.getValueAt(row, 0);
        txtId.setText(String.valueOf(selectedId));
        txtName.setText((String) tableModel.getValueAt(row, 1));
        txtContact.setText((String) tableModel.getValueAt(row, 2));
        txtEmail.setText((String) tableModel.getValueAt(row, 3));
        txtDepartment.setText((String) tableModel.getValueAt(row, 4));
    }

    private boolean validateFields() {
        if (txtName.getText().trim().isBlank()) {
            toast("Name is required.", true);
            return false;
        }
        if (txtContact.getText().trim().isBlank()) {
            toast("Contact is required.", true);
            return false;
        }
        if (txtEmail.getText().trim().isBlank()) {
            toast("Email is required.", true);
            return false;
        }
        if (txtDepartment.getText().trim().isBlank()) {
            toast("Department is required.", true);
            return false;
        }
        return true;
    }

    private Technician buildFromFields() {
        Technician t = new Technician();
        t.setName(txtName.getText().trim());
        t.setContactNo(txtContact.getText().trim());
        t.setEmail(txtEmail.getText().trim());
        t.setDepartment(txtDepartment.getText().trim());
        return t;
    }

    private void toast(String msg, boolean error) {
        JOptionPane.showMessageDialog(this, msg,
                error ? "Error" : "Success",
                error ? JOptionPane.ERROR_MESSAGE : JOptionPane.INFORMATION_MESSAGE);
    }

    // ==================== UI Helpers ====================
    private JLabel lbl(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 11));
        l.setForeground(TEXT_MUTED);
        l.setAlignmentX(LEFT_ALIGNMENT);
        return l;
    }

    private JTextField field() {
        JTextField f = new JTextField();
        f.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        f.setForeground(TEXT_WHITE);
        f.setBackground(new Color(40, 40, 60));
        f.setCaretColor(TEXT_WHITE);
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(70, 70, 100), 1, true),
                new EmptyBorder(8, 10, 8, 10)));
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        f.setAlignmentX(LEFT_ALIGNMENT);
        return f;
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
        tbl.setRowHeight(36);
        tbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tbl.setSelectionBackground(new Color(99, 102, 241, 80));
        tbl.setSelectionForeground(TEXT_WHITE);
        tbl.setShowHorizontalLines(true);
        tbl.setShowVerticalLines(false);
        tbl.setFillsViewportHeight(true);

        tbl.getTableHeader().setBackground(new Color(24, 24, 38));
        tbl.getTableHeader().setForeground(TEXT_MUTED);
        tbl.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        tbl.getColumnModel().getColumn(0).setCellRenderer(center);
        tbl.getColumnModel().getColumn(0).setMaxWidth(70);
    }
}