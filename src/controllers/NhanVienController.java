package controllers;

import database.DBConnection;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Pair;
import javafx.animation.PauseTransition;
import javafx.util.Duration;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.Objects;
// ====== Thư viện iText để xuất PDF ======
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;

import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import java.text.NumberFormat;
import java.util.Locale;
import java.io.File;
import java.io.FileOutputStream;
// ===== iText xuất PDF =====
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Font;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPCell;

// ===== Hệ thống Java cơ bản =====
import java.text.NumberFormat;
import java.util.Locale;
import java.io.File;
import java.io.FileOutputStream;

public class NhanVienController {

    // ===== Root & thanh trạng thái =====
    @FXML
    private BorderPane root;
    @FXML
    private Label lblWelcome;
    @FXML
    private Label lblClock;

    // ===== App bar / filter nhanh =====
    @FXML
    private TextField txtSearch;
    @FXML
    private DatePicker dpNgay;
    @FXML
    private ComboBox<String> cbRap;
    @FXML
    private ComboBox<String> cbPhong;

    @FXML
    private Button btnBanVe, btnDoiVe, btnTraVe, btnRefresh;
    @FXML
    private MenuButton mbUser;
    @FXML
    private MenuItem miProfile, miChangePwd, miLogout;

    // ===== Tabs / vùng nội dung =====
    @FXML
    private TabPane mainTabs;

    // Tab Phim đang chiếu
    @FXML
    private TilePane tilePhimDangChieu;

    // ===== Suất chiếu hôm nay (Table + Pagination) =====
    @FXML
    private TableView<SuatChieuVM> tblSuatChieu;
    @FXML
    private TableColumn<SuatChieuVM, String> colPhim;
    @FXML
    private TableColumn<SuatChieuVM, String> colPhong;
    @FXML
    private TableColumn<SuatChieuVM, String> colGio;
    @FXML
    private TableColumn<SuatChieuVM, BigDecimal> colGia;
    @FXML
    private Pagination paginationSuatChieu;

    // ===== Khách hàng (CRUD + SEARCH) =====
    @FXML
    private TableView<KhachHangVM> tblKhachHang;
    @FXML
    private TableColumn<KhachHangVM, Number> colMaKH;
    @FXML
    private TableColumn<KhachHangVM, String> colHoTen;
    @FXML
    private TableColumn<KhachHangVM, String> colSDT;
    @FXML
    private TableColumn<KhachHangVM, String> colEmail;
    @FXML
    private TableColumn<KhachHangVM, String> colHangTV;
    @FXML
    private TableColumn<KhachHangVM, Number> colDiem;

    @FXML
    private TextField txtHoTenKH, txtSdtKH, txtEmailKH, txtDiem;

    // >>> Thêm ô TÌM KIẾM khách hàng
    @FXML
    private TextField txtTimKhach;
    @FXML
    private Button btnTimKhach, btnClearTimKH;

    @FXML
    private ComboBox<String> cbHangTV;
    @FXML
    private Button btnThemKH, btnSuaKH, btnXoaKH;

    // ===== Hóa đơn (đơn vé + đơn hàng) =====
    @FXML
    private DatePicker dpFrom, dpTo;
    @FXML
    private TextField txtMaHD;
    @FXML
    private Button btnTraCuuHD;
    @FXML
    private TableView<HoaDonVM> tblHoaDon;
    @FXML
    private TableColumn<HoaDonVM, Number> colMaHD;
    @FXML
    private TableColumn<HoaDonVM, String> colNgay;
    @FXML
    private TableColumn<HoaDonVM, String> colNhanVien;
    @FXML
    private TableColumn<HoaDonVM, String> colKhach;
    @FXML
    private TableColumn<HoaDonVM, BigDecimal> colTongTien;

    // ===== Session / user hiện tại =====
    private Integer currentMaNv;   // nhan_vien.ma_nhan_vien
    private Integer currentMaTk;   // tai_khoan.ma_tai_khoan
    private String currentHoTen;

    private static final int PAGE_SIZE_SC = 12;

    // ====== ViewModels ======
    public static class SuatChieuVM {

        private final StringProperty tenPhim = new SimpleStringProperty();
        private final StringProperty tenPhong = new SimpleStringProperty();
        private final StringProperty gio = new SimpleStringProperty();
        private final ObjectProperty<BigDecimal> gia = new SimpleObjectProperty<>();

        public SuatChieuVM(String phim, String phong, String gio, BigDecimal gia) {
            this.tenPhim.set(phim);
            this.tenPhong.set(phong);
            this.gio.set(gio);
            this.gia.set(gia);
        }

        public StringProperty tenPhimProperty() {
            return tenPhim;
        }

        public StringProperty tenPhongProperty() {
            return tenPhong;
        }

        public StringProperty gioProperty() {
            return gio;
        }

        public ObjectProperty<BigDecimal> giaProperty() {
            return gia;
        }
    }

    public static class KhachHangVM {

        private final IntegerProperty ma = new SimpleIntegerProperty();
        private final IntegerProperty maTaiKhoan = new SimpleIntegerProperty();
        private final StringProperty hoTen = new SimpleStringProperty();
        private final StringProperty sdt = new SimpleStringProperty();
        private final StringProperty email = new SimpleStringProperty();
        private final StringProperty hang = new SimpleStringProperty();
        private final IntegerProperty diem = new SimpleIntegerProperty();

        public KhachHangVM(int maKh, int maTk, String hoTen, String sdt, String email, String hang, int diem) {
            this.ma.set(maKh);
            this.maTaiKhoan.set(maTk);
            this.hoTen.set(hoTen);
            this.sdt.set(sdt);
            this.email.set(email);
            this.hang.set(hang);
            this.diem.set(diem);
        }

        public IntegerProperty maProperty() {
            return ma;
        }

        public IntegerProperty maTaiKhoanProperty() {
            return maTaiKhoan;
        }

        public StringProperty hoTenProperty() {
            return hoTen;
        }

        public StringProperty sdtProperty() {
            return sdt;
        }

        public StringProperty emailProperty() {
            return email;
        }

        public StringProperty hangProperty() {
            return hang;
        }

        public IntegerProperty diemProperty() {
            return diem;
        }
    }

    public static class HoaDonVM {

        private final IntegerProperty ma = new SimpleIntegerProperty();
        private final StringProperty ngay = new SimpleStringProperty();
        private final StringProperty nv = new SimpleStringProperty();
        private final StringProperty kh = new SimpleStringProperty();
        private final ObjectProperty<BigDecimal> tong = new SimpleObjectProperty<>();

        public HoaDonVM(int ma, String ngay, String nv, String kh, BigDecimal tong) {
            this.ma.set(ma);
            this.ngay.set(ngay);
            this.nv.set(nv);
            this.kh.set(kh);
            this.tong.set(tong);
        }

