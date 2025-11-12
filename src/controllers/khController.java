package controllers;

import database.DBConnection;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;

// ===== THƯ VIỆN XUẤT PDF - IMPORT CỤ THỂ =====
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

// ===== IMPORT RÕ RÀNG ĐỂ TRÁNH XUNG ĐỘT =====
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;


// ===== THÊM CÁC IMPORT CÒN THIẾU =====
import java.io.File;
import java.io.FileOutputStream;
import com.itextpdf.text.BaseColor;
public class khController implements Initializable {

    // ===== THÔNG TIN KHÁCH HÀNG =====
    private Integer currentMaKhachHang;
    private String currentHoTen;
    private String currentEmail;
    private String currentHangTV;
    private Integer currentDiemTichLuy;

    // ===== TAB XEM PHIM & SUẤT CHIẾU =====
    @FXML private TilePane tilePhimDangChieu;
    @FXML private ComboBox<String> cbRap, cbNgayChieu;
    @FXML private TableView<SuatChieuVM> tblSuatChieu;
    @FXML private TableColumn<SuatChieuVM, String> colPhim, colPhong, colGio, colDinhDang;
    @FXML private TableColumn<SuatChieuVM, BigDecimal> colGia;
    @FXML private TableColumn<SuatChieuVM, Void> colChonSuat;

    // ===== TAB ĐẶT VÉ =====
    @FXML private Label lblTenPhimDat, lblPhongDat, lblGioDat, lblTongTien;
    @FXML private TilePane containerGhe;
    
    // SỬA: IMPORT RÕ RÀNG TextField từ JavaFX
    @FXML private javafx.scene.control.TextField txtMaKhuyenMai;
    
    @FXML private Label lblThongBaoKM;
    @FXML private Button btnApDungKM, btnDatVe, btnXuatHD;
    
    // ===== TAB COMBO =====
    @FXML private TableView<ComboVM> tblCombo;
    @FXML private TableColumn<ComboVM, String> colTenCombo;
    @FXML private TableColumn<ComboVM, BigDecimal> colGiaCombo;
    @FXML private TableColumn<ComboVM, String> colMoTaCombo;
    @FXML private TableColumn<ComboVM, Void> colChonCombo;
    
    @FXML private TableView<ComboChiTietVM> tblComboChiTiet;
    @FXML private TableColumn<ComboChiTietVM, String> colSanPham;
    @FXML private TableColumn<ComboChiTietVM, Integer> colSoLuong;
    @FXML private TableColumn<ComboChiTietVM, BigDecimal> colGiaSanPham;

    // ===== TAB LỊCH SỬ =====
    @FXML private TableView<VeDaDatVM> tblLichSu;
    @FXML private TableColumn<VeDaDatVM, String> colPhimLS, colPhongLS, colGheLS, colNgayLS;
    @FXML private TableColumn<VeDaDatVM, BigDecimal> colGiaLS;
    @FXML private TableColumn<VeDaDatVM, Void> colInHDLS;

    // ===== TAB THÔNG TIN CÁ NHÂN =====
    @FXML private Label lblHoTen, lblEmail, lblSdt, lblHangTV, lblDiem;
    
    // SỬA: IMPORT RÕ RÀNG TextField từ JavaFX
    @FXML private javafx.scene.control.TextField txtHoTen, txtEmail, txtSdt;
    
    @FXML private DatePicker dpNgaySinh;
    @FXML private Button btnSuaThongTin, btnDoiMatKhau;

    // ===== BIẾN TẠM =====
    private Map<String, Button> gheButtons = new HashMap<>();
    private java.util.List<Integer> gheDaChon = new ArrayList<>(); // SỬA: java.util.List
    private java.util.List<Integer> comboDaChon = new ArrayList<>(); // SỬA: java.util.List
    private BigDecimal tongTienVé = BigDecimal.ZERO;
    private BigDecimal tongTienCombo = BigDecimal.ZERO;
    private BigDecimal giamGiaKM = BigDecimal.ZERO;
    private Long maSuatChieuDangChon;
    private String maKhuyenMaiDangApDung;

    // ===== VIEW MODELS (GIỮ NGUYÊN) =====
    public static class SuatChieuVM {
        private final LongProperty maSuatChieu = new SimpleLongProperty();
        private final StringProperty tenPhim = new SimpleStringProperty();
        private final StringProperty tenPhong = new SimpleStringProperty();
        private final StringProperty dinhDang = new SimpleStringProperty();
        private final StringProperty gioChieu = new SimpleStringProperty();
        private final ObjectProperty<BigDecimal> giaVe = new SimpleObjectProperty<>();
        
        public SuatChieuVM(long maSuatChieu, String tenPhim, String tenPhong, String dinhDang, String gioChieu, BigDecimal giaVe) {
            this.maSuatChieu.set(maSuatChieu);
            this.tenPhim.set(tenPhim);
            this.tenPhong.set(tenPhong);
            this.dinhDang.set(dinhDang);
            this.gioChieu.set(gioChieu);
            this.giaVe.set(giaVe);
        }
        
        public long getMaSuatChieu() { return maSuatChieu.get(); }
        public String getTenPhim() { return tenPhim.get(); }
        public String getTenPhong() { return tenPhong.get(); }
        public String getDinhDang() { return dinhDang.get(); }
        public String getGioChieu() { return gioChieu.get(); }
        public BigDecimal getGiaVe() { return giaVe.get(); }
        
        public LongProperty maSuatChieuProperty() { return maSuatChieu; }
        public StringProperty tenPhimProperty() { return tenPhim; }
        public StringProperty tenPhongProperty() { return tenPhong; }
        public StringProperty dinhDangProperty() { return dinhDang; }
        public StringProperty gioChieuProperty() { return gioChieu; }
        public ObjectProperty<BigDecimal> giaVeProperty() { return giaVe; }
    }

