package controllers;

import database.DBConnection;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.sql.*;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Locale;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import javafx.scene.control.TextField;
import javafx.scene.control.DatePicker;

public class HoaDonController {

    @FXML private TableView<HoaDonVM> tblHoaDon;
    @FXML private TableColumn<HoaDonVM, Number> colMaHD;
    @FXML private TableColumn<HoaDonVM, String> colNgay, colNhanVien, colKhach;
    @FXML private TableColumn<HoaDonVM, BigDecimal> colTongTien;
    @FXML private TableColumn<HoaDonVM, Void> colInHD;
    @FXML private DatePicker dpFrom, dpTo;
    @FXML private TextField txtMaHD;
    @FXML private Button btnTraCuuHD;

    @FXML
    private void initialize() {
        colMaHD.setCellValueFactory(d->d.getValue().maProperty());
        colNgay.setCellValueFactory(d->d.getValue().ngayProperty());
        colNhanVien.setCellValueFactory(d->d.getValue().nvProperty());
        colKhach.setCellValueFactory(d->d.getValue().khProperty());
        colTongTien.setCellValueFactory(new PropertyValueFactory<>("tong"));
        colTongTien.setCellFactory(tc -> new TableCell<>(){
            @Override protected void updateItem(BigDecimal v, boolean empty){
                super.updateItem(v, empty);
                setText(empty||v==null?null:String.format("%,.0f đ", v));
            }
        });
        addPrintButtonColumn();
        if (dpFrom != null && dpFrom.getValue()==null) dpFrom.setValue(LocalDate.now());
        if (dpTo != null && dpTo.getValue()==null) dpTo.setValue(LocalDate.now());
        if (btnTraCuuHD != null) btnTraCuuHD.setOnAction(e->loadHoaDon());
        loadHoaDon();
    }

