import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class GameManagerApp extends JFrame {

    private static final String DB_URL  = "jdbc:mysql://localhost:3306/gamedb";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "P3n(!1234";

    private JTextField id, title, genre, platform, price, rating;
    private JTable table;
    private DefaultTableModel model;
    private Connection conn;

    public GameManagerApp() {
        setTitle("Game Inventory Manager");
        setSize(800, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        connectDB();
        initUI();
        loadTable();
    }

    private void connectDB() {
        try {
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "DB connection failed:\n" + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    private void initUI() {
        JPanel formPanel = new JPanel(new GridLayout(2, 6, 5, 5));
        id = new JTextField();
        id.setEditable(false);
        title = new JTextField();
        genre = new JTextField();
        platform = new JTextField();
        price = new JTextField();
        rating = new JTextField();

        formPanel.add(new JLabel("ID"));
        formPanel.add(new JLabel("Title"));
        formPanel.add(new JLabel("Genre"));
        formPanel.add(new JLabel("Platform"));
        formPanel.add(new JLabel("Price"));
        formPanel.add(new JLabel("Rating"));
        formPanel.add(id);
        formPanel.add(title);
        formPanel.add(genre);
        formPanel.add(platform);
        formPanel.add(price);
        formPanel.add(rating);

        JButton insertBtn = new JButton("Insert");
        JButton updateBtn = new JButton("Update");
        JButton deleteBtn = new JButton("Delete");
        JButton clearBtn  = new JButton("Clear");

        insertBtn.addActionListener(e -> insertGame());
        updateBtn.addActionListener(e -> updateGame());
        deleteBtn.addActionListener(e -> deleteGame());
        clearBtn.addActionListener(e -> clearForm());

        JPanel btnPanel = new JPanel();
        btnPanel.add(insertBtn);
        btnPanel.add(updateBtn);
        btnPanel.add(deleteBtn);
        btnPanel.add(clearBtn);

        model = new DefaultTableModel(new Object[]{"ID", "Title", "Genre", "Platform", "Price", "Rating"}, 0) {
            public boolean isCellEditable(int row, int col) { return false; }
        };
        table = new JTable(model);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) fillFormFromRow();
        });

        JPanel top = new JPanel(new BorderLayout());
        top.add(formPanel, BorderLayout.CENTER);
        top.add(btnPanel, BorderLayout.SOUTH);

        setLayout(new BorderLayout(10, 10));
        add(top, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    private void fillFormFromRow() {
        int row = table.getSelectedRow();
        id.setText(model.getValueAt(row, 0).toString());
        title.setText(model.getValueAt(row, 1).toString());
        genre.setText(model.getValueAt(row, 2).toString());
        platform.setText(model.getValueAt(row, 3).toString());
        price.setText(model.getValueAt(row, 4).toString());
        rating.setText(model.getValueAt(row, 5).toString());
    }

    private void loadTable() {
        model.setRowCount(0);
        String sql = "SELECT * FROM games";
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("id"), rs.getString("title"), rs.getString("genre"),
                        rs.getString("platform"), rs.getDouble("price"), rs.getDouble("rating")
                });
            }
        } catch (SQLException e) {
            showError(e);
        }
    }

    private void insertGame() {
        String sql = "INSERT INTO games (title, genre, platform, price, rating) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, title.getText());
            ps.setString(2, genre.getText());
            ps.setString(3, platform.getText());
            ps.setDouble(4, Double.parseDouble(price.getText()));
            ps.setDouble(5, Double.parseDouble(rating.getText()));
            ps.executeUpdate();
            loadTable();
            clearForm();
        } catch (SQLException | NumberFormatException e) {
            showError(e);
        }
    }

    private void updateGame() {
        if (id.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Select a row to update.");
            return;
        }
        String sql = "UPDATE games SET title=?, genre=?, platform=?, price=?, rating=? WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, title.getText());
            ps.setString(2, genre.getText());
            ps.setString(3, platform.getText());
            ps.setDouble(4, Double.parseDouble(price.getText()));
            ps.setDouble(5, Double.parseDouble(rating.getText()));
            ps.setInt(6, Integer.parseInt(id.getText()));
            ps.executeUpdate();
            loadTable();
            clearForm();
        } catch (SQLException | NumberFormatException e) {
            showError(e);
        }
    }

    private void deleteGame() {
        if (id.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Select a row to delete.");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Delete this game?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        String sql = "DELETE FROM games WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, Integer.parseInt(id.getText()));
            ps.executeUpdate();
            loadTable();
            clearForm();
        } catch (SQLException e) {
            showError(e);
        }
    }

    private void clearForm() {
        id.setText("");
        title.setText("");
        genre.setText("");
        platform.setText("");
        price.setText("");
        rating.setText("");
        table.clearSelection();
    }

    private void showError(Exception e) {
        JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GameManagerApp().setVisible(true));
    }
}