        public IntegerProperty maProperty() {
            return ma;
        }

        public StringProperty ngayProperty() {
            return ngay;
        }

        public StringProperty nvProperty() {
            return nv;
        }

        public StringProperty khProperty() {
            return kh;
        }

        public ObjectProperty<BigDecimal> tongProperty() {
            return tong;
        }
    }

    // ====== Lifecycle ======
    @FXML
    private void initialize() {
        if (dpNgay != null) {
            dpNgay.setValue(LocalDate.now());
        }
        if (miLogout != null) {
            miLogout.setOnAction(this::handleDangXuat);
        }

        Platform.runLater(() -> {
            if (root != null && root.getScene() != null) {
                attachAccelerators(root.getScene());
            }
        });

        wireSearch();
        if (txtSearch != null && !txtSearch.getText().isBlank()) {
            loadPhimByKeyword(txtSearch.getText().trim());
        } else {
            loadPhimDangChieu();
        }

        if (dpNgay != null) {
            dpNgay.valueProperty().addListener((obs, o, n) -> {
                if (txtSearch != null && !txtSearch.getText().isBlank()) {
                    loadPhimByKeyword(txtSearch.getText().trim());
                } else {
                    loadPhimDangChieu();
                }
                refreshSuatChieuPagination();
            });
        }

        initSuatChieuTable();

        initKhachHangTable();
        wireKhachHangButtons();
        wireKhachHangSearch(); // bật tìm kiếm khách hàng

        initHoaDonTable();

        if (btnRefresh != null) {
            btnRefresh.setOnAction(e -> {
                if (txtSearch != null && !txtSearch.getText().isBlank()) {
                    loadPhimByKeyword(txtSearch.getText().trim());
                } else {
                    loadPhimDangChieu();
                }
                refreshSuatChieuPagination();
                loadKhachHang(); // full
                loadHoaDon();
            });
        }
    }

    // Dùng cho đăng nhập
    public void setCurrentUser(Integer maNhanVien, Integer maTaiKhoan, String hoTen) {
        this.currentMaNv = maNhanVien;
        this.currentMaTk = maTaiKhoan;
        this.currentHoTen = hoTen;

        if (lblWelcome != null) {
            lblWelcome.setText("🎬 Xin chào, " + (hoTen != null ? hoTen : "Nhân viên") + "!");
        }
        if (mbUser != null) {
            mbUser.setText(hoTen != null ? hoTen : ("NV#" + maNhanVien));
        }
    }

    public void setTenNhanVien(String ten) {
        this.currentHoTen = ten;
        if (lblWelcome != null) {
            lblWelcome.setText("🎬 Xin chào, " + ten + "!");
        }
        if (mbUser != null) {
            mbUser.setText("NV: " + ten);
        }
    }

    private void attachAccelerators(Scene scene) {
        scene.getAccelerators().put(new KeyCodeCombination(KeyCode.F1), () -> safeFire(btnBanVe));
        scene.getAccelerators().put(new KeyCodeCombination(KeyCode.F2), () -> safeFire(btnDoiVe));
        scene.getAccelerators().put(new KeyCodeCombination(KeyCode.F3), () -> safeFire(btnTraVe));
        scene.getAccelerators().put(new KeyCodeCombination(KeyCode.F, KeyCombination.CONTROL_DOWN), () -> {
            if (txtSearch != null) {
                txtSearch.requestFocus();
            }
        });
    }

    private void safeFire(Button b) {
        if (b != null) {
            b.fire();
        }
    }

