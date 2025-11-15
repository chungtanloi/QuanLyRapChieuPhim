package controllers;

import database.DBConnection;
import models.film;
import models.IdNamePair;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.text.SimpleDateFormat;
import java.text.ParseException;

public class PhimController {

    // ================================
    // FXML KHAI BÁO
    // ================================
    @FXML private TableView<film> tblPhim;
    @FXML private TableColumn<film, Long> colMaPhim;
    @FXML private TableColumn<film, String> colTenPhim;
    @FXML private TableColumn<film, Integer> colThoiLuong;
    @FXML private TableColumn<film, String> colTheLoai;
    @FXML private TableColumn<film, String> colPhanLoai;
    @FXML private TableColumn<film, String> colNgayPhatHanh;
    @FXML private TableColumn<film, String> colTrangThai;

    @FXML private TextField txtTimKiemPhim;
    @FXML private ComboBox<IdNamePair> cbLocTheoTheLoai;
    @FXML private ComboBox<String> cbLocTheoPhanLoai;
    @FXML private DatePicker dpTuNgay;
    @FXML private DatePicker dpDenNgay;
    @FXML private CheckBox chkDangChieu;
    @FXML private CheckBox chkSapChieu;

    @FXML private TextField txtMaPhim, txtTenPhim, txtThoiLuong;
    @FXML private TextField txtTheLoai, txtPhanLoai, txtNgayKhoiChieu;

    @FXML private TextArea txtMoTa;
    @FXML private ImageView imgPhimPoster;

    @FXML private Button btnThemPhim, btnSuaPhim, btnXoaPhim, btnLuu, btnLamMoi, btnTimKiem;

    private final ObservableList<film> danhSachPhim = FXCollections.observableArrayList();
    private film phimDangChon = null;
      private boolean dangThemMoi = false; // THÊM BIẾN NÀY


    // ================================
    // INITIALIZE
    // ================================
    @FXML
    public void initialize() {
        System.out.println("🚀 PhimController đang khởi tạo...");
       
        // Setup table columns
        colMaPhim.setCellValueFactory(new PropertyValueFactory<>("maPhim"));
        colTenPhim.setCellValueFactory(new PropertyValueFactory<>("tenPhim"));
        colThoiLuong.setCellValueFactory(new PropertyValueFactory<>("thoiLuongPhut"));
        colTheLoai.setCellValueFactory(new PropertyValueFactory<>("theLoai"));
        colPhanLoai.setCellValueFactory(new PropertyValueFactory<>("phanLoai"));
        colNgayPhatHanh.setCellValueFactory(new PropertyValueFactory<>("ngayPhatHanh"));
        colTrangThai.setCellValueFactory(new PropertyValueFactory<>("trangThai"));

        loadFilterData();
        loadPhimTable(null, null);

        // Lắng nghe khi user click chọn 1 dòng
        tblPhim.getSelectionModel().selectedItemProperty().addListener((obs, old, phim) -> {
            if (phim != null) {
                System.out.println("🎯 Chọn phim: " + phim.getTenPhim());
                hienThiChiTietPhim(phim);
            } else {
                clearChiTietPhim();
            }
        });
       
        System.out.println("✅ PhimController khởi tạo thành công");
    }

