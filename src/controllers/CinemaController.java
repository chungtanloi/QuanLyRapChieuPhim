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
import javafx.scene.layout.VBox;

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
        
        screenTypeColumn.setCellValueFactory(data -> new SimpleStringProperty("Chuẩn"));
        
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
        
        actionsColumn.setCellFactory(col -> new TableCell<Cinema, String>() {
            private final Button editBtn = new Button("Sửa");
            private final Button deleteBtn = new Button("Xóa");
            private final Button detailBtn = new Button("Chi tiết");
            private final HBox container = new HBox(8, editBtn, detailBtn, deleteBtn);

            {
                container.setAlignment(Pos.CENTER);
                editBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-cursor: hand; -fx-padding: 5 12;");
                detailBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-cursor: hand; -fx-padding: 5 12;");
                deleteBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-cursor: hand; -fx-padding: 5 12;");
                
                editBtn.setOnAction(e -> {
                    Cinema cinema = getTableView().getItems().get(getIndex());
                    handleEdit(cinema);
                });
                
                detailBtn.setOnAction(e -> {
                    Cinema cinema = getTableView().getItems().get(getIndex());
                    handleShowDetail(cinema);
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

    /**
     * Cập nhật thống kê sử dụng Stored Procedure
     */
    private void updateStatistics() {
        try (Connection conn = DBConnection.getConnection();
             CallableStatement stmt = conn.prepareCall("{CALL sp_thong_ke_phong()}")) {
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                totalTheatersLabel.setText(String.valueOf(rs.getInt("tong_phong")));
                activeTheatersLabel.setText(String.valueOf(rs.getInt("dang_hoat_dong")));
                maintenanceTheatersLabel.setText(String.valueOf(rs.getInt("dang_bao_tri")));
                totalSeatsLabel.setText(String.valueOf(rs.getInt("tong_ghe")));
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tải thống kê: " + e.getMessage());
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
            if (cinema != null && insertCinemaUsingProcedure(cinema)) {
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
            if (updated != null && updateCinemaUsingProcedure(updated)) {
                loadCinemaData();
                updateStatistics();
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã cập nhật thông tin phòng chiếu!");
            }
        });
    }

    /**
     * Hiển thị chi tiết phòng sử dụng Stored Procedure
     */
    private void handleShowDetail(Cinema cinema) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Chi Tiết Phòng Chiếu");
        dialog.setHeaderText("Thông tin chi tiết: " + cinema.getTenPhong());
        
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: #f5f5f5;");
        
        try (Connection conn = DBConnection.getConnection();
             CallableStatement stmt = conn.prepareCall("{CALL sp_chi_tiet_phong(?)}")) {
            
            stmt.setLong(1, cinema.getMaPhong());
            
            // Thông tin phòng
            boolean hasResults = stmt.execute();
            if (hasResults) {
                ResultSet rsPhong = stmt.getResultSet();
                if (rsPhong.next()) {
                    Label infoLabel = new Label(
                        "Mã phòng: " + rsPhong.getLong("ma_phong") + "\n" +
                        "Tên phòng: " + rsPhong.getString("ten_phong") + "\n" +
                        "Sức chứa: " + rsPhong.getInt("suc_chua") + "\n" +
                        "Số ghế thực tế: " + rsPhong.getInt("so_ghe_thuc_te") + "\n" +
                        "Trạng thái: " + translateStatus(rsPhong.getString("trang_thai")) + "\n" +
                        "Tạo lúc: " + rsPhong.getTimestamp("tao_luc") + "\n" +
                        "Cập nhật lúc: " + rsPhong.getTimestamp("cap_nhat_luc")
                    );
                    infoLabel.setStyle("-fx-font-size: 14px; -fx-padding: 10; -fx-background-color: white; -fx-background-radius: 5;");
                    content.getChildren().add(infoLabel);
                }
                rsPhong.close();
            }
            
            // Danh sách ghế
            if (stmt.getMoreResults()) {
                ResultSet rsGhe = stmt.getResultSet();
                
                TableView<SeatInfo> seatTable = new TableView<>();
                seatTable.setPrefHeight(300);
                
                TableColumn<SeatInfo, String> rowCol = new TableColumn<>("Hàng");
                rowCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().hangGhe));
                rowCol.setPrefWidth(80);
                
                TableColumn<SeatInfo, String> seatCol = new TableColumn<>("Số ghế");
                seatCol.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().soGhe)));
                seatCol.setPrefWidth(80);
                
                TableColumn<SeatInfo, String> typeCol = new TableColumn<>("Loại ghế");
                typeCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().loaiGhe));
                typeCol.setPrefWidth(120);
                
                TableColumn<SeatInfo, String> ratioCol = new TableColumn<>("Hệ số giá");
                ratioCol.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().heSoGia)));
                ratioCol.setPrefWidth(100);
                
                seatTable.getColumns().addAll(rowCol, seatCol, typeCol, ratioCol);
                
                ObservableList<SeatInfo> seats = FXCollections.observableArrayList();
                while (rsGhe.next()) {
                    seats.add(new SeatInfo(
                        rsGhe.getString("hang_ghe"),
                        rsGhe.getInt("so_ghe"),
                        rsGhe.getString("ten_loai_ghe"),
                        rsGhe.getDouble("he_so_gia")
                    ));
                }
                seatTable.setItems(seats);
                
                Label seatLabel = new Label("Danh sách ghế (" + seats.size() + " ghế):");
                seatLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
                content.getChildren().addAll(seatLabel, seatTable);
                
                rsGhe.close();
            }
            
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tải chi tiết phòng: " + e.getMessage());
            e.printStackTrace();
        }
        
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().setPrefWidth(500);
        dialog.showAndWait();
    }

    private void handleDelete(Cinema cinema) {
        // Kiểm tra có thể xóa không bằng Function
        try (Connection conn = DBConnection.getConnection();
             CallableStatement stmt = conn.prepareCall("{? = CALL fn_kiem_tra_xoa_phong(?)}")) {
            
            stmt.registerOutParameter(1, Types.VARCHAR);
            stmt.setLong(2, cinema.getMaPhong());
            stmt.execute();
            
            String checkResult = stmt.getString(1);
            
            if (!checkResult.startsWith("CO_THE_XOA")) {
                showAlert(Alert.AlertType.WARNING, "Cảnh báo", 
                    "Không thể xóa phòng!\n" + checkResult.replace("_", " "));
                return;
            }
            
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể kiểm tra: " + e.getMessage());
            return;
        }
        
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận xóa");
        confirm.setHeaderText("Bạn có chắc muốn xóa phòng chiếu này?");
        confirm.setContentText("Phòng: " + cinema.getTenPhong() + "\nLưu ý: Các ghế liên quan sẽ bị xóa!");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (deleteCinemaUsingProcedure(cinema.getMaPhong())) {
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
        seatsField.setPromptText("Số ghế (20-500)");
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

    /**
     * Thêm phòng sử dụng Stored Procedure
     */
    private boolean insertCinemaUsingProcedure(Cinema cinema) {
        try (Connection conn = DBConnection.getConnection();
             CallableStatement stmt = conn.prepareCall("{CALL sp_them_phong(?, ?, ?, ?, ?)}")) {
            
            stmt.setString(1, cinema.getTenPhong());
            stmt.setInt(2, cinema.getSucChua());
            stmt.setString(3, cinema.getTrangThai());
            stmt.registerOutParameter(4, Types.BIGINT);
            stmt.registerOutParameter(5, Types.VARCHAR);
            
            stmt.execute();
            
            Long maPhong = stmt.getLong(4);
            String message = stmt.getString(5);
            
            if (maPhong != null && maPhong > 0) {
                System.out.println("✓ " + message);
                return true;
            } else {
                showAlert(Alert.AlertType.ERROR, "Lỗi", message);
                return false;
            }
            
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể thêm phòng: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Cập nhật phòng sử dụng Stored Procedure
     */
    private boolean updateCinemaUsingProcedure(Cinema cinema) {
        try (Connection conn = DBConnection.getConnection();
             CallableStatement stmt = conn.prepareCall("{CALL sp_cap_nhat_phong(?, ?, ?, ?, ?, ?)}")) {
            
            stmt.setLong(1, cinema.getMaPhong());
            stmt.setString(2, cinema.getTenPhong());
            stmt.setInt(3, cinema.getSucChua());
            stmt.setString(4, cinema.getTrangThai());
            stmt.registerOutParameter(5, Types.BOOLEAN);
            stmt.registerOutParameter(6, Types.VARCHAR);
            
            stmt.execute();
            
            boolean success = stmt.getBoolean(5);
            String message = stmt.getString(6);
            
            if (!success) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", message);
            } else {
                System.out.println("✓ " + message);
            }
            
            return success;
            
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể cập nhật phòng: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Xóa phòng sử dụng Stored Procedure
     */
    private boolean deleteCinemaUsingProcedure(long maPhong) {
        try (Connection conn = DBConnection.getConnection();
             CallableStatement stmt = conn.prepareCall("{CALL sp_xoa_phong(?, ?, ?)}")) {
            
            stmt.setLong(1, maPhong);
            stmt.registerOutParameter(2, Types.BOOLEAN);
            stmt.registerOutParameter(3, Types.VARCHAR);
            
            stmt.execute();
            
            boolean success = stmt.getBoolean(2);
            String message = stmt.getString(3);
            
            if (!success) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", message);
            } else {
                System.out.println("✓ " + message);
            }
            
            return success;
            
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể xóa phòng: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private String translateStatus(String status) {
        return switch (status) {
            case "HOAT_DONG" -> "Đang hoạt động";
            case "BAO_TRI" -> "Đang bảo trì";
            case "NGUNG" -> "Ngừng hoạt động";
            default -> status;
        };
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
    
    // Model class SeatInfo cho chi tiết ghế
    public static class SeatInfo {
        private String hangGhe;
        private int soGhe;
        private String loaiGhe;
        private double heSoGia;

        public SeatInfo(String hangGhe, int soGhe, String loaiGhe, double heSoGia) {
            this.hangGhe = hangGhe;
            this.soGhe = soGhe;
            this.loaiGhe = loaiGhe;
            this.heSoGia = heSoGia;
        }

        public String getHangGhe() { return hangGhe; }
        public int getSoGhe() { return soGhe; }
        public String getLoaiGhe() { return loaiGhe; }
        public double getHeSoGia() { return heSoGia; }
    }
}