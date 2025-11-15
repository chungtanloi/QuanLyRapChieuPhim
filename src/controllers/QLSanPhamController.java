package controllers;

import database.DBConnection;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;

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
    @FXML private Button btnThemSanPham, btnSuaSanPham;

    private ObservableList<ComboVM> comboList = FXCollections.observableArrayList();
    private ObservableList<SanPhamTrongComboVM> sanPhamTrongComboList = FXCollections.observableArrayList();
    private ObservableList<SanPhamVM> sanPhamList = FXCollections.observableArrayList();
    
    private ComboVM comboDangChon = null;
    private SanPhamTrongComboVM sanPhamDangSua = null;

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

    public static class SanPhamVM {
        private final IntegerProperty maSanPham = new SimpleIntegerProperty();
        private final StringProperty tenSanPham = new SimpleStringProperty();
        private final StringProperty loaiSanPham = new SimpleStringProperty();
        private final ObjectProperty<BigDecimal> giaSanPham = new SimpleObjectProperty<>();
        
        public SanPhamVM(int maSanPham, String tenSanPham, String loaiSanPham, BigDecimal giaSanPham) {
            this.maSanPham.set(maSanPham);
            this.tenSanPham.set(tenSanPham);
            this.loaiSanPham.set(loaiSanPham);
            this.giaSanPham.set(giaSanPham);
        }
        
        public int getMaSanPham() { return maSanPham.get(); }
        public String getTenSanPham() { return tenSanPham.get(); }
        public String getLoaiSanPham() { return loaiSanPham.get(); }
        public BigDecimal getGiaSanPham() { return giaSanPham.get(); }
        
        public IntegerProperty maSanPhamProperty() { return maSanPham; }
        public StringProperty tenSanPhamProperty() { return tenSanPham; }
        public StringProperty loaiSanPhamProperty() { return loaiSanPham; }
        public ObjectProperty<BigDecimal> giaSanPhamProperty() { return giaSanPham; }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("🚀 QLSanPhamController đang khởi tạo...");
        
        setupTableCombo();
        setupTableSanPham();
        setupFormControls();
        setupEventHandlers();
        loadComboData();
        loadSanPhamData();
        
        // Ban đầu vô hiệu hóa các nút thêm/sửa sản phẩm cho đến khi có combo được chọn
    if (btnThemSanPham != null) btnThemSanPham.setDisable(true);
    if (btnSuaSanPham != null) btnSuaSanPham.setDisable(true);
    
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

        // Sự kiện chọn sản phẩm để sửa
        tblSanPhamTrongCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            sanPhamDangSua = newVal;
        });
    }

    private void setupFormControls() {
        cbTrangThaiCombo.getItems().addAll("Đang bán", "Ngừng bán");
        cbTrangThaiCombo.setValue("Đang bán");
    }

    private void setupEventHandlers() {
        // Gán sự kiện cho các nút
        if (btnThemSanPham != null) {
            btnThemSanPham.setOnAction(e -> {
                System.out.println("Click Thêm sản phẩm vào combo");
                themSanPhamVaoCombo();
            });
        }

        if (btnSuaSanPham != null) {
            btnSuaSanPham.setOnAction(e -> {
                System.out.println("Click Sửa sản phẩm trong combo");
                suaSanPhamTrongCombo();
            });
        }
        
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

    private void loadSanPhamData() {
        String sql = "SELECT ma_san_pham, ten_san_pham, loai, gia FROM san_pham WHERE hoat_dong = 1 ORDER BY ten_san_pham";
        
        sanPhamList.clear();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                SanPhamVM sp = new SanPhamVM(
                    rs.getInt("ma_san_pham"),
                    rs.getString("ten_san_pham"),
                    rs.getString("loai"),
                    rs.getBigDecimal("gia")
                );
                sanPhamList.add(sp);
            }
            
            System.out.println("✅ Đã tải " + sanPhamList.size() + " sản phẩm từ database");
            
        } catch (SQLException e) {
            System.out.println("❌ Lỗi tải sản phẩm: " + e.getMessage());
            e.printStackTrace();
        }
    }

