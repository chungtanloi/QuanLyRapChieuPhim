package com.cinema.controller;

import database.DBConnection;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

import java.sql.*;
import java.util.Optional;

public class CinemaController {

    @FXML private TableView<Cinema> theaterTable;
    @FXML private TableColumn<Cinema, String> nameColumn;
    @FXML private TableColumn<Cinema, String> seatsColumn;
    @FXML private TableColumn<Cinema, String> screenTypeColumn;
    @FXML private TableColumn<Cinema, String> statusColumn;
    @FXML private TableColumn<Cinema, String> actionsColumn;
    
    @FXML private Label totalTheatersLabel;
    @FXML private Label activeTheatersLabel;
    @FXML private Label maintenanceTheatersLabel;
    @FXML private Label totalSeatsLabel;
    
    @FXML private Button addButton;

    private ObservableList<Cinema> cinemaList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTableColumns();
        loadCinemaData();
        updateStatistics();
        
        // Gán sự kiện cho nút thêm
        if (addButton != null) {
            addButton.setOnAction(e -> handleAddCinema());
        }
    }

    private void setupTableColumns() {
        nameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTenPhong()));
        seatsColumn.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getSucChua())));
        
        // Hiển thị "Không xác định" cho loại màn hình vì DB không có field này
        screenTypeColumn.setCellValueFactory(data -> new SimpleStringProperty("Chuẩn"));
        
        // Chuyển đổi trạng thái từ ENUM sang tiếng Việt
        statusColumn.setCellValueFactory(data -> {
            String status = data.getValue().getTrangThai();
            String displayStatus = switch (status) {
                case "HOAT_DONG" -> "Đang hoạt động";
                case "BAO_TRI" -> "Đang bảo trì";
                case "NGUNG" -> "Ngừng hoạt động";
                default -> status;
            };
            return new SimpleStringProperty(displayStatus);
        });
        
        // Cột thao tác với nút Sửa và Xóa
        actionsColumn.setCellFactory(col -> new TableCell<Cinema, String>() {
            private final Button editBtn = new Button("Sửa");
            private final Button deleteBtn = new Button("Xóa");
            private final HBox container = new HBox(10, editBtn, deleteBtn);

            {
                container.setAlignment(Pos.CENTER);
                editBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-cursor: hand; -fx-padding: 5 15;");
                deleteBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-cursor: hand; -fx-padding: 5 15;");
                
                editBtn.setOnAction(e -> {
                    Cinema cinema = getTableView().getItems().get(getIndex());
                    handleEdit(cinema);
                });
                
                deleteBtn.setOnAction(e -> {
                    Cinema cinema = getTableView().getItems().get(getIndex());
                    handleDelete(cinema);
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : container);
            }
        });
    }

    private void loadCinemaData() {
        cinemaList.clear();
        String query = "SELECT * FROM phong ORDER BY ma_phong";
        
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Cinema cinema = new Cinema(
                    rs.getLong("ma_phong"),
                    rs.getString("ten_phong"),
                    rs.getInt("suc_chua"),
                    rs.getString("trang_thai")
                );
                cinemaList.add(cinema);
            }
            theaterTable.setItems(cinemaList);
            
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tải dữ liệu phòng chiếu: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateStatistics() {
        try (Connection conn = DBConnection.getConnection()) {
            // Tổng số phòng
            String totalQuery = "SELECT COUNT(*) as total FROM phong";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(totalQuery)) {
                if (rs.next()) {
                    totalTheatersLabel.setText(String.valueOf(rs.getInt("total")));
                }
            }
            
            // Đang hoạt động
            String activeQuery = "SELECT COUNT(*) as active FROM phong WHERE trang_thai = 'HOAT_DONG'";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(activeQuery)) {
                if (rs.next()) {
                    activeTheatersLabel.setText(String.valueOf(rs.getInt("active")));
                }
            }
            
            // Đang bảo trì
            String maintenanceQuery = "SELECT COUNT(*) as maintenance FROM phong WHERE trang_thai = 'BAO_TRI'";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(maintenanceQuery)) {
                if (rs.next()) {
                    maintenanceTheatersLabel.setText(String.valueOf(rs.getInt("maintenance")));
                }
            }
            
            // Tổng ghế ngồi
            String seatsQuery = "SELECT SUM(suc_chua) as total_seats FROM phong";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(seatsQuery)) {
                if (rs.next()) {
                    int totalSeats = rs.getInt("total_seats");
                    totalSeatsLabel.setText(String.valueOf(totalSeats));
                }
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAddCinema() {
        Dialog<Cinema> dialog = new Dialog<>();
        dialog.setTitle("Thêm Phòng Chiếu Mới");
        dialog.setHeaderText("Nhập thông tin phòng chiếu");

        ButtonType addButtonType = new ButtonType("Thêm", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        GridPane grid = createCinemaForm(null);
        dialog.getDialogPane().setContent(grid);

        // Validation
        Button addBtn = (Button) dialog.getDialogPane().lookupButton(addButtonType);
        TextField nameField = (TextField) grid.getChildren().get(1);
        TextField seatsField = (TextField) grid.getChildren().get(3);
        
        addBtn.setDisable(true);
        nameField.textProperty().addListener((obs, oldVal, newVal) -> 
            addBtn.setDisable(newVal.trim().isEmpty() || seatsField.getText().trim().isEmpty()));
        seatsField.textProperty().addListener((obs, oldVal, newVal) -> 
            addBtn.setDisable(nameField.getText().trim().isEmpty() || newVal.trim().isEmpty()));

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                try {
                    return getCinemaFromForm(grid);
                } catch (NumberFormatException e) {
                    showAlert(Alert.AlertType.ERROR, "Lỗi", "Số ghế phải là số nguyên hợp lệ!");
                    return null;
                }
            }
            return null;
        });

        Optional<Cinema> result = dialog.showAndWait();
        result.ifPresent(cinema -> {
            if (cinema != null && insertCinema(cinema)) {
                loadCinemaData();
                updateStatistics();
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã thêm phòng chiếu mới!");
            }
        });
    }

    private void handleEdit(Cinema cinema) {
        Dialog<Cinema> dialog = new Dialog<>();
        dialog.setTitle("Sửa Thông Tin Phòng Chiếu");
        dialog.setHeaderText("Chỉnh sửa thông tin: " + cinema.getTenPhong());

        ButtonType saveButtonType = new ButtonType("Lưu", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = createCinemaForm(cinema);
        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    Cinema updated = getCinemaFromForm(grid);
                    updated.setMaPhong(cinema.getMaPhong());
                    return updated;
                } catch (NumberFormatException e) {
                    showAlert(Alert.AlertType.ERROR, "Lỗi", "Số ghế phải là số nguyên hợp lệ!");
                    return null;
                }
            }
            return null;
        });

        Optional<Cinema> result = dialog.showAndWait();
        result.ifPresent(updated -> {
            if (updated != null && updateCinema(updated)) {
                loadCinemaData();
                updateStatistics();
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã cập nhật thông tin phòng chiếu!");
            }
        });
    }

    private void handleDelete(Cinema cinema) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận xóa");
        confirm.setHeaderText("Bạn có chắc muốn xóa phòng chiếu này?");
        confirm.setContentText("Phòng: " + cinema.getTenPhong() + "\nLưu ý: Các suất chiếu và ghế liên quan sẽ bị xóa!");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (deleteCinema(cinema.getMaPhong())) {
                loadCinemaData();
                updateStatistics();
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã xóa phòng chiếu!");
            }
        }
    }

    private GridPane createCinemaForm(Cinema cinema) {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nameField = new TextField();
        nameField.setPromptText("Tên phòng (VD: Phòng 1, Phòng VIP)");
        if (cinema != null) nameField.setText(cinema.getTenPhong());

        TextField seatsField = new TextField();
        seatsField.setPromptText("Số ghế (VD: 100)");
        if (cinema != null) seatsField.setText(String.valueOf(cinema.getSucChua()));

        ComboBox<String> statusCombo = new ComboBox<>();
        statusCombo.getItems().addAll("Đang hoạt động", "Đang bảo trì", "Ngừng hoạt động");
        
        if (cinema != null) {
            String displayStatus = switch (cinema.getTrangThai()) {
                case "HOAT_DONG" -> "Đang hoạt động";
                case "BAO_TRI" -> "Đang bảo trì";
                case "NGUNG" -> "Ngừng hoạt động";
                default -> "Đang hoạt động";
            };
            statusCombo.setValue(displayStatus);
        } else {
            statusCombo.setValue("Đang hoạt động");
        }

        grid.add(new Label("Tên phòng: *"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Sức chứa: *"), 0, 1);
        grid.add(seatsField, 1, 1);
        grid.add(new Label("Trạng thái:"), 0, 2);
        grid.add(statusCombo, 1, 2);

        return grid;
    }

    private Cinema getCinemaFromForm(GridPane grid) throws NumberFormatException {
        TextField nameField = (TextField) grid.getChildren().get(1);
        TextField seatsField = (TextField) grid.getChildren().get(3);
        ComboBox<String> statusCombo = (ComboBox<String>) grid.getChildren().get(5);

        String statusEnum = switch (statusCombo.getValue()) {
            case "Đang hoạt động" -> "HOAT_DONG";
            case "Đang bảo trì" -> "BAO_TRI";
            case "Ngừng hoạt động" -> "NGUNG";
            default -> "HOAT_DONG";
        };

        return new Cinema(
            0L,
            nameField.getText().trim(),
            Integer.parseInt(seatsField.getText().trim()),
            statusEnum
        );
    }

    private boolean insertCinema(Cinema cinema) {
        String query = "INSERT INTO phong (ten_phong, suc_chua, trang_thai) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, cinema.getTenPhong());
            stmt.setInt(2, cinema.getSucChua());
            stmt.setString(3, cinema.getTrangThai());
            
            int affected = stmt.executeUpdate();
            
            if (affected > 0) {
                // Lấy ID vừa tạo để tạo ghế tự động
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        long maPhong = generatedKeys.getLong(1);
                        createDefaultSeats(maPhong, cinema.getSucChua());
                    }
                }
                return true;
            }
            
            return false;
            
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể thêm phòng: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private void createDefaultSeats(long maPhong, int totalSeats) {
        // Tạo ghế mặc định: 10 hàng x 10 ghế (hoặc tính toán dựa trên totalSeats)
        int rows = (int) Math.ceil(totalSeats / 10.0);
        int seatsPerRow = 10;
        
        String insertSeat = "INSERT INTO ghe (ma_phong, hang_ghe, so_ghe, ma_loai_ghe) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(insertSeat)) {
            
            // Lấy loại ghế mặc định (Thường)
            long maLoaiGhe = getDefaultSeatTypeId();
            
            char hangGhe = 'A';
            int seatCount = 0;
            
            for (int i = 0; i < rows && seatCount < totalSeats; i++) {
                for (int j = 1; j <= seatsPerRow && seatCount < totalSeats; j++) {
                    stmt.setLong(1, maPhong);
                    stmt.setString(2, String.valueOf(hangGhe));
                    stmt.setInt(3, j);
                    stmt.setLong(4, maLoaiGhe);
                    stmt.addBatch();
                    seatCount++;
                }
                hangGhe++;
            }
            
            stmt.executeBatch();
            
        } catch (SQLException e) {
            System.err.println("Không thể tạo ghế tự động: " + e.getMessage());
        }
    }

    private long getDefaultSeatTypeId() throws SQLException {
        String query = "SELECT ma_loai_ghe FROM loai_ghe LIMIT 1";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                return rs.getLong("ma_loai_ghe");
            }
        }
        // Nếu chưa có loại ghế, tạo mặc định
        String insert = "INSERT INTO loai_ghe (ten_loai_ghe, he_so_gia) VALUES ('Thường', 1.0)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS)) {
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        }
        return 1L;
    }

    private boolean updateCinema(Cinema cinema) {
        String query = "UPDATE phong SET ten_phong=?, suc_chua=?, trang_thai=? WHERE ma_phong=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, cinema.getTenPhong());
            stmt.setInt(2, cinema.getSucChua());
            stmt.setString(3, cinema.getTrangThai());
            stmt.setLong(4, cinema.getMaPhong());
            
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể cập nhật phòng: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private boolean deleteCinema(long maPhong) {
        String query = "DELETE FROM phong WHERE ma_phong=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setLong(1, maPhong);
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            if (e.getMessage().contains("foreign key constraint")) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", 
                    "Không thể xóa phòng này vì đang có suất chiếu hoặc dữ liệu liên quan!");
            } else {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể xóa phòng: " + e.getMessage());
            }
            e.printStackTrace();
            return false;
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // Model class Cinema
    public static class Cinema {
        private long maPhong;
        private String tenPhong;
        private int sucChua;
        private String trangThai;

        public Cinema(long maPhong, String tenPhong, int sucChua, String trangThai) {
            this.maPhong = maPhong;
            this.tenPhong = tenPhong;
            this.sucChua = sucChua;
            this.trangThai = trangThai;
        }

        public long getMaPhong() { return maPhong; }
        public void setMaPhong(long maPhong) { this.maPhong = maPhong; }
        public String getTenPhong() { return tenPhong; }
        public void setTenPhong(String tenPhong) { this.tenPhong = tenPhong; }
        public int getSucChua() { return sucChua; }
        public void setSucChua(int sucChua) { this.sucChua = sucChua; }
        public String getTrangThai() { return trangThai; }
        public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
    }
    
}