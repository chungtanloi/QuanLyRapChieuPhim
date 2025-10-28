package application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/models/home.fxml"));

            // Scene tự lấy kích thước theo FXML (prefSize)
            Scene scene = new Scene(root);

            primaryStage.setTitle("🎬 HỆ THỐNG QUẢN LÝ RẠP CHIẾU PHIM");
            primaryStage.setScene(scene);

            // Cho phép thay đổi kích thước cửa sổ
            primaryStage.setResizable(true);

            // (khuyến nghị) đặt kích thước tối thiểu để layout không vỡ
            primaryStage.setMinWidth(1024);
            primaryStage.setMinHeight(640);

            // (tuỳ chọn) mở lên ở trạng thái maximize
            // primaryStage.setMaximized(true);

            // (tuỳ chọn) canh giữa màn hình
            primaryStage.centerOnScreen();

            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