    // ================================
    // HIỂN THỊ CHI TIẾT PHIM
    // ================================
private void hienThiChiTietPhim(film phim) {
    System.out.println("📋 Hiển thị chi tiết phim: " + phim.getTenPhim());
    dangThemMoi = false; // QUAN TRỌNG: Khi chọn phim từ table → chế độ sửa
    phimDangChon = phim;
   
    txtMaPhim.setText(String.valueOf(phim.getMaPhim()));
    txtTenPhim.setText(phim.getTenPhim());
    txtThoiLuong.setText(String.valueOf(phim.getThoiLuongPhut()));
    txtTheLoai.setText(phim.getTheLoai());
    txtPhanLoai.setText(phim.getPhanLoai());
    txtNgayKhoiChieu.setText(phim.getNgayPhatHanh());

    // Lấy thông tin chi tiết từ database
    film full = getFullPhimDetails(phim.getMaPhim());
    if (full != null) {
        txtMoTa.setText(full.getMoTa());
    } else {
        txtMoTa.setText("");
    }
   
    System.out.println("✅ Đã vào chế độ sửa, dangThemMoi = " + dangThemMoi);
}

private void clearChiTietPhim() {
    System.out.println("🧹 Clear chi tiết phim");
    txtMaPhim.setText("Mã tự động");
    txtTenPhim.clear();
    txtThoiLuong.clear();
    txtTheLoai.clear();
    txtPhanLoai.clear();
    txtNgayKhoiChieu.clear();
    txtMoTa.clear();
    phimDangChon = null;
    // KHÔNG set dangThemMoi = false ở đây nữa!
    // Giữ nguyên trạng thái dangThemMoi hiện tại
    System.out.println("✅ Đã clear form, dangThemMoi vẫn giữ = " + dangThemMoi);
}

    private film getFullPhimDetails(long maPhim) {
   String sql = "SELECT mo_ta FROM phim WHERE ma_phim = ?";
    try (Connection conn = DBConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {

        ps.setLong(1, maPhim);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            film f = new film(maPhim);
            f.setMoTa(rs.getString("mo_ta"));
            return f;
        }

    } catch (SQLException e) {
        e.printStackTrace();
    }
    return null;
    }

