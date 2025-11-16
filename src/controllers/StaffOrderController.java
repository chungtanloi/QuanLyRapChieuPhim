package controllers;

import database.DBConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.*;
import java.time.LocalDate;
import java.util.*;

public class StaffOrderController {

    // ================== FXML ==================
    @FXML private TextField txtSearch;
    @FXML private DatePicker dpNgayChieu;
    @FXML private ComboBox<String> cbDinhDang;
    @FXML private TilePane tilePhim;

    @FXML private ListView<String> lvHoaDon;
    @FXML private Label lblTong;
    @FXML private Button btnThanhToan;
    @FXML private Button btnDangXuat;

    // ================== STATE ==================
    private final ObservableList<CartItem> gioHang = FXCollections.observableArrayList();
    private long tongTien = 0;

    private Showtime selectedShowtime;

    // ================== INIT ==================
    @FXML
    public void initialize() {
        dpNgayChieu.setValue(LocalDate.now());
        cbDinhDang.setItems(FXCollections.observableArrayList("Tất cả", "2D", "3D", "IMAX", "4DX"));
        cbDinhDang.getSelectionModel().selectFirst();

        lvHoaDon.setItems(FXCollections.observableArrayList());

        // Thêm context menu cho ListView để xóa item
        setupListViewContextMenu();

        addListeners();
        loadPhim();
    }

    private void addListeners() {
        txtSearch.textProperty().addListener((obs, o, n) -> loadPhim());
        dpNgayChieu.valueProperty().addListener((obs, o, n) -> loadPhim());
        cbDinhDang.valueProperty().addListener((obs, o, n) -> loadPhim());
    }

    // ================== CONTEXT MENU XÓA ITEM ==================
    private void setupListViewContextMenu() {
        ContextMenu contextMenu = new ContextMenu();
        
        MenuItem deleteItem = new MenuItem("🗑️ Xóa sản phẩm này");
        deleteItem.setOnAction(e -> {
            int selectedIndex = lvHoaDon.getSelectionModel().getSelectedIndex();
            if (selectedIndex >= 0) {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                confirm.setTitle("Xác nhận xóa");
                confirm.setHeaderText("Xóa sản phẩm khỏi giỏ hàng");
                confirm.setContentText("Bạn có chắc muốn xóa: " + 
                    gioHang.get(selectedIndex).ten + "?");
                
                if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                    gioHang.remove(selectedIndex);
                    updateHoaDonList();
                }
            }
        });

