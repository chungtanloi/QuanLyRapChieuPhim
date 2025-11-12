package controllers;

import database.DBConnection;
import models.BaoCaoPhimNgay;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.sql.*;
import java.time.LocalDate;

public class BaoCaoController {

    // KẾT NỐI VỚI FXML: Cần đảm bảo fx:id trong FXML khớp với tên biến dưới đây
    @FXML private TableView<BaoCaoPhimNgay> tblBaoCao;
    @FXML private TableColumn<BaoCaoPhimNgay, String> colTenPhim;
    @FXML private TableColumn<BaoCaoPhimNgay, Integer> colTongSuat;
    @FXML private TableColumn<BaoCaoPhimNgay, Integer> colThoiLuong;
    @FXML private DatePicker dpNgayBaoCao;
    @FXML private Button btnXemBaoCao;
    @FXML private Label lblTongThoiGian; // Thêm Label hiển thị kết quả Function

    @FXML
    private void initialize() {
        setupTableColumns();
        
        // Thiết lập ngày mặc định là hôm nay và tự động tải báo cáo
        dpNgayBaoCao.setValue(LocalDate.now());
        
        // Lắng nghe sự kiện (Click nút hoặc đổi ngày)
        btnXemBaoCao.setOnAction(e -> loadBaoCao());
        dpNgayBaoCao.valueProperty().addListener((obs, oldV, newV) -> loadBaoCao());
        
        loadBaoCao(); // Tải dữ liệu ban đầu
    }

    private void setupTableColumns() {
        colTenPhim.setCellValueFactory(cellData -> cellData.getValue().tenPhimProperty());
        colTongSuat.setCellValueFactory(cellData -> cellData.getValue().tongSuatChieuProperty().asObject());
        colThoiLuong.setCellValueFactory(cellData -> cellData.getValue().thoiLuongPhutProperty().asObject());
        
        // Định dạng cột thời lượng (ví dụ: 120 phút)
        colThoiLuong.setCellFactory(column -> new TableCell<BaoCaoPhimNgay, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item + " phút");
                }
            }
        });
    }

    private void loadBaoCao() {
        LocalDate ngayBaoCao = dpNgayBaoCao.getValue();
        if (ngayBaoCao == null) return;
        
        ObservableList<BaoCaoPhimNgay> baoCaoList = FXCollections.observableArrayList();
        
        // 1. CHẠY TRUY VẤN BÁO CÁO DANH SÁCH PHIM
        String sqlBaoCao = """
            SELECT 
                p.ten_phim,
                COUNT(sc.ma_suat_chieu) AS tong_suat_chieu,
                p.thoi_luong_phut
            FROM phim p
            JOIN suat_chieu sc ON p.ma_phim = sc.ma_phim
            WHERE DATE(sc.bat_dau_luc) = ?
            GROUP BY p.ma_phim, p.ten_phim, p.thoi_luong_phut
            ORDER BY tong_suat_chieu DESC
        """;
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sqlBaoCao)) {
            
            ps.setDate(1, Date.valueOf(ngayBaoCao));
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                BaoCaoPhimNgay item = new BaoCaoPhimNgay(
                    rs.getString("ten_phim"),
                    rs.getInt("tong_suat_chieu"),
                    rs.getInt("thoi_luong_phut")
                );
                baoCaoList.add(item);
            }
            tblBaoCao.setItems(baoCaoList);
            
            // 2. GỌI FUNCTION TÍNH TỔNG THỜI LƯỢNG
            loadTongThoiGian(conn, ngayBaoCao);
            
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi tải báo cáo từ CSDL: " + e.getMessage());
        }
    }
    
    // Hàm gọi Function SQL func_tong_thoi_luong_phim_ngay
    private void loadTongThoiGian(Connection conn, LocalDate ngayBaoCao) throws SQLException {
        String sqlFunction = "{? = CALL func_tong_thoi_luong_phim_ngay(?)}";
        int tongThoiLuong = 0;
        
        try (CallableStatement cs = conn.prepareCall(sqlFunction)) {
            cs.registerOutParameter(1, Types.INTEGER); 
            cs.setDate(2, Date.valueOf(ngayBaoCao));   
            cs.execute();
            
            tongThoiLuong = cs.getInt(1);
            
            int gio = tongThoiLuong / 60;
            int phut = tongThoiLuong % 60;
            
            lblTongThoiGian.setText(String.format("Tổng thời lượng phim chiếu trong ngày: %d giờ %d phút", gio, phut));
            
        } catch (SQLException e) {
            // Xử lý lỗi riêng cho Function
            lblTongThoiGian.setText("Lỗi tính tổng thời lượng. Vui lòng kiểm tra Function SQL.");
            throw e; 
        }
    }

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}