    // ======================== PHIM ĐANG CHIẾU ========================
    private void loadPhimDangChieu() {
        if (tilePhimDangChieu == null) {
            return;
        }
        tilePhimDangChieu.getChildren().clear();

        LocalDate d = (dpNgay != null && dpNgay.getValue() != null) ? dpNgay.getValue() : LocalDate.now();

        final String sql = """
            SELECT ma_phim, ten_phim, thoi_luong_phut, phan_loai, ngay_phat_hanh, poster_url
            FROM phim
            WHERE ngay_phat_hanh <= ?
            ORDER BY ngay_phat_hanh DESC
        """;

        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(d));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    long maPhim = rs.getLong("ma_phim");
                    String tenPhim = rs.getString("ten_phim");
                    int thoiLuong = rs.getInt("thoi_luong_phut");
                    String phanLoai = Objects.toString(rs.getString("phan_loai"), "");
                    String posterUrl = rs.getString("poster_url");
                    VBox card = createPhimCard(maPhim, tenPhim, thoiLuong, phanLoai, posterUrl);
                    tilePhimDangChieu.getChildren().add(card);
                }
            }
        } catch (SQLException e) {
            showError("Lỗi tải phim đang chiếu", e.getMessage());
        }
    }

    private VBox createPhimCard(long maPhim, String tenPhim, int thoiLuong, String phanLoai, String posterUrl) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(12));
        card.setPrefWidth(260);
        card.setStyle("""
            -fx-background-color: rgba(255,255,255,0.08);
            -fx-background-radius: 12;
            -fx-border-radius: 12;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.25), 10,0,0,4);
        """);

        ImageView img = new ImageView();
        img.setFitWidth(236);
        img.setFitHeight(160);
        img.setPreserveRatio(true);
        img.setSmooth(true);
        img.setCache(true);
        img.setImage(loadPosterSafely(posterUrl));

        Label lblName = new Label(tenPhim);
        lblName.getStyleClass().add("section-title");

        Label lblInfo = new Label("⏱ " + thoiLuong + " phút  |  " + phanLoai);
        lblInfo.setStyle("-fx-text-fill: #caf0f8;");

        Button btnSuat = new Button("🎟️ Xem suất chiếu");
        btnSuat.setStyle("""
            -fx-background-color: linear-gradient(to right, #0077b6, #00b4d8);
            -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8;
        """);
        btnSuat.setOnAction(e -> showSuatChieu(maPhim, tenPhim));

        Tooltip.install(card, new Tooltip("Xem các suất chiếu của phim " + tenPhim));
        card.getChildren().addAll(img, lblName, lblInfo, btnSuat);
        return card;
    }

    private Image loadPosterSafely(String posterUrl) {
        try {
            if (posterUrl != null && !posterUrl.isBlank()) {
                return new Image(posterUrl, true);
            }
        } catch (Exception ignore) {
        }
        try (InputStream is = getClass().getResourceAsStream("/Application/image/null.png")) {
            if (is != null) {
                return new Image(is);
            }
        } catch (Exception ignore) {
        }
        return new Image(Objects.requireNonNull(getClass().getResource("/javafx/scene/control/skin/caspian/dialog-confirm.png")).toExternalForm());
    }

    // =================== SUẤT CHIẾU (theo ngày) ===================
    private void showSuatChieu(long maPhim, String tenPhim) {
        LocalDate d = (dpNgay != null && dpNgay.getValue() != null) ? dpNgay.getValue() : LocalDate.now();

        final String sql = """
            SELECT s.ma_suat_chieu, p.ten_phong,
                   DATE_FORMAT(s.bat_dau_luc, '%H:%i') AS gio,
                   s.gia_co_ban
            FROM suat_chieu s
            JOIN phong p ON s.ma_phong = p.ma_phong
            WHERE s.ma_phim = ? AND DATE(s.bat_dau_luc) = ?
            ORDER BY s.bat_dau_luc
        """;

        StringBuilder info = new StringBuilder("🎞️ Suất chiếu ngày " + d + " – " + tenPhim + "\n\n");

        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, maPhim);
            ps.setDate(2, Date.valueOf(d));
            try (ResultSet rs = ps.executeQuery()) {
                boolean found = false;
                while (rs.next()) {
                    found = true;
                    info.append("🕒 ")
                            .append(rs.getString("gio"))
                            .append("  |  Phòng: ")
                            .append(rs.getString("ten_phong"))
                            .append("  |  Giá: ")
                            .append(rs.getBigDecimal("gia_co_ban"))
                            .append(" VNĐ\n");
                }
                if (!found) {
                    info.append("⚠️ Không có suất chiếu nào trong ngày đã chọn.");
                }
            }
        } catch (SQLException e) {
            info.append("⚠️ Lỗi khi tải dữ liệu suất chiếu.");
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Suất chiếu phim");
        alert.setHeaderText(null);
        alert.setContentText(info.toString());
        alert.showAndWait();
    }

    // =================== TÌM NHANH PHIM ===================
    private void wireSearch() {
        if (txtSearch == null) {
            return;
        }
        txtSearch.textProperty().addListener((obs, o, text) -> {
            loadPhimByKeyword(text == null ? "" : text.trim());
        });
    }

    private void loadPhimByKeyword(String keyword) {
        if (tilePhimDangChieu == null) {
            return;
        }
        tilePhimDangChieu.getChildren().clear();

        final String sql = """
            SELECT ma_phim, ten_phim, thoi_luong_phut, phan_loai, ngay_phat_hanh, poster_url
            FROM phim
            WHERE (? = '' OR ten_phim LIKE CONCAT('%', ?, '%'))
            ORDER BY ngay_phat_hanh DESC
            LIMIT 100
        """;

        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, keyword);
            ps.setString(2, keyword);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    long maPhim = rs.getLong("ma_phim");
                    String tenPhim = rs.getString("ten_phim");
                    int thoiLuong = rs.getInt("thoi_luong_phut");
                    String phanLoai = Objects.toString(rs.getString("phan_loai"), "");
                    String posterUrl = rs.getString("poster_url");
                    tilePhimDangChieu.getChildren().add(
                            createPhimCard(maPhim, tenPhim, thoiLuong, phanLoai, posterUrl)
                    );
                }
            }
        } catch (SQLException e) {
            showError("Lỗi tìm kiếm phim", e.getMessage());
        }
    }

    // =================== SUẤT CHIẾU HÔM NAY ===================
    private void initSuatChieuTable() {
        if (tblSuatChieu == null) {
            return;
        }

        colPhim.setCellValueFactory(d -> d.getValue().tenPhimProperty());
        colPhong.setCellValueFactory(d -> d.getValue().tenPhongProperty());
        colGio.setCellValueFactory(d -> d.getValue().gioProperty());
        colGia.setCellValueFactory(d -> d.getValue().giaProperty());
        colGia.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal v, boolean empty) {
                super.updateItem(v, empty);
                setText(empty || v == null ? null : String.format("%,.0f đ", v));
            }
        });

        if (paginationSuatChieu != null) {
            paginationSuatChieu.setPageFactory(this::createSuatChieuPage);
            paginationSuatChieu.setMaxPageIndicatorCount(10);
            refreshSuatChieuPagination();
        }
    }

    private int countSuatChieuToday() {
        String sql = "SELECT COUNT(*) FROM suat_chieu WHERE DATE(bat_dau_luc)=CURDATE()";
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            showError("Đếm suất chiếu", e.getMessage());
            return 0;
        }
    }

    private void refreshSuatChieuPagination() {
        if (paginationSuatChieu == null) {
            return;
        }
        int total = countSuatChieuToday();
        int pageCount = Math.max(1, (int) Math.ceil(total / (double) PAGE_SIZE_SC));
        paginationSuatChieu.setPageCount(pageCount);
        paginationSuatChieu.setCurrentPageIndex(0);
    }

    private javafx.scene.Node createSuatChieuPage(Integer pageIndex) {
        ObservableList<SuatChieuVM> data = FXCollections.observableArrayList();
        String sql = """
           SELECT p.ten_phim, r.ten_phong,
                  DATE_FORMAT(s.bat_dau_luc,'%H:%i') AS gio, s.gia_co_ban
           FROM suat_chieu s
           JOIN phim p  ON s.ma_phim  = p.ma_phim
           JOIN phong r ON s.ma_phong = r.ma_phong
           WHERE DATE(s.bat_dau_luc)=CURDATE()
           ORDER BY s.bat_dau_luc
           LIMIT ? OFFSET ?
        """;
        int offset = pageIndex * PAGE_SIZE_SC;
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, PAGE_SIZE_SC);
            ps.setInt(2, offset);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    data.add(new SuatChieuVM(
                            rs.getString("ten_phim"),
                            rs.getString("ten_phong"),
                            rs.getString("gio"),
                            rs.getBigDecimal("gia_co_ban")
                    ));
                }
            }
        } catch (SQLException e) {
            showError("Tải suất chiếu", e.getMessage());
        }
        if (tblSuatChieu != null) {
            tblSuatChieu.setItems(data);
        }
        return tblSuatChieu;
    }

    // =================== KHÁCH HÀNG - ENHANCED VERSION ===================
// Add these fields to NhanVienController class
    @FXML
    private Button btnThemKHDialog;

    private void initKhachHangTable() {
        if (tblKhachHang == null) {
            return;
        }

        colMaKH.setCellValueFactory(d -> d.getValue().maProperty());
        colHoTen.setCellValueFactory(d -> d.getValue().hoTenProperty());
        colSDT.setCellValueFactory(d -> d.getValue().sdtProperty());
        colEmail.setCellValueFactory(d -> d.getValue().emailProperty());
        colHangTV.setCellValueFactory(d -> d.getValue().hangProperty());
        colDiem.setCellValueFactory(d -> d.getValue().diemProperty());

        // Add action column for Edit/Delete/Toggle
        addCustomerActionColumn();

        if (cbHangTV != null) {
            cbHangTV.getItems().setAll("BRONZE", "SILVER", "GOLD", "PLATINUM");
        }
        loadKhachHang();
    }

    private void wireKhachHangButtons() {
        if (btnThemKH != null) {
            btnThemKH.setOnAction(e -> showKhachHangDialog(null));
        }
        if (btnThemKHDialog != null) {
            btnThemKHDialog.setOnAction(e -> showKhachHangDialog(null));
        }
    }

