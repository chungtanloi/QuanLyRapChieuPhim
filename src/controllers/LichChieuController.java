package controllers;

import database.DBConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import models.LichChieu;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.*;

public class LichChieuController {
    
    // --- FXML TỪ LỊCH CHIẾU CHÍNH (LichChieu.fxml) ---
    @FXML private Button btnThemLichChieu, btnXemTuanTruoc, btnXemHomNay, btnXemTuanSau;
    @FXML private ComboBox<String> cbRapLichChieu, cbPhongLichChieu;
    @FXML private Label lblTuanHienTai, lblDateMonday, lblDateTuesday, lblDateWednesday, 
                            lblDateThursday, lblDateFriday, lblDateSaturday, lblDateSunday;
    @FXML private VBox containerLichChieu; // Vùng chứa lịch biểu
    
    // --- FXML TỪ DIALOG THÊM LỊCH CHIẾU (ThemLichChieuDialog.fxml) ---
    // Các thuộc tính này được liên kết khi dialog được load
    @FXML private ComboBox<String> cbPhim;
    @FXML private ComboBox<String> cbPhong;
    @FXML private ComboBox<String> cbDinhDang;
    @FXML private DatePicker dpNgayChieu;
    @FXML private TextField txtGioBatDau;
    @FXML private Label lblThoiGianKetThuc;
    @FXML private Label lblXungDot;
    @FXML private Button btnLuu;
    @FXML private Button btnHuy; // Thêm nút hủy cho dialog
    

    // --- Biến nội bộ ---
    private LocalDate currentWeekStart;
    private ObservableList<LichChieu> lichChieuList = FXCollections.observableArrayList();
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM");
    private Map<String, List<LichChieu>> lichChieuTheoPhong = new HashMap<>();
    private Stage dialogStage; // Stage cho cửa sổ dialog mới
    private String selectedPhimId; // Lưu mã phim đã chọn (key)
    private Map<String, Integer> phimMap = new HashMap<>(); // Dùng chung để lưu Mã Phim

    
    // ====================================================================
    // 1. INITIALIZE VÀ SETUP
    // ====================================================================

    @FXML
    private void initialize() {
        // Lấy ngày bắt đầu tuần (Thứ Hai) của tuần hiện tại
        currentWeekStart = getStartOfWeek(LocalDate.now());
        
        loadComboBoxData(); // Tải Rạp/Phòng cho ComboBox chính
        setupEventHandlers();
        updateWeekDisplay(); // <--- FIX LỖI HIỂN THỊ NGÀY
        loadLichChieuTuan(); // Tải và vẽ lịch lần đầu
    }
    
    private void setupEventHandlers() {
        btnThemLichChieu.setOnAction(e -> showThemLichChieuDialog());
        btnXemTuanTruoc.setOnAction(e -> {
            currentWeekStart = currentWeekStart.minusWeeks(1);
            updateWeekDisplay();
            loadLichChieuTuan();
        });
        btnXemHomNay.setOnAction(e -> {
            currentWeekStart = getStartOfWeek(LocalDate.now());
            updateWeekDisplay();
            loadLichChieuTuan();
        });
        btnXemTuanSau.setOnAction(e -> {
            currentWeekStart = currentWeekStart.plusWeeks(1);
            updateWeekDisplay();
            loadLichChieuTuan();
        });
        
        // Listener cho ComboBox Lịch Chiếu chính
        cbRapLichChieu.valueProperty().addListener((obs, oldVal, newVal) -> loadLichChieuTuan());
        cbPhongLichChieu.valueProperty().addListener((obs, oldVal, newVal) -> loadLichChieuTuan());
    }
    
    // Lấy dữ liệu cho ComboBox Rạp và Phòng trên giao diện chính (Sử dụng JOIN RAP)
    private void loadComboBoxData() {
        // Load danh sách rạp
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT ten_rap FROM rap ORDER BY ten_rap");
             ResultSet rs = ps.executeQuery()) {
            
            ObservableList<String> rapList = FXCollections.observableArrayList();
            rapList.add("Chọn rạp"); // Tùy chọn mặc định
            while (rs.next()) {
                rapList.add(rs.getString("ten_rap"));
            }
            cbRapLichChieu.setItems(rapList);
            cbRapLichChieu.getSelectionModel().selectFirst();
            
        } catch (SQLException e) {
            showError("Lỗi tải dữ liệu Rạp", "Lỗi CSDL khi tải danh sách Rạp. Vui lòng kiểm tra lại Schema SQL.");
            e.printStackTrace();
        }
        
