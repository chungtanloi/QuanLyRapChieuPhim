package controllers;

import database.DBConnection; 
import models.phim; 
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

public class phimcontroller {

    // --- KHAI BÁO FXML CHO TABLE VÀ LỌC ---
    @FXML private TableView<phim> tblPhim;
    @FXML private TableColumn<phim, Long> colMaPhim;
    @FXML private TableColumn<phim, String> colTenPhim;
    @FXML private TableColumn<phim, Integer> colThoiLuong;
    @FXML private TableColumn<phim, String> colTheLoai;
    @FXML private TableColumn<phim, String> colPhanLoai;
    @FXML private TableColumn<phim, String> colNgayPhatHanh;
    @FXML private TableColumn<phim, String> colTrangThai;
    
    // Các field Lọc/Tìm kiếm
    @FXML private TextField txtTimKiemPhim;
    @FXML private ComboBox<IdNamePair> cbLocTheoTheLoai;
    @FXML private ComboBox<String> cbLocTheoPhanLoai;
    @FXML private DatePicker dpTuNgay;
    @FXML private DatePicker dpDenNgay;
    @FXML private CheckBox chkDangChieu;
    @FXML private CheckBox chkSapChieu;

    // Các field chi tiết (Không dùng cho chức năng chính, chỉ để hiển thị)
    @FXML private TextField txtMaPhim, txtTenPhim, txtThoiLuong, txtQuocGia;
    @FXML private TextArea txtMoTa;
    @FXML private ImageView imgPhimPoster;
    
    // Khắc phục lỗi: Khởi tạo ngay lập tức, không để NULL!
    private final ObservableList<phim> danhSachPhim = FXCollections.observableArrayList();


    @FXML
    public void initialize() {
        // 1. Liên kết cột (TableView)
        colMaPhim.setCellValueFactory(new PropertyValueFactory<>("maPhim")); 
        colTenPhim.setCellValueFactory(new PropertyValueFactory<>("tenPhim")); 
        colThoiLuong.setCellValueFactory(new PropertyValueFactory<>("thoiLuongPhut")); 
        colTheLoai.setCellValueFactory(new PropertyValueFactory<>("theLoai")); 
        colPhanLoai.setCellValueFactory(new PropertyValueFactory<>("phanLoai")); 
        colNgayPhatHanh.setCellValueFactory(new PropertyValueFactory<>("ngayPhatHanh")); 
        colTrangThai.setCellValueFactory(new PropertyValueFactory<>("trangThai")); 
        
        // 2. Tải dữ liệu lọc (ComboBox)
        if (cbLocTheoTheLoai != null) {
            loadFilterData();
        }

        // 3. Tải dữ liệu bảng ban đầu
        loadPhimTable(null, null);
    }

    // ======================================================================
    // HÀM CHUNG: TẢI BẢNG VÀ NẠP COMBO BOX
    // ======================================================================
    
    private void loadFilterData() {
        // Nạp Thể loại:
        String sqlTheLoai = "SELECT ma_the_loai, ten_the_loai FROM the_loai ORDER BY ten_the_loai";
        loadIdNameComboBox(sqlTheLoai, cbLocTheoTheLoai);
        
        // Nạp Phân loại (Manual list):
        ObservableList<String> phanLoaiList = FXCollections.observableArrayList(
            "Tất cả", "P", "T13", "T16", "T18"
        );
        cbLocTheoPhanLoai.setItems(phanLoaiList);
        cbLocTheoPhanLoai.getSelectionModel().selectFirst();
        
        // Chọn mặc định trạng thái
        chkDangChieu.setSelected(true);
        chkSapChieu.setSelected(true);
    }
    
