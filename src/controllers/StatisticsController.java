package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.*;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

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

    @FXML
    public void initialize() {
        // Khởi tạo ComboBox
        statisticTypeComboBox.setItems(FXCollections.observableArrayList(
            "Tất cả",
            "Theo phim",
            "Theo rạp chiếu",
            "Theo khách hàng",
            "Theo sản phẩm"
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
        // Xuất báo cáo ra file Excel hoặc PDF
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Xuất báo cáo");
        alert.setHeaderText("Chức năng đang phát triển");
        alert.setContentText("Báo cáo sẽ được xuất ra file Excel/PDF");
        alert.showAndWait();
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

        loadSummaryCards(fromDate, toDate);
        loadRevenueChart(fromDate, toDate);
        loadRevenuePieChart(fromDate, toDate);
        loadTopMoviesChart(fromDate, toDate);
        loadDetailTable(fromDate, toDate);
    }

    private void loadSummaryCards(LocalDate fromDate, LocalDate toDate) {
        try (Connection conn = DBConnection.getConnection()) {
            // Tổng doanh thu
            String revenueQuery = "SELECT COALESCE(SUM(TongTien), 0) as total FROM VePhim " +
                    "WHERE NgayDat BETWEEN ? AND ?";
            try (PreparedStatement ps = conn.prepareStatement(revenueQuery)) {
                ps.setDate(1, Date.valueOf(fromDate));
                ps.setDate(2, Date.valueOf(toDate));
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    double revenue = rs.getDouble("total");
                    totalRevenueLabel.setText(currencyFormat.format(revenue) + " VNĐ");
                }
            }

            // Tổng vé bán
            String ticketsQuery = "SELECT COUNT(*) as total FROM VePhim " +
                    "WHERE NgayDat BETWEEN ? AND ?";
            try (PreparedStatement ps = conn.prepareStatement(ticketsQuery)) {
                ps.setDate(1, Date.valueOf(fromDate));
                ps.setDate(2, Date.valueOf(toDate));
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    int tickets = rs.getInt("total");
                    totalTicketsLabel.setText(tickets + " vé");
                }
            }

            // Khách hàng mới
            String customersQuery = "SELECT COUNT(*) as total FROM KhachHang " +
                    "WHERE NgayDangKy BETWEEN ? AND ?";
            try (PreparedStatement ps = conn.prepareStatement(customersQuery)) {
                ps.setDate(1, Date.valueOf(fromDate));
                ps.setDate(2, Date.valueOf(toDate));
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    int customers = rs.getInt("total");
                    newCustomersLabel.setText(customers + " người");
                }
            }

            // Tổng suất chiếu
            String screeningsQuery = "SELECT COUNT(*) as total FROM SuatChieu " +
                    "WHERE NgayChieu BETWEEN ? AND ?";
            try (PreparedStatement ps = conn.prepareStatement(screeningsQuery)) {
                ps.setDate(1, Date.valueOf(fromDate));
                ps.setDate(2, Date.valueOf(toDate));
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    int screenings = rs.getInt("total");
                    totalScreeningsLabel.setText(screenings + " suất");
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Lỗi khi tải dữ liệu thống kê: " + e.getMessage());
        }
    }

    private void loadRevenueChart(LocalDate fromDate, LocalDate toDate) {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Doanh thu");

        try (Connection conn = DBConnection.getConnection()) {
            String query = "SELECT DATE(NgayDat) as date, SUM(TongTien) as revenue " +
                    "FROM VePhim WHERE NgayDat BETWEEN ? AND ? " +
                    "GROUP BY DATE(NgayDat) ORDER BY date";
            
            try (PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setDate(1, Date.valueOf(fromDate));
                ps.setDate(2, Date.valueOf(toDate));
                ResultSet rs = ps.executeQuery();

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");
                while (rs.next()) {
                    String date = rs.getDate("date").toLocalDate().format(formatter);
                    double revenue = rs.getDouble("revenue");
                    series.getData().add(new XYChart.Data<>(date, revenue));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        revenueChart.getData().clear();
        revenueChart.getData().add(series);
    }

    private void loadRevenuePieChart(LocalDate fromDate, LocalDate toDate) {
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();

        try (Connection conn = DBConnection.getConnection()) {
            String query = "SELECT 'Vé phim' as category, COALESCE(SUM(TongTien), 0) as revenue " +
                    "FROM VePhim WHERE NgayDat BETWEEN ? AND ? " +
                    "UNION ALL " +
                    "SELECT 'Sản phẩm', COALESCE(SUM(ThanhTien), 0) " +
                    "FROM ChiTietDonHang cdh " +
                    "JOIN DonHang dh ON cdh.MaDonHang = dh.MaDonHang " +
                    "WHERE dh.NgayDat BETWEEN ? AND ?";

            try (PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setDate(1, Date.valueOf(fromDate));
                ps.setDate(2, Date.valueOf(toDate));
                ps.setDate(3, Date.valueOf(fromDate));
                ps.setDate(4, Date.valueOf(toDate));
                ResultSet rs = ps.executeQuery();

                while (rs.next()) {
                    String category = rs.getString("category");
                    double revenue = rs.getDouble("revenue");
                    if (revenue > 0) {
                        pieData.add(new PieChart.Data(category + ": " + currencyFormat.format(revenue) + " VNĐ", revenue));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        revenuePieChart.setData(pieData);
    }

    private void loadTopMoviesChart(LocalDate fromDate, LocalDate toDate) {
        XYChart.Series<String, Number> series = new XYChart.Series<>();

        try (Connection conn = DBConnection.getConnection()) {
            String query = "SELECT p.TenPhim, SUM(vp.TongTien) as revenue " +
                    "FROM VePhim vp " +
                    "JOIN SuatChieu sc ON vp.MaSuatChieu = sc.MaSuatChieu " +
                    "JOIN Phim p ON sc.MaPhim = p.MaPhim " +
                    "WHERE vp.NgayDat BETWEEN ? AND ? " +
                    "GROUP BY p.MaPhim, p.TenPhim " +
                    "ORDER BY revenue DESC LIMIT 10";

            try (PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setDate(1, Date.valueOf(fromDate));
                ps.setDate(2, Date.valueOf(toDate));
                ResultSet rs = ps.executeQuery();

                while (rs.next()) {
                    String movieName = rs.getString("TenPhim");
                    double revenue = rs.getDouble("revenue");
                    // Rút gọn tên phim nếu quá dài
                    String shortName = movieName.length() > 20 ? movieName.substring(0, 17) + "..." : movieName;
                    series.getData().add(new XYChart.Data<>(shortName, revenue));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        topMoviesChart.getData().clear();
        topMoviesChart.getData().add(series);
    }

    private void loadDetailTable(LocalDate fromDate, LocalDate toDate) {
        ObservableList<StatisticDetail> data = FXCollections.observableArrayList();

        try (Connection conn = DBConnection.getConnection()) {
            String query = "SELECT DATE(vp.NgayDat) as date, p.TenPhim, " +
                    "COUNT(DISTINCT sc.MaSuatChieu) as screenings, " +
                    "COUNT(vp.MaVe) as tickets, " +
                    "SUM(vp.TongTien) as revenue, " +
                    "ROUND((COUNT(vp.MaVe) * 100.0 / (COUNT(DISTINCT sc.MaSuatChieu) * rc.SoGhe)), 2) as occupancy " +
                    "FROM VePhim vp " +
                    "JOIN SuatChieu sc ON vp.MaSuatChieu = sc.MaSuatChieu " +
                    "JOIN Phim p ON sc.MaPhim = p.MaPhim " +
                    "JOIN RapChieu rc ON sc.MaRap = rc.MaRap " +
                    "WHERE vp.NgayDat BETWEEN ? AND ? " +
                    "GROUP BY DATE(vp.NgayDat), p.MaPhim, p.TenPhim, rc.SoGhe " +
                    "ORDER BY date DESC";

            try (PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setDate(1, Date.valueOf(fromDate));
                ps.setDate(2, Date.valueOf(toDate));
                ResultSet rs = ps.executeQuery();

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                while (rs.next()) {
                    String date = rs.getDate("date").toLocalDate().format(formatter);
                    String movie = rs.getString("TenPhim");
                    int screenings = rs.getInt("screenings");
                    int tickets = rs.getInt("tickets");
                    String revenue = currencyFormat.format(rs.getDouble("revenue"));
                    String occupancy = percentFormat.format(rs.getDouble("occupancy")) + "%";

                    data.add(new StatisticDetail(date, movie, screenings, tickets, revenue, occupancy));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        detailTableView.setItems(data);
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Cảnh báo");
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

        public StatisticDetail(String date, String movie, int screenings, int tickets, String revenue, String occupancy) {
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