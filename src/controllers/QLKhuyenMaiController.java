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

    private ObservableList<KhuyenMaiVM> khuyenMaiList = FXCollections.observableArrayList();

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
        
        public String getMaCode() { return maCode.get(); }
        public String getKieuGiam() { return kieuGiam.get(); }
        public BigDecimal getGiaTriGiam() { return giaTriGiam.get(); }
        public BigDecimal getDonToiThieu() { return donToiThieu.get(); }
        public String getThoiGian() {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM HH:mm");
            return batDauLuc.get().toLocalDateTime().format(formatter) + " - " + 
                   ketThucLuc.get().toLocalDateTime().format(formatter);
        }
        public String getTrangThai() {
            LocalDateTime now = LocalDateTime.now();
            if (!hoatDong.get()) return "🔴 Tắt";
            if (now.isBefore(batDauLuc.get().toLocalDateTime())) return "🟡 Sắp diễn ra";
            if (now.isAfter(ketThucLuc.get().toLocalDateTime())) return "🔴 Hết hạn";
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
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTableKhuyenMai();
        setupFormControls();
        loadKhuyenMaiData();
    }

    private void setupTableKhuyenMai() {
        colMaKM.setCellValueFactory(new PropertyValueFactory<>("maCode"));
        colLoaiGiam.setCellValueFactory(new PropertyValueFactory<>("kieuGiam"));
        colMucGiam.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getMucGiamDisplay()));
        colDonToiThieu.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getDonToiThieuDisplay()));
        colThoiGianKM.setCellValueFactory(new PropertyValueFactory<>("thoiGian"));
        colTrangThaiKM.setCellValueFactory(new PropertyValueFactory<>("trangThai"));

        // CỘT ACTION
        colActionKM.setCellFactory(col -> new TableCell<KhuyenMaiVM, Void>() {
            private final Button btnSua = new Button("✏️");
            private final Button btnXoa = new Button("🗑️");
            private final HBox container = new HBox(5, btnSua, btnXoa);
            
            {
                btnSua.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-size: 12px;");
                btnXoa.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-size: 12px;");
                
                btnSua.setOnAction(e -> {
                    KhuyenMaiVM km = getTableView().getItems().get(getIndex());
                    suaKhuyenMai(km);
                });
                
                btnXoa.setOnAction(e -> {
                    KhuyenMaiVM km = getTableView().getItems().get(getIndex());
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
                hienThiChiTietKhuyenMai(newVal);
            }
        });
    }

    private void setupFormControls() {
        cbLoaiKM.getItems().addAll("Tất cả", "PHAN_TRAM", "SO_TIEN");
        cbTrangThaiKM.getItems().addAll("Tất cả", "Đang áp dụng", "Hết hạn", "Tắt");
        cbKieuGiam.getItems().addAll("PHAN_TRAM", "SO_TIEN");
        cbStatusKM.getItems().addAll("Bật", "Tắt");
        
        cbLoaiKM.setValue("Tất cả");
        cbTrangThaiKM.setValue("Tất cả");
        cbKieuGiam.setValue("PHAN_TRAM");
        cbStatusKM.setValue("Bật");
        
        // Set ngày mặc định
        dpBatDau.setValue(LocalDate.now());
        dpKetThuc.setValue(LocalDate.now().plusDays(30));
    }

    private void loadKhuyenMaiData() {
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
                khuyenMaiList.add(new KhuyenMaiVM(
                    rs.getInt("ma_khuyen_mai"),
                    rs.getString("ma_code"),
                    rs.getString("kieu_giam"),
                    rs.getBigDecimal("gia_tri_giam"),
                    rs.getBigDecimal("don_toi_thieu"),
                    rs.getTimestamp("bat_dau_luc"),
                    rs.getTimestamp("ket_thuc_luc"),
                    rs.getBoolean("hoat_dong")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        tblKhuyenMai.setItems(khuyenMaiList);
        lblTongKM.setText(khuyenMaiList.size() + " KM");
    }

    private void hienThiChiTietKhuyenMai(KhuyenMaiVM km) {
        txtMaKM.setText(km.getMaCode());
        cbKieuGiam.setValue(km.getKieuGiam());
        txtMucGiam.setText(String.valueOf(km.getGiaTriGiam()));
        txtDonToiThieu.setText(String.valueOf(km.getDonToiThieu()));
        
        // Set dates
        dpBatDau.setValue(km.batDauLuc.get().toLocalDateTime().toLocalDate());
        dpKetThuc.setValue(km.ketThucLuc.get().toLocalDateTime().toLocalDate());
        
        cbStatusKM.setValue(km.hoatDong.get() ? "Bật" : "Tắt");
    }

    @FXML
    private void themKhuyenMaiMoi() {
        txtMaKM.clear();
        txtMucGiam.clear();
        txtDonToiThieu.clear();
        cbKieuGiam.setValue("PHAN_TRAM");
        cbStatusKM.setValue("Bật");
        dpBatDau.setValue(LocalDate.now());
        dpKetThuc.setValue(LocalDate.now().plusDays(30));
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

        if (maCode.isEmpty() || mucGiamStr.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Vui lòng nhập mã KM và mức giảm");
            return;
        }

        if (batDau == null || ketThuc == null) {
            showAlert(Alert.AlertType.WARNING, "Vui lòng chọn thời gian");
            return;
        }

        try {
            BigDecimal mucGiam = new BigDecimal(mucGiamStr);
            BigDecimal donToiThieu = donToiThieuStr.isEmpty() ? BigDecimal.ZERO : new BigDecimal(donToiThieuStr);

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
                
                showAlert(Alert.AlertType.INFORMATION, "Thêm khuyến mãi thành công!");
                loadKhuyenMaiData();
                themKhuyenMaiMoi();
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi thêm khuyến mãi: " + e.getMessage());
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
                
                ps.setInt(1, km.maKhuyenMai.get());
                ps.executeUpdate();
                
                showAlert(Alert.AlertType.INFORMATION, "Xóa khuyến mãi thành công!");
                loadKhuyenMaiData();
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Lỗi xóa khuyến mãi: " + e.getMessage());
            }
        }
    }

    @FXML
    private void timKiemKhuyenMai() {
        String keyword = txtTimKM.getText().trim().toLowerCase();
        String loai = cbLoaiKM.getValue();
        String trangThai = cbTrangThaiKM.getValue();

        ObservableList<KhuyenMaiVM> filtered = khuyenMaiList.filtered(km -> {
            boolean matchKeyword = keyword.isEmpty() || km.getMaCode().toLowerCase().contains(keyword);
            boolean matchLoai = "Tất cả".equals(loai) || km.getKieuGiam().equals(loai);
            boolean matchTrangThai = "Tất cả".equals(trangThai) || km.getTrangThai().contains(trangThai);
            
            return matchKeyword && matchLoai && matchTrangThai;
        });

        tblKhuyenMai.setItems(filtered);
        lblTongKM.setText(filtered.size() + " KM");
    }

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}