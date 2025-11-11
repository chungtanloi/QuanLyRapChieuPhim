package controllers;

import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import database.DBConnection;

public class Customer_mnController {

    // ====== FXML Components ======
    @FXML private TextField txtTimKiemKH;
    @FXML private Label totalCustomersLabel;
    @FXML private Label activeCustomersLabel;
    @FXML private Label inactiveCustomersLabel;
    @FXML private Label activeAccountsLabel;
    @FXML private TableView<CustomerRow> customerTable;
    @FXML private TableColumn<CustomerRow, Number> colMaKhachHang;
    @FXML private TableColumn<CustomerRow, String> colHoTen;
    @FXML private TableColumn<CustomerRow, String> colEmail;
    @FXML private TableColumn<CustomerRow, String> colSoDienThoai;
    @FXML private TableColumn<CustomerRow, String> colNgaySinh;
    @FXML private TableColumn<CustomerRow, String> colDiemTichLuy;
    @FXML private TableColumn<CustomerRow, String> colTrangThai;
    @FXML private TableColumn<CustomerRow, Void> colThaoTac;

    // ====== Data ======
    private final ObservableList<CustomerRow> data = FXCollections.observableArrayList();
    private final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // ====== Lifecycle ======
    @FXML
    public void initialize() {
        setupTableColumns();
        setupActionButtons();
        customerTable.setItems(data);
        refreshData();
    }

    private void setupTableColumns() {
        colMaKhachHang.setCellValueFactory(c -> c.getValue().maKhachHangProperty());
        colHoTen.setCellValueFactory(c -> c.getValue().hoTenProperty());
        colEmail.setCellValueFactory(c -> c.getValue().emailProperty());
        colSoDienThoai.setCellValueFactory(c -> c.getValue().soDienThoaiProperty());
        colNgaySinh.setCellValueFactory(c -> c.getValue().ngaySinhProperty());
        colDiemTichLuy.setCellValueFactory(c -> c.getValue().diemTichLuyProperty());
        colTrangThai.setCellValueFactory(c -> c.getValue().trangThaiProperty());
    }

    // ====== Event Handlers ======
    @FXML
    public void handleSearchCustomer() {
        String keyword = txtTimKiemKH.getText();
        loadCustomers(keyword == null || keyword.isBlank() ? null : keyword.trim());
    }

    @FXML
    public void handleAddCustomer() {
        showCustomerDialog(null);
    }

    @FXML
    public void refreshData() {
        loadCustomers(null);
        updateStatistics();
    }

