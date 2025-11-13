package controllers;

import database.DBConnection;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;

import java.sql.*;
import java.time.LocalDate;
import java.util.Objects;

public class PhimController {
    @FXML private TilePane tilePhimDangChieu;

    private String kw, rap, phong, theloai, dinhdang;
    private boolean sapChieu;
    private LocalDate ngay;

    @FXML
    private void initialize() {
        // nothing yet
    }

    public void applyFilters(String kw, String rap, String phong, String theloai, String dinhdang, boolean sapChieu, LocalDate ngay) {
        this.kw = kw; this.rap = rap; this.phong = phong; this.theloai = theloai; this.dinhdang = dinhdang;
        this.sapChieu = sapChieu; this.ngay = ngay != null ? ngay : LocalDate.now();
        loadPhim();
    }

    private void loadPhim() {
        if (tilePhimDangChieu == null) return;
        tilePhimDangChieu.getChildren().clear();

        String sql = """
            SELECT ma_phim, ten_phim, thoi_luong_phut, phan_loai, poster_url
            FROM phim
            WHERE (? = '' OR ten_phim LIKE CONCAT('%', ?, '%'))
            ORDER BY ngay_phat_hanh DESC
            LIMIT 100
        """;

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, kw == null ? "" : kw);
            ps.setString(2, kw == null ? "" : kw);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    long ma = rs.getLong("ma_phim");
                    String ten = rs.getString("ten_phim");
                    int tl = rs.getInt("thoi_luong_phut");
                    String pl = rs.getString("phan_loai");
                    String poster = rs.getString("poster_url");
                    tilePhimDangChieu.getChildren().add(createCard(ma, ten, tl, pl, poster));
                }
            }
        } catch (SQLException ignored) {}
    }

    private VBox createCard(long maPhim, String tenPhim, int thoiLuong, String phanLoai, String posterUrl) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(10));
        card.setPrefWidth(220);
        card.setStyle("-fx-background-color: rgba(255,255,255,0.06); -fx-background-radius: 10; -fx-padding:10;");

        ImageView img = new ImageView();
        img.setFitWidth(200); img.setFitHeight(140); img.setPreserveRatio(true);
        try { if (posterUrl != null && !posterUrl.isBlank()) img.setImage(new Image(posterUrl, true)); }
        catch (Exception ignore) { img.setImage(new Image(Objects.requireNonNull(getClass().getResource("/javafx/scene/control/skin/caspian/dialog-confirm.png")).toExternalForm())); }

        Label name = new Label(tenPhim);
        Label info = new Label("⏱ " + thoiLuong + " phút  |  " + phanLoai);
        Tooltip.install(card, new Tooltip("Xem suất chiếu của " + tenPhim));

        card.getChildren().addAll(img, name, info);
        return card;
    }
}
