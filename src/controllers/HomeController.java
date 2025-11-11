package controllers;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import database.DBConnection;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Alert;

import javafx.fxml.FXML;

import javafx.scene.control.TextField;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.Alert;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.*;

public class HomeController {

    // ====== FXML refs ======
    @FXML
    private VBox sidebar;
    @FXML
    private ImageView imgToggle;
    @FXML
    private ScrollPane scroll;

    // ⚙️ Các thành phần thống kê (trùng với home.fxml)
    @FXML
    private Label lblDoanhThuHomNay;
    @FXML
    private Label lblTongVeBan;
    @FXML
    private Label lblTiLeLapDay;
    @FXML
    private LineChart<String, Number> chartDoanhThu;
    @FXML
    private PieChart chartPhanBoPhim;

    // ====== State ======
    private boolean collapsed = false;
    private final double expandedWidth = 260;
    private final double collapsedWidth = 72;
    private final List<Button> menuButtons = new ArrayList<>();
    private final Map<String, Parent> viewCache = new HashMap<>();

    // ====== INIT ======
    @FXML
    private void initialize() {
        System.out.println("[HomeController] initialize()");
        if (imgToggle != null) {
            imgToggle.setStyle("-fx-cursor: hand;");
        }
        if (sidebar != null) {
            collectMenuButtons(sidebar);
        }
        if (scroll != null && scroll.getContent() instanceof VBox) {
            VBox content = (VBox) scroll.getContent();
            content.setFillWidth(true);
            content.prefWidthProperty().bind(scroll.widthProperty().subtract(24));
        }

        // 🔹 Gọi load dữ liệu dashboard (rất quan trọng)
        Platform.runLater(() -> {
            loadTongQuan();
            loadBieuDoDoanhThu();
            loadBieuDoPhanBoPhim();
        });
    }

    private void collectMenuButtons(VBox box) {
        for (Node n : box.getChildren()) {
            if (n instanceof Button b && b.getStyleClass().contains("nav-button")) {
                menuButtons.add(b);
            } else if (n instanceof VBox inner) {
                collectMenuButtons(inner);
            }
        }
    }

    // ====== ĐIỀU HƯỚNG ======
    @FXML
    private void handleNav(ActionEvent e) {
        if (!(e.getSource() instanceof Button b)) {
            return;
        }
        Object ud = b.getUserData();
        if (ud == null) {
            return;
        }
        String fxmlPath = ud.toString();
        if (loadViewIntoCenter(fxmlPath)) {
            highlightActiveButton(b);
        }
    }

