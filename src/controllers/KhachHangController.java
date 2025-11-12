package controllers;

import database.DBConnection;
import javafx.animation.PauseTransition;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.util.Duration;

import java.sql.*;
import java.time.LocalDate;

public class KhachHangController {

    // ===== FXML controls =====
    @FXML private TextField txtTimKhach;
    @FXML private Button btnTimKhach, btnClearTimKH, btnThemKH, btnThemKHDialog;
    @FXML private TableView<KhachHangVM> tblKhachHang;
    @FXML private TableColumn<KhachHangVM, Number> colMaKH, colDiem;
    @FXML private TableColumn<KhachHangVM, String> colHoTen, colSDT, colEmail, colHangTV;

    @FXML
    private void initialize() {
        initKhachHangTable();
        wireKhachHangButtons();
        wireKhachHangSearch();
        loadKhachHang();
    }

    // ===== Table setup =====
    private void initKhachHangTable() {
        if (tblKhachHang == null) return;

        if (colMaKH != null)   colMaKH.setCellValueFactory(d -> d.getValue().maProperty());
        if (colHoTen != null)  colHoTen.setCellValueFactory(d -> d.getValue().hoTenProperty());
        if (colSDT != null)    colSDT.setCellValueFactory(d -> d.getValue().sdtProperty());
        if (colEmail != null)  colEmail.setCellValueFactory(d -> d.getValue().emailProperty());
        if (colHangTV != null) colHangTV.setCellValueFactory(d -> d.getValue().hangProperty());
        if (colDiem != null)   colDiem.setCellValueFactory(d -> d.getValue().diemProperty());

        addCustomerActionColumn();
    }

