package controllers;

import database.DBConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import models.SuatChieu;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

public class ThemSuasuat_chieuController {

    // FXML elements
    @FXML private ComboBox<String> cbPhim;
    @FXML private ComboBox<String> cbPhong;
    @FXML private ComboBox<String> cbDinhDang;
    @FXML private DatePicker dpNgayChieu;
    @FXML private TextField txtGioBatDau;
    @FXML private TextField txtGiaCoBan;
    @FXML private Label lblThoiGianKetThuc;
    @FXML private Label lblXungDot;
    @FXML private Button btnLuu;
    @FXML private Label lblTieuDe;

    // Biến nội bộ
    private Stage dialogStage;
    private suat_chieuController parentController;
    private Map<String, Long> phimMap = new HashMap<>(); 
    private Map<String, Integer> phongMap = new HashMap<>(); 
    private Map<String, Integer> dinhDangMap = new HashMap<>(); 
    private SuatChieu suatChieuToEdit; 
    
    private Long selectedPhimId; 
    private Integer selectedPhongId;
    
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    @FXML
    private void initialize() {
        loadDialogComboBoxData();
        setupDialogListeners();
        dpNgayChieu.setValue(LocalDate.now()); 
        checkThoiGianXungDot(); 
    }
    
    public void setSuatChieuToEdit(SuatChieu suatChieu) {
        this.suatChieuToEdit = suatChieu;
        lblTieuDe.setText("SỬA SUẤT CHIẾU: " + suatChieu.getMaSuatChieu());
        btnLuu.setText("Cập Nhật Suất Chiếu");
        fillFields(suatChieu);
    }
    
    private void fillFields(SuatChieu sc) {
        cbPhim.getSelectionModel().select(sc.getTenPhim());
        cbPhong.getSelectionModel().select(sc.getTenPhong());
        cbDinhDang.getSelectionModel().select(sc.getDinhDang());
        dpNgayChieu.setValue(sc.getBatDauLuc().toLocalDate());
        txtGioBatDau.setText(sc.getBatDauLuc().format(TIME_FORMATTER));
        txtGiaCoBan.setText(String.valueOf(sc.getGiaCoBan()));
        
        selectedPhimId = sc.getMaPhim();
        selectedPhongId = sc.getMaPhong();
        
        updateKetThucLuc();
        checkThoiGianXungDot();
    }
    
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setParentController(suat_chieuController parentController) {
        this.parentController = parentController;
    }

    // ===============================================
    // LOAD DỮ LIỆU (ĐÃ SỬA LỖI GENERIC TYPE)
    // ===============================================

    private void loadDialogComboBoxData() {
        loadDataToMap("phim", "ten_phim", "ma_phim", cbPhim, phimMap);
        loadDataToMap("phong", "ten_phong", "ma_phong", cbPhong, phongMap);
        loadDataToMap("dinh_dang", "ten_dinh_dang", "ma_dinh_dang", cbDinhDang, dinhDangMap);
    }

    // ĐÃ SỬA SIGNATURE VÀ LOẠI BỎ 'instanceof'
    private void loadDataToMap(String tableName, String nameColumn, String idColumn, ComboBox<String> comboBox, Map<String, ?> map) {
         ObservableList<String> list = FXCollections.observableArrayList();
         
         // SỬ DỤNG TÊN CỘT ID ĐỂ XÁC ĐỊNH KIỂU DỮ LIỆU THAY VÌ 'instanceof'
         boolean isLongType = idColumn.equals("ma_phim"); 
         
         String sql = String.format("SELECT %s, %s FROM %s ORDER BY %s", idColumn, nameColumn, tableName, nameColumn); 
         try (Connection conn = DBConnection.getConnection();
              PreparedStatement ps = conn.prepareStatement(sql);
              ResultSet rs = ps.executeQuery()) {
            
             while (rs.next()) {
                 String name = rs.getString(nameColumn);
                 list.add(name);
                 
                 if (isLongType) {
                     // Ép kiểu an toàn (cast) nếu kiểu là Long
                     ((Map<String, Long>) map).put(name, rs.getLong(idColumn));
                 } else {
                     // Ép kiểu an toàn (cast) nếu kiểu là Integer 
                     ((Map<String, Integer>) map).put(name, rs.getInt(idColumn));
                 }
             }
             comboBox.setItems(list);
         } catch (SQLException e) {
             showError("Lỗi CSDL", "Không thể tải danh sách " + nameColumn + ". Chi tiết: " + e.getMessage());
             e.printStackTrace();
         }
    }
    
    // ===============================================
    // LOGIC CHECK XUNG ĐỘT
    // ===============================================

