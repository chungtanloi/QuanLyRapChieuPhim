package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.LocalTime;

import java.io.File;
import java.io.FileOutputStream;
import java.sql.*;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;

import database.DBConnection;

public class StatisticsController {

    // Filter components
    @FXML private DatePicker fromDatePicker;
    @FXML private DatePicker toDatePicker;
    @FXML private ComboBox<String> statisticTypeComboBox;
    @FXML private Button filterButton;
    @FXML private Button exportButton;

    // Summary cards
    @FXML private Label totalRevenueLabel;
    @FXML private Label revenueChangeLabel;
    @FXML private Label totalTicketsLabel;
    @FXML private Label ticketsChangeLabel;
    @FXML private Label newCustomersLabel;
    @FXML private Label customersChangeLabel;
    @FXML private Label totalScreeningsLabel;
    @FXML private Label screeningsChangeLabel;

    // Charts
    @FXML private LineChart<String, Number> revenueChart;
    @FXML private PieChart revenuePieChart;
    @FXML private BarChart<String, Number> topMoviesChart;

    // Table
    @FXML private TableView<StatisticDetail> detailTableView;
    @FXML private TableColumn<StatisticDetail, String> dateColumn;
    @FXML private TableColumn<StatisticDetail, String> movieColumn;
    @FXML private TableColumn<StatisticDetail, Integer> screeningsColumn;
    @FXML private TableColumn<StatisticDetail, Integer> ticketsColumn;
    @FXML private TableColumn<StatisticDetail, String> revenueColumn;
    @FXML private TableColumn<StatisticDetail, String> occupancyColumn;

    private final DecimalFormat currencyFormat = new DecimalFormat("#,###");
    private final DecimalFormat percentFormat = new DecimalFormat("##.##");

    // Lưu dữ liệu để xuất PDF
    private double currentRevenue = 0;
    private int currentTickets = 0;
    private int currentCustomers = 0;
    private int currentScreenings = 0;

    @FXML
    public void initialize() {
        // Khởi tạo ComboBox
        statisticTypeComboBox.setItems(FXCollections.observableArrayList(
            "Tất cả",
            "Theo phim",
            "Theo phòng chiếu",
            "Theo sản phẩm",
            "Theo giờ chiếu"
        ));
        statisticTypeComboBox.setValue("Tất cả");

        // Đặt ngày mặc định (30 ngày trước đến hôm nay)
        toDatePicker.setValue(LocalDate.now());
        fromDatePicker.setValue(LocalDate.now().minusDays(30));

        // Khởi tạo table columns
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        movieColumn.setCellValueFactory(new PropertyValueFactory<>("movie"));
        screeningsColumn.setCellValueFactory(new PropertyValueFactory<>("screenings"));
        ticketsColumn.setCellValueFactory(new PropertyValueFactory<>("tickets"));
        revenueColumn.setCellValueFactory(new PropertyValueFactory<>("revenue"));
        occupancyColumn.setCellValueFactory(new PropertyValueFactory<>("occupancy"));

        // Tải dữ liệu ban đầu
        loadStatistics();
    }

    @FXML
    private void handleFilter() {
        loadStatistics();
    }

    @FXML
    private void handleExport() {
        try {
            // Tạo FileChooser
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Lưu báo cáo PDF");
            fileChooser.setInitialFileName("BaoCaoThongKe_" + 
                LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".pdf");
            fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PDF Files", "*.pdf")
            );

            File file = fileChooser.showSaveDialog(exportButton.getScene().getWindow());
            
            if (file != null) {
                exportToPDF(file);
                showSuccessAlert("Xuất báo cáo thành công!\nĐã lưu tại: " + file.getAbsolutePath());
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Lỗi khi xuất PDF: " + e.getMessage());
        }
    }

    private void exportToPDF(File file) throws Exception {
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, new FileOutputStream(file));
        document.open();