    private void loadIdNameComboBox(String sql, ComboBox<IdNamePair> comboBox) {
        ObservableList<IdNamePair> list = FXCollections.observableArrayList();
        list.add(new IdNamePair(0, "Tất cả")); 
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                long id = rs.getLong(1);
                String name = rs.getString(2);
                list.add(new IdNamePair(id, name));
            }
            comboBox.setItems(list);
            comboBox.getSelectionModel().selectFirst();

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi nạp Combo Box", "Không thể nạp dữ liệu lọc từ CSDL.");
            e.printStackTrace();
        }
    }

    /**
     * Hàm tải dữ liệu chính, dùng cho cả Initialize và Tìm Kiếm/Lọc.
     * @param customSql SQL Query tùy chỉnh (dùng cho Tìm kiếm), hoặc null/rỗng để tải tất cả.
     * @param params Danh sách tham số cho PreparedStatement (dùng cho Tìm kiếm), hoặc null.
     */
    public void loadPhimTable(String customSql, List<Object> params) {
        danhSachPhim.clear();
        
        // SQL mặc định (Tải tất cả)
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
        
        String sqlToExecute = (customSql == null || customSql.isEmpty()) ? sqlDefault : customSql;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sqlToExecute)) {
            
            if (params != null) {
                for (int i = 0; i < params.size(); i++) {
                    // Dùng setObject để gán kiểu dữ liệu động
                    ps.setObject(i + 1, params.get(i));
                }
            }
            
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                danhSachPhim.add(new phim(
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
            if (danhSachPhim.isEmpty()) {
                tblPhim.setPlaceholder(new Label("Không tìm thấy bộ phim nào theo điều kiện lọc."));
            }

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "LỖI CSDL", "Không thể tải dữ liệu phim. Vui lòng kiểm tra kết nối CSDL. Lỗi: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ======================================================================
    // HÀM LỌC ĐỘNG
    // ======================================================================

    @FXML
    private void handleTimKiem() {
        // Lấy giá trị lọc
        String tenPhim = txtTimKiemPhim.getText().trim();
        IdNamePair selectedTheLoai = cbLocTheoTheLoai.getSelectionModel().getSelectedItem();
        long maTheLoai = (selectedTheLoai != null) ? selectedTheLoai.getId() : 0;
        String phanLoai = cbLocTheoPhanLoai.getSelectionModel().getSelectedItem();
        LocalDate tuNgay = dpTuNgay.getValue();
        LocalDate denNgay = dpDenNgay.getValue();
        boolean dangChieu = chkDangChieu.isSelected();
        boolean sapChieu = chkSapChieu.isSelected();

        // Xây dựng câu truy vấn SQL động
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

        if (maTheLoai > 0) {
            sql.append(" AND EXISTS (SELECT 1 FROM phim_the_loai ptl2 WHERE ptl2.ma_phim = p.ma_phim AND ptl2.ma_the_loai = ?) ");
            params.add(maTheLoai);
        }

        if (phanLoai != null && !phanLoai.equals("Tất cả")) {
            sql.append(" AND p.phan_loai = ? ");
            params.add(phanLoai);
        }
        
        if (dangChieu && !sapChieu) {
            sql.append(" AND p.ngay_phat_hanh <= CURDATE() ");
        } else if (!dangChieu && sapChieu) {
            sql.append(" AND p.ngay_phat_hanh > CURDATE() ");
        } else if (!dangChieu && !sapChieu) {
            sql.append(" AND 1=0 "); // Không hiển thị gì
        }

        if (tuNgay != null) {
            sql.append(" AND p.ngay_phat_hanh >= ? ");
            params.add(Date.valueOf(tuNgay));
        }
        if (denNgay != null) {
            sql.append(" AND p.ngay_phat_hanh <= ? ");
            params.add(Date.valueOf(denNgay));
        }
        
        sql.append(" GROUP BY p.ma_phim ORDER BY p.ngay_phat_hanh DESC");
        
        loadPhimTable(sql.toString(), params);
    }
    
    // ======================================================================
    // CHỨC NĂNG CRUD
    // ======================================================================
    
    @FXML
    private void handleThemMoi() {
        openThemSuaPhimDialog(null);
    }

    @FXML
    private void handleSuaPhim() {
        phim selectedPhim = tblPhim.getSelectionModel().getSelectedItem();
        if (selectedPhim == null) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng chọn một phim để sửa.");
            return;
        }
        openThemSuaPhimDialog(selectedPhim);
    }
    
    /**
     * Mở Dialog Thêm/Sửa Phim
     * @param phimToEdit Đối tượng phim cần sửa, hoặc null nếu là thêm mới.
     */
    private void openThemSuaPhimDialog(phim phimToEdit) {
        try {
            // 
            // ĐIỀU CHỈNH ĐƯỜNG DẪN FXML CHO KHỚP VỚI CẤU TRÚC PROJECT CỦA BẠN!
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/ThemSuaPhimDialog.fxml"));
            Parent parent = loader.load();
            
            // Lấy Controller của Dialog
            ThemSuaphimController controller = loader.getController();
            controller.setParentController(this); // Gán Controller cha (phimcontroller)
            
            // Cài đặt Dialog
            Stage stage = new Stage();
            stage.setTitle((phimToEdit == null ? "Thêm Phim Mới" : "Sửa Phim: " + phimToEdit.getTenPhim()));
            stage.setScene(new Scene(parent));
            stage.initModality(Modality.APPLICATION_MODAL); // Chặn cửa sổ chính
            
            if (phimToEdit != null) {
                controller.setPhimData(phimToEdit); // Nạp dữ liệu vào form nếu là Sửa
            }

            stage.showAndWait();
            
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi tải giao diện", "Không tìm thấy file FXML Dialog: " + e.getMessage());
            e.printStackTrace();
        }
    }


    @FXML
    private void handleXoaPhim() {
        phim selectedPhim = tblPhim.getSelectionModel().getSelectedItem();
        if (selectedPhim == null) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng chọn một phim để xóa.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, 
            "Bạn có chắc chắn muốn xóa phim '" + selectedPhim.getTenPhim() + "' (ID: " + selectedPhim.getMaPhim() + ")? Hành động này sẽ xóa suất chiếu liên quan và không thể hoàn tác.", 
            ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Xác nhận xóa");
        Optional<ButtonType> result = confirm.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.YES) {
            // Do đã thiết lập ON DELETE CASCADE, chỉ cần xóa trong bảng phim.
            String sql = "DELETE FROM phim WHERE ma_phim = ?";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                
                ps.setLong(1, selectedPhim.getMaPhim());
                int rowsAffected = ps.executeUpdate();

                if (rowsAffected > 0) {
                    showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã xóa phim thành công.");
                    loadPhimTable(null, null); // Tải lại bảng
                } else {
                    showAlert(Alert.AlertType.ERROR, "Lỗi", "Không tìm thấy phim để xóa hoặc lỗi CSDL.");
                }

            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Lỗi CSDL", "Không thể xóa phim. Có thể do lỗi khóa ngoại hoặc kết nối: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    // Hàm hiển thị thông báo chung
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}