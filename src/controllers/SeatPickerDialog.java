
package controllers;

import database.DBConnection;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * SeatPickerDialog (FIX): dùng setResultConverter để đảm bảo trả kết quả khi nhấn OK.
 * Hiển thị sơ đồ theo hang_ghe + so_ghe, chọn ghế SAN_SANG, tính tổng tiền.
 */
public class SeatPickerDialog extends Dialog<SeatPickerDialog.Result> {

    public static class Result {
        public final List<Integer> veIds;
        public final long tongTien;
        public Result(List<Integer> veIds, long tongTien) {
            this.veIds = veIds; this.tongTien = tongTien;
        }
    }

    private static class SeatCell {
        int veId;
        String hangGhe;
        int soGhe;
        String trangThai;
        int giaBan;
        int rowIndex;
        int colIndex;
        Button btn;
        boolean selected = false;
        String label() { return hangGhe + soGhe; }
    }

    private final int maSuatChieu;
    private final String tenPhim;
    private final GridPane grid = new GridPane();
    private final Label lblTotal = new Label("0 đ");
    private final List<SeatCell> allCells = new ArrayList<>();

    public SeatPickerDialog(int maSuatChieu, String tenPhim) {
        this.maSuatChieu = maSuatChieu;
        this.tenPhim = tenPhim;

        setTitle("Chọn ghế cho suất #" + maSuatChieu);
        DialogPane pane = getDialogPane();
        pane.getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(12));

        Label title = new Label("🎬 " + tenPhim + "  •  Suất #" + maSuatChieu);
        title.getStyleClass().add("card-title");
        root.setTop(title);
        BorderPane.setMargin(title, new Insets(0,0,8,0));

        ScrollPane sp = new ScrollPane(grid);
        sp.setFitToWidth(true);
        sp.setPrefViewportHeight(420);
        root.setCenter(sp);

        HBox footer = new HBox(16);
        footer.setAlignment(Pos.CENTER_LEFT);
        footer.getChildren().addAll(makeLegend(), new Region(), new Label("Tổng:"), lblTotal);
        HBox.setHgrow(footer.getChildren().get(1), Priority.ALWAYS);
        root.setBottom(footer);
        BorderPane.setMargin(footer, new Insets(8,0,0,0));

        pane.setContent(root);

