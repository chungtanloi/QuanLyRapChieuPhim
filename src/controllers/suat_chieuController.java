package controllers;

import database.DBConnection;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;

public class suat_chieuController {

    @FXML
    private TableView<SuatChieuVM> tblSuatChieu;
    @FXML
    private TableColumn<SuatChieuVM, ImageView> colPoster;
    @FXML
    private TableColumn<SuatChieuVM, String> colPhim, colPhong, colGio, colTrangThai;
    @FXML
    private TableColumn<SuatChieuVM, BigDecimal> colGia;
    @FXML
    private TableColumn<SuatChieuVM, Void> colHanhDong;
    @FXML
    private Pagination paginationSuatChieu;

    private static final int PAGE_SIZE = 12;

    private LocalDate ngay = LocalDate.now();

    private java.sql.Date sqlDate(LocalDate d) {
        return java.sql.Date.valueOf(d);
    }

    @FXML
    private void initialize() {

        colPhim.setCellValueFactory(new PropertyValueFactory<>("tenPhim"));
        colPhong.setCellValueFactory(new PropertyValueFactory<>("tenPhong"));
        colGio.setCellValueFactory(new PropertyValueFactory<>("gio"));
        colGia.setCellValueFactory(new PropertyValueFactory<>("gia"));

        colGia.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal v, boolean empty) {
                super.updateItem(v, empty);
                setText(empty || v == null ? null : String.format("%,.0f đ", v));
            }
        });

        colTrangThai.setCellValueFactory(new PropertyValueFactory<>("trangThai"));

        colPoster.setCellFactory(tc -> new TableCell<>() {
            private final ImageView img = new ImageView();

            {
                img.setFitWidth(60);
                img.setFitHeight(80);
                img.setPreserveRatio(true);
            }

            @Override
            protected void updateItem(ImageView ignore, boolean empty) {
                super.updateItem(ignore, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }
                SuatChieuVM vm = getTableView().getItems().get(getIndex());
                img.setImage(loadPoster(vm.getPosterUrl()));
                setGraphic(img);
            }
        });

        colHanhDong.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("Bán vé");

            {
                btn.setStyle("-fx-background-color:#2563eb; -fx-text-fill:white; -fx-font-weight:bold;");
                btn.setOnAction(e -> {
                    SuatChieuVM vm = getTableView().getItems().get(getIndex());
                    if (vm != null) {
                        try {
                            openSeatDialog(vm.getMaSuatChieu(), vm.getTenPhim(), vm.getTenPhong(), vm.getGio());
                        } catch (SQLException ex) {
                            showError("Lỗi tải ghế", ex.getMessage());
                        }
                    }
                });
            }

            @Override
            protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                setGraphic(empty ? null : btn);
            }
        });

        paginationSuatChieu.setPageFactory(this::loadPage);
        paginationSuatChieu.setMaxPageIndicatorCount(10);

        refresh();
    }

    private void refresh() {
        int total = countSuatChieu();
        int pages = Math.max(1, (int) Math.ceil(total / (double) PAGE_SIZE));
        paginationSuatChieu.setPageCount(pages);
        paginationSuatChieu.setCurrentPageIndex(0);
    }
