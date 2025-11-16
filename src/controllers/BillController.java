package controllers;

import database.DBConnection;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

import java.io.File;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.sql.*;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Optional;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import javafx.scene.control.TextField;

public class BillController {

    // ================== FXML ==================
    @FXML private TableView<HoaDonVM> tblHoaDon;
    @FXML private TableColumn<HoaDonVM, Number> colMaHD;
    @FXML private TableColumn<HoaDonVM, String> colNgay, colNhanVien, colKhach;
    @FXML private TableColumn<HoaDonVM, BigDecimal> colTongTien;
    @FXML private TableColumn<HoaDonVM, String> colTrangThai;
    @FXML private TableColumn<HoaDonVM, Void> colThaoTac;

    @FXML private DatePicker dpFrom, dpTo;
    @FXML private TextField txtTimKiem;
    @FXML private ComboBox<String> cboTrangThai;
    @FXML private Label lblTongHoaDon, lblTongDoanhThu;

    private final ObservableList<HoaDonVM> data = FXCollections.observableArrayList();
    private final DateTimeFormatter DATE_TIME_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML
    public void initialize() {
        setupTableColumns();
        setupActionButtons();
        setupFilters();
        tblHoaDon.setItems(data);
        refreshData();
    }

    // ================== TABLE SETUP ==================
    private void setupTableColumns() {
        colMaHD.setCellValueFactory(d -> d.getValue().maProperty());
        colNgay.setCellValueFactory(d -> d.getValue().ngayProperty());
        colNhanVien.setCellValueFactory(d -> d.getValue().nvProperty());
        colKhach.setCellValueFactory(d -> d.getValue().khProperty());
        colTongTien.setCellValueFactory(d -> d.getValue().tongProperty());
        colTrangThai.setCellValueFactory(d -> d.getValue().trangThaiProperty());

        colTongTien.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal v, boolean empty) {
                super.updateItem(v, empty);
                setText(empty || v == null
                        ? null
                        : String.format("%,.0f đ", v));
            }
        });
    }

    private void setupFilters() {
        if (dpFrom.getValue() == null) dpFrom.setValue(LocalDate.now().minusMonths(1));
        if (dpTo.getValue() == null) dpTo.setValue(LocalDate.now());

        cboTrangThai.setItems(FXCollections.observableArrayList(
                "Tất cả", "Đã thanh toán", "Đã hủy", "Chờ xử lý"
        ));
        cboTrangThai.setValue("Tất cả");
    }

    @FXML
    public void refreshData() {
        loadHoaDon();
        updateStatistics();
    }

    @FXML
    public void handleTimKiem() { loadHoaDon(); }

    // ================== LOAD DATA ==================
    private void loadHoaDon() {
        data.clear();

        StringBuilder sql = new StringBuilder("""
            SELECT dh.ma_don_hang, dh.tao_luc,
                   COALESCE(tkKH.ho_ten, 'Khách lẻ') AS khach,
                   COALESCE(tkNV.ho_ten, 'Hệ thống') AS nhanvien,
                   dh.tong_tien,
                   CASE dh.trang_thai
                        WHEN 1 THEN 'Đã thanh toán'
                        WHEN 0 THEN 'Đã hủy'
                        ELSE 'Chờ xử lý'
                   END AS trang_thai
            FROM don_hang dh
            LEFT JOIN khach_hang kh ON kh.ma_khach_hang = dh.ma_khach_hang
            LEFT JOIN tai_khoan tkKH ON tkKH.ma_tai_khoan = kh.ma_tai_khoan
            LEFT JOIN nhan_vien nv ON nv.ma_nhan_vien = dh.ma_nhan_vien
            LEFT JOIN tai_khoan tkNV ON tkNV.ma_tai_khoan = nv.ma_tai_khoan
            WHERE dh.tao_luc BETWEEN ? AND ?
        """);

        if (!txtTimKiem.getText().isBlank()) {
            sql.append(" AND (dh.ma_don_hang LIKE ? OR tkKH.ho_ten LIKE ? OR tkNV.ho_ten LIKE ?)");
        }

        String trangThai = cboTrangThai.getValue();
        if (!"Tất cả".equals(trangThai)) {
            sql.append(" AND dh.trang_thai = ")
               .append("Đã thanh toán".equals(trangThai) ? 1 :
                       "Đã hủy".equals(trangThai) ? 0 : 2);
        }

        sql.append(" ORDER BY dh.tao_luc DESC");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            int idx = 1;
            ps.setTimestamp(idx++, Timestamp.valueOf(dpFrom.getValue().atStartOfDay()));
            ps.setTimestamp(idx++, Timestamp.valueOf(LocalDateTime.of(dpTo.getValue(), java.time.LocalTime.MAX)));

            if (!txtTimKiem.getText().isBlank()) {
                String kw = "%" + txtTimKiem.getText().trim() + "%";
                ps.setString(idx++, kw);
                ps.setString(idx++, kw);
                ps.setString(idx++, kw);
            }

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                data.add(new HoaDonVM(
                        rs.getInt("ma_don_hang"),
                        rs.getTimestamp("tao_luc").toInstant().atZone(ZoneId.systemDefault())
                                .toLocalDateTime().format(DATE_TIME_FMT),
                        rs.getString("khach"),
                        rs.getString("nhanvien"),
                        rs.getBigDecimal("tong_tien"),
                        rs.getString("trang_thai")
                ));
            }

        } catch (Exception e) {
            showError("Không thể tải dữ liệu hóa đơn", e);
        }
    }

    private void updateStatistics() {
    String sql = """
        SELECT 
            COUNT(*) AS tong,
            fn_tinh_doanh_thu(DATE(?), DATE(?)) AS doanh_thu
        FROM don_hang
        WHERE DATE(tao_luc) BETWEEN DATE(?) AND DATE(?)
    """;

    try (Connection conn = DBConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {

        Date fromDate = Date.valueOf(dpFrom.getValue());
        Date toDate = Date.valueOf(dpTo.getValue());
        
        ps.setDate(1, fromDate);
        ps.setDate(2, toDate);
        ps.setDate(3, fromDate);
        ps.setDate(4, toDate);

        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            lblTongHoaDon.setText(String.valueOf(rs.getInt("tong")));
            lblTongDoanhThu.setText(String.format("%,.0f đ",
                    rs.getBigDecimal("doanh_thu") != null ? rs.getBigDecimal("doanh_thu") : BigDecimal.ZERO));
        }

    } catch (Exception e) {
        showError("Không thể cập nhật thống kê", e);
    }
}

    // ================== ACTION BUTTON ==================
    private void setupActionButtons() {
        colThaoTac.setCellFactory(col -> new TableCell<>() {
            private final Button btnPrint = createButton("In", "#16a34a");
            private final Button btnDelete = createButton("Xóa", "#ef4444");
            private final HBox box = new HBox(8, btnPrint, btnDelete);

            {
                box.setAlignment(Pos.CENTER);

                btnPrint.setOnAction(e -> {
                    HoaDonVM row = getTableRow().getItem();
                    if (row != null) {
                        try {
                            exportHoaDonPDF(row);
                            showInfo("Đã xuất PDF hóa đơn #" + row.getMa());
                        } catch (Exception ex) {
                            showError("Lỗi khi xuất PDF", ex);
                        }
                    }
                });

                btnDelete.setOnAction(e -> {
                    HoaDonVM row = getTableRow().getItem();
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
        Button b = new Button(text);
        b.setStyle(
                "-fx-background-color:" + color +
                ";-fx-text-fill:white;" +
                "-fx-padding:4 10;" +
                "-fx-background-radius:6;" +
                "-fx-cursor:hand;"
        );
        return b;
    }

    // ================== DELETE ==================
    private void confirmDelete(HoaDonVM row) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setTitle("Xóa hóa đơn");
        a.setHeaderText("Bạn muốn xóa hóa đơn #" + row.getMa());
        a.showAndWait().ifPresent(res -> {
            if (res == ButtonType.OK) deleteHoaDon(row);
        });
    }

    private void deleteHoaDon(HoaDonVM row) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM don_hang WHERE ma_don_hang=?")) {

            ps.setInt(1, row.getMa());
            ps.executeUpdate();
            refreshData();
            showInfo("Đã xóa hóa đơn #" + row.getMa());

        } catch (Exception e) {
            showError("Không thể xóa hóa đơn (liên quan vé/combo)", e);
        }
    }

    // ================== EXPORT PDF ==================
    private void exportHoaDonPDF(HoaDonVM hd) throws Exception {
        File dir = new File("HoadonPDF");
        if (!dir.exists()) dir.mkdirs();

        String fileName = "HoadonPDF/HoaDon_" + hd.getMa() + ".pdf";

        Document doc = new Document(PageSize.A5, 36, 36, 54, 36);
        PdfWriter.getInstance(doc, new FileOutputStream(fileName));
        doc.open();

        // FONT
        BaseFont bf = BaseFont.createFont("c:/windows/fonts/arial.ttf",
                BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        Font title = new Font(bf, 16, Font.BOLD, BaseColor.BLUE);
        Font text = new Font(bf, 12, Font.NORMAL);
        Font bold = new Font(bf, 12, Font.BOLD);
        NumberFormat nf = NumberFormat.getInstance(new Locale("vi", "VN"));

        // TITLE
        Paragraph p = new Paragraph("HÓA ĐƠN BÁN HÀNG\n\n", title);
        p.setAlignment(Element.ALIGN_CENTER);
        doc.add(p);

        // ================== THÔNG TIN CƠ BẢN ==================
        PdfPTable info = new PdfPTable(2);
        info.setWidthPercentage(100);
        info.addCell(cell("Mã HD:", bold));
        info.addCell(cell(String.valueOf(hd.getMa()), text));
        info.addCell(cell("Ngày lập:", bold));
        info.addCell(cell(hd.getNgay(), text));
        info.addCell(cell("Khách hàng:", bold));
        info.addCell(cell(hd.getKhach(), text));
        info.addCell(cell("Nhân viên:", bold));
        info.addCell(cell(hd.getNhanVien(), text));
        info.addCell(cell("Trạng thái:", bold));
        info.addCell(cell(hd.getTrangThai(), text));
        doc.add(info);

        doc.add(new Paragraph("\n"));

        // ================== LOAD CHI TIẾT HÓA ĐƠN ==================
        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.addCell(cell("Sản phẩm", bold));
        table.addCell(cell("SL", bold));
        table.addCell(cell("Giá", bold));
        table.addCell(cell("T.Tiền", bold));

        try (Connection conn = DBConnection.getConnection()) {

            // ===== VÉ PHIM =====
            PreparedStatement ps1 = conn.prepareStatement("""
                SELECT p.ten_phim, sc.bat_dau_luc, v.gia_ban
                FROM don_ve dv
                JOIN ve v ON v.ma_ve = dv.ma_ve
                JOIN suat_chieu sc ON sc.ma_suat_chieu = v.ma_suat_chieu
                JOIN phim p ON p.ma_phim = sc.ma_phim
                WHERE dv.ma_don_hang=?
            """);
            ps1.setInt(1, hd.getMa());
            ResultSet rs1 = ps1.executeQuery();
            while (rs1.next()) {
                String ten = rs1.getString("ten_phim");
                BigDecimal gia = rs1.getBigDecimal("gia_ban");

                table.addCell(cell(ten, text));
                table.addCell(cell("1", text));
                table.addCell(cell(nf.format(gia), text));
                table.addCell(cell(nf.format(gia), text));
            }

            // ===== COMBO =====
            PreparedStatement ps2 = conn.prepareStatement("""
                SELECT c.ten_combo, dc.so_luong, dc.gia_ban
                FROM don_combo dc
                JOIN combo c ON c.ma_combo = dc.ma_combo
                WHERE dc.ma_don_hang=?
            """);
            ps2.setInt(1, hd.getMa());
            ResultSet rs2 = ps2.executeQuery();

            while (rs2.next()) {
                String ten = rs2.getString("ten_combo");
                int sl = rs2.getInt("so_luong");
                BigDecimal gia = rs2.getBigDecimal("gia_ban");
                BigDecimal tt = gia.multiply(new BigDecimal(sl));

                table.addCell(cell(ten, text));
                table.addCell(cell(String.valueOf(sl), text));
                table.addCell(cell(nf.format(gia), text));
                table.addCell(cell(nf.format(tt), text));
            }

        }

        doc.add(table);

        // ================== TỔNG TIỀN ==================
        PdfPTable total = new PdfPTable(2);
        total.setWidthPercentage(100);
        PdfPCell L = new PdfPCell(new Phrase("TỔNG TIỀN:", bold));
        L.setBorder(Rectangle.TOP);
        L.setHorizontalAlignment(Element.ALIGN_RIGHT);
        PdfPCell R = new PdfPCell(new Phrase(nf.format(hd.getTong()) + " đ", bold));
        R.setBorder(Rectangle.TOP);

        total.addCell(L);
        total.addCell(R);

        doc.add(total);

        doc.add(new Paragraph("\nCảm ơn quý khách!", text));

        doc.close();
    }

    private PdfPCell cell(String text, Font font) {
        PdfPCell c = new PdfPCell(new Phrase(text, font));
        c.setBorder(Rectangle.NO_BORDER);
        return c;
    }

    // ================== ALERT ==================
    private void showError(String msg, Exception e) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Lỗi");
        a.setHeaderText(msg);
        a.setContentText(e != null ? e.getMessage() : null);
        a.showAndWait();
    }

    private void showInfo(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    // ================== MODEL ==================
    public static class HoaDonVM {
        private final IntegerProperty ma = new SimpleIntegerProperty();
        private final StringProperty ngay = new SimpleStringProperty();
        private final StringProperty kh = new SimpleStringProperty();
        private final StringProperty nv = new SimpleStringProperty();
        private final ObjectProperty<BigDecimal> tong = new SimpleObjectProperty<>();
        private final StringProperty trangThai = new SimpleStringProperty();

        public HoaDonVM(int ma, String ngay, String kh, String nv, BigDecimal tong, String tt) {
            this.ma.set(ma);
            this.ngay.set(ngay);
            this.kh.set(kh);
            this.nv.set(nv);
            this.tong.set(tong);
            this.trangThai.set(tt);
        }

        public int getMa() { return ma.get(); }
        public String getNgay() { return ngay.get(); }
        public String getKhach() { return kh.get(); }
        public String getNhanVien() { return nv.get(); }
        public BigDecimal getTong() { return tong.get(); }
        public String getTrangThai() { return trangThai.get(); }

        public IntegerProperty maProperty() { return ma; }
        public StringProperty ngayProperty() { return ngay; }
        public StringProperty khProperty() { return kh; }
        public StringProperty nvProperty() { return nv; }
        public ObjectProperty<BigDecimal> tongProperty() { return tong; }
        public StringProperty trangThaiProperty() { return trangThai; }
    }
}