        try {
            loadSeats();
            layoutGrid();
        } catch (SQLException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, e.getMessage(), ButtonType.OK).showAndWait();
        }

        // VALIDATE trước khi đóng: nếu chưa chọn ghế thì chặn
        final Button okBtn = (Button) pane.lookupButton(ButtonType.OK);
        okBtn.addEventFilter(javafx.event.ActionEvent.ACTION, evt -> {
            if (getSelectedIds().isEmpty()) {
                evt.consume();
                new Alert(Alert.AlertType.INFORMATION, "Bạn chưa chọn ghế nào.", ButtonType.OK).showAndWait();
            }
        });

        // Trả kết quả chuẩn bằng result converter
        setResultConverter(button -> {
    if (button == ButtonType.OK) {
        List<Integer> ids = getSelectedIds();
        long sum = getSelectedSum();
        return new Result(ids, sum);
    }
    return null;
});

    }

    private Node makeLegend() {
        HBox box = new HBox(12);
        box.setAlignment(Pos.CENTER_LEFT);
        box.getChildren().addAll(
                legendItem("# Available", "-fx-background-color: -fx-card-bg; -fx-border-color: #94a3b8;"),
                legendItem("# Selected", "-fx-background-color: #dbeafe; -fx-border-color: #1d4ed8;"),
                legendItem("# Sold",     "-fx-background-color: #e5e7eb; -fx-text-fill: #9ca3af; -fx-border-color: #9ca3af;"),
                legendItem("# Held",     "-fx-background-color: #fff1f2; -fx-border-color: #f43f5e;")
        );
        return box;
    }

    private Node legendItem(String text, String style) {
        HBox item = new HBox(6);
        Rectangle r = new Rectangle(18,18);
        r.setStyle(style);
        item.setAlignment(Pos.CENTER_LEFT);
        item.getChildren().addAll(r, new Text(text));
        return item;
    }

    private void loadSeats() throws SQLException {
        String sql = """
            SELECT v.ma_ve, v.trang_thai, v.gia_ban,
                   g.hang_ghe, g.so_ghe
            FROM ve v
            JOIN ghe g ON g.ma_ghe = v.ma_ghe
            WHERE v.ma_suat_chieu = ?
            ORDER BY g.hang_ghe, g.so_ghe
        """;
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, maSuatChieu);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    SeatCell sc = new SeatCell();
                    sc.veId = rs.getInt("ma_ve");
                    sc.trangThai = rs.getString("trang_thai");
                    sc.giaBan = rs.getInt("gia_ban");
                    sc.hangGhe = rs.getString("hang_ghe");
                    sc.soGhe = rs.getInt("so_ghe");
                    sc.rowIndex = lettersToIndex(sc.hangGhe);
                    sc.colIndex = sc.soGhe;
                    allCells.add(sc);
                }
            }
        }
    }

    private void layoutGrid() {
        grid.getChildren().clear();
        grid.setHgap(8);
        grid.setVgap(8);
        grid.setPadding(new Insets(12));

        int maxRow = 0, maxCol = 0;
        for (SeatCell s : allCells) {
            maxRow = Math.max(maxRow, s.rowIndex);
            maxCol = Math.max(maxCol, s.colIndex);
        }

        // Header cột
        for (int c = 1; c <= maxCol; c++) {
            Label lbl = new Label(String.valueOf(c));
            lbl.getStyleClass().add("muted");
            grid.add(lbl, c, 0);
        }

        // Ô ghế + header hàng
        Set<Integer> headerRows = new HashSet<>();
        for (SeatCell s : allCells) {
            if (!headerRows.contains(s.rowIndex)) {
                Label lblRow = new Label(indexToLetters(s.rowIndex));
                lblRow.getStyleClass().add("muted");
                grid.add(lblRow, 0, s.rowIndex);
                headerRows.add(s.rowIndex);
            }
            Button b = new Button(s.label());
            b.setPrefWidth(46);
            b.setMinHeight(34);
            b.setMaxHeight(34);
            b.setFocusTraversable(false);
            styleSeat(b, s.trangThai, false);
            final SeatCell ref = s;
            b.setOnAction(e -> toggleSeat(ref));
            s.btn = b;
            grid.add(b, s.colIndex, s.rowIndex);
        }
    }

    private void toggleSeat(SeatCell s) {
        if (!"SAN_SANG".equalsIgnoreCase(s.trangThai)) return;
        s.selected = !s.selected;
        styleSeat(s.btn, s.trangThai, s.selected);
        updateTotalLabel();
    }

    private void styleSeat(Button b, String status, boolean selected) {
        String base = "-fx-background-radius: 8; -fx-border-radius: 8; -fx-border-width: 1; -fx-padding: 6 10;";
        if ("SAN_SANG".equalsIgnoreCase(status)) {
            if (selected) b.setStyle(base + " -fx-background-color: #dbeafe; -fx-border-color: #1d4ed8;");
            else          b.setStyle(base + " -fx-background-color: -fx-card-bg; -fx-border-color: #94a3b8;");
            b.setDisable(false);
        } else if ("DA_BAN".equalsIgnoreCase(status)) {
            b.setStyle(base + " -fx-background-color: #e5e7eb; -fx-text-fill: #9ca3af; -fx-border-color: #9ca3af;");
            b.setDisable(true);
        } else {
            b.setStyle(base + " -fx-background-color: #fff1f2; -fx-border-color: #f43f5e;");
            b.setDisable(true);
        }
    }

    private void updateTotalLabel() {
        long sum = getSelectedSum();
        lblTotal.setText(String.format("%,d đ", sum));
    }

    private long getSelectedSum() {
        long s = 0;
        for (SeatCell c : allCells) if (c.selected) s += c.giaBan;
        return s;
    }

    private List<Integer> getSelectedIds() {
        List<Integer> ids = new ArrayList<>();
        for (SeatCell c : allCells) if (c.selected) ids.add(c.veId);
        return ids;
    }

    private static int lettersToIndex(String letters) {
        if (letters == null) return 1;
        String s = letters.trim().toUpperCase();
        int n = 0;
        for (int i = 0; i < s.length(); i++) {
            n = n * 26 + (s.charAt(i) - 'A' + 1);
        }
        return Math.max(n, 1);
    }

    private static String indexToLetters(int idx) {
        StringBuilder sb = new StringBuilder();
        int n = Math.max(idx, 1);
        while (n > 0) {
            int r = (n - 1) % 26;
            sb.insert(0, (char) ('A' + r));
            n = (n - 1) / 26;
        }
        return sb.toString();
    }
}
