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
        if (!validateInput()) {
            return;
        }

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);
            
            System.out.println("Bắt đầu lưu phim...");
            System.out.println("Mode: " + mode);
            System.out.println("Tên phim: " + txtTenPhim.getText());
            
            if (mode == Mode.ADD) {
                insertFilm(conn);
            } else {
                System.out.println("Cập nhật phim ID: " + currentFilm.getMaPhim());
                updateFilm(conn);
            }
            
            conn.commit();
            System.out.println("✅ Lưu thành công!");
            saved = true;
            closeDialog();
            
        } catch (SQLException e) {
            System.err.println("❌ Lỗi SQL: " + e.getMessage());
            e.printStackTrace();
            
            if (conn != null) {
                try {
                    conn.rollback();
                    System.out.println("Đã rollback transaction");
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            
            showError("Lỗi khi lưu phim", "Chi tiết lỗi: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("❌ Lỗi khác: " + e.getMessage());
            e.printStackTrace();
            showError("Lỗi", "Có lỗi xảy ra: " + e.getMessage());
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void insertFilm(Connection conn) throws SQLException {
        String sql = "INSERT INTO phim (ten_phim, thoi_luong_phut, phan_loai, ngay_phat_hanh, mo_ta, poster_url) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, txtTenPhim.getText().trim());
            pstmt.setInt(2, Integer.parseInt(txtThoiLuong.getText().trim()));
            pstmt.setString(3, cmbPhanLoai.getValue());
            pstmt.setDate(4, Date.valueOf(dpNgayPhatHanh.getValue()));
            
            // Xử lý mô tả - có thể null
            String moTa = txtMoTa.getText() != null ? txtMoTa.getText().trim() : "";
            pstmt.setString(5, moTa.isEmpty() ? null : moTa);
            
            // Xử lý poster URL - có thể null
            String posterUrl = txtPosterUrl.getText() != null ? txtPosterUrl.getText().trim() : "";
            pstmt.setString(6, posterUrl.isEmpty() ? null : posterUrl);
            
            int rowsAffected = pstmt.executeUpdate();
            System.out.println("Đã thêm " + rowsAffected + " phim mới");
            
            // Lấy ID phim vừa thêm
            ResultSet generatedKeys = pstmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                long filmId = generatedKeys.getLong(1);
                System.out.println("ID phim mới: " + filmId);
                saveFilmGenres(conn, filmId);
            }
        }
    }

    private void updateFilm(Connection conn) throws SQLException {
        String sql = "UPDATE phim SET ten_phim = ?, thoi_luong_phut = ?, phan_loai = ?, " +
                     "ngay_phat_hanh = ?, mo_ta = ?, poster_url = ? WHERE ma_phim = ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, txtTenPhim.getText().trim());
            pstmt.setInt(2, Integer.parseInt(txtThoiLuong.getText().trim()));
            pstmt.setString(3, cmbPhanLoai.getValue());
            pstmt.setDate(4, Date.valueOf(dpNgayPhatHanh.getValue()));
            
            // Xử lý mô tả - có thể null
            String moTa = txtMoTa.getText() != null ? txtMoTa.getText().trim() : "";
            pstmt.setString(5, moTa.isEmpty() ? null : moTa);
            
            // Xử lý poster URL - có thể null
            String posterUrl = txtPosterUrl.getText() != null ? txtPosterUrl.getText().trim() : "";
            pstmt.setString(6, posterUrl.isEmpty() ? null : posterUrl);
            
            pstmt.setLong(7, currentFilm.getMaPhim());
            
            int rowsAffected = pstmt.executeUpdate();
            System.out.println("Đã cập nhật " + rowsAffected + " dòng trong bảng phim");
            
            // Xóa thể loại cũ và thêm mới
            try (PreparedStatement deletePstmt = conn.prepareStatement(
                    "DELETE FROM phim_the_loai WHERE ma_phim = ?")) {
                deletePstmt.setLong(1, currentFilm.getMaPhim());
                int deletedRows = deletePstmt.executeUpdate();
                System.out.println("Đã xóa " + deletedRows + " thể loại cũ");
            }
            
            saveFilmGenres(conn, currentFilm.getMaPhim());
        }
    }

    private void saveFilmGenres(Connection conn, long filmId) throws SQLException {
        String sql = "INSERT INTO phim_the_loai (ma_phim, ma_the_loai) VALUES (?, ?)";
        
        int genreCount = 0;
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (CheckBox checkBox : genreCheckBoxes) {
                if (checkBox.isSelected()) {
                    pstmt.setLong(1, filmId);
                    pstmt.setInt(2, (Integer) checkBox.getUserData());
                    pstmt.addBatch();
                    genreCount++;
                }
            }
            
            if (genreCount > 0) {
                int[] results = pstmt.executeBatch();
                System.out.println("Đã thêm " + results.length + " thể loại cho phim");
            } else {
                System.out.println("Không có thể loại nào được chọn");
            }
        }
    }

    private boolean validateInput() {
        StringBuilder errors = new StringBuilder();

        if (txtTenPhim.getText().trim().isEmpty()) {
            errors.append("- Tên phim không được để trống\n");
        }

        if (txtThoiLuong.getText().trim().isEmpty()) {
            errors.append("- Thời lượng không được để trống\n");
        } else {
            try {
                int duration = Integer.parseInt(txtThoiLuong.getText().trim());
                if (duration <= 0) {
                    errors.append("- Thời lượng phải lớn hơn 0\n");
                }
            } catch (NumberFormatException e) {
                errors.append("- Thời lượng phải là số nguyên\n");
            }
        }

        if (cmbPhanLoai.getValue() == null) {
            errors.append("- Vui lòng chọn phân loại\n");
        }

        if (dpNgayPhatHanh.getValue() == null) {
            errors.append("- Vui lòng chọn ngày phát hành\n");
        }

        boolean hasGenre = false;
        for (CheckBox checkBox : genreCheckBoxes) {
            if (checkBox.isSelected()) {
                hasGenre = true;
                break;
            }
        }
        if (!hasGenre) {
            errors.append("- Vui lòng chọn ít nhất một thể loại\n");
        }

        if (errors.length() > 0) {
            showWarning("Dữ liệu không hợp lệ", errors.toString());
            return false;
        }

        return true;
    }

    @FXML
    private void handleCancel() {
        closeDialog();
    }

    private void closeDialog() {
        Stage stage = (Stage) btnCancel.getScene().getWindow();
        stage.close();
    }

    public boolean isSaved() {
        return saved;
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
}