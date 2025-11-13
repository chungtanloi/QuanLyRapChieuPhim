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
import java.util.ResourceBundle;
import javafx.scene.layout.HBox;

public class QLSanPhamController implements Initializable {

    // ===== TABLE COMBO =====
    @FXML private TableView<ComboVM> tblCombo;
    @FXML private TableColumn<ComboVM, Integer> colMaCombo;
    @FXML private TableColumn<ComboVM, String> colTenCombo;
    @FXML private TableColumn<ComboVM, BigDecimal> colGiaCombo;
    @FXML private TableColumn<ComboVM, String> colTrangThaiCombo;
    @FXML private TableColumn<ComboVM, Void> colActionCombo;

    // ===== TABLE SAN PHAM TRONG COMBO =====
    @FXML private TableView<SanPhamTrongComboVM> tblSanPhamTrongCombo;
    @FXML private TableColumn<SanPhamTrongComboVM, String> colTenSP;
    @FXML private TableColumn<SanPhamTrongComboVM, Integer> colSoLuongSP;
    @FXML private TableColumn<SanPhamTrongComboVM, BigDecimal> colGiaSP;
    @FXML private TableColumn<SanPhamTrongComboVM, Void> colActionSP;

    // ===== FORM CONTROLS =====
    @FXML private TextField txtTimCombo, txtTenCombo, txtGiaCombo;
    @FXML private TextArea txtMoTaCombo;
    @FXML private ComboBox<String> cbTrangThaiCombo;
    @FXML private Label lblTongCombo;

    // ===== BUTTONS =====
    @FXML private Button btnThemCombo, btnLamMoi, btnTimKiem, btnHuy, btnLuu;

    private ObservableList<ComboVM> comboList = FXCollections.observableArrayList();
    private ObservableList<SanPhamTrongComboVM> sanPhamTrongComboList = FXCollections.observableArrayList();
    private ComboVM comboDangChon = null;

    // ===== VIEW MODELS =====
    public static class ComboVM {
        private final IntegerProperty maCombo = new SimpleIntegerProperty();
        private final StringProperty tenCombo = new SimpleStringProperty();
        private final ObjectProperty<BigDecimal> giaCombo = new SimpleObjectProperty<>();
        private final BooleanProperty hoatDong = new SimpleBooleanProperty();
        
        public ComboVM(int maCombo, String tenCombo, BigDecimal giaCombo, boolean hoatDong) {
            this.maCombo.set(maCombo);
            this.tenCombo.set(tenCombo);
            this.giaCombo.set(giaCombo);
            this.hoatDong.set(hoatDong);
        }
        
        public int getMaCombo() { return maCombo.get(); }
        public String getTenCombo() { return tenCombo.get(); }
        public BigDecimal getGiaCombo() { return giaCombo.get(); }
        public boolean isHoatDong() { return hoatDong.get(); }
        public String getTrangThai() { 
            return hoatDong.get() ? "Đang bán" : "Ngừng bán"; 
        }
        
        public IntegerProperty maComboProperty() { return maCombo; }
        public StringProperty tenComboProperty() { return tenCombo; }
        public ObjectProperty<BigDecimal> giaComboProperty() { return giaCombo; }
        public BooleanProperty hoatDongProperty() { return hoatDong; }
    }

    public static class SanPhamTrongComboVM {
        private final StringProperty tenSanPham = new SimpleStringProperty();
        private final IntegerProperty soLuong = new SimpleIntegerProperty();
        private final ObjectProperty<BigDecimal> giaSanPham = new SimpleObjectProperty<>();
        private final IntegerProperty maSanPham = new SimpleIntegerProperty();
        
        public SanPhamTrongComboVM(String tenSanPham, int soLuong, BigDecimal giaSanPham, int maSanPham) {
            this.tenSanPham.set(tenSanPham);
            this.soLuong.set(soLuong);
            this.giaSanPham.set(giaSanPham);
            this.maSanPham.set(maSanPham);
        }
        
        public String getTenSanPham() { return tenSanPham.get(); }
        public int getSoLuong() { return soLuong.get(); }
        public BigDecimal getGiaSanPham() { return giaSanPham.get(); }
        public int getMaSanPham() { return maSanPham.get(); }
        
