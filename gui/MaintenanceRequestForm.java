package gui;

import dao.MaintenanceRequestDAO;
import dao.TechnicianDAO;
import model.MaintenanceRequest;
import model.MaintenanceRequest.Priority;
import model.MaintenanceRequest.Status;
import model.Technician;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Maintenance Request management panel.
 */
public class MaintenanceRequestForm extends JPanel {
    
private static final Color BG_DARK = new Color(18,18,28);
private static final Color BG_CARD = new Color(30,30,46);
private static final Color TEXT_WHITE = new Color(240,240,255);
private static final Color TEXT_MUTED = new Color(148,148,180);

    // Added for dark field styling
    private static final Color FIELD_BG = new Color(40, 40, 60);
    private static final Color FIELD_BORDER = new Color(70, 70, 100);

    private final AdminDashboard       parent;
    private final MaintenanceRequestDAO dao  = new MaintenanceRequestDAO();
    private final TechnicianDAO        tDao = new TechnicianDAO();

    private JTextField  txtTitle, txtLocation, txtSearch;
    private JTextArea   txtDescription;
    private JComboBox<String>     cmbPriority, cmbStatus, cmbFilter;
    private JComboBox<Technician> cmbTechnician;

    private JTable            table;
    private DefaultTableModel tableModel;

    private JButton btnAdd, btnUpdate, btnDelete, btnClear;

    private int selectedId = -1;

    public MaintenanceRequestForm(AdminDashboard parent) {
        this.parent = parent;
        setBackground(BG_DARK);
        setLayout(new BorderLayout(16, 0));
        setBorder(new EmptyBorder(28, 28, 28, 28));

        add(buildFormPanel(), BorderLayout.WEST);
        add(buildTablePanel(), BorderLayout.CENTER);

        loadTechniciansCombo();
        loadTable(dao.getAllRequests());
    }

    private JPanel buildFormPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(BG_CARD);
        panel.setBorder(new EmptyBorder(24, 24, 24, 24));
        panel.setPreferredSize(new Dimension(320, 0));

        JLabel title = new JLabel("Request Details");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(TEXT_WHITE);
        title.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(title);
        panel.add(Box.createVerticalStrut(20));

        panel.add(lbl("Title *"));
        txtTitle = field();
        panel.add(txtTitle);
        panel.add(Box.createVerticalStrut(10));

        panel.add(lbl("Location *"));
        txtLocation = field();
        panel.add(txtLocation);
        panel.add(Box.createVerticalStrut(10));

        panel.add(lbl("Description"));
        txtDescription = new JTextArea(4, 30);
        txtDescription.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtDescription.setForeground(TEXT_WHITE);
        txtDescription.setBackground(FIELD_BG);
        txtDescription.setCaretColor(TEXT_WHITE);
        txtDescription.setLineWrap(true);
        txtDescription.setWrapStyleWord(true);
        JScrollPane descScroll = new JScrollPane(txtDescription);
        descScroll.setBorder(BorderFactory.createLineBorder(FIELD_BORDER));
        descScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
        descScroll.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(descScroll);
        panel.add(Box.createVerticalStrut(10));

        panel.add(lbl("Priority"));
        cmbPriority = combo(new String[]{"LOW", "MEDIUM", "HIGH"});
        cmbPriority.setSelectedItem("MEDIUM");
        panel.add(cmbPriority);
        panel.add(Box.createVerticalStrut(10));

        panel.add(lbl("Status"));
        cmbStatus = combo(new String[]{"PENDING", "IN_PROGRESS", "COMPLETED"});
        panel.add(cmbStatus);
        panel.add(Box.createVerticalStrut(10));

        panel.add(lbl("Assign Technician"));
        cmbTechnician = new JComboBox<>();
        styleCmb(cmbTechnician);
        panel.add(cmbTechnician);
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

