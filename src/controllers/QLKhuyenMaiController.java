package controllers;

import database.DBConnection;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import javafx.scene.layout.HBox;

public class QLKhuyenMaiController implements Initializable {

    // ===== TABLE KHUYEN MAI =====
    @FXML private TableView<KhuyenMaiVM> tblKhuyenMai;
    @FXML private TableColumn<KhuyenMaiVM, String> colMaKM;
    @FXML private TableColumn<KhuyenMaiVM, String> colLoaiGiam;
    @FXML private TableColumn<KhuyenMaiVM, String> colMucGiam;
    @FXML private TableColumn<KhuyenMaiVM, String> colDonToiThieu;
    @FXML private TableColumn<KhuyenMaiVM, String> colThoiGianKM;
    @FXML private TableColumn<KhuyenMaiVM, String> colTrangThaiKM;
    @FXML private TableColumn<KhuyenMaiVM, Void> colActionKM;

    // ===== FORM CONTROLS =====
    @FXML private TextField txtTimKM, txtMaKM, txtMucGiam, txtDonToiThieu;
    @FXML private ComboBox<String> cbLoaiKM, cbTrangThaiKM, cbKieuGiam, cbStatusKM;
    @FXML private DatePicker dpBatDau, dpKetThuc;
    @FXML private Label lblTongKM;

    // ===== BUTTONS =====
    @FXML private Button btnThemKM, btnTimKiem, btnHuy, btnLuu;

    private ObservableList<KhuyenMaiVM> khuyenMaiList = FXCollections.observableArrayList();
    private KhuyenMaiVM khuyenMaiDangChon = null;

    // ===== VIEW MODEL =====
    public static class KhuyenMaiVM {
        private final IntegerProperty maKhuyenMai = new SimpleIntegerProperty();
        private final StringProperty maCode = new SimpleStringProperty();
        private final StringProperty kieuGiam = new SimpleStringProperty();
        private final ObjectProperty<BigDecimal> giaTriGiam = new SimpleObjectProperty<>();
        private final ObjectProperty<BigDecimal> donToiThieu = new SimpleObjectProperty<>();
        private final ObjectProperty<Timestamp> batDauLuc = new SimpleObjectProperty<>();
        private final ObjectProperty<Timestamp> ketThucLuc = new SimpleObjectProperty<>();
        private final BooleanProperty hoatDong = new SimpleBooleanProperty();
        
        public KhuyenMaiVM(int maKhuyenMai, String maCode, String kieuGiam, BigDecimal giaTriGiam, 
                          BigDecimal donToiThieu, Timestamp batDauLuc, Timestamp ketThucLuc, boolean hoatDong) {
            this.maKhuyenMai.set(maKhuyenMai);
            this.maCode.set(maCode);
            this.kieuGiam.set(kieuGiam);
            this.giaTriGiam.set(giaTriGiam);
            this.donToiThieu.set(donToiThieu);
            this.batDauLuc.set(batDauLuc);
            this.ketThucLuc.set(ketThucLuc);
            this.hoatDong.set(hoatDong);
        }
        
        // Getters
        public int getMaKhuyenMai() { return maKhuyenMai.get(); }
        public String getMaCode() { return maCode.get(); }
        public String getKieuGiam() { return kieuGiam.get(); }
        public BigDecimal getGiaTriGiam() { return giaTriGiam.get(); }
        public BigDecimal getDonToiThieu() { return donToiThieu.get(); }
        public Timestamp getBatDauLuc() { return batDauLuc.get(); }
        public Timestamp getKetThucLuc() { return ketThucLuc.get(); }
        public boolean isHoatDong() { return hoatDong.get(); }
        
        // Display methods
        public String getThoiGian() {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            return batDauLuc.get().toLocalDateTime().format(formatter) + " - " + 
                   ketThucLuc.get().toLocalDateTime().format(formatter);
        }
        
        public String getTrangThai() {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime batDau = batDauLuc.get().toLocalDateTime();
            LocalDateTime ketThuc = ketThucLuc.get().toLocalDateTime();
            
            if (!hoatDong.get()) return "🔴 Tắt";
            if (now.isBefore(batDau)) return "🟡 Sắp diễn ra";
            if (now.isAfter(ketThuc)) return "🔴 Hết hạn";
            return "🟢 Đang áp dụng";
        }
        