    public static class ComboVM {
        private final IntegerProperty maCombo = new SimpleIntegerProperty();
        private final StringProperty tenCombo = new SimpleStringProperty();
        private final ObjectProperty<BigDecimal> giaCombo = new SimpleObjectProperty<>();
        private final StringProperty moTa = new SimpleStringProperty();
        
        public ComboVM(int maCombo, String tenCombo, BigDecimal giaCombo, String moTa) {
            this.maCombo.set(maCombo);
            this.tenCombo.set(tenCombo);
            this.giaCombo.set(giaCombo);
            this.moTa.set(moTa);
        }
        
        public IntegerProperty maComboProperty() { return maCombo; }
        public StringProperty tenComboProperty() { return tenCombo; }
        public ObjectProperty<BigDecimal> giaComboProperty() { return giaCombo; }
        public StringProperty moTaProperty() { return moTa; }
    }

    public static class ComboChiTietVM {
        private final StringProperty tenSanPham = new SimpleStringProperty();
        private final StringProperty loaiSanPham = new SimpleStringProperty();
        private final IntegerProperty soLuong = new SimpleIntegerProperty();
        private final ObjectProperty<BigDecimal> giaSanPham = new SimpleObjectProperty<>();
        
        public ComboChiTietVM(String tenSanPham, String loaiSanPham, int soLuong, BigDecimal giaSanPham) {
            this.tenSanPham.set(tenSanPham);
            this.loaiSanPham.set(loaiSanPham);
            this.soLuong.set(soLuong);
            this.giaSanPham.set(giaSanPham);
        }
        
        public StringProperty tenSanPhamProperty() { return tenSanPham; }
        public StringProperty loaiSanPhamProperty() { return loaiSanPham; }
        public IntegerProperty soLuongProperty() { return soLuong; }
        public ObjectProperty<BigDecimal> giaSanPhamProperty() { return giaSanPham; }
    }

    public static class VeDaDatVM {
        private final LongProperty maVe = new SimpleLongProperty();
        private final StringProperty tenPhim = new SimpleStringProperty();
        private final StringProperty tenPhong = new SimpleStringProperty();
        private final StringProperty tenGhe = new SimpleStringProperty();
        private final StringProperty ngayChieu = new SimpleStringProperty();
        private final ObjectProperty<BigDecimal> giaVe = new SimpleObjectProperty<>();
        
        public VeDaDatVM(long maVe, String tenPhim, String tenPhong, String tenGhe, String ngayChieu, BigDecimal giaVe) {
            this.maVe.set(maVe);
            this.tenPhim.set(tenPhim);
            this.tenPhong.set(tenPhong);
            this.tenGhe.set(tenGhe);
            this.ngayChieu.set(ngayChieu);
            this.giaVe.set(giaVe);
        }
        
        public LongProperty maVeProperty() { return maVe; }
        public StringProperty tenPhimProperty() { return tenPhim; }
        public StringProperty tenPhongProperty() { return tenPhong; }
        public StringProperty tenGheProperty() { return tenGhe; }
        public StringProperty ngayChieuProperty() { return ngayChieu; }
        public ObjectProperty<BigDecimal> giaVeProperty() { return giaVe; }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initPhimTab();
        initDatVeTab();
        initComboTab();
        initLichSuTab();
        initTaiKhoanTab();
    }

    public void setCurrentCustomer(Integer maKhachHang, String hoTen, String email) {
        this.currentMaKhachHang = maKhachHang;
        this.currentHoTen = hoTen;
        this.currentEmail = email;
        
        Platform.runLater(() -> {
            lblHoTen.setText(hoTen);
            lblEmail.setText(email);
            txtHoTen.setText(hoTen);
            txtEmail.setText(email);
            
            loadThongTinKhachHang();
            loadPhimDangChieu();
            loadLichSuDatVe();
        });
    }

    // =================== CÁC PHƯƠNG THỨC KHÁC GIỮ NGUYÊN ===================
    // ... (giữ nguyên tất cả các phương thức khác từ code trước)
    