// ===== ADD ACTION COLUMN TO CUSTOMER TABLE =====
    private void addCustomerActionColumn() {
        TableColumn<KhachHangVM, Void> colAction = new TableColumn<>("Thao Tác");
        colAction.setPrefWidth(180);
        colAction.setCellFactory(col -> new TableCell<>() {
            private final Button btnEdit = new Button("Sửa");
            private final Button btnToggle = new Button("Bật/Tắt");
            private final Button btnDelete = new Button("Xóa");

            {
                btnEdit.setStyle("-fx-background-color: #2196f3; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 5 10;");
                btnToggle.setStyle("-fx-background-color: #ff9800; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 5 10;");
                btnDelete.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 5 10;");

                btnEdit.setOnAction(e -> {
                    KhachHangVM kh = getTableView().getItems().get(getIndex());
                    showKhachHangDialog(kh);
                });

                btnToggle.setOnAction(e -> {
                    KhachHangVM kh = getTableView().getItems().get(getIndex());
                    toggleKhachHangStatus(kh);
                });

                btnDelete.setOnAction(e -> {
                    KhachHangVM kh = getTableView().getItems().get(getIndex());
                    deleteKhachHang(kh);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    javafx.scene.layout.HBox box = new javafx.scene.layout.HBox(5);
                    box.getChildren().addAll(btnEdit, btnToggle, btnDelete);
                    setGraphic(box);
                }
            }
        });

        if (tblKhachHang != null) {
            tblKhachHang.getColumns().add(colAction);
        }
    }

    private void wireKhachHangSearch() {
        if (txtTimKhach == null) {
            return;
        }
        if (btnTimKhach != null) {
            btnTimKhach.setOnAction(e -> timKhachHang());
        }
        if (btnClearTimKH != null) {
            btnClearTimKH.setOnAction(e -> {
                txtTimKhach.clear();
                loadKhachHang();
            });
        }

        txtTimKhach.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                timKhachHang();
            }
        });
        PauseTransition pt = new PauseTransition(Duration.millis(300));
        txtTimKhach.textProperty().addListener((obs, ov, nv) -> {
            pt.setOnFinished(ev -> timKhachHang());
            pt.playFromStart();
        });
        ContextMenu cm = new ContextMenu();
        MenuItem miClear = new MenuItem("Xoá ô tìm");
        miClear.setOnAction(ev -> {
            txtTimKhach.clear();
            loadKhachHang();
        });
        cm.getItems().add(miClear);
        txtTimKhach.setContextMenu(cm);
    }

    private void loadKhachHang(String keyword) {
        if (tblKhachHang == null) {
            return;
        }
        String sql = """
            SELECT kh.ma_khach_hang,
                   tk.ma_tai_khoan,
                   tk.ho_ten,
                   tk.so_dien_thoai,
                   tk.email,
                   kh.diem_tich_luy,
                   CASE
                       WHEN kh.diem_tich_luy >= 2000 THEN 'PLATINUM'
                       WHEN kh.diem_tich_luy >= 1000 THEN 'GOLD'
                       WHEN kh.diem_tich_luy >= 500  THEN 'SILVER'
                       ELSE 'BRONZE'
                   END AS hang_tv
            FROM khach_hang kh
            JOIN tai_khoan tk ON kh.ma_tai_khoan = tk.ma_tai_khoan
            WHERE (? = '' OR tk.ho_ten LIKE CONCAT('%', ?, '%')
                          OR tk.so_dien_thoai LIKE CONCAT('%', ?, '%')
                          OR tk.email LIKE CONCAT('%', ?, '%'))
            ORDER BY kh.ma_khach_hang DESC
            LIMIT 300
        """;

        ObservableList<KhachHangVM> list = FXCollections.observableArrayList();
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            String k = keyword == null ? "" : keyword.trim();
            ps.setString(1, k);
            ps.setString(2, k);
            ps.setString(3, k);
            ps.setString(4, k);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new KhachHangVM(
                            rs.getInt("ma_khach_hang"),
                            rs.getInt("ma_tai_khoan"),
                            rs.getString("ho_ten"),
                            rs.getString("so_dien_thoai"),
                            rs.getString("email"),
                            rs.getString("hang_tv"),
                            rs.getInt("diem_tich_luy")
                    ));
                }
            }
        } catch (SQLException e) {
            showError("Lỗi tìm khách hàng", e.getMessage());
        }
        tblKhachHang.setItems(list);
    }

    @FXML
    private void timKhachHang() {
        String kw = (txtTimKhach == null) ? "" : String.valueOf(txtTimKhach.getText()).trim();
        if (kw.isEmpty()) {
            loadKhachHang();
        } else {
            loadKhachHang(kw);
        }

    }

