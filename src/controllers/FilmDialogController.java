package controllers;

import controllers.FilmController.Film;
import database.DBConnection;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class FilmDialogController {

    @FXML private Label lblTitle;
    @FXML private TextField txtTenPhim;
    @FXML private TextField txtThoiLuong;
    @FXML private ComboBox<String> cmbPhanLoai;
    @FXML private DatePicker dpNgayPhatHanh;
    @FXML private VBox vboxTheLoai;
    @FXML private TextField txtPosterUrl;
    @FXML private TextArea txtMoTa;
    @FXML private Button btnSave;
    @FXML private Button btnCancel;

    private Mode mode;
    private Film currentFilm;
    private boolean saved = false;
    private List<CheckBox> genreCheckBoxes = new ArrayList<>();

    public enum Mode {
        ADD, EDIT
    }

    @FXML
    public void initialize() {
        setupPhanLoaiComboBox();
        loadTheLoaiCheckBoxes();
    }

    public void setMode(Mode mode) {
        this.mode = mode;
        if (mode == Mode.ADD) {
            lblTitle.setText("THÊM PHIM MỚI");
        } else {
            lblTitle.setText("SỬA THÔNG TIN PHIM");
        }
    }

    public void setFilm(Film film) {
        this.currentFilm = film;
        if (film != null) {
            txtTenPhim.setText(film.getTenPhim());
            txtThoiLuong.setText(String.valueOf(film.getThoiLuongPhut()));
            cmbPhanLoai.setValue(film.getPhanLoai());
            dpNgayPhatHanh.setValue(film.getNgayPhatHanh());
            txtPosterUrl.setText(film.getPosterUrl());
            txtMoTa.setText(film.getMoTa());
            
            // Load thể loại của phim
            loadFilmGenres(film.getMaPhim());
        }
    }

    private void setupPhanLoaiComboBox() {
        cmbPhanLoai.getItems().addAll("P", "T13", "T16", "T18", "C");
        cmbPhanLoai.setValue("P");
    }

    private void loadTheLoaiCheckBoxes() {
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT ma_the_loai, ten_the_loai FROM the_loai ORDER BY ten_the_loai")) {
            
            while (rs.next()) {
                CheckBox checkBox = new CheckBox(rs.getString("ten_the_loai"));
                checkBox.setUserData(rs.getInt("ma_the_loai"));
                genreCheckBoxes.add(checkBox);
                vboxTheLoai.getChildren().add(checkBox);
            }
        } catch (SQLException e) {
            showError("Lỗi khi tải thể loại", e.getMessage());
        }
    }

    private void loadFilmGenres(Long maPhim) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                 "SELECT ma_the_loai FROM phim_the_loai WHERE ma_phim = ?")) {
            
            pstmt.setLong(1, maPhim);
            ResultSet rs = pstmt.executeQuery();
            
            List<Integer> selectedGenres = new ArrayList<>();
            while (rs.next()) {
                selectedGenres.add(rs.getInt("ma_the_loai"));
            }
            
            // Đánh dấu các checkbox
            for (CheckBox checkBox : genreCheckBoxes) {
                Integer genreId = (Integer) checkBox.getUserData();
                checkBox.setSelected(selectedGenres.contains(genreId));
            }
            
        } catch (SQLException e) {
            showError("Lỗi khi tải thể loại phim", e.getMessage());
        }
    }

    @FXML
