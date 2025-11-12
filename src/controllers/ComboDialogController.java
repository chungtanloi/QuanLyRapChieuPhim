package controllers;

import database.DBConnection;
import javafx.beans.property.*;
import javafx.collections.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.control.cell.CheckBoxTableCell;

public class ComboDialogController {

    @FXML private TableView<ComboItem> tblCombo;
    @FXML private TableColumn<ComboItem, Boolean> colChon;
    @FXML private TableColumn<ComboItem, String> colTenCombo;
    @FXML private TableColumn<ComboItem, BigDecimal> colGia;
    @FXML private Label lblTongTien;

    private final ObservableList<ComboItem> comboList = FXCollections.observableArrayList();
    private final List<ComboItem> selectedCombos = new ArrayList<>();

    @FXML
    public void initialize() {
        setupTable();
        loadCombos();
    }

    private void setupTable() {
        colChon.setCellValueFactory(c -> c.getValue().chonProperty());
        colChon.setCellFactory(CheckBoxTableCell.forTableColumn(colChon));
        colChon.setEditable(true);

        colTenCombo.setCellValueFactory(c -> c.getValue().tenComboProperty());
        colGia.setCellValueFactory(c -> c.getValue().giaProperty());

        tblCombo.setItems(comboList);
        tblCombo.setEditable(true);

        // Khi tick/untick -> tính lại tổng tiền
        comboList.addListener((ListChangeListener<ComboItem>) change -> updateTongTien());
    }

    private void loadCombos() {
        String sql = "SELECT ma_combo, ten_combo, gia FROM combo WHERE hoat_dong = 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            comboList.clear();
            while (rs.next()) {
                long ma = rs.getLong("ma_combo");
                String ten = rs.getString("ten_combo");
                BigDecimal gia = rs.getBigDecimal("gia");
                comboList.add(new ComboItem(ma, ten, gia));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onConfirm() {
        selectedCombos.clear();
        BigDecimal tong = BigDecimal.ZERO;
        for (ComboItem c : comboList) {
            if (c.isChon()) {
                selectedCombos.add(c);
                tong = tong.add(c.getGia());
            }
        }
        lblTongTien.setText(tong.toPlainString());
        Stage stage = (Stage) tblCombo.getScene().getWindow();
        stage.setUserData(selectedCombos);
        stage.close();
    }

    @FXML
    private void onCancel() {
        selectedCombos.clear();
        Stage stage = (Stage) tblCombo.getScene().getWindow();
        stage.setUserData(null);
        stage.close();
    }

    private void updateTongTien() {
        BigDecimal tong = BigDecimal.ZERO;
        for (ComboItem c : comboList) {
            if (c.isChon()) tong = tong.add(c.getGia());
        }
        lblTongTien.setText(tong.toPlainString());
    }

    // Getter để các controller khác lấy kết quả sau khi dialog đóng
    public List<ComboItem> getSelectedCombos() {
        return selectedCombos;
    }

    // ======= Inner class =======
    public static class ComboItem {
        private final LongProperty maCombo = new SimpleLongProperty();
        private final StringProperty tenCombo = new SimpleStringProperty();
        private final ObjectProperty<BigDecimal> gia = new SimpleObjectProperty<>();
        private final BooleanProperty chon = new SimpleBooleanProperty(false);

        public ComboItem(long ma, String ten, BigDecimal gia) {
            this.maCombo.set(ma);
            this.tenCombo.set(ten);
            this.gia.set(gia);
        }

        public long getMaCombo() { return maCombo.get(); }
        public String getTenCombo() { return tenCombo.get(); }
        public BigDecimal getGia() { return gia.get(); }
        public boolean isChon() { return chon.get(); }

        public LongProperty maComboProperty() { return maCombo; }
        public StringProperty tenComboProperty() { return tenCombo; }
        public ObjectProperty<BigDecimal> giaProperty() { return gia; }
        public BooleanProperty chonProperty() { return chon; }
    }
}
