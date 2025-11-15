package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class FormController implements Initializable {

    @FXML private BorderPane root;
    @FXML private VBox sidebar;
    @FXML private Button btnLogout;
    @FXML private ScrollPane scroll;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Load trang chủ mặc định khi khởi động
        try {
            loadPage("/models/home.fxml");
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Lỗi", "Không thể tải trang chủ: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    /**
     * Xử lý điều hướng giữa các trang
     */
    @FXML
    private void handleNav(javafx.event.ActionEvent event) {
        try {
            Button btn = (Button) event.getSource();
            String fxmlPath = (String) btn.getUserData();
            
            if (fxmlPath != null && !fxmlPath.isEmpty()) {
                // Xóa class "active" khỏi tất cả các button trong sidebar
                for (javafx.scene.Node node : sidebar.getChildren()) {
                    if (node instanceof Button) {
                        node.getStyleClass().remove("active");
                    }
                }
                
                // Thêm class "active" cho button được click
                btn.getStyleClass().add("active");
                
                // Tải FXML mới
                loadPage(fxmlPath);
            }
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Lỗi", "Không thể tải trang: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    /**
     * Load trang mới vào ScrollPane
     */
    private void loadPage(String fxmlPath) throws IOException {
        URL url = getClass().getResource(fxmlPath);
        if (url == null) {
            throw new IOException("Không tìm thấy file: " + fxmlPath);
        }
        
        Parent newContent = FXMLLoader.load(url);
        scroll.setContent(newContent);
    }

    /**
     * Xử lý đăng xuất
     */
    @FXML
    private void onLogout(javafx.event.ActionEvent event) {
        try {
            // Tải lại trang đăng nhập
            Parent loginRoot = FXMLLoader.load(getClass().getResource("/models/login.fxml"));
            Stage stage = (Stage) btnLogout.getScene().getWindow();
            stage.setScene(new Scene(loginRoot));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Lỗi", "Không thể đăng xuất: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    /**
     * Hiển thị hộp thoại thông báo
     */
    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}