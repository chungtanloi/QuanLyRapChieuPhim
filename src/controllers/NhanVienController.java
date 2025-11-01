package controllers;

import database.DBConnection;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.Scene;
import java.sql.*;
import java.util.Objects;

public class NhanVienController {

    @FXML private Label lblWelcome;
    @FXML private TilePane tilePhimDangChieu;

    private String tenNhanVien;

    // Khi nhân viên đăng nhập xong -> set tên + load danh sách phim
    public void setTenNhanVien(String ten) {
        this.tenNhanVien = ten;
        lblWelcome.setText("🎬 Xin chào, " + tenNhanVien + "!");
        loadPhimDangChieu();
    }

    // 🔹 Lấy danh sách phim đang chiếu
    private void loadPhimDangChieu() {
        tilePhimDangChieu.getChildren().clear();

        String sql = """
            SELECT ma_phim, ten_phim, thoi_luong_phut, phan_loai, ngay_phat_hanh, poster_url
            FROM phim
            WHERE ngay_phat_hanh <= CURDATE()
            ORDER BY ngay_phat_hanh DESC;
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                long maPhim = rs.getLong("ma_phim");
                String tenPhim = rs.getString("ten_phim");
                int thoiLuong = rs.getInt("thoi_luong_phut");
                String phanLoai = rs.getString("phan_loai");
                String posterUrl = rs.getString("poster_url");

                VBox card = createPhimCard(maPhim, tenPhim, thoiLuong, phanLoai, posterUrl);
                tilePhimDangChieu.getChildren().add(card);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 🔹 Tạo từng thẻ phim
    private VBox createPhimCard(long maPhim, String tenPhim, int thoiLuong, String phanLoai, String posterUrl) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(12));
        card.setPrefWidth(280);
        card.setStyle("""
            -fx-background-color: rgba(255,255,255,0.08);
            -fx-background-radius: 12;
            -fx-border-radius: 12;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.25), 10,0,0,4);
            """);

        // 🔸 Ảnh poster phim
        ImageView img = new ImageView();
        img.setFitWidth(260);
        img.setFitHeight(180);
        img.setPreserveRatio(true);

        try {
            Image poster;
            if (posterUrl != null && !posterUrl.isEmpty()) {
                poster = new Image(posterUrl, true);
            } else {
                poster = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/Application/image/null.png")));
            }
            img.setImage(poster);
        } catch (Exception e) {
            System.err.println("⚠️ Không thể tải ảnh cho phim " + tenPhim + ": " + e.getMessage());
            img.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/Application/image/null.png"))));
        }

        // 🔸 Tiêu đề phim
        Label lblName = new Label(tenPhim);
        lblName.setStyle("-fx-text-fill: white; -fx-font-size: 16; -fx-font-weight: bold;");

        // 🔸 Thông tin ngắn
        Label lblInfo = new Label("⏱ " + thoiLuong + " phút  |  " + phanLoai);
        lblInfo.setStyle("-fx-text-fill: #caf0f8;");

        // 🔸 Nút xem suất chiếu
        Button btnSuat = new Button("🎟️ Xem suất chiếu");
        btnSuat.setStyle("""
            -fx-background-color: linear-gradient(to right, #0077b6, #00b4d8);
            -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8;
            """);
        btnSuat.setOnAction(e -> showSuatChieu(maPhim, tenPhim));

        Tooltip tooltip = new Tooltip("Xem các suất chiếu của phim " + tenPhim);
        Tooltip.install(card, tooltip);

        card.getChildren().addAll(img, lblName, lblInfo, btnSuat);
        return card;
    }

    // 🔹 Hiển thị danh sách suất chiếu
    private void showSuatChieu(long maPhim, String tenPhim) {
        String sql = """
            SELECT s.ma_suat_chieu, p.ten_phong, DATE_FORMAT(s.bat_dau_luc, '%H:%i') AS gio,
                   s.gia_co_ban
            FROM suat_chieu s
            JOIN phong p ON s.ma_phong = p.ma_phong
            WHERE s.ma_phim = ? AND DATE(s.bat_dau_luc) = CURDATE()
            ORDER BY s.bat_dau_luc;
        """;

        StringBuilder info = new StringBuilder("🎞️ Suất chiếu hôm nay của phim: " + tenPhim + "\n\n");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, maPhim);
            ResultSet rs = ps.executeQuery();

            boolean found = false;
            while (rs.next()) {
                found = true;
                info.append("🕒 ")
                        .append(rs.getString("gio"))
                        .append("  |  Phòng: ")
                        .append(rs.getString("ten_phong"))
                        .append("  |  Giá: ")
                        .append(rs.getDouble("gia_co_ban"))
                        .append(" VNĐ\n");
            }

            if (!found) info.append("⚠️ Không có suất chiếu nào trong ngày hôm nay.");

        } catch (SQLException e) {
            info.append("⚠️ Lỗi khi tải dữ liệu suất chiếu.");
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Suất chiếu phim");
        alert.setHeaderText(null);
        alert.setContentText(info.toString());
        alert.showAndWait();
    }

    // 🔹 Đăng xuất quay lại màn hình đăng nhập
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
            System.err.println("❌ Lỗi khi đăng xuất: " + e.getMessage());
        }
    }
}