        public String getMucGiamDisplay() {
            return "PHAN_TRAM".equals(kieuGiam.get()) ? 
                String.format("%.0f%%", giaTriGiam.get()) : 
                String.format("%,.0f đ", giaTriGiam.get());
        }
        
        public String getDonToiThieuDisplay() {
            return donToiThieu.get().compareTo(BigDecimal.ZERO) > 0 ? 
                String.format("%,.0f đ", donToiThieu.get()) : "Không có";
        }
        
        public String getLoaiGiamDisplay() {
            return "PHAN_TRAM".equals(kieuGiam.get()) ? "Phần trăm" : "Số tiền";
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("🚀 QLKhuyenMaiController đang khởi tạo...");
           // Kiểm tra các control
    System.out.println("🔍 Kiểm tra controls:");
    System.out.println(" - tblKhuyenMai: " + (tblKhuyenMai != null ? "✅" : "❌"));
    System.out.println(" - btnThemKM: " + (btnThemKM != null ? "✅" : "❌"));
    System.out.println(" - btnTimKiem: " + (btnTimKiem != null ? "✅" : "❌"));
    System.out.println(" - btnHuy: " + (btnHuy != null ? "✅" : "❌"));
    System.out.println(" - btnLuu: " + (btnLuu != null ? "✅" : "❌"));
    
        setupTableKhuyenMai();
        setupFormControls();
        setupEventHandlers();
        loadKhuyenMaiData();
        
        System.out.println("✅ QLKhuyenMaiController khởi tạo thành công");
    }

    private void setupTableKhuyenMai() {
        colMaKM.setCellValueFactory(new PropertyValueFactory<>("maCode"));
        
        // Hiển thị "Phần trăm" / "Số tiền" thay vì "PHAN_TRAM" / "SO_TIEN"
        colLoaiGiam.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getLoaiGiamDisplay()));
        
