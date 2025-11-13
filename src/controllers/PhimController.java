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

    @FXML private TextField txtMaPhim, txtTenPhim, txtThoiLuong, txtQuocGia;
    @FXML private TextArea txtMoTa;
    @FXML private ImageView imgPhimPoster;

    @FXML private Button btnThemPhim, btnSuaPhim, btnXoaPhim, btnLuu, btnLamMoi;

    private final ObservableList<film> danhSachPhim = FXCollections.observableArrayList();

    // ================================
    // INITIALIZE
    // ================================
    @FXML
    public void initialize() {

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
            if (phim != null) hienThiChiTietPhim(phim);
            else clearChiTietPhim();
        });
    }

    // ================================
    // HIỂN THỊ CHI TIẾT PHIM
    // ================================
    private void hienThiChiTietPhim(film phim) {

        film full = getFullPhimDetails(phim.getMaPhim());

        txtMaPhim.setText(String.valueOf(phim.getMaPhim()));
        txtTenPhim.setText(phim.getTenPhim());
        txtThoiLuong.setText(String.valueOf(phim.getThoiLuongPhut()));

        if (full != null) {
            txtQuocGia.setText(full.getQuocGia());
            txtMoTa.setText(full.getMoTa());
        } else {
            txtQuocGia.setText("");
            txtMoTa.setText("");
        }
    }

    private void clearChiTietPhim() {
        txtMaPhim.setText("Mã tự động");
        txtTenPhim.clear();
        txtThoiLuong.clear();
        txtQuocGia.clear();
        txtMoTa.clear();
    }

    private film getFullPhimDetails(long maPhim) {
        String sql = "SELECT mo_ta, quoc_gia FROM phim WHERE ma_phim = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, maPhim);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                film f = new film(maPhim);
                f.setMoTa(rs.getString("mo_ta"));
                f.setQuocGia(rs.getString("quoc_gia"));
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
    }

    // ================================
    // LOAD BẢNG PHIM
    // ================================
    public void loadPhimTable(String customSql, List<Object> params) {

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

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi CSDL", e.getMessage());
        }
    }

    // ================================
    // NÚT TÌM KIẾM / LỌC
    // ================================
    @FXML
    private void handleTimKiem() {

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
        openThemSuaPhimDialog(null);
    }

    @FXML
    private void handleSuaPhim() {
        film f = tblPhim.getSelectionModel().getSelectedItem();
        if (f == null) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng chọn phim để sửa");
            return;
        }
        openThemSuaPhimDialog(f);
    }

    private void openThemSuaPhimDialog(film phimToEdit) {

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
            showAlert(Alert.AlertType.ERROR, "Lỗi FXML", e.getMessage());
        }
    }

    // ================================
    // NÚT XOÁ
    // ================================
    @FXML
    private void handleXoaPhim() {

        film f = tblPhim.getSelectionModel().getSelectedItem();
        if (f == null) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Chọn phim để xoá");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Bạn chắc chắn muốn xoá phim \"" + f.getTenPhim() + "\"?",
                ButtonType.YES, ButtonType.NO);

        Optional<ButtonType> result = confirm.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.YES) {
            String sql = "DELETE FROM phim WHERE ma_phim = ?";

            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setLong(1, f.getMaPhim());
                int affected = ps.executeUpdate();

                if (affected > 0) {
                    showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã xoá phim.");
                    loadPhimTable(null, null);
                } else {
                    showAlert(Alert.AlertType.ERROR, "Lỗi", "Không xoá được.");
                }

            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Lỗi CSDL", e.getMessage());
            }
        }
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
