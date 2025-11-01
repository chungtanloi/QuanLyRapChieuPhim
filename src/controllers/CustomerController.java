
package controllers;

import database.DBConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;

import java.sql.*;
import java.time.LocalDate;
import java.util.*;

public class CustomerController {

    @FXML private TextField txtSearch;
    @FXML private DatePicker dpNgayChieu;
    @FXML private ComboBox<String> cbRap;
    @FXML private ComboBox<String> cbDinhDang;
    @FXML private TilePane tilePhim;
    @FXML private ListView<String> lvGioVe;
    @FXML private Label lblTong;

    private final ObservableList<String> gioVe = FXCollections.observableArrayList();
    private final List<Integer> selectedVeIds = new ArrayList<>();
    private long tongTien = 0;

    @FXML
    public void initialize() {
        dpNgayChieu.setValue(LocalDate.now());
        cbRap.setItems(FXCollections.observableArrayList("Tất cả rạp"));
        cbRap.getSelectionModel().selectFirst();
        cbDinhDang.setItems(FXCollections.observableArrayList("Tất cả định dạng", "2D", "3D", "IMAX", "4DX"));
        cbDinhDang.getSelectionModel().selectFirst();
        lvGioVe.setItems(gioVe);

        addListeners();
        loadPhim();
    }

    private void addListeners() {
        txtSearch.textProperty().addListener((obs, o, n) -> loadPhim());
        dpNgayChieu.valueProperty().addListener((obs, o, n) -> loadPhim());
        cbDinhDang.valueProperty().addListener((obs, o, n) -> loadPhim());
    }

    public void onRefresh() {
        txtSearch.clear();
        dpNgayChieu.setValue(LocalDate.now());
        cbRap.getSelectionModel().selectFirst();
        cbDinhDang.getSelectionModel().selectFirst();
        loadPhim();
    }

