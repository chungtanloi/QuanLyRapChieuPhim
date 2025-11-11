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

public class StaffController {

    // ====== FXML Components ======
    @FXML private TextField txtTimKiemNV;
    @FXML private Label totalEmployeesLabel;
    @FXML private Label activeEmployeesLabel;
    @FXML private Label onLeaveEmployeesLabel;
    @FXML private Label activeAccountsLabel;
    @FXML private TableView<EmployeeRow> employeeTable;
    @FXML private TableColumn<EmployeeRow, Number> colMaNhanVien;
    @FXML private TableColumn<EmployeeRow, String> colHoTen;
    @FXML private TableColumn<EmployeeRow, String> colEmail;
    @FXML private TableColumn<EmployeeRow, String> colSoDienThoai;
    @FXML private TableColumn<EmployeeRow, String> colNgayVaoLam;
    @FXML private TableColumn<EmployeeRow, String> colTrangThai;
    @FXML private TableColumn<EmployeeRow, Void> colThaoTac;

    // ====== Data ======
    private final ObservableList<EmployeeRow> data = FXCollections.observableArrayList();
    private final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // ====== Lifecycle ======
    @FXML
    public void initialize() {
        setupTableColumns();
        setupActionButtons();
        employeeTable.setItems(data);
        refreshData();
    }

    private void setupTableColumns() {
        colMaNhanVien.setCellValueFactory(c -> c.getValue().maNhanVienProperty());
        colHoTen.setCellValueFactory(c -> c.getValue().hoTenProperty());
        colEmail.setCellValueFactory(c -> c.getValue().emailProperty());
        colSoDienThoai.setCellValueFactory(c -> c.getValue().soDienThoaiProperty());
        colNgayVaoLam.setCellValueFactory(c -> c.getValue().ngayVaoLamProperty());
        colTrangThai.setCellValueFactory(c -> c.getValue().trangThaiProperty());
    }

    // ====== Event Handlers ======
    @FXML
    public void handleSearchEmployee() {
        String keyword = txtTimKiemNV.getText();
        loadEmployees(keyword == null || keyword.isBlank() ? null : keyword.trim());
    }

    @FXML
    public void handleAddEmployee() {
        showEmployeeDialog(null);
    }

    // ====== Database Operations ======
    private void refreshData() {
        loadEmployees(null);
        updateStatistics();
    }

