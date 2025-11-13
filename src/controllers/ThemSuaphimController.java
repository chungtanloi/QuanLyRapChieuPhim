package controllers;

import database.DBConnection;
import models.film;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ThemSuaphimController { // LƯU Ý: TÊN CLASS LÀ ThemSuaphimController (chữ 's' thường)

    // --- FXML FIELDS ---
    @FXML private Label lblTitle;
    @FXML private TextField txtTenPhim;
    @FXML private TextField txtThoiLuong;
    @FXML private ComboBox<String> cbPhanLoai;
    @FXML private DatePicker dpNgayPhatHanh;
    @FXML private TextArea txtMoTa;
    @FXML private VBox vboxTheLoai; 

    // --- BIẾN NỘI BỘ ---
    private film phimToEdit;
    private PhimController parentController;
    private List<CheckBox> theLoaiCheckBoxes = new ArrayList<>();
    
    private static final ObservableList<String> PHAN_LOAI_LIST = FXCollections.observableArrayList(
        "P", "T13", "T16", "T18"
    );

    @FXML
    public void initialize() {
        cbPhanLoai.setItems(PHAN_LOAI_LIST);
        loadTheLoaiFromDatabase();
    }

    // ======================================================================
    // SETTER VÀ INITIALIZATION
    // ======================================================================
    
    public void setParentController(PhimController parentController) {
        this.parentController = parentController;
    }

    /**
     * Nạp dữ liệu film vào form khi ở chế độ Sửa.
     */
    public void setPhimData(film phim) {
        this.phimToEdit = phim;
        lblTitle.setText("SỬA THÔNG TIN PHIM: " + phim.getTenPhim());
        
        // Nạp dữ liệu cơ bản
        txtTenPhim.setText(phim.getTenPhim());
        txtThoiLuong.setText(String.valueOf(phim.getThoiLuongPhut()));
        
        // 🚨 FIX: Nạp Ngày Phát Hành gốc và Mô tả từ CSDL
        loadOriginalPhimData(phim.getMaPhim()); 
        
        cbPhanLoai.getSelectionModel().select(phim.getPhanLoai());
        
        loadSelectedTheLoai(phim.getMaPhim()); // Nạp thể loại đã chọn
    }
    
    /**
     * Truy vấn CSDL để lấy Ngày Phát Hành (Date) và Mô tả chi tiết.
     */
    private void loadOriginalPhimData(long maPhim) {
        String sql = "SELECT ngay_phat_hanh, mo_ta FROM phim WHERE ma_phim = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setLong(1, maPhim);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Date sqlDate = rs.getDate("ngay_phat_hanh");
                    if (sqlDate != null) {
                        dpNgayPhatHanh.setValue(sqlDate.toLocalDate());
                    }
                    // Nếu 'mo_ta' là NULL trong DB, rs.getString() sẽ trả về null, nên cần kiểm tra.
                    String moTa = rs.getString("mo_ta");
                    txtMoTa.setText(moTa != null ? moTa : ""); 
                }
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi Nạp Dữ Liệu", "Không thể nạp Ngày Phát Hành và Mô tả gốc.");
            e.printStackTrace();
        }
    }


    // ======================================================================
    // LOGIC THỂ LOẠI (TẠO ĐỘNG VÀ XỬ LÝ)
    // ======================================================================

    private void loadTheLoaiFromDatabase() {
        String sql = "SELECT ma_the_loai, ten_the_loai FROM the_loai ORDER BY ten_the_loai";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                long maTheLoai = rs.getLong("ma_the_loai");
                String tenTheLoai = rs.getString("ten_the_loai");
                
                CheckBox cb = new CheckBox(tenTheLoai);
                cb.setUserData(maTheLoai); // Lưu ID vào UserData
                
                theLoaiCheckBoxes.add(cb);
                vboxTheLoai.getChildren().add(cb);
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi CSDL", "Không thể nạp danh sách thể loại. Vui lòng kiểm tra bảng the_loai.");
            e.printStackTrace();
        }
    }
    
    private void loadSelectedTheLoai(long maPhim) {
        String sql = "SELECT ma_the_loai FROM phim_the_loai WHERE ma_phim = ?";
        List<Long> selectedIds = new ArrayList<>();
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, maPhim);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    selectedIds.add(rs.getLong("ma_the_loai"));
                }
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi CSDL", "Không thể nạp thể loại cũ.");
            e.printStackTrace();
            return;
        }
        
        for (CheckBox cb : theLoaiCheckBoxes) {
            long cbId = (Long) cb.getUserData(); 
            cb.setSelected(selectedIds.contains(cbId));
        }
    }

    private List<Long> getSelectedTheLoaiIds() {
        List<Long> maTheLoaiDaChon = new ArrayList<>();
        for (CheckBox cb : theLoaiCheckBoxes) {
            if (cb.isSelected()) {
                maTheLoaiDaChon.add((Long) cb.getUserData()); 
            }
        }
        return maTheLoaiDaChon;
    }


    // ======================================================================
    // LOGIC LƯU (THÊM HOẶC CẬP NHẬT)
    // ======================================================================
    
    @FXML
    private void handleLuuPhim() {
        // 1. VALIDATE DỮ LIỆU
        String tenPhim = txtTenPhim.getText().trim();
        if (tenPhim.isEmpty() || cbPhanLoai.getValue() == null || dpNgayPhatHanh.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Thiếu thông tin", "Vui lòng nhập đủ Tên phim, Phân loại và Ngày phát hành.");
            return;
        }
        
        int thoiLuong = 0;
        try {
            thoiLuong = Integer.parseInt(txtThoiLuong.getText());
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi nhập liệu", "Thời lượng phải là số nguyên.");
            return;
        }

        if (phimToEdit == null) {
            luuPhimMoi(tenPhim, thoiLuong, cbPhanLoai.getValue(), dpNgayPhatHanh.getValue(), txtMoTa.getText());
        } else {
            capNhatPhim(phimToEdit.getMaPhim(), tenPhim, thoiLuong, cbPhanLoai.getValue(), dpNgayPhatHanh.getValue(), txtMoTa.getText());
        }
    }

    private void luuPhimMoi(String tenPhim, int thoiLuong, String phanLoai, LocalDate ngayPhatHanh, String moTa) {
        List<Long> maTheLoaiDaChon = getSelectedTheLoaiIds(); 
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); 
            
            // BƯỚC 1: INSERT PHIM
            String sqlPhim = "INSERT INTO phim(ten_phim, thoi_luong_phut, phan_loai, ngay_phat_hanh, mo_ta) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement psPhim = conn.prepareStatement(sqlPhim, Statement.RETURN_GENERATED_KEYS);
            psPhim.setString(1, tenPhim);
            psPhim.setInt(2, thoiLuong);
            psPhim.setString(3, phanLoai);
            psPhim.setDate(4, Date.valueOf(ngayPhatHanh));
            psPhim.setString(5, txtMoTa.getText()); // Dùng txtMoTa.getText()
            psPhim.executeUpdate();
            
            long maPhimMoi = -1;
            try (ResultSet generatedKeys = psPhim.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    maPhimMoi = generatedKeys.getLong(1);
                } else {
                    throw new SQLException("Thêm phim thất bại, không lấy được ID.");
                }
            }

            // BƯỚC 2: INSERT PHIM_THE_LOAI
            if (!maTheLoaiDaChon.isEmpty()) {
                String sqlTheLoai = "INSERT INTO phim_the_loai(ma_phim, ma_the_loai) VALUES (?, ?)";
                PreparedStatement psTheLoai = conn.prepareStatement(sqlTheLoai);
                for (Long maTL : maTheLoaiDaChon) {
                    psTheLoai.setLong(1, maPhimMoi);
                    psTheLoai.setLong(2, maTL);
                    psTheLoai.addBatch();
                }
                psTheLoai.executeBatch();
            }

            conn.commit(); 
            showAlert(Alert.AlertType.INFORMATION, "Thành công!", "Thêm phim mới thành công.");
            
            closeWindow();
           // parentController.loadPhimTable(null, null); 

        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
             showAlert(Alert.AlertType.ERROR, "Lỗi Lưu CSDL", "Không thể thêm phim. Vui lòng kiểm tra lại CSDL và các ràng buộc.\nChi tiết: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
        }
    }
    
    private void capNhatPhim(long maPhim, String tenPhim, int thoiLuong, String phanLoai, LocalDate ngayPhatHanh, String moTa) {
        List<Long> maTheLoaiMoi = getSelectedTheLoaiIds(); 
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); 
            
            // BƯỚC 1: UPDATE PHIM
            String sqlPhim = "UPDATE phim SET ten_phim=?, thoi_luong_phut=?, phan_loai=?, ngay_phat_hanh=?, mo_ta=? WHERE ma_phim=?";
            PreparedStatement psPhim = conn.prepareStatement(sqlPhim);
            psPhim.setString(1, tenPhim);
            psPhim.setInt(2, thoiLuong);
            psPhim.setString(3, phanLoai);
            psPhim.setDate(4, Date.valueOf(ngayPhatHanh));
            psPhim.setString(5, txtMoTa.getText()); // Dùng txtMoTa.getText()
            psPhim.setLong(6, maPhim);
            psPhim.executeUpdate();

            // BƯỚC 2: CẬP NHẬT PHIM_THE_LOAI (Xóa cũ, Chèn mới)
            String sqlDeleteTL = "DELETE FROM phim_the_loai WHERE ma_phim = ?";
            PreparedStatement psDeleteTL = conn.prepareStatement(sqlDeleteTL);
            psDeleteTL.setLong(1, maPhim);
            psDeleteTL.executeUpdate();

            if (!maTheLoaiMoi.isEmpty()) {
                String sqlInsertTL = "INSERT INTO phim_the_loai(ma_phim, ma_the_loai) VALUES (?, ?)";
                PreparedStatement psInsertTL = conn.prepareStatement(sqlInsertTL);
                for (Long maTL : maTheLoaiMoi) {
                    psInsertTL.setLong(1, maPhim);
                    psInsertTL.setLong(2, maTL);
                    psInsertTL.addBatch();
                }
                psInsertTL.executeBatch();
            }

            conn.commit();
            showAlert(Alert.AlertType.INFORMATION, "Thành công!", "Cập nhật phim thành công.");
            
            closeWindow();
          //  parentController.loadPhimTable(null, null); 

        } catch (SQLException e) {
            if (conn != null) { try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); } }
            showAlert(Alert.AlertType.ERROR, "Lỗi Lưu CSDL", "Không thể cập nhật phim. Vui lòng kiểm tra lại CSDL và các ràng buộc.\nChi tiết: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (conn != null) { try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ex) { ex.printStackTrace(); } }
        }
    }

    @FXML
    private void handleHuy() {
        closeWindow();
    }
    
    private void closeWindow() {
        Stage stage = (Stage) txtTenPhim.getScene().getWindow();
        stage.close();
    }
    
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}