    private void loadPhim() {
        tilePhim.getChildren().clear();
        String keyword = txtSearch.getText() == null ? "" : txtSearch.getText().trim();
        LocalDate ngay = dpNgayChieu.getValue();

        String sql =
            "SELECT p.ma_phim, p.ten_phim, p.thoi_luong_phut, p.poster_url, " +
            "       (SELECT MIN(v.gia_ban) " +
            "        FROM suat_chieu sc " +
            "        JOIN ve v ON v.ma_suat_chieu = sc.ma_suat_chieu " +
            "        WHERE sc.ma_phim = p.ma_phim AND DATE(sc.bat_dau_luc) = ?) AS gia_min " +
            "FROM phim p " +
            "WHERE (? = '' OR p.ten_phim LIKE CONCAT('%', ?, '%')) " +
            "ORDER BY p.ten_phim";

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setDate(1, java.sql.Date.valueOf(ngay));
            ps.setString(2, keyword);
            ps.setString(3, keyword);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String ma = rs.getString("ma_phim");
                    String ten = rs.getString("ten_phim");
                    int thoiluong = rs.getInt("thoi_luong_phut");
                    int giaMin = rs.getObject("gia_min") != null ? rs.getInt("gia_min") : 0;
                    String poster = null;
                    try { poster = rs.getString("poster_url"); } catch (Exception ignore) {}

                    tilePhim.getChildren().add(makeMovieCard(ma, ten, thoiluong, giaMin, poster));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, e.getMessage(), ButtonType.OK).showAndWait();
        }
    }

    private VBox makeMovieCard(String ma, String ten, int thoiluong, int giaMin, String posterUrl) {
        VBox card = new VBox(8);
        card.getStyleClass().add("card");
        card.setPadding(new Insets(10));

        ImageView poster = new ImageView();
        poster.setFitWidth(190);
        poster.setFitHeight(255);
        poster.setPreserveRatio(true);
        try {
            if (posterUrl != null && !posterUrl.isBlank())
                poster.setImage(new Image(posterUrl, true));
        } catch (Exception ignore) {}

        Label lblTen = new Label(ten);
        lblTen.getStyleClass().add("card-title");

        Label lblMeta = new Label(thoiluong + " phút");
        lblMeta.getStyleClass().add("muted");

        Label lblGia = new Label(giaMin > 0 ? String.format("%,d đ", giaMin) : "—");
        lblGia.getStyleClass().add("price");

        Button btnXem = new Button("Xem suất & chọn ghế");
        btnXem.setOnAction(e -> chooseShowtimeAndPickSeats(ma, ten));

        card.getChildren().addAll(poster, lblTen, lblMeta, lblGia, btnXem);
        return card;
    }

    private void chooseShowtimeAndPickSeats(String maPhim, String tenPhim) {
        LocalDate ngay = dpNgayChieu.getValue();
        List<Showtime> showtimes = fetchShowtimes(maPhim, ngay);
        if (showtimes.isEmpty()) {
            new Alert(Alert.AlertType.INFORMATION, "Không có suất phù hợp trong ngày.", ButtonType.OK).showAndWait();
            return;
        }
        Map<String, Showtime> map = new LinkedHashMap<>();
        for (Showtime s : showtimes) {
            String label = String.format("#%d • %s • Phòng %s • %s • base %s đ",
                    s.id, s.gio, s.phong, s.dinhDang, String.format("%,d", s.basePrice));
            map.put(label, s);
        }
        ChoiceDialog<String> dlg = new ChoiceDialog<>(map.keySet().iterator().next(), map.keySet());
        dlg.setTitle("Chọn suất chiếu");
        dlg.setHeaderText("Phim: " + tenPhim + " (" + maPhim + ")  •  Ngày " + ngay);
        dlg.setContentText("Chọn một suất:");
        Optional<String> pick = dlg.showAndWait();
        if (pick.isEmpty()) return;
        Showtime chosen = map.get(pick.get());

        SeatPickerDialog seatDlg = new SeatPickerDialog(chosen.id, tenPhim);
        Optional<SeatPickerDialog.Result> res = seatDlg.showAndWait();
        res.ifPresent(r -> {
            for (int veId : r.veIds) {
                addToCart("Vé #" + veId + " • " + tenPhim, 1, getGiaVeById(veId));
                selectedVeIds.add(veId);
            }
        });
    }

    private static class Showtime {
        int id;
        String gio;
        String phong;
        String dinhDang;
        int basePrice;
    }

    private List<Showtime> fetchShowtimes(String maPhim, LocalDate ngay) {
        String sql = "SELECT sc.ma_suat_chieu, TIME(sc.bat_dau_luc) AS gio_bat_dau, " +
                     "       sc.ma_phong, dd.ten_dinh_dang, sc.gia_co_ban " +
                     "FROM suat_chieu sc " +
                     "JOIN dinh_dang dd ON dd.ma_dinh_dang = sc.ma_dinh_dang " +
                     "WHERE sc.ma_phim = ? AND DATE(sc.bat_dau_luc) = ? " +
                     "ORDER BY sc.bat_dau_luc";
        List<Showtime> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, maPhim);
            ps.setDate(2, java.sql.Date.valueOf(ngay));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Showtime s = new Showtime();
                    s.id = rs.getInt("ma_suat_chieu");
                    s.gio = rs.getString("gio_bat_dau");
                    s.phong = rs.getString("ma_phong");
                    s.dinhDang = rs.getString("ten_dinh_dang");
                    s.basePrice = rs.getInt("gia_co_ban");
                    list.add(s);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    private int getGiaVeById(int veId) {
        String sql = "SELECT gia_ban FROM ve WHERE ma_ve = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, veId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public void onCheckout() {
        if (selectedVeIds.isEmpty()) {
            new Alert(Alert.AlertType.INFORMATION, "Giỏ vé đang trống.", ButtonType.OK).showAndWait();
            return;
        }
        String jsonArray = buildJsonArray(selectedVeIds);
        String call = "CALL sp_dat_ve(?, ?, 'CHUYEN_KHOAN')";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(call)) {
            ps.setNull(1, Types.BIGINT);
            ps.setString(2, jsonArray);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    long donId = rs.getLong("ma_don_hang");
                    new Alert(Alert.AlertType.INFORMATION,
                            "Đặt vé thành công! Mã đơn: " + donId, ButtonType.OK).showAndWait();
                    selectedVeIds.clear();
                    gioVe.clear();
                    tongTien = 0;
                    lblTong.setText("0 đ");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, e.getMessage(), ButtonType.OK).showAndWait();
        }
    }

    private static String buildJsonArray(List<Integer> ids) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < ids.size(); i++) {
            if (i > 0) sb.append(',');
            sb.append(ids.get(i));
        }
        sb.append(']');
        return sb.toString();
    }

    private void addToCart(String label, int soLuong, int donGia) {
        gioVe.add(label + "  x" + soLuong + "  •  " + String.format("%,d đ", donGia));
        tongTien += (long) soLuong * donGia;
        lblTong.setText(String.format("%,d đ", tongTien));
    }
}
