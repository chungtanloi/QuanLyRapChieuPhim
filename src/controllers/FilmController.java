package controllers;

import database.DBConnection;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class FilmController {

    @FXML private VBox filmRoot;
    @FXML private TextField txtSearchName;
    @FXML private ComboBox<String> cmbGenre;
    @FXML private ComboBox<String> cmbRating;
    @FXML private DatePicker dpFromDate;
    @FXML private DatePicker dpToDate;
    @FXML private CheckBox chkShowNow;
    @FXML private CheckBox chkUpcoming;
    
    @FXML private TableView<Film> tblFilm;
    @FXML private TableColumn<Film, String> colId;
    @FXML private TableColumn<Film, String> colName;
    @FXML private TableColumn<Film, String> colDuration;
    @FXML private TableColumn<Film, String> colGenre;
    @FXML private TableColumn<Film, String> colRating;
    @FXML private TableColumn<Film, String> colReleaseDate;
    @FXML private TableColumn<Film, String> colStatus;
    
    @FXML private Button btnAddFilm;
    @FXML private Button btnEdit;
    @FXML private Button btnDelete;
    @FXML private Button btnSearch;
    @FXML private Button btnRefresh;

    private ObservableList<Film> filmList = FXCollections.observableArrayList();
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML
    public void initialize() {
        setupTableColumns();
        loadComboBoxData();
        loadFilmData();
        setupTableSelection();
    }

    private void setupTableColumns() {
        colId.setCellValueFactory(cellData -> 
            new SimpleStringProperty(String.valueOf(cellData.getValue().getMaPhim())));
        colName.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getTenPhim()));
        colDuration.setCellValueFactory(cellData -> 
            new SimpleStringProperty(String.valueOf(cellData.getValue().getThoiLuongPhut())));
        colGenre.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getTheLoai()));
        colRating.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getPhanLoai()));
        colReleaseDate.setCellValueFactory(cellData -> {
            LocalDate date = cellData.getValue().getNgayPhatHanh();
            return new SimpleStringProperty(date != null ? date.format(dateFormatter) : "");
        });
        colStatus.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getTrangThai()));
        
        tblFilm.setItems(filmList);
    }

    private void setupTableSelection() {
        tblFilm.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            boolean isSelected = newSelection != null;
            btnEdit.setDisable(!isSelected);
            btnDelete.setDisable(!isSelected);
        });
    }

    private void loadComboBoxData() {
        // Load Thể loại
        ObservableList<String> genres = FXCollections.observableArrayList();
        genres.add("Tất cả");
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT ten_the_loai FROM the_loai ORDER BY ten_the_loai")) {
            while (rs.next()) {
                genres.add(rs.getString("ten_the_loai"));
            }
        } catch (SQLException e) {
            showError("Lỗi khi tải thể loại", e.getMessage());
        }
        cmbGenre.setItems(genres);
        cmbGenre.getSelectionModel().selectFirst();

        // Load Phân loại
        ObservableList<String> ratings = FXCollections.observableArrayList();
        ratings.addAll("Tất cả", "P", "T13", "T16", "T18", "C");
        cmbRating.setItems(ratings);
        cmbRating.getSelectionModel().selectFirst();
    }

    private void loadFilmData() {
        filmList.clear();
        
        StringBuilder query = new StringBuilder(
            "SELECT p.ma_phim, p.ten_phim, p.thoi_luong_phut, p.phan_loai, p.ngay_phat_hanh, " +
            "GROUP_CONCAT(DISTINCT tl.ten_the_loai SEPARATOR ', ') as the_loai " +
            "FROM phim p " +
            "LEFT JOIN phim_the_loai ptl ON p.ma_phim = ptl.ma_phim " +
            "LEFT JOIN the_loai tl ON ptl.ma_the_loai = tl.ma_the_loai " +
            "WHERE 1=1 "
        );

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query.toString() + "GROUP BY p.ma_phim ORDER BY p.ma_phim DESC")) {
            
            LocalDate today = LocalDate.now();
            
            while (rs.next()) {
                Film film = new Film();
                film.setMaPhim(rs.getLong("ma_phim"));
                film.setTenPhim(rs.getString("ten_phim"));
                film.setThoiLuongPhut(rs.getInt("thoi_luong_phut"));
                film.setPhanLoai(rs.getString("phan_loai"));
                
                Date sqlDate = rs.getDate("ngay_phat_hanh");
                if (sqlDate != null) {
                    film.setNgayPhatHanh(sqlDate.toLocalDate());
                }
                
                film.setTheLoai(rs.getString("the_loai"));
                
                // Xác định trạng thái
                if (film.getNgayPhatHanh() != null) {
                    if (film.getNgayPhatHanh().isAfter(today)) {
                        film.setTrangThai("Sắp chiếu");
                    } else {
                        film.setTrangThai("Đang chiếu");
                    }
                } else {
                    film.setTrangThai("Chưa xác định");
                }
                
                filmList.add(film);
            }
        } catch (SQLException e) {
            showError("Lỗi khi tải dữ liệu phim", e.getMessage());
        }
    }

    @FXML
    private void handleSearch() {
        filmList.clear();
        
        StringBuilder query = new StringBuilder(
            "SELECT p.ma_phim, p.ten_phim, p.thoi_luong_phut, p.phan_loai, p.ngay_phat_hanh, " +
            "GROUP_CONCAT(DISTINCT tl.ten_the_loai SEPARATOR ', ') as the_loai " +
            "FROM phim p " +
            "LEFT JOIN phim_the_loai ptl ON p.ma_phim = ptl.ma_phim " +
            "LEFT JOIN the_loai tl ON ptl.ma_the_loai = tl.ma_the_loai " +
            "WHERE 1=1 "
        );

        String searchName = txtSearchName.getText().trim();
        if (!searchName.isEmpty()) {
            query.append("AND p.ten_phim LIKE '%").append(searchName).append("%' ");
        }

        String selectedGenre = cmbGenre.getValue();
        if (selectedGenre != null && !selectedGenre.equals("Tất cả")) {
            query.append("AND tl.ten_the_loai = '").append(selectedGenre).append("' ");
        }

        String selectedRating = cmbRating.getValue();
        if (selectedRating != null && !selectedRating.equals("Tất cả")) {
            query.append("AND p.phan_loai = '").append(selectedRating).append("' ");
        }

        LocalDate fromDate = dpFromDate.getValue();
        if (fromDate != null) {
            query.append("AND p.ngay_phat_hanh >= '").append(fromDate).append("' ");
        }

        LocalDate toDate = dpToDate.getValue();
        if (toDate != null) {
            query.append("AND p.ngay_phat_hanh <= '").append(toDate).append("' ");
        }

        query.append("GROUP BY p.ma_phim ORDER BY p.ma_phim DESC");

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query.toString())) {
            
            LocalDate today = LocalDate.now();
            
            while (rs.next()) {
                Film film = new Film();
                film.setMaPhim(rs.getLong("ma_phim"));
                film.setTenPhim(rs.getString("ten_phim"));
                film.setThoiLuongPhut(rs.getInt("thoi_luong_phut"));
                film.setPhanLoai(rs.getString("phan_loai"));
                
                Date sqlDate = rs.getDate("ngay_phat_hanh");
                if (sqlDate != null) {
                    film.setNgayPhatHanh(sqlDate.toLocalDate());
                }
                
                film.setTheLoai(rs.getString("the_loai"));
                
                // Xác định trạng thái
                if (film.getNgayPhatHanh() != null) {
                    if (film.getNgayPhatHanh().isAfter(today)) {
                        film.setTrangThai("Sắp chiếu");
                    } else {
                        film.setTrangThai("Đang chiếu");
                    }
                } else {
                    film.setTrangThai("Chưa xác định");
                }
                
                // Lọc theo trạng thái checkbox
                boolean showThis = false;
                if (chkShowNow.isSelected() && film.getTrangThai().equals("Đang chiếu")) {
                    showThis = true;
                }
                if (chkUpcoming.isSelected() && film.getTrangThai().equals("Sắp chiếu")) {
                    showThis = true;
                }
                
                if (showThis || (!chkShowNow.isSelected() && !chkUpcoming.isSelected())) {
                    filmList.add(film);
                }
            }
        } catch (SQLException e) {
            showError("Lỗi khi tìm kiếm", e.getMessage());
        }
    }

    @FXML
    private void handleAddFilm() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/models/film_dialog.fxml"));
            Parent root = loader.load();
            
            FilmDialogController controller = loader.getController();
            controller.setMode(FilmDialogController.Mode.ADD);
            
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Thêm Phim Mới");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(btnAddFilm.getScene().getWindow());
            dialogStage.setScene(new Scene(root));
            
            dialogStage.showAndWait();
            
            if (controller.isSaved()) {
                loadFilmData();
                showInfo("Thành công", "Đã thêm phim mới thành công!");
            }
        } catch (Exception e) {
            showError("Lỗi", "Không thể mở form thêm phim: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleEdit() {
        Film selected = tblFilm.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Chưa chọn phim", "Vui lòng chọn phim cần sửa!");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/models/film_dialog.fxml"));
            Parent root = loader.load();
            
            FilmDialogController controller = loader.getController();
            controller.setMode(FilmDialogController.Mode.EDIT);
            controller.setFilm(selected);
            
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Sửa Thông Tin Phim");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(btnEdit.getScene().getWindow());
            dialogStage.setScene(new Scene(root));
            
            dialogStage.showAndWait();
            
            if (controller.isSaved()) {
                loadFilmData();
                showInfo("Thành công", "Đã cập nhật thông tin phim thành công!");
            }
        } catch (Exception e) {
            showError("Lỗi", "Không thể mở form sửa phim: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleDelete() {
        Film selected = tblFilm.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Chưa chọn phim", "Vui lòng chọn phim cần xóa!");
            return;
        }

        Optional<ButtonType> result = showConfirmation(
            "Xác nhận xóa",
            "Bạn có chắc chắn muốn xóa phim \"" + selected.getTenPhim() + "\" không?"
        );

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement("DELETE FROM phim WHERE ma_phim = ?")) {
                
                pstmt.setLong(1, selected.getMaPhim());
                pstmt.executeUpdate();
                
                loadFilmData();
                showInfo("Thành công", "Đã xóa phim thành công!");
                
            } catch (SQLException e) {
                showError("Lỗi khi xóa phim", e.getMessage());
            }
        }
    }

    @FXML
    private void handleRefresh() {
        txtSearchName.clear();
        cmbGenre.getSelectionModel().selectFirst();
        cmbRating.getSelectionModel().selectFirst();
        dpFromDate.setValue(null);
        dpToDate.setValue(null);
        chkShowNow.setSelected(true);
        chkUpcoming.setSelected(true);
        loadFilmData();
    }

    // Utility methods for showing alerts
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

    private Optional<ButtonType> showConfirmation(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        return alert.showAndWait();
    }

    // Inner class for Film model
    public static class Film {
        private Long maPhim;
        private String tenPhim;
        private Integer thoiLuongPhut;
        private String phanLoai;
        private LocalDate ngayPhatHanh;
        private String theLoai;
        private String trangThai;
        private String moTa;
        private String posterUrl;

        // Getters and Setters
        public Long getMaPhim() { return maPhim; }
        public void setMaPhim(Long maPhim) { this.maPhim = maPhim; }

        public String getTenPhim() { return tenPhim; }
        public void setTenPhim(String tenPhim) { this.tenPhim = tenPhim; }

        public Integer getThoiLuongPhut() { return thoiLuongPhut; }
        public void setThoiLuongPhut(Integer thoiLuongPhut) { this.thoiLuongPhut = thoiLuongPhut; }

        public String getPhanLoai() { return phanLoai; }
        public void setPhanLoai(String phanLoai) { this.phanLoai = phanLoai; }

        public LocalDate getNgayPhatHanh() { return ngayPhatHanh; }
        public void setNgayPhatHanh(LocalDate ngayPhatHanh) { this.ngayPhatHanh = ngayPhatHanh; }

        public String getTheLoai() { return theLoai; }
        public void setTheLoai(String theLoai) { this.theLoai = theLoai; }

        public String getTrangThai() { return trangThai; }
        public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

        public String getMoTa() { return moTa; }
        public void setMoTa(String moTa) { this.moTa = moTa; }

        public String getPosterUrl() { return posterUrl; }
        public void setPosterUrl(String posterUrl) { this.posterUrl = posterUrl; }
    }
}