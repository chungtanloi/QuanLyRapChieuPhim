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
    @FXML private BorderPane root;
    @FXML private Label lblWelcome;
    @FXML private Label lblClock;

    // ===== App bar / filter nhanh =====
    @FXML private TextField txtSearch;
    @FXML private DatePicker dpNgay;
    @FXML private ComboBox<String> cbRap;
    @FXML private ComboBox<String> cbPhong;

    @FXML private Button btnBanVe, btnDoiVe, btnTraVe, btnRefresh;
    @FXML private MenuButton mbUser;
    @FXML private MenuItem miProfile, miChangePwd, miLogout;

    // ===== Tabs / vùng nội dung =====
    @FXML private TabPane mainTabs;

    // Tab Phim đang chiếu
    @FXML private TilePane tilePhimDangChieu;

    // ===== Suất chiếu hôm nay (Table + Pagination) =====
    @FXML private TableView<SuatChieuVM> tblSuatChieu;
    @FXML private TableColumn<SuatChieuVM, String> colPhim;
    @FXML private TableColumn<SuatChieuVM, String> colPhong;
    @FXML private TableColumn<SuatChieuVM, String> colGio;
    @FXML private TableColumn<SuatChieuVM, BigDecimal> colGia;
    @FXML private Pagination paginationSuatChieu;

    // ===== Khách hàng (CRUD + SEARCH) =====
    @FXML private TableView<KhachHangVM> tblKhachHang;
    @FXML private TableColumn<KhachHangVM, Number> colMaKH;
    @FXML private TableColumn<KhachHangVM, String> colHoTen;
    @FXML private TableColumn<KhachHangVM, String> colSDT;
    @FXML private TableColumn<KhachHangVM, String> colEmail;
    @FXML private TableColumn<KhachHangVM, String> colHangTV;
    @FXML private TableColumn<KhachHangVM, Number> colDiem;

    @FXML private TextField txtHoTenKH, txtSdtKH, txtEmailKH, txtDiem;

    // >>> Thêm ô TÌM KIẾM khách hàng
    @FXML private TextField txtTimKhach;
    @FXML private Button btnTimKhach, btnClearTimKH;

    @FXML private ComboBox<String> cbHangTV;
    @FXML private Button btnThemKH, btnSuaKH, btnXoaKH;

    // ===== Hóa đơn (đơn vé + đơn hàng) =====
    @FXML private DatePicker dpFrom, dpTo;
    @FXML private TextField txtMaHD;
    @FXML private Button btnTraCuuHD;
    @FXML private TableView<HoaDonVM> tblHoaDon;
    @FXML private TableColumn<HoaDonVM, Number> colMaHD;
    @FXML private TableColumn<HoaDonVM, String> colNgay;
    @FXML private TableColumn<HoaDonVM, String> colNhanVien;
    @FXML private TableColumn<HoaDonVM, String> colKhach;
    @FXML private TableColumn<HoaDonVM, BigDecimal> colTongTien;

    // ===== Session / user hiện tại =====
    private Integer currentMaNv;   // nhan_vien.ma_nhan_vien
    private Integer currentMaTk;   // tai_khoan.ma_tai_khoan
    private String  currentHoTen;

    private static final int PAGE_SIZE_SC = 12;

    // ====== ViewModels ======
    public static class SuatChieuVM {
        private final StringProperty tenPhim = new SimpleStringProperty();
        private final StringProperty tenPhong = new SimpleStringProperty();
        private final StringProperty gio = new SimpleStringProperty();
        private final ObjectProperty<BigDecimal> gia = new SimpleObjectProperty<>();
        public SuatChieuVM(String phim, String phong, String gio, BigDecimal gia) {
            this.tenPhim.set(phim); this.tenPhong.set(phong); this.gio.set(gio); this.gia.set(gia);
        }
        public StringProperty tenPhimProperty(){ return tenPhim; }
        public StringProperty tenPhongProperty(){ return tenPhong; }
        public StringProperty gioProperty(){ return gio; }
        public ObjectProperty<BigDecimal> giaProperty(){ return gia; }
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
            this.ma.set(maKh); this.maTaiKhoan.set(maTk);
            this.hoTen.set(hoTen); this.sdt.set(sdt); this.email.set(email);
            this.hang.set(hang); this.diem.set(diem);
        }
        public IntegerProperty maProperty(){ return ma; }
        public IntegerProperty maTaiKhoanProperty(){ return maTaiKhoan; }
        public StringProperty hoTenProperty(){ return hoTen; }
        public StringProperty sdtProperty(){ return sdt; }
        public StringProperty emailProperty(){ return email; }
        public StringProperty hangProperty(){ return hang; }
        public IntegerProperty diemProperty(){ return diem; }
    }

    public static class HoaDonVM {
        private final IntegerProperty ma = new SimpleIntegerProperty();
        private final StringProperty ngay = new SimpleStringProperty();
        private final StringProperty nv = new SimpleStringProperty();
        private final StringProperty kh = new SimpleStringProperty();
        private final ObjectProperty<BigDecimal> tong = new SimpleObjectProperty<>();
        public HoaDonVM(int ma, String ngay, String nv, String kh, BigDecimal tong) {
            this.ma.set(ma); this.ngay.set(ngay); this.nv.set(nv); this.kh.set(kh); this.tong.set(tong);
        }
        public IntegerProperty maProperty(){ return ma; }
        public StringProperty ngayProperty(){ return ngay; }
        public StringProperty nvProperty(){ return nv; }
        public StringProperty khProperty(){ return kh; }
        public ObjectProperty<BigDecimal> tongProperty(){ return tong; }
    }

    // ====== Lifecycle ======
    @FXML
    private void initialize() {
        if (dpNgay != null) dpNgay.setValue(LocalDate.now());
        if (miLogout != null) miLogout.setOnAction(this::handleDangXuat);

        Platform.runLater(() -> {
            if (root != null && root.getScene() != null) attachAccelerators(root.getScene());
        });

        wireSearch();
        if (txtSearch != null && !txtSearch.getText().isBlank()) {
            loadPhimByKeyword(txtSearch.getText().trim());
        } else {
            loadPhimDangChieu();
        }

        if (dpNgay != null) {
            dpNgay.valueProperty().addListener((obs, o, n) -> {
                if (txtSearch != null && !txtSearch.getText().isBlank())
                    loadPhimByKeyword(txtSearch.getText().trim());
                else
                    loadPhimDangChieu();
                refreshSuatChieuPagination();
            });
        }

        initSuatChieuTable();

        initKhachHangTable();
        wireKhachHangButtons();
        wireKhachHangSearch(); // bật tìm kiếm khách hàng

        initHoaDonTable();
        wireAccountMenu();

        if (btnRefresh != null) btnRefresh.setOnAction(e -> {
            if (txtSearch != null && !txtSearch.getText().isBlank())
                loadPhimByKeyword(txtSearch.getText().trim());
            else
                loadPhimDangChieu();
            refreshSuatChieuPagination();
            loadKhachHang(); // full
            loadHoaDon();
        });
    }

    // Dùng cho đăng nhập
    public void setCurrentUser(Integer maNhanVien, Integer maTaiKhoan, String hoTen) {
        this.currentMaNv = maNhanVien;
        this.currentMaTk = maTaiKhoan;
        this.currentHoTen = hoTen;

        if (lblWelcome != null) lblWelcome.setText("🎬 Xin chào, " + (hoTen != null ? hoTen : "Nhân viên") + "!");
        if (mbUser != null) mbUser.setText(hoTen != null ? hoTen : ("NV#" + maNhanVien));
    }

    public void setTenNhanVien(String ten) {
        this.currentHoTen = ten;
        if (lblWelcome != null) lblWelcome.setText("🎬 Xin chào, " + ten + "!");
        if (mbUser != null) mbUser.setText("NV: " + ten);
    }

    private void attachAccelerators(Scene scene) {
        scene.getAccelerators().put(new KeyCodeCombination(KeyCode.F1), () -> safeFire(btnBanVe));
        scene.getAccelerators().put(new KeyCodeCombination(KeyCode.F2), () -> safeFire(btnDoiVe));
        scene.getAccelerators().put(new KeyCodeCombination(KeyCode.F3), () -> safeFire(btnTraVe));
        scene.getAccelerators().put(new KeyCodeCombination(KeyCode.F, KeyCombination.CONTROL_DOWN), () -> {
            if (txtSearch != null) txtSearch.requestFocus();
        });
    }
    private void safeFire(Button b) { if (b != null) b.fire(); }

    // ======================== PHIM ĐANG CHIẾU ========================
    private void loadPhimDangChieu() {
        if (tilePhimDangChieu == null) return;
        tilePhimDangChieu.getChildren().clear();

        LocalDate d = (dpNgay != null && dpNgay.getValue() != null) ? dpNgay.getValue() : LocalDate.now();

        final String sql = """
            SELECT ma_phim, ten_phim, thoi_luong_phut, phan_loai, ngay_phat_hanh, poster_url
            FROM phim
            WHERE ngay_phat_hanh <= ?
            ORDER BY ngay_phat_hanh DESC
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(d));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    long maPhim   = rs.getLong("ma_phim");
                    String tenPhim = rs.getString("ten_phim");
                    int thoiLuong  = rs.getInt("thoi_luong_phut");
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
        } catch (Exception ignore) { }
        try (InputStream is = getClass().getResourceAsStream("/Application/image/null.png")) {
            if (is != null) return new Image(is);
        } catch (Exception ignore) { }
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

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
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
                if (!found) info.append("⚠️ Không có suất chiếu nào trong ngày đã chọn.");
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
        if (txtSearch == null) return;
        txtSearch.textProperty().addListener((obs, o, text) -> {
            loadPhimByKeyword(text == null ? "" : text.trim());
        });
    }

    private void loadPhimByKeyword(String keyword) {
        if (tilePhimDangChieu == null) return;
        tilePhimDangChieu.getChildren().clear();

        final String sql = """
            SELECT ma_phim, ten_phim, thoi_luong_phut, phan_loai, ngay_phat_hanh, poster_url
            FROM phim
            WHERE (? = '' OR ten_phim LIKE CONCAT('%', ?, '%'))
            ORDER BY ngay_phat_hanh DESC
            LIMIT 100
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
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
        if (tblSuatChieu == null) return;

        colPhim.setCellValueFactory(d -> d.getValue().tenPhimProperty());
        colPhong.setCellValueFactory(d -> d.getValue().tenPhongProperty());
        colGio.setCellValueFactory(d -> d.getValue().gioProperty());
        colGia.setCellValueFactory(d -> d.getValue().giaProperty());
        colGia.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(BigDecimal v, boolean empty) {
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
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            showError("Đếm suất chiếu", e.getMessage());
            return 0;
        }
    }

    private void refreshSuatChieuPagination() {
        if (paginationSuatChieu == null) return;
        int total = countSuatChieuToday();
        int pageCount = Math.max(1, (int)Math.ceil(total / (double) PAGE_SIZE_SC));
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
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
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
        } catch (SQLException e) { showError("Tải suất chiếu", e.getMessage()); }
        if (tblSuatChieu != null) tblSuatChieu.setItems(data);
        return tblSuatChieu;
    }

    // =================== KHÁCH HÀNG ===================
    private void initKhachHangTable() {
        if (tblKhachHang == null) return;

        colMaKH.setCellValueFactory(d -> d.getValue().maProperty());
        colHoTen.setCellValueFactory(d -> d.getValue().hoTenProperty());
        colSDT.setCellValueFactory(d -> d.getValue().sdtProperty());
        colEmail.setCellValueFactory(d -> d.getValue().emailProperty());
        colHangTV.setCellValueFactory(d -> d.getValue().hangProperty());
        colDiem.setCellValueFactory(d -> d.getValue().diemProperty());

        if (cbHangTV != null) cbHangTV.getItems().setAll("BRONZE","SILVER","GOLD","PLATINUM");
        loadKhachHang();
    }

    private void wireKhachHangButtons() {
        if (btnThemKH != null) btnThemKH.setOnAction(e -> insertKhachHang());
        if (btnSuaKH  != null) btnSuaKH.setOnAction(e -> updateKhachHang());
        if (btnXoaKH  != null) btnXoaKH.setOnAction(e -> deleteKhachHang());
    }

    // >>> Tìm kiếm khách hàng
    private void wireKhachHangSearch(){
        if (txtTimKhach == null) return;
        if (btnTimKhach != null) btnTimKhach.setOnAction(e -> timKhachHang());
        if (btnClearTimKH != null) btnClearTimKH.setOnAction(e -> { txtTimKhach.clear(); loadKhachHang(); });

        txtTimKhach.setOnKeyPressed(e -> { if (e.getCode() == KeyCode.ENTER) timKhachHang(); });
        PauseTransition pt = new PauseTransition(Duration.millis(300));
        txtTimKhach.textProperty().addListener((obs, ov, nv) -> {
            pt.setOnFinished(ev -> timKhachHang());
            pt.playFromStart();
        });
        ContextMenu cm = new ContextMenu();
        MenuItem miClear = new MenuItem("Xoá ô tìm");
        miClear.setOnAction(ev -> { txtTimKhach.clear(); loadKhachHang(); });
        cm.getItems().add(miClear);
        txtTimKhach.setContextMenu(cm);
    }

    private void loadKhachHang() {
        if (tblKhachHang == null) return;
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
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
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
        } catch (SQLException e) { showError("Lỗi tải khách hàng", e.getMessage()); }
        tblKhachHang.setItems(list);
    }

    private void loadKhachHang(String keyword) {
        if (tblKhachHang == null) return;
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
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
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
        } catch (SQLException e) { showError("Lỗi tìm khách hàng", e.getMessage()); }
        tblKhachHang.setItems(list);
    }

    @FXML private void timKhachHang(){
        String kw = (txtTimKhach == null) ? "" : txtTimKhach.getText();
        if (kw == null || kw.isBlank()) loadKhachHang();
        else loadKhachHang(kw);
    }

    private void insertKhachHang() {
        String sqlTk  = "INSERT INTO tai_khoan(ho_ten, so_dien_thoai, email, vai_tro, trang_thai) VALUES (?,?,?,?,?)";
        String sqlKh  = "INSERT INTO khach_hang(ma_tai_khoan, diem_tich_luy) VALUES (?,?)";

        try (Connection c = DBConnection.getConnection()) {
            c.setAutoCommit(false);

            int maTaiKhoan;
            try (PreparedStatement ps = c.prepareStatement(sqlTk, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, safe(txtHoTenKH));
                ps.setString(2, safe(txtSdtKH));
                ps.setString(3, safe(txtEmailKH));
                ps.setString(4, "KHACH");
                ps.setString(5, "ACTIVE");
                ps.executeUpdate();
                try (ResultSet gk = ps.getGeneratedKeys()) {
                    if (!gk.next()) throw new SQLException("Không lấy được khóa chính tai_khoan");
                    maTaiKhoan = gk.getInt(1);
                }
            }

            try (PreparedStatement ps = c.prepareStatement(sqlKh)) {
                ps.setInt(1, maTaiKhoan);
                ps.setInt(2, parseIntSafe(txtDiem));
                ps.executeUpdate();
            }

            c.commit();
            info("Đã thêm khách hàng");
            loadKhachHang();
        } catch (SQLException e) { showError("Thêm khách hàng", e.getMessage()); }
    }

    private void updateKhachHang() {
        KhachHangVM sel = tblKhachHang.getSelectionModel().getSelectedItem();
        if (sel == null) { warn("Chọn một dòng để sửa"); return; }

        String sqlTk = "UPDATE tai_khoan SET ho_ten=?, so_dien_thoai=?, email=? WHERE ma_tai_khoan=?";
        String sqlKh = "UPDATE khach_hang SET diem_tich_luy=? WHERE ma_khach_hang=?";

        try (Connection c = DBConnection.getConnection()) {
            c.setAutoCommit(false);

            try (PreparedStatement ps = c.prepareStatement(sqlTk)) {
                ps.setString(1, safe(txtHoTenKH));
                ps.setString(2, safe(txtSdtKH));
                ps.setString(3, safe(txtEmailKH));
                ps.setInt(4, sel.maTaiKhoanProperty().get());
                ps.executeUpdate();
            }
            try (PreparedStatement ps = c.prepareStatement(sqlKh)) {
                ps.setInt(1, parseIntSafe(txtDiem));
                ps.setInt(2, sel.maProperty().get());
                ps.executeUpdate();
            }

            c.commit();
            info("Đã cập nhật khách hàng");
            loadKhachHang();
        } catch (SQLException e) { showError("Sửa khách hàng", e.getMessage()); }
    }

    private void deleteKhachHang() {
        KhachHangVM sel = tblKhachHang.getSelectionModel().getSelectedItem();
        if (sel == null) { warn("Chọn một dòng để xoá"); return; }
        String sql = "DELETE FROM khach_hang WHERE ma_khach_hang=?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, sel.maProperty().get());
            if (ps.executeUpdate() > 0) { info("Đã xoá khách hàng"); loadKhachHang(); }
        } catch (SQLException e) { showError("Xoá khách hàng", e.getMessage()); }
    }

    // =================== HÓA ĐƠN (ĐƠN VÉ + ĐƠN HÀNG) ===================
    

