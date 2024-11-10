import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class AttendanceSystemGUI {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/college";
    private static final String DB_USERNAME = "root";
    private static final String DB_PASSWORD = "Div@1aswal";
    static Map<String, String> admissionNumbers = new HashMap<>(); // Map to store admission numbers
    static int totalClasses = 0;

    public static void main(String[] args) {
        showMainMenu();
    }

    public static void showMainMenu() {
        JFrame frame = new JFrame("Attendance System");
        frame.setSize(400, 200);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new GridLayout(3, 1));

        JButton adminButton = new JButton("Admin");
        JButton studentButton = new JButton("Student");

        adminButton.addActionListener(e -> {
            frame.dispose();
            showAdminLogin();
        });

        studentButton.addActionListener(e -> {
            frame.dispose();
            showStudentPanel();
        });

        frame.add(new JLabel("Welcome to Attendance System", SwingConstants.CENTER));
        frame.add(adminButton);
        frame.add(studentButton);

        frame.setVisible(true);
    }

    public static void showAdminLogin() {
        JFrame loginFrame = new JFrame("Admin Login");
        loginFrame.setSize(300, 150);
        loginFrame.setLayout(new GridLayout(3, 1));

        JLabel passwordLabel = new JLabel("Enter Admin Password:");
        JPasswordField passwordField = new JPasswordField();
        JButton loginButton = new JButton("Login");

        loginButton.addActionListener(e -> {
            String password = new String(passwordField.getPassword());
            if (password.equals("1234")) { // Check if password is correct
                loginFrame.dispose();
                showAdminPanel();
            } else {
                JOptionPane.showMessageDialog(loginFrame, "Incorrect password. Try again.");
            }
        });

        loginFrame.add(passwordLabel);
        loginFrame.add(passwordField);
        loginFrame.add(loginButton);

        loginFrame.setVisible(true);
    }

    public static void showAdminPanel() {
        JFrame adminFrame = new JFrame("Admin Panel - Select Department");
        adminFrame.setSize(400, 200);
        adminFrame.setLayout(new GridLayout(4, 1));

        JLabel instructionLabel = new JLabel("Select Department to Mark Attendance:");
        JButton btaimlButton = new JButton("BTAIML");
        JButton btdsButton = new JButton("BTDS");
        JButton btcybersButton = new JButton("BTCYBERS");

        btaimlButton.addActionListener(e -> loadStudentData("BTAIML", adminFrame));
        btdsButton.addActionListener(e -> loadStudentData("BTDS", adminFrame));
        btcybersButton.addActionListener(e -> loadStudentData("BTCYBERS", adminFrame));

        adminFrame.add(instructionLabel);
        adminFrame.add(btaimlButton);
        adminFrame.add(btdsButton);
        adminFrame.add(btcybersButton);

        adminFrame.setVisible(true);
    }

    private static void loadStudentData(String tableName, JFrame adminFrame) {
        // Clear existing data
        admissionNumbers.clear();

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT Name, Admission_no FROM " + tableName)) {

            while (rs.next()) {
                String name = rs.getString("Name");
                String admissionNo = rs.getString("Admission_no");
                admissionNumbers.put(admissionNo, name); // Store admission number as key, name as value
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(adminFrame, "Database connection error: " + e.getMessage());
            return;
        }

        adminFrame.dispose();
        showMarkAttendancePanel();
    }

    public static void showMarkAttendancePanel() {
        JFrame markFrame = new JFrame("Mark Attendance");
        markFrame.setSize(400, 400);
        markFrame.setLayout(new GridLayout(admissionNumbers.size() + 2, 2));

        JLabel instructionLabel = new JLabel("Mark Attendance for Each Student:");
        markFrame.add(instructionLabel);
        markFrame.add(new JLabel("")); // Placeholder for layout

        Map<String, JComboBox<String>> attendanceBoxes = new HashMap<>();

        for (String admissionNo : admissionNumbers.keySet()) {
            JLabel studentLabel = new JLabel(admissionNumbers.get(admissionNo)); // Show student name
            JComboBox<String> statusBox = new JComboBox<>(new String[]{"Present", "Absent"});
            markFrame.add(studentLabel);
            markFrame.add(statusBox);
            attendanceBoxes.put(admissionNo, statusBox);
        }

        JButton submitButton = new JButton("Submit Attendance");
        JButton backButton = new JButton("Back to Main Menu");

        submitButton.addActionListener(e -> {
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
                 PreparedStatement pstmt = conn.prepareStatement(
                         "INSERT INTO attendance_records (admission_no, student_name, date, status) VALUES (?, ?, CURDATE(), ?)")) {

                for (String admissionNo : admissionNumbers.keySet()) {
                    String status = (String) attendanceBoxes.get(admissionNo).getSelectedItem();
                    String studentName = admissionNumbers.get(admissionNo);

                    // Insert attendance record for each student
                    pstmt.setString(1, admissionNo);
                    pstmt.setString(2, studentName);
                    pstmt.setString(3, status);
                    pstmt.addBatch();
                }

                pstmt.executeBatch(); // Execute all inserts in a batch for efficiency
                totalClasses++; // Increment total class count
                JOptionPane.showMessageDialog(markFrame, "Attendance has been recorded for Class #" + totalClasses);

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(markFrame, "Error storing attendance data: " + ex.getMessage());
            }
        });

        backButton.addActionListener(e -> {
            markFrame.dispose();
            showMainMenu();
        });

        markFrame.add(submitButton);
        markFrame.add(backButton);

        markFrame.setVisible(true);
    }

    public static double calculateAttendancePercentage(String admissionNo) {
        int totalClassesAttended = 0;
        int totalClasses = 0;

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(
                     "SELECT status FROM attendance_records WHERE admission_no = ?")) {

            pstmt.setString(1, admissionNo);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                totalClasses++;
                if (rs.getString("status").equals("Present")) {
                    totalClassesAttended++;
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }

        return totalClasses > 0 ? ((double) totalClassesAttended / totalClasses) * 100 : 0;
    }

    public static void showStudentPanel() {
        JFrame studentFrame = new JFrame("Student Panel - Check Attendance");
        studentFrame.setSize(400, 300);
        studentFrame.setLayout(new GridLayout(4, 1));

        JLabel idLabel = new JLabel("Enter your admission number:");
        JTextField idField = new JTextField();
        JButton checkButton = new JButton("Check Attendance");
        JButton backButton = new JButton("Back to Main Menu");

        checkButton.addActionListener(e -> {
            String admissionNumber = idField.getText().trim();
            if (admissionNumbers.containsKey(admissionNumber)) {
                double attendancePercentage = calculateAttendancePercentage(admissionNumber);
                String studentName = admissionNumbers.get(admissionNumber);
                JOptionPane.showMessageDialog(studentFrame, "Student: " + studentName + "\nAttendance Percentage: " + attendancePercentage + "%");
            } else {
                JOptionPane.showMessageDialog(studentFrame, "Admission number not found.");
            }
        });

        backButton.addActionListener(e -> {
            studentFrame.dispose();
            showMainMenu();
        });

        studentFrame.add(idLabel);
        studentFrame.add(idField);
        studentFrame.add(checkButton);
        studentFrame.add(backButton);

        studentFrame.setVisible(true);
    }
}