    private void addCustomerActionColumn() {
        if (tblKhachHang == null) return;

        TableColumn<KhachHangVM, Void> colAction = new TableColumn<>("Thao tác");
        colAction.setPrefWidth(220);
        colAction.setCellFactory(col -> new TableCell<>() {
            private final Button btnEdit   = new Button("Sửa");
            private final Button btnToggle = new Button("Bật/Tắt");
            private final Button btnDelete = new Button("Xóa");
            private final HBox box = new HBox(6, btnEdit, btnToggle, btnDelete);

            {
                btnEdit.setStyle("-fx-background-color:#2196f3; -fx-text-fill:white; -fx-font-weight:bold; -fx-background-radius:6; -fx-padding:5 10;");
                btnToggle.setStyle("-fx-background-color:#ff9800; -fx-text-fill:white; -fx-font-weight:bold; -fx-background-radius:6; -fx-padding:5 10;");
                btnDelete.setStyle("-fx-background-color:#f44336; -fx-text-fill:white; -fx-font-weight:bold; -fx-background-radius:6; -fx-padding:5 10;");

                btnEdit.setOnAction(e -> {
                    KhachHangVM kh = getTableView().getItems().get(getIndex());
                    showKhachHangDialog(kh);
                });
                btnToggle.setOnAction(e -> {
                    KhachHangVM kh = getTableView().getItems().get(getIndex());
                    toggleKhachHangStatus(kh);
                });
                btnDelete.setOnAction(e -> {
                    KhachHangVM kh = getTableView().getItems().get(getIndex());
                    deleteKhachHang(kh);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });

        boolean exists = tblKhachHang.getColumns().stream().anyMatch(c -> "Thao tác".equals(c.getText()));
        if (!exists) tblKhachHang.getColumns().add(colAction);
    }

    // ===== Wire events =====
    private void wireKhachHangButtons() {
        if (btnThemKH != null)       btnThemKH.setOnAction(e -> showKhachHangDialog(null));
        if (btnThemKHDialog != null) btnThemKHDialog.setOnAction(e -> showKhachHangDialog(null));
        if (btnTimKhach != null)     btnTimKhach.setOnAction(e -> timKhachHang());
        if (btnClearTimKH != null)   btnClearTimKH.setOnAction(e -> { if (txtTimKhach != null) txtTimKhach.clear(); loadKhachHang(); });
    }

    private void wireKhachHangSearch() {
        if (txtTimKhach == null) return;

        txtTimKhach.setOnKeyPressed(e -> { if (e.getCode() == KeyCode.ENTER) timKhachHang(); });

        PauseTransition pt = new PauseTransition(Duration.millis(300));
        txtTimKhach.textProperty().addListener((obs, ov, nv) -> {
            pt.setOnFinished(ev -> timKhachHang());
            pt.playFromStart();
        });

        ContextMenu cm = new ContextMenu();
        MenuItem miClear = new MenuItem("Xoá ô tìm");
        miClear.setOnAction(ev -> { txtTimKhach.clear(); loadKhachHang(); });
        cm.getItems().add(miClear);
        txtTimKhach.setContextMenu(cm);
    }

    // ===== Data load/search =====
    @FXML
private void timKhachHang() {
    if (txtTimKhach == null) {
        loadKhachHang("");
        return;
    }
    String kw = txtTimKhach.getText();
    if (kw == null) kw = "";
    loadKhachHang(kw.trim());
}

private void loadKhachHang() { loadKhachHang(""); }

private void loadKhachHang(String keyword) {
    if (tblKhachHang == null) return;

    String k = (keyword == null) ? "" : keyword.trim();

    final String baseSql = """
        SELECT kh.ma_khach_hang,
               tk.ma_tai_khoan,
               tk.ho_ten,
               tk.so_dien_thoai,
               tk.email,
               kh.diem_tich_luy
        FROM khach_hang kh
        JOIN tai_khoan tk ON kh.ma_tai_khoan = tk.ma_tai_khoan
        ORDER BY kh.ma_khach_hang DESC
        LIMIT 300
    """;

    final String searchSql = """
        SELECT kh.ma_khach_hang,
               tk.ma_tai_khoan,
               tk.ho_ten,
               tk.so_dien_thoai,
               tk.email,
               kh.diem_tich_luy
        FROM khach_hang kh
        JOIN tai_khoan tk ON kh.ma_tai_khoan = tk.ma_tai_khoan
        WHERE tk.ho_ten        LIKE ?
           OR tk.so_dien_thoai LIKE ?
           OR tk.email         LIKE ?
        ORDER BY kh.ma_khach_hang DESC
        LIMIT 300
    """;

    ObservableList<KhachHangVM> list = FXCollections.observableArrayList();

    try (Connection c = DBConnection.getConnection();
         PreparedStatement ps = k.isEmpty()
                 ? c.prepareStatement(baseSql)
                 : c.prepareStatement(searchSql)) {

        if (!k.isEmpty()) {
            // thêm wildcard + escape để không lỗi khi có % / _
            String like = "%" + k.replace("\\","\\\\").replace("%","\\%").replace("_","\\_") + "%";
            ps.setString(1, like);
            ps.setString(2, like);
            ps.setString(3, like);
        }

        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int diem = rs.getInt("diem_tich_luy");
                String hang = (diem >= 2000) ? "PLATINUM"
                             : (diem >= 1000) ? "GOLD"
                             : (diem >= 500)  ? "SILVER"
                             : "BRONZE";
                list.add(new KhachHangVM(
                        rs.getInt("ma_khach_hang"),
                        rs.getInt("ma_tai_khoan"),
                        rs.getString("ho_ten"),
                        rs.getString("so_dien_thoai"),
                        rs.getString("email"),
                        hang, diem
                ));
            }
        }
    } catch (SQLException e) {
        showError("Lỗi tải khách hàng", e.getMessage());
    }

    tblKhachHang.setItems(list);
}

    // ===== Dialog thêm/sửa (KHÔNG còn trạng thái) =====
    private void showKhachHangDialog(KhachHangVM existingKH) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(existingKH == null ? "➕ Thêm khách hàng" : "✏️ Sửa khách hàng");
        dialog.setHeaderText(existingKH == null ? "Điền thông tin khách hàng mới"
                                                : "Chỉnh sửa: " + existingKH.hoTenProperty().get());

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);
        grid.setPadding(new Insets(16));

