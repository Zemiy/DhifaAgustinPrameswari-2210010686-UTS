/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package aplikasikeuangan;
import com.toedter.calendar.JDateChooser;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.io.*;
import java.util.*;
import javax.swing.*;
import org.json.JSONArray;
import org.json.JSONObject;

public class MainForm extends javax.swing.JFrame {
    private Connection connection;
    private DefaultTableModel tableModel;
    
    /**
     * Creates new form MainForm
     */
    public MainForm() {
    initComponents(); // Inisialisasi komponen GUI menggunakan NetBeans GUI Builder.

    // Inisialisasi tabel model dengan kolom ID, Nama Transaksi, Jumlah, Tanggal, Kategori.
    tableModel = new DefaultTableModel(new String[]{"ID", "Nama Transaksi", "Jumlah", "Tanggal", "Kategori"}, 0);

    // Hubungkan model ke JTable.
    transaksiTable.setModel(tableModel);

    // Menambahkan listener ke JTable untuk menangani seleksi baris.
    transaksiTable.getSelectionModel().addListSelectionListener(e -> {
        int selectedRow = transaksiTable.getSelectedRow();
        if (selectedRow != -1) { // Jika ada baris yang dipilih
            // Ambil data dari tabel dan tampilkan di JTextField/JComboBox
            String namaTransaksi = (String) tableModel.getValueAt(selectedRow, 1);
            double jumlah = (double) tableModel.getValueAt(selectedRow, 2);
            String tanggal = (String) tableModel.getValueAt(selectedRow, 3);
            String kategori = (String) tableModel.getValueAt(selectedRow, 4);

            // Tampilkan data pada komponen input
            namaField.setText(namaTransaksi);
            totalField.setText(String.valueOf(jumlah));
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            try {
                dateChooserTanggal.setDate(dateFormat.parse(tanggal));
            } catch (ParseException ex) {
                ex.printStackTrace();
            }
            kategoriComboBox.setSelectedItem(kategori);
        }
    });

    connectToDatabase(); // Hubungkan aplikasi ke database SQLite.
    loadTransactions(); // Muat semua transaksi dari database ke JTable.
}

private void connectToDatabase() {
    try {
        // Membuat koneksi ke SQLite dan membuat tabel jika belum ada.
        connection = DriverManager.getConnection("jdbc:sqlite:keuangan.db");
        Statement stmt = connection.createStatement();
        String createTableSQL = "CREATE TABLE IF NOT EXISTS tb_transaksi (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "nama_transaksi TEXT, " +
                "jumlah REAL, " +
                "tanggal TEXT, " +
                "kategori TEXT)";
        stmt.execute(createTableSQL);
        stmt.close();
        System.out.println("Database connected and table ready.");
    } catch (SQLException e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(this, "Gagal terhubung ke database.", "Error", JOptionPane.ERROR_MESSAGE);
    }
}

private void loadTransactions() {
    try {
        tableModel.setRowCount(0); // Kosongkan data tabel sebelum memuat ulang.
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM tb_transaksi");
        while (rs.next()) {
            // Tambahkan setiap baris dari database ke JTable.
            tableModel.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getString("nama_transaksi"),
                    rs.getDouble("jumlah"),
                    rs.getString("tanggal"),
                    rs.getString("kategori")
            });
        }
        rs.close();
        stmt.close();
    } catch (SQLException e) {
        e.printStackTrace();
    }
}