// ===== DIALOG FOR ADD/EDIT CUSTOMER =====
    private void showKhachHangDialog(KhachHangVM existingKH) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(existingKH == null ? "➕ Thêm Khách Hàng Mới" : "✏️ Chỉnh Sửa Thông Tin Khách Hàng");
        dialog.setHeaderText(existingKH == null
                ? "Điền thông tin khách hàng mới"
                : "Chỉnh sửa thông tin: " + existingKH.hoTenProperty().get());

        // Create form content
        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        grid.setStyle("-fx-background-color: #f5f5f5;");

        TextField tfHoTen = new TextField();
        TextField tfSDT = new TextField();
        TextField tfEmail = new TextField();
        DatePicker dpNgaySinh = new DatePicker();
        ComboBox<String> cbTrangThai = new ComboBox<>();
        cbTrangThai.getItems().addAll("Hoạt động", "Đang bảo trì");
        cbTrangThai.setValue("Hoạt động");

        PasswordField pfMatKhau = new PasswordField();

        // Style inputs
        String inputStyle = "-fx-background-color: white; -fx-border-color: #ddd; -fx-border-radius: 5; -fx-background-radius: 5; -fx-padding: 8;";
        tfHoTen.setStyle(inputStyle);
        tfSDT.setStyle(inputStyle);
        tfEmail.setStyle(inputStyle);
        dpNgaySinh.setStyle(inputStyle);
        cbTrangThai.setStyle(inputStyle);
        pfMatKhau.setStyle(inputStyle);

        tfHoTen.setPromptText("Nhập họ tên đầy đủ");
        tfSDT.setPromptText("Số điện thoại (10 số)");
        tfEmail.setPromptText("email@example.com");
        pfMatKhau.setPromptText("Mật khẩu (nếu thêm mới)");

        // Labels with icons
        Label lblHoTen = new Label("👤 Họ Tên:");
        Label lblSDT = new Label("📱 Số Điện Thoại:");
        Label lblEmail = new Label("📧 Email:");
        Label lblNgaySinh = new Label("🎂 Ngày Sinh:");
        Label lblTrangThai = new Label("🔘 Trạng Thái:");
        Label lblMatKhau = new Label("🔒 Mật Khẩu:");

        String labelStyle = "-fx-font-weight: bold; -fx-font-size: 13px;";
        lblHoTen.setStyle(labelStyle);
        lblSDT.setStyle(labelStyle);
        lblEmail.setStyle(labelStyle);
        lblNgaySinh.setStyle(labelStyle);
        lblTrangThai.setStyle(labelStyle);
        lblMatKhau.setStyle(labelStyle);

        grid.add(lblHoTen, 0, 0);
        grid.add(tfHoTen, 1, 0);
        grid.add(lblSDT, 0, 1);
        grid.add(tfSDT, 1, 1);
        grid.add(lblEmail, 0, 2);
        grid.add(tfEmail, 1, 2);
        grid.add(lblNgaySinh, 0, 3);
        grid.add(dpNgaySinh, 1, 3);
        grid.add(lblTrangThai, 0, 4);
        grid.add(cbTrangThai, 1, 4);

        if (existingKH == null) {
            grid.add(lblMatKhau, 0, 5);
            grid.add(pfMatKhau, 1, 5);
        }

        // Pre-fill if editing
        if (existingKH != null) {
            tfHoTen.setText(existingKH.hoTenProperty().get());
            tfSDT.setText(existingKH.sdtProperty().get());
            tfEmail.setText(existingKH.emailProperty().get());
            // Load existing date of birth from DB if needed
            cbTrangThai.setValue("Hoạt động"); // Default, adjust based on actual status
        }

        dialog.getDialogPane().setContent(grid);

        // Add buttons
        ButtonType btnSave = new ButtonType("Lưu", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnCancel = new ButtonType("Hủy", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(btnSave, btnCancel);

        // Style buttons
        javafx.scene.Node saveButton = dialog.getDialogPane().lookupButton(btnSave);
        saveButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 8;");

        // Validation
        saveButton.setDisable(true);
        tfHoTen.textProperty().addListener((obs, o, n)
                -> saveButton.setDisable(n.trim().isEmpty() || tfSDT.getText().trim().isEmpty()));
        tfSDT.textProperty().addListener((obs, o, n)
                -> saveButton.setDisable(n.trim().isEmpty() || tfHoTen.getText().trim().isEmpty()));

        dialog.showAndWait().ifPresent(response -> {
            if (response == btnSave) {
                String hoTen = tfHoTen.getText().trim();
                String sdt = tfSDT.getText().trim();
                String email = tfEmail.getText().trim();
                LocalDate ngaySinh = dpNgaySinh.getValue();
                int hoatDong = cbTrangThai.getValue().equals("Hoạt động") ? 1 : 0;
                String matKhau = pfMatKhau.getText().trim();

                if (existingKH == null) {
                    // Add new customer using stored procedure
                    insertKhachHangWithProc(hoTen, email, sdt, ngaySinh, hoatDong, matKhau);
                } else {
                    // Update existing customer
                    updateKhachHangWithProc(existingKH.maProperty().get(), hoTen, email, sdt, ngaySinh, hoatDong);
                }
            }
        });
    }

// ===== INSERT CUSTOMER USING STORED PROCEDURE =====
    private void insertKhachHangWithProc(String hoTen, String email, String sdt, LocalDate ngaySinh, int hoatDong, String matKhau) {
        String sql = "{CALL proc_kh_insert(?, ?, ?, ?, ?, ?,?)}";

        try (Connection c = DBConnection.getConnection(); CallableStatement cs = c.prepareCall(sql)) {

            cs.setString(1, hoTen);
            cs.setString(2, email);
            cs.setString(3, sdt);
            cs.setDate(4, ngaySinh == null ? null : Date.valueOf(ngaySinh));
            cs.setInt(5, hoatDong);
            cs.setString(6, matKhau.isEmpty() ? "123456" : matKhau); // Default password
            cs.registerOutParameter(7, Types.BIGINT);

            cs.execute();
            long newMaKH = cs.getLong(7);

            info("✅ Đã thêm khách hàng thành công! Mã KH: " + newMaKH);
            loadKhachHang();

        } catch (SQLException e) {
            showError("Lỗi thêm khách hàng", e.getMessage());
        }
    }

// ===== UPDATE CUSTOMER USING STORED PROCEDURE =====
    private void updateKhachHangWithProc(int maKH, String hoTen, String email, String sdt, LocalDate ngaySinh, int hoatDong) {
        String sql = "{CALL proc_kh_update(?, ?, ?, ?, ?, ?)}";

        try (Connection c = DBConnection.getConnection(); CallableStatement cs = c.prepareCall(sql)) {

            cs.setInt(1, maKH);
            cs.setString(2, hoTen);
            cs.setString(3, email);
            cs.setString(4, sdt);
            cs.setDate(5, ngaySinh == null ? null : Date.valueOf(ngaySinh));
            cs.setInt(6, hoatDong);

            cs.execute();

            info("✅ Đã cập nhật thông tin khách hàng!");
            loadKhachHang();

        } catch (SQLException e) {
            showError("Lỗi cập nhật khách hàng", e.getMessage());
        }
    }

// ===== TOGGLE CUSTOMER ACTIVE STATUS =====
    private void toggleKhachHangStatus(KhachHangVM kh) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận thay đổi trạng thái");
        confirm.setHeaderText("Bật/Tắt trạng thái hoạt động");
        confirm.setContentText("Bạn có chắc muốn thay đổi trạng thái của khách hàng: " + kh.hoTenProperty().get() + "?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                String sql = "{CALL proc_toggle_hoat_dong_by_ma_kh(?)}";

                try (Connection c = DBConnection.getConnection(); CallableStatement cs = c.prepareCall(sql)) {

                    cs.setInt(1, kh.maProperty().get());
                    cs.execute();

                    info("✅ Đã thay đổi trạng thái khách hàng!");
                    loadKhachHang();

                } catch (SQLException e) {
                    showError("Lỗi thay đổi trạng thái", e.getMessage());
                }
            }
        });
    }