        public StringProperty tenSanPhamProperty() { return tenSanPham; }
        public IntegerProperty soLuongProperty() { return soLuong; }
        public ObjectProperty<BigDecimal> giaSanPhamProperty() { return giaSanPham; }
        public IntegerProperty maSanPhamProperty() { return maSanPham; }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("🚀 QLSanPhamController đang khởi tạo...");
        
        setupTableCombo();
        setupTableSanPham();
        setupFormControls();
        setupEventHandlers();
        loadComboData();
        
        System.out.println("✅ QLSanPhamController khởi tạo thành công");
    }

    private void setupTableCombo() {
        System.out.println("🔄 Thiết lập table combo...");
        
        colMaCombo.setCellValueFactory(new PropertyValueFactory<>("maCombo"));
        colTenCombo.setCellValueFactory(new PropertyValueFactory<>("tenCombo"));
        colGiaCombo.setCellValueFactory(new PropertyValueFactory<>("giaCombo"));
        
        colGiaCombo.setCellFactory(tc -> new TableCell<ComboVM, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%,d đ", item.intValue()));
                }
            }
        });
        
        colTrangThaiCombo.setCellValueFactory(new PropertyValueFactory<>("trangThai"));

        // CỘT ACTION
        colActionCombo.setCellFactory(col -> new TableCell<ComboVM, Void>() {
            private final Button btnSua = new Button("✏️ Sửa");
            private final Button btnXoa = new Button("🗑️ Xóa");
            private final HBox container = new HBox(5, btnSua, btnXoa);
            
            {
                btnSua.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-size: 12px;");
                btnXoa.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-size: 12px;");
                
                btnSua.setOnAction(e -> {
                    ComboVM combo = getTableView().getItems().get(getIndex());
                    System.out.println("Sửa combo: " + combo.getTenCombo());
                    suaCombo(combo);
                });
                
                btnXoa.setOnAction(e -> {
                    ComboVM combo = getTableView().getItems().get(getIndex());
                    System.out.println("Xóa combo: " + combo.getTenCombo());
                    xoaCombo(combo);
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : container);
            }
        });

        // SỰ KIỆN CHỌN COMBO
        tblCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                System.out.println("Chọn combo: " + newVal.getTenCombo());
                hienThiChiTietCombo(newVal);
            }
        });
    }

    private void setupTableSanPham() {
        colTenSP.setCellValueFactory(new PropertyValueFactory<>("tenSanPham"));
        colSoLuongSP.setCellValueFactory(new PropertyValueFactory<>("soLuong"));
        colGiaSP.setCellValueFactory(new PropertyValueFactory<>("giaSanPham"));
        
        colGiaSP.setCellFactory(tc -> new TableCell<SanPhamTrongComboVM, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%,d đ", item.intValue()));
                }
            }
        });

        colActionSP.setCellFactory(col -> new TableCell<SanPhamTrongComboVM, Void>() {
            private final Button btnXoa = new Button("Xóa");
            {
                btnXoa.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-size: 12px;");
                btnXoa.setOnAction(e -> {
                    SanPhamTrongComboVM sp = getTableView().getItems().get(getIndex());
                    System.out.println("Xóa sản phẩm: " + sp.getTenSanPham());
                    xoaSanPhamKhoiCombo(sp);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btnXoa);
            }
        });
    }

    private void setupFormControls() {
        cbTrangThaiCombo.getItems().addAll("Đang bán", "Ngừng bán");
        cbTrangThaiCombo.setValue("Đang bán");
    }

    private void setupEventHandlers() {
        // Gán sự kiện cho các nút
        if (btnThemCombo != null) {
            btnThemCombo.setOnAction(e -> {
                System.out.println("Click Thêm Combo");
                themComboMoi();
            });
        } else {
            System.out.println("❌ btnThemCombo là null - kiểm tra fx:id trong FXML");
        }
        
        if (btnLuu != null) {
            btnLuu.setOnAction(e -> {
                System.out.println("Click Lưu Combo");
                luuCombo();
            });
        }
        
        if (btnHuy != null) {
            btnHuy.setOnAction(e -> {
                System.out.println("Click Hủy");
                themComboMoi();
            });
        }
        
        if (btnLamMoi != null) {
            btnLamMoi.setOnAction(e -> {
                System.out.println("Click Làm mới");
                loadComboData();
            });
        }
        
        if (btnTimKiem != null) {
            btnTimKiem.setOnAction(e -> {
                System.out.println("Click Tìm kiếm");
                timKiemCombo();
            });
        }
        
        // Tìm kiếm khi nhập text
        if (txtTimCombo != null) {
            txtTimCombo.textProperty().addListener((obs, oldVal, newVal) -> {
                timKiemCombo();
            });
        }
    }

    private void loadComboData() {
        System.out.println("🔄 Đang tải dữ liệu combo...");
        
        String sql = "SELECT ma_combo, ten_combo, gia, hoat_dong FROM combo ORDER BY ma_combo";
        
        comboList.clear();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                ComboVM combo = new ComboVM(
                    rs.getInt("ma_combo"),
                    rs.getString("ten_combo"),
                    rs.getBigDecimal("gia"),
                    rs.getBoolean("hoat_dong")
                );
                comboList.add(combo);
            }
            
            System.out.println("✅ Đã tải " + comboList.size() + " combo từ database");
            
            // Debug: in ra danh sách combo
            for (ComboVM combo : comboList) {
                System.out.println("📦 Combo: " + combo.getMaCombo() + " - " + combo.getTenCombo() + " - " + combo.getGiaCombo());
            }
            
        } catch (SQLException e) {
            System.out.println("❌ Lỗi SQL: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi tải dữ liệu: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("❌ Lỗi kết nối database: " + e.getMessage());
            e.printStackTrace();
        }
        
        tblCombo.setItems(comboList);
        lblTongCombo.setText(comboList.size() + " combo");
    }

    private void hienThiChiTietCombo(ComboVM combo) {
        if (combo == null) return;
        
        System.out.println("📋 Hiển thị chi tiết combo: " + combo.getTenCombo());
        
        comboDangChon = combo;
        txtTenCombo.setText(combo.getTenCombo());
        txtGiaCombo.setText(combo.getGiaCombo().toString());
        cbTrangThaiCombo.setValue(combo.isHoatDong() ? "Đang bán" : "Ngừng bán");
        
        loadSanPhamTrongCombo(combo.getMaCombo());
    }

    private void loadSanPhamTrongCombo(int maCombo) {
        String sql = """
            SELECT sp.ma_san_pham, sp.ten_san_pham, ct.so_luong, sp.gia
            FROM combo_chi_tiet ct
            JOIN san_pham sp ON ct.ma_san_pham = sp.ma_san_pham
            WHERE ct.ma_combo = ?
        """;
        
        sanPhamTrongComboList.clear();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, maCombo);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    SanPhamTrongComboVM sp = new SanPhamTrongComboVM(
                        rs.getString("ten_san_pham"),
                        rs.getInt("so_luong"),
                        rs.getBigDecimal("gia"),
                        rs.getInt("ma_san_pham")
                    );
                    sanPhamTrongComboList.add(sp);
                }
            }
            
            System.out.println("✅ Đã tải " + sanPhamTrongComboList.size() + " sản phẩm trong combo");
            
        } catch (SQLException e) {
            System.out.println("❌ Lỗi tải sản phẩm trong combo: " + e.getMessage());
            e.printStackTrace();
        }
        
        tblSanPhamTrongCombo.setItems(sanPhamTrongComboList);
    }

    @FXML
    private void themComboMoi() {
        System.out.println("🆕 Thêm combo mới");
        
        txtTenCombo.clear();
        txtGiaCombo.clear();
        txtMoTaCombo.clear();
        cbTrangThaiCombo.setValue("Đang bán");
        sanPhamTrongComboList.clear();
        comboDangChon = null;
        
        tblCombo.getSelectionModel().clearSelection();
    }

    @FXML
    private void luuCombo() {
        String tenCombo = txtTenCombo.getText().trim();
        String giaStr = txtGiaCombo.getText().trim();
        boolean hoatDong = "Đang bán".equals(cbTrangThaiCombo.getValue());

        System.out.println("💾 Lưu combo: " + tenCombo + ", giá: " + giaStr);

        if (tenCombo.isEmpty() || giaStr.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Vui lòng nhập tên combo và giá");
            return;
        }

        try {
            BigDecimal gia = new BigDecimal(giaStr);
            
            if (comboDangChon == null) {
                // THÊM MỚI
                String sql = "INSERT INTO combo (ten_combo, gia, hoat_dong) VALUES (?, ?, ?)";
                
                try (Connection conn = DBConnection.getConnection();
                     PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    
                    ps.setString(1, tenCombo);
                    ps.setBigDecimal(2, gia);
                    ps.setBoolean(3, hoatDong);
                    int affectedRows = ps.executeUpdate();
                    
                    System.out.println("✅ Thêm combo thành công, affected rows: " + affectedRows);
                    showAlert(Alert.AlertType.INFORMATION, "Thêm combo thành công!");
                    loadComboData();
                    themComboMoi();
                }
            } else {
                // CẬP NHẬT
                String sql = "UPDATE combo SET ten_combo = ?, gia = ?, hoat_dong = ? WHERE ma_combo = ?";
                
                try (Connection conn = DBConnection.getConnection();
                     PreparedStatement ps = conn.prepareStatement(sql)) {
                    
                    ps.setString(1, tenCombo);
                    ps.setBigDecimal(2, gia);
                    ps.setBoolean(3, hoatDong);
                    ps.setInt(4, comboDangChon.getMaCombo());
                    int affectedRows = ps.executeUpdate();
                    
                    System.out.println("✅ Cập nhật combo thành công, affected rows: " + affectedRows);
                    showAlert(Alert.AlertType.INFORMATION, "Cập nhật combo thành công!");
                    loadComboData();
                }
            }
        } catch (NumberFormatException e) {
            System.out.println("❌ Lỗi định dạng số: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Giá combo phải là số!");
        } catch (Exception e) {
            System.out.println("❌ Lỗi lưu combo: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Lỗi lưu combo: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void suaCombo(ComboVM combo) {
        hienThiChiTietCombo(combo);
    }

    private void xoaCombo(ComboVM combo) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận xóa");
        confirm.setHeaderText("Xóa combo: " + combo.getTenCombo());
        confirm.setContentText("Bạn có chắc chắn muốn xóa combo này?");
        
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            String sql = "DELETE FROM combo WHERE ma_combo = ?";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                
                ps.setInt(1, combo.getMaCombo());
                int affectedRows = ps.executeUpdate();
                
                if (affectedRows > 0) {
                    System.out.println("✅ Xóa combo thành công");
                    showAlert(Alert.AlertType.INFORMATION, "Xóa combo thành công!");
                    loadComboData();
                    themComboMoi();
                }
            } catch (SQLException e) {
                System.out.println("❌ Lỗi xóa combo: " + e.getMessage());
                showAlert(Alert.AlertType.ERROR, "Lỗi xóa combo: " + e.getMessage());
            }
        }
    }

    private void xoaSanPhamKhoiCombo(SanPhamTrongComboVM sanPham) {
        if (comboDangChon == null) return;
        
        System.out.println("🗑️ Xóa sản phẩm " + sanPham.getTenSanPham() + " khỏi combo");
        
        String sql = "DELETE FROM combo_chi_tiet WHERE ma_combo = ? AND ma_san_pham = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, comboDangChon.getMaCombo());
            ps.setInt(2, sanPham.getMaSanPham());
            int affectedRows = ps.executeUpdate();
            
            System.out.println("✅ Đã xóa " + affectedRows + " sản phẩm khỏi combo");
            
            loadSanPhamTrongCombo(comboDangChon.getMaCombo());
            
        } catch (SQLException e) {
            System.out.println("❌ Lỗi xóa sản phẩm: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Lỗi xóa sản phẩm: " + e.getMessage());
        }
    }

    @FXML
    private void timKiemCombo() {
        String keyword = txtTimCombo.getText().trim().toLowerCase();
        System.out.println("🔍 Tìm kiếm với từ khóa: " + keyword);
        
        if (keyword.isEmpty()) {
            tblCombo.setItems(comboList);
            lblTongCombo.setText(comboList.size() + " combo");
            return;
        }

        ObservableList<ComboVM> filtered = comboList.filtered(combo -> 
            combo.getTenCombo().toLowerCase().contains(keyword) ||
            String.valueOf(combo.getMaCombo()).contains(keyword)
        );
        tblCombo.setItems(filtered);
        lblTongCombo.setText(filtered.size() + " combo");
        
        System.out.println("✅ Tìm thấy " + filtered.size() + " combo");
    }

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}