    // ================================
    // LOAD COMBOBOX FILTER
    // ================================
    private void loadFilterData() {
        System.out.println("🔄 Đang tải dữ liệu filter...");

        // load thể loại
        ObservableList<IdNamePair> list = FXCollections.observableArrayList();
        list.add(new IdNamePair(0, "Tất cả"));

        String sql = "SELECT ma_the_loai, ten_the_loai FROM the_loai ORDER BY ten_the_loai";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(new IdNamePair(rs.getLong(1), rs.getString(2)));
            }

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không tải thể loại!");
        }

        cbLocTheoTheLoai.setItems(list);
        cbLocTheoTheLoai.getSelectionModel().selectFirst();

        cbLocTheoPhanLoai.setItems(FXCollections.observableArrayList("Tất cả", "P", "T13", "T16", "T18"));
        cbLocTheoPhanLoai.getSelectionModel().selectFirst();

        chkDangChieu.setSelected(true);
        chkSapChieu.setSelected(true);
       
        System.out.println("✅ Đã tải dữ liệu filter");
    }

    // ================================
    // LOAD BẢNG PHIM
    // ================================
    public void loadPhimTable(String customSql, List<Object> params) {
        System.out.println("🔄 Đang tải danh sách phim...");

        danhSachPhim.clear();

        String sqlDefault = """
            SELECT
                p.ma_phim, p.ten_phim, p.thoi_luong_phut, p.phan_loai, p.mo_ta,
                GROUP_CONCAT(DISTINCT tl.ten_the_loai SEPARATOR ', ') AS theLoaiStr,
                DATE_FORMAT(p.ngay_phat_hanh, '%d/%m/%Y') AS ngayKCStr,
                CASE WHEN p.ngay_phat_hanh <= CURDATE() THEN 'Đang Chiếu' ELSE 'Sắp Chiếu' END AS trangThaiStr
            FROM phim p
            LEFT JOIN phim_the_loai ptl ON p.ma_phim = ptl.ma_phim
            LEFT JOIN the_loai tl ON ptl.ma_the_loai = tl.ma_the_loai
            GROUP BY p.ma_phim
            ORDER BY p.ngay_phat_hanh DESC
        """;

        String sql = (customSql == null ? sqlDefault : customSql);

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (params != null) {
                for (int i = 0; i < params.size(); i++) {
                    ps.setObject(i + 1, params.get(i));
                }
            }

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                danhSachPhim.add(new film(
                        rs.getLong("ma_phim"),
                        rs.getString("ten_phim"),
                        rs.getInt("thoi_luong_phut"),
                        rs.getString("theLoaiStr"),
                        rs.getString("phan_loai"),
                        rs.getString("ngayKCStr"),
                        rs.getString("trangThaiStr")
                ));
            }

            tblPhim.setItems(danhSachPhim);
            System.out.println("✅ Đã tải " + danhSachPhim.size() + " phim");

        } catch (SQLException e) {
            System.out.println("❌ Lỗi tải phim: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Lỗi CSDL", e.getMessage());
        }
    }

    // ================================
    // NÚT TÌM KIẾM / LỌC
    // ================================
    @FXML
    private void handleTimKiem() {
        System.out.println("🔍 Bắt đầu tìm kiếm phim...");

        String tenPhim = txtTimKiemPhim.getText().trim();
        IdNamePair tl = cbLocTheoTheLoai.getValue();
        String phanLoai = cbLocTheoPhanLoai.getValue();
        LocalDate tuNgay = dpTuNgay.getValue();
        LocalDate denNgay = dpDenNgay.getValue();
        boolean dang = chkDangChieu.isSelected();
        boolean sap = chkSapChieu.isSelected();

        StringBuilder sql = new StringBuilder("""
            SELECT
                p.ma_phim, p.ten_phim, p.thoi_luong_phut, p.phan_loai, p.mo_ta,
                GROUP_CONCAT(DISTINCT tl.ten_the_loai SEPARATOR ', ') AS theLoaiStr,
                DATE_FORMAT(p.ngay_phat_hanh, '%d/%m/%Y') AS ngayKCStr,
                CASE WHEN p.ngay_phat_hanh <= CURDATE() THEN 'Đang Chiếu' ELSE 'Sắp Chiếu' END AS trangThaiStr
            FROM phim p
            LEFT JOIN phim_the_loai ptl ON p.ma_phim = ptl.ma_phim
            LEFT JOIN the_loai tl ON ptl.ma_the_loai = tl.ma_the_loai
            WHERE 1=1
        """);

        List<Object> params = new ArrayList<>();

        if (!tenPhim.isEmpty()) {
            sql.append(" AND p.ten_phim LIKE ? ");
            params.add("%" + tenPhim + "%");
        }

        if (tl != null && tl.getId() != 0) {
            sql.append("""
                AND EXISTS (
                    SELECT 1 FROM phim_the_loai t
                    WHERE t.ma_phim = p.ma_phim AND t.ma_the_loai = ?
                )
            """);
            params.add(tl.getId());
        }

        if (!phanLoai.equals("Tất cả")) {
            sql.append(" AND p.phan_loai = ? ");
            params.add(phanLoai);
        }

        if (dang && !sap) sql.append(" AND p.ngay_phat_hanh <= CURDATE() ");
        if (!dang && sap) sql.append(" AND p.ngay_phat_hanh > CURDATE() ");
        if (!dang && !sap) sql.append(" AND 1=0 ");

        if (tuNgay != null) {
            sql.append(" AND p.ngay_phat_hanh >= ? ");
            params.add(Date.valueOf(tuNgay));
        }
        if (denNgay != null) {
            sql.append(" AND p.ngay_phat_hanh <= ? ");
            params.add(Date.valueOf(denNgay));
        }

        sql.append(" GROUP BY p.ma_phim ORDER BY p.ngay_phat_hanh DESC ");

        loadPhimTable(sql.toString(), params);
    }

    // ================================
    // NÚT THÊM MỚI
    // ================================
@FXML
private void handleThemMoi() {
    System.out.println("➕ Click Thêm phim mới");
    dangThemMoi = true; // QUAN TRỌNG: Set cờ đang thêm mới
   
    // Clear form NHƯNG KHÔNG reset dangThemMoi
    txtMaPhim.setText("Mã tự động");
    txtTenPhim.clear();
    txtThoiLuong.clear();
    txtTheLoai.clear();
    txtPhanLoai.clear();
    txtNgayKhoiChieu.clear();
    txtMoTa.clear();
    phimDangChon = null;
   
    // Bỏ selection trên table
    tblPhim.getSelectionModel().clearSelection();
   
    System.out.println("✅ Đã vào chế độ thêm mới, dangThemMoi = " + dangThemMoi);
}
    // ================================