    private void loadSuatChieuTheoFilter() {
        if (tblSuatChieu == null) return;

        String selectedRap = cbRap.getValue();
        String selectedNgay = cbNgayChieu.getValue();
        
        LocalDate ngayChieu = LocalDate.parse(selectedNgay, 
            DateTimeFormatter.ofPattern("dd/MM/yyyy"));

        String sql = """
            SELECT sc.ma_suat_chieu, p.ten_phim, ph.ten_phong, dd.ten_dinh_dang,
                   DATE_FORMAT(sc.bat_dau_luc, '%H:%i') AS gio,
                   tinh_gia_ve_theo_loai_khach(sc.ma_suat_chieu, ?) as gia_ve
            FROM suat_chieu sc
            JOIN phim p ON sc.ma_phim = p.ma_phim
            JOIN phong ph ON sc.ma_phong = ph.ma_phong
            JOIN dinh_dang dd ON sc.ma_dinh_dang = dd.ma_dinh_dang
            WHERE DATE(sc.bat_dau_luc) = ?
            AND sc.bat_dau_luc > NOW()
            AND (? = 'Tất cả rạp' OR 
                (CASE 
                    WHEN ph.ten_phong LIKE 'CGV%' THEN 'CGV Sense City'
                    WHEN ph.ten_phong LIKE 'Lotte%' THEN 'Lotte Ninh Kieu'
                    ELSE 'Rạp khác'
                END) = ?)
            ORDER BY sc.bat_dau_luc
        """;

        ObservableList<SuatChieuVM> data = FXCollections.observableArrayList();
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setObject(1, currentMaKhachHang);
            // SỬA: Thêm java.sql.Date
            ps.setDate(2, java.sql.Date.valueOf(ngayChieu));
            ps.setString(3, selectedRap);
            ps.setString(4, selectedRap);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    data.add(new SuatChieuVM(
                        rs.getLong("ma_suat_chieu"),
                        rs.getString("ten_phim"),
                        rs.getString("ten_phong"),
                        rs.getString("ten_dinh_dang"),
                        rs.getString("gio"),
                        rs.getBigDecimal("gia_ve")
                    ));
                }
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi tải suất chiếu: " + e.getMessage());
        }

        tblSuatChieu.setItems(data);
    }

    private void loadThongTinKhachHang() {
        if (currentMaKhachHang == null) return;

        String sql = """
            SELECT kh.diem_tich_luy, lkh.ten_loai_khach_hang, tk.so_dien_thoai, tk.ngay_sinh
            FROM khach_hang kh
            JOIN tai_khoan tk ON kh.ma_tai_khoan = tk.ma_tai_khoan
            LEFT JOIN loai_khach_hang lkh ON kh.ma_loai_khach_hang = lkh.ma_loai_khach_hang
            WHERE kh.ma_khach_hang = ?
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, currentMaKhachHang);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    currentDiemTichLuy = rs.getInt("diem_tich_luy");
                    currentHangTV = rs.getString("ten_loai_khach_hang");
                    String sdt = rs.getString("so_dien_thoai");
                    // SỬA: Thêm java.sql.Date
                    java.sql.Date ngaySinh = rs.getDate("ngay_sinh");
                    
                    Platform.runLater(() -> {
                        lblSdt.setText(sdt != null ? sdt : "--");
                        lblHangTV.setText(currentHangTV != null ? currentHangTV : "Thường");
                        lblDiem.setText(String.valueOf(currentDiemTichLuy));
                        
                        txtSdt.setText(sdt != null ? sdt : "");
                        if (ngaySinh != null) {
                            dpNgaySinh.setValue(ngaySinh.toLocalDate());
                        }
                    });
                }
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi tải thông tin khách hàng: " + e.getMessage());
        }
    }

    @FXML
    private void handleSuaThongTin(ActionEvent event) {
        String hoTenMoi = txtHoTen.getText().trim();
        String emailMoi = txtEmail.getText().trim();
        String sdtMoi = txtSdt.getText().trim();
        LocalDate ngaySinhMoi = dpNgaySinh.getValue();

        if (hoTenMoi.isEmpty() || emailMoi.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Họ tên và email không được để trống");
            return;
        }

        try {
            String sql = "CALL proc_kh_update(?, ?, ?, ?, ?, ?)";
            try (Connection conn = DBConnection.getConnection();
                 CallableStatement stmt = conn.prepareCall(sql)) {
                
                stmt.setInt(1, currentMaKhachHang);
                stmt.setString(2, hoTenMoi);
                stmt.setString(3, emailMoi);
                stmt.setString(4, sdtMoi);
                // SỬA: Thêm java.sql.Date
                stmt.setDate(5, ngaySinhMoi != null ? java.sql.Date.valueOf(ngaySinhMoi) : null);
                stmt.setInt(6, 1);
                
                stmt.executeUpdate();
                
                currentHoTen = hoTenMoi;
                currentEmail = emailMoi;
                
                Platform.runLater(() -> {
                    lblHoTen.setText(hoTenMoi);
                    lblEmail.setText(emailMoi);
                    lblSdt.setText(sdtMoi);
                });
                
                showAlert(Alert.AlertType.INFORMATION, "Cập nhật thông tin thành công!");
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi cập nhật thông tin: " + e.getMessage());
        }
    }

    // =================== PHƯƠNG THỨC TIỆN ÍCH ===================
    private void showAlert(Alert.AlertType alertType, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(alertType);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
        // =================== CÁC PHƯƠNG THỨC KHỞI TẠO TAB ===================
    private void initPhimTab() {
        initRapComboBox();
        initNgayChieuComboBox();
        initSuatChieuTable();
        loadPhimDangChieu();
    }

    private void initDatVeTab() {
        btnApDungKM.setOnAction(this::handleApDungKhuyenMai);
        btnDatVe.setOnAction(this::handleDatVe);
        btnXuatHD.setOnAction(this::handleXuatHoaDon);
    }

    private void initComboTab() {
        initComboTable();
        loadComboData();
    }

    private void initLichSuTab() {
        initLichSuTable();
        loadLichSuDatVe();
    }

    private void initTaiKhoanTab() {
        btnSuaThongTin.setOnAction(this::handleSuaThongTin);
        btnDoiMatKhau.setOnAction(this::handleDoiMatKhau);
    }

    // =================== CÁC PHƯƠNG THỨC LOAD DỮ LIỆU ===================
    private void loadPhimDangChieu() {
        if (tilePhimDangChieu == null) return;
        tilePhimDangChieu.getChildren().clear();

        String sql = """
            SELECT p.ma_phim, p.ten_phim, p.thoi_luong_phut, p.phan_loai, 
                   p.poster_url, p.mo_ta, p.dao_dien, p.trailer_url
            FROM phim p
            WHERE p.ngay_ket_thuc >= CURDATE() AND p.trang_thai = 'DANG_CHIEU'
            ORDER BY p.ngay_phat_hanh DESC
            LIMIT 20
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                VBox card = createPhimCard(
                    rs.getLong("ma_phim"),
                    rs.getString("ten_phim"),
                    rs.getInt("thoi_luong_phut"),
                    rs.getString("phan_loai"),
                    rs.getString("poster_url"),
                    rs.getString("mo_ta"),
                    rs.getString("dao_dien"),
                    rs.getString("trailer_url")
                );
                tilePhimDangChieu.getChildren().add(card);
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi tải danh sách phim: " + e.getMessage());
        }
    }

    private VBox createPhimCard(long maPhim, String tenPhim, int thoiLuong, 
                               String phanLoai, String posterUrl, String moTa, 
                               String daoDien, String trailerUrl) {
        VBox card = new VBox(10);
        card.getStyleClass().add("movie-card");
        card.setPrefWidth(280);
        card.setPadding(new Insets(15));
        card.setStyle("""
            -fx-background-color: white;
            -fx-background-radius: 12;
            -fx-border-radius: 12;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 4);
        """);

        // POSTER PHIM
        ImageView imgPoster = new ImageView();
        imgPoster.setFitWidth(250);
        imgPoster.setFitHeight(350);
        imgPoster.setPreserveRatio(false);
        loadPosterImage(imgPoster, posterUrl);

        // THÔNG TIN PHIM
        Label lblTen = new Label(tenPhim);
        lblTen.setStyle("""
            -fx-font-size: 16px;
            -fx-font-weight: bold;
            -fx-text-fill: #1f2937;
            -fx-wrap-text: true;
        """);

        Label lblInfo = new Label(String.format("⏱ %d phút | %s", thoiLuong, phanLoai));
        lblInfo.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 14px;");

        Label lblDaoDien = new Label("🎬 " + daoDien);
        lblDaoDien.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 14px;");

        Button btnXemSuat = new Button("🎟️ Xem suất chiếu");
        btnXemSuat.setStyle("""
            -fx-background-color: #e63946;
            -fx-text-fill: white;
            -fx-font-weight: bold;
            -fx-background-radius: 8;
            -fx-padding: 8 16;
        """);
        btnXemSuat.setOnAction(e -> hienThiSuatChieuTheoPhim(maPhim, tenPhim));

        card.getChildren().addAll(imgPoster, lblTen, lblInfo, lblDaoDien, btnXemSuat);
        return card;
    }

    private void loadPosterImage(ImageView imageView, String posterUrl) {
        try {
            if (posterUrl != null && !posterUrl.trim().isEmpty()) {
                Image image = new Image(posterUrl, true);
                imageView.setImage(image);
            } else {
                // ẢNH MẶC ĐỊNH
                Image defaultImage = new Image(Objects.requireNonNull(
                    getClass().getResource("/images/default-movie.png")).toExternalForm());
                imageView.setImage(defaultImage);
            }
        } catch (Exception e) {
            System.out.println("Lỗi load ảnh: " + e.getMessage());
        }
    }

    private void hienThiSuatChieuTheoPhim(long maPhim, String tenPhim) {
        TabPane tabPane = (TabPane) tilePhimDangChieu.getScene().lookup("#mainTabs");
        if (tabPane != null) {
            tabPane.getSelectionModel().select(1);
        }
        loadSuatChieuTheoFilter();
    }

    private void initComboTable() {
        if (tblCombo == null) return;

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
        colMoTaCombo.setCellValueFactory(new PropertyValueFactory<>("moTa"));

        colChonCombo.setCellFactory(col -> new TableCell<ComboVM, Void>() {
            private final Button btnChon = new Button("🛒 Chọn");
            
            {
                btnChon.getStyleClass().add("btn-success");
                btnChon.setOnAction(e -> {
                    ComboVM combo = getTableView().getItems().get(getIndex());
                    if (combo != null) {
                        chonCombo(combo);
                    }
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btnChon);
            }
        });

        if (tblComboChiTiet != null) {
            colSanPham.setCellValueFactory(new PropertyValueFactory<>("tenSanPham"));
            colSoLuong.setCellValueFactory(new PropertyValueFactory<>("soLuong"));
            colGiaSanPham.setCellValueFactory(new PropertyValueFactory<>("giaSanPham"));
            colGiaSanPham.setCellFactory(tc -> new TableCell<ComboChiTietVM, BigDecimal>() {
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
        }

        tblCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                loadComboChiTiet(newVal.maComboProperty().get());
            }
        });
    }

    private void loadComboData() {
        if (tblCombo == null) return;
        
        String sql = """
            SELECT ma_combo, ten_combo, gia, mo_ta
            FROM combo
            WHERE hoat_dong = 1
            ORDER BY ma_combo
        """;
        
        ObservableList<ComboVM> list = FXCollections.observableArrayList();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new ComboVM(
                    rs.getInt("ma_combo"),
                    rs.getString("ten_combo"),
                    rs.getBigDecimal("gia"),
                    rs.getString("mo_ta")
                ));
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi tải combo: " + e.getMessage());
        }
        
        tblCombo.setItems(list);
    }

    private void loadComboChiTiet(int maCombo) {
        if (tblComboChiTiet == null) return;
        
        String sql = """
            SELECT sp.ten_san_pham, sp.loai, sp.gia, ct.so_luong
            FROM combo_chi_tiet ct
            JOIN san_pham sp ON ct.ma_san_pham = sp.ma_san_pham
            WHERE ct.ma_combo = ?
            ORDER BY sp.loai, sp.ten_san_pham
        """;
        
        ObservableList<ComboChiTietVM> list = FXCollections.observableArrayList();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maCombo);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new ComboChiTietVM(
                        rs.getString("ten_san_pham"),
                        rs.getString("loai"),
                        rs.getInt("so_luong"),
                        rs.getBigDecimal("gia")
                    ));
                }
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi tải chi tiết combo: " + e.getMessage());
        }
        
        tblComboChiTiet.setItems(list);
    }

    private void chonCombo(ComboVM combo) {
        int maCombo = combo.maComboProperty().get();
        BigDecimal giaCombo = combo.giaComboProperty().get();
        
        if (comboDaChon.contains(maCombo)) {
            comboDaChon.remove(Integer.valueOf(maCombo));
            tongTienCombo = tongTienCombo.subtract(giaCombo);
        } else {
            comboDaChon.add(maCombo);
            tongTienCombo = tongTienCombo.add(giaCombo);
        }
        
        capNhatTongTien();
        showAlert(Alert.AlertType.INFORMATION, 
            "Đã " + (comboDaChon.contains(maCombo) ? "thêm" : "bỏ") + " combo: " + combo.tenComboProperty().get());
    }

    private void initLichSuTable() {
        if (tblLichSu == null) return;

        colPhimLS.setCellValueFactory(new PropertyValueFactory<>("tenPhim"));
        colPhongLS.setCellValueFactory(new PropertyValueFactory<>("tenPhong"));
        colGheLS.setCellValueFactory(new PropertyValueFactory<>("tenGhe"));
        colNgayLS.setCellValueFactory(new PropertyValueFactory<>("ngayChieu"));
        colGiaLS.setCellValueFactory(new PropertyValueFactory<>("giaVe"));
        colGiaLS.setCellFactory(tc -> new TableCell<VeDaDatVM, BigDecimal>() {
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

        colInHDLS.setCellFactory(col -> new TableCell<VeDaDatVM, Void>() {
            private final Button btnIn = new Button("🖨️ In");
            
            {
                btnIn.getStyleClass().add("btn-primary");
                btnIn.setOnAction(e -> {
                    VeDaDatVM ve = getTableView().getItems().get(getIndex());
                    if (ve != null) {
                        try {
                            xuatHoaDonTheoVe(ve);
                        } catch (Exception ex) {
                            showAlert(Alert.AlertType.ERROR, "Lỗi xuất hóa đơn: " + ex.getMessage());
                        }
                    }
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btnIn);
            }
        });
    }

    private void loadLichSuDatVe() {
        if (tblLichSu == null || currentMaKhachHang == null) return;

        String sql = """
            SELECT v.ma_ve, p.ten_phim, ph.ten_phong, g.ten_ghe,
                   DATE_FORMAT(sc.bat_dau_luc, '%d/%m/%Y %H:%i') as ngay_chieu,
                   v.gia_ban
            FROM ve v
            JOIN suat_chieu sc ON v.ma_suat_chieu = sc.ma_suat_chieu
            JOIN phim p ON sc.ma_phim = p.ma_phim
            JOIN phong ph ON sc.ma_phong = ph.ma_phong
            JOIN ghe g ON v.ma_ghe = g.ma_ghe
            WHERE v.ma_khach_hang = ? AND v.trang_thai = 'DA_BAN'
            ORDER BY sc.bat_dau_luc DESC
            LIMIT 50
        """;

        ObservableList<VeDaDatVM> list = FXCollections.observableArrayList();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, currentMaKhachHang);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new VeDaDatVM(
                        rs.getLong("ma_ve"),
                        rs.getString("ten_phim"),
                        rs.getString("ten_phong"),
                        rs.getString("ten_ghe"),
                        rs.getString("ngay_chieu"),
                        rs.getBigDecimal("gia_ban")
                    ));
                }
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi tải lịch sử đặt vé: " + e.getMessage());
        }

        tblLichSu.setItems(list);
    }

    // =================== CÁC PHƯƠNG THỨC XỬ LÝ SỰ KIỆN ===================
    @FXML
    private void handleApDungKhuyenMai(ActionEvent event) {
        String maCode = txtMaKhuyenMai.getText().trim();
        if (maCode.isEmpty()) {
            lblThongBaoKM.setText("Vui lòng nhập mã khuyến mãi");
            lblThongBaoKM.setStyle("-fx-text-fill: #ef4444;");
            return;
        }

        String sql = """
            SELECT kieu_giam, gia_tri_giam, don_toi_thieu
            FROM khuyen_mai
            WHERE ma_code = ? 
            AND hoat_dong = 1
            AND bat_dau_luc <= NOW() 
            AND ket_thuc_luc >= NOW()
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, maCode);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String kieuGiam = rs.getString("kieu_giam");
                    BigDecimal giaTriGiam = rs.getBigDecimal("gia_tri_giam");
                    BigDecimal donToiThieu = rs.getBigDecimal("don_toi_thieu");
                    
                    BigDecimal tongTienTruocGiam = tongTienVé.add(tongTienCombo);
                    
                    if (tongTienTruocGiam.compareTo(donToiThieu) >= 0) {
                        if ("PHAN_TRAM".equals(kieuGiam)) {
                            giamGiaKM = tongTienTruocGiam.multiply(giaTriGiam).divide(BigDecimal.valueOf(100));
                        } else {
                            giamGiaKM = giaTriGiam;
                        }
                        maKhuyenMaiDangApDung = maCode;
                        
                        lblThongBaoKM.setText("✅ Áp dụng mã thành công! Giảm: " + 
                            String.format("%,d đ", giamGiaKM.intValue()));
                        lblThongBaoKM.setStyle("-fx-text-fill: #10b981;");
                    } else {
                        lblThongBaoKM.setText("❌ Đơn tối thiểu: " + 
                            String.format("%,d đ", donToiThieu.intValue()));
                        lblThongBaoKM.setStyle("-fx-text-fill: #ef4444;");
                    }
                } else {
                    lblThongBaoKM.setText("❌ Mã khuyến mãi không hợp lệ hoặc đã hết hạn");
                    lblThongBaoKM.setStyle("-fx-text-fill: #ef4444;");
                }
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi kiểm tra khuyến mãi: " + e.getMessage());
        }
        
        capNhatTongTien();
    }

    @FXML
    private void handleDatVe(ActionEvent event) {
        if (maSuatChieuDangChon == null) {
            showAlert(Alert.AlertType.WARNING, "Vui lòng chọn suất chiếu");
            return;
        }
        
        if (gheDaChon.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Vui lòng chọn ít nhất 1 ghế");
            return;
        }

        BigDecimal tongTien = tongTienVé.add(tongTienCombo).subtract(giamGiaKM);
        
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận đặt vé");
        confirm.setHeaderText("Thông tin đặt vé");
        confirm.setContentText(String.format("""
            Phim: %s
            Phòng: %s
            Ghế: %s
            Tổng tiền: %,d đ
            
            Xác nhận đặt vé?
            """, 
            lblTenPhimDat.getText(),
            lblPhongDat.getText(),
            gheDaChon.toString(),
            tongTien.intValue()
        ));

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                datVeXuongDatabase();
                showAlert(Alert.AlertType.INFORMATION, "Đặt vé thành công!");
                resetDatVe();
                loadLichSuDatVe();
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Lỗi đặt vé: " + e.getMessage());
            }
        }
    }

    private void datVeXuongDatabase() throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            
            String sqlDonHang = """
                INSERT INTO don_hang (ma_khach_hang, kenh, trang_thai, tong_tien)
                VALUES (?, 'TRUC_TUYEN', 'DA_THANH_TOAN', ?)
            """;
            int maDonHang;
            
            try (PreparedStatement ps = conn.prepareStatement(sqlDonHang, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, currentMaKhachHang);
                ps.setBigDecimal(2, tongTienVé.add(tongTienCombo).subtract(giamGiaKM));
                ps.executeUpdate();
                
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        maDonHang = rs.getInt(1);
                    } else {
                        throw new SQLException("Không thể tạo đơn hàng");
                    }
                }
            }
            
            for (int maGhe : gheDaChon) {
                String sqlUpdateVe = """
                    UPDATE ve 
                    SET trang_thai = 'DA_BAN', ban_luc = NOW(), ma_khach_hang = ?
                    WHERE ma_suat_chieu = ? AND ma_ghe = ?
                """;
                try (PreparedStatement ps = conn.prepareStatement(sqlUpdateVe)) {
                    ps.setInt(1, currentMaKhachHang);
                    ps.setLong(2, maSuatChieuDangChon);
                    ps.setInt(3, maGhe);
                    ps.executeUpdate();
                }
            }
            
            conn.commit();
            
            int diemThuong = tongTienVé.divide(BigDecimal.valueOf(10000)).intValue();
            currentDiemTichLuy += diemThuong;
            Platform.runLater(() -> lblDiem.setText(String.valueOf(currentDiemTichLuy)));
            
        } catch (SQLException e) {
            throw e;
        }
    }

    @FXML
    private void handleXuatHoaDon(ActionEvent event) {
        if (gheDaChon.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Vui lòng đặt vé trước khi xuất hóa đơn");
            return;
        }
        
        try {
            xuatHoaDonPDF();
            showAlert(Alert.AlertType.INFORMATION, "Xuất hóa đơn thành công!");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi xuất hóa đơn: " + e.getMessage());
        }
    }

    @FXML
    private void handleDoiMatKhau(ActionEvent event) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Đổi mật khẩu");
        dialog.setHeaderText("Nhập mật khẩu mới");
        dialog.setContentText("Mật khẩu mới:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(matKhauMoi -> {
            if (matKhauMoi.length() < 6) {
                showAlert(Alert.AlertType.WARNING, "Mật khẩu phải có ít nhất 6 ký tự");
                return;
            }

            try {
                String sql = "CALL proc_doi_mat_khau_khach_hang(?, ?)";
                try (Connection conn = DBConnection.getConnection();
                     CallableStatement stmt = conn.prepareCall(sql)) {
                    
                    stmt.setInt(1, currentMaKhachHang);
                    stmt.setString(2, matKhauMoi);
                    
                    stmt.executeUpdate();
                    showAlert(Alert.AlertType.INFORMATION, "Đổi mật khẩu thành công!");
                }
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Lỗi đổi mật khẩu: " + e.getMessage());
            }
        });
    }

    // =================== PHƯƠNG THỨC TIỆN ÍCH ===================
    private void capNhatTongTien() {
        BigDecimal tong = tongTienVé.add(tongTienCombo).subtract(giamGiaKM);
        lblTongTien.setText(String.format("%,d đ", tong.intValue()));
    }

    private void resetDatVe() {
        gheDaChon.clear();
        comboDaChon.clear();
        tongTienVé = BigDecimal.ZERO;
        tongTienCombo = BigDecimal.ZERO;
        giamGiaKM = BigDecimal.ZERO;
        maKhuyenMaiDangApDung = null;
        
        lblTenPhimDat.setText("--");
        lblPhongDat.setText("--");
        lblGioDat.setText("--");
        lblTongTien.setText("0 đ");
        txtMaKhuyenMai.clear();
        lblThongBaoKM.setText("");
        
        if (containerGhe != null) {
            containerGhe.getChildren().clear();
        }
    }

    private void xuatHoaDonPDF() throws Exception {
        // TẠO THƯ MỤC NẾU CHƯA CÓ
        File dir = new File("HoaDonKhachHang");
        if (!dir.exists()) dir.mkdirs();

        String fileName = String.format("HoaDonKhachHang/Ve_%s_%s.pdf", 
            currentMaKhachHang, 
            System.currentTimeMillis());
        
        Document document = new Document(PageSize.A5, 36, 36, 54, 36);
        PdfWriter.getInstance(document, new FileOutputStream(fileName));
        document.open();

        BaseFont bf = BaseFont.createFont("c:/windows/fonts/arial.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        Font titleFont = new Font(bf, 18, Font.BOLD, BaseColor.RED);
        Font headerFont = new Font(bf, 14, Font.BOLD, BaseColor.BLACK);
        Font normalFont = new Font(bf, 12, Font.NORMAL, BaseColor.BLACK);
        Font boldFont = new Font(bf, 12, Font.BOLD, BaseColor.BLACK);

        Paragraph title = new Paragraph("RẠP CHIẾU PHIM CINEMA 4U\nHÓA ĐƠN VÉ XEM PHIM", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
        document.add(new Paragraph("\n"));

        Paragraph khachHang = new Paragraph("Thông tin khách hàng:", headerFont);
        document.add(khachHang);
        
        PdfPTable infoTable = new PdfPTable(2);
        infoTable.setWidthPercentage(100);
        infoTable.setSpacingBefore(5f);
        infoTable.setSpacingAfter(10f);
        
        infoTable.addCell(createCell("Họ tên:", boldFont));
        infoTable.addCell(createCell(currentHoTen, normalFont));
        infoTable.addCell(createCell("Email:", boldFont));
        infoTable.addCell(createCell(currentEmail, normalFont));
        infoTable.addCell(createCell("Hạng thành viên:", boldFont));
        infoTable.addCell(createCell(currentHangTV, normalFont));
        infoTable.addCell(createCell("Điểm tích lũy:", boldFont));
        infoTable.addCell(createCell(String.valueOf(currentDiemTichLuy), normalFont));
        
        document.add(infoTable);

        Paragraph veInfo = new Paragraph("Thông tin vé đã đặt:", headerFont);
        document.add(veInfo);
        
        PdfPTable veTable = new PdfPTable(2);
        veTable.setWidthPercentage(100);
        veTable.setSpacingBefore(5f);
        veTable.setSpacingAfter(10f);
        
        veTable.addCell(createCell("Phim:", boldFont));
        veTable.addCell(createCell(lblTenPhimDat.getText(), normalFont));
        veTable.addCell(createCell("Phòng:", boldFont));
        veTable.addCell(createCell(lblPhongDat.getText(), normalFont));
        veTable.addCell(createCell("Suất chiếu:", boldFont));
        veTable.addCell(createCell(lblGioDat.getText(), normalFont));
        veTable.addCell(createCell("Ghế đã chọn:", boldFont));
        veTable.addCell(createCell(gheDaChon.toString(), normalFont));
        
        document.add(veTable);

        document.add(new Paragraph("\n\n"));
        Paragraph thankYou = new Paragraph("Cảm ơn quý khách đã sử dụng dịch vụ!\nHẹn gặp lại!", normalFont);
        thankYou.setAlignment(Element.ALIGN_CENTER);
        document.add(thankYou);

        document.close();
    }

    private void xuatHoaDonTheoVe(VeDaDatVM ve) throws Exception {
        File dir = new File("HoaDonKhachHang");
        if (!dir.exists()) dir.mkdirs();

        String fileName = String.format("HoaDonKhachHang/Ve_%d.pdf", ve.maVeProperty().get());
        
        Document document = new Document(PageSize.A5, 36, 36, 54, 36);
        PdfWriter.getInstance(document, new FileOutputStream(fileName));
        document.open();

        BaseFont bf = BaseFont.createFont("c:/windows/fonts/arial.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        Font titleFont = new Font(bf, 18, Font.BOLD, BaseColor.RED);
        Font normalFont = new Font(bf, 12, Font.NORMAL, BaseColor.BLACK);
        Font boldFont = new Font(bf, 12, Font.BOLD, BaseColor.BLACK);

        Paragraph title = new Paragraph("RẠP CHIẾU PHIM CINEMA 4U\nHÓA ĐƠN VÉ ĐÃ MUA", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
        document.add(new Paragraph("\n"));

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        
        table.addCell(createCell("Mã vé:", boldFont));
        table.addCell(createCell(String.valueOf(ve.maVeProperty().get()), normalFont));
        table.addCell(createCell("Phim:", boldFont));
        table.addCell(createCell(ve.tenPhimProperty().get(), normalFont));
        table.addCell(createCell("Phòng:", boldFont));
        table.addCell(createCell(ve.tenPhongProperty().get(), normalFont));
        table.addCell(createCell("Ghế:", boldFont));
        table.addCell(createCell(ve.tenGheProperty().get(), normalFont));
        table.addCell(createCell("Ngày chiếu:", boldFont));
        table.addCell(createCell(ve.ngayChieuProperty().get(), normalFont));
        table.addCell(createCell("Giá vé:", boldFont));
        table.addCell(createCell(String.format("%,d đ", ve.giaVeProperty().get().intValue()), normalFont));
        
        document.add(table);
        document.add(new Paragraph("\n"));

        Paragraph thankYou = new Paragraph("Cảm ơn quý khách!", normalFont);
        thankYou.setAlignment(Element.ALIGN_CENTER);
        document.add(thankYou);

        document.close();
    }

    private PdfPCell createCell(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(5);
        return cell;
    }
    
        // =================== CÁC PHƯƠNG THỨC BỊ THIẾU ===================
    private void initRapComboBox() {
        if (cbRap == null) return;
        
        cbRap.getItems().clear();
        cbRap.getItems().addAll("Tất cả rạp", "CGV Sense City", "Lotte Ninh Kieu", "Rạp khác");
        cbRap.setValue("Tất cả rạp");
        
        cbRap.valueProperty().addListener((obs, oldVal, newVal) -> {
            loadSuatChieuTheoFilter();
        });
    }

    private void initNgayChieuComboBox() {
        if (cbNgayChieu == null) return;
        
        cbNgayChieu.getItems().clear();
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        
        for (int i = 0; i < 7; i++) {
            LocalDate date = today.plusDays(i);
            cbNgayChieu.getItems().add(date.format(formatter));
        }
        cbNgayChieu.setValue(today.format(formatter));
        
        cbNgayChieu.valueProperty().addListener((obs, oldVal, newVal) -> {
            loadSuatChieuTheoFilter();
        });
    }

    private void initSuatChieuTable() {
        if (tblSuatChieu == null) return;

        colPhim.setCellValueFactory(new PropertyValueFactory<>("tenPhim"));
        colPhong.setCellValueFactory(new PropertyValueFactory<>("tenPhong"));
        colDinhDang.setCellValueFactory(new PropertyValueFactory<>("dinhDang"));
        colGio.setCellValueFactory(new PropertyValueFactory<>("gioChieu"));
        colGia.setCellValueFactory(new PropertyValueFactory<>("giaVe"));
        colGia.setCellFactory(tc -> new TableCell<SuatChieuVM, BigDecimal>() {
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

        // CỘT CHỌN SUẤT CHIẾU
        colChonSuat.setCellFactory(col -> new TableCell<SuatChieuVM, Void>() {
            private final Button btnChon = new Button("🎬 Chọn");
            
            {
                btnChon.getStyleClass().add("btn-primary");
                btnChon.setOnAction(e -> {
                    SuatChieuVM suatChieu = getTableView().getItems().get(getIndex());
                    if (suatChieu != null) {
                        chonSuatChieu(suatChieu);
                    }
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btnChon);
            }
        });
    }

    private void chonSuatChieu(SuatChieuVM suatChieu) {
        this.maSuatChieuDangChon = suatChieu.getMaSuatChieu();
        
        Platform.runLater(() -> {
            lblTenPhimDat.setText(suatChieu.getTenPhim());
            lblPhongDat.setText(suatChieu.getTenPhong());
            lblGioDat.setText(suatChieu.getGioChieu() + " - " + suatChieu.getDinhDang());
            
            TabPane tabPane = (TabPane) tblSuatChieu.getScene().lookup("#mainTabs");
            if (tabPane != null) {
                tabPane.getSelectionModel().select(2);
            }
            
            loadGheTrong();
        });
    }

    private void loadGheTrong() {
        if (containerGhe == null || maSuatChieuDangChon == null) return;
        
        containerGhe.getChildren().clear();
        gheButtons.clear();
        gheDaChon.clear();
        tongTienVé = BigDecimal.ZERO;
        capNhatTongTien();

        String sql = """
            SELECT g.ma_ghe, g.ten_ghe, lg.ten_loai_ghe, v.gia_ban, v.trang_thai
            FROM ghe g
            JOIN loai_ghe lg ON g.ma_loai_ghe = lg.ma_loai_ghe
            JOIN ve v ON g.ma_ghe = v.ma_ghe AND v.ma_suat_chieu = ?
            ORDER BY g.ten_ghe
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setLong(1, maSuatChieuDangChon);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String tenGhe = rs.getString("ten_ghe");
                    String loaiGhe = rs.getString("ten_loai_ghe");
                    BigDecimal giaBan = rs.getBigDecimal("gia_ban");
                    String trangThai = rs.getString("trang_thai");
                    
                    Button btnGhe = createGheButton(tenGhe, loaiGhe, giaBan, trangThai);
                    containerGhe.getChildren().add(btnGhe);
                    gheButtons.put(tenGhe, btnGhe);
                }
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi tải danh sách ghế: " + e.getMessage());
        }
    }

    private Button createGheButton(String tenGhe, String loaiGhe, BigDecimal giaBan, String trangThai) {
        Button btn = new Button(tenGhe);
        btn.setPrefSize(40, 40);
        
        if (!"SAN_SANG".equals(trangThai)) {
            btn.setStyle("""
                -fx-background-color: #6b7280;
                -fx-text-fill: white;
                -fx-font-weight: bold;
                -fx-cursor: default;
            """);
            btn.setDisable(true);
            btn.setTooltip(new Tooltip("Ghế đã được đặt"));
        } else {
            String color = switch (loaiGhe) {
                case "VIP" -> "#f59e0b";
                case "Couple" -> "#ec4899";
                default -> "#10b981";
            };
            
            btn.setStyle(String.format("""
                -fx-background-color: %s;
                -fx-text-fill: white;
                -fx-font-weight: bold;
                -fx-cursor: hand;
            """, color));
            
            btn.setTooltip(new Tooltip(String.format("%s - %s - %,d đ", tenGhe, loaiGhe, giaBan.intValue())));
            
            final int maGhe = Integer.parseInt(tenGhe.replaceAll("\\D", ""));
            btn.setOnAction(e -> {
                if (gheDaChon.contains(maGhe)) {
                    gheDaChon.remove(Integer.valueOf(maGhe));
                    btn.setStyle(String.format("""
                        -fx-background-color: %s;
                        -fx-text-fill: white;
                    """, color));
                    tongTienVé = tongTienVé.subtract(giaBan);
                } else {
                    gheDaChon.add(maGhe);
                    btn.setStyle("""
                        -fx-background-color: #3b82f6;
                        -fx-text-fill: white;
                    """);
                    tongTienVé = tongTienVé.add(giaBan);
                }
                capNhatTongTien();
            });
        }
        
        return btn;
    }
}