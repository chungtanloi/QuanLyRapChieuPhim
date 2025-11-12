package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;

// Child controllers (auto-injected via <fx:include fx:id="..."> naming convention)
import controllers.SuatChieuController;
import controllers.PhimController;
import controllers.KhachHangController;
import controllers.HoaDonController;
import controllers.ComboController;
import controllers.KhuyenMaiController;

public class NhanVienShellController {

    // root & top bar
    @FXML private BorderPane root;
    @FXML private TextField txtSearch;
    @FXML private DatePicker dpNgay;
    @FXML private ComboBox<String> cbRap, cbPhong, cbTheLoai, cbDinhDang;
    @FXML private CheckBox ckcConVe, ckcSapChieu;
    @FXML private Label lblWelcome, lblTongSuat;
    @FXML private Button btnRefresh;
    @FXML private Button btnBanVe, btnDoiVe, btnTraVe;
    @FXML private MenuButton mbUser;
    @FXML private MenuItem miProfile, miChangePwd, miLogout;

    // nav
    @FXML private ToggleGroup nav;
    @FXML private ToggleButton navSuatChieu, navPhim, navKhachHang, navHoaDon, navCombo, navKhuyenMai;
    @FXML private TabPane mainTabs;

    // children (note: field name = fx:id + "Controller" in parent when using <fx:include>)
    @FXML private SuatChieuController suatChieuController;
    @FXML private PhimController phimController;
    @FXML private KhachHangController khachHangController;
    @FXML private HoaDonController hoaDonController;
    @FXML private ComboController comboController;
    @FXML private KhuyenMaiController khuyenMaiController;

    @FXML
    private void initialize() {
        // Basic nav bindings
        if (navSuatChieu != null) navSuatChieu.setOnAction(e -> mainTabs.getSelectionModel().select(0));
        if (navPhim != null)       navPhim.setOnAction(e -> mainTabs.getSelectionModel().select(1));
        if (navKhachHang != null)  navKhachHang.setOnAction(e -> mainTabs.getSelectionModel().select(2));
        if (navHoaDon != null)     navHoaDon.setOnAction(e -> mainTabs.getSelectionModel().select(3));
        if (navCombo != null)      navCombo.setOnAction(e -> mainTabs.getSelectionModel().select(4));
        if (navKhuyenMai != null)  navKhuyenMai.setOnAction(e -> mainTabs.getSelectionModel().select(5));

        // Filters change -> broadcast to children
        Runnable broadcast = () -> {
            String kw = txtSearch != null ? txtSearch.getText() : "";
            String rap = cbRap != null ? (cbRap.getValue()) : null;
            String phong = cbPhong != null ? (cbPhong.getValue()) : null;
            String theloai = cbTheLoai != null ? cbTheLoai.getValue() : null;
            String dinhdang = cbDinhDang != null ? cbDinhDang.getValue() : null;
            boolean conVe = ckcConVe != null && ckcConVe.isSelected();
            boolean sapChieu = ckcSapChieu != null && ckcSapChieu.isSelected();

            if (suatChieuController != null)
                suatChieuController.applyFilters(kw, rap, phong, theloai, dinhdang, conVe, sapChieu, dpNgay.getValue(), lblTongSuat);
            if (phimController != null)
                phimController.applyFilters(kw, rap, phong, theloai, dinhdang, sapChieu, dpNgay.getValue());
        };

        if (txtSearch != null) txtSearch.textProperty().addListener((o,ov,nv)->broadcast.run());
        if (dpNgay != null) dpNgay.valueProperty().addListener((o,ov,nv)->broadcast.run());
        if (cbRap != null) cbRap.valueProperty().addListener((o,ov,nv)->broadcast.run());
        if (cbPhong != null) cbPhong.valueProperty().addListener((o,ov,nv)->broadcast.run());
        if (cbTheLoai != null) cbTheLoai.valueProperty().addListener((o,ov,nv)->broadcast.run());
        if (cbDinhDang != null) cbDinhDang.valueProperty().addListener((o,ov,nv)->broadcast.run());
        if (ckcConVe != null) ckcConVe.selectedProperty().addListener((o,ov,nv)->broadcast.run());
        if (ckcSapChieu != null) ckcSapChieu.selectedProperty().addListener((o,ov,nv)->broadcast.run());

        if (btnRefresh != null) btnRefresh.setOnAction(e -> broadcast.run());

        // Gắn phím tắt khi scene sẵn sàng
        if (root != null) {
            root.sceneProperty().addListener((o, ov, nv) -> { if (nv != null) attachAccelerators(nv); });
        }
    }
    
    // === Được gọi từ LoginController sau khi load FXML ===
    private String pendingTenNV;
    public void setTenNhanVien(String ten) {
        this.pendingTenNV = ten;
        if (lblWelcome != null && ten != null) {
            lblWelcome.setText("Xin chào, " + ten + "!");
        }
    }


    // === Helpers: phím tắt và safe fire ===
    private void attachAccelerators(javafx.scene.Scene scene) {
        if (scene == null) return;
        scene.getAccelerators().put(new javafx.scene.input.KeyCodeCombination(javafx.scene.input.KeyCode.F1),
                () -> safeFire(btnBanVe));
        scene.getAccelerators().put(new javafx.scene.input.KeyCodeCombination(javafx.scene.input.KeyCode.F2),
                () -> safeFire(btnDoiVe));
        scene.getAccelerators().put(new javafx.scene.input.KeyCodeCombination(javafx.scene.input.KeyCode.F3),
                () -> safeFire(btnTraVe));
        scene.getAccelerators().put(new javafx.scene.input.KeyCodeCombination(javafx.scene.input.KeyCode.F, javafx.scene.input.KeyCombination.CONTROL_DOWN),
                () -> { if (txtSearch != null) txtSearch.requestFocus(); });
    }

    private void safeFire(javafx.scene.control.Button b) {
        if (b != null && !b.isDisabled()) b.fire();
    }

}
