package application; // hoặc Application; giữ đúng với file của bạn

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;
import java.util.Objects;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        // DÙNG ĐƯỜNG DẪN TUYỆT ĐỐI (có dấu / ở đầu)
        URL url = Objects.requireNonNull(
                Main.class.getResource("/models/login.fxml"),
                "Không tìm thấy /models/home.fxml trên classpath. Hãy đặt file vào src/models/"
        );
        System.out.println("FXML URL = " + url); // debug: phải không null

        Parent root = FXMLLoader.load(url);
        stage.setTitle("🎬 HỆ THỐNG QUẢN LÝ RẠP CHIẾU PHIM");
        stage.setScene(new Scene(root));
        stage.show();
    }

    public static void main(String[] args) { launch(args); }
}
