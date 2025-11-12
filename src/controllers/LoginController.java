package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import database.DBConnection;
import controllers.NhanVienController;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginController {

    @FXML private TextField txtEmail;
    @FXML private PasswordField txtPassword;
    @FXML private Label lblStatus;

    @FXML
    private void handleLogin(ActionEvent event) {
        String email = txtEmail.getText().trim();
        String password = txtPassword.getText().trim();

        if (email.isEmpty() || password.isEmpty()) {
            lblStatus.setText("⚠️ Vui lòng nhập đầy đủ thông tin!");
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT vai_tro, ho_ten FROM tai_khoan WHERE email = ? AND mat_khau_ma = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, email);
            ps.setString(2, password); 

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String role = rs.getString("vai_tro");
                String name = rs.getString("ho_ten");

                lblStatus.setText("✅ Đăng nhập thành công (" + role + ")!");

                // Gọi hàm mở trang phù hợp
                openByRole(event, role, name);

            } else {
                lblStatus.setText("❌ Sai email hoặc mật khẩu!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            lblStatus.setText("❌ Lỗi kết nối cơ sở dữ liệu!");
        }
    }

    private void openByRole(ActionEvent event, String role, String name) {
    try {
        FXMLLoader loader;
        Parent view;

        if ("QUAN_TRI".equalsIgnoreCase(role)) {
            loader = new FXMLLoader(getClass().getResource("/models/form.fxml"));
            view = loader.load();

        } else if ("NHAN_VIEN".equalsIgnoreCase(role)) {
            loader = new FXMLLoader(getClass().getResource("/models/nhanvien.fxml"));
            view = loader.load();
            NhanVienShellController nvController = loader.getController();
            nvController.setTenNhanVien(name);
        } else {
            loader = new FXMLLoader(getClass().getResource("/models/customer_UI.fxml"));
            view = loader.load();
        }

        Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(view));
        stage.centerOnScreen();
        stage.setTitle("🎬 Xin chào " + name + " (" + role + ")");
        stage.show();

    } catch (Exception e) {
        e.printStackTrace();
        lblStatus.setText("❌ Không thể mở giao diện phù hợp với vai trò!");
    }
}


    @FXML
private void openSignup(ActionEvent event) {
    try {
        Parent signupView = FXMLLoader.load(getClass().getResource("/models/signup.fxml"));
        Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(signupView));
        stage.centerOnScreen();
        stage.setTitle("🎬 Đăng ký tài khoản");
        stage.show();
    } catch (Exception e) {
        e.printStackTrace();
        lblStatus.setText("❌ Không thể mở trang đăng ký!");
    }
}

}