        colMucGiam.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getMucGiamDisplay()));
        colDonToiThieu.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getDonToiThieuDisplay()));
        colThoiGianKM.setCellValueFactory(new PropertyValueFactory<>("thoiGian"));
        colTrangThaiKM.setCellValueFactory(new PropertyValueFactory<>("trangThai"));

        // CỘT ACTION
        colActionKM.setCellFactory(col -> new TableCell<KhuyenMaiVM, Void>() {
            private final Button btnSua = new Button("Sửa");
            private final Button btnXoa = new Button("Xóa");
            private final HBox container = new HBox(5, btnSua, btnXoa);
            
            {
                btnSua.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-size: 12px; -fx-pref-width: 60; -fx-background-radius: 3;");
                btnXoa.setStyle("-fx-background-color: #dc2626; -fx-text-fill: white; -fx-font-size: 12px; -fx-pref-width: 60; -fx-background-radius: 3;");
                
                btnSua.setOnAction(e -> {
                    KhuyenMaiVM km = getTableView().getItems().get(getIndex());
                    System.out.println("Sửa khuyến mãi: " + km.getMaCode());
                    suaKhuyenMai(km);
                });
                
                btnXoa.setOnAction(e -> {
                    KhuyenMaiVM km = getTableView().getItems().get(getIndex());
                    System.out.println("Xóa khuyến mãi: " + km.getMaCode());
                    xoaKhuyenMai(km);
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : container);
            }
        });

        // SỰ KIỆN CHỌN KHUYẾN MÃI
        tblKhuyenMai.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                System.out.println("Chọn khuyến mãi: " + newVal.getMaCode());
                hienThiChiTietKhuyenMai(newVal);
            }
        });
    }

    private void setupFormControls() {
        // Combo box options
        cbLoaiKM.getItems().addAll("Tất cả", "PHAN_TRAM", "SO_TIEN");
        cbTrangThaiKM.getItems().addAll("Tất cả", "Đang áp dụng", "Hết hạn", "Tắt");
        cbKieuGiam.getItems().addAll("PHAN_TRAM", "SO_TIEN");
        cbStatusKM.getItems().addAll("Bật", "Tắt");
        
        // Default values
        cbLoaiKM.setValue("Tất cả");
        cbTrangThaiKM.setValue("Tất cả");
        cbKieuGiam.setValue("PHAN_TRAM");
        cbStatusKM.setValue("Bật");
        
        // Set ngày mặc định
        dpBatDau.setValue(LocalDate.now());
        dpKetThuc.setValue(LocalDate.now().plusDays(30));
    }

    private void setupEventHandlers() {
         // Nút Thêm KM
    if (btnThemKM != null) {
        System.out.println("✅ Nút Thêm KM được tìm thấy");
        btnThemKM.setOnAction(e -> {
            System.out.println("🎯 Click Thêm KM - Đang gọi themKhuyenMaiMoi()");
            themKhuyenMaiMoi();
        });
    } else {
        System.out.println("❌ Nút Thêm KM là NULL - kiểm tra fx:id trong FXML");
    }
        
          // Nút Tìm kiếm
    if (btnTimKiem != null) {
        System.out.println("✅ Nút Tìm kiếm được tìm thấy");
        btnTimKiem.setOnAction(e -> {
            System.out.println("🎯 Click Tìm kiếm");
            timKiemKhuyenMai();
        });
    } else {
        System.out.println("❌ Nút Tìm kiếm là NULL");
    }
        
          // Nút Hủy
    if (btnHuy != null) {
        System.out.println("✅ Nút Hủy được tìm thấy");
        btnHuy.setOnAction(e -> {
            System.out.println("🎯 Click Hủy");
            themKhuyenMaiMoi();
        });
    } else {
        System.out.println("❌ Nút Hủy là NULL");
    }
    
        
         // Nút Lưu
    if (btnLuu != null) {
        System.out.println("✅ Nút Lưu được tìm thấy");
        btnLuu.setOnAction(e -> {
            System.out.println("🎯 Click Lưu");
            luuKhuyenMai();
        });
    } else {
        System.out.println("❌ Nút Lưu là NULL");
    }
        
         // Tìm kiếm khi nhập text
    if (txtTimKM != null) {
        txtTimKM.textProperty().addListener((obs, oldVal, newVal) -> {
            timKiemKhuyenMai();
        });
    }
          // Tìm kiếm khi thay đổi combo box
    if (cbLoaiKM != null) {
        cbLoaiKM.valueProperty().addListener((obs, oldVal, newVal) -> {
            timKiemKhuyenMai();
        });
    }
    
    if (cbTrangThaiKM != null) {
        cbTrangThaiKM.valueProperty().addListener((obs, oldVal, newVal) -> {
            timKiemKhuyenMai();
        });
    }
    }

    private void loadKhuyenMaiData() {
        System.out.println("🔄 Đang tải dữ liệu khuyến mãi...");
        
        String sql = """
            SELECT ma_khuyen_mai, ma_code, kieu_giam, gia_tri_giam, don_toi_thieu, 
                   bat_dau_luc, ket_thuc_luc, hoat_dong
            FROM khuyen_mai 
            ORDER BY bat_dau_luc DESC
        """;
        
        khuyenMaiList.clear();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                KhuyenMaiVM km = new KhuyenMaiVM(
                    rs.getInt("ma_khuyen_mai"),
                    rs.getString("ma_code"),
                    rs.getString("kieu_giam"),
                    rs.getBigDecimal("gia_tri_giam"),
                    rs.getBigDecimal("don_toi_thieu"),
                    rs.getTimestamp("bat_dau_luc"),
                    rs.getTimestamp("ket_thuc_luc"),
                    rs.getBoolean("hoat_dong")
                );
                khuyenMaiList.add(km);
            }
            
            System.out.println("✅ Đã tải " + khuyenMaiList.size() + " khuyến mãi từ database");
            
        } catch (SQLException e) {
            System.out.println("❌ Lỗi tải khuyến mãi: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi tải dữ liệu: " + e.getMessage());
        }
        
        tblKhuyenMai.setItems(khuyenMaiList);
        lblTongKM.setText(khuyenMaiList.size() + " KM");
    }

    private void hienThiChiTietKhuyenMai(KhuyenMaiVM km) {
        if (km == null) return;
        
        System.out.println("📋 Hiển thị chi tiết khuyến mãi: " + km.getMaCode());
        
        khuyenMaiDangChon = km;
        txtMaKM.setText(km.getMaCode());
        cbKieuGiam.setValue(km.getKieuGiam());
        txtMucGiam.setText(km.getGiaTriGiam().toString());
        txtDonToiThieu.setText(km.getDonToiThieu().toString());
        
        // Set dates
        dpBatDau.setValue(km.getBatDauLuc().toLocalDateTime().toLocalDate());
        dpKetThuc.setValue(km.getKetThucLuc().toLocalDateTime().toLocalDate());
        
        cbStatusKM.setValue(km.isHoatDong() ? "Bật" : "Tắt");
    }

   @FXML
