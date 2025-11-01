package controllers;

import database.DBConnection;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;

import java.io.InputStream;
import java.sql.*;
import java.time.LocalDate;
import java.util.Objects;
import javafx.geometry.Insets;
public class NhanVienController {

    // ===== Root & thanh trạng thái =====
    @FXML private BorderPane root;              // <BorderPane fx:id="root" ...>
    @FXML private Label lblWelcome;             // statusbar
    @FXML private Label lblClock;               // (tuỳ chọn: gắn timer HH:mm nếu muốn)

    // ===== App bar / filter nhanh =====
    @FXML private TextField txtSearch;          // tìm nhanh (phim/suất/khách)
    @FXML private DatePicker dpNgay;            // ngày làm việc (mặc định hôm nay)
    @FXML private ComboBox<String> cbRap;       // (nếu schema có)
    @FXML private ComboBox<String> cbPhong;     // (nếu schema có)

    @FXML private Button btnBanVe, btnDoiVe, btnTraVe, btnRefresh;
    @FXML private MenuButton mbUser;            // chip người dùng góc phải
    @FXML private MenuItem miProfile, miChangePwd, miLogout;

    // ===== Tabs / vùng nội dung =====
    @FXML private TabPane mainTabs;

    // Tab Phim đang chiếu
    @FXML private TilePane tilePhimDangChieu;

    // (Các bảng khác trong FXML đã liệt kê sẵn – có thể bind sau)

    private String tenNhanVien;

    // ====== Lifecycle ======
    @FXML
    private void initialize() {
        // Ngày mặc định = hôm nay
        if (dpNgay != null) dpNgay.setValue(LocalDate.now());

        // Gắn handler cho Logout
        if (miLogout != null) miLogout.setOnAction(this::handleDangXuat);

        // Sau khi scene sẵn sàng → gắn phím tắt toàn cục
        Platform.runLater(() -> {
            if (root != null && root.getScene() != null) attachAccelerators(root.getScene());
        });

        // Tải phim ngay lần đầu
        loadPhimDangChieu();

        // Đổi ngày → reload danh sách phim (đang chiếu tới ngày đã chọn)
        if (dpNgay != null) {
            dpNgay.valueProperty().addListener((obs, oldV, newV) -> loadPhimDangChieu());
        }

        // Làm mới thủ công
        if (btnRefresh != null) btnRefresh.setOnAction(e -> loadPhimDangChieu());
    }

    public void setTenNhanVien(String ten) {
        this.tenNhanVien = ten;
        if (lblWelcome != null) lblWelcome.setText("🎬 Xin chào, " + ten + "!");
        if (mbUser != null) mbUser.setText("NV: " + ten);
        // có thể nạp theo quyền tại đây
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

    // =======================================================
    //                 PHIM ĐANG CHIẾU (TilePane)
    // =======================================================
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

        // Poster
        ImageView img = new ImageView();
        img.setFitWidth(236);
        img.setFitHeight(160);
        img.setPreserveRatio(true);
        img.setSmooth(true);
        img.setCache(true);
        img.setImage(loadPosterSafely(posterUrl));

        // Tên phim + info
        Label lblName = new Label(tenPhim);
        lblName.getStyleClass().add("section-title");

        Label lblInfo = new Label("⏱ " + thoiLuong + " phút  |  " + phanLoai);
        lblInfo.setStyle("-fx-text-fill: #caf0f8;");

        // Nút xem suất chiếu
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
        // Fallback ảnh mặc định từ resources (đổi đường dẫn theo dự án của bạn)
        try (InputStream is = getClass().getResourceAsStream("/Application/image/null.png")) {
            if (is != null) return new Image(is);
        } catch (Exception ignore) { }
        return new Image(Objects.requireNonNull(getClass().getResource("/javafx/scene/control/skin/caspian/dialog-confirm.png")).toExternalForm());
    }

    // =======================================================
    //                    SUẤT CHIẾU (theo ngày)
    // =======================================================
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

        StringBuilder info = new StringBuilder("🎞️ Suất chiếu ngày " + d + " – " + tenPhim + "");

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
                        .append(" VNĐ");
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

    // =======================================================
    //                       ĐĂNG XUẤT
    // =======================================================
    @FXML
    private void handleDangXuat(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/models/login.fxml"));
            Parent loginView = loader.load();
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(loginView));
            stage.centerOnScreen();
            stage.setTitle("🎬 Đăng nhập hệ thống");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Không thể đăng xuất", e.getMessage());
        }
    }

    // =======================================================
    //                      TIỆN ÍCH
    // =======================================================
    private void showError(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}