private void loadHoaDon() {
    if (tblHoaDon == null) return;

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
    LocalDate from = (dpFrom != null && dpFrom.getValue() != null) ? dpFrom.getValue() : LocalDate.of(2000,1,1);
    LocalDate to   = (dpTo   != null && dpTo.getValue()   != null) ? dpTo.getValue()   : LocalDate.now();
    String ma = (txtMaHD == null) ? "" : txtMaHD.getText().trim();

    try (Connection c = DBConnection.getConnection();
         PreparedStatement ps = c.prepareStatement(sql)) {

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
    private void wireAccountMenu() {
        if (miProfile != null) miProfile.setOnAction(e -> openProfileDialog());
        if (miChangePwd != null) miChangePwd.setOnAction(e -> openChangePwdDialog());
    }

    private void openProfileDialog() {
        if (currentMaNv == null) { warn("Chưa xác định nhân viên đang đăng nhập"); return; }

        String sql = """
            SELECT tk.ho_ten, tk.email, tk.so_dien_thoai, tk.vai_tro
            FROM nhan_vien nv
            JOIN tai_khoan tk ON nv.ma_tai_khoan = tk.ma_tai_khoan
            WHERE nv.ma_nhan_vien = ?
        """;
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, currentMaNv);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Alert a = new Alert(Alert.AlertType.INFORMATION);
                    a.setTitle("Hồ sơ nhân viên");
                    a.setHeaderText(rs.getString("ho_ten"));
                    a.setContentText("""
                            Email: %s
                            SĐT: %s
                            Vai trò: %s
                            """.formatted(
                            rs.getString("email"),
                            rs.getString("so_dien_thoai"),
                            rs.getString("vai_tro")
                    ));
                    a.showAndWait();
                }
            }
        } catch (SQLException e) { showError("Lỗi xem hồ sơ", e.getMessage()); }
    }

    private void openChangePwdDialog() {
        if (currentMaTk == null) { warn("Chưa xác định tài khoản đang đăng nhập"); return; }

        Dialog<Pair<String,String>> dlg = new Dialog<>();
        dlg.setTitle("Đổi mật khẩu");
        PasswordField oldPw = new PasswordField(); oldPw.setPromptText("Mật khẩu hiện tại");
        PasswordField newPw = new PasswordField(); newPw.setPromptText("Mật khẩu mới");
        VBox box = new VBox(10, new Label("Nhập mật khẩu:"), oldPw, newPw);
        box.setPadding(new Insets(10));
        dlg.getDialogPane().setContent(box);
        dlg.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dlg.setResultConverter(bt -> bt==ButtonType.OK ? new Pair<>(oldPw.getText(), newPw.getText()) : null);

        var res = dlg.showAndWait();
        if (res.isEmpty()) return;

        String sqlCheck  = "SELECT 1 FROM tai_khoan WHERE ma_tai_khoan=? AND mat_khau=SHA2(?,256)";
        String sqlUpdate = "UPDATE tai_khoan SET mat_khau=SHA2(?,256) WHERE ma_tai_khoan=?";
        try (Connection c = DBConnection.getConnection()) {
            try (PreparedStatement ps = c.prepareStatement(sqlCheck)) {
                ps.setInt(1, currentMaTk);
                ps.setString(2, res.get().getKey());
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) { warn("Mật khẩu hiện tại không đúng"); return; }
                }
            }
            try (PreparedStatement ps = c.prepareStatement(sqlUpdate)) {
                ps.setString(1, res.get().getValue());
                ps.setInt(2, currentMaTk);
                if (ps.executeUpdate() > 0) info("Đổi mật khẩu thành công");
            }
        } catch (SQLException e) { showError("Đổi mật khẩu", e.getMessage()); }
    }

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
    private String safe(TextField tf) { return tf == null ? "" : tf.getText().trim(); }
    private int parseIntSafe(TextField tf) { try { return Integer.parseInt(safe(tf)); } catch(Exception ex){ return 0; } }
    private void info(String m){ new Alert(Alert.AlertType.INFORMATION, m, ButtonType.OK).showAndWait(); }
    private void warn(String m){ new Alert(Alert.AlertType.WARNING, m, ButtonType.OK).showAndWait(); }

    private void showError(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
    // =================== IN HÓA ĐƠN (PDF) ===================

// =================== IN HÓA ĐƠN (PDF) ===================
@FXML private TableColumn<HoaDonVM, Void> colInHD;

private void addPrintButtonColumn() {
    if (tblHoaDon == null || colInHD == null) return;

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
    if (tblHoaDon == null) return;

    colMaHD.setCellValueFactory(d -> d.getValue().maProperty());
    colNgay.setCellValueFactory(d -> d.getValue().ngayProperty());
    colNhanVien.setCellValueFactory(d -> d.getValue().nvProperty());
    colKhach.setCellValueFactory(d -> d.getValue().khProperty());
    colTongTien.setCellValueFactory(d -> d.getValue().tongProperty());
    colTongTien.setCellFactory(tc -> new TableCell<>() {
        @Override protected void updateItem(BigDecimal v, boolean empty) {
            super.updateItem(v, empty);
            setText(empty || v == null ? null : String.format("%,.0f đ", v));
        }
    });

    addPrintButtonColumn(); // ✅ Thêm cột “In” tại đây

    LocalDate today = LocalDate.now();
    if (dpFrom != null && dpFrom.getValue() == null) dpFrom.setValue(today);
    if (dpTo   != null && dpTo.getValue()   == null) dpTo.setValue(today);

    if (btnTraCuuHD != null) btnTraCuuHD.setOnAction(e -> loadHoaDon());
    loadHoaDon();
}


private void exportHoaDonPDF(HoaDonVM hd) throws Exception {
    // Tạo thư mục lưu PDF trong dự án
    File dir = new File("HoadonPDF");
    if (!dir.exists()) dir.mkdirs();

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
    Font textFont = new Font(bf, 12, Font.NORMAL, BaseColor.BLACK);

    // Tiêu đề
    Paragraph title = new Paragraph("RẠP CHIẾU PHIM CINEMA 4U\n\nHÓA ĐƠN BÁN HÀNG", titleFont);
    title.setAlignment(Element.ALIGN_CENTER);
    document.add(title);
    document.add(new Paragraph("\n"));

    // Nội dung hóa đơn
    NumberFormat nf = NumberFormat.getInstance(new Locale("vi", "VN"));
    PdfPTable table = new PdfPTable(2);
    table.setWidthPercentage(100);
    table.setSpacingBefore(10f);
    table.setSpacingAfter(10f);

    table.addCell(cell("Mã hóa đơn:", textFont));
    table.addCell(cell(String.valueOf(hd.maProperty().get()), textFont));
    table.addCell(cell("Ngày lập:", textFont));
    table.addCell(cell(hd.ngayProperty().get(), textFont));
    table.addCell(cell("Khách hàng:", textFont));
    table.addCell(cell(hd.khProperty().get(), textFont));
    table.addCell(cell("Nhân viên:", textFont));
    table.addCell(cell(hd.nvProperty().get(), textFont));
    table.addCell(cell("Tổng tiền:", textFont));
    table.addCell(cell(nf.format(hd.tongProperty().get()) + " VNĐ", textFont));

    document.add(table);

    // Cảm ơn
    Paragraph thank = new Paragraph("💖 Cảm ơn quý khách đã ủng hộ rạp!\nHẹn gặp lại quý khách lần sau.", textFont);
    thank.setAlignment(Element.ALIGN_CENTER);
    document.add(thank);

    document.close();
}

private PdfPCell cell(String text, Font font) {
    PdfPCell c = new PdfPCell(new Phrase(text, font));
    c.setBorder(Rectangle.NO_BORDER);
    return c;
}

}
