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
    @FXML private TableView<SanPhamVM> tblSanPhamTrongCombo;
    @FXML private TableColumn<SanPhamVM, String> colTenSP;
    @FXML private TableColumn<SanPhamVM, Integer> colSoLuongSP;
    @FXML private TableColumn<SanPhamVM, BigDecimal> colGiaSP;
    @FXML private TableColumn<SanPhamVM, Void> colActionSP;

    // ===== FORM CONTROLS =====
    @FXML private TextField txtTimCombo, txtTenCombo, txtGiaCombo;
    @FXML private TextArea txtMoTaCombo;
    @FXML private ComboBox<String> cbTrangThaiCombo;
    @FXML private Label lblTongCombo;

    private ObservableList<ComboVM> comboList = FXCollections.observableArrayList();
    private ObservableList<SanPhamVM> sanPhamList = FXCollections.observableArrayList();

    // ===== VIEW MODELS =====
    public static class ComboVM {
        private final IntegerProperty maCombo = new SimpleIntegerProperty();
        private final StringProperty tenCombo = new SimpleStringProperty();
        private final ObjectProperty<BigDecimal> giaCombo = new SimpleObjectProperty<>();
        private final StringProperty moTa = new SimpleStringProperty();
        private final BooleanProperty hoatDong = new SimpleBooleanProperty();
        
        public ComboVM(int maCombo, String tenCombo, BigDecimal giaCombo, String moTa, boolean hoatDong) {
            this.maCombo.set(maCombo);
            this.tenCombo.set(tenCombo);
            this.giaCombo.set(giaCombo);
            this.moTa.set(moTa);
            this.hoatDong.set(hoatDong);
        }
        
        public int getMaCombo() { return maCombo.get(); }
        public String getTenCombo() { return tenCombo.get(); }
        public BigDecimal getGiaCombo() { return giaCombo.get(); }
        public String getMoTa() { return moTa.get(); }
        public boolean isHoatDong() { return hoatDong.get(); }
        public String getTrangThai() { return hoatDong.get() ? "Đang bán" : "Ngừng bán"; }
        
        public IntegerProperty maComboProperty() { return maCombo; }
        public StringProperty tenComboProperty() { return tenCombo; }
        public ObjectProperty<BigDecimal> giaComboProperty() { return giaCombo; }
        public StringProperty moTaProperty() { return moTa; }
        public BooleanProperty hoatDongProperty() { return hoatDong; }
    }

    public static class SanPhamVM {
        private final StringProperty tenSanPham = new SimpleStringProperty();
        private final IntegerProperty soLuong = new SimpleIntegerProperty();
        private final ObjectProperty<BigDecimal> giaSanPham = new SimpleObjectProperty<>();
        
        public SanPhamVM(String tenSanPham, int soLuong, BigDecimal giaSanPham) {
            this.tenSanPham.set(tenSanPham);
            this.soLuong.set(soLuong);
            this.giaSanPham.set(giaSanPham);
        }
        
        public StringProperty tenSanPhamProperty() { return tenSanPham; }
        public IntegerProperty soLuongProperty() { return soLuong; }
        public ObjectProperty<BigDecimal> giaSanPhamProperty() { return giaSanPham; }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTableCombo();
        setupTableSanPham();
        loadComboData();
        setupFormControls();
    }

    private void setupTableCombo() {
        colMaCombo.setCellValueFactory(new PropertyValueFactory<>("maCombo"));
        colTenCombo.setCellValueFactory(new PropertyValueFactory<>("tenCombo"));
        colGiaCombo.setCellValueFactory(new PropertyValueFactory<>("giaCombo"));
        colGiaCombo.setCellFactory(tc -> new TableCell<ComboVM, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : String.format("%,d đ", item.intValue()));
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
                    suaCombo(combo);
                });
                
                btnXoa.setOnAction(e -> {
                    ComboVM combo = getTableView().getItems().get(getIndex());
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
                hienThiChiTietCombo(newVal);
            }
        });
    }

    private void setupTableSanPham() {
        colTenSP.setCellValueFactory(new PropertyValueFactory<>("tenSanPham"));
        colSoLuongSP.setCellValueFactory(new PropertyValueFactory<>("soLuong"));
        colGiaSP.setCellValueFactory(new PropertyValueFactory<>("giaSanPham"));
        colGiaSP.setCellFactory(tc -> new TableCell<SanPhamVM, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : String.format("%,d đ", item.intValue()));
            }
        });

        colActionSP.setCellFactory(col -> new TableCell<SanPhamVM, Void>() {
            private final Button btnXoa = new Button("Xóa");
            {
                btnXoa.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-size: 12px;");
                btnXoa.setOnAction(e -> {
                    SanPhamVM sp = getTableView().getItems().get(getIndex());
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

    private void loadComboData() {
        String sql = "SELECT ma_combo, ten_combo, gia, mo_ta, hoat_dong FROM combo ORDER BY ma_combo";
        
        comboList.clear();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                comboList.add(new ComboVM(
                    rs.getInt("ma_combo"),
                    rs.getString("ten_combo"),
                    rs.getBigDecimal("gia"),
                    rs.getString("mo_ta"),
                    rs.getBoolean("hoat_dong")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        tblCombo.setItems(comboList);
        lblTongCombo.setText(comboList.size() + " combo");
    }

    private void hienThiChiTietCombo(ComboVM combo) {
        txtTenCombo.setText(combo.getTenCombo());
        txtGiaCombo.setText(String.valueOf(combo.getGiaCombo()));
        txtMoTaCombo.setText(combo.getMoTa());
        cbTrangThaiCombo.setValue(combo.isHoatDong() ? "Đang bán" : "Ngừng bán");
        
        loadSanPhamTrongCombo(combo.getMaCombo());
    }

    private void loadSanPhamTrongCombo(int maCombo) {
        String sql = """
            SELECT sp.ten_san_pham, ct.so_luong, sp.gia
            FROM combo_chi_tiet ct
            JOIN san_pham sp ON ct.ma_san_pham = sp.ma_san_pham
            WHERE ct.ma_combo = ?
        """;
        
        sanPhamList.clear();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, maCombo);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    sanPhamList.add(new SanPhamVM(
                        rs.getString("ten_san_pham"),
                        rs.getInt("so_luong"),
                        rs.getBigDecimal("gia")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        tblSanPhamTrongCombo.setItems(sanPhamList);
    }

    @FXML
    private void themComboMoi() {
        // Reset form
        txtTenCombo.clear();
        txtGiaCombo.clear();
        txtMoTaCombo.clear();
        cbTrangThaiCombo.setValue("Đang bán");
        sanPhamList.clear();
    }

    @FXML
    private void luuCombo() {
        String tenCombo = txtTenCombo.getText().trim();
        String giaStr = txtGiaCombo.getText().trim();
        String moTa = txtMoTaCombo.getText().trim();
        boolean hoatDong = "Đang bán".equals(cbTrangThaiCombo.getValue());

        if (tenCombo.isEmpty() || giaStr.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Vui lòng nhập tên combo và giá");
            return;
        }

        try {
            BigDecimal gia = new BigDecimal(giaStr);
            String sql = "INSERT INTO combo (ten_combo, gia, mo_ta, hoat_dong) VALUES (?, ?, ?, ?)";
            
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                
                ps.setString(1, tenCombo);
                ps.setBigDecimal(2, gia);
                ps.setString(3, moTa);
                ps.setBoolean(4, hoatDong);
                ps.executeUpdate();
                
                showAlert(Alert.AlertType.INFORMATION, "Thêm combo thành công!");
                loadComboData();
                themComboMoi();
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi thêm combo: " + e.getMessage());
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
                ps.executeUpdate();
                
                showAlert(Alert.AlertType.INFORMATION, "Xóa combo thành công!");
                loadComboData();
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Lỗi xóa combo: " + e.getMessage());
            }
        }
    }

    private void xoaSanPhamKhoiCombo(SanPhamVM sanPham) {
        sanPhamList.remove(sanPham);
    }

    @FXML
    private void timKiemCombo() {
        String keyword = txtTimCombo.getText().trim().toLowerCase();
        if (keyword.isEmpty()) {
            tblCombo.setItems(comboList);
            return;
        }

        ObservableList<ComboVM> filtered = comboList.filtered(combo -> 
            combo.getTenCombo().toLowerCase().contains(keyword)
        );
        tblCombo.setItems(filtered);
        lblTongCombo.setText(filtered.size() + " combo");
    }

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}