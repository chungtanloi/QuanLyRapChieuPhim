package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;

public class HomeController {

    @FXML
    private Label lblTitle;

    @FXML
    private Button btnDatVe;
    @FXML
    private ImageView imgBanner;

    // Hàm khởi tạo khi giao diện load
    public void initialize() {
        lblTitle.setText("🎥 Phim hôm nay: The Avengers - Chiếu lúc 19:00!");
    }

    // Các hàm xử lý khi nhấn nút
    @FXML
    private void handleDatVe() {
        lblTitle.setText("Bạn đã chọn chức năng Đặt Vé!");
        System.out.println("→ Người dùng nhấn nút Đặt Vé");
    }

    @FXML
    private void goHome() {
        lblTitle.setText("Đang ở Trang Chủ 🎬");
    }

    @FXML
    private void goPhim() {
        lblTitle.setText("Xem danh sách Phim đang chiếu!");
    }

    @FXML
    private void goLichChieu() {
        lblTitle.setText("Lịch chiếu hôm nay 📅");
    }

    @FXML
    private void goDatVe() {
        lblTitle.setText("Trang Đặt Vé 🎟");
    }

    @FXML
    private void goLogin() {
        lblTitle.setText("Đăng nhập hệ thống 🔐");
    }
}
