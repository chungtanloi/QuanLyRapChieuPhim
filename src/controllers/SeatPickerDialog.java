package controllers;

import database.DBConnection;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.sql.*;
import java.util.*;

public class SeatPickerDialog extends Stage {

    private final int suatChieuId;
    private final String tenPhim;

    private final GridPane grid = new GridPane();
    private final Label lblTotal = new Label("0 đ");

    private final Map<Integer, Button> seatButtons = new HashMap<>();
    private final Set<Integer> selectedSeatIds = new HashSet<>();
    private long giaVeCoBan = 0;

    // KẾT QUẢ: danh sách ma_ghe đã chọn
    public static class Result {
        public final List<Integer> veIds;
        public Result(List<Integer> ids) { this.veIds = ids; }
    }

    public SeatPickerDialog(int suatChieuId, String tenPhim) {
        this.suatChieuId = suatChieuId;
        this.tenPhim = tenPhim;

        initModality(Modality.APPLICATION_MODAL);
        setTitle("Chọn ghế – " + tenPhim);

        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.TOP_CENTER);
        root.setStyle("-fx-background-color: #f3f4f6;");

        Label lblTitle = new Label("Chọn ghế (click để chọn / bỏ chọn)");
        lblTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        Label lblScreen = new Label("– MÀN HÌNH –");
        lblScreen.setStyle("-fx-font-size: 16px; -fx-text-fill: #2563eb;");

        grid.setHgap(10);
        grid.setVgap(10);
        grid.setAlignment(Pos.CENTER);

        // Legend
        HBox legend = new HBox(20);
        legend.setAlignment(Pos.CENTER);
        legend.getChildren().addAll(
                makeLegend("Còn trống", "#ffffff"),
                makeLegend("Đang chọn", "#3b82f6"),
                makeLegend("Đã bán", "#1f2937")
        );

        // Tổng tiền
        HBox totalBox = new HBox(10);
        totalBox.setAlignment(Pos.CENTER);
        lblTotal.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: red;");
        totalBox.getChildren().addAll(new Label("Tổng tiền:"), lblTotal);

        Button btnOK = new Button("Xác nhận");
        btnOK.setStyle("-fx-background-color: #16a34a; -fx-text-fill: white; -fx-padding: 10 20; -fx-font-size: 16px;");
        btnOK.setOnAction(e -> {
            this.setUserData(new Result(new ArrayList<>(selectedSeatIds)));
            this.close();
        });

        root.getChildren().addAll(lblTitle, lblScreen, grid, legend, totalBox, btnOK);

        loadSeatMap();

        Scene scene = new Scene(root, 600, 700);
        setScene(scene);
    }

    private HBox makeLegend(String text, String bg) {
        Button box = new Button("  ");
        box.setDisable(true);
        box.setStyle("-fx-background-color: " + bg + "; -fx-border-color: #000; -fx-min-width:20; -fx-min-height:20;");
        Label lb = new Label(text);
        return new HBox(5, box, lb);
    }

    // ========================= LOAD SEAT MAP =============================
    private void loadSeatMap() {
        try (Connection c = DBConnection.getConnection()) {

            // 1. Giá vé cơ bản
            try (PreparedStatement ps0 = c.prepareStatement(
                    "SELECT gia_co_ban FROM suat_chieu WHERE ma_suat_chieu = ?")) {
                ps0.setInt(1, suatChieuId);
                ResultSet rs0 = ps0.executeQuery();
                if (rs0.next()) giaVeCoBan = rs0.getLong(1);
            }

            // 2. Danh sách ghế: dùng hang_ghe, so_ghe
            String sql = """
                SELECT g.ma_ghe,
                       g.hang_ghe,
                       g.so_ghe,
                       CASE WHEN dh.ma_don_hang IS NOT NULL THEN 1 ELSE 0 END AS da_ban
                FROM ghe g
                LEFT JOIN ve v
                       ON v.ma_ghe = g.ma_ghe
                      AND v.ma_suat_chieu = ?
                LEFT JOIN don_ve dv ON dv.ma_ve = v.ma_ve
                LEFT JOIN don_hang dh ON dh.ma_don_hang = dv.ma_don_hang
                                     AND dh.trang_thai = 1
                WHERE g.ma_phong = (SELECT ma_phong FROM suat_chieu WHERE ma_suat_chieu = ?)
                ORDER BY g.hang_ghe, g.so_ghe
            """;

            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setInt(1, suatChieuId);
                ps.setInt(2, suatChieuId);

                ResultSet rs = ps.executeQuery();

                grid.getChildren().clear();
                seatButtons.clear();
                selectedSeatIds.clear();

                while (rs.next()) {
                    int maGhe = rs.getInt("ma_ghe");
                    String hang = rs.getString("hang_ghe");
                    int so = rs.getInt("so_ghe");
                    boolean daBan = rs.getInt("da_ban") == 1;

                    String seatLabel = hang + so;

                    Button btn = new Button(seatLabel);
                    btn.setMinSize(45, 40);
                    btn.setStyle(defaultSeatStyle(daBan));

                    if (daBan) {
                        btn.setDisable(true);
                    }

                    final boolean isSold = daBan;
                    btn.setOnAction(e -> {
                        if (isSold) return;

                        if (selectedSeatIds.contains(maGhe)) {
                            selectedSeatIds.remove(maGhe);
                            btn.setStyle(defaultSeatStyle(false));
                        } else {
                            selectedSeatIds.add(maGhe);
                            btn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill:white; -fx-font-weight:bold; -fx-border-color:black;");
                        }
                        updateTotal();
                    });

                    seatButtons.put(maGhe, btn);

                    // Sắp xếp ghế theo đúng hàng / cột
                    int rowIndex = 0;
                    if (hang != null && !hang.isEmpty()) {
                        rowIndex = Character.toUpperCase(hang.charAt(0)) - 'A';
                        if (rowIndex < 0) rowIndex = 0;
                    }
                    int colIndex = Math.max(0, so - 1);

                    grid.add(btn, colIndex, rowIndex);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private String defaultSeatStyle(boolean sold) {
        if (sold) {
            return "-fx-background-color:#1f2937; -fx-text-fill:white; -fx-border-color:black;";
        } else {
            return "-fx-background-color:white; -fx-text-fill:black; -fx-border-color:black;";
        }
    }

    private void updateTotal() {
        long total = selectedSeatIds.size() * giaVeCoBan;
        lblTotal.setText(String.format("%,d đ", total));
    }
}