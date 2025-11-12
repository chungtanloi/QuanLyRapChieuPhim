package controllers;

import database.DBConnection;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.math.BigDecimal;
import java.sql.*;

/**
 * Combo tab controller (schema WITHOUT 'mo_ta' column).
 * Table combo: ma_combo (PK), ten_combo, gia, hoat_dong, tao_luc, cap_nhat_luc
 */
public class ComboController {

    @FXML private TableView<ComboVM> tblCombo;
    @FXML private TableColumn<ComboVM, String> colTenCombo, colTrangThaiCombo;
    @FXML private TableColumn<ComboVM, BigDecimal> colGiaCombo;

    @FXML private TableView<ComboChiTietVM> tblComboChiTiet;
    @FXML private TableColumn<ComboChiTietVM, String> colSanPham;
    @FXML private TableColumn<ComboChiTietVM, Number> colSoLuong;
    @FXML private TableColumn<ComboChiTietVM, BigDecimal> colGiaSanPham;

    @FXML
    private void initialize(){
        // ===== Columns for COMBO =====
        colTenCombo.setCellValueFactory(d -> d.getValue().tenComboProperty());
        colGiaCombo.setCellValueFactory(d -> d.getValue().giaComboProperty());
        colTrangThaiCombo.setCellValueFactory(d -> d.getValue().trangThaiProperty());
        colGiaCombo.setCellFactory(tc -> new TableCell<>(){
            @Override protected void updateItem(BigDecimal v, boolean empty){
                super.updateItem(v, empty);
                setText(empty||v==null?null:String.format("%,.0f đ", v));
            }
        });

        // ===== Columns for COMBO DETAILS =====
        if (tblComboChiTiet != null) {
            colSanPham.setCellValueFactory(d -> d.getValue().tenSanPhamProperty());
            colSoLuong.setCellValueFactory(new PropertyValueFactory<>("soLuong"));
            colGiaSanPham.setCellValueFactory(d -> d.getValue().giaSanPhamProperty());
            colGiaSanPham.setCellFactory(tc -> new TableCell<>(){
                @Override protected void updateItem(BigDecimal v, boolean empty){
                    super.updateItem(v, empty);
                    setText(empty||v==null?null:String.format("%,.0f đ", v));
                }
            });
        }

        // Load initial data
        loadComboData();

        // On selection -> load details
        if (tblCombo != null) {
            tblCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    loadComboChiTiet(newVal.maComboProperty().get());
                } else if (tblComboChiTiet != null) {
                    tblComboChiTiet.getItems().clear();
                }
            });
        }
    }

    private void loadComboData() {
        if (tblCombo == null) return;
        final String sql = """
            SELECT ma_combo, ten_combo, gia, hoat_dong
            FROM combo
            ORDER BY ma_combo
        """;

        ObservableList<ComboVM> list = FXCollections.observableArrayList();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new ComboVM(
                        rs.getInt("ma_combo"),
                        rs.getString("ten_combo"),
                        rs.getBigDecimal("gia"),
                        rs.getBoolean("hoat_dong")
                ));
            }
        } catch (SQLException e) {
            showError("Lỗi tải combo", e.getMessage());
        }
        tblCombo.setItems(list);
        if (!list.isEmpty()) {
            tblCombo.getSelectionModel().selectFirst();
        }
    }

    private void loadComboChiTiet(int maCombo) {
        if (tblComboChiTiet == null) return;

        final String sql = """
            SELECT sp.ten_san_pham, sp.loai, sp.gia, ct.so_luong
            FROM combo_chi_tiet ct
            JOIN san_pham sp ON ct.ma_san_pham = sp.ma_san_pham
            WHERE ct.ma_combo = ?
            ORDER BY sp.loai, sp.ten_san_pham
        """;

        ObservableList<ComboChiTietVM> list = FXCollections.observableArrayList();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maCombo);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new ComboChiTietVM(
                            rs.getString("ten_san_pham"),
                            rs.getString("loai"),
                            rs.getInt("so_luong"),
                            rs.getBigDecimal("gia")
                    ));
                }
            }
        } catch (SQLException e) {
            showError("Lỗi tải chi tiết combo", e.getMessage());
        }
        tblComboChiTiet.setItems(list);
    }

    private void showError(String header, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR, msg);
        a.setHeaderText(header);
        a.showAndWait();
    }

    // ====== VMs ======
    public static class ComboVM {
        private final IntegerProperty maCombo = new SimpleIntegerProperty();
        private final StringProperty tenCombo = new SimpleStringProperty();
        private final ObjectProperty<BigDecimal> giaCombo = new SimpleObjectProperty<>();
        private final StringProperty trangThai = new SimpleStringProperty();

        public ComboVM(int ma, String ten, BigDecimal gia, boolean hoatDong){
            this.maCombo.set(ma);
            this.tenCombo.set(ten);
            this.giaCombo.set(gia);
            this.trangThai.set(hoatDong ? "Đang bán" : "Ngừng bán");
        }
        public IntegerProperty maComboProperty(){ return maCombo; }
        public StringProperty tenComboProperty(){ return tenCombo; }
        public ObjectProperty<BigDecimal> giaComboProperty(){ return giaCombo; }
        public StringProperty trangThaiProperty(){ return trangThai; }
    }

    public static class ComboChiTietVM {
        private final StringProperty tenSanPham = new SimpleStringProperty();
        private final StringProperty loai = new SimpleStringProperty();
        private final IntegerProperty soLuong = new SimpleIntegerProperty();
        private final ObjectProperty<BigDecimal> giaSanPham = new SimpleObjectProperty<>();

        public ComboChiTietVM(String ten, String loai, int sl, BigDecimal gia){
            this.tenSanPham.set(ten);
            this.loai.set(loai);
            this.soLuong.set(sl);
            this.giaSanPham.set(gia);
        }
        public StringProperty tenSanPhamProperty(){ return tenSanPham; }
        public StringProperty loaiProperty(){ return loai; }
        public IntegerProperty soLuongProperty(){ return soLuong; }
        public ObjectProperty<BigDecimal> giaSanPhamProperty(){ return giaSanPham; }
    }
}