// NÚT LÀM MỚI - THÊM PHƯƠNG THỨC NÀY
// ================================
@FXML
private void handleLamMoi() {
    System.out.println("🔄 Click Làm mới");
    clearChiTietPhim();
    tblPhim.getSelectionModel().clearSelection();
   
    // Reset các bộ lọc
    txtTimKiemPhim.clear();
    cbLocTheoTheLoai.getSelectionModel().selectFirst();
    cbLocTheoPhanLoai.getSelectionModel().selectFirst();
    dpTuNgay.setValue(null);
    dpDenNgay.setValue(null);
    chkDangChieu.setSelected(true);
    chkSapChieu.setSelected(true);
   
    loadPhimTable(null, null);
}

    @FXML
    private void handleSuaPhim() {
        System.out.println("✏️ Click Sửa phim");
        film f = tblPhim.getSelectionModel().getSelectedItem();
        if (f == null) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng chọn phim để sửa");
            return;
        }
        openThemSuaPhimDialog(f);
    }

    private void openThemSuaPhimDialog(film phimToEdit) {
        System.out.println("📝 Mở dialog thêm/sửa phim");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/ThemSuaPhimDialog.fxml"));
            Parent ui = loader.load();

            ThemSuaphimController controller = loader.getController();
            controller.setParentController(this);

            if (phimToEdit != null) controller.setPhimData(phimToEdit);

            Stage stage = new Stage();
            stage.setTitle(phimToEdit == null ? "Thêm phim" : "Sửa: " + phimToEdit.getTenPhim());
            stage.setScene(new Scene(ui));
            stage.initModality(Modality.APPLICATION_MODAL);

            stage.showAndWait();

        } catch (IOException e) {
            System.out.println("❌ Lỗi mở dialog: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Lỗi FXML", e.getMessage());
        }
    }
   
   
   
    // ================================
// NÚT LƯU PHIM - THÊM PHƯƠNG THỨC NÀY
// ================================
@FXML
private void handleLuuPhim() {
    System.out.println("💾 Click Lưu phim");
    System.out.println("🔍 Trạng thái: dangThemMoi = " + dangThemMoi + ", phimDangChon = " + phimDangChon);
   
    if (dangThemMoi) {
        // CHẾ ĐỘ THÊM MỚI
        System.out.println("🆕 Đang thực hiện thêm mới...");
        themPhimMoi();
    } else if (phimDangChon != null) {
        // CHẾ ĐỘ SỬA
        System.out.println("✏️ Đang thực hiện sửa...");
        suaPhim();
    } else {
        System.out.println("❌ Không xác định được chế độ!");
        showAlert(Alert.AlertType.WARNING, "Cảnh báo",
            "Vui lòng chọn phim để cập nhật hoặc bấm 'Thêm Mới' để thêm phim mới");
        return;
    }
}

    // ================================
    // NÚT XOÁ
    // ================================