// ===== DELETE CUSTOMER (UPDATED) =====
    private void deleteKhachHang(KhachHangVM kh) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("⚠️ Xác nhận xóa");
        confirm.setHeaderText("Xóa khách hàng");
        confirm.setContentText("Bạn có chắc muốn xóa khách hàng: " + kh.hoTenProperty().get() + "?\nHành động này không thể hoàn tác!");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                String sql = "DELETE FROM khach_hang WHERE ma_khach_hang=?";
                try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
                    ps.setInt(1, kh.maProperty().get());
                    if (ps.executeUpdate() > 0) {
                        info("✅ Đã xóa khách hàng thành công!");
                        loadKhachHang();
                    }
                } catch (SQLException e) {
                    showError("Lỗi xóa khách hàng", e.getMessage());
                }
            }
        });
    }

// ===== LOAD CUSTOMERS (UNCHANGED) =====
    private void loadKhachHang() {
        if (tblKhachHang == null) {
            return;
        }
        String sql = """
        SELECT kh.ma_khach_hang,
               tk.ma_tai_khoan,
               tk.ho_ten,
               tk.so_dien_thoai,
               tk.email,
               kh.diem_tich_luy,
               CASE
                   WHEN kh.diem_tich_luy >= 2000 THEN 'PLATINUM'
                   WHEN kh.diem_tich_luy >= 1000 THEN 'GOLD'
                   WHEN kh.diem_tich_luy >= 500  THEN 'SILVER'
                   ELSE 'BRONZE'
               END AS hang_tv
        FROM khach_hang kh
        JOIN tai_khoan tk ON kh.ma_tai_khoan = tk.ma_tai_khoan
        ORDER BY kh.ma_khach_hang DESC
        LIMIT 300
    """;
        ObservableList<KhachHangVM> list = FXCollections.observableArrayList();
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new KhachHangVM(
                        rs.getInt("ma_khach_hang"),
                        rs.getInt("ma_tai_khoan"),
                        rs.getString("ho_ten"),
                        rs.getString("so_dien_thoai"),
                        rs.getString("email"),
                        rs.getString("hang_tv"),
                        rs.getInt("diem_tich_luy")
                ));
            }
        } catch (SQLException e) {
            showError("Lỗi tải khách hàng", e.getMessage());
        }
        tblKhachHang.setItems(list);
    }
    // =================== HÓA ĐƠN (ĐƠN VÉ + ĐƠN HÀNG) ===================

    private void loadHoaDon() {
        if (tblHoaDon == null) {
            return;
        }

        String sql = """
        SELECT * FROM (
            SELECT 
                'VE' AS loai,
                dv.ma_ve AS ma,
                DATE_FORMAT(sc.bat_dau_luc, '%Y-%m-%d %H:%i') AS ngay,
                '--' AS ten_nhan_vien,
                COALESCE(tk_kh.ho_ten, 'Khách lẻ') AS khach,
                dv.don_gia AS tong_tien
            FROM don_ve dv
            JOIN ve v              ON dv.ma_ve        = v.ma_ve
            JOIN suat_chieu sc     ON v.ma_suat_chieu = sc.ma_suat_chieu
            LEFT JOIN don_hang dh  ON dv.ma_don_hang  = dh.ma_don_hang
            LEFT JOIN khach_hang kh   ON dh.ma_khach_hang = kh.ma_khach_hang
            LEFT JOIN tai_khoan tk_kh ON kh.ma_tai_khoan  = tk_kh.ma_tai_khoan
            WHERE sc.bat_dau_luc >= ? AND sc.bat_dau_luc < DATE_ADD(?, INTERVAL 1 DAY)
              AND (? = '' OR dv.ma_ve = ?)

            UNION ALL

            SELECT
                'HANG' AS loai,
                dh.ma_don_hang AS ma,
                DATE_FORMAT(dh.tao_luc, '%Y-%m-%d %H:%i') AS ngay,
                '--' AS ten_nhan_vien,
                COALESCE(tk_kh.ho_ten, 'Khách lẻ') AS khach,
                dh.tong_tien
            FROM don_hang dh
            LEFT JOIN khach_hang kh   ON dh.ma_khach_hang = kh.ma_khach_hang
            LEFT JOIN tai_khoan tk_kh ON kh.ma_tai_khoan  = tk_kh.ma_tai_khoan
            WHERE dh.tao_luc >= ? AND dh.tao_luc < DATE_ADD(?, INTERVAL 1 DAY)
              AND (? = '' OR dh.ma_don_hang = ?)
        ) t
        ORDER BY ngay DESC
        LIMIT 500
    """;

        ObservableList<HoaDonVM> list = FXCollections.observableArrayList();
        LocalDate from = (dpFrom != null && dpFrom.getValue() != null) ? dpFrom.getValue() : LocalDate.of(2000, 1, 1);
        LocalDate to = (dpTo != null && dpTo.getValue() != null) ? dpTo.getValue() : LocalDate.now();
        String ma = (txtMaHD == null) ? "" : txtMaHD.getText().trim();

        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setTimestamp(1, Timestamp.valueOf(from.atStartOfDay()));
            ps.setDate(2, Date.valueOf(to));
            ps.setString(3, ma);
            ps.setString(4, ma);

            ps.setTimestamp(5, Timestamp.valueOf(from.atStartOfDay()));
            ps.setDate(6, Date.valueOf(to));
            ps.setString(7, ma);
            ps.setString(8, ma);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new HoaDonVM(
                            rs.getInt("ma"),
                            rs.getString("ngay"),
                            rs.getString("ten_nhan_vien"),
                            rs.getString("khach"),
                            rs.getBigDecimal("tong_tien")
                    ));
                }
            }
        } catch (SQLException e) {
            showError("Lỗi tải đơn vé/đơn hàng", e.getMessage());
        }

        tblHoaDon.setItems(list);
    }

    // =================== MENU TÀI KHOẢN ===================
    // =================== ĐĂNG XUẤT ===================
    @FXML
    private void handleDangXuat(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/models/login.fxml"));
            Parent loginView = loader.load();
            Stage stage = (Stage) root.getScene().getWindow();
            stage.setScene(new Scene(loginView));
            stage.centerOnScreen();
            stage.setTitle("🎬 Đăng nhập hệ thống");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Không thể đăng xuất", e.getMessage());
        }
    }

    // =================== TIỆN ÍCH ===================
    private String safe(TextField tf) {
        return tf == null ? "" : tf.getText().trim();
    }

    private int parseIntSafe(TextField tf) {
        try {
            return Integer.parseInt(safe(tf));
        } catch (Exception ex) {
            return 0;
        }
    }

    private void info(String m) {
        new Alert(Alert.AlertType.INFORMATION, m, ButtonType.OK).showAndWait();
    }

    private void warn(String m) {
        new Alert(Alert.AlertType.WARNING, m, ButtonType.OK).showAndWait();
    }

    private void showError(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
    // =================== IN HÓA ĐƠN (PDF) ===================

// =================== IN HÓA ĐƠN (PDF) ===================
    @FXML
    private TableColumn<HoaDonVM, Void> colInHD;

    private void addPrintButtonColumn() {
        if (tblHoaDon == null || colInHD == null) {
            return;
        }

        colInHD.setCellFactory(col -> new TableCell<>() {
            private final Button btnIn = new Button("🖨️ In");

            {
                btnIn.setOnAction(e -> {
                    HoaDonVM hd = getTableView().getItems().get(getIndex());
                    if (hd != null) {
                        try {
                            exportHoaDonPDF(hd);
                            info("✅ Đã xuất: HoadonPDF/HoaDon_" + hd.maProperty().get() + ".pdf");
                        } catch (Exception ex) {
                            showError("Lỗi in hóa đơn", ex.getMessage());
                        }
                    }
                });
                btnIn.setStyle("-fx-background-color: #2196f3; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6;");
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btnIn);
            }
        });
    }