// ======================= HÀM GỌI TỪ FILE KHÁC =======================

    public void loadSuatChieuTable() {
        refresh();  // reset lại số trang, load lại dữ liệu
        paginationSuatChieu.setCurrentPageIndex(0); // quay về trang đầu
        loadPage(0); // load lại trang 0 để TableView hiển thị dữ liệu mới
    }

    private int countSuatChieu() {

        final String sql = """
            SELECT COUNT(*)
            FROM suat_chieu sc
            JOIN phim p ON sc.ma_phim = p.ma_phim
            JOIN phong ph ON sc.ma_phong = ph.ma_phong
            WHERE DATE(sc.bat_dau_luc) = ?
        """;

        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setDate(1, sqlDate(ngay));
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;

        } catch (Exception e) {
            return 0;
        }
    }

    private Image loadPoster(String url) {
        try {
            return (url == null || url.isBlank())
                    ? new Image(Objects.requireNonNull(getClass().getResource(
                            "/javafx/scene/control/skin/caspian/dialog-confirm.png")).toExternalForm())
                    : new Image(url, true);
        } catch (Exception e) {
            return new Image(Objects.requireNonNull(getClass().getResource(
                    "/javafx/scene/control/skin/caspian/dialog-confirm.png")).toExternalForm());
        }
    }

    private javafx.scene.Node loadPage(int pageIndex) {

        ObservableList<SuatChieuVM> data = FXCollections.observableArrayList();

        final String sql = """
            SELECT sc.ma_suat_chieu, p.ten_phim, ph.ten_phong,
                   DATE_FORMAT(sc.bat_dau_luc,'%H:%i') AS gio,
                   sc.gia_co_ban, p.poster_url, sc.bat_dau_luc,
                   (SELECT COUNT(*) FROM ve v WHERE v.ma_suat_chieu = sc.ma_suat_chieu AND v.trang_thai = 'SAN_SANG') AS con_ve
            FROM suat_chieu sc
            JOIN phim p ON sc.ma_phim = p.ma_phim
            JOIN phong ph ON sc.ma_phong = ph.ma_phong
            WHERE DATE(sc.bat_dau_luc) = ?
            ORDER BY sc.bat_dau_luc
            LIMIT ? OFFSET ?
        """;

        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setDate(1, sqlDate(ngay));
            ps.setInt(2, PAGE_SIZE);
            ps.setInt(3, pageIndex * PAGE_SIZE);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {

                Timestamp bd = rs.getTimestamp("bat_dau_luc");
                int conVe = rs.getInt("con_ve");

                String trangThai = getTrangThai(bd);
                if (conVe == 0) {
                    trangThai = "HẾT VÉ";
                }

                data.add(new SuatChieuVM(
                        rs.getInt("ma_suat_chieu"),
                        rs.getString("ten_phim"),
                        rs.getString("ten_phong"),
                        rs.getString("gio"),
                        rs.getBigDecimal("gia_co_ban"),
                        trangThai,
                        rs.getString("poster_url")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        tblSuatChieu.setItems(data);
        return tblSuatChieu;
    }

    private String getTrangThai(Timestamp batDau) {
        long minutes = (batDau.getTime() - System.currentTimeMillis()) / 60000;
        if (minutes > 30) {
            return "Sắp chiếu";
        }
        if (minutes >= -120) {
            return "Đang chiếu";
        }
        return "Đã chiếu";
    }

    // ======================== BÁN VÉ =========================
    private void openSeatDialog(int maSC, String tenPhim, String tenPhong, String gio) throws SQLException {

        Stage stage = new Stage();

        VBox root = new VBox(10);
        root.setStyle("-fx-padding:14;");

        Label lbl = new Label("Suất: " + tenPhim + " - " + tenPhong + " (" + gio + ")");
        Label hint = new Label("Xanh lá = trống, Xanh dương = đã chọn");

        TilePane tile = new TilePane(5, 5);
        tile.setPrefColumns(10);

        BigDecimal[] tong = {BigDecimal.ZERO};
        List<Integer> selected = new ArrayList<>();

        final String sql = """
            SELECT g.ma_ghe, g.hang_ghe, g.so_ghe, v.gia_ban
            FROM ve v
            JOIN ghe g ON v.ma_ghe = g.ma_ghe
            WHERE v.ma_suat_chieu = ? AND v.trang_thai = 'SAN_SANG'
            ORDER BY g.hang_ghe, g.so_ghe
        """;

        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, maSC);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                int maGhe = rs.getInt("ma_ghe");
                String label = rs.getString("hang_ghe") + rs.getInt("so_ghe");
                BigDecimal gia = rs.getBigDecimal("gia_ban");

                Button b = new Button(label);
                b.setPrefSize(40, 40);
                b.setStyle("-fx-background-color:#16a34a; -fx-text-fill:white; -fx-font-weight:bold;");

                b.setOnAction(e -> {

                    if (selected.contains(maGhe)) {
                        selected.remove((Integer) maGhe);
                        b.setStyle("-fx-background-color:#16a34a; -fx-text-fill:white;");
                        tong[0] = tong[0].subtract(gia);
                    } else {
                        selected.add(maGhe);
                        b.setStyle("-fx-background-color:#2563eb; -fx-text-fill:white;");
                        tong[0] = tong[0].add(gia);
                    }
                });

                tile.getChildren().add(b);
            }
        }

        Button ok = new Button("Xác nhận bán vé");
        ok.setStyle("-fx-background-color:#2563eb; -fx-text-fill:white; -fx-font-weight:bold;");
        ok.setOnAction(e -> {
            if (selected.isEmpty()) {
                showWarn("Chọn ít nhất 1 ghế!");
                return;
            }
            try {
                commitTickets(maSC, selected, tong[0]);
                stage.close();
            } catch (Exception ex) {
                showError("Lỗi bán vé", ex.getMessage());
            }
        });

        root.getChildren().addAll(lbl, hint, tile, ok);
        stage.setScene(new Scene(root, 500, 450));
        stage.show();
    }

    private void commitTickets(int maSC, List<Integer> seats, BigDecimal tongTien) throws SQLException {

        try (Connection c = DBConnection.getConnection()) {

            c.setAutoCommit(false);

            int maDon;

            final String sqlDon = """
                INSERT INTO don_hang(kenh,trang_thai,tong_tien)
                VALUES('TRUC_TIEP','DA_THANH_TOAN',?)
            """;

            try (PreparedStatement ps = c.prepareStatement(sqlDon, Statement.RETURN_GENERATED_KEYS)) {

                ps.setBigDecimal(1, tongTien);
                ps.executeUpdate();

                ResultSet rs = ps.getGeneratedKeys();
                if (!rs.next()) {
                    throw new SQLException("Không thể tạo đơn hàng!");
                }
                maDon = rs.getInt(1);
            }

            final String sqlVe = """
                UPDATE ve
                SET trang_thai='DA_BAN', ban_luc=NOW(), ma_don_hang=?
                WHERE ma_suat_chieu=? AND ma_ghe=? AND trang_thai='SAN_SANG'
            """;

            try (PreparedStatement ps = c.prepareStatement(sqlVe)) {

                for (int ghe : seats) {
                    ps.setInt(1, maDon);
                    ps.setInt(2, maSC);
                    ps.setInt(3, ghe);
                    ps.addBatch();
                }

                ps.executeBatch();
            }

            c.commit();

            Alert a = new Alert(Alert.AlertType.INFORMATION);
            a.setContentText("Bán " + seats.size() + " vé thành công!\nTổng tiền: " + tongTien);
            a.show();
        }
    }

    private void showError(String h, String m) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setHeaderText(h);
        a.setContentText(m);
        a.show();
    }

    private void showWarn(String m) {
        new Alert(Alert.AlertType.WARNING, m).show();
    }

    // ===================== VIEWMODEL =======================
    public static class SuatChieuVM {

        private final IntegerProperty maSuatChieu = new SimpleIntegerProperty();
        private final StringProperty tenPhim = new SimpleStringProperty();
        private final StringProperty tenPhong = new SimpleStringProperty();
        private final StringProperty gio = new SimpleStringProperty();
        private final ObjectProperty<BigDecimal> gia = new SimpleObjectProperty<>();
        private final StringProperty trangThai = new SimpleStringProperty();
        private final StringProperty posterUrl = new SimpleStringProperty();

        public SuatChieuVM(int ma, String phim, String phong,
                String gio, BigDecimal gia,
                String trangThai, String poster) {

            this.maSuatChieu.set(ma);
            this.tenPhim.set(phim);
            this.tenPhong.set(phong);
            this.gio.set(gio);
            this.gia.set(gia);
            this.trangThai.set(trangThai);
            this.posterUrl.set(poster);
        }

        public int getMaSuatChieu() {
            return maSuatChieu.get();
        }

        public String getPosterUrl() {
            return posterUrl.get();
        }

        public String getTenPhim() {
            return tenPhim.get();
        }

        public String getTenPhong() {
            return tenPhong.get();
        }

        public String getGio() {
            return gio.get();
        }

        public String getTrangThai() {
            return trangThai.get();
        }

        public BigDecimal getGia() {
            return gia.get();
        }
    }
}
