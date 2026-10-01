import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class GMA {
    static Connection con;
    static DefaultTableModel model;

    public static void main(String[] args) {
        try {
            con = DriverManager.getConnection("jdbc:mysql://localhost:3306/gamedb", "root", "P3n(!1234");
        } catch (Exception e) {
            e.printStackTrace();
        }

        JFrame frame = new JFrame("Game Inventory Manager");
        frame.setLayout(new BorderLayout(10, 10));

        JPanel form = new JPanel(new GridLayout(2, 6, 5, 5));
        JTextField id = new JTextField();
        id.setEditable(false);
        JTextField title = new JTextField();
        JTextField genre = new JTextField();
        JTextField platform = new JTextField();
        JTextField price = new JTextField();
        JTextField rating = new JTextField();

        form.add(new JLabel("ID"));
        form.add(new JLabel("Title"));
        form.add(new JLabel("Genre"));
        form.add(new JLabel("Platform"));
        form.add(new JLabel("Price"));
        form.add(new JLabel("Rating"));
        form.add(id);
        form.add(title);
        form.add(genre);
        form.add(platform);
        form.add(price);
        form.add(rating);

        JButton insert = new JButton("Insert");
        JButton update = new JButton("Update");
        JButton delete = new JButton("Delete");
        JButton clear = new JButton("Clear");
        JPanel btns = new JPanel();
        btns.add(insert);
        btns.add(update);
        btns.add(delete);
        btns.add(clear);

        model = new DefaultTableModel(new Object[]{"ID", "Title", "Genre", "Platform", "Price", "Rating"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);

        JPanel top = new JPanel(new BorderLayout());
        top.add(form, BorderLayout.CENTER);
        top.add(btns, BorderLayout.SOUTH);
        frame.add(top, BorderLayout.NORTH);
        frame.add(new JScrollPane(table), BorderLayout.CENTER);
        frame.setSize(800, 500);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        loadTable();

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) {
                int row = table.getSelectedRow();
                id.setText(model.getValueAt(row, 0).toString());
                title.setText(model.getValueAt(row, 1).toString());
                genre.setText(model.getValueAt(row, 2).toString());
                platform.setText(model.getValueAt(row, 3).toString());
                price.setText(model.getValueAt(row, 4).toString());
                rating.setText(model.getValueAt(row, 5).toString());
            }
        });

        insert.addActionListener(e -> {
            try {
                double p = Double.parseDouble(price.getText());
                double r = Double.parseDouble(rating.getText());
                PreparedStatement pst = con.prepareStatement(
                        "Insert into games(title,genre,platform,price,rating) values(?,?,?,?,?)");
                pst.setString(1, title.getText());
                pst.setString(2, genre.getText());
                pst.setString(3, platform.getText());
                pst.setDouble(4, p);
                pst.setDouble(5, r);
                pst.executeUpdate();
                loadTable();
                clearForm(id, title, genre, platform, price, rating, table);
            } catch (Exception f) {
                JOptionPane.showMessageDialog(frame, "Error: " + f.getMessage());
            }
        });

        update.addActionListener(e -> {
            if (id.getText().isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Select a row to update.");
                return;
            }
            try {
                double p = Double.parseDouble(price.getText());
                double r = Double.parseDouble(rating.getText());
                PreparedStatement pst = con.prepareStatement(
                        "Update games set title=?, genre=?, platform=?, price=?, rating=? where id=?");
                pst.setString(1, title.getText());
                pst.setString(2, genre.getText());
                pst.setString(3, platform.getText());
                pst.setDouble(4, p);
                pst.setDouble(5, r);
                pst.setInt(6, Integer.parseInt(id.getText()));
                pst.executeUpdate();
                loadTable();
                clearForm(id, title, genre, platform, price, rating, table);
            } catch (Exception f) {
                JOptionPane.showMessageDialog(frame, "Error: " + f.getMessage());
            }
        });

        delete.addActionListener(e -> {
            if (id.getText().isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Select a row to delete.");
                return;
            }
            int confirm = JOptionPane.showConfirmDialog(frame, "Delete this game?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) return;
            try {
                PreparedStatement pst = con.prepareStatement("Delete from games where id=?");
                pst.setInt(1, Integer.parseInt(id.getText()));
                pst.executeUpdate();
                loadTable();
                clearForm(id, title, genre, platform, price, rating, table);
            } catch (Exception f) {
                JOptionPane.showMessageDialog(frame, "Error: " + f.getMessage());
            }
        });

        clear.addActionListener(e -> clearForm(id, title, genre, platform, price, rating, table));
    }

    static void loadTable() {
        model.setRowCount(0);
        try {
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("Select * from games");
            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("id"), rs.getString("title"), rs.getString("genre"),
                        rs.getString("platform"), rs.getDouble("price"), rs.getDouble("rating")
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static void clearForm(JTextField id, JTextField title, JTextField genre, JTextField platform,
                           JTextField price, JTextField rating, JTable table) {
        id.setText("");
        title.setText("");
        genre.setText("");
        platform.setText("");
        price.setText("");
        rating.setText("");
        table.clearSelection();
    }
}
