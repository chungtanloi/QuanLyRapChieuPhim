package controllers;

import database.DBConnection;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.math.BigDecimal;
import java.sql.*;
import java.time.format.DateTimeFormatter;

public class KhuyenMaiController {

    @FXML private TableView<KhuyenMaiVM> tblKhuyenMai;
    @FXML private TableColumn<KhuyenMaiVM, String> colMaKM, colLoaiGiam, colMucGiam, colDonToiThieu, colThoiGian, colTrangThaiKM;

    @FXML
    private void initialize(){
        if (tblKhuyenMai == null) return;

        // Map columns -> VM properties
        colMaKM.setCellValueFactory(d -> d.getValue().maCodeProperty());
        colLoaiGiam.setCellValueFactory(d -> d.getValue().kieuGiamProperty());
        colMucGiam.setCellValueFactory(d -> d.getValue().mucGiamTextProperty());
        colDonToiThieu.setCellValueFactory(d -> d.getValue().donToiThieuTextProperty());
        colThoiGian.setCellValueFactory(d -> d.getValue().thoiGianProperty());
        colTrangThaiKM.setCellValueFactory(d -> d.getValue().trangThaiProperty());

        loadData();
    }

    private void loadData(){
        final String sql = """
            SELECT ma_code, kieu_giam, gia_tri_giam, don_toi_thieu, bat_dau_luc, ket_thuc_luc, hoat_dong
            FROM khuyen_mai
            ORDER BY bat_dau_luc DESC
        """;

        ObservableList<KhuyenMaiVM> list = FXCollections.observableArrayList();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()){
            while (rs.next()){
                list.add(new KhuyenMaiVM(
                        rs.getString("ma_code"),
                        rs.getString("kieu_giam"),
                        rs.getBigDecimal("gia_tri_giam"),
                        rs.getBigDecimal("don_toi_thieu"),
                        rs.getTimestamp("bat_dau_luc"),
                        rs.getTimestamp("ket_thuc_luc"),
                        rs.getBoolean("hoat_dong")
                ));
            }
        } catch (SQLException e){
            showError("Lỗi tải khuyến mãi", e.getMessage());
        }
        tblKhuyenMai.setItems(list);
    }

    private void showError(String title, String msg){
        Alert a = new Alert(Alert.AlertType.ERROR, msg);
        a.setHeaderText(title);
        a.showAndWait();
    }

    // ===== View Model =====
    public static class KhuyenMaiVM {
        private final StringProperty maCode = new SimpleStringProperty();
        private final StringProperty kieuGiam = new SimpleStringProperty();
        private final ObjectProperty<BigDecimal> giaTriGiam = new SimpleObjectProperty<>();
        private final ObjectProperty<BigDecimal> donToiThieu = new SimpleObjectProperty<>();
        private final StringProperty thoiGian = new SimpleStringProperty();
        private final StringProperty trangThai = new SimpleStringProperty();
        private final StringProperty mucGiamText = new SimpleStringProperty();
        private final StringProperty donToiThieuText = new SimpleStringProperty();

        public KhuyenMaiVM(String maCode, String kieuGiam, BigDecimal giaTriGiam, BigDecimal donToiThieu,
                           Timestamp batDau, Timestamp ketThuc, boolean hoatDong){
            this.maCode.set(maCode);
            this.kieuGiam.set(kieuGiam);
            this.giaTriGiam.set(giaTriGiam);
            this.donToiThieu.set(donToiThieu);

            // Hiển thị thời gian đẹp
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM HH:mm");
            String tg = batDau.toLocalDateTime().format(fmt) + " - " + ketThuc.toLocalDateTime().format(fmt);
            this.thoiGian.set(tg);

            // Trạng thái
            long now = System.currentTimeMillis();
            if (!hoatDong) this.trangThai.set("🔴 Tắt");
            else if (batDau.getTime() > now) this.trangThai.set("🟡 Sắp diễn ra");
            else if (ketThuc.getTime() < now) this.trangThai.set("🔴 Hết hạn");
            else this.trangThai.set("🟢 Đang áp dụng");

            // Text mức giảm & đơn tối thiểu
            if ("PHAN_TRAM".equalsIgnoreCase(kieuGiam)) {
                this.mucGiamText.set(String.format("%.0f%%", giaTriGiam));
            } else {
                this.mucGiamText.set(String.format("%,.0f đ", giaTriGiam));
            }
            if (donToiThieu != null && donToiThieu.compareTo(BigDecimal.ZERO) > 0) {
                this.donToiThieuText.set(String.format("%,.0f đ", donToiThieu));
            } else {
                this.donToiThieuText.set("Không có");
            }
        }

        public StringProperty maCodeProperty(){ return maCode; }
        public StringProperty kieuGiamProperty(){ return kieuGiam; }
        public StringProperty thoiGianProperty(){ return thoiGian; }
        public StringProperty trangThaiProperty(){ return trangThai; }
        public StringProperty mucGiamTextProperty(){ return mucGiamText; }
        public StringProperty donToiThieuTextProperty(){ return donToiThieuText; }
    }
}