        btnAdd.addActionListener(e -> addRequest());
        btnUpdate.addActionListener(e -> updateRequest());
        btnDelete.addActionListener(e -> deleteRequest());
        btnClear.addActionListener(e -> clearForm());

        return panel;
    }

    private JPanel buildTablePanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(BG_DARK);

        JPanel header = new JPanel(new BorderLayout(10, 0));
        header.setBackground(BG_DARK);

        JLabel title = new JLabel("Maintenance Requests");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(TEXT_WHITE);
        header.add(title, BorderLayout.WEST);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        controls.setBackground(BG_DARK);

        cmbFilter = new JComboBox<>(new String[]{"All", "PENDING", "IN_PROGRESS", "COMPLETED"});
        styleCmb(cmbFilter);
        cmbFilter.setPreferredSize(new Dimension(140, 32));
        cmbFilter.addActionListener(e -> filterTable());

        txtSearch = new JTextField();
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtSearch.setForeground(TEXT_WHITE);
        txtSearch.setBackground(FIELD_BG);
        txtSearch.setCaretColor(TEXT_WHITE);
        txtSearch.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(FIELD_BORDER, 1, true),
                new EmptyBorder(4, 10, 4, 10)));
        txtSearch.setPreferredSize(new Dimension(200, 32));
        txtSearch.setToolTipText("Search requests...");
        txtSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
        });

        controls.add(cmbFilter);
        controls.add(txtSearch);
        header.add(controls, BorderLayout.EAST);
        panel.add(header, BorderLayout.NORTH);

        String[] cols = {"ID", "Title", "Location", "Priority", "Status", "Technician", "Created"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        styleTable(table);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) populateFormFromTable();
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(50, 50, 70)));
        scroll.getViewport().setBackground(BG_CARD);
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    private void addRequest() {
        if (!validateForm()) return;
        MaintenanceRequest r = buildFromFields();
        if (dao.addRequest(r)) {
            toast("Request added successfully!", false);
            clearForm();
            refreshAll();
        } else {
            toast("Failed to add request.", true);
        }
    }

    private void updateRequest() {
        if (selectedId < 0) {
            toast("Select a request to update.", true);
            return;
        }
        if (!validateForm()) return;
        MaintenanceRequest r = buildFromFields();
        r.setRequestId(selectedId);
        if (dao.updateRequest(r)) {
            toast("Request updated successfully!", false);
            clearForm();
            refreshAll();
        } else {
            toast("Failed to update request.", true);
        }
    }

    private void deleteRequest() {
        if (selectedId < 0) {
            toast("Select a request to delete.", true);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, 
                "Delete request ID " + selectedId + "?", "Confirm", 
                JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            if (dao.deleteRequest(selectedId)) {
                toast("Request deleted successfully.", false);
                clearForm();
                refreshAll();
            } else {
                toast("Failed to delete request.", true);
            }
        }
    }

    private void refreshAll() {
        loadTable(dao.getAllRequests());
        parent.refreshStats();
    }

    private void clearForm() {
        txtTitle.setText("");
        txtLocation.setText("");
        txtDescription.setText("");
        cmbPriority.setSelectedItem("MEDIUM");
        cmbStatus.setSelectedIndex(0);
        cmbTechnician.setSelectedIndex(0);
        table.clearSelection();
        selectedId = -1;
    }

    private void filterTable() {
        String keyword = txtSearch.getText().trim();
        String filter = (String) cmbFilter.getSelectedItem();

        List<MaintenanceRequest> list = keyword.isEmpty() 
                ? dao.getAllRequests() 
                : dao.searchRequests(keyword);

        if (!"All".equals(filter)) {
            Status s = Status.valueOf(filter);
            list = list.stream().filter(r -> r.getStatus() == s).toList();
        }
        loadTable(list);
    }

    private void loadTechniciansCombo() {
        cmbTechnician.removeAllItems();
        cmbTechnician.addItem(new Technician(0, "— None —", "", "", "", "Available"));
        for (Technician t : tDao.getAllTechnicians()) {
            cmbTechnician.addItem(t);
        }
    }
    private void loadTable(List<MaintenanceRequest> list) {
        // Sort by Request ID ascending
        list.sort((r1, r2) -> Integer.compare(r1.getRequestId(), r2.getRequestId()));
        
        tableModel.setRowCount(0);
        for (MaintenanceRequest r : list) {
            tableModel.addRow(new Object[]{
                r.getRequestId(),
                r.getTitle(),
                r.getLocation(),
                r.getPriority().name(),
                r.getStatus().name(),
                r.getTechnicianName() != null ? r.getTechnicianName() : "—",
                r.getCreatedAtFormatted()
            });
        }
    }

    private void populateFormFromTable() {
        int row = table.getSelectedRow();
        if (row < 0) return;

        selectedId = (int) tableModel.getValueAt(row, 0);
        txtTitle.setText((String) tableModel.getValueAt(row, 1));
        txtLocation.setText((String) tableModel.getValueAt(row, 2));
        cmbPriority.setSelectedItem(tableModel.getValueAt(row, 3));
        cmbStatus.setSelectedItem(tableModel.getValueAt(row, 4));
    }

    private boolean validateForm() {
        if (txtTitle.getText().trim().isBlank()) {
            toast("Title is required.", true);
            return false;
        }
        if (txtLocation.getText().trim().isBlank()) {
            toast("Location is required.", true);
            return false;
        }
        return true;
    }

    private MaintenanceRequest buildFromFields() {
        MaintenanceRequest r = new MaintenanceRequest();
        r.setTitle(txtTitle.getText().trim());
        r.setLocation(txtLocation.getText().trim());
        r.setDescription(txtDescription.getText().trim());
        r.setPriority(Priority.valueOf((String) cmbPriority.getSelectedItem()));
        r.setStatus(Status.valueOf((String) cmbStatus.getSelectedItem()));

        Technician tech = (Technician) cmbTechnician.getSelectedItem();
        if (tech != null && tech.getTechnicianId() > 0) {
            r.setAssignedTechnicianId(tech.getTechnicianId());
        }
        return r;
    }

    private void toast(String msg, boolean error) {
        JOptionPane.showMessageDialog(this, msg,
                error ? "Error" : "Success",
                error ? JOptionPane.ERROR_MESSAGE : JOptionPane.INFORMATION_MESSAGE);
    }

    // UI Helpers
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
        f.setBackground(FIELD_BG);
        f.setCaretColor(TEXT_WHITE);
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(FIELD_BORDER, 1, true),
                new EmptyBorder(8, 10, 8, 10)));
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        f.setAlignmentX(LEFT_ALIGNMENT);
        return f;
    }

    private JComboBox<String> combo(String[] items) {
        JComboBox<String> c = new JComboBox<>(items);
        styleCmb(c);
        return c;
    }

    private <T> void styleCmb(JComboBox<T> cmb) {
        cmb.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cmb.setBackground(FIELD_BG);
        cmb.setForeground(TEXT_WHITE);
        cmb.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        cmb.setAlignmentX(LEFT_ALIGNMENT);
        cmb.setBorder(BorderFactory.createLineBorder(FIELD_BORDER));
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

        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val, boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                setHorizontalAlignment(CENTER);
                String v = val == null ? "" : val.toString();
                setForeground(switch (v) {
                    case "HIGH", "PENDING" -> new Color(239, 68, 68);
                    case "MEDIUM" -> new Color(245, 158, 11);
                    case "LOW", "COMPLETED" -> new Color(34, 197, 94);
                    case "IN_PROGRESS" -> new Color(59, 130, 246);
                    default -> TEXT_WHITE;
                });
                return this;
            }
        };

        tbl.getColumnModel().getColumn(0).setMaxWidth(60);
        tbl.getColumnModel().getColumn(3).setCellRenderer(renderer);
        tbl.getColumnModel().getColumn(4).setCellRenderer(renderer);
    }
}