    private boolean loadViewIntoCenter(String fxmlPath) {
        if (scroll == null) {
            return false;
        }
        try {
            Parent view = viewCache.get(fxmlPath);
            if (view == null) {
                URL url = getClass().getResource(fxmlPath);
                if (url == null) {
                    System.err.println("❌ Không tìm thấy FXML: " + fxmlPath);
                    return false;
                }
                view = FXMLLoader.load(url);
                viewCache.put(fxmlPath, view);
            }
            scroll.setContent(view);
            return true;
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    private void highlightActiveButton(Button active) {
        for (Button btn : menuButtons) {
            btn.getStyleClass().remove("active");
        }
        if (!active.getStyleClass().contains("active")) {
            active.getStyleClass().add("active");
        }
    }

    // =============================================================
    // ========== 📊 CÁC HÀM LẤY DỮ LIỆU DASHBOARD ==================
    // =============================================================
    private void loadTongQuan() {
        String sqlDoanhThu = """
            SELECT IFNULL(SUM(tong_tien),0) AS doanh_thu
            FROM don_hang
            WHERE DATE(dat_luc) = CURDATE() AND trang_thai = 'DA_THANH_TOAN';
        """;

        String sqlTongVe = """
            SELECT IFNULL(COUNT(*),0) AS tong_ve
            FROM ve
            WHERE trang_thai = 'DA_BAN' AND DATE(ban_luc) = CURDATE();
        """;

        String sqlTiLeLapDay = """
            SELECT IFNULL(ROUND(100 * COUNT(CASE WHEN v.trang_thai = 'DA_BAN' THEN 1 END) / COUNT(*), 2),0) AS tile
            FROM ve v
            JOIN suat_chieu s ON v.ma_suat_chieu = s.ma_suat_chieu
            WHERE DATE(s.bat_dau_luc) = CURDATE();
        """;

        try (Connection conn = DBConnection.getConnection()) {
            // Doanh thu hôm nay
            try (PreparedStatement ps = conn.prepareStatement(sqlDoanhThu); ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    double dt = rs.getDouble("doanh_thu");
                    lblDoanhThuHomNay.setText(String.format("%,.0f VNĐ", dt));
                }
            }

            // Tổng vé bán
            try (PreparedStatement ps = conn.prepareStatement(sqlTongVe); ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    lblTongVeBan.setText(rs.getInt("tong_ve") + " vé");
                }
            }

            // Tỉ lệ lấp đầy
            try (PreparedStatement ps = conn.prepareStatement(sqlTiLeLapDay); ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    lblTiLeLapDay.setText(rs.getDouble("tile") + "%");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadBieuDoDoanhThu() {
        String sql = """
            SELECT DATE(dat_luc) AS ngay, SUM(tong_tien) AS doanh_thu
            FROM don_hang
            WHERE trang_thai = 'DA_THANH_TOAN'
            GROUP BY DATE(dat_luc)
            ORDER BY ngay ASC
            LIMIT 7;
        """;

        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Doanh thu 7 ngày gần nhất");

            while (rs.next()) {
                series.getData().add(new XYChart.Data<>(
                        rs.getString("ngay"), rs.getDouble("doanh_thu")));
            }

            chartDoanhThu.getData().clear();
            chartDoanhThu.getData().add(series);

            System.out.println("✅ Đã load biểu đồ doanh thu: " + series.getData().size() + " điểm.");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadBieuDoPhanBoPhim() {
        String sql = """
            SELECT p.ten_phim, SUM(dh.tong_tien) AS doanh_thu
            FROM don_hang dh
            JOIN don_ve dv ON dh.ma_don_hang = dv.ma_don_hang
            JOIN ve v ON dv.ma_ve = v.ma_ve
            JOIN suat_chieu sc ON v.ma_suat_chieu = sc.ma_suat_chieu
            JOIN phim p ON sc.ma_phim = p.ma_phim
            WHERE dh.trang_thai = 'DA_THANH_TOAN'
            GROUP BY p.ten_phim
            ORDER BY doanh_thu DESC
            LIMIT 5;
        """;

        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            chartPhanBoPhim.getData().clear();

            while (rs.next()) {
                chartPhanBoPhim.getData().add(new PieChart.Data(
                        rs.getString("ten_phim"),
                        rs.getDouble("doanh_thu")
                ));
            }

            System.out.println("✅ Đã load biểu đồ phân bố phim: " + chartPhanBoPhim.getData().size() + " phần.");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private TextField txtTimKiem;
    @FXML
    private TableView<SearchResult> tblKetQua;
    @FXML
    private TableColumn<SearchResult, String> colLoai, colMa, colTen, colThongTin;

    @FXML
    public void timKiemTongHop() {
        String tuKhoa = txtTimKiem.getText().trim();
        if (tuKhoa.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Vui lòng nhập từ khóa tìm kiếm!");
            alert.show();
            return;
        }

        ObservableList<SearchResult> ketQua = FXCollections.observableArrayList();
        try (Connection conn = DBConnection.getConnection()) {
            // Ghi log tìm kiếm
            String logSQL = "INSERT INTO log_tim_kiem(tu_khoa) VALUES (?)";
            try (PreparedStatement logStmt = conn.prepareStatement(logSQL)) {
                logStmt.setString(1, tuKhoa);
                logStmt.executeUpdate();
            }

            // Gọi procedure
            CallableStatement cs = conn.prepareCall("{CALL proc_tim_kiem_tong_hop(?)}");
            cs.setString(1, tuKhoa);
            ResultSet rs = cs.executeQuery();

            while (rs.next()) {
                ketQua.add(new SearchResult(
                        rs.getString("loai"),
                        rs.getString("ma"),
                        rs.getString("ten"),
                        rs.getString("thong_tin")
                ));
            }

            tblKetQua.setItems(ketQua);

        } catch (SQLException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Lỗi khi tìm kiếm: " + e.getMessage());
            alert.show();
        }
    }

// Class phụ để chứa kết quả
    public static class SearchResult {

        private final SimpleStringProperty loai, ma, ten, thongTin;

        public SearchResult(String loai, String ma, String ten, String thongTin) {
            this.loai = new SimpleStringProperty(loai);
            this.ma = new SimpleStringProperty(ma);
            this.ten = new SimpleStringProperty(ten);
            this.thongTin = new SimpleStringProperty(thongTin);
        }

        public String getLoai() {
            return loai.get();
        }

        public String getMa() {
            return ma.get();
        }

        public String getTen() {
            return ten.get();
        }

        public String getThongTin() {
            return thongTin.get();
        }
    }
// ====== ĐĂNG XUẤT ======

    @FXML
    private void onLogout(ActionEvent event) {
        // Xác nhận
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Đăng xuất");
        confirm.setHeaderText("Bạn có chắc muốn đăng xuất?");
        confirm.setContentText("Mọi tiến trình đang chạy sẽ kết thúc và quay về màn hình đăng nhập.");
        Optional<javafx.scene.control.ButtonType> choice = confirm.showAndWait();
        if (choice.isEmpty() || choice.get() != javafx.scene.control.ButtonType.OK) {
            return;
        }

        try {
            // Dọn state tạm (nếu có)
            viewCache.clear();
            menuButtons.clear();
            collapsed = false;

            // (Nếu bạn có timer/thread nền → dừng tại đây)
            // Đổi scene sang login.fxml
            Node src = (event != null && event.getSource() instanceof Node) ? (Node) event.getSource() : scroll;
            javafx.stage.Stage stage = (javafx.stage.Stage) src.getScene().getWindow();

            Parent loginRoot = FXMLLoader.load(getClass().getResource("/models/login.fxml"));
            javafx.scene.Scene scene = new javafx.scene.Scene(loginRoot);
            stage.setScene(scene);
            stage.setTitle("Đăng nhập");
            stage.show();

        } catch (IOException ex) {
            ex.printStackTrace();
            Alert err = new Alert(Alert.AlertType.ERROR, "Không thể mở màn hình đăng nhập: " + ex.getMessage());
            err.show();
        }
    }

}