        // Tạo font tiếng Việt (sử dụng font mặc định hỗ trợ Unicode)
        BaseFont bf = BaseFont.createFont("c:/windows/fonts/arial.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        Font titleFont = new Font(bf, 18, Font.BOLD, BaseColor.RED);
        Font headerFont = new Font(bf, 14, Font.BOLD, BaseColor.BLACK);
        Font normalFont = new Font(bf, 11, Font.NORMAL, BaseColor.BLACK);
        Font boldFont = new Font(bf, 11, Font.BOLD, BaseColor.BLACK);

        // Tiêu đề
        Paragraph title = new Paragraph("BÁO CÁO THỐNG KÊ RẠP CHIẾU PHIM", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(10);
        document.add(title);

        // Thời gian báo cáo
        LocalDate fromDate = fromDatePicker.getValue();
        LocalDate toDate = toDatePicker.getValue();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        
        Paragraph period = new Paragraph(
            "Từ ngày: " + fromDate.format(formatter) + " - Đến ngày: " + toDate.format(formatter),
            normalFont
        );
        period.setAlignment(Element.ALIGN_CENTER);
        period.setSpacingAfter(20);
        document.add(period);

        // Loại thống kê
        Paragraph type = new Paragraph("Loại thống kê: " + statisticTypeComboBox.getValue(), boldFont);
        type.setSpacingAfter(15);
        document.add(type);

        // === PHẦN TỔNG QUAN ===
        Paragraph summaryTitle = new Paragraph("TỔNG QUAN", headerFont);
        summaryTitle.setSpacingAfter(10);
        document.add(summaryTitle);

        // Bảng tổng quan
        PdfPTable summaryTable = new PdfPTable(2);
        summaryTable.setWidthPercentage(100);
        summaryTable.setSpacingAfter(20);
        
        addSummaryRow(summaryTable, "Tổng doanh thu:", 
            currencyFormat.format(currentRevenue) + " VNĐ", normalFont, boldFont);
        addSummaryRow(summaryTable, "Tổng vé bán:", 
            currentTickets + " vé", normalFont, boldFont);
        addSummaryRow(summaryTable, "Khách hàng mới:", 
            currentCustomers + " người", normalFont, boldFont);
        addSummaryRow(summaryTable, "Tổng suất chiếu:", 
            currentScreenings + " suất", normalFont, boldFont);
        
        document.add(summaryTable);

        // === PHẦN CHI TIẾT ===
        Paragraph detailTitle = new Paragraph("CHI TIẾT THỐNG KÊ", headerFont);
        detailTitle.setSpacingAfter(10);
        document.add(detailTitle);

        // Bảng chi tiết
        ObservableList<StatisticDetail> data = detailTableView.getItems();
        
        if (data != null && !data.isEmpty()) {
            PdfPTable detailTable = new PdfPTable(6);
            detailTable.setWidthPercentage(100);
            detailTable.setWidths(new int[]{15, 30, 12, 12, 18, 13});

            // Header
            addTableHeader(detailTable, "Ngày", boldFont);
            addTableHeader(detailTable, "Phim/Mục", boldFont);
            addTableHeader(detailTable, "Suất chiếu", boldFont);
            addTableHeader(detailTable, "Vé/SL", boldFont);
            addTableHeader(detailTable, "Doanh thu", boldFont);
            addTableHeader(detailTable, "Tỷ lệ", boldFont);

            // Data rows
            for (StatisticDetail detail : data) {
                addTableCell(detailTable, detail.getDate(), normalFont);
                addTableCell(detailTable, detail.getMovie(), normalFont);
                addTableCell(detailTable, String.valueOf(detail.getScreenings()), normalFont);
                addTableCell(detailTable, String.valueOf(detail.getTickets()), normalFont);
                addTableCell(detailTable, detail.getRevenue(), normalFont);
                addTableCell(detailTable, detail.getOccupancy(), normalFont);
            }

            document.add(detailTable);
        }

        // Footer
        Paragraph footer = new Paragraph(
            "\n\nBáo cáo được tạo tự động vào: " + 
            LocalDate.now().format(formatter) + " " + 
            java.time.LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")),
            new Font(bf, 9, Font.ITALIC, BaseColor.GRAY)
        );
        footer.setAlignment(Element.ALIGN_RIGHT);
        document.add(footer);

        document.close();
    }

    private void addSummaryRow(PdfPTable table, String label, String value, Font labelFont, Font valueFont) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setPadding(8);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, valueFont));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setPadding(8);
        table.addCell(valueCell);
    }

    private void addTableHeader(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(8);
        table.addCell(cell);
    }

    private void addTableCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(6);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(cell);
    }

    private void loadStatistics() {
        LocalDate fromDate = fromDatePicker.getValue();
        LocalDate toDate = toDatePicker.getValue();

        if (fromDate == null || toDate == null) {
            showAlert("Vui lòng chọn khoảng thời gian");
            return;
        }

        if (fromDate.isAfter(toDate)) {
            showAlert("Ngày bắt đầu phải trước ngày kết thúc");
            return;
        }

        // Load dữ liệu dựa trên loại thống kê được chọn
        String selectedType = statisticTypeComboBox.getValue();
        
        loadSummaryCards(fromDate, toDate);
        loadRevenueChart(fromDate, toDate);
        loadRevenuePieChart(fromDate, toDate);
        loadTopMoviesChart(fromDate, toDate);
        
        // Load bảng chi tiết theo loại được chọn
        switch (selectedType) {
            case "Theo phim":
                loadTopMoviesTable(fromDate, toDate);
                break;
            case "Theo phòng chiếu":
                loadRoomRevenueTable(fromDate, toDate);
                break;
            case "Theo sản phẩm":
                loadProductRevenueTable(fromDate, toDate);
                break;
            case "Theo giờ chiếu":
                loadHourlyRevenueTable(fromDate, toDate);
                break;
            default:
                loadDetailTable(fromDate, toDate);
        }
    }

    /**
     * Sử dụng stored procedure sp_tong_quan_doanh_thu
     */
    private void loadSummaryCards(LocalDate fromDate, LocalDate toDate) {
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                showAlert("Không thể kết nối database!");
                return;
            }

            // Gọi stored procedure
            String sql = "{CALL sp_tong_quan_doanh_thu(?, ?)}";
            try (CallableStatement cs = conn.prepareCall(sql)) {
                cs.setDate(1, Date.valueOf(fromDate));
                cs.setDate(2, Date.valueOf(toDate));
                
                ResultSet rs = cs.executeQuery();
                if (rs.next()) {
                    // Dữ liệu kỳ hiện tại
                    currentRevenue = rs.getDouble("tong_doanh_thu");
                    currentTickets = rs.getInt("tong_ve_ban");
                    currentCustomers = rs.getInt("khach_hang_moi");
                    currentScreenings = rs.getInt("tong_suat_chieu");
                    
                    // Dữ liệu kỳ trước
                    double prevRevenue = rs.getDouble("doanh_thu_ky_truoc");
                    int prevTickets = rs.getInt("ve_ban_ky_truoc");
                    int prevCustomers = rs.getInt("khach_hang_ky_truoc");
                    int prevScreenings = rs.getInt("suat_chieu_ky_truoc");
                    
                    // Cập nhật UI
                    totalRevenueLabel.setText(currencyFormat.format(currentRevenue) + " VNĐ");
                    totalTicketsLabel.setText(currentTickets + " vé");
                    newCustomersLabel.setText(currentCustomers + " người");
                    totalScreeningsLabel.setText(currentScreenings + " suất");
                    
                    // Cập nhật % thay đổi
                    updateChangeLabel(revenueChangeLabel, currentRevenue, prevRevenue);
                    updateChangeLabel(ticketsChangeLabel, currentTickets, prevTickets);
                    updateChangeLabel(customersChangeLabel, currentCustomers, prevCustomers);
                    updateChangeLabel(screeningsChangeLabel, currentScreenings, prevScreenings);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Lỗi khi tải dữ liệu thống kê: " + e.getMessage());
        }
    }

    /**
     * Sử dụng stored procedure sp_doanh_thu_theo_ngay
     */
    private void loadRevenueChart(LocalDate fromDate, LocalDate toDate) {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Doanh thu");

        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) return;
            
            String sql = "{CALL sp_doanh_thu_theo_ngay(?, ?)}";
            try (CallableStatement cs = conn.prepareCall(sql)) {
                cs.setDate(1, Date.valueOf(fromDate));
                cs.setDate(2, Date.valueOf(toDate));
                
                ResultSet rs = cs.executeQuery();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");
                
                while (rs.next()) {
                    LocalDate date = rs.getDate("ngay").toLocalDate();
                    double revenue = rs.getDouble("doanh_thu");
                    series.getData().add(new XYChart.Data<>(date.format(formatter), revenue));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        revenueChart.getData().clear();
        revenueChart.getData().add(series);
    }

    /**
     * Sử dụng stored procedure sp_phan_bo_doanh_thu
     */
    private void loadRevenuePieChart(LocalDate fromDate, LocalDate toDate) {
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();

        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) return;
            
            String sql = "{CALL sp_phan_bo_doanh_thu(?, ?)}";
            try (CallableStatement cs = conn.prepareCall(sql)) {
                cs.setDate(1, Date.valueOf(fromDate));
                cs.setDate(2, Date.valueOf(toDate));
                
                ResultSet rs = cs.executeQuery();
                
                while (rs.next()) {
                    String loai = rs.getString("loai");
                    double doanhthu = rs.getDouble("doanh_thu");
                    double tyLe = rs.getDouble("ty_le_phan_tram");
                    
                    if (doanhthu > 0) {
                        String label = String.format("%s: %s VNĐ (%.1f%%)", 
                            loai, currencyFormat.format(doanhthu), tyLe);
                        pieData.add(new PieChart.Data(label, doanhthu));
                    }
                }
                
                if (pieData.isEmpty()) {
                    pieData.add(new PieChart.Data("Chưa có dữ liệu", 1));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        revenuePieChart.setData(pieData);
    }

    /**
     * Sử dụng stored procedure sp_top_phim_doanh_thu
     */
    private void loadTopMoviesChart(LocalDate fromDate, LocalDate toDate) {
        XYChart.Series<String, Number> series = new XYChart.Series<>();

        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) return;
            
            String sql = "{CALL sp_top_phim_doanh_thu(?, ?, ?)}";
            try (CallableStatement cs = conn.prepareCall(sql)) {
                cs.setDate(1, Date.valueOf(fromDate));
                cs.setDate(2, Date.valueOf(toDate));
                cs.setInt(3, 10); // Top 10
                
                ResultSet rs = cs.executeQuery();

                while (rs.next()) {
                    String movieName = rs.getString("ten_phim");
                    double revenue = rs.getDouble("doanh_thu");
                    String shortName = movieName.length() > 20 ? 
                        movieName.substring(0, 17) + "..." : movieName;
                    series.getData().add(new XYChart.Data<>(shortName, revenue));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        topMoviesChart.getData().clear();
        topMoviesChart.getData().add(series);
    }

    /**
     * Sử dụng stored procedure sp_chi_tiet_doanh_thu
     */
    private void loadDetailTable(LocalDate fromDate, LocalDate toDate) {
        ObservableList<StatisticDetail> data = FXCollections.observableArrayList();

        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) return;
            
            String sql = "{CALL sp_chi_tiet_doanh_thu(?, ?)}";
            try (CallableStatement cs = conn.prepareCall(sql)) {
                cs.setDate(1, Date.valueOf(fromDate));
                cs.setDate(2, Date.valueOf(toDate));
                
                ResultSet rs = cs.executeQuery();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                
                while (rs.next()) {
                    String date = rs.getDate("ngay").toLocalDate().format(formatter);
                    String movie = rs.getString("ten_phim");
                    int screenings = rs.getInt("so_suat_chieu");
                    int tickets = rs.getInt("so_ve_ban");
                    String revenue = currencyFormat.format(rs.getDouble("doanh_thu"));
                    String occupancy = percentFormat.format(rs.getDouble("ty_le_lap_day")) + "%";

                    data.add(new StatisticDetail(date, movie, screenings, tickets, revenue, occupancy));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        detailTableView.setItems(data);
    }

    /**
     * Bảng thống kê theo phim (sử dụng sp_top_phim_doanh_thu)
     */
    private void loadTopMoviesTable(LocalDate fromDate, LocalDate toDate) {
        ObservableList<StatisticDetail> data = FXCollections.observableArrayList();

        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) return;
            
            String sql = "{CALL sp_top_phim_doanh_thu(?, ?, ?)}";
            try (CallableStatement cs = conn.prepareCall(sql)) {
                cs.setDate(1, Date.valueOf(fromDate));
                cs.setDate(2, Date.valueOf(toDate));
                cs.setInt(3, 50); // Lấy nhiều hơn cho bảng
                
                ResultSet rs = cs.executeQuery();
                
                while (rs.next()) {
                    String movie = rs.getString("ten_phim");
                    int screenings = rs.getInt("so_suat_chieu");
                    int tickets = rs.getInt("so_ve_ban");
                    String revenue = currencyFormat.format(rs.getDouble("doanh_thu"));
                    String occupancy = percentFormat.format(rs.getDouble("ty_le_lap_day_tb")) + "%";

                    data.add(new StatisticDetail("-", movie, screenings, tickets, revenue, occupancy));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        detailTableView.setItems(data);
    }

    /**
     * Bảng thống kê theo phòng (sử dụng sp_doanh_thu_theo_phong)
     */
    private void loadRoomRevenueTable(LocalDate fromDate, LocalDate toDate) {
        ObservableList<StatisticDetail> data = FXCollections.observableArrayList();

        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) return;
            
            String sql = "{CALL sp_doanh_thu_theo_phong(?, ?)}";
            try (CallableStatement cs = conn.prepareCall(sql)) {
                cs.setDate(1, Date.valueOf(fromDate));
                cs.setDate(2, Date.valueOf(toDate));
                
                ResultSet rs = cs.executeQuery();
                
                while (rs.next()) {
                    String room = rs.getString("ten_phong");
                    int screenings = rs.getInt("so_suat_chieu");
                    int tickets = rs.getInt("so_ve_ban");
                    String revenue = currencyFormat.format(rs.getDouble("doanh_thu"));
                    String occupancy = percentFormat.format(rs.getDouble("ty_le_lap_day_tb")) + "%";

                    data.add(new StatisticDetail("-", room, screenings, tickets, revenue, occupancy));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        detailTableView.setItems(data);
    }

    /**
     * Bảng thống kê sản phẩm (sử dụng sp_top_san_pham)
     */
    private void loadProductRevenueTable(LocalDate fromDate, LocalDate toDate) {
        ObservableList<StatisticDetail> data = FXCollections.observableArrayList();

        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) return;
            
            String sql = "{CALL sp_top_san_pham(?, ?, ?)}";
            try (CallableStatement cs = conn.prepareCall(sql)) {
                cs.setDate(1, Date.valueOf(fromDate));
                cs.setDate(2, Date.valueOf(toDate));
                cs.setInt(3, 50);
                
                ResultSet rs = cs.executeQuery();
                
                while (rs.next()) {
                    String loai = rs.getString("loai");
                    String ten = rs.getString("ten");
                    int soLuong = rs.getInt("so_luong_ban");
                    String revenue = currencyFormat.format(rs.getDouble("doanh_thu"));

                    data.add(new StatisticDetail("-", loai + ": " + ten, 0, soLuong, revenue, "-"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        detailTableView.setItems(data);
    }

    /**
     * Bảng thống kê theo giờ (sử dụng sp_doanh_thu_theo_gio)
     */
    private void loadHourlyRevenueTable(LocalDate fromDate, LocalDate toDate) {
        ObservableList<StatisticDetail> data = FXCollections.observableArrayList();

        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) return;
            
            String sql = "{CALL sp_doanh_thu_theo_gio(?, ?)}";
            try (CallableStatement cs = conn.prepareCall(sql)) {
                cs.setDate(1, Date.valueOf(fromDate));
                cs.setDate(2, Date.valueOf(toDate));
                
                ResultSet rs = cs.executeQuery();
                
                while (rs.next()) {
                    int gio = rs.getInt("gio_chieu");
                    int screenings = rs.getInt("so_suat_chieu");
                    int tickets = rs.getInt("so_ve_ban");
                    String revenue = currencyFormat.format(rs.getDouble("doanh_thu"));
                    String occupancy = percentFormat.format(rs.getDouble("ty_le_lap_day_tb")) + "%";

                    data.add(new StatisticDetail("-", gio + ":00 - " + (gio+1) + ":00", 
                        screenings, tickets, revenue, occupancy));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        detailTableView.setItems(data);
    }

    private void updateChangeLabel(Label label, double current, double previous) {
        if (previous == 0) {
            label.setText("Chưa có dữ liệu kỳ trước");
            label.setStyle("-fx-text-fill: #666; -fx-font-size: 11;");
            return;
        }
        
        double change = ((current - previous) / previous) * 100;
        String arrow = change >= 0 ? "↑" : "↓";
        String color = change >= 0 ? "#4CAF50" : "#f44336";
        
        label.setText(String.format("%s %.1f%% so với kỳ trước", arrow, Math.abs(change)));
        label.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 11;");
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Cảnh báo");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccessAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Thành công");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Inner class cho dữ liệu bảng
    public static class StatisticDetail {
        private final String date;
        private final String movie;
        private final int screenings;
        private final int tickets;
        private final String revenue;
        private final String occupancy;

        public StatisticDetail(String date, String movie, int screenings, int tickets, 
                              String revenue, String occupancy) {
            this.date = date;
            this.movie = movie;
            this.screenings = screenings;
            this.tickets = tickets;
            this.revenue = revenue;
            this.occupancy = occupancy;
        }

        public String getDate() { return date; }
        public String getMovie() { return movie; }
        public int getScreenings() { return screenings; }
        public int getTickets() { return tickets; }
        public String getRevenue() { return revenue; }
        public String getOccupancy() { return occupancy; }
    }
}