        MenuItem clearAll = new MenuItem("🗑️ Xóa tất cả");
        clearAll.setOnAction(e -> {
            if (gioHang.isEmpty()) {
                alert("Giỏ hàng đang trống!");
                return;
            }
            
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Xác nhận xóa");
            confirm.setHeaderText("Xóa toàn bộ giỏ hàng");
            confirm.setContentText("Bạn có chắc muốn xóa tất cả sản phẩm?");
            
            if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                gioHang.clear();
                selectedShowtime = null;
                updateHoaDonList();
            }
        });

        contextMenu.getItems().addAll(deleteItem, new SeparatorMenuItem(), clearAll);
        lvHoaDon.setContextMenu(contextMenu);
    }

    // ================== ĐĂNG XUẤT ==================
    @FXML
    private void onDangXuat() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Đăng xuất");
        confirm.setHeaderText("Xác nhận đăng xuất");
        confirm.setContentText("Bạn có chắc muốn đăng xuất?\n" +
                "Giỏ hàng hiện tại sẽ bị xóa.");
        
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                // Xóa giỏ hàng
                gioHang.clear();
                selectedShowtime = null;
                
                // Quay về màn hình đăng nhập (thay đổi đường dẫn phù hợp)
                Stage stage = (Stage) btnDangXuat.getScene().getWindow();
                
                // Thay "LOGIN.fxml" bằng tên file FXML đăng nhập của bạn
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/models/LOGIN.fxml"));
                Parent root = loader.load();
                
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.setTitle("Đăng nhập");
                stage.show();
                
            } catch (Exception e) {
                e.printStackTrace();
                alert("Lỗi khi đăng xuất: " + e.getMessage());
            }
        }
    }

    // ================== LOAD PHIM ==================
    private void loadPhim() {
        tilePhim.getChildren().clear();

        String kw = txtSearch.getText() == null ? "" : txtSearch.getText().trim();
        String dinhDang = cbDinhDang.getValue();

        String sql = """
            SELECT DISTINCT p.ma_phim, p.ten_phim, p.thoi_luong_phut, p.poster_url
            FROM phim p
            JOIN suat_chieu sc ON sc.ma_phim = p.ma_phim
            JOIN dinh_dang dd ON dd.ma_dinh_dang = sc.ma_dinh_dang
            WHERE (? = '' OR p.ten_phim LIKE CONCAT('%', ?, '%'))
              AND DATE(sc.bat_dau_luc) = ?
              AND (? = 'Tất cả' OR dd.ten_dinh_dang = ?)
            ORDER BY p.ten_phim
        """;

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, kw);
            ps.setString(2, kw);
            ps.setDate(3, java.sql.Date.valueOf(dpNgayChieu.getValue()));
            ps.setString(4, dinhDang);
            ps.setString(5, dinhDang);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                tilePhim.getChildren().add(
                        makeMovieCard(
                                rs.getString("ma_phim"),
                                rs.getString("ten_phim"),
                                rs.getInt("thoi_luong_phut"),
                                rs.getString("poster_url")
                        )
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
            alert("Lỗi load phim: " + e.getMessage());
        }
    }

    private VBox makeMovieCard(String ma, String ten, int thoiluong, String posterUrl) {
        VBox card = new VBox(8);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 8;");
        card.setPadding(new Insets(10));
        card.setAlignment(Pos.CENTER);

        ImageView img = new ImageView();
        img.setFitWidth(180);
        img.setFitHeight(250);

        try {
            if (posterUrl != null && !posterUrl.isBlank()) {
                img.setImage(new Image(posterUrl, true));
            }
        } catch (Exception ignore) {}

        Label lblTen = new Label(ten);
        lblTen.setStyle("-fx-font-weight: bold;");
        lblTen.setWrapText(true);
        lblTen.setMaxWidth(180);

        Label lblTL = new Label(thoiluong + " phút");

        Button btn = new Button("Chọn suất");
        btn.setStyle("-fx-background-color:#3b82f6; -fx-text-fill:white;");
        btn.setOnAction(e -> openShowtime(ma, ten));

        card.getChildren().addAll(img, lblTen, lblTL, btn);
        return card;
    }

    // ================== SUẤT CHIẾU ==================
    private void openShowtime(String maPhim, String tenPhim) {
        List<Showtime> list = fetchShowtime(maPhim);

        if (list.isEmpty()) {
            alert("Không có suất chiếu!");
            return;
        }

        Map<String, Showtime> map = new LinkedHashMap<>();
        for (Showtime s : list) {
            map.put(
                    s.gio + " • Phòng " + s.phong + " • " + s.dinhDang + " • " +
                    String.format("%,d đ", s.basePrice),
                    s
            );
        }

        ChoiceDialog<String> dialog =
                new ChoiceDialog<>(map.keySet().iterator().next(), map.keySet());
        dialog.setTitle("Chọn suất chiếu");
        dialog.setHeaderText("Phim: " + tenPhim);

        Optional<String> res = dialog.showAndWait();
        if (res.isEmpty()) return;

        selectedShowtime = map.get(res.get());

        openChonGhe(tenPhim);
    }

    private static class Showtime {
        int id;
        String gio;
        String phong;
        String dinhDang;
        long basePrice;
    }

    private List<Showtime> fetchShowtime(String maPhim) {
        List<Showtime> list = new ArrayList<>();

        String sql = """
            SELECT sc.ma_suat_chieu,
                   TIME_FORMAT(sc.bat_dau_luc, '%H:%i') AS gio,
                   sc.ma_phong,
                   dd.ten_dinh_dang,
                   sc.gia_co_ban
            FROM suat_chieu sc
            JOIN dinh_dang dd ON dd.ma_dinh_dang = sc.ma_dinh_dang
            WHERE sc.ma_phim = ?
              AND DATE(sc.bat_dau_luc) = ?
        """;

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, maPhim);
            ps.setDate(2, java.sql.Date.valueOf(dpNgayChieu.getValue()));

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Showtime s = new Showtime();
                s.id = rs.getInt("ma_suat_chieu");
                s.gio = rs.getString("gio");
                s.phong = rs.getString("ma_phong");
                s.dinhDang = rs.getString("ten_dinh_dang");
                s.basePrice = rs.getLong("gia_co_ban");
                list.add(s);
            }

        } catch (Exception e) {
            e.printStackTrace();
            alert("Lỗi load suất chiếu: " + e.getMessage());
        }

        return list;
    }

    // lấy tên ghế kiểu "A1"
    private String getTenGhe(int maGhe) {
        String sql = "SELECT hang_ghe, so_ghe FROM ghe WHERE ma_ghe = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, maGhe);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String hang = rs.getString("hang_ghe");
                int so = rs.getInt("so_ghe");
                return hang + so;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return String.valueOf(maGhe);
    }

    // ================== CHỌN GHẾ ==================
    private void openChonGhe(String tenPhim) {

        if (selectedShowtime == null) {
            alert("Chưa chọn suất chiếu!");
            return;
        }

        SeatPickerDialog dlg = new SeatPickerDialog(selectedShowtime.id, tenPhim);
        dlg.showAndWait();

        SeatPickerDialog.Result r = (SeatPickerDialog.Result) dlg.getUserData();

        if (r == null || r.veIds == null || r.veIds.isEmpty()) {
            return;
        }

        long giaVe = selectedShowtime.basePrice;

        for (Integer maGhe : r.veIds) {
            String tenGhe = getTenGhe(maGhe);

            gioHang.add(new CartItem(
                    "VE",
                    maGhe,
                    "Vé " + tenGhe,
                    1,
                    giaVe
            ));
        }

        updateHoaDonList();

        Alert ask = new Alert(Alert.AlertType.CONFIRMATION);
        ask.setTitle("Sản phẩm");
        ask.setHeaderText("Thêm combo / sản phẩm?");
        ask.setContentText("Bạn có muốn thêm combo hoặc sản phẩm không?");
        ask.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);

        if (ask.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
            openChonSanPham();
        }
    }

    // ================== SẢN PHẨM ==================
    private static class Product {
        int id;
        String ten;
        long gia;
    }

    private static class CartItem {
        String loai;   // "VE" hoặc "SP"
        int id;        // VE: ma_ghe, SP: ma_san_pham
        String ten;
        int soLuong;
        long giaBan;

        CartItem(String loai, int id, String ten, int soLuong, long giaBan) {
            this.loai = loai;
            this.id = id;
            this.ten = ten;
            this.soLuong = soLuong;
            this.giaBan = giaBan;
        }
    }

    @FXML
    public void openChonSanPham() {
        Map<String, Product> ds = fetchSanPham();
        if (ds.isEmpty()) {
            alert("Không có sản phẩm!");
            return;
        }

        ChoiceDialog<String> dlg =
                new ChoiceDialog<>(ds.keySet().iterator().next(), ds.keySet());
        dlg.setTitle("Thêm sản phẩm");
        dlg.setHeaderText("Chọn sản phẩm / combo");

        Optional<String> res = dlg.showAndWait();
        if (res.isEmpty()) return;

        Product p = ds.get(res.get());

        TextInputDialog qty = new TextInputDialog("1");
        qty.setTitle("Số lượng");
        qty.setHeaderText("Nhập số lượng cho: " + p.ten);
        qty.setContentText("Số lượng:");
        Optional<String> qtyRes = qty.showAndWait();

        int sl = 1;
        try {
            sl = Integer.parseInt(qtyRes.orElse("1"));
            if (sl < 1) sl = 1;
        } catch (Exception ignore) {}

        gioHang.add(new CartItem("SP", p.id, p.ten, sl, p.gia));
        updateHoaDonList();
    }

    private Map<String, Product> fetchSanPham() {
        Map<String, Product> map = new LinkedHashMap<>();

        String sql = """
            SELECT ma_san_pham, ten_san_pham, gia
            FROM san_pham
            ORDER BY ten_san_pham
        """;

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Product p = new Product();
                p.id = rs.getInt(1);
                p.ten = rs.getString(2);
                p.gia = rs.getLong(3);

                map.put(p.ten + " – " + String.format("%,d đ", p.gia), p);
            }

        } catch (Exception e) {
            e.printStackTrace();
            alert("Lỗi load sản phẩm: " + e.getMessage());
        }

        return map;
    }

    // ================== THANH TOÁN ==================
    @FXML
    private void onThanhToan() {
        if (selectedShowtime == null) {
            alert("Chưa chọn suất chiếu!");
            return;
        }
        if (gioHang.isEmpty()) {
            alert("Giỏ hàng đang trống!");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận thanh toán");
        confirm.setHeaderText("Thanh toán đơn hàng");
        confirm.setContentText("Tổng tiền: " + String.format("%,d đ", tongTien)
                + "\n\nBạn có chắc chắn muốn thanh toán?");
        Optional<ButtonType> res = confirm.showAndWait();
        if (res.isEmpty() || res.get() != ButtonType.OK) return;

        try (Connection conn = DBConnection.getConnection()) {

            if (conn == null) {
                alert("Không thể kết nối CSDL!");
                return;
            }

            int maDon = createDonHang(conn, tongTien);

            for (CartItem item : gioHang) {
                if ("VE".equals(item.loai)) {
                    int maVe = createOrUpdateVeUsingSP(conn, selectedShowtime.id, item.id, item.giaBan);
                    if (maVe > 0) {
                        insertDonVe(conn, maDon, maVe);
                    }
                } else {
                    insertDonCombo(conn, maDon, item.id, item.soLuong, item.giaBan);
                }
            }

            try {
                BillExporter.export(maDon);
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            gioHang.clear();
            lvHoaDon.getItems().clear();
            tongTien = 0;
            lblTong.setText("0 đ");
            selectedShowtime = null;

            Alert ok = new Alert(Alert.AlertType.INFORMATION);
            ok.setTitle("Thành công");
            ok.setHeaderText("Thanh toán thành công!");
            ok.setContentText("Mã đơn hàng: " + maDon +
                    "\nHóa đơn đã được lưu tại thư mục HoadonPDF.");
            ok.showAndWait();

        } catch (SQLException ex) {
            ex.printStackTrace();
            alert("Lỗi khi lưu đơn hàng: " + ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            alert("Không thể kết nối CSDL: " + ex.getMessage());
        }
    }

    private int createDonHang(Connection conn, long tongTien) throws SQLException {
        String sql = """
            INSERT INTO don_hang(ma_khach_hang, ma_nhan_vien, tao_luc, tong_tien, trang_thai)
            VALUES (NULL, NULL, NOW(), ?, 1)
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, tongTien);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        throw new SQLException("Không lấy được mã đơn hàng vừa tạo");
    }

    private int createOrUpdateVeUsingSP(Connection conn, int maSuatChieu, int maGhe, long giaBan) throws SQLException {
        String sql = "{CALL sp_create_or_update_ve(?, ?, ?, ?, ?)}";
        
        try (CallableStatement cs = conn.prepareCall(sql)) {
            cs.setInt(1, maSuatChieu);
            cs.setInt(2, maGhe);
            cs.setLong(3, giaBan);
            cs.registerOutParameter(4, Types.INTEGER);
            cs.registerOutParameter(5, Types.VARCHAR);
            
            cs.execute();
            
            int maVe = cs.getInt(4);
            String errorMsg = cs.getString(5);
            
            if (errorMsg != null && !errorMsg.isEmpty()) {
                throw new SQLException(errorMsg);
            }
            
            return maVe;
        }
    }

    private void insertDonVe(Connection conn, int maDon, int maVe) throws SQLException {
        String getGiaSql = "SELECT gia_ban FROM ve WHERE ma_ve = ?";
        long giaVe = 0;
        
        try (PreparedStatement ps = conn.prepareStatement(getGiaSql)) {
            ps.setInt(1, maVe);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                giaVe = rs.getLong("gia_ban");
            }
        }
        
        String sql = """
            INSERT INTO don_ve(ma_don_hang, ma_ve, don_gia)
            VALUES (?, ?, ?)
        """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maDon);
            ps.setInt(2, maVe);
            ps.setLong(3, giaVe);
            ps.executeUpdate();
        }
    }

    private void insertDonCombo(Connection conn, int maDon, int maSP, int sl, long giaBan) throws SQLException {
        String sql = """
            INSERT INTO don_combo(ma_don_hang, ma_combo, so_luong, don_gia, gia_ban)
            VALUES (?, ?, ?, ?,?)
        """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maDon);
            ps.setInt(2, maSP);
            ps.setInt(3, sl);
            ps.setLong(4, giaBan);
            ps.setLong(5, giaBan);
            ps.executeUpdate();
        }
    }

    // ================== UPDATE UI ==================
    private void updateHoaDonList() {
        lvHoaDon.getItems().clear();
        tongTien = 0;

        for (CartItem c : gioHang) {
            long tt = c.giaBan * c.soLuong;
            lvHoaDon.getItems().add(
                    c.ten + " x" + c.soLuong + " – " + String.format("%,d đ", tt)
            );
            tongTien += tt;
        }

        lblTong.setText(String.format("%,d đ", tongTien));
    }

    // ================== ALERT ==================
    private void alert(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}