        TextField tfHoTen = new TextField();
        TextField tfSDT = new TextField();
        TextField tfEmail = new TextField();
        DatePicker dpNgaySinh = new DatePicker();
        PasswordField pfMatKhau = new PasswordField(); // chỉ dùng khi thêm mới

        grid.addRow(0, new Label("👤 Họ tên:"), tfHoTen);
        grid.addRow(1, new Label("📱 Số ĐT:"), tfSDT);
        grid.addRow(2, new Label("📧 Email:"), tfEmail);
        grid.addRow(3, new Label("🎂 Ngày sinh:"), dpNgaySinh);
        if (existingKH == null) grid.addRow(4, new Label("🔒 Mật khẩu:"), pfMatKhau);

        if (existingKH != null) {
            tfHoTen.setText(existingKH.hoTenProperty().get());
            tfSDT.setText(existingKH.sdtProperty().get());
            tfEmail.setText(existingKH.emailProperty().get());
        }

        dialog.getDialogPane().setContent(grid);
        ButtonType btnSave = new ButtonType("Lưu", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnCancel = new ButtonType("Hủy", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(btnSave, btnCancel);

        Button saveButton = (Button) dialog.getDialogPane().lookupButton(btnSave);
        saveButton.setDisable(true);
        tfHoTen.textProperty().addListener((o,ov,nv)-> saveButton.setDisable(nv.trim().isEmpty() || tfSDT.getText().trim().isEmpty()));
        tfSDT.textProperty().addListener((o,ov,nv)-> saveButton.setDisable(nv.trim().isEmpty() || tfHoTen.getText().trim().isEmpty()));

        dialog.showAndWait().ifPresent(resp -> {
            if (resp == btnSave) {
                String hoTen = tfHoTen.getText().trim();
                String sdt   = tfSDT.getText().trim();
                String email = tfEmail.getText().trim();
                LocalDate ngaySinh = dpNgaySinh.getValue();
                int hoatDong = 1; // mặc định hoạt động
                String matKhau = pfMatKhau.getText().trim();

                if (existingKH == null) {
                    insertKhachHangWithProc(hoTen, email, sdt, ngaySinh, hoatDong, matKhau);
                } else {
                    updateKhachHangWithProc(existingKH.maProperty().get(), hoTen, email, sdt, ngaySinh, hoatDong);
                }
            }
        });
    }

    private void insertKhachHangWithProc(String hoTen, String email, String sdt, LocalDate ngaySinh, int hoatDong, String matKhau) {
        final String sql = "{CALL proc_kh_insert(?, ?, ?, ?, ?, ?, ?)}";
        try (Connection c = DBConnection.getConnection(); CallableStatement cs = c.prepareCall(sql)) {
            cs.setString(1, hoTen);
            cs.setString(2, email);
            cs.setString(3, sdt);
            cs.setDate(4, ngaySinh == null ? null : Date.valueOf(ngaySinh));
            cs.setInt(5, hoatDong);
            cs.setString(6, (matKhau == null || matKhau.isEmpty()) ? "123456" : matKhau);
            cs.registerOutParameter(7, Types.BIGINT);
            cs.execute();
            long newMaKH = cs.getLong(7);
            info("✅ Đã thêm khách hàng! Mã KH: " + newMaKH);
            loadKhachHang();
        } catch (SQLException e) {
            showError("Lỗi thêm khách hàng", e.getMessage());
        }
    }

    private void updateKhachHangWithProc(int maKH, String hoTen, String email, String sdt, LocalDate ngaySinh, int hoatDong) {
        final String sql = "{CALL proc_kh_update(?, ?, ?, ?, ?, ?)}";
        try (Connection c = DBConnection.getConnection(); CallableStatement cs = c.prepareCall(sql)) {
            cs.setInt(1, maKH);
            cs.setString(2, hoTen);
            cs.setString(3, email);
            cs.setString(4, sdt);
            cs.setDate(5, ngaySinh == null ? null : Date.valueOf(ngaySinh));
            cs.setInt(6, hoatDong);
            cs.execute();
            info("✅ Đã cập nhật khách hàng!");
            loadKhachHang();
        } catch (SQLException e) {
            showError("Lỗi cập nhật khách hàng", e.getMessage());
        }
    }

    private void toggleKhachHangStatus(KhachHangVM kh) {
        // Vẫn cho phép bật/tắt bằng proc nếu bạn cần dùng (dù không hiển thị cột trạng thái)
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Bật/Tắt trạng thái");
        confirm.setHeaderText("Thay đổi trạng thái hoạt động");
        confirm.setContentText("Khách hàng: " + kh.hoTenProperty().get());
        confirm.showAndWait().ifPresent(res -> {
            if (res == ButtonType.OK) {
                final String sql = "{CALL proc_toggle_hoat_dong_by_ma_kh(?)}";
                try (Connection c = DBConnection.getConnection(); CallableStatement cs = c.prepareCall(sql)) {
                    cs.setInt(1, kh.maProperty().get());
                    cs.execute();
                    info("✅ Đã thay đổi trạng thái!");
                    loadKhachHang();
                } catch (SQLException e) {
                    showError("Lỗi thay đổi trạng thái", e.getMessage());
                }
            }
        });
    }

    private void deleteKhachHang(KhachHangVM kh) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xóa khách hàng");
        confirm.setHeaderText("Hành động không thể hoàn tác");
        confirm.setContentText("Xóa: " + kh.hoTenProperty().get() + " ?");
        confirm.showAndWait().ifPresent(res -> {
            if (res == ButtonType.OK) {
                final String sql = "DELETE FROM khach_hang WHERE ma_khach_hang=?";
                try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
                    ps.setInt(1, kh.maProperty().get());
                    if (ps.executeUpdate() > 0) {
                        info("✅ Đã xóa khách hàng!");
                        loadKhachHang();
                    }
                } catch (SQLException e) {
                    showError("Lỗi xóa khách hàng", e.getMessage());
                }
            }
        });
    }

    // ===== Helpers =====
    private void info(String msg) { new Alert(Alert.AlertType.INFORMATION, msg).showAndWait(); }
    private void showError(String header, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR, msg);
        a.setHeaderText(header);
        a.showAndWait();
    }

    // ===== View Model =====
    public static class KhachHangVM {
        private final IntegerProperty ma = new SimpleIntegerProperty();
        private final IntegerProperty maTaiKhoan = new SimpleIntegerProperty();
        private final StringProperty hoTen = new SimpleStringProperty();
        private final StringProperty email = new SimpleStringProperty();
        private final StringProperty sdt = new SimpleStringProperty();
        private final StringProperty hang = new SimpleStringProperty();
        private final IntegerProperty diem = new SimpleIntegerProperty();

        public KhachHangVM(int ma, int maTK, String hoTen, String sdt, String email, String hang, int diem) {
            this.ma.set(ma);
            this.maTaiKhoan.set(maTK);
            this.hoTen.set(hoTen);
            this.email.set(email);
            this.sdt.set(sdt);
            this.hang.set(hang);
            this.diem.set(diem);
        }

        public IntegerProperty maProperty(){ return ma; }
        public IntegerProperty maTaiKhoanProperty(){ return maTaiKhoan; }
        public StringProperty hoTenProperty(){ return hoTen; }
        public StringProperty emailProperty(){ return email; }
        public StringProperty sdtProperty(){ return sdt; }
        public StringProperty hangProperty(){ return hang; }
        public IntegerProperty diemProperty(){ return diem; }
    }
}