    private void loadEmployees(String keyword) {
        data.clear();
        
        StringBuilder sql = new StringBuilder(
            "SELECT nv.ma_nhan_vien, nv.ngay_vao_lam, " +
            "tk.ho_ten, tk.email, tk.so_dien_thoai, tk.hoat_dong " +
            "FROM nhan_vien nv " +
            "JOIN tai_khoan tk ON tk.ma_tai_khoan = nv.ma_tai_khoan "
        );

        if (keyword != null) {
            sql.append("WHERE nv.ma_nhan_vien LIKE ? OR tk.ho_ten LIKE ? OR tk.email LIKE ? OR tk.so_dien_thoai LIKE ? ");
        }
        
        sql.append("ORDER BY nv.ma_nhan_vien DESC");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            if (keyword != null) {
                String pattern = "%" + keyword + "%";
                for (int i = 1; i <= 4; i++) {
                    ps.setString(i, pattern);
                }
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    long maNV = rs.getLong("ma_nhan_vien");
                    String hoTen = rs.getString("ho_ten");
                    String email = rs.getString("email");
                    String sdt = rs.getString("so_dien_thoai");
                    Date ngayVao = rs.getDate("ngay_vao_lam");
                    int hoatDong = rs.getInt("hoat_dong");

                    String ngayStr = (ngayVao == null) ? "" : ngayVao.toLocalDate().format(DATE_FMT);
                    String trangThai = (hoatDong == 1) ? "Hoạt động" : "Ngừng";

                    data.add(new EmployeeRow(maNV, hoTen, email, sdt, ngayStr, trangThai));
                }
            }
        } catch (SQLException e) {
            showError("Không thể tải danh sách nhân viên", e);
        }
    }

    private void updateStatistics() {
        String sql = 
            "SELECT COUNT(*) AS total, " +
            "SUM(tk.hoat_dong = 1) AS active, " +
            "SUM(tk.hoat_dong = 0) AS inactive " +
            "FROM nhan_vien nv " +
            "JOIN tai_khoan tk ON tk.ma_tai_khoan = nv.ma_tai_khoan";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                totalEmployeesLabel.setText(String.valueOf(rs.getLong("total")));
                activeEmployeesLabel.setText(String.valueOf(rs.getLong("active")));
                onLeaveEmployeesLabel.setText(String.valueOf(rs.getLong("inactive")));
                activeAccountsLabel.setText(String.valueOf(rs.getLong("active")));
            }
        } catch (SQLException e) {
            showError("Không thể cập nhật thống kê", e);
        }
    }

    // ====== CRUD Operations ======
   private Long insertEmployee(String hoTen, String email, String sdt, Date ngayVao, int hoatDong, String matKhau) {
    String sql = "{CALL proc_nv_insert(?, ?, ?, ?, ?, ?, ?)}";
    try (Connection conn = DBConnection.getConnection();
         CallableStatement cs = conn.prepareCall(sql)) {

        cs.setString(1, hoTen);
        cs.setString(2, email);
        cs.setString(3, sdt);
        cs.setDate(4, ngayVao);
        cs.setInt(5, hoatDong);
        cs.setString(6, matKhau);
        cs.registerOutParameter(7, Types.BIGINT);

        cs.execute();
        return cs.getLong(7);

    } catch (SQLException e) {
        showError("Không thể thêm nhân viên", e);
        return null;
    }
}


    private boolean updateEmployee(long maNhanVien, String hoTen, String email, String sdt, Date ngayVao, int hoatDong) {
        String sql = 
            "UPDATE tai_khoan tk " +
            "JOIN nhan_vien nv ON nv.ma_tai_khoan = tk.ma_tai_khoan " +
            "SET tk.ho_ten = ?, tk.email = ?, tk.so_dien_thoai = ?, tk.hoat_dong = ?, " +
            "nv.ngay_vao_lam = ?, tk.cap_nhat_luc = NOW(), nv.cap_nhat_luc = NOW() " +
            "WHERE nv.ma_nhan_vien = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, hoTen);
            ps.setString(2, email);
            ps.setString(3, sdt);
            ps.setInt(4, hoatDong);
            ps.setDate(5, ngayVao);
            ps.setLong(6, maNhanVien);
            
            return ps.executeUpdate() > 0;
            
        } catch (SQLException e) {
            showError("Không thể cập nhật nhân viên", e);
            return false;
        }
    }

    private boolean changePassword(long maNhanVien, String newPassword) {
        String sql = 
            "UPDATE tai_khoan tk " +
            "JOIN nhan_vien nv ON nv.ma_tai_khoan = tk.ma_tai_khoan " +
            "SET tk.mat_khau_ma = ?, tk.cap_nhat_luc = NOW() " +
            "WHERE nv.ma_nhan_vien = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, newPassword);
            ps.setLong(2, maNhanVien);
            
            int rows = ps.executeUpdate();
            if (rows == 0) {
                showError("Không tìm thấy nhân viên", null);
                return false;
            }
            return true;
            
        } catch (SQLException e) {
            showError("Không thể đổi mật khẩu", e);
            return false;
        }
    }

    private void toggleActive(long maNhanVien) {
        String sql = 
            "UPDATE tai_khoan tk " +
            "JOIN nhan_vien nv ON nv.ma_tai_khoan = tk.ma_tai_khoan " +
            "SET tk.hoat_dong = 1 - tk.hoat_dong, tk.cap_nhat_luc = NOW() " +
            "WHERE nv.ma_nhan_vien = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setLong(1, maNhanVien);
            ps.executeUpdate();
            refreshData();
            
        } catch (SQLException e) {
            showError("Không thể thay đổi trạng thái", e);
        }
    }

    private void deleteEmployee(long maNhanVien) {
        String sql = "DELETE FROM nhan_vien WHERE ma_nhan_vien = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setLong(1, maNhanVien);
            ps.executeUpdate();
            refreshData();
            showInfo("Đã xóa nhân viên");
            
        } catch (SQLException e) {
            showError("Không thể xóa nhân viên (có ràng buộc dữ liệu)", e);
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
                    EmployeeRow row = getTableRow().getItem();
                    if (row != null) showEmployeeDialog(row);
                });

                btnToggle.setOnAction(e -> {
                    EmployeeRow row = getTableRow().getItem();
                    if (row != null) toggleActive(row.getMaNhanVien());
                });

                btnDelete.setOnAction(e -> {
                    EmployeeRow row = getTableRow().getItem();
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
            "-fx-background-color:%s;-fx-text-fill:white;-fx-cursor:hand;-fx-padding:5 10;",
            color
        ));
        return btn;
    }

   private void showEmployeeDialog(EmployeeRow current) {
    boolean isEdit = current != null;

    Dialog<EmployeeRow> dialog = new Dialog<>();
    dialog.setTitle(isEdit ? "Chỉnh sửa Nhân viên" : "Thêm Nhân viên");

    ButtonType saveButton = new ButtonType(isEdit ? "Lưu" : "Thêm", ButtonBar.ButtonData.OK_DONE);
    dialog.getDialogPane().getButtonTypes().addAll(saveButton, ButtonType.CANCEL);

    GridPane grid = new GridPane();
    grid.setHgap(10);
    grid.setVgap(10);
    grid.setPadding(new Insets(20, 150, 10, 10));

    TextField txtHoTen = new TextField();
    txtHoTen.setPromptText("Họ tên");
    TextField txtEmail = new TextField();
    txtEmail.setPromptText("Email");
    TextField txtSDT = new TextField();
    txtSDT.setPromptText("Số điện thoại");
    DatePicker dpNgayVao = new DatePicker();
    dpNgayVao.setPromptText("Ngày vào làm");
    CheckBox chkHoatDong = new CheckBox("Đang hoạt động");
    chkHoatDong.setSelected(true);
    PasswordField pfPassword = new PasswordField();
    pfPassword.setPromptText(isEdit ? "Mật khẩu mới (để trống nếu không đổi)" : "Mật khẩu");

    if (isEdit) {
        txtHoTen.setText(current.getHoTen());
        txtEmail.setText(current.getEmail());
        txtSDT.setText(current.getSoDienThoai());
        if (current.getNgayVaoLam() != null && !current.getNgayVaoLam().isBlank()) {
            dpNgayVao.setValue(LocalDate.parse(current.getNgayVaoLam(), DATE_FMT));
        }
        chkHoatDong.setSelected("Hoạt động".equals(current.getTrangThai()));
    }

    int row = 0;
    grid.addRow(row++, new Label("Họ tên:"), txtHoTen);
    grid.addRow(row++, new Label("Email:"), txtEmail);
    grid.addRow(row++, new Label("SĐT:"), txtSDT);
    grid.addRow(row++, new Label("Ngày vào làm:"), dpNgayVao);
    grid.addRow(row++, new Label("Hoạt động:"), chkHoatDong);
    grid.addRow(row++, new Label("Mật khẩu:"), pfPassword);

    dialog.getDialogPane().setContent(grid);

    Node saveBtn = dialog.getDialogPane().lookupButton(saveButton);
    Runnable validate = () -> {
        boolean valid = !txtHoTen.getText().trim().isEmpty()
                && !txtEmail.getText().trim().isEmpty()
                && !txtSDT.getText().trim().isEmpty()
                && (isEdit || !pfPassword.getText().trim().isEmpty());
        saveBtn.setDisable(!valid);
    };
    txtHoTen.textProperty().addListener((o, a, b) -> validate.run());
    txtEmail.textProperty().addListener((o, a, b) -> validate.run());
    txtSDT.textProperty().addListener((o, a, b) -> validate.run());
    if (!isEdit) pfPassword.textProperty().addListener((o, a, b) -> validate.run());
    validate.run();

    dialog.setResultConverter(button -> {
        if (button != saveButton) return null;

        String hoTen = txtHoTen.getText().trim();
        String email = txtEmail.getText().trim();
        String sdt = txtSDT.getText().trim();
        LocalDate ngay = dpNgayVao.getValue();
        int hoatDong = chkHoatDong.isSelected() ? 1 : 0;
        Date sqlDate = (ngay == null) ? null : Date.valueOf(ngay);
        String ngayStr = (ngay == null) ? "" : ngay.format(DATE_FMT);

        if (isEdit) {
            if (!updateEmployee(current.getMaNhanVien(), hoTen, email, sdt, sqlDate, hoatDong)) {
                return null;
            }
            String newPw = pfPassword.getText().trim();
            if (!newPw.isEmpty() && !changePassword(current.getMaNhanVien(), newPw)) {
                return null;
            }
            return new EmployeeRow(current.getMaNhanVien(), hoTen, email, sdt, ngayStr,
                    hoatDong == 1 ? "Hoạt động" : "Ngừng");
        } else {
            String pw = pfPassword.getText().trim();
            Long newId = insertEmployee(hoTen, email, sdt, sqlDate, hoatDong, pw);
            if (newId == null) return null;
            return new EmployeeRow(newId, hoTen, email, sdt, ngayStr,
                    hoatDong == 1 ? "Hoạt động" : "Ngừng");
        }
    });

    // --- Quan trọng: phần này rất “sạch” để không bị hiểu lầm do ngoặc ---
    Optional<EmployeeRow> dlgResult = dialog.showAndWait();
    if (dlgResult.isPresent()) {
        refreshData();
        showInfo(isEdit ? "Đã lưu thay đổi" : "Đã thêm nhân viên mới");
    }
}


    

    private void confirmDelete(EmployeeRow row) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xác nhận xóa");
        alert.setHeaderText("Xóa nhân viên");
        alert.setContentText(String.format(
            "Bạn có chắc muốn xóa nhân viên %s (Mã: %d)?",
            row.getHoTen(), row.getMaNhanVien()
        ));

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                deleteEmployee(row.getMaNhanVien());
            }
        });
    }

    // ====== Utility Methods ======
    private void showError(String message, Exception e) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Lỗi");
        alert.setHeaderText(message);
        if (e != null) {
            alert.setContentText(e.getMessage());
        }
        alert.showAndWait();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Thông báo");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // ====== Data Model ======
    public static class EmployeeRow {
        private final SimpleLongProperty maNhanVien = new SimpleLongProperty();
        private final SimpleStringProperty hoTen = new SimpleStringProperty();
        private final SimpleStringProperty email = new SimpleStringProperty();
        private final SimpleStringProperty soDienThoai = new SimpleStringProperty();
        private final SimpleStringProperty ngayVaoLam = new SimpleStringProperty();
        private final SimpleStringProperty trangThai = new SimpleStringProperty();

        public EmployeeRow(long maNV, String hoTen, String email, String sdt, String ngay, String trangThai) {
            this.maNhanVien.set(maNV);
            this.hoTen.set(hoTen);
            this.email.set(email);
            this.soDienThoai.set(sdt);
            this.ngayVaoLam.set(ngay);
            this.trangThai.set(trangThai);
        }

        public long getMaNhanVien() { return maNhanVien.get(); }
        public SimpleLongProperty maNhanVienProperty() { return maNhanVien; }

        public String getHoTen() { return hoTen.get(); }
        public SimpleStringProperty hoTenProperty() { return hoTen; }

        public String getEmail() { return email.get(); }
        public SimpleStringProperty emailProperty() { return email; }

        public String getSoDienThoai() { return soDienThoai.get(); }
        public SimpleStringProperty soDienThoaiProperty() { return soDienThoai; }

        public String getNgayVaoLam() { return ngayVaoLam.get(); }
        public SimpleStringProperty ngayVaoLamProperty() { return ngayVaoLam; }

        public String getTrangThai() { return trangThai.get(); }
        public SimpleStringProperty trangThaiProperty() { return trangThai; }
    }
}