private void hienThiChiTietCombo(ComboVM combo) {
    if (combo == null) return;
    
    System.out.println("📋 Hiển thị chi tiết combo: " + combo.getTenCombo());
    
    comboDangChon = combo;
    txtTenCombo.setText(combo.getTenCombo());
    txtGiaCombo.setText(combo.getGiaCombo().toString());
    cbTrangThaiCombo.setValue(combo.isHoatDong() ? "Đang bán" : "Ngừng bán");
    
    // KÍCH HOẠT các nút thêm/sửa sản phẩm khi có combo được chọn
    if (btnThemSanPham != null) btnThemSanPham.setDisable(false);
    if (btnSuaSanPham != null) btnSuaSanPham.setDisable(false);
    
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
    
    private void themSanPhamVaoCombo() {
        if (comboDangChon == null) {
            showAlert(Alert.AlertType.WARNING, "Vui lòng chọn một combo để thêm sản phẩm.");
            return;
        }

           if (comboDangChon.getMaCombo() == 0) {
        // Combo mới chưa được lưu, thêm vào danh sách tạm
        themSanPhamVaoComboTam();
    } else {
        // Combo đã có trong database, thêm trực tiếp
        themSanPhamVaoComboDaTonTai();
    }
        // Tạo dialog chọn sản phẩm
        Dialog<SanPhamVM> dialog = new Dialog<>();
        dialog.setTitle("Chọn sản phẩm");
        dialog.setHeaderText("Chọn sản phẩm và số lượng để thêm vào combo");

        // Tạo layout cho dialog
        VBox content = new VBox(10);
        content.setPadding(new javafx.geometry.Insets(10));

        // TableView cho sản phẩm
        TableView<SanPhamVM> tableSP = new TableView<>();
        tableSP.setPrefHeight(300);

        TableColumn<SanPhamVM, Integer> colMa = new TableColumn<>("Mã");
        colMa.setCellValueFactory(new PropertyValueFactory<>("maSanPham"));
        colMa.setPrefWidth(60);

        TableColumn<SanPhamVM, String> colTen = new TableColumn<>("Tên sản phẩm");
        colTen.setCellValueFactory(new PropertyValueFactory<>("tenSanPham"));
        colTen.setPrefWidth(200);

        TableColumn<SanPhamVM, String> colLoai = new TableColumn<>("Loại");
        colLoai.setCellValueFactory(new PropertyValueFactory<>("loaiSanPham"));
        colLoai.setPrefWidth(100);

        TableColumn<SanPhamVM, BigDecimal> colGia = new TableColumn<>("Giá");
        colGia.setCellValueFactory(new PropertyValueFactory<>("giaSanPham"));
        colGia.setPrefWidth(100);
        colGia.setCellFactory(tc -> new TableCell<SanPhamVM, BigDecimal>() {
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

        tableSP.getColumns().addAll(colMa, colTen, colLoai, colGia);
        tableSP.setItems(sanPhamList);

        // Spinner cho số lượng
        HBox quantityBox = new HBox(10);
        quantityBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        Label lblQuantity = new Label("Số lượng:");
        Spinner<Integer> spinner = new Spinner<>(1, 100, 1);
        spinner.setPrefWidth(80);
        quantityBox.getChildren().addAll(lblQuantity, spinner);

        content.getChildren().addAll(tableSP, quantityBox);

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Xử lý kết quả
        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                SanPhamVM selected = tableSP.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    // Kiểm tra xem sản phẩm đã có trong combo chưa
                    for (SanPhamTrongComboVM sp : sanPhamTrongComboList) {
                        if (sp.getMaSanPham() == selected.getMaSanPham()) {
                            showAlert(Alert.AlertType.WARNING, "Sản phẩm này đã có trong combo!");
                            return null;
                        }
                    }
                    
                    int soLuong = spinner.getValue();
                    themSanPhamVaoDatabase(selected.getMaSanPham(), soLuong);
                    return selected;
                } else {
                    showAlert(Alert.AlertType.WARNING, "Vui lòng chọn một sản phẩm!");
                }
            }
            return null;
        });

        dialog.showAndWait();
    }

    private void themSanPhamVaoDatabase(int maSanPham, int soLuong) {
        if (comboDangChon == null) return;

        String sql = "INSERT INTO combo_chi_tiet (ma_combo, ma_san_pham, so_luong) VALUES (?, ?, ?)";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, comboDangChon.getMaCombo());
            ps.setInt(2, maSanPham);
            ps.setInt(3, soLuong);
            
            int affectedRows = ps.executeUpdate();
            
            if (affectedRows > 0) {
                System.out.println("✅ Thêm sản phẩm vào combo thành công");
                loadSanPhamTrongCombo(comboDangChon.getMaCombo());
            }
            
        } catch (SQLException e) {
            System.out.println("❌ Lỗi thêm sản phẩm vào combo: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Lỗi thêm sản phẩm: " + e.getMessage());
        }
    }

    private void suaSanPhamTrongCombo() {
        if (sanPhamDangSua == null) {
            showAlert(Alert.AlertType.WARNING, "Vui lòng chọn một sản phẩm trong combo để sửa.");
            return;
        }

        // Tạo dialog sửa số lượng
        TextInputDialog dialog = new TextInputDialog(String.valueOf(sanPhamDangSua.getSoLuong()));
        dialog.setTitle("Sửa số lượng");
        dialog.setHeaderText("Sửa số lượng cho: " + sanPhamDangSua.getTenSanPham());
        dialog.setContentText("Số lượng mới:");

        dialog.showAndWait().ifPresent(newQuantity -> {
            try {
                int soLuong = Integer.parseInt(newQuantity);
                if (soLuong <= 0) {
                    showAlert(Alert.AlertType.ERROR, "Số lượng phải lớn hơn 0!");
                    return;
                }

                capNhatSoLuongSanPham(sanPhamDangSua.getMaSanPham(), soLuong);
                
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Số lượng phải là số nguyên!");
            }
        });
    }
