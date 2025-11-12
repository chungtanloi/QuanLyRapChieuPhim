package controllers;

import database.DBConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import models.SuatChieu;
import javafx.beans.property.SimpleStringProperty; // <<< KIỂM TRA DÒNG IMPORT QUAN TRỌNG NÀY

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class suat_chieuController {
    
    @FXML private TableView<SuatChieu> tblSuatChieu;
    @FXML private TableColumn<SuatChieu, Long> colMaSuat;
    @FXML private TableColumn<SuatChieu, String> colTenPhimSC;
    @FXML private TableColumn<SuatChieu, String> colPhongChieu;
    @FXML private TableColumn<SuatChieu, String> colNgayChieu;
    @FXML private TableColumn<SuatChieu, String> colGioChieu;
    @FXML private TableColumn<SuatChieu, String> colDinhDang;
    @FXML private TableColumn<SuatChieu, BigDecimal> colGiaVe;
    @FXML private TableColumn<SuatChieu, String> colTrangThaiSC;
    
    @FXML private DatePicker dpLichChieu;
    @FXML private ComboBox<String> cbPhimFilter;
    @FXML private Button btnThemSuatChieu, btnSuaSuatChieu, btnXoaSuatChieu;
    
    private ObservableList<SuatChieu> suatChieuList = FXCollections.observableArrayList();
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    
    @FXML
    private void initialize() {
        setupTableColumns();
        loadComboBoxData();
        setupEventHandlers();
        dpLichChieu.setValue(LocalDate.now());
        loadDanhSachSuatChieu();
    }
    
    private void setupTableColumns() {
        colMaSuat.setCellValueFactory(cellData -> cellData.getValue().maSuatChieuProperty().asObject());
        colTenPhimSC.setCellValueFactory(cellData -> cellData.getValue().tenPhimProperty());
        colPhongChieu.setCellValueFactory(cellData -> cellData.getValue().tenPhongProperty());
        colDinhDang.setCellValueFactory(cellData -> cellData.getValue().dinhDangProperty());
        colTrangThaiSC.setCellValueFactory(cellData -> cellData.getValue().trangThaiProperty());
        
        // Format cột Ngày chiếu (Sử dụng SimpleStringProperty đã import)
        colNgayChieu.setCellValueFactory(cellData -> {
            LocalDateTime batDauLuc = cellData.getValue().getBatDauLuc();
            return new SimpleStringProperty(
                batDauLuc != null ? batDauLuc.format(dateFormatter) : ""
            );
        });
        
        // Format cột Giờ chiếu (Sử dụng SimpleStringProperty đã import)
        colGioChieu.setCellValueFactory(cellData -> {
            LocalDateTime batDauLuc = cellData.getValue().getBatDauLuc();
            return new SimpleStringProperty(
                batDauLuc != null ? batDauLuc.format(timeFormatter) : ""
            );
        });

        // Thiết lập Cell Factory cho cột Giá
        colGiaVe.setCellFactory(column -> new TableCell<SuatChieu, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    // Định dạng tiền tệ VND
                    setText(String.format("%,.0f VNĐ", item));
                }
            }
        });
    }
    
    private void loadComboBoxData() {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT ten_phim FROM phim ORDER BY ten_phim");
             ResultSet rs = ps.executeQuery()) {
            
            ObservableList<String> phimList = FXCollections.observableArrayList("Tất cả phim");
            while (rs.next()) {
                phimList.add(rs.getString("ten_phim"));
            }
            cbPhimFilter.setItems(phimList);
            cbPhimFilter.getSelectionModel().selectFirst();
            
        } catch (SQLException e) {
            showError("Lỗi tải danh sách phim", e.getMessage());
        }
    }
    
    private void setupEventHandlers() {
        btnThemSuatChieu.setOnAction(e -> showThemSuaSuatChieuDialog(null));
        btnSuaSuatChieu.setOnAction(e -> {
            SuatChieu selected = tblSuatChieu.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert(Alert.AlertType.WARNING, "Vui lòng chọn suất chiếu cần sửa");
                return;
            }
            showThemSuaSuatChieuDialog(selected); 
        });
        btnXoaSuatChieu.setOnAction(e -> xoaSuatChieu());
        
        dpLichChieu.valueProperty().addListener((obs, oldV, newV) -> loadDanhSachSuatChieu());
        cbPhimFilter.valueProperty().addListener((obs, oldV, newV) -> loadDanhSachSuatChieu());
    }
    
    // Hàm tải dữ liệu chính
    private void loadDanhSachSuatChieu() {
        LocalDate ngayChieu = dpLichChieu.getValue();
        String phimFilter = cbPhimFilter.getValue();
        
        // SQL query sử dụng cú pháp multi-line cho dễ đọc (yêu cầu Java 15+)
        StringBuilder sql = new StringBuilder("""
            SELECT sc.ma_suat_chieu, p.ma_phim, p.ten_phim, ph.ma_phong, ph.ten_phong, 
                   sc.bat_dau_luc, dd.ten_dinh_dang, sc.gia_co_ban,
                   CASE 
                       WHEN DATE_ADD(sc.bat_dau_luc, INTERVAL p.thoi_luong_phut MINUTE) < NOW() THEN 'ĐÃ CHIẾU'
                       WHEN sc.bat_dau_luc > NOW() THEN 'SẮP CHIẾU'
                       ELSE 'ĐANG CHIẾU'
                   END as trang_thai
            FROM suat_chieu sc
            JOIN phim p ON sc.ma_phim = p.ma_phim
            JOIN phong ph ON sc.ma_phong = ph.ma_phong
            LEFT JOIN dinh_dang dd ON sc.ma_dinh_dang = dd.ma_dinh_dang
            WHERE 1=1
        """);
        
        if (ngayChieu != null) {
            sql.append(" AND DATE(sc.bat_dau_luc) = ?");
        }
        
        if (phimFilter != null && !phimFilter.isEmpty() && !phimFilter.equals("Tất cả phim")) {
            sql.append(" AND p.ten_phim = ?");
        }
        
        sql.append(" ORDER BY sc.bat_dau_luc ASC");
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            
            int paramIndex = 1;
            if (ngayChieu != null) {
                ps.setDate(paramIndex++, java.sql.Date.valueOf(ngayChieu));
            }
            
            if (phimFilter != null && !phimFilter.isEmpty() && !phimFilter.equals("Tất cả phim")) {
                ps.setString(paramIndex++, phimFilter);
            }
            
            ResultSet rs = ps.executeQuery();
            suatChieuList.clear();
            while (rs.next()) {
                SuatChieu sc = new SuatChieu(
                    rs.getLong("ma_suat_chieu"),
                    rs.getString("ten_phim"),
                    rs.getString("ten_phong"),
                    rs.getTimestamp("bat_dau_luc").toLocalDateTime(),
                    rs.getString("ten_dinh_dang"),
                    rs.getBigDecimal("gia_co_ban"),
                    rs.getString("trang_thai"),
                    rs.getLong("ma_phim"), 
                    rs.getInt("ma_phong")
                );
                suatChieuList.add(sc);
            }
            tblSuatChieu.setItems(suatChieuList);
            
        } catch (SQLException e) {
            showError("Lỗi tải suất chiếu", e.getMessage());
        }
    }
    
    // Hàm public được Controller Dialog gọi để tải lại dữ liệu
    public void loadSuatChieuTable() {
        loadDanhSachSuatChieu(); 
    }
    
    // Hàm mở Dialog Thêm/Sửa
    private void showThemSuaSuatChieuDialog(SuatChieu suatChieuToEdit) {
        try {
            // Đảm bảo đường dẫn FXML phải chính xác!
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/models/ThemSuaSuatChieuDialog.fxml"));
            Parent root = loader.load();
            
            ThemSuasuat_chieuController controller = loader.getController();

            Stage dialogStage = new Stage();
            dialogStage.setTitle(suatChieuToEdit == null ? "Thêm Suất Chiếu Mới" : "Sửa Suất Chiếu: " + suatChieuToEdit.getMaSuatChieu());
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            
            controller.setDialogStage(dialogStage);
            controller.setParentController(this);
            
            if (suatChieuToEdit != null) {
                controller.setSuatChieuToEdit(suatChieuToEdit);
            }

            Scene scene = new Scene(root);
            dialogStage.setScene(scene);
            dialogStage.showAndWait();
            
        } catch (Exception e) {
            showError("Lỗi Mở Cửa Sổ", "Không thể tải ThemSuaSuatChieuDialog.fxml. Lỗi: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Logic Xóa Suất Chiếu
    private void xoaSuatChieu() {
        SuatChieu selected = tblSuatChieu.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Vui lòng chọn suất chiếu cần xóa");
            return;
        }
        
        // Kiểm tra suất chiếu đã chiếu
        if (selected.getTrangThai().equals("ĐÃ CHIẾU")) {
             showAlert(Alert.AlertType.WARNING, "Không thể xóa suất chiếu đã chiếu.");
             return;
        }
        
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận xóa");
        confirm.setHeaderText("Xóa suất chiếu: " + selected.getMaSuatChieu() + " (" + selected.getTenPhim() + ")");
        confirm.setContentText("Bạn có chắc chắn muốn xóa suất chiếu này?");
        
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            String sql = "DELETE FROM suat_chieu WHERE ma_suat_chieu = ?";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                
                ps.setLong(1, selected.getMaSuatChieu());
                int rowsAffected = ps.executeUpdate();
                
                if (rowsAffected > 0) {
                    showAlert(Alert.AlertType.INFORMATION, "Xóa suất chiếu thành công!");
                    loadDanhSachSuatChieu(); // Reload danh sách
                } else {
                    showAlert(Alert.AlertType.ERROR, "Không tìm thấy hoặc không thể xóa suất chiếu.");
                }
            } catch (SQLException e) {
                 showError("Lỗi Xóa CSDL", "Không thể xóa suất chiếu. Chi tiết: " + e.getMessage());
            }
        }
    }
    
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}