    // ====== Database Operations ======
    private void loadCustomers(String keyword) {
        data.clear();
        StringBuilder sql = new StringBuilder(
            "SELECT kh.ma_khach_hang, kh.ngay_sinh, kh.diem_tich_luy, " +
            "tk.ho_ten, tk.email, tk.so_dien_thoai, tk.hoat_dong " +
            "FROM khach_hang kh " +
            "JOIN tai_khoan tk ON tk.ma_tai_khoan = kh.ma_tai_khoan "
        );

        if (keyword != null) {
            sql.append("WHERE kh.ma_khach_hang LIKE ? OR tk.ho_ten LIKE ? OR tk.email LIKE ? OR tk.so_dien_thoai LIKE ? ");
        }
        sql.append("ORDER BY kh.ma_khach_hang DESC");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            if (keyword != null) {
                String pattern = "%" + keyword + "%";
                for (int i = 1; i <= 4; i++) ps.setString(i, pattern);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    long maKH = rs.getLong("ma_khach_hang");
                    String hoTen = rs.getString("ho_ten");
                    String email = rs.getString("email");
                    String sdt = rs.getString("so_dien_thoai");
                    Date ngaySinh = rs.getDate("ngay_sinh");
                    Integer diem = rs.getInt("diem_tich_luy");
                    int hoatDong = rs.getInt("hoat_dong");

                    String ngayStr = (ngaySinh == null) ? "" : ngaySinh.toLocalDate().format(DATE_FMT);
                    String trangThai = (hoatDong == 1) ? "Hoạt động" : "Ngừng";

                    data.add(new CustomerRow(maKH, hoTen, email, sdt, ngayStr,
                            diem.toString(), trangThai));
                }
            }
        } catch (SQLException e) {
            showError("Không thể tải danh sách khách hàng", e);
        }
    }

    private void updateStatistics() {
        String sql =
            "SELECT COUNT(*) AS total, " +
            "SUM(tk.hoat_dong = 1) AS active, " +
            "SUM(tk.hoat_dong = 0) AS inactive " +
            "FROM khach_hang kh " +
            "JOIN tai_khoan tk ON tk.ma_tai_khoan = kh.ma_tai_khoan";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                totalCustomersLabel.setText(String.valueOf(rs.getLong("total")));
                activeCustomersLabel.setText(String.valueOf(rs.getLong("active")));
                inactiveCustomersLabel.setText(String.valueOf(rs.getLong("inactive")));
                activeAccountsLabel.setText(String.valueOf(rs.getLong("active")));
            }
        } catch (SQLException e) {
            showError("Không thể cập nhật thống kê", e);
        }
    }

    // ====== CRUD Operations ======
    private Long insertCustomer(String hoTen, String email, String sdt,
                                Date ngaySinh, int hoatDong, String matKhau) {
        String sql = "{CALL proc_kh_insert(?, ?, ?, ?, ?, ?, ?)}";
        try (Connection conn = DBConnection.getConnection();
             CallableStatement cs = conn.prepareCall(sql)) {

            cs.setString(1, hoTen);
            cs.setString(2, email);
            cs.setString(3, sdt);
            cs.setDate(4, ngaySinh);
            cs.setInt(5, hoatDong);
            cs.setString(6, matKhau);
            cs.registerOutParameter(7, Types.BIGINT);
            cs.execute();
            return cs.getLong(7);

        } catch (SQLException e) {
            showError("Không thể thêm khách hàng", e);
            return null;
        }
    }

    private boolean updateCustomer(long maKH, String hoTen, String email,
                                   String sdt, Date ngaySinh, int hoatDong) {
        String sql =
            "UPDATE tai_khoan tk " +
            "JOIN khach_hang kh ON kh.ma_tai_khoan = tk.ma_tai_khoan " +
            "SET tk.ho_ten=?, tk.email=?, tk.so_dien_thoai=?, tk.hoat_dong=?, kh.ngay_sinh=? " +
            "WHERE kh.ma_khach_hang=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, hoTen);
            ps.setString(2, email);
            ps.setString(3, sdt);
            ps.setInt(4, hoatDong);
            ps.setDate(5, ngaySinh);
            ps.setLong(6, maKH);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            showError("Không thể cập nhật khách hàng", e);
            return false;
        }
    }

    private void toggleActive(long maKH) {
        String sql =
            "UPDATE tai_khoan tk " +
            "JOIN khach_hang kh ON kh.ma_tai_khoan = tk.ma_tai_khoan " +
            "SET tk.hoat_dong = 1 - tk.hoat_dong WHERE kh.ma_khach_hang=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, maKH);
            ps.executeUpdate();
            refreshData();
        } catch (SQLException e) {
            showError("Không thể thay đổi trạng thái khách hàng", e);
        }
    }

    private void deleteCustomer(long maKH) {
        String sql = "DELETE FROM khach_hang WHERE ma_khach_hang=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, maKH);
            ps.executeUpdate();
            refreshData();
            showInfo("Đã xóa khách hàng!");
        } catch (SQLException e) {
            showError("Không thể xóa khách hàng (ràng buộc dữ liệu)", e);
        }
    }

    // ====== UI Components ======
    private void setupActionButtons() {
        colThaoTac.setCellFactory(col -> new TableCell<>() {
            private final Button btnEdit = createButton("Sửa", "#2563eb");
            private final Button btnToggle = createButton("Bật/Tắt", "#6b7280");
            private final Button btnDelete = createButton("Xóa", "#ef4444");
            private final HBox box = new HBox(8, btnEdit, btnToggle, btnDelete);

            {
                box.setAlignment(Pos.CENTER);

                btnEdit.setOnAction(e -> {
                    CustomerRow row = getTableRow().getItem();
                    if (row != null) showCustomerDialog(row);
                });

                btnToggle.setOnAction(e -> {
                    CustomerRow row = getTableRow().getItem();
                    if (row != null) toggleActive(row.getMaKhachHang());
                });

                btnDelete.setOnAction(e -> {
                    CustomerRow row = getTableRow().getItem();
                    if (row != null) confirmDelete(row);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
    }

    private Button createButton(String text, String color) {
        Button btn = new Button(text);
        btn.setStyle(String.format(
            "-fx-background-color:%s;-fx-text-fill:white;-fx-cursor:hand;-fx-padding:4 8;-fx-background-radius:6;",
            color
        ));
        return btn;
    }

    // ====== Dialogs & Alerts ======
    private void showCustomerDialog(CustomerRow current) {
        boolean isEdit = current != null;

        Dialog<CustomerRow> dialog = new Dialog<>();
        dialog.setTitle(isEdit ? "Chỉnh sửa khách hàng" : "Thêm khách hàng");

        ButtonType saveButton = new ButtonType(isEdit ? "Lưu" : "Thêm", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButton, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField txtHoTen = new TextField();
        txtHoTen.setPromptText("Họ tên");
        TextField txtEmail = new TextField();
        txtEmail.setPromptText("Email");
        TextField txtSDT = new TextField();
        txtSDT.setPromptText("Số điện thoại");
        DatePicker dpNgaySinh = new DatePicker();
        dpNgaySinh.setPromptText("Ngày sinh");
        CheckBox chkHoatDong = new CheckBox("Hoạt động");
        PasswordField pfPassword = new PasswordField();
        pfPassword.setPromptText(isEdit ? "Mật khẩu mới (để trống nếu không đổi)" : "Mật khẩu");

        if (isEdit) {
            txtHoTen.setText(current.getHoTen());
            txtEmail.setText(current.getEmail());
            txtSDT.setText(current.getSoDienThoai());
            if (current.getNgaySinh() != null && !current.getNgaySinh().isBlank())
                dpNgaySinh.setValue(LocalDate.parse(current.getNgaySinh(), DATE_FMT));
            chkHoatDong.setSelected("Hoạt động".equals(current.getTrangThai()));
        }

        grid.addRow(0, new Label("Họ tên:"), txtHoTen);
        grid.addRow(1, new Label("Email:"), txtEmail);
        grid.addRow(2, new Label("SĐT:"), txtSDT);
        grid.addRow(3, new Label("Ngày sinh:"), dpNgaySinh);
        grid.addRow(4, new Label("Trạng thái:"), chkHoatDong);
        grid.addRow(5, new Label("Mật khẩu:"), pfPassword);

        dialog.getDialogPane().setContent(grid);

        Node saveBtn = dialog.getDialogPane().lookupButton(saveButton);
        Runnable validate = () -> {
            boolean valid = !txtHoTen.getText().isBlank()
                    && !txtEmail.getText().isBlank()
                    && !txtSDT.getText().isBlank()
                    && (isEdit || !pfPassword.getText().isBlank());
            saveBtn.setDisable(!valid);
        };
        txtHoTen.textProperty().addListener((o, a, b) -> validate.run());
        txtEmail.textProperty().addListener((o, a, b) -> validate.run());
        txtSDT.textProperty().addListener((o, a, b) -> validate.run());
        pfPassword.textProperty().addListener((o, a, b) -> validate.run());
        validate.run();

        dialog.setResultConverter(button -> {
            if (button != saveButton) return null;
            String hoTen = txtHoTen.getText().trim();
            String email = txtEmail.getText().trim();
            String sdt = txtSDT.getText().trim();
            LocalDate ngay = dpNgaySinh.getValue();
            int hoatDong = chkHoatDong.isSelected() ? 1 : 0;
            Date sqlDate = (ngay == null) ? null : Date.valueOf(ngay);
            String ngayStr = (ngay == null) ? "" : ngay.format(DATE_FMT);

            if (isEdit) {
                if (!updateCustomer(current.getMaKhachHang(), hoTen, email, sdt, sqlDate, hoatDong)) return null;
                return new CustomerRow(current.getMaKhachHang(), hoTen, email, sdt, ngayStr, current.getDiemTichLuy(),
                        hoatDong == 1 ? "Hoạt động" : "Ngừng");
            } else {
                String pw = pfPassword.getText().trim();
                Long newId = insertCustomer(hoTen, email, sdt, sqlDate, hoatDong, pw);
                if (newId == null) return null;
                return new CustomerRow(newId, hoTen, email, sdt, ngayStr, "0",
                        hoatDong == 1 ? "Hoạt động" : "Ngừng");
            }
        });

        Optional<CustomerRow> dlgResult = dialog.showAndWait();
        if (dlgResult.isPresent()) {
            refreshData();
            showInfo(isEdit ? "Đã lưu thay đổi" : "Đã thêm khách hàng mới");
        }
    }

    private void confirmDelete(CustomerRow row) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xóa khách hàng");
        alert.setHeaderText("Bạn có chắc muốn xóa khách hàng " + row.getHoTen() + "?");
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) deleteCustomer(row.getMaKhachHang());
        });
    }

    // ====== Alert helpers ======
    private void showError(String message, Exception e) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Lỗi"); a.setHeaderText(message);
        if (e != null) a.setContentText(e.getMessage());
        a.showAndWait();
    }

    private void showInfo(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Thông báo");
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    // ====== Model ======
    public static class CustomerRow {
        private final SimpleLongProperty maKhachHang = new SimpleLongProperty();
        private final SimpleStringProperty hoTen = new SimpleStringProperty();
        private final SimpleStringProperty email = new SimpleStringProperty();
        private final SimpleStringProperty soDienThoai = new SimpleStringProperty();
        private final SimpleStringProperty ngaySinh = new SimpleStringProperty();
        private final SimpleStringProperty diemTichLuy = new SimpleStringProperty();
        private final SimpleStringProperty trangThai = new SimpleStringProperty();

        public CustomerRow(long ma, String ten, String email, String sdt,
                           String ngay, String diem, String trangThai) {
            this.maKhachHang.set(ma);
            this.hoTen.set(ten);
            this.email.set(email);
            this.soDienThoai.set(sdt);
            this.ngaySinh.set(ngay);
            this.diemTichLuy.set(diem);
            this.trangThai.set(trangThai);
        }

        public long getMaKhachHang() { return maKhachHang.get(); }
        public String getHoTen() { return hoTen.get(); }
        public String getEmail() { return email.get(); }
        public String getSoDienThoai() { return soDienThoai.get(); }
        public String getNgaySinh() { return ngaySinh.get(); }
        public String getDiemTichLuy() { return diemTichLuy.get(); }
        public String getTrangThai() { return trangThai.get(); }

        public SimpleLongProperty maKhachHangProperty() { return maKhachHang; }
        public SimpleStringProperty hoTenProperty() { return hoTen; }
        public SimpleStringProperty emailProperty() { return email; }
        public SimpleStringProperty soDienThoaiProperty() { return soDienThoai; }
        public SimpleStringProperty ngaySinhProperty() { return ngaySinh; }
        public SimpleStringProperty diemTichLuyProperty() { return diemTichLuy; }
        public SimpleStringProperty trangThaiProperty() { return trangThai; }
    }
}