private void handleSave() {
    // Bước 1: Validate dữ liệu
    if (!validateInput()) {
        return;
    }
    
    Connection conn = null;
    
    try {
        // Bước 2: Kết nối database
        conn = DBConnection.getConnection();
        conn.setAutoCommit(false);
        
        // Bước 3: Kiểm tra Mode và thực hiện INSERT hoặc UPDATE
        if (mode == Mode.ADD) {
            System.out.println("→ Chế độ THÊM MỚI");
            insertFilm(conn);
        } else if (mode == Mode.EDIT) {
            System.out.println("→ Chế độ CÂP NHẬT");
            if (currentFilm == null) {
                throw new IllegalStateException("Không tìm thấy thông tin phim cần sửa!");
            }
            updateFilm(conn);
        }
        
        // Bước 4: Commit tất cả thay đổi
        conn.commit();
        System.out.println("✅ LƯU THÀNH CÔNG!");
        
        // Bước 5: Đánh dấu đã lưu và đóng dialog
        saved = true;
        
        
        closeDialog();
        
    } catch (SQLException e) {
        System.err.println("❌ Lỗi SQL: " + e.getMessage());
        e.printStackTrace();
        
        // Rollback nếu có lỗi
        if (conn != null) {
            try {
                conn.rollback();
                System.out.println("Đã rollback các thay đổi");
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
        
        showError("Lỗi khi lưu", "Không thể lưu phim!\nChi tiết: " + e.getMessage());
        
    } catch (NumberFormatException e) {
        showError("Lỗi dữ liệu", "Thời lượng phải là số nguyên!");
        
    } catch (Exception e) {
        System.err.println("❌ Lỗi: " + e.getMessage());
        e.printStackTrace();
        showError("Lỗi", "Có lỗi xảy ra: " + e.getMessage());
        
    } finally {
        // Bật lại auto-commit
        if (conn != null) {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}

// ===== PHƯƠNG THỨC INSERT - THÊM PHIM MỚI =====
private void insertFilm(Connection conn) throws SQLException {
    PreparedStatement pstmtPhim = null;
    PreparedStatement pstmtGenre = null;
    ResultSet generatedKeys = null;
    
    try {
        // Kiểm tra xem có cột poster_url không
        boolean hasPosterUrl = checkColumnExists(conn, "phim", "poster_url");
        
        // Tạo câu SQL INSERT
        String sqlInsert;
        if (hasPosterUrl) {
            sqlInsert = "INSERT INTO phim (ten_phim, thoi_luong_phut, phan_loai, ngay_phat_hanh, mo_ta, poster_url) " +
                       "VALUES (?, ?, ?, ?, ?, ?)";
        } else {
            sqlInsert = "INSERT INTO phim (ten_phim, thoi_luong_phut, phan_loai, ngay_phat_hanh, mo_ta) " +
                       "VALUES (?, ?, ?, ?, ?)";
        }
        
        pstmtPhim = conn.prepareStatement(sqlInsert, Statement.RETURN_GENERATED_KEYS);
        
        // Set các tham số
        pstmtPhim.setString(1, txtTenPhim.getText().trim());
        pstmtPhim.setInt(2, Integer.parseInt(txtThoiLuong.getText().trim()));
        pstmtPhim.setString(3, cmbPhanLoai.getValue());
        pstmtPhim.setDate(4, Date.valueOf(dpNgayPhatHanh.getValue()));
        
        // Xử lý mô tả
        String moTa = txtMoTa.getText();
        if (moTa != null && !moTa.trim().isEmpty()) {
            pstmtPhim.setString(5, moTa.trim());
        } else {
            pstmtPhim.setNull(5, java.sql.Types.VARCHAR);
        }
        
        // Xử lý poster URL nếu có
        if (hasPosterUrl) {
            String posterUrl = (txtPosterUrl != null && txtPosterUrl.getText() != null) 
                              ? txtPosterUrl.getText().trim() : "";
            if (!posterUrl.isEmpty()) {
                pstmtPhim.setString(6, posterUrl);
            } else {
                pstmtPhim.setNull(6, java.sql.Types.VARCHAR);
            }
        }
        
        // Thực thi INSERT
        int rowsInserted = pstmtPhim.executeUpdate();
        System.out.println("✓ Đã thêm " + rowsInserted + " phim mới");
        
        // Lấy ID phim vừa thêm
        generatedKeys = pstmtPhim.getGeneratedKeys();
        if (generatedKeys.next()) {
            long filmId = generatedKeys.getLong(1);
            System.out.println("✓ ID phim mới: " + filmId);
            
            // Thêm thể loại cho phim mới
            insertFilmGenres(conn, filmId);
        } else {
            throw new SQLException("Không lấy được ID phim vừa thêm!");
        }
        
    } finally {
        if (generatedKeys != null) generatedKeys.close();
        if (pstmtGenre != null) pstmtGenre.close();
        if (pstmtPhim != null) pstmtPhim.close();
    }
}

// ===== PHƯƠNG THỨC UPDATE - CẬP NHẬT PHIM =====
private void updateFilm(Connection conn) throws SQLException {
    PreparedStatement pstmtPhim = null;
    PreparedStatement pstmtDeleteGenre = null;
    
    try {
        // Kiểm tra xem có cột poster_url không
        boolean hasPosterUrl = checkColumnExists(conn, "phim", "poster_url");
        
        // Tạo câu SQL UPDATE
        String sqlUpdate;
        if (hasPosterUrl) {
            sqlUpdate = "UPDATE phim SET ten_phim = ?, thoi_luong_phut = ?, " +
                       "phan_loai = ?, ngay_phat_hanh = ?, mo_ta = ?, poster_url = ? " +
                       "WHERE ma_phim = ?";
        } else {
            sqlUpdate = "UPDATE phim SET ten_phim = ?, thoi_luong_phut = ?, " +
                       "phan_loai = ?, ngay_phat_hanh = ?, mo_ta = ? " +
                       "WHERE ma_phim = ?";
        }
        
        pstmtPhim = conn.prepareStatement(sqlUpdate);
        
        // Set các tham số
        pstmtPhim.setString(1, txtTenPhim.getText().trim());
        pstmtPhim.setInt(2, Integer.parseInt(txtThoiLuong.getText().trim()));
        pstmtPhim.setString(3, cmbPhanLoai.getValue());
        pstmtPhim.setDate(4, Date.valueOf(dpNgayPhatHanh.getValue()));
        
        // Xử lý mô tả
        String moTa = txtMoTa.getText();
        if (moTa != null && !moTa.trim().isEmpty()) {
            pstmtPhim.setString(5, moTa.trim());
        } else {
            pstmtPhim.setNull(5, java.sql.Types.VARCHAR);
        }
        
        // Xử lý poster URL và set ID phim
        if (hasPosterUrl) {
            String posterUrl = (txtPosterUrl != null && txtPosterUrl.getText() != null) 
                              ? txtPosterUrl.getText().trim() : "";
            if (!posterUrl.isEmpty()) {
                pstmtPhim.setString(6, posterUrl);
            } else {
                pstmtPhim.setNull(6, java.sql.Types.VARCHAR);
            }
            pstmtPhim.setLong(7, currentFilm.getMaPhim());
        } else {
            pstmtPhim.setLong(6, currentFilm.getMaPhim());
        }
        
        // Thực thi UPDATE
        int rowsUpdated = pstmtPhim.executeUpdate();
        
        if (rowsUpdated == 0) {
            throw new SQLException("Không thể cập nhật phim. Phim không tồn tại!");
        }
        
        System.out.println("✓ Đã cập nhật phim ID: " + currentFilm.getMaPhim());
        
        // Xóa tất cả thể loại cũ
        String sqlDeleteGenre = "DELETE FROM phim_the_loai WHERE ma_phim = ?";
        pstmtDeleteGenre = conn.prepareStatement(sqlDeleteGenre);
        pstmtDeleteGenre.setLong(1, currentFilm.getMaPhim());
        int deletedGenres = pstmtDeleteGenre.executeUpdate();
        System.out.println("✓ Đã xóa " + deletedGenres + " thể loại cũ");
        
        // Thêm thể loại mới
        insertFilmGenres(conn, currentFilm.getMaPhim());
        
    } finally {
        if (pstmtDeleteGenre != null) pstmtDeleteGenre.close();
        if (pstmtPhim != null) pstmtPhim.close();
    }
}

// ===== PHƯƠNG THỨC THÊM THỂ LOẠI CHO PHIM =====
private void insertFilmGenres(Connection conn, long filmId) throws SQLException {
    PreparedStatement pstmtGenre = null;
    
    try {
        String sqlInsertGenre = "INSERT INTO phim_the_loai (ma_phim, ma_the_loai) VALUES (?, ?)";
        pstmtGenre = conn.prepareStatement(sqlInsertGenre);
        
        int countGenres = 0;
        for (CheckBox checkBox : genreCheckBoxes) {
            if (checkBox.isSelected()) {
                Integer maTheLoai = (Integer) checkBox.getUserData();
                pstmtGenre.setLong(1, filmId);
                pstmtGenre.setInt(2, maTheLoai);
                pstmtGenre.executeUpdate();
                countGenres++;
            }
        }
        
        System.out.println("✓ Đã thêm " + countGenres + " thể loại");
        
    } finally {
        if (pstmtGenre != null) pstmtGenre.close();
    }
}

// Kiểm tra xem cột có tồn tại trong bảng không
private boolean checkColumnExists(Connection conn, String tableName, String columnName) {
    try {
        DatabaseMetaData metaData = conn.getMetaData();
        ResultSet columns = metaData.getColumns(null, null, tableName, columnName);
        boolean exists = columns.next();
        columns.close();
        return exists;
    } catch (SQLException e) {
        System.err.println("Không kiểm tra được cột: " + e.getMessage());
        return false;
    }
}

// ===== CÁC PHƯƠNG THỨC HỖ TRỢ =====

private boolean validateInput() {
    StringBuilder errors = new StringBuilder();

    // Kiểm tra tên phim
    if (txtTenPhim.getText() == null || txtTenPhim.getText().trim().isEmpty()) {
        errors.append("• Tên phim không được để trống\n");
    }

    // Kiểm tra thời lượng
    if (txtThoiLuong.getText() == null || txtThoiLuong.getText().trim().isEmpty()) {
        errors.append("• Thời lượng không được để trống\n");
    } else {
        try {
            int duration = Integer.parseInt(txtThoiLuong.getText().trim());
            if (duration <= 0) {
                errors.append("• Thời lượng phải lớn hơn 0\n");
            }
            if (duration > 500) {
                errors.append("• Thời lượng không hợp lý (quá 500 phút)\n");
            }
        } catch (NumberFormatException e) {
            errors.append("• Thời lượng phải là số nguyên\n");
        }
    }

    // Kiểm tra phân loại
    if (cmbPhanLoai.getValue() == null || cmbPhanLoai.getValue().isEmpty()) {
        errors.append("• Vui lòng chọn phân loại\n");
    }

    // Kiểm tra ngày phát hành
    if (dpNgayPhatHanh.getValue() == null) {
        errors.append("• Vui lòng chọn ngày phát hành\n");
    }

    // Kiểm tra ít nhất một thể loại
    boolean hasGenre = false;
    for (CheckBox checkBox : genreCheckBoxes) {
        if (checkBox.isSelected()) {
            hasGenre = true;
            break;
        }
    }
    
    if (!hasGenre) {
        errors.append("• Vui lòng chọn ít nhất một thể loại\n");
    }

    // Hiển thị lỗi nếu có
    if (errors.length() > 0) {
        showWarning("Dữ liệu không hợp lệ", errors.toString());
        return false;
    }

    return true;
}

private void closeDialog() {
    Stage stage = (Stage) btnSave.getScene().getWindow();
    stage.close();
}

private void showInfo(String title, String message) {
    Alert alert = new Alert(Alert.AlertType.INFORMATION);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(message);
    alert.showAndWait();
}

private void showWarning(String title, String message) {
    Alert alert = new Alert(Alert.AlertType.WARNING);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(message);
    alert.showAndWait();
}

private void showError(String title, String message) {
    Alert alert = new Alert(Alert.AlertType.ERROR);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(message);
    alert.showAndWait();
}

// Getter cho biến saved (dùng trong FilmController)
public boolean isSaved() {
    return saved;
}
@FXML
private void handleCancel() {
    closeDialog();
}
}