// Tambahkan transaksi baru ke database.
private void addTransaction() {
    String namaTransaksi = namaField.getText();
    String totalStr = totalField.getText();
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    String tanggal = dateFormat.format(dateChooserTanggal.getDate());
    String kategori = (String) kategoriComboBox.getSelectedItem();

    // Validasi input pengguna.
    if (namaTransaksi.isEmpty() || totalStr.isEmpty() || tanggal.isEmpty()) {
        JOptionPane.showMessageDialog(this, "Semua kolom harus diisi.", "Error", JOptionPane.ERROR_MESSAGE);
        return;
    }

    try {
        double total = Double.parseDouble(totalStr); // Konversi string ke angka.
        String insertSQL = "INSERT INTO tb_transaksi (nama_transaksi, jumlah, tanggal, kategori) VALUES (?, ?, ?, ?)";
        PreparedStatement pstmt = connection.prepareStatement(insertSQL);
        pstmt.setString(1, namaTransaksi);
        pstmt.setDouble(2, total);
        pstmt.setString(3, tanggal);
        pstmt.setString(4, kategori);

        pstmt.executeUpdate();
        pstmt.close();
        JOptionPane.showMessageDialog(this, "Transaksi berhasil ditambahkan.", "Success", JOptionPane.INFORMATION_MESSAGE);
        loadTransactions(); // Muat ulang data setelah penambahan.
    } catch (SQLException e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(this, "Gagal menambahkan transaksi.", "Error", JOptionPane.ERROR_MESSAGE);
    } catch (NumberFormatException e) {
        JOptionPane.showMessageDialog(this, "Total harus berupa angka.", "Error", JOptionPane.ERROR_MESSAGE);
    }
}

// Perbarui transaksi yang dipilih di database.
private void updateTransaction() {
    int selectedRow = transaksiTable.getSelectedRow();
    if (selectedRow == -1) {
        JOptionPane.showMessageDialog(this, "Pilih transaksi yang ingin diedit.", "Error", JOptionPane.ERROR_MESSAGE);
        return;
    }

    // Ambil data dari tabel dan komponen input.
    int id = (int) tableModel.getValueAt(selectedRow, 0);
    String namaTransaksi = namaField.getText();
    String totalStr = totalField.getText();
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    String tanggal = dateFormat.format(dateChooserTanggal.getDate());
    String kategori = (String) kategoriComboBox.getSelectedItem();

    if (namaTransaksi.isEmpty() || totalStr.isEmpty() || tanggal.isEmpty()) {
        JOptionPane.showMessageDialog(this, "Semua kolom harus diisi.", "Error", JOptionPane.ERROR_MESSAGE);
        return;
    }

    try {
        double total = Double.parseDouble(totalStr);
        String updateSQL = "UPDATE tb_transaksi SET nama_transaksi = ?, jumlah = ?, tanggal = ?, kategori = ? WHERE id = ?";
        PreparedStatement pstmt = connection.prepareStatement(updateSQL);
        pstmt.setString(1, namaTransaksi);
        pstmt.setDouble(2, total);
        pstmt.setString(3, tanggal);
        pstmt.setString(4, kategori);
        pstmt.setInt(5, id);

        pstmt.executeUpdate();
        pstmt.close();
        JOptionPane.showMessageDialog(this, "Transaksi berhasil diperbarui.", "Success", JOptionPane.INFORMATION_MESSAGE);
        loadTransactions();
    } catch (SQLException e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(this, "Gagal memperbarui transaksi.", "Error", JOptionPane.ERROR_MESSAGE);
    } catch (NumberFormatException e) {
        JOptionPane.showMessageDialog(this, "Total harus berupa angka.", "Error", JOptionPane.ERROR_MESSAGE);
    }
}

