package controllers;

import database.DBConnection;
import models.film;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.File;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ThemSuaphimController {

    // ===================== FXML FIELD =====================
    @FXML private Label lblTitle;
    @FXML private TextField txtTenPhim;
    @FXML private TextField txtThoiLuong;
    @FXML private ComboBox<String> cbPhanLoai;
    @FXML private DatePicker dpNgayPhatHanh;
    @FXML private TextArea txtMoTa;
    @FXML private VBox vboxTheLoai;

    // --- Poster trong dialog ---
    @FXML private ImageView imgPoster;
    @FXML private Button btnUploadPoster;

    // ===================== BIẾN NỘI BỘ =====================
    private film phimToEdit;
    private PhimController parentController;

    private List<CheckBox> theLoaiCheckBoxes = new ArrayList<>();

    /** Đường dẫn ảnh hiện tại (URI) – sẽ lưu xuống cột poster_url */
    private String currentPosterPath;

    private static final ObservableList<String> PHAN_LOAI_LIST = FXCollections.observableArrayList(
            "P", "T13", "T16", "T18"
    );

    @FXML
    public void initialize() {
        cbPhanLoai.setItems(PHAN_LOAI_LIST);
        loadTheLoaiFromDatabase();
        setDefaultPoster();
    }

    public void setParentController(PhimController parentController) {
        this.parentController = parentController;
    }

    // ======================================================
    //            NẠP DỮ LIỆU PHIM CŨ KHI SỬA
    // ======================================================
    public void setPhimData(film phim) {
        this.phimToEdit = phim;

        lblTitle.setText("SỬA PHIM: " + phim.getTenPhim());
        txtTenPhim.setText(phim.getTenPhim());
        txtThoiLuong.setText(String.valueOf(phim.getThoiLuongPhut()));

        cbPhanLoai.getSelectionModel().select(phim.getPhanLoai());

        // ⭐ Lấy Ngày phát hành + mô tả + poster từ DB
        loadOriginalPhimData(phim.getMaPhim());

        // ⭐ Load thể loại đã chọn
        loadSelectedTheLoai(phim.getMaPhim());
    }

    private void loadOriginalPhimData(long maPhim) {
        String sql = "SELECT ngay_phat_hanh, mo_ta, poster_url FROM phim WHERE ma_phim = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, maPhim);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Date d = rs.getDate("ngay_phat_hanh");
                    if (d != null) dpNgayPhatHanh.setValue(d.toLocalDate());

                    String moTa = rs.getString("mo_ta");
                    txtMoTa.setText(moTa != null ? moTa : "");

                    currentPosterPath = rs.getString("poster_url");
                    if (currentPosterPath != null && !currentPosterPath.isBlank()) {
                        try {
                            Image img = new Image(currentPosterPath, true);
                            imgPoster.setImage(img);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            setDefaultPoster();
                        }
                    } else {
                        setDefaultPoster();
                    }
                }
            }

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể nạp dữ liệu phim.");
        }
    }

    // ======================================================
    //               NẠP THỂ LOẠI TỪ DB
    // ======================================================
    private void loadTheLoaiFromDatabase() {
        String sql = "SELECT ma_the_loai, ten_the_loai FROM the_loai ORDER BY ten_the_loai";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                long ma = rs.getLong("ma_the_loai");
                String ten = rs.getString("ten_the_loai");

                CheckBox cb = new CheckBox(ten);
                cb.setUserData(ma);

                theLoaiCheckBoxes.add(cb);
                vboxTheLoai.getChildren().add(cb);
            }

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi CSDL", "Không tải được thể loại!");
        }
    }

    private void loadSelectedTheLoai(long maPhim) {
        String sql = "SELECT ma_the_loai FROM phim_the_loai WHERE ma_phim = ?";
        List<Long> selectedIds = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, maPhim);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) selectedIds.add(rs.getLong("ma_the_loai"));

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tải thể loại cũ!");
        }

        for (CheckBox cb : theLoaiCheckBoxes) {
            long id = (Long) cb.getUserData();
            cb.setSelected(selectedIds.contains(id));
        }
    }

    private List<Long> getSelectedTheLoaiIds() {
        List<Long> ids = new ArrayList<>();
        for (CheckBox cb : theLoaiCheckBoxes)
            if (cb.isSelected()) ids.add((Long) cb.getUserData());
        return ids;
    }

    // ======================================================
    //                 XỬ LÝ NÚT LƯU
    // ======================================================
    @FXML
    private void handleLuuPhim() {

        if (txtTenPhim.getText().isBlank()
                || cbPhanLoai.getValue() == null
                || dpNgayPhatHanh.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Thiếu thông tin",
                    "Tên phim, phân loại và ngày phát hành không được bỏ trống!");
            return;
        }

        int thoiLuong;
        try {
            thoiLuong = Integer.parseInt(txtThoiLuong.getText());
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Thời lượng phải là số!");
            return;
        }

        if (phimToEdit == null)
            luuPhimMoi(txtTenPhim.getText(), thoiLuong, cbPhanLoai.getValue(),
                    dpNgayPhatHanh.getValue(), txtMoTa.getText());
        else
            capNhatPhim(phimToEdit.getMaPhim(), txtTenPhim.getText(), thoiLuong,
                    cbPhanLoai.getValue(), dpNgayPhatHanh.getValue(), txtMoTa.getText());
    }

    // ======================================================
    //                 INSERT PHIM MỚI
    // ======================================================
    private void luuPhimMoi(String tenPhim, int thoiLuong, String phanLoai,
                            LocalDate ngayPhatHanh, String moTa) {

        List<Long> dsTheLoai = getSelectedTheLoaiIds();
        Connection conn = null;

        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            String sql =
                "INSERT INTO phim(ten_phim, thoi_luong_phut, phan_loai, ngay_phat_hanh, mo_ta, poster_url) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, tenPhim);
            ps.setInt(2, thoiLuong);
            ps.setString(3, phanLoai);
            ps.setDate(4, Date.valueOf(ngayPhatHanh));
            ps.setString(5, moTa);
            ps.setString(6, currentPosterPath); // ⭐ lưu poster_url

            ps.executeUpdate();

            long maPhimMoi;
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) maPhimMoi = rs.getLong(1);
            else throw new SQLException("Không lấy được ID phim mới!");

            // Insert thể loại
            if (!dsTheLoai.isEmpty()) {
                String sqlTL = "INSERT INTO phim_the_loai(ma_phim, ma_the_loai) VALUES (?, ?)";
                PreparedStatement pstl = conn.prepareStatement(sqlTL);

                for (Long tl : dsTheLoai) {
                    pstl.setLong(1, maPhimMoi);
                    pstl.setLong(2, tl);
                    pstl.addBatch();
                }
                pstl.executeBatch();
            }

            conn.commit();
            showAlert(Alert.AlertType.INFORMATION, "Thành công!", "Đã thêm phim.");

            if (parentController != null) parentController.loadPhimTable(null, null);
            closeWindow();

        } catch (SQLException e) {
            try { if (conn != null) conn.rollback(); } catch (Exception ignored) {}
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể thêm phim!\n" + e.getMessage());

        } finally {
            try { if (conn != null) conn.setAutoCommit(true); conn.close(); } catch (Exception ignored) {}
        }
    }

    // ======================================================
    //                 UPDATE PHIM
    // ======================================================
    private void capNhatPhim(long id, String tenPhim, int thoiLuong,
                             String phanLoai, LocalDate ngayPhatHanh, String moTa) {

        List<Long> dsTheLoai = getSelectedTheLoaiIds();
        Connection conn = null;

        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            String sql =
                    "UPDATE phim SET ten_phim=?, thoi_luong_phut=?, phan_loai=?, " +
                    "ngay_phat_hanh=?, mo_ta=?, poster_url=? WHERE ma_phim=?";

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, tenPhim);
            ps.setInt(2, thoiLuong);
            ps.setString(3, phanLoai);
            ps.setDate(4, Date.valueOf(ngayPhatHanh));
            ps.setString(5, moTa);
            ps.setString(6, currentPosterPath);   // ⭐ cập nhật poster_url
            ps.setLong(7, id);
            ps.executeUpdate();

            // Xóa TL cũ
            PreparedStatement psDel = conn.prepareStatement(
                    "DELETE FROM phim_the_loai WHERE ma_phim=?");
            psDel.setLong(1, id);
            psDel.executeUpdate();

            // Chèn TL mới
            if (!dsTheLoai.isEmpty()) {
                PreparedStatement psTL = conn.prepareStatement(
                        "INSERT INTO phim_the_loai(ma_phim, ma_the_loai) VALUES (?, ?)");

                for (Long tl : dsTheLoai) {
                    psTL.setLong(1, id);
                    psTL.setLong(2, tl);
                    psTL.addBatch();
                }
                psTL.executeBatch();
            }

            conn.commit();
            showAlert(Alert.AlertType.INFORMATION, "Thành công!", "Đã cập nhật phim.");

            if (parentController != null) parentController.loadPhimTable(null, null);
            closeWindow();

        } catch (SQLException e) {
            try { if (conn != null) conn.rollback(); } catch (Exception ignored) {}
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể cập nhật phim!\n" + e.getMessage());

        } finally {
            try { if (conn != null) conn.setAutoCommit(true); conn.close(); } catch (Exception ignored) {}
        }
    }

    // ======================================================
    //            UPLOAD POSTER TRONG DIALOG
    // ======================================================
    @FXML
    private void handleUploadPoster() {
        try {
            Window window = imgPoster.getScene().getWindow();

            FileChooser chooser = new FileChooser();
            chooser.setTitle("Chọn poster phim");
            chooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Ảnh (*.png, *.jpg, *.jpeg)", "*.png", "*.jpg", "*.jpeg"),
                    new FileChooser.ExtensionFilter("Tất cả file", "*.*")
            );

            File file = chooser.showOpenDialog(window);
            if (file == null) return;

            currentPosterPath = file.toURI().toString();
            Image img = new Image(currentPosterPath, true);
            imgPoster.setImage(img);

        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tải ảnh: " + ex.getMessage());
        }
    }

    private void setDefaultPoster() {
        try {
            Image img = new Image(
                getClass().getResource("/Application/image/cinema.png").toExternalForm(),
                true
            );
            imgPoster.setImage(img);
        } catch (Exception ex) {
            imgPoster.setImage(null);
        }
    }

    // ================================
    //       HỦY
    // ================================
    @FXML
    private void handleHuy() {
        closeWindow();
    }

    private void closeWindow() {
        Stage s = (Stage) txtTenPhim.getScene().getWindow();
        s.close();
    }

    private void showAlert(Alert.AlertType t, String title, String msg) {
        Alert a = new Alert(t);
        a.setHeaderText(null);
        a.setTitle(title);
        a.setContentText(msg);
        a.showAndWait();
    }
}