private void themSanPhamVaoComboTam() {
    // Tạo dialog chọn sản phẩm
    Dialog<SanPhamVM> dialog = new Dialog<>();
    dialog.setTitle("Chọn sản phẩm");
    dialog.setHeaderText("Chọn sản phẩm và số lượng để thêm vào combo");

    // Tạo layout cho dialog
    VBox content = new VBox(10);
    content.setPadding(new Insets(10));

    // TableView cho sản phẩm
    TableView<SanPhamVM> tableSP = new TableView<>();
    tableSP.setPrefHeight(300);

    TableColumn<SanPhamVM, Integer> colMa = new TableColumn<>("Mã");
    colMa.setCellValueFactory(new PropertyValueFactory<>("maSanPham"));
    colMa.setPrefWidth(60);

    TableColumn<SanPhamVM, String> colTen = new TableColumn<>("Tên sản phẩm");
    colTen.setCellValueFactory(new PropertyValueFactory<>("tenSanPham"));
    colTen.setPrefWidth(200);

    TableColumn<SanPhamVM, String> colLoai = new TableColumn<>("Loại");
    colLoai.setCellValueFactory(new PropertyValueFactory<>("loaiSanPham"));
    colLoai.setPrefWidth(100);

    TableColumn<SanPhamVM, BigDecimal> colGia = new TableColumn<>("Giá");
    colGia.setCellValueFactory(new PropertyValueFactory<>("giaSanPham"));
    colGia.setPrefWidth(100);
    colGia.setCellFactory(tc -> new TableCell<SanPhamVM, BigDecimal>() {
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

    tableSP.getColumns().addAll(colMa, colTen, colLoai, colGia);
    tableSP.setItems(sanPhamList);

    // Spinner cho số lượng
    HBox quantityBox = new HBox(10);
    quantityBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
    Label lblQuantity = new Label("Số lượng:");
    Spinner<Integer> spinner = new Spinner<>(1, 100, 1);
    spinner.setPrefWidth(80);
    quantityBox.getChildren().addAll(lblQuantity, spinner);

    content.getChildren().addAll(tableSP, quantityBox);

    dialog.getDialogPane().setContent(content);
    dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

    // Xử lý kết quả
    dialog.setResultConverter(buttonType -> {
        if (buttonType == ButtonType.OK) {
            SanPhamVM selected = tableSP.getSelectionModel().getSelectedItem();
            if (selected != null) {
                // Kiểm tra xem sản phẩm đã có trong combo chưa
                for (SanPhamTrongComboVM sp : sanPhamTrongComboList) {
                    if (sp.getMaSanPham() == selected.getMaSanPham()) {
                        showAlert(Alert.AlertType.WARNING, "Sản phẩm này đã có trong combo!");
                        return null;
                    }
                }
                
                int soLuong = spinner.getValue();
                
                // THÊM VÀO DANH SÁCH TẠM (chưa lưu database)
                SanPhamTrongComboVM spMoi = new SanPhamTrongComboVM(
                    selected.getTenSanPham(),
                    soLuong,
                    selected.getGiaSanPham(),
                    selected.getMaSanPham()
                );
                sanPhamTrongComboList.add(spMoi);
                
                // CẬP NHẬT LẠI TABLE VIEW
                tblSanPhamTrongCombo.setItems(sanPhamTrongComboList);
                
                // TÍNH LẠI GIÁ COMBO
                tinhGiaComboTuDong();
                
                System.out.println("✅ Đã thêm sản phẩm vào combo tạm: " + selected.getTenSanPham());
                return selected;
            } else {
                showAlert(Alert.AlertType.WARNING, "Vui lòng chọn một sản phẩm!");
            }
        }
        return null;
    });

    dialog.showAndWait();
}

// PHƯƠNG THỨC MỚI: Thêm sản phẩm vào combo đã tồn tại trong database
private void themSanPhamVaoComboDaTonTai() {
    // Tạo dialog chọn sản phẩm (tương tự như trên)
    Dialog<SanPhamVM> dialog = new Dialog<>();
    dialog.setTitle("Chọn sản phẩm");
    dialog.setHeaderText("Chọn sản phẩm và số lượng để thêm vào combo");

    VBox content = new VBox(10);
    content.setPadding(new Insets(10));

    TableView<SanPhamVM> tableSP = new TableView<>();
    tableSP.setPrefHeight(300);

    TableColumn<SanPhamVM, Integer> colMa = new TableColumn<>("Mã");
    colMa.setCellValueFactory(new PropertyValueFactory<>("maSanPham"));
    colMa.setPrefWidth(60);

    TableColumn<SanPhamVM, String> colTen = new TableColumn<>("Tên sản phẩm");
    colTen.setCellValueFactory(new PropertyValueFactory<>("tenSanPham"));
    colTen.setPrefWidth(200);

    TableColumn<SanPhamVM, String> colLoai = new TableColumn<>("Loại");
    colLoai.setCellValueFactory(new PropertyValueFactory<>("loaiSanPham"));
    colLoai.setPrefWidth(100);

    TableColumn<SanPhamVM, BigDecimal> colGia = new TableColumn<>("Giá");
    colGia.setCellValueFactory(new PropertyValueFactory<>("giaSanPham"));
    colGia.setPrefWidth(100);
    colGia.setCellFactory(tc -> new TableCell<SanPhamVM, BigDecimal>() {
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

    tableSP.getColumns().addAll(colMa, colTen, colLoai, colGia);
    tableSP.setItems(sanPhamList);

    HBox quantityBox = new HBox(10);
    quantityBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
    Label lblQuantity = new Label("Số lượng:");
    Spinner<Integer> spinner = new Spinner<>(1, 100, 1);
    spinner.setPrefWidth(80);
    quantityBox.getChildren().addAll(lblQuantity, spinner);

    content.getChildren().addAll(tableSP, quantityBox);

    dialog.getDialogPane().setContent(content);
    dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

    // Xử lý kết quả
    dialog.setResultConverter(buttonType -> {
        if (buttonType == ButtonType.OK) {
            SanPhamVM selected = tableSP.getSelectionModel().getSelectedItem();
            if (selected != null) {
                int soLuong = spinner.getValue();
                themSanPhamVaoDatabase(selected.getMaSanPham(), soLuong);
                return selected;
            } else {
                showAlert(Alert.AlertType.WARNING, "Vui lòng chọn một sản phẩm!");
            }
        }
        return null;
    });

    dialog.showAndWait();
}

    private void capNhatSoLuongSanPham(int maSanPham, int soLuong) {
        if (comboDangChon == null) return;

        String sql = "UPDATE combo_chi_tiet SET so_luong = ? WHERE ma_combo = ? AND ma_san_pham = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, soLuong);
            ps.setInt(2, comboDangChon.getMaCombo());
            ps.setInt(3, maSanPham);
            
            int affectedRows = ps.executeUpdate();
            
            if (affectedRows > 0) {
                System.out.println("✅ Cập nhật số lượng thành công");
                loadSanPhamTrongCombo(comboDangChon.getMaCombo());
            }
            
        } catch (SQLException e) {
            System.out.println("❌ Lỗi cập nhật số lượng: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Lỗi cập nhật số lượng: " + e.getMessage());
        }
    }

@FXML
private void themComboMoi() {
    System.out.println("🆕 Thêm combo mới");
    
    txtTenCombo.clear();
    txtGiaCombo.clear();
    txtMoTaCombo.clear();
    cbTrangThaiCombo.setValue("Đang bán");
    sanPhamTrongComboList.clear();
    
    // Tạo một combo tạm thời để đánh dấu đang thêm mới
    comboDangChon = new ComboVM(0, "", BigDecimal.ZERO, true);
    sanPhamDangSua = null;
    
    tblCombo.getSelectionModel().clearSelection();
    tblSanPhamTrongCombo.getSelectionModel().clearSelection();
    
    // KÍCH HOẠT các nút thêm/sửa sản phẩm
    if (btnThemSanPham != null) btnThemSanPham.setDisable(false);
    if (btnSuaSanPham != null) btnSuaSanPham.setDisable(false);
    
    System.out.println("✅ Đã chuyển sang chế độ thêm combo mới - có thể thêm sản phẩm");
}

private void tinhGiaComboTuDong() {
    BigDecimal tongGia = BigDecimal.ZERO;
    for (SanPhamTrongComboVM sp : sanPhamTrongComboList) {
        BigDecimal giaSanPham = sp.getGiaSanPham();
        int soLuong = sp.getSoLuong();
        tongGia = tongGia.add(giaSanPham.multiply(BigDecimal.valueOf(soLuong)));
    }
    
    // Có thể áp dụng giảm giá cho combo (ví dụ: giảm 5%)
    BigDecimal giaCombo = tongGia.multiply(BigDecimal.valueOf(0.95));
    txtGiaCombo.setText(giaCombo.setScale(0, BigDecimal.ROUND_HALF_UP).toString());
}
private void themTatCaSanPhamVaoComboMoi(int maComboMoi) {
    if (sanPhamTrongComboList.isEmpty()) {
        System.out.println("📝 Combo mới không có sản phẩm nào");
        return;
    }
    
    String sql = "INSERT INTO combo_chi_tiet (ma_combo, ma_san_pham, so_luong) VALUES (?, ?, ?)";
    
    try (Connection conn = DBConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
        
        for (SanPhamTrongComboVM sp : sanPhamTrongComboList) {
            ps.setInt(1, maComboMoi);
            ps.setInt(2, sp.getMaSanPham());
            ps.setInt(3, sp.getSoLuong());
            ps.addBatch();
        }
        
        int[] results = ps.executeBatch();
        System.out.println("✅ Đã thêm " + results.length + " sản phẩm vào combo mới");
        
    } catch (SQLException e) {
        System.out.println("❌ Lỗi thêm sản phẩm vào combo mới: " + e.getMessage());
        e.printStackTrace();
    }
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

    if (sanPhamTrongComboList.isEmpty()) {
        showAlert(Alert.AlertType.WARNING, "Combo phải có ít nhất 1 sản phẩm!");
        return;
    }

    try {
        BigDecimal gia = new BigDecimal(giaStr);
        
        if (comboDangChon != null && comboDangChon.getMaCombo() == 0) {
            // THÊM MỚI COMBO
            String sql = "INSERT INTO combo (ten_combo, gia, hoat_dong) VALUES (?, ?, ?)";
            
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                
                ps.setString(1, tenCombo);
                ps.setBigDecimal(2, gia);
                ps.setBoolean(3, hoatDong);
                int affectedRows = ps.executeUpdate();
                
                if (affectedRows > 0) {
                    // Lấy mã combo vừa tạo
                    try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            int maComboMoi = generatedKeys.getInt(1);
                            System.out.println("✅ Thêm combo thành công, mã combo: " + maComboMoi);
                            
                            // THÊM TẤT CẢ SẢN PHẨM TRONG DANH SÁCH TẠM VÀO COMBO MỚI
                            themTatCaSanPhamVaoComboMoi(maComboMoi);
                            
                            showAlert(Alert.AlertType.INFORMATION, "Thêm combo thành công!");
                            loadComboData();
                            themComboMoi(); // Reset form để thêm combo tiếp theo
                        }
                    }
                }
            }
        } else if (comboDangChon != null) {
            // CẬP NHẬT COMBO ĐÃ CÓ
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
        
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận xóa");
        confirm.setHeaderText("Xóa sản phẩm khỏi combo");
        confirm.setContentText("Bạn có chắc chắn muốn xóa " + sanPham.getTenSanPham() + " khỏi combo?");
        
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            String sql = "DELETE FROM combo_chi_tiet WHERE ma_combo = ? AND ma_san_pham = ?";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                
                ps.setInt(1, comboDangChon.getMaCombo());
                ps.setInt(2, sanPham.getMaSanPham());
                int affectedRows = ps.executeUpdate();
                
                System.out.println("✅ Đã xóa " + affectedRows + " sản phẩm khỏi combo");
                showAlert(Alert.AlertType.INFORMATION, "Xóa sản phẩm thành công!");
                loadSanPhamTrongCombo(comboDangChon.getMaCombo());
                
            } catch (SQLException e) {
                System.out.println("❌ Lỗi xóa sản phẩm: " + e.getMessage());
                showAlert(Alert.AlertType.ERROR, "Lỗi xóa sản phẩm: " + e.getMessage());
            }
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