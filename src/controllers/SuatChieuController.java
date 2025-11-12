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
import javafx.scene.layout.GridPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SuatChieuController {

    @FXML private TableView<SuatChieuVM> tblSuatChieu;
    @FXML private TableColumn<SuatChieuVM, ImageView> colPoster;
    @FXML private TableColumn<SuatChieuVM, String> colPhim, colPhong, colGio, colTrangThai;
    @FXML private TableColumn<SuatChieuVM, BigDecimal> colGia;
    @FXML private TableColumn<SuatChieuVM, Void> colHanhDong;
    @FXML private Pagination paginationSuatChieu;

    private static final int PAGE_SIZE = 12;

    // filter state (nhận từ shell nếu có)
    private String kw, rap, phong, theloai, dinhdang;
    private boolean conVe, sapChieu;
    private LocalDate ngay = LocalDate.now();    // luôn có giá trị mặc định

    // ===== Helpers =====
    private java.sql.Date sqlDate(LocalDate d) {
        return (d == null) ? new java.sql.Date(System.currentTimeMillis())
                           : java.sql.Date.valueOf(d);
    }
    private void info(String m){ new Alert(Alert.AlertType.INFORMATION, m).showAndWait(); }
    private void warn(String m){ new Alert(Alert.AlertType.WARNING, m).showAndWait(); }
    private void error(String h, String m){ Alert a=new Alert(Alert.AlertType.ERROR,m); a.setHeaderText(h); a.showAndWait(); }

    // ===== Lifecycle =====
    @FXML
    private void initialize() {
        if (tblSuatChieu == null) return;

        colPhim.setCellValueFactory(new PropertyValueFactory<>("tenPhim"));
        colPhong.setCellValueFactory(new PropertyValueFactory<>("tenPhong"));
        colGio.setCellValueFactory(new PropertyValueFactory<>("gio"));
        colGia.setCellValueFactory(new PropertyValueFactory<>("gia"));
        colGia.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(BigDecimal v, boolean empty) {
                super.updateItem(v, empty);
                setText(empty || v == null ? null : String.format("%,.0f đ", v));
            }
        });
        colTrangThai.setCellValueFactory(new PropertyValueFactory<>("trangThai"));

        if (colPoster != null) {
            colPoster.setCellFactory(tc -> new TableCell<>() {
                private final ImageView img = new ImageView();
                { img.setFitWidth(60); img.setFitHeight(80); img.setPreserveRatio(true); }
                @Override protected void updateItem(ImageView ignore, boolean empty) {
                    super.updateItem(ignore, empty);
                    if (empty) { setGraphic(null); return; }
                    SuatChieuVM vm = getTableView().getItems().get(getIndex());
                    img.setImage(loadPosterSafely(vm.getPosterUrl()));
                    setGraphic(img);
                }
            });
        }

        if (colHanhDong != null) {
            colHanhDong.setCellFactory(col -> new TableCell<>() {
                private final Button btn = new Button("Bán vé");
                {
                    btn.setStyle("-fx-background-color:#2563eb; -fx-text-fill:white; -fx-font-weight:bold; -fx-background-radius:6;");
                    btn.setOnAction(e -> {
                        SuatChieuVM vm = getTableView().getItems().get(getIndex());
                        if (vm != null) {
                            try {
                                openSeatDialog(vm.getMaSuatChieu(), vm.getTenPhim(), vm.getTenPhong(), vm.getGio());
                            } catch (SQLException ex) {
                                error("Lỗi tải ghế", ex.getMessage());
                            }
                        }
                    });
                }
                @Override protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    setGraphic(empty ? null : btn);
                }
            });
        }

        if (paginationSuatChieu != null) {
            paginationSuatChieu.setPageFactory(this::loadPage);
            paginationSuatChieu.setMaxPageIndicatorCount(10);
        }

        // lần đầu tải
        refresh(null);
    }

    public void applyFilters(String kw, String rap, String phong, String theloai, String dinhdang,
                             boolean conVe, boolean sapChieu, LocalDate ngay, Label lblTongSuat) {
        this.kw = kw; this.rap = rap; this.phong = phong; this.theloai = theloai; this.dinhdang = dinhdang;
        this.conVe = conVe; this.sapChieu = sapChieu; this.ngay = (ngay == null ? LocalDate.now() : ngay);
        refresh(lblTongSuat);
    }

    private void refresh(Label lblTongSuat) {
        if (paginationSuatChieu == null) return;
        int total = countSuatChieu();
        int pages = Math.max(1, (int) Math.ceil(total / (double) PAGE_SIZE));
        paginationSuatChieu.setPageCount(pages);
        paginationSuatChieu.setCurrentPageIndex(0);
        if (lblTongSuat != null) lblTongSuat.setText(total + " suất");
    }

    private int countSuatChieu() {
        final String sql = """
            SELECT COUNT(*)
            FROM suat_chieu sc
            JOIN phim  p  ON sc.ma_phim  = p.ma_phim
            JOIN phong ph ON sc.ma_phong = ph.ma_phong
            WHERE DATE(sc.bat_dau_luc) = ?
        """;
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setDate(1, sqlDate(ngay));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    private javafx.scene.Node loadPage(Integer pageIndex) {
        ObservableList<SuatChieuVM> data = FXCollections.observableArrayList();
        final String sql = """
            SELECT sc.ma_suat_chieu, p.ten_phim, ph.ten_phong,
                   DATE_FORMAT(sc.bat_dau_luc,'%H:%i') AS gio,
                   sc.gia_co_ban, p.poster_url, sc.bat_dau_luc
            FROM suat_chieu sc
            JOIN phim  p  ON sc.ma_phim  = p.ma_phim
            JOIN phong ph ON sc.ma_phong = ph.ma_phong
            WHERE DATE(sc.bat_dau_luc) = ?
            ORDER BY sc.bat_dau_luc
            LIMIT ? OFFSET ?
        """;
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setDate(1, sqlDate(ngay));
            ps.setInt(2, PAGE_SIZE);
            ps.setInt(3, pageIndex * PAGE_SIZE);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Timestamp bd = rs.getTimestamp("bat_dau_luc");
                    data.add(new SuatChieuVM(
                            rs.getInt("ma_suat_chieu"),
                            rs.getString("ten_phim"),
                            rs.getString("ten_phong"),
                            rs.getString("gio"),
                            rs.getBigDecimal("gia_co_ban"),
                            tinhTrangThai(bd),
                            rs.getString("poster_url")
                    ));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (tblSuatChieu != null) tblSuatChieu.setItems(data);
        return tblSuatChieu;
    }

    private Image loadPosterSafely(String posterUrl) {
        try {
            if (posterUrl != null && !posterUrl.isBlank()) return new Image(posterUrl, true);
        } catch (Exception ignore) {}
        // ảnh mặc định của JFX
        return new Image(Objects.requireNonNull(
                getClass().getResource("/javafx/scene/control/skin/caspian/dialog-confirm.png"))
                .toExternalForm());
    }

    private String tinhTrangThai(Timestamp batDau) {
        if (batDau == null) return "";
        long minutes = (batDau.getTime() - System.currentTimeMillis()) / 60000;
        if (minutes > 30) return "Sắp chiếu";
        if (minutes >= -120) return "Đang chiếu";
        return "Đã chiếu";
        // bạn có thể thêm “Còn vé/ Hết vé” theo số vé SAN_SANG nếu cần
    }

    // ======= BÁN VÉ (tối giản, đủ chạy) =======
    private void openSeatDialog(int maSuatChieu, String tenPhim, String tenPhong, String gio) throws SQLException {
        Stage stage = new Stage();
        VBox root = new VBox(10);
        root.setStyle("-fx-padding:14;");
        Label lblTitle = new Label("Suất: " + tenPhim + " - " + tenPhong + " (" + gio + ")");
        Label lblHint  = new Label("Xanh lá: trống, Xanh dương: đã chọn");
        Label lblTong  = new Label("Tổng tiền: 0 đ");

        TilePane tile = new TilePane(5,5);
        tile.setPrefColumns(10);

        final BigDecimal[] tong = {BigDecimal.ZERO};
        List<Integer> selected = new ArrayList<>();

        final String sqlGhe = """
            SELECT g.ma_ghe, g.hang_ghe, g.so_ghe, v.gia_ban
            FROM ve v
            JOIN ghe g ON v.ma_ghe = g.ma_ghe
            WHERE v.ma_suat_chieu = ? AND v.trang_thai = 'SAN_SANG'
            ORDER BY g.hang_ghe, g.so_ghe
        """;

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sqlGhe)) {
            ps.setInt(1, maSuatChieu);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int maGhe = rs.getInt("ma_ghe");
                    String label = rs.getString("hang_ghe") + rs.getInt("so_ghe");
                    BigDecimal gia = rs.getBigDecimal("gia_ban");

                    Button b = new Button(label);
                    b.setPrefSize(40, 40);
                    b.setStyle("-fx-background-color:#16a34a; -fx-text-fill:white; -fx-font-weight:bold;");

                    b.setOnAction(ev -> {
                        if (selected.contains(maGhe)) {
                            selected.remove(Integer.valueOf(maGhe));
                            b.setStyle("-fx-background-color:#16a34a; -fx-text-fill:white; -fx-font-weight:bold;");
                            tong[0] = tong[0].subtract(gia);
                        } else {
                            selected.add(maGhe);
                            b.setStyle("-fx-background-color:#2563eb; -fx-text-fill:white; -fx-font-weight:bold;");
                            tong[0] = tong[0].add(gia);
                        }
                        lblTong.setText("Tổng tiền: " + String.format("%,.0f đ", tong[0]));
                    });

                    tile.getChildren().add(b);
                }
            }
        }

        Button btnOK = new Button("Xác nhận bán vé");
        btnOK.setStyle("-fx-background-color:#2563eb; -fx-text-fill:white; -fx-font-weight:bold; -fx-background-radius:6;");
        btnOK.setOnAction(e -> {
            if (selected.isEmpty()) { warn("Vui lòng chọn ít nhất 1 ghế"); return; }
            try {
                commitTickets(maSuatChieu, selected, tong[0]);
                stage.close();
            } catch (SQLException ex) {
                error("Lỗi bán vé", ex.getMessage());
            }
        });

        ScrollPane sp = new ScrollPane(tile);
        sp.setFitToWidth(true);
        sp.setPrefHeight(320);

        root.getChildren().addAll(lblTitle, lblHint, sp, lblTong, btnOK);
        stage.setScene(new Scene(root, 520, 420));
        stage.setTitle("Bán vé");
        stage.show();
    }

    private void commitTickets(int maSuatChieu, List<Integer> seats, BigDecimal tongTien) throws SQLException {
        try (Connection c = DBConnection.getConnection()) {
            c.setAutoCommit(false);

            // 1) tạo đơn hàng tối giản
            int maDonHang;
            final String sqlDon = "INSERT INTO don_hang(kenh, trang_thai, tong_tien) VALUES('TRUC_TIEP','DA_THANH_TOAN', ?)";
            try (PreparedStatement ps = c.prepareStatement(sqlDon, Statement.RETURN_GENERATED_KEYS)) {
                ps.setBigDecimal(1, tongTien);
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (!rs.next()) throw new SQLException("Không tạo được đơn hàng");
                    maDonHang = rs.getInt(1);
                }
            }

            // 2) cập nhật vé
            final String sqlUpdate = """
                UPDATE ve
                SET trang_thai='DA_BAN', ban_luc=NOW(), ma_don_hang=?
                WHERE ma_suat_chieu=? AND ma_ghe=? AND trang_thai='SAN_SANG'
            """;
            try (PreparedStatement ps = c.prepareStatement(sqlUpdate)) {
                for (int maGhe : seats) {
                    ps.setInt(1, maDonHang);
                    ps.setInt(2, maSuatChieu);
                    ps.setInt(3, maGhe);
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            c.commit();
            info("✅ Bán " + seats.size() + " vé thành công. Tổng tiền: " + String.format("%,.0f đ", tongTien));
        }
    }

    // ===== ViewModel =====
    public static class SuatChieuVM {
        private final IntegerProperty maSuatChieu = new SimpleIntegerProperty();
        private final StringProperty tenPhim = new SimpleStringProperty();
        private final StringProperty tenPhong = new SimpleStringProperty();
        private final StringProperty gio = new SimpleStringProperty();
        private final ObjectProperty<BigDecimal> gia = new SimpleObjectProperty<>();
        private final StringProperty trangThai = new SimpleStringProperty();
        private final StringProperty posterUrl = new SimpleStringProperty();

        public SuatChieuVM(int maSuatChieu, String phim, String phong, String gio,
                           BigDecimal gia, String trangThai, String posterUrl) {
            this.maSuatChieu.set(maSuatChieu);
            this.tenPhim.set(phim);
            this.tenPhong.set(phong);
            this.gio.set(gio);
            this.gia.set(gia);
            this.trangThai.set(trangThai);
            this.posterUrl.set(posterUrl);
        }

        public int getMaSuatChieu(){ return maSuatChieu.get(); }
        public String getPosterUrl(){ return posterUrl.get(); }
        public String getTenPhim(){ return tenPhim.get(); }
        public String getTenPhong(){ return tenPhong.get(); }
        public String getGio(){ return gio.get(); }

        public IntegerProperty maSuatChieuProperty(){ return maSuatChieu; }
        public StringProperty tenPhimProperty(){ return tenPhim; }
        public StringProperty tenPhongProperty(){ return tenPhong; }
        public StringProperty gioProperty(){ return gio; }
        public ObjectProperty<BigDecimal> giaProperty(){ return gia; }
        public StringProperty trangThaiProperty(){ return trangThai; }
    }
}
