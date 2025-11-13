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

    @FXML private Button btnThemLichChieu, btnXemTuanTruoc, btnXemHomNay, btnXemTuanSau;
    @FXML private ComboBox<String> cbRapLichChieu, cbPhongLichChieu;
    @FXML private Label lblTuanHienTai, lblDateMonday, lblDateTuesday, lblDateWednesday,
            lblDateThursday, lblDateFriday, lblDateSaturday, lblDateSunday;
    @FXML private VBox containerLichChieu;

    @FXML private ComboBox<String> cbPhim;
    @FXML private ComboBox<String> cbPhong;
    @FXML private ComboBox<String> cbDinhDang;
    @FXML private DatePicker dpNgayChieu;
    @FXML private TextField txtGioBatDau;
    @FXML private Label lblThoiGianKetThuc;
    @FXML private Label lblXungDot;
    @FXML private Button btnLuu;
    @FXML private Button btnHuy;

    private LocalDate currentWeekStart;
    private ObservableList<LichChieu> lichChieuList = FXCollections.observableArrayList();
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM");
    private Map<String, List<LichChieu>> lichChieuTheoPhong = new HashMap<>();
    private Stage dialogStage;
    private String selectedPhimId;
    private Map<String, Integer> phimMap = new HashMap<>();

    @FXML
    private void initialize() {
        currentWeekStart = getStartOfWeek(LocalDate.now());

        setupComboBox();
        setupEventHandlers();
        updateWeekDisplay();
        loadLichChieuTuan();
    }

    private void setupComboBox() {
        // ❗ Vì không dùng bảng RAP → ComboBox Rạp chỉ có 1 lựa chọn
        cbRapLichChieu.setItems(FXCollections.observableArrayList("Tất cả"));
        cbRapLichChieu.getSelectionModel().selectFirst();

        // Load danh sách phòng
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT ten_phong FROM phong ORDER BY ten_phong");
             ResultSet rs = ps.executeQuery()) {

            ObservableList<String> phongList = FXCollections.observableArrayList("Tất cả");
            while (rs.next()) phongList.add(rs.getString("ten_phong"));
            cbPhongLichChieu.setItems(phongList);
            cbPhongLichChieu.getSelectionModel().selectFirst();

        } catch (Exception e) {
            showError("Lỗi phòng", e.getMessage());
        }
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

        cbPhongLichChieu.valueProperty().addListener((obs, oldVal, newVal) -> loadLichChieuTuan());
    }

    private LocalDate getStartOfWeek(LocalDate date) {
        return date.with(TemporalAdjusters.previousOrSame(
                WeekFields.of(Locale.getDefault()).getFirstDayOfWeek()
        ));
    }

    private void updateWeekDisplay() {
        LocalDate monday = currentWeekStart;

        lblTuanHienTai.setText("Tuần: " + monday.format(dateFormatter)
                + " - " + monday.plusDays(6).format(dateFormatter));

        lblDateMonday.setText("Thứ 2\n" + monday.format(dateFormatter));
        lblDateTuesday.setText("Thứ 3\n" + monday.plusDays(1).format(dateFormatter));
        lblDateWednesday.setText("Thứ 4\n" + monday.plusDays(2).format(dateFormatter));
        lblDateThursday.setText("Thứ 5\n" + monday.plusDays(3).format(dateFormatter));
        lblDateFriday.setText("Thứ 6\n" + monday.plusDays(4).format(dateFormatter));
        lblDateSaturday.setText("Thứ 7\n" + monday.plusDays(5).format(dateFormatter));
        lblDateSunday.setText("Chủ nhật\n" + monday.plusDays(6).format(dateFormatter));
    }

    // =====================================================================================
    // 🔥  LOAD LỊCH CHIẾU TUẦN — KHÔNG CÓ RẠP
    // =====================================================================================
    private void loadLichChieuTuan() {
        LocalDate start = currentWeekStart;
        LocalDate end = start.plusDays(6);
        String phongFilter = cbPhongLichChieu.getValue();

        StringBuilder sql = new StringBuilder("""
            SELECT sc.ma_suat_chieu, pm.ten_phim, ph.ten_phong,
                   sc.bat_dau_luc,
                   DATE_ADD(sc.bat_dau_luc, INTERVAL pm.thoi_luong_phut MINUTE) AS ket_thuc_luc,
                   dd.ten_dinh_dang,
                   CASE
                       WHEN sc.bat_dau_luc > NOW() THEN 'SẮP CHIẾU'
                       WHEN sc.bat_dau_luc <= NOW()
                         AND DATE_ADD(sc.bat_dau_luc, INTERVAL pm.thoi_luong_phut MINUTE) > NOW()
                           THEN 'ĐANG CHIẾU'
                       ELSE 'ĐÃ CHIẾU'
                   END AS trang_thai
            FROM suat_chieu sc
            JOIN phim pm ON sc.ma_phim = pm.ma_phim
            JOIN phong ph ON sc.ma_phong = ph.ma_phong
            LEFT JOIN dinh_dang dd ON sc.ma_dinh_dang = dd.ma_dinh_dang
            WHERE DATE(sc.bat_dau_luc) BETWEEN ? AND ?
        """);

        if (phongFilter != null && !phongFilter.equals("Tất cả"))
            sql.append(" AND ph.ten_phong = ? ");

        sql.append(" ORDER BY ph.ten_phong, sc.bat_dau_luc ");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            int idx = 1;
            ps.setDate(idx++, java.sql.Date.valueOf(start));
            ps.setDate(idx++, java.sql.Date.valueOf(end));

            if (phongFilter != null && !phongFilter.equals("Tất cả"))
                ps.setString(idx++, phongFilter);

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

                lichChieuTheoPhong
                        .computeIfAbsent(item.getTenPhong(), k -> new ArrayList<>())
                        .add(item);
            }

            renderLichChieuCalendar();

        } catch (Exception e) {
            showError("Lỗi tải lịch", e.getMessage());
        }
    }

    // ================= Render lịch lên giao diện ====================
    private void renderLichChieuCalendar() {
        containerLichChieu.getChildren().clear();

        List<String> phongs = new ArrayList<>(lichChieuTheoPhong.keySet());
        Collections.sort(phongs);

        for (String phong : phongs)
            containerLichChieu.getChildren().add(createPhongRow(phong, lichChieuTheoPhong.get(phong)));

        if (phongs.isEmpty()) {
            Label lbl = new Label("Không có suất chiếu trong tuần này.");
            lbl.setStyle("-fx-text-fill:#888; -fx-padding:20;");
            containerLichChieu.getChildren().add(lbl);
        }
    }

    private HBox createPhongRow(String tenPhong, List<LichChieu> list) {
        HBox row = new HBox(0);
        row.setPrefHeight(80);
        row.setPrefWidth(120 + 150 * 7);
        row.setStyle("-fx-border-color:#ddd; -fx-border-width:0 0 1 0;");

        VBox phongCell = new VBox();
        phongCell.setPrefWidth(120);
        phongCell.setAlignment(Pos.CENTER);
        phongCell.setPadding(new Insets(5));
        phongCell.setStyle("-fx-background-color:#f5f5f5; -fx-border-color:#ddd; -fx-border-width:0 1 0 0;");
        phongCell.getChildren().add(new Label(tenPhong));

        row.getChildren().add(phongCell);

        for (int i = 0; i < 7; i++) {
            LocalDate ngay = currentWeekStart.plusDays(i);
            row.getChildren().add(createDayCell(ngay, list));
        }

        return row;
    }

    private VBox createDayCell(LocalDate ngay, List<LichChieu> list) {
        VBox cell = new VBox(2);
        cell.setPadding(new Insets(5));
        cell.setStyle("-fx-border-color:#eee; -fx-background-color:#fafafa;");

        List<LichChieu> today = list.stream()
                .filter(s -> s.getBatDauLuc().toLocalDate().equals(ngay))
                .sorted(Comparator.comparing(LichChieu::getBatDauLuc))
                .toList();

        if (today.isEmpty()) {
            Label empty = new Label("—");
            empty.setStyle("-fx-text-fill:#bbb;");
            empty.setAlignment(Pos.CENTER);
            cell.getChildren().add(empty);
        } else {
            for (LichChieu s : today)
                cell.getChildren().add(createSuatButton(s));
        }

        return cell;
    }

    private Button createSuatButton(LichChieu s) {
        Button btn = new Button(s.getBatDauLuc().toLocalTime().toString());
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setStyle("-fx-background-color:" + s.getMauSac() +
                "; -fx-text-fill:white; -fx-font-size:10; -fx-background-radius:3;");

        btn.setOnAction(e -> showSuatChieuDetail(s));
        return btn;
    }

    private void showSuatChieuDetail(LichChieu s) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Chi tiết suất chiếu");
        a.setHeaderText(s.getTenPhim());
        a.setContentText(
                "Phòng: " + s.getTenPhong() +
                        "\nThời gian: " + s.getBatDauLuc().format(DateTimeFormatter.ofPattern("HH:mm dd/MM")) +
                        " - " + s.getKetThucLuc().format(DateTimeFormatter.ofPattern("HH:mm dd/MM")) +
                        "\nĐịnh dạng: " + s.getDinhDang() +
                        "\nTrạng thái: " + s.getTrangThai()
        );
        a.showAndWait();
    }

    // ================= THÊM SUẤT CHIẾU — GIỮ NGUYÊN ======================
    private void showThemLichChieuDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/models/ThemLichChieuDialog.fxml"));
            loader.setController(this);
            VBox page = loader.load();

            dialogStage = new Stage();
            dialogStage.setTitle("Thêm suất chiếu");
            dialogStage.initModality(Modality.WINDOW_MODAL);

            Stage parent = (Stage) btnThemLichChieu.getScene().getWindow();
            dialogStage.initOwner(parent);

            dialogStage.setScene(new Scene(page));
            loadDialogComboBoxData();
            setupDialogListeners();
            dialogStage.showAndWait();

        } catch (Exception e) {
            showError("Lỗi mở dialog", e.getMessage());
        }
    }

    private void loadDialogComboBoxData() {
        phimMap.clear();
        ObservableList<String> phimList = FXCollections.observableArrayList();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT ma_phim, ten_phim FROM phim ORDER BY ten_phim")) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                phimMap.put(rs.getString("ten_phim"), rs.getInt("ma_phim"));
                phimList.add(rs.getString("ten_phim"));
            }
            cbPhim.setItems(phimList);

        } catch (Exception e) {
            showError("Lỗi phim", e.getMessage());
        }

        ObservableList<String> phongList = FXCollections.observableArrayList(cbPhongLichChieu.getItems());
        phongList.remove("Tất cả");
        cbPhong.setItems(phongList);

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT ten_dinh_dang FROM dinh_dang")) {
            ResultSet rs = ps.executeQuery();
            ObservableList<String> list = FXCollections.observableArrayList();
            while (rs.next()) list.add(rs.getString("ten_dinh_dang"));
            cbDinhDang.setItems(list);
        } catch (Exception e) {
            showError("Lỗi định dạng", e.getMessage());
        }
    }

    private void setupDialogListeners() {
        dpNgayChieu.valueProperty().addListener((a, b, c) -> checkConflict());
        txtGioBatDau.textProperty().addListener((a, b, c) -> {
            updateKetThucLuc();
            checkConflict();
        });
        cbPhong.valueProperty().addListener((a, b, c) -> checkConflict());

        if (btnHuy != null)
            btnHuy.setOnAction(e -> dialogStage.close());

        btnLuu.setOnAction(e -> saveSuatChieu());
    }

    private void updateKetThucLuc() {
        try {
            if (cbPhim.getValue() == null || txtGioBatDau.getText().isBlank()) {
                lblThoiGianKetThuc.setText("Thời gian kết thúc:");
                return;
            }

            int maPhim = phimMap.get(cbPhim.getValue());
            int dur = 0;

            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(
                         "SELECT thoi_luong_phut FROM phim WHERE ma_phim = ?")) {
                ps.setInt(1, maPhim);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) dur = rs.getInt(1);
            }

            LocalDate ngay = dpNgayChieu.getValue();
            LocalTime gio = LocalTime.parse(txtGioBatDau.getText(), DateTimeFormatter.ofPattern("HH:mm"));

            LocalDateTime ketThuc = LocalDateTime.of(ngay, gio).plusMinutes(dur);

            lblThoiGianKetThuc.setText("Thời gian kết thúc: " +
                    ketThuc.format(DateTimeFormatter.ofPattern("HH:mm dd/MM")));

        } catch (Exception ignore) {}
    }

    private void checkConflict() {
        lblXungDot.setText("");
        btnLuu.setDisable(true);

        try {
            if (cbPhim.getValue() == null || cbPhong.getValue() == null ||
                    dpNgayChieu.getValue() == null || txtGioBatDau.getText().isBlank()) {
                return;
            }

            LocalTime gioBD = LocalTime.parse(txtGioBatDau.getText(), DateTimeFormatter.ofPattern("HH:mm"));
            LocalDateTime batDauMoi = LocalDateTime.of(dpNgayChieu.getValue(), gioBD);

            int dur = 0;
            int maPhim = phimMap.get(cbPhim.getValue());

            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(
                         "SELECT thoi_luong_phut FROM phim WHERE ma_phim = ?")) {
                ps.setInt(1, maPhim);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) dur = rs.getInt(1);
            }

            LocalDateTime ketThucMoi = batDauMoi.plusMinutes(dur);

            String sql = """
                SELECT pm.ten_phim, sc.bat_dau_luc,
                       DATE_ADD(sc.bat_dau_luc, INTERVAL pm.thoi_luong_phut MINUTE)
                FROM suat_chieu sc
                JOIN phong ph ON sc.ma_phong = ph.ma_phong
                JOIN phim pm ON sc.ma_phim = pm.ma_phim
                WHERE ph.ten_phong = ?
                AND sc.bat_dau_luc < ? AND DATE_ADD(sc.bat_dau_luc, INTERVAL pm.thoi_luong_phut MINUTE) > ?
            """;

            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, cbPhong.getValue());
                ps.setTimestamp(2, java.sql.Timestamp.valueOf(ketThucMoi));
                ps.setTimestamp(3, java.sql.Timestamp.valueOf(batDauMoi));
                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    lblXungDot.setText("⚠ Xung đột với suất " + rs.getString(1));
                    lblXungDot.setStyle("-fx-text-fill:red;");
                    return;
                }
            }

            lblXungDot.setText("✔ Thời gian hợp lệ");
            lblXungDot.setStyle("-fx-text-fill:green;");
            btnLuu.setDisable(false);

        } catch (Exception e) {
            lblXungDot.setText("Lỗi kiểm tra!");
            lblXungDot.setStyle("-fx-text-fill:red;");
        }
    }

    private void saveSuatChieu() {
        try {
            int maPhim = phimMap.get(cbPhim.getValue());
            int maPhong = getMaByName("phong", "ma_phong", "ten_phong", cbPhong.getValue());
            int maDinhDang = getMaByName("dinh_dang", "ma_dinh_dang", "ten_dinh_dang", cbDinhDang.getValue());

            LocalTime gioBD = LocalTime.parse(txtGioBatDau.getText(), DateTimeFormatter.ofPattern("HH:mm"));
            LocalDateTime batDau = LocalDateTime.of(dpNgayChieu.getValue(), gioBD);

            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(
                         "INSERT INTO suat_chieu(ma_phim, ma_phong, ma_dinh_dang, bat_dau_luc, gia_co_ban, trang_thai) VALUES(?,?,?,?,0,'MO_BAN')")) {
                ps.setInt(1, maPhim);
                ps.setInt(2, maPhong);
                ps.setInt(3, maDinhDang);
                ps.setTimestamp(4, java.sql.Timestamp.valueOf(batDau));
                ps.executeUpdate();
            }

            dialogStage.close();
            loadLichChieuTuan();

            Alert a = new Alert(Alert.AlertType.INFORMATION);
            a.setContentText("Thêm suất chiếu thành công!");
            a.show();

        } catch (Exception e) {
            showError("Lỗi lưu", e.getMessage());
        }
    }

    private int getMaByName(String table, String maCol, String tenCol, String ten) throws Exception {
        String sql = "SELECT " + maCol + " FROM " + table + " WHERE " + tenCol + "=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, ten);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private void showError(String h, String m) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setHeaderText(h);
        a.setContentText(m);
        a.show();
    }
}