        // Load danh sách phòng
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT ten_phong FROM phong ORDER BY ten_phong");
             ResultSet rs = ps.executeQuery()) {
            
            ObservableList<String> phongList = FXCollections.observableArrayList();
            phongList.add("Chọn phòng"); // Tùy chọn mặc định
            while (rs.next()) {
                phongList.add(rs.getString("ten_phong"));
            }
            cbPhongLichChieu.setItems(phongList);
            cbPhongLichChieu.getSelectionModel().selectFirst();
            
        } catch (SQLException e) {
            showError("Lỗi tải dữ liệu Phòng", "Lỗi CSDL khi tải danh sách Phòng.");
            e.printStackTrace();
        }
    }
    
    // Tính toán ngày bắt đầu tuần (Thứ Hai)
    private LocalDate getStartOfWeek(LocalDate date) {
        // Tìm ngày Thứ Hai của tuần hiện tại (hoặc tuần của ngày đó)
        return date.with(TemporalAdjusters.previousOrSame(
            WeekFields.of(Locale.getDefault()).getFirstDayOfWeek()
        ));
    }
    
    // Cập nhật nhãn ngày tháng trên Header 
    private void updateWeekDisplay() {
        LocalDate monday = currentWeekStart;
        
        lblTuanHienTai.setText("Tuần: " + monday.format(dateFormatter) + " - " + monday.plusDays(6).format(dateFormatter));
        
        lblDateMonday.setText("Thứ 2\n" + monday.format(dateFormatter));
        lblDateTuesday.setText("Thứ 3\n" + monday.plusDays(1).format(dateFormatter));
        lblDateWednesday.setText("Thứ 4\n" + monday.plusDays(2).format(dateFormatter));
        lblDateThursday.setText("Thứ 5\n" + monday.plusDays(3).format(dateFormatter));
        lblDateFriday.setText("Thứ 6\n" + monday.plusDays(4).format(dateFormatter));
        lblDateSaturday.setText("Thứ 7\n" + monday.plusDays(5).format(dateFormatter));
        lblDateSunday.setText("Chủ nhật\n" + monday.plusDays(6).format(dateFormatter));
    }

    // ====================================================================
    // 2. TẢI VÀ VẼ LỊCH CHIẾU (Đã cập nhật SQL JOIN RAP)
    // ====================================================================
    
    private void loadLichChieuTuan() {
        LocalDate startOfWeek = currentWeekStart;
        LocalDate endOfWeek = startOfWeek.plusDays(6);
        
        String rapFilter = cbRapLichChieu.getValue();
        String phongFilter = cbPhongLichChieu.getValue();
        
        // Truy vấn SQL phức tạp để lấy thông tin Suất Chiếu, Phim, Phòng, Rạp
        StringBuilder sql = new StringBuilder("""
            SELECT sc.ma_suat_chieu, pm.ten_phim, ph.ten_phong, 
                   sc.bat_dau_luc, 
                   DATE_ADD(sc.bat_dau_luc, INTERVAL pm.thoi_luong_phut MINUTE) as ket_thuc_luc,
                   dd.ten_dinh_dang,
                   CASE 
                       WHEN sc.bat_dau_luc > NOW() THEN 'SẮP CHIẾU'
                       WHEN sc.bat_dau_luc <= NOW() AND DATE_ADD(sc.bat_dau_luc, INTERVAL pm.thoi_luong_phut MINUTE) > NOW() THEN 'ĐANG CHIẾU'
                       ELSE 'ĐÃ CHIẾU'
                   END as trang_thai
            FROM suat_chieu sc
            JOIN phim pm ON sc.ma_phim = pm.ma_phim
            JOIN phong ph ON sc.ma_phong = ph.ma_phong
            LEFT JOIN dinh_dang dd ON sc.ma_dinh_dang = dd.ma_dinh_dang
            LEFT JOIN rap r ON ph.ma_rap = r.ma_rap /* <--- JOIN BẢNG RAP MỚI */
            WHERE DATE(sc.bat_dau_luc) BETWEEN ? AND ?
        """);
        
        // Thêm điều kiện lọc theo Rạp
        if (rapFilter != null && !rapFilter.isEmpty() && !rapFilter.equals("Chọn rạp")) {
            sql.append(" AND r.ten_rap = ?");
        }
        
        // Thêm điều kiện lọc theo Phòng
        if (phongFilter != null && !phongFilter.isEmpty() && !phongFilter.equals("Chọn phòng")) {
            sql.append(" AND ph.ten_phong = ?");
        }
        
        sql.append(" ORDER BY ph.ten_phong, sc.bat_dau_luc");
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            
            int paramIndex = 1;
            ps.setDate(paramIndex++, java.sql.Date.valueOf(startOfWeek));
            ps.setDate(paramIndex++, java.sql.Date.valueOf(endOfWeek));
            
            if (rapFilter != null && !rapFilter.isEmpty() && !rapFilter.equals("Chọn rạp")) {
                ps.setString(paramIndex++, rapFilter);
            }
            
            if (phongFilter != null && !phongFilter.isEmpty() && !phongFilter.equals("Chọn phòng")) {
                ps.setString(paramIndex++, phongFilter);
            }
            
            ResultSet rs = ps.executeQuery();
            lichChieuList.clear();
            lichChieuTheoPhong.clear();
            
            while (rs.next()) {
                LichChieu item = new LichChieu(
                    rs.getLong("ma_suat_chieu"),
                    rs.getString("ten_phim"),
                    rs.getString("ten_phong"),
                    rs.getTimestamp("bat_dau_luc").toLocalDateTime(),
                    rs.getTimestamp("ket_thuc_luc").toLocalDateTime(),
                    rs.getString("ten_dinh_dang"),
                    rs.getString("trang_thai")
                );
                lichChieuList.add(item);
                
                String phong = item.getTenPhong();
                lichChieuTheoPhong.computeIfAbsent(phong, k -> new ArrayList<>()).add(item);
            }
            
            renderLichChieuCalendar();
            
        } catch (SQLException e) {
            showError("Lỗi tải lịch chiếu", "Lỗi CSDL: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // (Giữ nguyên các hàm renderLichChieuCalendar, createPhongRow, createDayCell, createSuatChieuButton, showSuatChieuDetail)
    // ... CÁC HÀM NÀY GIỮ NGUYÊN NHƯ CODE HOÀN CHỈNH TRƯỚC ...

    private void renderLichChieuCalendar() {
        containerLichChieu.getChildren().clear();
        
        List<String> sortedPhongs = new ArrayList<>(lichChieuTheoPhong.keySet());
        Collections.sort(sortedPhongs);

        for (String phong : sortedPhongs) {
            HBox phongRow = createPhongRow(phong, lichChieuTheoPhong.get(phong));
            containerLichChieu.getChildren().add(phongRow);
        }
        
        if(sortedPhongs.isEmpty()){
             Label lblEmpty = new Label("Không có suất chiếu nào được tìm thấy trong tuần này.");
             lblEmpty.setStyle("-fx-text-fill: #9e9e9e; -fx-font-size: 14px; -fx-padding: 20px;");
             containerLichChieu.getChildren().add(lblEmpty);
        }
    }
    
    private HBox createPhongRow(String tenPhong, List<LichChieu> suatChieus) {
        HBox row = new HBox(0); 
        row.setPrefHeight(80);
        row.setPrefWidth(120 + 150 * 7); 
        row.setStyle("-fx-border-color: #e0e0e0; -fx-border-width: 0 0 1 0;");
        
        // Ô tên phòng
        VBox phongCell = new VBox();
        phongCell.setPrefWidth(120);
        phongCell.setAlignment(Pos.CENTER);
        phongCell.setStyle("-fx-background-color: #f5f5f5; -fx-border-color: #e0e0e0; -fx-border-width: 0 1 0 0;");
        phongCell.setPadding(new Insets(5));
        
        Label lblPhong = new Label(tenPhong);
        lblPhong.setFont(Font.font("System", FontWeight.BOLD, 12));
        lblPhong.setWrapText(true);
        phongCell.getChildren().add(lblPhong);
        
        row.getChildren().add(phongCell);
        
        // Tạo ô cho mỗi ngày trong tuần
        for (int i = 0; i < 7; i++) {
            LocalDate ngay = currentWeekStart.plusDays(i);
            VBox dayCell = createDayCell(ngay, suatChieus);
            dayCell.setPrefWidth(150.0); 
            row.getChildren().add(dayCell);
        }
        
        return row;
    }
    
    private VBox createDayCell(LocalDate ngay, List<LichChieu> suatChieus) {
        VBox dayCell = new VBox(2);
        dayCell.setStyle("-fx-background-color: #fafafa; -fx-border-color: #e0e0e0; -fx-border-width: 0 1 0 0;");
        dayCell.setPadding(new Insets(5));
        
        List<LichChieu> suatChieuHomNay = suatChieus.stream()
            .filter(suat -> suat.getBatDauLuc().toLocalDate().equals(ngay))
            .sorted((s1, s2) -> s1.getBatDauLuc().compareTo(s2.getBatDauLuc()))
            .toList();
        
        if (suatChieuHomNay.isEmpty()) {
            Label lblEmpty = new Label("—");
            lblEmpty.setStyle("-fx-text-fill: #9e9e9e; -fx-font-size: 10px;");
            dayCell.setAlignment(Pos.CENTER);
            dayCell.getChildren().add(lblEmpty);
        } else {
            for (LichChieu suat : suatChieuHomNay) {
                Button btnSuat = createSuatChieuButton(suat);
                dayCell.getChildren().add(btnSuat);
            }
        }
        
        return dayCell;
    }
    
    private Button createSuatChieuButton(LichChieu suat) {
        Button btn = new Button();
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setPrefHeight(30);
        btn.setStyle(String.format(
            "-fx-background-color: %s; -fx-text-fill: white; -fx-font-size: 9px; -fx-border-radius: 3; -fx-background-radius: 3;",
            suat.getMauSac()
        ));
        
        String tooltipText = String.format(
            "%s\n%s: %s - %s\nĐịnh dạng: %s\nTrạng thái: %s",
            suat.getTenPhim(),
            suat.getTenPhong(),
            suat.getBatDauLuc().toLocalTime(),
            suat.getKetThucLuc().toLocalTime(),
            suat.getDinhDang(),
            suat.getTrangThai()
        );
        
        Tooltip tooltip = new Tooltip(tooltipText);
        Tooltip.install(btn, tooltip);
        
        btn.setText(suat.getBatDauLuc().toLocalTime().toString());
        btn.setOnAction(e -> showSuatChieuDetail(suat));
        
        return btn;
    }
    
    private void showSuatChieuDetail(LichChieu suat) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Chi tiết suất chiếu");
        alert.setHeaderText(suat.getTenPhim());
        alert.setContentText(String.format(
            "Phòng: %s\n" +
            "Thời gian: %s - %s\n" +
            "Định dạng: %s\n" +
            "Trạng thái: %s\n" +
            "Mã suất: %d",
            suat.getTenPhong(),
            suat.getBatDauLuc().format(DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy")),
            suat.getKetThucLuc().format(DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy")),
            suat.getDinhDang(),
            suat.getTrangThai(),
            suat.getMaSuatChieu()
        ));
        alert.showAndWait();
    }
    
    // ... (Giữ nguyên các hàm cho DIALOG THÊM LỊCH CHIẾU)
    
    @FXML
    private void showThemLichChieuDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/models/ThemLichChieuDialog.fxml"));
            loader.setController(this); 
            VBox page = loader.load();

            dialogStage = new Stage();
            dialogStage.setTitle("Thêm Lịch Chiếu Mới");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            
            Stage parentStage = (Stage) btnThemLichChieu.getScene().getWindow();
            dialogStage.initOwner(parentStage); 
            
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);
            
            loadDialogComboBoxData();
            setupDialogListeners();
            
            dialogStage.showAndWait();
            
        } catch (Exception e) {
            showError("Lỗi Mở Cửa Sổ", "Không thể tải ThemLichChieuDialog.fxml: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void loadDialogComboBoxData() {
        // 1. Load Phim (Lấy tên phim và lưu Mã Phim vào map)
        phimMap.clear(); 
        ObservableList<String> phimList = FXCollections.observableArrayList();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT ma_phim, ten_phim, thoi_luong_phut FROM phim ORDER BY ten_phim");
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                String tenPhim = rs.getString("ten_phim");
                phimList.add(tenPhim);
                phimMap.put(tenPhim, rs.getInt("ma_phim")); 
            }
            cbPhim.setItems(phimList);
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        cbPhim.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                selectedPhimId = String.valueOf(phimMap.get(newVal)); 
                updateKetThucLuc();
                checkThoiGianXungDot();
            }
        });

        // 2. Load Phòng (Copy từ ComboBox chính)
        ObservableList<String> phongDialogList = FXCollections.observableArrayList(cbPhongLichChieu.getItems());
        if(phongDialogList.contains("Chọn phòng")) {
             phongDialogList.remove("Chọn phòng");
        }
        cbPhong.setItems(phongDialogList);
        
        // 3. Load Định Dạng
        ObservableList<String> dinhDangList = FXCollections.observableArrayList();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT ten_dinh_dang FROM dinh_dang ORDER BY ten_dinh_dang");
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                dinhDangList.add(rs.getString("ten_dinh_dang"));
            }
            cbDinhDang.setItems(dinhDangList);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private void setupDialogListeners() {
        dpNgayChieu.valueProperty().addListener((obs, oldVal, newVal) -> checkThoiGianXungDot());
        txtGioBatDau.textProperty().addListener((obs, oldVal, newVal) -> {
            updateKetThucLuc();
            checkThoiGianXungDot();
        });
        cbPhong.valueProperty().addListener((obs, oldVal, newVal) -> checkThoiGianXungDot());
        
        if (btnHuy != null) {
            btnHuy.setOnAction(e -> handleCancelThemSuatChieu());
        }
        
        btnLuu.setOnAction(e -> handleSaveLichChieu());
    }
    
    private void updateKetThucLuc() {
        // ... (Giữ nguyên logic tính thời gian kết thúc)
        if (cbPhim.getValue() == null || txtGioBatDau.getText().isEmpty() || selectedPhimId == null) {
            lblThoiGianKetThuc.setText("Thời gian kết thúc (Tự động):");
            return;
        }
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT thoi_luong_phut FROM phim WHERE ma_phim = ?")) {
            
            ps.setString(1, selectedPhimId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int thoiLuong = rs.getInt("thoi_luong_phut");
                
                LocalDate ngay = dpNgayChieu.getValue() != null ? dpNgayChieu.getValue() : LocalDate.now();
                
                LocalTime gio = LocalTime.parse(txtGioBatDau.getText(), DateTimeFormatter.ofPattern("HH:mm"));
                
                LocalDateTime ketThuc = LocalDateTime.of(ngay, gio).plusMinutes(thoiLuong);
                
                lblThoiGianKetThuc.setText("Thời gian kết thúc (Tự động): " + 
                                           ketThuc.format(DateTimeFormatter.ofPattern("HH:mm dd/MM")));
            }
        } catch (SQLException e) {
             lblThoiGianKetThuc.setText("Lỗi SQL khi lấy thời lượng phim.");
             btnLuu.setDisable(true);
        } catch (DateTimeParseException e) {
            lblThoiGianKetThuc.setText("Lỗi định dạng giờ (HH:mm).");
            btnLuu.setDisable(true);
        }
    }

    private void checkThoiGianXungDot() {
        // ... (Giữ nguyên logic kiểm tra xung đột)
        lblXungDot.setText("");
        btnLuu.setDisable(true);
        
        if (cbPhong.getValue() == null || dpNgayChieu.getValue() == null || 
            txtGioBatDau.getText().isEmpty() || selectedPhimId == null || 
            cbDinhDang.getValue() == null) {
            lblXungDot.setText("Vui lòng điền đủ thông tin.");
            lblXungDot.setStyle("-fx-text-fill: #9e9e9e;");
            return;
        }
        
        try {
            LocalTime gioBatDau = LocalTime.parse(txtGioBatDau.getText(), DateTimeFormatter.ofPattern("HH:mm"));
            LocalDateTime batDauMoi = LocalDateTime.of(dpNgayChieu.getValue(), gioBatDau);
            
            int thoiLuong = 0;
            // Lấy thời lượng phim để tính thời gian kết thúc
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement("SELECT thoi_luong_phut FROM phim WHERE ma_phim = ?")) {
                ps.setString(1, selectedPhimId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    thoiLuong = rs.getInt("thoi_luong_phut");
                }
            }
            
            if (thoiLuong == 0) {
                 lblXungDot.setText("Lỗi: Phim không có thời lượng.");
                 lblXungDot.setStyle("-fx-text-fill: red;");
                 return;
            }
            
            LocalDateTime ketThucMoi = batDauMoi.plusMinutes(thoiLuong);
            String tenPhong = cbPhong.getValue();
            
            String sql = """
                SELECT sc.ma_suat_chieu, pm.ten_phim, sc.bat_dau_luc,
                       DATE_ADD(sc.bat_dau_luc, INTERVAL pm.thoi_luong_phut MINUTE) as ket_thuc_luc
                FROM suat_chieu sc
                JOIN phong ph ON sc.ma_phong = ph.ma_phong
                JOIN phim pm ON sc.ma_phim = pm.ma_phim
                WHERE ph.ten_phong = ? 
                AND sc.bat_dau_luc < ? AND DATE_ADD(sc.bat_dau_luc, INTERVAL pm.thoi_luong_phut MINUTE) > ?
            """;
            
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                
                ps.setString(1, tenPhong);
                ps.setTimestamp(2, java.sql.Timestamp.valueOf(ketThucMoi));
                ps.setTimestamp(3, java.sql.Timestamp.valueOf(batDauMoi));
                
                ResultSet rs = ps.executeQuery();
                
                if (rs.next()) {
                    String tenPhimCu = rs.getString("ten_phim");
                    LocalDateTime batDauCu = rs.getTimestamp("bat_dau_luc").toLocalDateTime();
                    LocalDateTime ketThucCu = rs.getTimestamp("ket_thuc_luc").toLocalDateTime();
                    
                    lblXungDot.setText("LỖI: Xung đột với suất chiếu phim '" + tenPhimCu + 
                                       "' lúc " + batDauCu.format(DateTimeFormatter.ofPattern("HH:mm")) + 
                                       " đến " + ketThucCu.format(DateTimeFormatter.ofPattern("HH:mm")) + "!");
                    lblXungDot.setStyle("-fx-text-fill: red;");
                    btnLuu.setDisable(true);
                    return;
                }
            }
            
            lblXungDot.setText("Thời gian hợp lệ.");
            lblXungDot.setStyle("-fx-text-fill: green;");
            btnLuu.setDisable(false); 
            
        } catch (Exception e) {
            lblXungDot.setText("Lỗi kiểm tra xung đột: Định dạng giờ sai (HH:mm) hoặc lỗi CSDL.");
            lblXungDot.setStyle("-fx-text-fill: red;");
            btnLuu.setDisable(true);
            e.printStackTrace();
        }
    }
    
    private void handleSaveLichChieu() {
        // ... (Giữ nguyên logic lưu lịch chiếu)
        if (btnLuu.isDisable()) {
            showError("Lỗi Lưu", "Vui lòng khắc phục lỗi xung đột hoặc điền đầy đủ thông tin hợp lệ.");
            return;
        }
        
        try {
            int maPhong = getMaFromTen("phong", "ten_phong", cbPhong.getValue(), "ma_phong");
            int maDinhDang = getMaFromTen("dinh_dang", "ten_dinh_dang", cbDinhDang.getValue(), "ma_dinh_dang");
            
            LocalTime gioBatDau = LocalTime.parse(txtGioBatDau.getText(), DateTimeFormatter.ofPattern("HH:mm"));
            LocalDateTime batDauLuc = LocalDateTime.of(dpNgayChieu.getValue(), gioBatDau);
            
            String sql = "INSERT INTO suat_chieu (ma_phim, ma_phong, ma_dinh_dang, bat_dau_luc, gia_co_ban, trang_thai) VALUES (?, ?, ?, ?, ?, 'MO_BAN')";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                
                ps.setString(1, selectedPhimId);
                ps.setInt(2, maPhong);
                ps.setInt(3, maDinhDang);
                ps.setTimestamp(4, java.sql.Timestamp.valueOf(batDauLuc));
                
                ps.executeUpdate();
                
                Alert successAlert = new Alert(Alert.AlertType.INFORMATION, "Thêm lịch chiếu thành công!");
                successAlert.showAndWait();
                
                handleCancelThemSuatChieu(); // Đóng dialog
                loadLichChieuTuan(); // Tải lại lịch
            }

        } catch (Exception e) {
            showError("Lỗi Lưu CSDL", "Không thể thêm suất chiếu: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private int getMaFromTen(String tenBang, String tenCotTen, String ten, String tenCotMa) throws SQLException {
        String sql = String.format("SELECT %s FROM %s WHERE %s = ?", tenCotMa, tenBang, tenCotTen);
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, ten);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(tenCotMa);
            return 0;
        }
    }

    @FXML
    private void handleCancelThemSuatChieu() {
        if (dialogStage != null) {
            dialogStage.close();
        }
    }
    
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}