    private void addPrintButtonColumn() {
        if (tblHoaDon == null || colInHD == null) return;
        colInHD.setCellFactory(col -> new TableCell<>() {
            private final Button btnIn = new Button("🖨 In");
            {
                btnIn.setOnAction(e -> {
                    HoaDonVM hd = getTableView().getItems().get(getIndex());
                    if (hd != null) {
                        try {
                            exportHoaDonPDF(hd);
                            info("✅ Đã xuất: HoadonPDF/HoaDon_" + hd.maProperty().get() + ".pdf");
                        } catch (Exception ex) {
                            showError("Lỗi in hóa đơn", ex.getMessage());
                        }
                    }
                });
                btnIn.setStyle("-fx-background-color:#2196f3; -fx-text-fill:white; -fx-font-weight:bold; -fx-background-radius:6;");
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btnIn);
            }
        });
    }

    private void loadHoaDon() {
        if (tblHoaDon == null) return;

        String sql = """
            SELECT dh.ma_don_hang,
                   dh.tao_luc,
                   COALESCE(tk.ho_ten, 'Khách lẻ') AS khach,
                   dh.tong_tien
            FROM don_hang dh
            LEFT JOIN khach_hang kh ON kh.ma_khach_hang = dh.ma_khach_hang
            LEFT JOIN tai_khoan tk   ON tk.ma_tai_khoan   = kh.ma_tai_khoan
            WHERE dh.tao_luc BETWEEN ? AND ?
              AND (? = 0 OR dh.ma_don_hang = ?)
            ORDER BY dh.tao_luc DESC
        """;

        LocalDate from = dpFrom != null && dpFrom.getValue()!=null ? dpFrom.getValue() : LocalDate.now();
        LocalDate to = dpTo != null && dpTo.getValue()!=null ? dpTo.getValue() : LocalDate.now();
        Timestamp tsFrom = Timestamp.valueOf(from.atStartOfDay());
        Timestamp tsTo = Timestamp.valueOf(LocalDateTime.of(to, java.time.LocalTime.MAX));

        int filterMa = 0;
        try {
            if (txtMaHD != null && txtMaHD.getText()!=null && !txtMaHD.getText().isBlank()) {
                filterMa = Integer.parseInt(txtMaHD.getText().trim());
            }
        } catch (NumberFormatException ignored){ filterMa = 0; }

        ObservableList<HoaDonVM> list = FXCollections.observableArrayList();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setTimestamp(1, tsFrom);
            ps.setTimestamp(2, tsTo);
            ps.setInt(3, filterMa);
            ps.setInt(4, filterMa);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int ma = rs.getInt("ma_don_hang");
                    Timestamp t = rs.getTimestamp("tao_luc");
                    String ngay = t.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
                            .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
                    String kh = rs.getString("khach");
                    String nv = "Quầy";
                    BigDecimal tong = rs.getBigDecimal("tong_tien");
                    list.add(new HoaDonVM(ma, ngay, nv, kh, tong));
                }
            }
        } catch (SQLException e) {
            showError("Lỗi tải hóa đơn", e.getMessage());
        }
        tblHoaDon.setItems(list);
    }

    private void exportHoaDonPDF(HoaDonVM hd) throws Exception {
        File dir = new File("HoadonPDF");
        if (!dir.exists()) dir.mkdirs();
        String fileName = "HoadonPDF/HoaDon_" + hd.maProperty().get() + ".pdf";
        Document document = new Document(PageSize.A5, 36, 36, 54, 36);
        PdfWriter.getInstance(document, new FileOutputStream(fileName));
        document.open();
        try { Image logo = Image.getInstance("src/Application/image/logo.png"); logo.scaleToFit(80, 80); logo.setAlignment(Element.ALIGN_LEFT); document.add(logo);} catch (Exception ignored) {}
        BaseFont bf = BaseFont.createFont("c:/windows/fonts/arial.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        Font titleFont = new Font(bf, 16, Font.BOLD, BaseColor.BLUE);
        Font textFont  = new Font(bf, 12, Font.NORMAL, BaseColor.BLACK);
        Font boldFont  = new Font(bf, 12, Font.BOLD, BaseColor.BLACK);
        NumberFormat nf = NumberFormat.getInstance(new Locale("vi","VN"));

        Paragraph title = new Paragraph("RẠP CHIẾU PHIM CINEMA 4U\n\nHÓA ĐƠN BÁN HÀNG", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
        document.add(new Paragraph("\n"));

        PdfPTable infoTable = new PdfPTable(2);
        infoTable.setWidthPercentage(100);
        infoTable.addCell(cell("Mã hóa đơn:", boldFont));
        infoTable.addCell(cell(String.valueOf(hd.maProperty().get()), textFont));
        infoTable.addCell(cell("Ngày lập:", boldFont));
        infoTable.addCell(cell(hd.ngayProperty().get(), textFont));
        infoTable.addCell(cell("Khách hàng:", boldFont));
        infoTable.addCell(cell(hd.khProperty().get(), textFont));
        infoTable.addCell(cell("Nhân viên:", boldFont));
        infoTable.addCell(cell(hd.nvProperty().get(), textFont));
        document.add(infoTable);

        PdfPTable totalTable = new PdfPTable(2);
        totalTable.setWidthPercentage(100);
        totalTable.setSpacingBefore(10f);
        PdfPCell totalLabelCell = new PdfPCell(new Phrase("TỔNG THANH TOÁN:", new Font(bf, 14, Font.BOLD, BaseColor.RED)));
        totalLabelCell.setBorder(Rectangle.TOP);
        totalLabelCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        totalLabelCell.setPaddingTop(8f);
        totalLabelCell.setPaddingBottom(8f);
        PdfPCell totalValueCell = new PdfPCell(new Phrase(nf.format(hd.tongProperty().get()) + " VNĐ", new Font(bf, 14, Font.BOLD, BaseColor.RED)));
        totalValueCell.setBorder(Rectangle.TOP);
        totalValueCell.setPaddingTop(8f);
        totalValueCell.setPaddingBottom(8f);
        totalTable.addCell(totalLabelCell);
        totalTable.addCell(totalValueCell);
        document.add(totalTable);

        document.add(new Paragraph("\n\nCảm ơn quý khách đã ủng hộ rạp!", textFont));
        document.close();
    }

    private PdfPCell cell(String text, Font font) {
        PdfPCell c = new PdfPCell(new Phrase(text, font));
        c.setBorder(Rectangle.NO_BORDER);
        return c;
    }
    private void info(String msg){ Alert a=new Alert(Alert.AlertType.INFORMATION, msg); a.setHeaderText(null); a.showAndWait(); }
    private void showError(String header, String msg){ Alert a=new Alert(Alert.AlertType.ERROR, msg); a.setHeaderText(header); a.showAndWait(); }

    public static class HoaDonVM {
        private final IntegerProperty ma = new SimpleIntegerProperty();
        private final StringProperty ngay = new SimpleStringProperty();
        private final StringProperty nv = new SimpleStringProperty();
        private final StringProperty kh = new SimpleStringProperty();
        private final ObjectProperty<BigDecimal> tong = new SimpleObjectProperty<>();
        public HoaDonVM(int ma, String ngay, String nv, String kh, BigDecimal tong){ this.ma.set(ma); this.ngay.set(ngay); this.nv.set(nv); this.kh.set(kh); this.tong.set(tong); }
        public IntegerProperty maProperty(){ return ma; }
        public StringProperty ngayProperty(){ return ngay; }
        public StringProperty nvProperty(){ return nv; }
        public StringProperty khProperty(){ return kh; }
        public ObjectProperty<BigDecimal> tongProperty(){ return tong; }
    }
}
