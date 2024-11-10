import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class DBWithGUI extends JFrame {
    private JTextField sNoField, nameField, admissionNoField, enrollmentNoField, mobileNoField, emailField, dobField;
    private JComboBox<String> tableDropdownAdd, tableDropdownFetch;
    private JTable dataTable;
    private JPanel mainMenuPanel, addDataPanel, fetchDataPanel;
    private DefaultTableModel tableModel;

    private final String url = "jdbc:mysql://localhost:3306/college";
    private final String user = "root";
    private final String password = "Div@1aswal";

    public DBWithGUI() {
        setTitle("Database GUI");
        setSize(500, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new CardLayout());

        // Main menu panel with heading and smaller buttons
        mainMenuPanel = new JPanel(new BorderLayout());

        // Create and add heading
        JLabel headingLabel = new JLabel("Student Management", SwingConstants.CENTER);
        headingLabel.setFont(new Font("Arial", Font.BOLD, 24));
        mainMenuPanel.add(headingLabel, BorderLayout.NORTH);

        // Panel for buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(2, 1, 10, 10));

        JButton addDataButton = new JButton("Add Data");
        JButton fetchDataButton = new JButton("Fetch Data");

        // Set preferred size for smaller buttons
        addDataButton.setPreferredSize(new Dimension(150, 40));
        fetchDataButton.setPreferredSize(new Dimension(150, 40));

        addDataButton.addActionListener(e -> showAddDataPanel());
        fetchDataButton.addActionListener(e -> showFetchDataPanel());

        // Add buttons to the button panel
        buttonPanel.add(addDataButton);
        buttonPanel.add(fetchDataButton);

        // Center button panel in main menu
        JPanel centerPanel = new JPanel();
        centerPanel.add(buttonPanel);

        mainMenuPanel.add(centerPanel, BorderLayout.CENTER);
        add(mainMenuPanel, "MainMenu");

        // Add Data panel setup
        addDataPanel = new JPanel(new BorderLayout());
        JPanel inputPanel = new JPanel(new GridLayout(9, 2));

        tableDropdownAdd = new JComboBox<>(new String[]{"BTAIML", "BTDS", "BTCYBERS"});
        inputPanel.add(new JLabel("Table:"));
        inputPanel.add(tableDropdownAdd);

        sNoField = new JTextField();
        inputPanel.add(new JLabel("S_no:"));
        inputPanel.add(sNoField);

        nameField = new JTextField();
        inputPanel.add(new JLabel("Name:"));
        inputPanel.add(nameField);

        admissionNoField = new JTextField();
        inputPanel.add(new JLabel("Admission_no:"));
        inputPanel.add(admissionNoField);

        enrollmentNoField = new JTextField();
        inputPanel.add(new JLabel("Enrollment_no:"));
        inputPanel.add(enrollmentNoField);

        mobileNoField = new JTextField();
        inputPanel.add(new JLabel("Mobile_no:"));
        inputPanel.add(mobileNoField);

        emailField = new JTextField();
        inputPanel.add(new JLabel("Email ID:"));
        inputPanel.add(emailField);

        dobField = new JTextField();
        inputPanel.add(new JLabel("DOB (YYYY-MM-DD):"));
        inputPanel.add(dobField);

        JButton submitButton = new JButton("Submit");
        submitButton.addActionListener(e -> addData());
        JButton backButton1 = new JButton("Back");
        backButton1.addActionListener(e -> showMainMenu());

        JPanel buttonPanelAdd = new JPanel();
        buttonPanelAdd.add(submitButton);
        buttonPanelAdd.add(backButton1);

        addDataPanel.add(inputPanel, BorderLayout.CENTER);
        addDataPanel.add(buttonPanelAdd, BorderLayout.SOUTH);
        add(addDataPanel, "AddData");

        // Fetch Data panel setup
        fetchDataPanel = new JPanel(new BorderLayout());

        // Dropdown for table selection in fetch panel
        JPanel fetchControlPanel = new JPanel();
        tableDropdownFetch = new JComboBox<>(new String[]{"BTAIML", "BTDS", "BTCYBERS"});
        fetchControlPanel.add(new JLabel("Select Table:"));
        fetchControlPanel.add(tableDropdownFetch);

        JButton fetchButton = new JButton("Fetch");
        fetchButton.addActionListener(e -> fetchData());
        fetchControlPanel.add(fetchButton);

        JButton backButton2 = new JButton("Back");
        backButton2.addActionListener(e -> showMainMenu());

        // Table setup for displaying data
        tableModel = new DefaultTableModel();
        dataTable = new JTable(tableModel);

        fetchDataPanel.add(fetchControlPanel, BorderLayout.NORTH);
        fetchDataPanel.add(new JScrollPane(dataTable), BorderLayout.CENTER);
        fetchDataPanel.add(backButton2, BorderLayout.SOUTH);

        add(fetchDataPanel, "FetchData");

        showMainMenu();
    }

    private void showMainMenu() {
        CardLayout cl = (CardLayout) getContentPane().getLayout();
        cl.show(getContentPane(), "MainMenu");
    }

    private void showAddDataPanel() {
        CardLayout cl = (CardLayout) getContentPane().getLayout();
        cl.show(getContentPane(), "AddData");
    }

    private void showFetchDataPanel() {
        CardLayout cl = (CardLayout) getContentPane().getLayout();
        cl.show(getContentPane(), "FetchData");
    }

    private void addData() {
        String tableName = (String) tableDropdownAdd.getSelectedItem();
        int sNo = Integer.parseInt(sNoField.getText());
        String name = nameField.getText();
        String admissionNo = admissionNoField.getText();
        long enrollmentNo = Long.parseLong(enrollmentNoField.getText());
        long mobileNo = Long.parseLong(mobileNoField.getText());
        String emailId = emailField.getText();
        String dob = dobField.getText();

        String checkQuery = "SELECT COUNT(*) FROM " + tableName + " WHERE S_no = ? OR Admission_no = ? OR Enrollment_no = ?";
        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {

            checkStmt.setInt(1, sNo);
            checkStmt.setString(2, admissionNo);
            checkStmt.setLong(3, enrollmentNo);

            ResultSet rs = checkStmt.executeQuery();
            rs.next();
            int count = rs.getInt(1);

            if (count > 0) {
                JOptionPane.showMessageDialog(this, "Data already exists in the table.");
                return;
            }

            String insertQuery = "INSERT INTO " + tableName + " (S_no, Name, Admission_no, Enrollment_no, Mobile_no, emailid, DOB) VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(insertQuery)) {
                pstmt.setInt(1, sNo);
                pstmt.setString(2, name);
                pstmt.setString(3, admissionNo);
                pstmt.setLong(4, enrollmentNo);
                pstmt.setLong(5, mobileNo);
                pstmt.setString(6, emailId);
                pstmt.setDate(7, Date.valueOf(dob));

                int rowsAffected = pstmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "Data added successfully! Rows affected: " + rowsAffected);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error adding data: " + e.getMessage());
        }
    }

    private void fetchData() {
        String tableName = (String) tableDropdownFetch.getSelectedItem();
        String query = "SELECT * FROM " + tableName;

        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            // Clear existing table data
            tableModel.setRowCount(0);
            tableModel.setColumnCount(0);

            // Populate column headers
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            for (int i = 1; i <= columnCount; i++) {
                tableModel.addColumn(metaData.getColumnName(i));
            }

            // Populate rows with data
            while (rs.next()) {
                Object[] row = new Object[columnCount];
                for (int i = 1; i <= columnCount; i++) {
                    row[i - 1] = rs.getObject(i);
                }
                tableModel.addRow(row);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error fetching data: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            DBWithGUI app = new DBWithGUI();
            app.setVisible(true);
        });
    }
}

