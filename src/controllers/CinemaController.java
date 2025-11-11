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

    @FXML private TableView<Theater> theaterTable;
    @FXML private TableColumn<Theater, String> nameColumn;
    @FXML private TableColumn<Theater, String> seatsColumn;
    @FXML private TableColumn<Theater, String> screenTypeColumn;
    @FXML private TableColumn<Theater, String> statusColumn;
    @FXML private TableColumn<Theater, String> actionsColumn;
    
    @FXML private Label totalTheatersLabel;
    @FXML private Label activeTheatersLabel;
    @FXML private Label maintenanceTheatersLabel;
    @FXML private Label totalSeatsLabel;

    private ObservableList<Theater> theaterList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTableColumns();
        loadTheaterData();
        updateStatistics();
    }

    private void setupTableColumns() {
        nameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));
        seatsColumn.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getSeats())));
        screenTypeColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getScreenType()));
        statusColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus()));
        
        // Cột thao tác với nút Sửa và Xóa
        actionsColumn.setCellFactory(col -> new TableCell<Theater, String>() {
            private final Button editBtn = new Button("Sửa");
            private final Button deleteBtn = new Button("Xóa");
            private final HBox container = new HBox(10, editBtn, deleteBtn);

            {
                container.setAlignment(Pos.CENTER);
                editBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-cursor: hand;");
                deleteBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-cursor: hand;");
                
                editBtn.setOnAction(e -> {
                    Theater theater = getTableView().getItems().get(getIndex());
                    handleEdit(theater);
                });
                
                deleteBtn.setOnAction(e -> {
                    Theater theater = getTableView().getItems().get(getIndex());
                    handleDelete(theater);
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : container);
            }
        });
    }

    private void loadTheaterData() {
        theaterList.clear();
        String query = "SELECT * FROM rap_chieu";
        
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Theater theater = new Theater(
                    rs.getInt("ma_rap"),
                    rs.getString("ten_rap"),
                    rs.getInt("so_ghe"),
                    rs.getString("loai_man_hinh"),
                    rs.getString("trang_thai")
                );
                theaterList.add(theater);
            }
            theaterTable.setItems(theaterList);
            
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tải dữ liệu rạp: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateStatistics() {
        try (Connection conn = DBConnection.getConnection()) {
            // Tổng số rạp
            String totalQuery = "SELECT COUNT(*) as total FROM rap_chieu";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(totalQuery)) {
                if (rs.next()) {
                    totalTheatersLabel.setText(String.valueOf(rs.getInt("total")));
                }
            }
            
            // Đang hoạt động
            String activeQuery = "SELECT COUNT(*) as active FROM rap_chieu WHERE trang_thai = 'Đang hoạt động'";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(activeQuery)) {
                if (rs.next()) {
                    activeTheatersLabel.setText(String.valueOf(rs.getInt("active")));
                }
            }
            
            // Đang bảo trì
            String maintenanceQuery = "SELECT COUNT(*) as maintenance FROM rap_chieu WHERE trang_thai = 'Đang bảo trì'";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(maintenanceQuery)) {
                if (rs.next()) {
                    maintenanceTheatersLabel.setText(String.valueOf(rs.getInt("maintenance")));
                }
            }
            
            // Tổng ghế ngồi
            String seatsQuery = "SELECT SUM(so_ghe) as total_seats FROM rap_chieu";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(seatsQuery)) {
                if (rs.next()) {
                    totalSeatsLabel.setText(String.valueOf(rs.getInt("total_seats")));
                }
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAddTheater() {
        Dialog<Theater> dialog = new Dialog<>();
        dialog.setTitle("Thêm Rạp Mới");
        dialog.setHeaderText("Nhập thông tin rạp chiếu");

        ButtonType addButtonType = new ButtonType("Thêm", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        GridPane grid = createTheaterForm(null);
        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                return getTheaterFromForm(grid);
            }
            return null;
        });

        Optional<Theater> result = dialog.showAndWait();
        result.ifPresent(theater -> {
            if (insertTheater(theater)) {
                loadTheaterData();
                updateStatistics();
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã thêm rạp mới!");
            }
        });
    }

    private void handleEdit(Theater theater) {
        Dialog<Theater> dialog = new Dialog<>();
        dialog.setTitle("Sửa Thông Tin Rạp");
        dialog.setHeaderText("Chỉnh sửa thông tin rạp: " + theater.getName());

        ButtonType saveButtonType = new ButtonType("Lưu", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = createTheaterForm(theater);
        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                Theater updated = getTheaterFromForm(grid);
                updated.setId(theater.getId());
                return updated;
            }
            return null;
        });

        Optional<Theater> result = dialog.showAndWait();
        result.ifPresent(updated -> {
            if (updateTheater(updated)) {
                loadTheaterData();
                updateStatistics();
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã cập nhật thông tin rạp!");
            }
        });
    }

    private void handleDelete(Theater theater) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận xóa");
        confirm.setHeaderText("Bạn có chắc muốn xóa rạp này?");
        confirm.setContentText("Rạp: " + theater.getName());

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (deleteTheater(theater.getId())) {
                loadTheaterData();
                updateStatistics();
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã xóa rạp!");
            }
        }
    }

    private GridPane createTheaterForm(Theater theater) {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nameField = new TextField();
        nameField.setPromptText("Tên rạp");
        if (theater != null) nameField.setText(theater.getName());

        TextField seatsField = new TextField();
        seatsField.setPromptText("Số ghế");
        if (theater != null) seatsField.setText(String.valueOf(theater.getSeats()));

        ComboBox<String> screenTypeCombo = new ComboBox<>();
        screenTypeCombo.getItems().addAll("2D", "3D", "IMAX", "4DX");
        if (theater != null) screenTypeCombo.setValue(theater.getScreenType());
        else screenTypeCombo.setValue("2D");

        ComboBox<String> statusCombo = new ComboBox<>();
        statusCombo.getItems().addAll("Đang hoạt động", "Đang bảo trì");
        if (theater != null) statusCombo.setValue(theater.getStatus());
        else statusCombo.setValue("Đang hoạt động");

        grid.add(new Label("Tên rạp:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Số ghế:"), 0, 1);
        grid.add(seatsField, 1, 1);
        grid.add(new Label("Loại màn hình:"), 0, 2);
        grid.add(screenTypeCombo, 1, 2);
        grid.add(new Label("Trạng thái:"), 0, 3);
        grid.add(statusCombo, 1, 3);

        return grid;
    }

    private Theater getTheaterFromForm(GridPane grid) {
        TextField nameField = (TextField) grid.getChildren().get(1);
        TextField seatsField = (TextField) grid.getChildren().get(3);
        ComboBox<String> screenTypeCombo = (ComboBox<String>) grid.getChildren().get(5);
        ComboBox<String> statusCombo = (ComboBox<String>) grid.getChildren().get(7);

        return new Theater(
            0,
            nameField.getText(),
            Integer.parseInt(seatsField.getText()),
            screenTypeCombo.getValue(),
            statusCombo.getValue()
        );
    }

    private boolean insertTheater(Theater theater) {
        String query = "INSERT INTO rap_chieu (ten_rap, so_ghe, loai_man_hinh, trang_thai) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, theater.getName());
            stmt.setInt(2, theater.getSeats());
            stmt.setString(3, theater.getScreenType());
            stmt.setString(4, theater.getStatus());
            
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể thêm rạp: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private boolean updateTheater(Theater theater) {
        String query = "UPDATE rap_chieu SET ten_rap=?, so_ghe=?, loai_man_hinh=?, trang_thai=? WHERE ma_rap=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, theater.getName());
            stmt.setInt(2, theater.getSeats());
            stmt.setString(3, theater.getScreenType());
            stmt.setString(4, theater.getStatus());
            stmt.setInt(5, theater.getId());
            
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể cập nhật rạp: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private boolean deleteTheater(int theaterId) {
        String query = "DELETE FROM rap_chieu WHERE ma_rap=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, theaterId);
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể xóa rạp: " + e.getMessage());
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

    // Model class Theater
    public static class Theater {
        private int id;
        private String name;
        private int seats;
        private String screenType;
        private String status;

        public Theater(int id, String name, int seats, String screenType, String status) {
            this.id = id;
            this.name = name;
            this.seats = seats;
            this.screenType = screenType;
            this.status = status;
        }

        public int getId() { return id; }
        public void setId(int id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getSeats() { return seats; }
        public void setSeats(int seats) { this.seats = seats; }
        public String getScreenType() { return screenType; }
        public void setScreenType(String screenType) { this.screenType = screenType; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }
    
}