    private void setupDialogListeners() {
        cbPhim.valueProperty().addListener((obs, oldVal, newVal) -> {
            selectedPhimId = newVal != null ? phimMap.get(newVal) : null;
            updateKetThucLuc();
            checkThoiGianXungDot();
        });
        
        cbPhong.valueProperty().addListener((obs, oldVal, newVal) -> {
            selectedPhongId = newVal != null ? phongMap.get(newVal) : null;
            checkThoiGianXungDot();
        });
        
        dpNgayChieu.valueProperty().addListener((obs, oldVal, newVal) -> {
            updateKetThucLuc();
            checkThoiGianXungDot();
        });
        
        txtGioBatDau.textProperty().addListener((obs, oldVal, newVal) -> {
            updateKetThucLuc();
            checkThoiGianXungDot();
        });
        
        cbDinhDang.valueProperty().addListener((obs, oldVal, newVal) -> checkThoiGianXungDot());
        txtGiaCoBan.textProperty().addListener((obs, oldVal, newVal) -> checkThoiGianXungDot()); 
    }
    
    private void updateKetThucLuc() {
        lblThoiGianKetThuc.setText("Thời gian kết thúc (Tự động):");
        
        if (selectedPhimId == null || txtGioBatDau.getText().isEmpty() || dpNgayChieu.getValue() == null) {
             return;
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT thoi_luong_phut FROM phim WHERE ma_phim = ?")) {
            
            ps.setLong(1, selectedPhimId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int thoiLuong = rs.getInt("thoi_luong_phut");
                
                LocalTime gio = LocalTime.parse(txtGioBatDau.getText(), TIME_FORMATTER);
                LocalDateTime ketThuc = LocalDateTime.of(dpNgayChieu.getValue(), gio).plusMinutes(thoiLuong);
                
                lblThoiGianKetThuc.setText("Thời gian kết thúc (Tự động): " + 
                                            ketThuc.format(DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy")));
            }
        } catch (SQLException e) {
             lblThoiGianKetThuc.setText("Lỗi SQL khi lấy thời lượng phim.");
             e.printStackTrace();
        } catch (DateTimeParseException e) {
             lblThoiGianKetThuc.setText("Lỗi định dạng giờ (HH:mm).");
        }
    }
    
    private void checkThoiGianXungDot() {
        lblXungDot.setText("");
        btnLuu.setDisable(true);
        
        // 1. Kiểm tra đủ thông tin và ID
        if (selectedPhongId == null || dpNgayChieu.getValue() == null || 
            txtGioBatDau.getText().isEmpty() || selectedPhimId == null || 
            cbDinhDang.getValue() == null || txtGiaCoBan.getText().isEmpty()) {
            
            lblXungDot.setText("Vui lòng điền đủ thông tin.");
            lblXungDot.setStyle("-fx-text-fill: #9e9e9e;");
            return;
        }
        
        // 2. Validation Giờ và Giá
        try {
            LocalTime.parse(txtGioBatDau.getText(), TIME_FORMATTER);
            double gia = Double.parseDouble(txtGiaCoBan.getText());
            if (gia <= 0) throw new NumberFormatException();
        } catch (DateTimeParseException e) {
            lblXungDot.setText("LỖI: Định dạng Giờ bắt đầu phải là HH:mm.");
            lblXungDot.setStyle("-fx-text-fill: red;");
            return;
        } catch (NumberFormatException e) {
            lblXungDot.setText("LỖI: Giá cơ bản phải là số dương.");
            lblXungDot.setStyle("-fx-text-fill: red;");
            return;
        }

        // 3. Kiểm tra xung đột thời gian
        try {
            LocalTime gioBatDau = LocalTime.parse(txtGioBatDau.getText(), TIME_FORMATTER);
            LocalDateTime batDauMoi = LocalDateTime.of(dpNgayChieu.getValue(), gioBatDau);
            
            int thoiLuong = 0;
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement("SELECT thoi_luong_phut FROM phim WHERE ma_phim = ?")) {
                ps.setLong(1, selectedPhimId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) thoiLuong = rs.getInt("thoi_luong_phut");
            }
            if (thoiLuong == 0) throw new Exception("Không tìm thấy thời lượng phim.");
            
            LocalDateTime ketThucMoi = batDauMoi.plusMinutes(thoiLuong);
            
            String sql = """
                SELECT sc.ma_suat_chieu, pm.ten_phim, sc.bat_dau_luc,
                        DATE_ADD(sc.bat_dau_luc, INTERVAL pm.thoi_luong_phut MINUTE) as ket_thuc_luc
                FROM suat_chieu sc
                JOIN phim pm ON sc.ma_phim = pm.ma_phim
                WHERE sc.ma_phong = ? 
                AND sc.bat_dau_luc < ? AND DATE_ADD(sc.bat_dau_luc, INTERVAL pm.thoi_luong_phut MINUTE) > ?
            """;
            
            // Nếu đang sửa, loại trừ suất chiếu đang sửa ra khỏi danh sách kiểm tra
            if (suatChieuToEdit != null) {
                sql += " AND sc.ma_suat_chieu != " + suatChieuToEdit.getMaSuatChieu();
            }
            
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                
                ps.setInt(1, selectedPhongId);
                ps.setTimestamp(2, java.sql.Timestamp.valueOf(ketThucMoi));
                ps.setTimestamp(3, java.sql.Timestamp.valueOf(batDauMoi));
                
                ResultSet rs = ps.executeQuery();
                
                if (rs.next()) {
                    // CÓ XUNG ĐỘT
                    String tenPhimCu = rs.getString("ten_phim");
                    LocalDateTime batDauCu = rs.getTimestamp("bat_dau_luc").toLocalDateTime();
                    
                    lblXungDot.setText("LỖI: Xung đột với suất chiếu '" + tenPhimCu + 
                                            "' lúc " + batDauCu.format(TIME_FORMATTER) + "!");
                    lblXungDot.setStyle("-fx-text-fill: red;");
                    btnLuu.setDisable(true);
                    return;
                }
            }
            
            // KHÔNG CÓ XUNG ĐỘT
            lblXungDot.setText("Thời gian hợp lệ. Sẵn sàng lưu.");
            lblXungDot.setStyle("-fx-text-fill: green;");
            btnLuu.setDisable(false); 
            
        } catch (Exception e) {
            lblXungDot.setText("Lỗi kiểm tra xung đột: Lỗi CSDL hoặc dữ liệu không hợp lệ.");
            lblXungDot.setStyle("-fx-text-fill: red;");
            btnLuu.setDisable(true);
            e.printStackTrace();
        }
    }
    