// gọi hàm thêm cột in trong initHoaDonTable()
    private void initHoaDonTable() {
        if (tblHoaDon == null) {
            return;
        }

        colMaHD.setCellValueFactory(d -> d.getValue().maProperty());
        colNgay.setCellValueFactory(d -> d.getValue().ngayProperty());
        colNhanVien.setCellValueFactory(d -> d.getValue().nvProperty());
        colKhach.setCellValueFactory(d -> d.getValue().khProperty());
        colTongTien.setCellValueFactory(d -> d.getValue().tongProperty());
        colTongTien.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal v, boolean empty) {
                super.updateItem(v, empty);
                setText(empty || v == null ? null : String.format("%,.0f đ", v));
            }
        });

        addPrintButtonColumn(); // ✅ Thêm cột “In” tại đây

        LocalDate today = LocalDate.now();
        if (dpFrom != null && dpFrom.getValue() == null) {
            dpFrom.setValue(today);
        }
        if (dpTo != null && dpTo.getValue() == null) {
            dpTo.setValue(today);
        }

        if (btnTraCuuHD != null) {
            btnTraCuuHD.setOnAction(e -> loadHoaDon());
        }
        loadHoaDon();
    }

    private void exportHoaDonPDF(HoaDonVM hd) throws Exception {
        // Tạo thư mục lưu PDF trong dự án
        File dir = new File("HoadonPDF");
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String fileName = "HoadonPDF/HoaDon_" + hd.maProperty().get() + ".pdf";
        Document document = new Document(PageSize.A5, 36, 36, 54, 36);
        PdfWriter.getInstance(document, new FileOutputStream(fileName));
        document.open();

        // Logo
        String logoPath = "src/Application/image/logo.png";
        try {
            com.itextpdf.text.Image logo = com.itextpdf.text.Image.getInstance(logoPath);
            logo.scaleToFit(80, 80);
            logo.setAlignment(Element.ALIGN_LEFT);
            document.add(logo);
        } catch (Exception e) {
            System.out.println("Không tìm thấy logo: " + e.getMessage());
        }

        // Font Unicode
        BaseFont bf = BaseFont.createFont("c:/windows/fonts/arial.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        Font titleFont = new Font(bf, 16, Font.BOLD, BaseColor.BLUE);
        Font headerFont = new Font(bf, 14, Font.BOLD, BaseColor.BLACK);
        Font textFont = new Font(bf, 12, Font.NORMAL, BaseColor.BLACK);
        Font boldFont = new Font(bf, 12, Font.BOLD, BaseColor.BLACK);
        Font smallFont = new Font(bf, 10, Font.NORMAL, BaseColor.DARK_GRAY);

        // Tiêu đề
        Paragraph title = new Paragraph("RẠP CHIẾU PHIM CINEMA 4U\n\nHÓA ĐƠN BÁN HÀNG", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
        document.add(new Paragraph("\n"));

        NumberFormat nf = NumberFormat.getInstance(new Locale("vi", "VN"));

        // Thông tin chung
        PdfPTable infoTable = new PdfPTable(2);
        infoTable.setWidthPercentage(100);
        infoTable.setSpacingBefore(10f);
        infoTable.setSpacingAfter(10f);

        infoTable.addCell(cell("Mã hóa đơn:", boldFont));
        infoTable.addCell(cell(String.valueOf(hd.maProperty().get()), textFont));
        infoTable.addCell(cell("Ngày lập:", boldFont));
        infoTable.addCell(cell(hd.ngayProperty().get(), textFont));
        infoTable.addCell(cell("Khách hàng:", boldFont));
        infoTable.addCell(cell(hd.khProperty().get(), textFont));
        infoTable.addCell(cell("Nhân viên:", boldFont));
        infoTable.addCell(cell(hd.nvProperty().get(), textFont));

        document.add(infoTable);
        document.add(new Paragraph("\n"));

        // ===== CHI TIẾT VÉ PHIM =====
        int maHD = hd.maProperty().get();
        String sqlVe = """
        SELECT p.ten_phim, 
               ph.ten_phong, 
               g.hang_ghe AS hang_ghe, 
               g.so_ghe,
               DATE_FORMAT(sc.bat_dau_luc, '%d/%m/%Y %H:%i') AS gio_chieu,
               dv.don_gia
        FROM don_ve dv
        JOIN ve v ON dv.ma_ve = v.ma_ve
        JOIN ghe g ON v.ma_ghe = g.ma_ghe
        JOIN suat_chieu sc ON v.ma_suat_chieu = sc.ma_suat_chieu
        JOIN phim p ON sc.ma_phim = p.ma_phim
        JOIN phong ph ON sc.ma_phong = ph.ma_phong
        WHERE dv.ma_don_hang = (SELECT ma_don_hang FROM don_ve WHERE ma_ve = ?)
        ORDER BY g.hang_ghe, g.so_ghe
    """;

        BigDecimal tongVe = BigDecimal.ZERO;
        boolean hasVe = false;

        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sqlVe)) {
            ps.setInt(1, maHD);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    hasVe = true;
                    Paragraph veHeader = new Paragraph("CHI TIẾT VÉ XEM PHIM", headerFont);
                    veHeader.setSpacingBefore(5f);
                    document.add(veHeader);

                    PdfPTable veTable = new PdfPTable(new float[]{3f, 2f, 1.5f, 2f});
                    veTable.setWidthPercentage(100);
                    veTable.setSpacingBefore(5f);
                    veTable.setSpacingAfter(10f);

                    // Header
                    veTable.addCell(cellHeader("Phim - Phòng", boldFont));
                    veTable.addCell(cellHeader("Giờ chiếu", boldFont));
                    veTable.addCell(cellHeader("Ghế", boldFont));
                    veTable.addCell(cellHeader("Giá", boldFont));

                    do {
                        String tenPhim = rs.getString("ten_phim");
                        String tenPhong = rs.getString("ten_phong");
                        String gioChieu = rs.getString("gio_chieu");
                        String ghe = rs.getString("hang_ghe") + rs.getInt("so_ghe");
                        BigDecimal gia = rs.getBigDecimal("don_gia");

                        veTable.addCell(cellData(tenPhim + "\n(" + tenPhong + ")", smallFont));
                        veTable.addCell(cellData(gioChieu, smallFont));
                        veTable.addCell(cellData(ghe, smallFont));
                        veTable.addCell(cellData(nf.format(gia) + " đ", smallFont));

                        tongVe = tongVe.add(gia);
                    } while (rs.next());

                    document.add(veTable);

                    // Tổng vé
                    PdfPTable tongVeTable = new PdfPTable(2);
                    tongVeTable.setWidthPercentage(100);
                    tongVeTable.addCell(cellRight("Tổng tiền vé:", boldFont));
                    tongVeTable.addCell(cellData(nf.format(tongVe) + " VNĐ", boldFont));
                    document.add(tongVeTable);
                }
            }
        }

        // ===== CHI TIẾT SẢN PHẨM =====
        String sqlSP = """
        SELECT sp.ten_san_pham,
               cbo.so_luong,
               sp.gia AS don_gia,
               (cbo.so_luong * sp.gia) AS thanh_tien
        FROM combo_chi_tiet cbo
        JOIN san_pham sp ON cbo.ma_san_pham = sp.ma_san_pham
        JOIN don_ve dv ON cbo.ma_combo = (
            SELECT c.ma_combo 
            FROM combo c 
            WHERE c.ma_combo = dv.ma_don_hang
        )
        WHERE dv.ma_ve = ?
        ORDER BY sp.ten_san_pham
    """;

        BigDecimal tongSP = BigDecimal.ZERO;
        boolean hasSP = false;

        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sqlSP)) {
            ps.setInt(1, maHD);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    hasSP = true;
                    document.add(new Paragraph("\n"));
                    Paragraph spHeader = new Paragraph("CHI TIẾT SẢN PHẨM", headerFont);
                    spHeader.setSpacingBefore(5f);
                    document.add(spHeader);

                    PdfPTable spTable = new PdfPTable(new float[]{3f, 1f, 2f, 2f});
                    spTable.setWidthPercentage(100);
                    spTable.setSpacingBefore(5f);
                    spTable.setSpacingAfter(10f);

                    // Header
                    spTable.addCell(cellHeader("Sản phẩm", boldFont));
                    spTable.addCell(cellHeader("SL", boldFont));
                    spTable.addCell(cellHeader("Đơn giá", boldFont));
                    spTable.addCell(cellHeader("Thành tiền", boldFont));

                    do {
                        String tenSP = rs.getString("ten_san_pham");
                        int soLuong = rs.getInt("so_luong");
                        BigDecimal donGia = rs.getBigDecimal("don_gia");
                        BigDecimal thanhTien = rs.getBigDecimal("thanh_tien");

                        spTable.addCell(cellData(tenSP, textFont));
                        spTable.addCell(cellData(String.valueOf(soLuong), textFont));
                        spTable.addCell(cellData(nf.format(donGia) + " đ", textFont));
                        spTable.addCell(cellData(nf.format(thanhTien) + " đ", textFont));

                        tongSP = tongSP.add(thanhTien);
                    } while (rs.next());

                    document.add(spTable);

                    // Tổng sản phẩm
                    PdfPTable tongSPTable = new PdfPTable(2);
                    tongSPTable.setWidthPercentage(100);
                    tongSPTable.addCell(cellRight("Tổng tiền sản phẩm:", boldFont));
                    tongSPTable.addCell(cellData(nf.format(tongSP) + " VNĐ", boldFont));
                    document.add(tongSPTable);
                }
            }
        }

        // ===== TỔNG CỘNG =====
        document.add(new Paragraph("\n"));
        PdfPTable totalTable = new PdfPTable(2);
        totalTable.setWidthPercentage(100);
        totalTable.setSpacingBefore(10f);

        PdfPCell totalLabelCell = new PdfPCell(new Phrase("TỔNG THANH TOÁN:", new Font(bf, 14, Font.BOLD, BaseColor.RED)));
        totalLabelCell.setBorder(Rectangle.TOP);
        totalLabelCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        totalLabelCell.setPaddingTop(8f);
        totalLabelCell.setPaddingBottom(8f);

        PdfPCell totalValueCell = new PdfPCell(new Phrase(nf.format(hd.tongProperty().get()) + " VNĐ", new Font(bf, 14, Font.BOLD, BaseColor.RED)));
        totalValueCell.setBorder(Rectangle.TOP);
        totalValueCell.setPaddingTop(8f);
        totalValueCell.setPaddingBottom(8f);

        totalTable.addCell(totalLabelCell);
        totalTable.addCell(totalValueCell);
        document.add(totalTable);

        // Cảm ơn
        document.add(new Paragraph("\n\n"));
        Paragraph thank = new Paragraph("Cảm ơn quý khách đã ủng hộ rạp!\nHẹn gặp lại quý khách lần sau.", textFont);
        thank.setAlignment(Element.ALIGN_CENTER);
        document.add(thank);

        document.close();
    }

    private PdfPCell cell(String text, Font font) {
        PdfPCell c = new PdfPCell(new Phrase(text, font));
        c.setBorder(Rectangle.NO_BORDER);
        return c;
    }