// Hapus transaksi yang dipilih dari database.
private void deleteTransaction() {
    int selectedRow = transaksiTable.getSelectedRow();
    if (selectedRow == -1) {
        JOptionPane.showMessageDialog(this, "Pilih transaksi yang ingin dihapus.", "Error", JOptionPane.ERROR_MESSAGE);
        return;
    }

    int id = (int) tableModel.getValueAt(selectedRow, 0);

    // Konfirmasi penghapusan.
    int confirm = JOptionPane.showConfirmDialog(this, "Apakah Anda yakin ingin menghapus transaksi ini?", "Konfirmasi", JOptionPane.YES_NO_OPTION);
    if (confirm == JOptionPane.YES_OPTION) {
        try {
            String deleteSQL = "DELETE FROM tb_transaksi WHERE id = ?";
            PreparedStatement pstmt = connection.prepareStatement(deleteSQL);
            pstmt.setInt(1, id);

            pstmt.executeUpdate();
            pstmt.close();
            JOptionPane.showMessageDialog(this, "Transaksi berhasil dihapus.", "Success", JOptionPane.INFORMATION_MESSAGE);
            loadTransactions();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Gagal menghapus transaksi.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

// Pencarian transaksi berdasarkan nama atau kategori.
private void searchTransaction(String query) {
    if (query.isEmpty()) {
        loadTransactions();
        return;
    }

    try {
        tableModel.setRowCount(0); // Kosongkan tabel sebelum menambahkan hasil pencarian.
        String searchSQL = "SELECT * FROM tb_transaksi WHERE nama_transaksi LIKE ? OR kategori LIKE ?";
        PreparedStatement pstmt = connection.prepareStatement(searchSQL);
        pstmt.setString(1, "%" + query + "%");
        pstmt.setString(2, "%" + query + "%");

        ResultSet rs = pstmt.executeQuery();
        while (rs.next()) {
            tableModel.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getString("nama_transaksi"),
                    rs.getDouble("jumlah"),
                    rs.getString("tanggal"),
                    rs.getString("kategori")
            });
        }
        rs.close();
        pstmt.close();
    } catch (SQLException e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(this, "Gagal mencari transaksi.", "Error", JOptionPane.ERROR_MESSAGE);
    }
}

// Fitur Ekspor dan Impor
private void exportToFile() {
    try {
        // Show a Save File Dialog
        JFileChooser fileChooser = new JFileChooser();
        int choice = fileChooser.showSaveDialog(this);
        if (choice == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();

            // Determine file format based on the file extension
            String fileName = file.getName().toLowerCase();
            if (fileName.endsWith(".txt")) {
                exportToTextFile(file);
            } else if (fileName.endsWith(".json")) {
                exportToJsonFile(file);
            } else {
                JOptionPane.showMessageDialog(this, "Unsupported file format. Use .txt or .json.");
            }
        }
    } catch (Exception e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(this, "Failed to export data.", "Error", JOptionPane.ERROR_MESSAGE);
    }
}
private void exportToTextFile(File file) throws IOException {
    BufferedWriter writer = new BufferedWriter(new FileWriter(file));
    for (int i = 0; i < tableModel.getRowCount(); i++) {
        StringBuilder row = new StringBuilder();
        for (int j = 0; j < tableModel.getColumnCount(); j++) {
            row.append(tableModel.getValueAt(i, j)).append("\t");
        }
        writer.write(row.toString().trim());
        writer.newLine();
    }
    writer.close();
    JOptionPane.showMessageDialog(this, "Data exported to TXT file successfully.");
}

private void exportToJsonFile(File file) throws IOException {
    JSONArray jsonArray = new JSONArray();
    for (int i = 0; i < tableModel.getRowCount(); i++) {
        JSONObject jsonObject = new JSONObject();
        for (int j = 0; j < tableModel.getColumnCount(); j++) {
            String columnName = tableModel.getColumnName(j);
            Object value = tableModel.getValueAt(i, j);
            jsonObject.put(columnName, value);
        }
        jsonArray.put(jsonObject);
    }

    BufferedWriter writer = new BufferedWriter(new FileWriter(file));
    writer.write(jsonArray.toString(4)); // Pretty print with 4 spaces indentation
    writer.close();
    JOptionPane.showMessageDialog(this, "Data exported to JSON file successfully.");
}

        private void importFromFile() {
            try {
                // Show an Open File Dialog
                JFileChooser fileChooser = new JFileChooser();
                int choice = fileChooser.showOpenDialog(this);
                if (choice == JFileChooser.APPROVE_OPTION) {
                    File file = fileChooser.getSelectedFile();

                    // Determine file format based on the file extension
                    String fileName = file.getName().toLowerCase();
                    if (fileName.endsWith(".txt")) {
                        importFromTextFile(file);
                    } else if (fileName.endsWith(".json")) {
                        importFromJsonFile(file);
                    } else {
                        JOptionPane.showMessageDialog(this, "Unsupported file format. Use .txt or .json.");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Failed to import data.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        private void importFromTextFile(File file) throws IOException {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            tableModel.setRowCount(0); // Clear existing rows
            while ((line = reader.readLine()) != null) {
                String[] rowData = line.split("\t");
                tableModel.addRow(rowData);
            }
            reader.close();
            JOptionPane.showMessageDialog(this, "Data imported from TXT file successfully.");
        }

        private void importFromJsonFile(File file) throws IOException {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            StringBuilder jsonContent = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonContent.append(line);
            }
            reader.close();

            JSONArray jsonArray = new JSONArray(jsonContent.toString());
            tableModel.setRowCount(0); // Clear existing rows
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                Object[] rowData = new Object[tableModel.getColumnCount()];
                for (int j = 0; j < tableModel.getColumnCount(); j++) {
                    String columnName = tableModel.getColumnName(j);
                    rowData[j] = jsonObject.get(columnName);
                }
                tableModel.addRow(rowData);
            }
            JOptionPane.showMessageDialog(this, "Data imported from JSON file successfully.");
        }


    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        cariField = new javax.swing.JTextField();
        exportBtn = new javax.swing.JButton();
        cariBtn = new javax.swing.JButton();
        importBtn = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        transaksiTable = new javax.swing.JTable();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        namaField = new javax.swing.JTextField();
        totalField = new javax.swing.JTextField();
        dateChooserTanggal = new com.toedter.calendar.JDateChooser();
        kategoriComboBox = new javax.swing.JComboBox<>();
        jPanel3 = new javax.swing.JPanel();
        simpanBtn = new javax.swing.JButton();
        editBtn = new javax.swing.JButton();
        hapusBtn = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(234, 214, 205));
        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createTitledBorder(""), "Aplikasi Pengelolaan Keuangan", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.TOP, new java.awt.Font("Segoe UI", 1, 18))); // NOI18N
        jPanel1.setForeground(new java.awt.Color(255, 255, 255));
        jPanel1.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        java.awt.GridBagLayout jPanel1Layout = new java.awt.GridBagLayout();
        jPanel1Layout.columnWidths = new int[] {0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0};
        jPanel1Layout.rowHeights = new int[] {0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0};
        jPanel1.setLayout(jPanel1Layout);

        jPanel2.setBackground(new java.awt.Color(241, 227, 227));
        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Data Transaksi"));
        jPanel2.setForeground(new java.awt.Color(222, 208, 235));
        jPanel2.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 181;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(23, 51, 0, 0);
        jPanel2.add(cariField, gridBagConstraints);

        exportBtn.setText("Export");
        exportBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportBtnActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(23, 18, 0, 0);
        jPanel2.add(exportBtn, gridBagConstraints);

        cariBtn.setText("Cari");
        cariBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cariBtnActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(23, 18, 0, 0);
        jPanel2.add(cariBtn, gridBagConstraints);

        importBtn.setText("Import");
        importBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                importBtnActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(23, 18, 0, 0);
        jPanel2.add(importBtn, gridBagConstraints);

        transaksiTable.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        transaksiTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null}
            },
            new String [] {
                "ID", "Nama Transaksi", "Total", "Tanggal", "Kategori"
            }
        ));
        jScrollPane1.setViewportView(transaksiTable);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 682;
        gridBagConstraints.ipady = 407;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 19, 5, 20);
        jPanel2.add(jScrollPane1, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.gridwidth = 11;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(30, 18, 30, 18);
        jPanel1.add(jPanel2, gridBagConstraints);

        jLabel2.setText("Nama Transaksi :");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 5);
        jPanel1.add(jLabel2, gridBagConstraints);

        jLabel3.setText("Total :");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 5);
        jPanel1.add(jLabel3, gridBagConstraints);

        jLabel4.setText("Tanggal  :");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 5);
        jPanel1.add(jLabel4, gridBagConstraints);

        jLabel5.setText("Kategori :");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 5);
        jPanel1.add(jLabel5, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(1, 3, 1, 1);
        jPanel1.add(namaField, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(1, 3, 1, 1);
        jPanel1.add(totalField, gridBagConstraints);

        dateChooserTanggal.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                dateChooserTanggalPropertyChange(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(1, 3, 1, 1);
        jPanel1.add(dateChooserTanggal, gridBagConstraints);

        kategoriComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Pilih Kategori", "Bills", "Transfer", "Transport", "Makanan", "Dan Lain Lain", " " }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(1, 3, 1, 1);
        jPanel1.add(kategoriComboBox, gridBagConstraints);

        jPanel3.setBackground(new java.awt.Color(232, 212, 204));
        jPanel3.setForeground(new java.awt.Color(204, 204, 255));
        jPanel3.setLayout(new java.awt.GridBagLayout());

        simpanBtn.setText("Simpan");
        simpanBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                simpanBtnActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(2, 46, 2, 7);
        jPanel3.add(simpanBtn, gridBagConstraints);

        editBtn.setText("Edit");
        editBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editBtnActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(2, 46, 2, 7);
        jPanel3.add(editBtn, gridBagConstraints);

        hapusBtn.setText("Hapus");
        hapusBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                hapusBtnActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(2, 46, 2, 7);
        jPanel3.add(hapusBtn, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 10;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridheight = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        jPanel1.add(jPanel3, gridBagConstraints);

        getContentPane().add(jPanel1, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void dateChooserTanggalPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_dateChooserTanggalPropertyChange

    }//GEN-LAST:event_dateChooserTanggalPropertyChange

    private void editBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editBtnActionPerformed
        editBtn.addActionListener(e -> updateTransaction());
    }//GEN-LAST:event_editBtnActionPerformed

    private void simpanBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_simpanBtnActionPerformed
        simpanBtn.addActionListener(e -> addTransaction());
    }//GEN-LAST:event_simpanBtnActionPerformed

    private void hapusBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hapusBtnActionPerformed
         hapusBtn.addActionListener(e -> deleteTransaction());
    }//GEN-LAST:event_hapusBtnActionPerformed

    private void cariBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cariBtnActionPerformed
         cariBtn.addActionListener(e -> searchTransaction(cariField.getText()));
    }//GEN-LAST:event_cariBtnActionPerformed

    private void exportBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportBtnActionPerformed
        // TODO add your handling code here:
        exportBtn.addActionListener(e -> exportToFile());
    }//GEN-LAST:event_exportBtnActionPerformed

    private void importBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_importBtnActionPerformed
        // TODO add your handling code here:
        importBtn.addActionListener(e -> importFromFile());
    }//GEN-LAST:event_importBtnActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(MainForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(MainForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(MainForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MainForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new MainForm().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cariBtn;
    private javax.swing.JTextField cariField;
    private com.toedter.calendar.JDateChooser dateChooserTanggal;
    private javax.swing.JButton editBtn;
    private javax.swing.JButton exportBtn;
    private javax.swing.JButton hapusBtn;
    private javax.swing.JButton importBtn;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JComboBox<String> kategoriComboBox;
    private javax.swing.JTextField namaField;
    private javax.swing.JButton simpanBtn;
    private javax.swing.JTextField totalField;
    private javax.swing.JTable transaksiTable;
    // End of variables declaration//GEN-END:variables
}
