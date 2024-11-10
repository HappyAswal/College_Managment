import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;

// Class to represent a Book with name and quantity
class Book {
    private String name;
    private int quantity;

    public Book(String name, int quantity) {
        this.name = name;
        this.quantity = quantity;
    }

    public String getName() {
        return name;
    }

    public int getQuantity() {
        return quantity;
    }

    public void deductQuantity() {
        if (quantity > 0) {
            quantity--;
        }
    }

    public void returnBook() {
        quantity++;
    }

    public boolean isAvailable() {
        return quantity > 0;
    }
}

// Database connection details and class to manage books
class Library {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/college"; // Directly using the 'college' database
    private static final String DB_USERNAME = "root";
    private static final String DB_PASSWORD = "Div@1aswal";
    private Connection connection;

    // Constructor for connecting to the 'college' database
    public Library() {
        try {
            connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
            System.out.println("Connected to the college database successfully!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Map<String, Book> getBooks() {
        Map<String, Book> books = new HashMap<>();
        String query = "SELECT * FROM books";
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                String name = rs.getString("name");
                int quantity = rs.getInt("quantity");
                books.put(name, new Book(name, quantity));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return books;
    }

    public Book getBook(String name) {
        String query = "SELECT * FROM books WHERE name = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, name);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String bookName = rs.getString("name");
                int quantity = rs.getInt("quantity");
                return new Book(bookName, quantity);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void updateBookQuantity(String name, int newQuantity) {
        String query = "UPDATE books SET quantity = ? WHERE name = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, newQuantity);
            stmt.setString(2, name);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addBook(String name, int quantity) {
        String query = "INSERT INTO books (name, quantity) VALUES (?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, name);
            stmt.setInt(2, quantity);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteBook(String name) {
        String query = "DELETE FROM books WHERE name = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, name);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Connection getConnection() {
        return connection;
    }
}

// GUI Class for the Library Management System
public class LibraryManagementSystemGUI extends JFrame {
    private Library library;
    private JTextField nameField, admissionField, bookField;
    private JTextArea bookListArea;
    private JComboBox<String> sectionComboBox;
    private JLabel messageLabel;

    public LibraryManagementSystemGUI() {
        // Initialize with college database for student data
        library = new Library();

        setTitle("Library Management System");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new GridLayout(5, 2));

        JLabel nameLabel = new JLabel("Name:");
        nameField = new JTextField();

        JLabel admissionLabel = new JLabel("Admission Number:");
        admissionField = new JTextField();

        JLabel sectionLabel = new JLabel("Section:");
        sectionComboBox = new JComboBox<>(new String[]{"BTAIML", "BTDS", "BTCYBERS"});

        JLabel bookLabel = new JLabel("Book Name:");
        bookField = new JTextField();

        JButton borrowButton = new JButton("Borrow Book");
        JButton returnButton = new JButton("Return Book");
        JButton showBooksButton = new JButton("Show Available Books");
        JButton updateBooksButton = new JButton("Update Books");

        messageLabel = new JLabel("", JLabel.CENTER);

        inputPanel.add(nameLabel);
        inputPanel.add(nameField);
        inputPanel.add(admissionLabel);
        inputPanel.add(admissionField);
        inputPanel.add(sectionLabel);
        inputPanel.add(sectionComboBox);
        inputPanel.add(bookLabel);
        inputPanel.add(bookField);
        inputPanel.add(borrowButton);
        inputPanel.add(returnButton);

        bookListArea = new JTextArea();
        bookListArea.setEditable(false);

        JScrollPane scrollPane = new JScrollPane(bookListArea);

        add(inputPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(showBooksButton, BorderLayout.WEST);
        add(updateBooksButton, BorderLayout.EAST);
        add(messageLabel, BorderLayout.SOUTH);

        showBooksButton.addActionListener(e -> displayAvailableBooks());
        borrowButton.addActionListener(e -> borrowBook());
        returnButton.addActionListener(e -> returnBook());
        updateBooksButton.addActionListener(e -> openUpdateBooksDialog());
    }

    private void displayAvailableBooks() {
        Map<String, Book> books = library.getBooks();
        StringBuilder booksDisplay = new StringBuilder("Available Books:\n");
        for (Book book : books.values()) {
            booksDisplay.append(book.getName()).append(" (Quantity: ").append(book.getQuantity()).append(")\n");
        }
        bookListArea.setText(booksDisplay.toString());
    }

    private boolean authenticateStudent(String admissionNumber, String section, String name) {
        // Directly using the 'college' database for student authentication
        String query = "SELECT * FROM " + getTableNameForSection(section) + " WHERE Admission_no = ? AND name = ?";
        try (PreparedStatement stmt = library.getConnection().prepareStatement(query)) {
            stmt.setString(1, admissionNumber);
            stmt.setString(2, name);
            ResultSet rs = stmt.executeQuery();
            return rs.next(); // If student is found, return true
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private String getTableNameForSection(String section) {
        switch (section) {
            case "BTAIML":
                return "BTAIML";
            case "BTDS":
                return "BTDS";
            case "BTCYBERS":
                return "BTCYBERS";
            default:
                return "";
        }
    }

    private void borrowBook() {
        String bookName = bookField.getText();
        String userName = nameField.getText();
        String admissionNumber = admissionField.getText();
        String section = (String) sectionComboBox.getSelectedItem();

        if (userName.isEmpty() || admissionNumber.isEmpty() || bookName.isEmpty()) {
            messageLabel.setText("Please fill all the fields!");
            return;
        }

        if (!authenticateStudent(admissionNumber, section, userName)) {
            messageLabel.setText("Student not authenticated.");
            return;
        }

        Book book = library.getBook(bookName);
        if (book != null && book.isAvailable()) {
            book.deductQuantity();
            library.updateBookQuantity(book.getName(), book.getQuantity());
            messageLabel.setText("Book \"" + book.getName() + "\" borrowed successfully!");
        } else if (book != null) {
            messageLabel.setText("Sorry, \"" + book.getName() + "\" is currently out of stock.");
        } else {
            messageLabel.setText("Book not found in the library.");
        }
    }

    private void returnBook() {
        String bookName = bookField.getText();
        String userName = nameField.getText();
        String admissionNumber = admissionField.getText();
        String section = (String) sectionComboBox.getSelectedItem();

        if (userName.isEmpty() || admissionNumber.isEmpty() || bookName.isEmpty()) {
            messageLabel.setText("Please fill all the fields!");
            return;
        }

        if (!authenticateStudent(admissionNumber, section, userName)) {
            messageLabel.setText("Student not authenticated.");
            return;
        }

        Book book = library.getBook(bookName);
        if (book != null) {
            book.returnBook();
            library.updateBookQuantity(book.getName(), book.getQuantity());
            messageLabel.setText("Book \"" + book.getName() + "\" returned successfully!");
        } else {
            messageLabel.setText("Book not found in the library.");
        }
    }

    private void openUpdateBooksDialog() {
        String bookName = JOptionPane.showInputDialog("Enter book name:");
        String quantityString = JOptionPane.showInputDialog("Enter quantity:");
        try {
            int quantity = Integer.parseInt(quantityString);
            library.addBook(bookName, quantity);
            messageLabel.setText("Book added successfully!");
        } catch (NumberFormatException e) {
            messageLabel.setText("Invalid quantity input.");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            LibraryManagementSystemGUI gui = new LibraryManagementSystemGUI();
            gui.setVisible(true);
        });
    }
}