    // ===============================================
    // LƯU VÀ HỦY
    // ===============================================

    @FXML
    private void handleSave() {
        if (btnLuu.isDisable()) {
            showError("Lỗi Lưu", "Vui lòng khắc phục lỗi xung đột hoặc điền đầy đủ thông tin hợp lệ.");
            return;
        }

        try {
            double giaCoBan = Double.parseDouble(txtGiaCoBan.getText());
            int maDinhDang = dinhDangMap.get(cbDinhDang.getValue());
            
            LocalTime gioBatDau = LocalTime.parse(txtGioBatDau.getText(), TIME_FORMATTER);
            LocalDateTime batDauLuc = LocalDateTime.of(dpNgayChieu.getValue(), gioBatDau);
            
            if (suatChieuToEdit == null) {
                // CHỨC NĂNG THÊM MỚI
                String sql = "INSERT INTO suat_chieu (ma_phim, ma_phong, ma_dinh_dang, bat_dau_luc, gia_co_ban, trang_thai) VALUES (?, ?, ?, ?, ?, 'SẮP CHIẾU')";
                
                try (Connection conn = DBConnection.getConnection();
                     PreparedStatement ps = conn.prepareStatement(sql)) {
                    
                    ps.setLong(1, selectedPhimId);
                    ps.setInt(2, selectedPhongId);
                    ps.setInt(3, maDinhDang);
                    ps.setTimestamp(4, java.sql.Timestamp.valueOf(batDauLuc));
                    ps.setDouble(5, giaCoBan); 
                    
                    ps.executeUpdate();
                    
                    showAlert(Alert.AlertType.INFORMATION, "Thêm suất chiếu thành công!");
                }
            } else {
                // CHỨC NĂNG SỬA
                String sql = "UPDATE suat_chieu SET ma_phim = ?, ma_phong = ?, ma_dinh_dang = ?, bat_dau_luc = ?, gia_co_ban = ? WHERE ma_suat_chieu = ?";
                
                try (Connection conn = DBConnection.getConnection();
                     PreparedStatement ps = conn.prepareStatement(sql)) {
                    
                    ps.setLong(1, selectedPhimId);
                    ps.setInt(2, selectedPhongId);
                    ps.setInt(3, maDinhDang);
                    ps.setTimestamp(4, java.sql.Timestamp.valueOf(batDauLuc));
                    ps.setDouble(5, giaCoBan); 
                    ps.setLong(6, suatChieuToEdit.getMaSuatChieu());
                    
                    ps.executeUpdate();
                    
                    showAlert(Alert.AlertType.INFORMATION, "Cập nhật suất chiếu thành công!");
                }
            }
            
            handleCancel(); 
            if (parentController != null) {
                parentController.loadSuatChieuTable(); 
            }

        } catch (Exception e) {
            showError("Lỗi Lưu CSDL", "Không thể lưu suất chiếu: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCancel() {
        if (dialogStage != null) {
            dialogStage.close();
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