@FXML
private void handleXoaPhim() {
    System.out.println("🗑️ Click Xóa phim");
    film f = tblPhim.getSelectionModel().getSelectedItem();
    if (f == null) {
        showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Chọn phim để xoá");
        return;
    }

    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
            "Bạn chắc chắn muốn xoá phim \"" + f.getTenPhim() + "\"?\n\nLƯU Ý: Sẽ xóa tất cả suất chiếu và thể loại liên quan!",
            ButtonType.YES, ButtonType.NO);

    Optional<ButtonType> result = confirm.showAndWait();

    if (result.isPresent() && result.get() == ButtonType.YES) {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // Bắt đầu transaction

            // 1. Xóa các suất chiếu liên quan
            String sql1 = "DELETE FROM suat_chieu WHERE ma_phim = ?";
            try (PreparedStatement ps1 = conn.prepareStatement(sql1)) {
                ps1.setLong(1, f.getMaPhim());
                ps1.executeUpdate();
            }

            // 2. Xóa thể loại phim
            String sql2 = "DELETE FROM phim_the_loai WHERE ma_phim = ?";
            try (PreparedStatement ps2 = conn.prepareStatement(sql2)) {
                ps2.setLong(1, f.getMaPhim());
                ps2.executeUpdate();
            }

            // 3. Xóa phim
            String sql3 = "DELETE FROM phim WHERE ma_phim = ?";
            try (PreparedStatement ps3 = conn.prepareStatement(sql3)) {
                ps3.setLong(1, f.getMaPhim());
                int affected = ps3.executeUpdate();

                if (affected > 0) {
                    conn.commit(); // Commit transaction
                    showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã xoá phim và tất cả dữ liệu liên quan.");
                    loadPhimTable(null, null);
                    clearChiTietPhim();
                } else {
                    conn.rollback(); // Rollback nếu lỗi
                    showAlert(Alert.AlertType.ERROR, "Lỗi", "Không xoá được phim.");
                }
            }

        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {}
            showAlert(Alert.AlertType.ERROR, "Lỗi CSDL", "Không thể xóa phim: " + e.getMessage());
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException e) {}
        }
    }
}

    // ================================
    // NÚT LÀM MỚI
    // ================================
    @FXML
    private void suaPhim() {
        System.out.println("✏️ Thực hiện sửa phim");
        // ... code sửa phim hiện tại (phần handleLuuPhim cũ)
        String tenPhim = txtTenPhim.getText().trim();
        String thoiLuongStr = txtThoiLuong.getText().trim();
        String phanLoai = txtPhanLoai.getText().trim();
        String ngayKhoiChieuStr = txtNgayKhoiChieu.getText().trim();
        String moTa = txtMoTa.getText().trim();

        if (tenPhim.isEmpty() || thoiLuongStr.isEmpty() || phanLoai.isEmpty() || ngayKhoiChieuStr.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng nhập đầy đủ thông tin");
            return;
        }

        try {
            int thoiLuong = Integer.parseInt(thoiLuongStr);
           
            java.sql.Date ngayKhoiChieu = null;
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                java.util.Date date = sdf.parse(ngayKhoiChieuStr);
                ngayKhoiChieu = new java.sql.Date(date.getTime());
            } catch (ParseException e) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Định dạng ngày phải là dd/MM/yyyy");
                return;
            }
           
            String sql = "UPDATE phim SET ten_phim = ?, thoi_luong_phut = ?, phan_loai = ?, ngay_phat_hanh = ?, mo_ta = ? WHERE ma_phim = ?";
           
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
               
                ps.setString(1, tenPhim);
                ps.setInt(2, thoiLuong);
                ps.setString(3, phanLoai);
                ps.setDate(4, ngayKhoiChieu);
                ps.setString(5, moTa);
                ps.setLong(6, phimDangChon.getMaPhim());
               
                int affected = ps.executeUpdate();
               
                if (affected > 0) {
                    showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã cập nhật phim.");
                    loadPhimTable(null, null);
                } else {
                    showAlert(Alert.AlertType.ERROR, "Lỗi", "Không cập nhật được.");
                }
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Thời lượng phải là số!");
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi CSDL", e.getMessage());
        }
    }