private void themKhuyenMaiMoi() {
    System.out.println("🎯 BẮT ĐẦU: themKhuyenMaiMoi()");
    
    try {
        // Reset form
        txtMaKM.clear();
        System.out.println("✅ Đã clear txtMaKM");
        
        txtMucGiam.clear();
        System.out.println("✅ Đã clear txtMucGiam");
        
        txtDonToiThieu.clear();
        System.out.println("✅ Đã clear txtDonToiThieu");
        
        cbKieuGiam.setValue("PHAN_TRAM");
        System.out.println("✅ Đã set cbKieuGiam: " + cbKieuGiam.getValue());
        
        cbStatusKM.setValue("Bật");
        System.out.println("✅ Đã set cbStatusKM: " + cbStatusKM.getValue());
        
        dpBatDau.setValue(LocalDate.now());
        System.out.println("✅ Đã set dpBatDau: " + dpBatDau.getValue());
        
        dpKetThuc.setValue(LocalDate.now().plusDays(30));
        System.out.println("✅ Đã set dpKetThuc: " + dpKetThuc.getValue());
        
        khuyenMaiDangChon = null;
        System.out.println("✅ Đã set khuyenMaiDangChon = null");
        
        // Bỏ chọn table
        tblKhuyenMai.getSelectionModel().clearSelection();
        System.out.println("✅ Đã clear selection table");
        
        System.out.println("🎯 KẾT THÚC: themKhuyenMaiMoi() - Form đã được reset");
        
    } catch (Exception e) {
        System.out.println("❌ LỖI trong themKhuyenMaiMoi(): " + e.getMessage());
        e.printStackTrace();
    }
}

    @FXML
    private void luuKhuyenMai() {
        String maCode = txtMaKM.getText().trim();
        String kieuGiam = cbKieuGiam.getValue();
        String mucGiamStr = txtMucGiam.getText().trim();
        String donToiThieuStr = txtDonToiThieu.getText().trim();
        LocalDate batDau = dpBatDau.getValue();
        LocalDate ketThuc = dpKetThuc.getValue();
        boolean hoatDong = "Bật".equals(cbStatusKM.getValue());

        System.out.println("💾 Lưu khuyến mãi: " + maCode + ", kiểu: " + kieuGiam);

        if (maCode.isEmpty() || mucGiamStr.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Vui lòng nhập mã KM và mức giảm");
            return;
        }

        if (batDau == null || ketThuc == null) {
            showAlert(Alert.AlertType.WARNING, "Vui lòng chọn thời gian");
            return;
        }

        if (batDau.isAfter(ketThuc)) {
            showAlert(Alert.AlertType.WARNING, "Thời gian bắt đầu phải trước thời gian kết thúc");
            return;
        }

        try {
            BigDecimal mucGiam = new BigDecimal(mucGiamStr);
            BigDecimal donToiThieu = donToiThieuStr.isEmpty() ? BigDecimal.ZERO : new BigDecimal(donToiThieuStr);

            if (khuyenMaiDangChon == null) {
                // THÊM MỚI
                String sql = """
                    INSERT INTO khuyen_mai (ma_code, kieu_giam, gia_tri_giam, don_toi_thieu, bat_dau_luc, ket_thuc_luc, hoat_dong)
                    VALUES (?, ?, ?, ?, ?, ?, ?)
                """;
                
                try (Connection conn = DBConnection.getConnection();
                     PreparedStatement ps = conn.prepareStatement(sql)) {
                    
                    ps.setString(1, maCode);
                    ps.setString(2, kieuGiam);
                    ps.setBigDecimal(3, mucGiam);
                    ps.setBigDecimal(4, donToiThieu);
                    ps.setTimestamp(5, Timestamp.valueOf(batDau.atStartOfDay()));
                    ps.setTimestamp(6, Timestamp.valueOf(ketThuc.atTime(23, 59, 59)));
                    ps.setBoolean(7, hoatDong);
                    ps.executeUpdate();
                    
                    System.out.println("✅ Thêm khuyến mãi thành công");
                    showAlert(Alert.AlertType.INFORMATION, "Thêm khuyến mãi thành công!");
                    loadKhuyenMaiData();
                    themKhuyenMaiMoi();
                }
            } else {
                // CẬP NHẬT
                String sql = """
                    UPDATE khuyen_mai 
                    SET ma_code = ?, kieu_giam = ?, gia_tri_giam = ?, don_toi_thieu = ?, 
                        bat_dau_luc = ?, ket_thuc_luc = ?, hoat_dong = ?
                    WHERE ma_khuyen_mai = ?
                """;
                
                try (Connection conn = DBConnection.getConnection();
                     PreparedStatement ps = conn.prepareStatement(sql)) {
                    
                    ps.setString(1, maCode);
                    ps.setString(2, kieuGiam);
                    ps.setBigDecimal(3, mucGiam);
                    ps.setBigDecimal(4, donToiThieu);
                    ps.setTimestamp(5, Timestamp.valueOf(batDau.atStartOfDay()));
                    ps.setTimestamp(6, Timestamp.valueOf(ketThuc.atTime(23, 59, 59)));
                    ps.setBoolean(7, hoatDong);
                    ps.setInt(8, khuyenMaiDangChon.getMaKhuyenMai());
                    ps.executeUpdate();
                    
                    System.out.println("✅ Cập nhật khuyến mãi thành công");
                    showAlert(Alert.AlertType.INFORMATION, "Cập nhật khuyến mãi thành công!");
                    loadKhuyenMaiData();
                }
            }
        } catch (NumberFormatException e) {
            System.out.println("❌ Lỗi định dạng số: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Mức giảm và đơn tối thiểu phải là số!");
        } catch (Exception e) {
            System.out.println("❌ Lỗi lưu khuyến mãi: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Lỗi lưu khuyến mãi: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void suaKhuyenMai(KhuyenMaiVM km) {
        hienThiChiTietKhuyenMai(km);
    }

    private void xoaKhuyenMai(KhuyenMaiVM km) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận xóa");
        confirm.setHeaderText("Xóa khuyến mãi: " + km.getMaCode());
        confirm.setContentText("Bạn có chắc chắn muốn xóa khuyến mãi này?");
        
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            String sql = "DELETE FROM khuyen_mai WHERE ma_khuyen_mai = ?";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                
                ps.setInt(1, km.getMaKhuyenMai());
                int affectedRows = ps.executeUpdate();
                
                if (affectedRows > 0) {
                    System.out.println("✅ Xóa khuyến mãi thành công");
                    showAlert(Alert.AlertType.INFORMATION, "Xóa khuyến mãi thành công!");
                    loadKhuyenMaiData();
                    themKhuyenMaiMoi();
                }
            } catch (SQLException e) {
                System.out.println("❌ Lỗi xóa khuyến mãi: " + e.getMessage());
                showAlert(Alert.AlertType.ERROR, "Lỗi xóa khuyến mãi: " + e.getMessage());
            }
        }
    }

    @FXML
    private void timKiemKhuyenMai() {
        String keyword = txtTimKM.getText().trim().toLowerCase();
        String loai = cbLoaiKM.getValue();
        String trangThai = cbTrangThaiKM.getValue();

        System.out.println("🔍 Tìm kiếm với từ khóa: " + keyword + ", loại: " + loai + ", trạng thái: " + trangThai);

        ObservableList<KhuyenMaiVM> filtered = khuyenMaiList.filtered(km -> {
            boolean matchKeyword = keyword.isEmpty() || km.getMaCode().toLowerCase().contains(keyword);
            boolean matchLoai = "Tất cả".equals(loai) || km.getKieuGiam().equals(loai);
            boolean matchTrangThai = "Tất cả".equals(trangThai) || km.getTrangThai().contains(trangThai);
            
            return matchKeyword && matchLoai && matchTrangThai;
        });

        tblKhuyenMai.setItems(filtered);
        lblTongKM.setText(filtered.size() + " KM");
        
        System.out.println("✅ Tìm thấy " + filtered.size() + " khuyến mãi");
    }

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}