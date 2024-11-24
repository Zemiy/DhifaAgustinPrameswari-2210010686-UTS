
# Aplikasi Keuangan Pribadi

Aplikasi ini adalah aplikasi manajemen keuangan berbasis desktop yang dibuat menggunakan Java Swing. Aplikasi ini memungkinkan pengguna untuk menambah, mengedit, menghapus, mencari, mengimpor, dan mengekspor data transaksi. Data transaksi disimpan dalam database SQLite dan dapat diekspor ke file dengan format JSON atau TXT.





## Fitur Utama

- **Tambah Transaksi** : Pengguna dapat menambahkan data transaksi baru, termasuk nama transaksi, jumlah, tanggal, dan kategori
- **Edit Transaksi** : Pengguna dapat mengedit data transaksi yang telah ada.
- **Hapus Transaksi** : Pengguna dapat menghapus data transaksi yang tidak diperlukan.
- **Cari Transaksi** :Pengguna dapat mencari transaksi berdasarkan nama transaksi atau kategori.
- **Ekspor Data** : Data transaksi dapat diekspor ke file dengan format JSON atau TXT untuk digunakan di luar aplikasi.
- **Impor Data** : Data transaksi dapat diimpor dari file dengan format JSON atau TXT ke dalam aplikasi.

## Teknologi yang Digunakan

- **Java Swing**: Untuk membuat antarmuka pengguna.
- **SQLite**: Untuk penyimpanan data lokal.
- **JDateChooser**: Komponen GUI untuk memilih tanggal.
- **org.json**: Library untuk membaca dan menulis file JSON.
## Struktur Tabel Data Base (SQLite)

| Kolom | Type     | Deskripsi                       |
| :-------- | :------- | :-------------------------------- |
| `id`      | `INTERGER` | **Primary Key**. Auto Increment |
| `nama_transaksi`      | `TEXT` | Nama Transaksi |
| `jumlah`      | `TEXT` | Jumlah Transaksi (angka) |
| `tanggal`      | `TEXT` | Tanggal transaksi (yyyy-MM-dd) |
| `kategori`      | `TEXT` | Kategori Transaksi |


## Koneksi Ke DataBase

```javascript
private void connectToDatabase() {
    try {
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

```
Membuka koneksi ke SQLite.

Membuat tabel tb_transaksi jika belum ada.
## Memuat Data dari Database


```javascript
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


```
Membaca data dari database dan menampilkannya di JTable.

## Tambah Transaksi


```javascript
private void addTransaction() {
    String namaTransaksi = namaField.getText();
    String totalStr = totalField.getText();
    String tanggal = new SimpleDateFormat("yyyy-MM-dd").format(dateChooserTanggal.getDate());
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

```
Data transaksi diambil dari komponen input dan divalidasi.

Data yang valid disimpan di database menggunakan PreparedStatement.

## Edit Transaksi

```javascript
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
    String tanggal = new SimpleDateFormat("yyyy-MM-dd").format(dateChooserTanggal.getDate());
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
```
Mengambil data dari input field dan memperbarui database berdasarkan ID transaksi yang dipilih.

## Hapus Transaksi

```javascript
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
```
Menghapus transaksi berdasarkan ID yang dipilih di tabel.

## Pencarian Transaksi
```javascript
private void searchTransaction(String query) {
    if (query.isEmpty())
```
## Eksport dan Inport
```javascript
private void exportToFile() {
    JFileChooser fileChooser = new JFileChooser();
    if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
        File file = fileChooser.getSelectedFile();
        try {
            if (file.getName().endsWith(".txt")) {
                exportToTextFile(file);
            } else if (file.getName().endsWith(".json")) {
                exportToJsonFile(file);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

private void importFromFile() {
    JFileChooser fileChooser = new JFileChooser();
    if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
        File file = fileChooser.getSelectedFile();
        try {
            if (file.getName().endsWith(".txt")) {
                importFromTextFile(file);
            } else if (file.getName().endsWith(".json")) {
                importFromJsonFile(file);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

```