private void themPhimMoi() {
    System.out.println("🆕 Thực hiện thêm phim mới");
   
    String tenPhim = txtTenPhim.getText().trim();
    String thoiLuongStr = txtThoiLuong.getText().trim();
    String theLoai = txtTheLoai.getText().trim(); // THÊM DÒNG NÀY
    String phanLoai = txtPhanLoai.getText().trim();
    String ngayKhoiChieuStr = txtNgayKhoiChieu.getText().trim();
    String moTa = txtMoTa.getText().trim();

    if (tenPhim.isEmpty() || thoiLuongStr.isEmpty() || theLoai.isEmpty() || phanLoai.isEmpty() || ngayKhoiChieuStr.isEmpty()) {
        showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng nhập đầy đủ thông tin");
        return;
    }

    try {
        int thoiLuong = Integer.parseInt(thoiLuongStr);
       
        // Chuyển đổi ngày từ dd/MM/yyyy sang yyyy-MM-dd
        java.sql.Date ngayKhoiChieu = null;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            java.util.Date date = sdf.parse(ngayKhoiChieuStr);
            ngayKhoiChieu = new java.sql.Date(date.getTime());
        } catch (ParseException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Định dạng ngày phải là dd/MM/yyyy");
            return;
        }
       
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // Bắt đầu transaction

            // 1. Thêm phim
            String sqlPhim = "INSERT INTO phim (ten_phim, thoi_luong_phut, phan_loai, ngay_phat_hanh, mo_ta) VALUES (?, ?, ?, ?, ?)";
            long maPhimMoi = 0;
           
            try (PreparedStatement ps = conn.prepareStatement(sqlPhim, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, tenPhim);
                ps.setInt(2, thoiLuong);
                ps.setString(3, phanLoai);
                ps.setDate(4, ngayKhoiChieu);
                ps.setString(5, moTa);
               
                int affected = ps.executeUpdate();
               
                if (affected > 0) {
                    // Lấy mã phim vừa tạo
                    try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            maPhimMoi = generatedKeys.getLong(1);
                            System.out.println("✅ Đã thêm phim mới, mã: " + maPhimMoi);
                        }
                    }
                } else {
                    conn.rollback();
                    showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thêm được phim mới.");
                    return;
                }
            }

            // 2. Thêm thể loại (nếu có)
            if (!theLoai.isEmpty()) {
                themTheLoaiChoPhim(conn, maPhimMoi, theLoai);
            }

            conn.commit(); // Commit transaction
            showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã thêm phim mới thành công!");
            loadPhimTable(null, null);
            clearChiTietPhim();
            dangThemMoi = false;

        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {}
            showAlert(Alert.AlertType.ERROR, "Lỗi CSDL", e.getMessage());
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException e) {}
        }
    } catch (NumberFormatException e) {
        showAlert(Alert.AlertType.ERROR, "Lỗi", "Thời lượng phải là số!");
    }
}


// ================================
// PHƯƠNG THỨC THÊM THỂ LOẠI CHO PHIM
// ================================
private void themTheLoaiChoPhim(Connection conn, long maPhim, String theLoaiStr) throws SQLException {
    // Tách các thể loại bằng dấu phẩy
    String[] theLoais = theLoaiStr.split(",");
   
    for (String tenTheLoai : theLoais) {
        String tenTheLoaiTrim = tenTheLoai.trim();
        if (tenTheLoaiTrim.isEmpty()) continue;
       
        // 1. Tìm hoặc thêm thể loại
        Long maTheLoai = timHoacThemTheLoai(conn, tenTheLoaiTrim);
       
        if (maTheLoai != null) {
            // 2. Thêm vào bảng phim_the_loai
            String sql = "INSERT IGNORE INTO phim_the_loai (ma_phim, ma_the_loai) VALUES (?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, maPhim);
                ps.setLong(2, maTheLoai);
                ps.executeUpdate();
            }
        }
    }
}

// ================================
// TÌM HOẶC THÊM THỂ LOẠI
// ================================
private Long timHoacThemTheLoai(Connection conn, String tenTheLoai) throws SQLException {
    // 1. Tìm thể loại đã có
    String sqlTim = "SELECT ma_the_loai FROM the_loai WHERE ten_the_loai = ?";
    try (PreparedStatement ps = conn.prepareStatement(sqlTim)) {
        ps.setString(1, tenTheLoai);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return rs.getLong("ma_the_loai");
        }
    }
   
    // 2. Nếu không có, thêm mới
    String sqlThem = "INSERT INTO the_loai (ten_the_loai) VALUES (?)";
    try (PreparedStatement ps = conn.prepareStatement(sqlThem, Statement.RETURN_GENERATED_KEYS)) {
        ps.setString(1, tenTheLoai);
        int affected = ps.executeUpdate();
        if (affected > 0) {
            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getLong(1);
                }
            }
        }
    }
   
    return null;
}
    // ================================
    // THÔNG BÁO
    // ================================
    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert a = new Alert(type);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}