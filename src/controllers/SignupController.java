package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import database.DBConnection;

import java.sql.CallableStatement;
import java.sql.Connection;

public class SignupController {

    @FXML private TextField txtName;
    @FXML private TextField txtEmail;
    @FXML private PasswordField txtPassword;
    @FXML private PasswordField txtConfirm;
    @FXML private Label lblStatus;

    @FXML
    private void handleSignup(ActionEvent event) {
        String name = txtName.getText().trim();
        String email = txtEmail.getText().trim();
        String pass = txtPassword.getText().trim();
        String confirm = txtConfirm.getText().trim();

        if (name.isEmpty() || email.isEmpty() || pass.isEmpty() || confirm.isEmpty()) {
            lblStatus.setText("⚠️ Vui lòng nhập đầy đủ thông tin!");
            return;
        }
        if (!pass.equals(confirm)) {
            lblStatus.setText("⚠️ Mật khẩu nhập lại không khớp!");
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            CallableStatement cs = conn.prepareCall("{CALL sp_dangky_khachhang(?, ?, ?)}");
            cs.setString(1, email);
            cs.setString(2, pass);
            cs.setString(3, name);
            cs.execute();
            lblStatus.setText("✅ Đăng ký thành công!");
            backToLogin(event);
        } catch (Exception e) {
            e.printStackTrace();
            lblStatus.setText("⚠️ Email đã tồn tại hoặc lỗi hệ thống!");
        }
    }

    @FXML
    private void backToLogin(ActionEvent event) {
        try {
            Parent login = FXMLLoader.load(getClass().getResource("/models/login.fxml"));
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(login));
            stage.centerOnScreen();
            stage.setTitle("🎬 Đăng nhập hệ thống");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