// ===== PDF cell helpers (header, data-left, data-right) =====

    private PdfPCell cellHeader(String text, Font font) {
        PdfPCell c = new PdfPCell(new Phrase(text == null ? "" : text, font));
        c.setHorizontalAlignment(Element.ALIGN_CENTER);
        c.setVerticalAlignment(Element.ALIGN_MIDDLE);
        c.setBackgroundColor(BaseColor.LIGHT_GRAY);
        c.setPaddingTop(6f);
        c.setPaddingBottom(6f);
        return c;
    }

    private PdfPCell cellData(String text, Font font) {
        PdfPCell c = new PdfPCell(new Phrase(text == null ? "" : text, font));
        c.setBorder(Rectangle.NO_BORDER);
        c.setHorizontalAlignment(Element.ALIGN_LEFT);
        c.setPaddingTop(4f);
        c.setPaddingBottom(4f);
        return c;
    }

    private PdfPCell cellRight(String text, Font font) {
        PdfPCell c = cellData(text, font);
        c.setHorizontalAlignment(Element.ALIGN_RIGHT);
        return c;
    }

// (tuỳ chọn) overload tiện cho số
    private PdfPCell cellData(Number n, Font font, NumberFormat nf) {
        return cellData(n == null ? "" : nf.format(n), font);
    }

    private PdfPCell cellRight(Number n, Font font, NumberFormat nf) {
        return cellRight(n == null ? "" : nf.format(n), font);
    }

}
