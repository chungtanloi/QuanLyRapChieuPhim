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
            "Theo phòng chiếu",
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
            if (conn == null) {
                showAlert("Không thể kết nối database!");
                return;
            }

            // 1. Tổng doanh thu (từ đơn hàng đã thanh toán)
            String revenueQuery = "SELECT COALESCE(SUM(tong_tien), 0) as total FROM don_hang " +
                    "WHERE trang_thai = 'DA_THANH_TOAN' AND DATE(dat_luc) BETWEEN ? AND ?";
            try (PreparedStatement ps = conn.prepareStatement(revenueQuery)) {
                ps.setDate(1, Date.valueOf(fromDate));
                ps.setDate(2, Date.valueOf(toDate));
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    double revenue = rs.getDouble("total");
                    totalRevenueLabel.setText(currencyFormat.format(revenue) + " VNĐ");
                    
                    // Tính % thay đổi so với kỳ trước
                    double previousRevenue = getPreviousPeriodRevenue(conn, fromDate, toDate);
                    updateChangeLabel(revenueChangeLabel, revenue, previousRevenue);
                }
            }

            // 2. Tổng vé đã bán
            String ticketsQuery = "SELECT COUNT(DISTINCT dv.ma_ve) as total " +
                    "FROM don_ve dv " +
                    "JOIN don_hang dh ON dv.ma_don_hang = dh.ma_don_hang " +
                    "WHERE dh.trang_thai = 'DA_THANH_TOAN' AND DATE(dh.dat_luc) BETWEEN ? AND ?";
            try (PreparedStatement ps = conn.prepareStatement(ticketsQuery)) {
                ps.setDate(1, Date.valueOf(fromDate));
                ps.setDate(2, Date.valueOf(toDate));
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    int tickets = rs.getInt("total");
                    totalTicketsLabel.setText(tickets + " vé");
                    
                    int previousTickets = getPreviousPeriodTickets(conn, fromDate, toDate);
                    updateChangeLabel(ticketsChangeLabel, tickets, previousTickets);
                }
            }

            // 3. Khách hàng mới
            String customersQuery = "SELECT COUNT(*) as total FROM khach_hang " +
                    "WHERE DATE(tao_luc) BETWEEN ? AND ?";
            try (PreparedStatement ps = conn.prepareStatement(customersQuery)) {
                ps.setDate(1, Date.valueOf(fromDate));
                ps.setDate(2, Date.valueOf(toDate));
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    int customers = rs.getInt("total");
                    newCustomersLabel.setText(customers + " người");
                    
                    int previousCustomers = getPreviousPeriodCustomers(conn, fromDate, toDate);
                    updateChangeLabel(customersChangeLabel, customers, previousCustomers);
                }
            }

            // 4. Tổng suất chiếu
            String screeningsQuery = "SELECT COUNT(*) as total FROM suat_chieu " +
                    "WHERE DATE(bat_dau_luc) BETWEEN ? AND ? AND trang_thai != 'HUY'";
            try (PreparedStatement ps = conn.prepareStatement(screeningsQuery)) {
                ps.setDate(1, Date.valueOf(fromDate));
                ps.setDate(2, Date.valueOf(toDate));
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    int screenings = rs.getInt("total");
                    totalScreeningsLabel.setText(screenings + " suất");
                    
                    int previousScreenings = getPreviousPeriodScreenings(conn, fromDate, toDate);
                    updateChangeLabel(screeningsChangeLabel, screenings, previousScreenings);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Lỗi khi tải dữ liệu thống kê: " + e.getMessage());
        }
    }

    private void updateChangeLabel(Label label, double current, double previous) {
        if (previous == 0) {
            label.setText("Chưa có dữ liệu kỳ trước");
            return;
        }
        
        double change = ((current - previous) / previous) * 100;
        String arrow = change >= 0 ? "↑" : "↓";
        String color = change >= 0 ? "#4CAF50" : "#f44336";
        
        label.setText(String.format("%s %.1f%% so với kỳ trước", arrow, Math.abs(change)));
        label.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 11;");
    }

    private double getPreviousPeriodRevenue(Connection conn, LocalDate fromDate, LocalDate toDate) throws SQLException {
        long daysDiff = toDate.toEpochDay() - fromDate.toEpochDay();
        LocalDate prevFrom = fromDate.minusDays(daysDiff + 1);
        LocalDate prevTo = fromDate.minusDays(1);
        
        String query = "SELECT COALESCE(SUM(tong_tien), 0) as total FROM don_hang " +
                "WHERE trang_thai = 'DA_THANH_TOAN' AND DATE(dat_luc) BETWEEN ? AND ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setDate(1, Date.valueOf(prevFrom));
            ps.setDate(2, Date.valueOf(prevTo));
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getDouble("total") : 0;
        }
    }

    private int getPreviousPeriodTickets(Connection conn, LocalDate fromDate, LocalDate toDate) throws SQLException {
        long daysDiff = toDate.toEpochDay() - fromDate.toEpochDay();
        LocalDate prevFrom = fromDate.minusDays(daysDiff + 1);
        LocalDate prevTo = fromDate.minusDays(1);
        
        String query = "SELECT COUNT(DISTINCT dv.ma_ve) as total " +
                "FROM don_ve dv JOIN don_hang dh ON dv.ma_don_hang = dh.ma_don_hang " +
                "WHERE dh.trang_thai = 'DA_THANH_TOAN' AND DATE(dh.dat_luc) BETWEEN ? AND ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setDate(1, Date.valueOf(prevFrom));
            ps.setDate(2, Date.valueOf(prevTo));
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getInt("total") : 0;
        }
    }

    private int getPreviousPeriodCustomers(Connection conn, LocalDate fromDate, LocalDate toDate) throws SQLException {
        long daysDiff = toDate.toEpochDay() - fromDate.toEpochDay();
        LocalDate prevFrom = fromDate.minusDays(daysDiff + 1);
        LocalDate prevTo = fromDate.minusDays(1);
        
        String query = "SELECT COUNT(*) as total FROM khach_hang WHERE DATE(tao_luc) BETWEEN ? AND ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setDate(1, Date.valueOf(prevFrom));
            ps.setDate(2, Date.valueOf(prevTo));
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getInt("total") : 0;
        }
    }

    private int getPreviousPeriodScreenings(Connection conn, LocalDate fromDate, LocalDate toDate) throws SQLException {
        long daysDiff = toDate.toEpochDay() - fromDate.toEpochDay();
        LocalDate prevFrom = fromDate.minusDays(daysDiff + 1);
        LocalDate prevTo = fromDate.minusDays(1);
        
        String query = "SELECT COUNT(*) as total FROM suat_chieu " +
                "WHERE DATE(bat_dau_luc) BETWEEN ? AND ? AND trang_thai != 'HUY'";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setDate(1, Date.valueOf(prevFrom));
            ps.setDate(2, Date.valueOf(prevTo));
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getInt("total") : 0;
        }
    }

    private void loadRevenueChart(LocalDate fromDate, LocalDate toDate) {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Doanh thu");

        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) return;
            
            String query = "SELECT DATE(dat_luc) as date, SUM(tong_tien) as revenue " +
                    "FROM don_hang WHERE trang_thai = 'DA_THANH_TOAN' " +
                    "AND DATE(dat_luc) BETWEEN ? AND ? " +
                    "GROUP BY DATE(dat_luc) ORDER BY date";
            
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
            if (conn == null) return;
            
            // Doanh thu từ vé
            String ticketQuery = "SELECT COALESCE(SUM(dv.don_gia), 0) as revenue " +
                    "FROM don_ve dv " +
                    "JOIN don_hang dh ON dv.ma_don_hang = dh.ma_don_hang " +
                    "WHERE dh.trang_thai = 'DA_THANH_TOAN' AND DATE(dh.dat_luc) BETWEEN ? AND ?";
            
            double ticketRevenue = 0;
            try (PreparedStatement ps = conn.prepareStatement(ticketQuery)) {
                ps.setDate(1, Date.valueOf(fromDate));
                ps.setDate(2, Date.valueOf(toDate));
                ResultSet rs = ps.executeQuery();
                if (rs.next()) ticketRevenue = rs.getDouble("revenue");
            }

            // Doanh thu từ hàng hóa (sản phẩm + combo)
            String productQuery = "SELECT COALESCE(SUM(hh.don_gia * hh.so_luong), 0) as revenue " +
                    "FROM hang_hoa hh " +
                    "JOIN don_hang dh ON hh.ma_don_hang = dh.ma_don_hang " +
                    "WHERE dh.trang_thai = 'DA_THANH_TOAN' AND DATE(dh.dat_luc) BETWEEN ? AND ?";
            
            double productRevenue = 0;
            try (PreparedStatement ps = conn.prepareStatement(productQuery)) {
                ps.setDate(1, Date.valueOf(fromDate));
                ps.setDate(2, Date.valueOf(toDate));
                ResultSet rs = ps.executeQuery();
                if (rs.next()) productRevenue = rs.getDouble("revenue");
            }

            if (ticketRevenue > 0) {
                pieData.add(new PieChart.Data("Vé phim: " + currencyFormat.format(ticketRevenue) + " VNĐ", ticketRevenue));
            }
            if (productRevenue > 0) {
                pieData.add(new PieChart.Data("Hàng hóa: " + currencyFormat.format(productRevenue) + " VNĐ", productRevenue));
            }
            
            if (pieData.isEmpty()) {
                pieData.add(new PieChart.Data("Chưa có dữ liệu", 1));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        revenuePieChart.setData(pieData);
    }

    private void loadTopMoviesChart(LocalDate fromDate, LocalDate toDate) {
        XYChart.Series<String, Number> series = new XYChart.Series<>();

        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) return;
            
            String query = "SELECT p.ten_phim, SUM(dv.don_gia) as revenue " +
                    "FROM don_ve dv " +
                    "JOIN don_hang dh ON dv.ma_don_hang = dh.ma_don_hang " +
                    "JOIN ve v ON dv.ma_ve = v.ma_ve " +
                    "JOIN suat_chieu sc ON v.ma_suat_chieu = sc.ma_suat_chieu " +
                    "JOIN phim p ON sc.ma_phim = p.ma_phim " +
                    "WHERE dh.trang_thai = 'DA_THANH_TOAN' " +
                    "AND DATE(dh.dat_luc) BETWEEN ? AND ? " +
                    "GROUP BY p.ma_phim, p.ten_phim " +
                    "ORDER BY revenue DESC LIMIT 10";

            try (PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setDate(1, Date.valueOf(fromDate));
                ps.setDate(2, Date.valueOf(toDate));
                ResultSet rs = ps.executeQuery();

                while (rs.next()) {
                    String movieName = rs.getString("ten_phim");
                    double revenue = rs.getDouble("revenue");
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
            if (conn == null) return;
            
            String query = "SELECT DATE(dh.dat_luc) as date, p.ten_phim, " +
                    "COUNT(DISTINCT sc.ma_suat_chieu) as screenings, " +
                    "COUNT(DISTINCT v.ma_ve) as tickets, " +
                    "SUM(dv.don_gia) as revenue, " +
                    "ROUND((COUNT(DISTINCT v.ma_ve) * 100.0 / " +
                    "(COUNT(DISTINCT sc.ma_suat_chieu) * ph.suc_chua)), 2) as occupancy " +
                    "FROM don_hang dh " +
                    "JOIN don_ve dv ON dh.ma_don_hang = dv.ma_don_hang " +
                    "JOIN ve v ON dv.ma_ve = v.ma_ve " +
                    "JOIN suat_chieu sc ON v.ma_suat_chieu = sc.ma_suat_chieu " +
                    "JOIN phim p ON sc.ma_phim = p.ma_phim " +
                    "JOIN phong ph ON sc.ma_phong = ph.ma_phong " +
                    "WHERE dh.trang_thai = 'DA_THANH_TOAN' " +
                    "AND DATE(dh.dat_luc) BETWEEN ? AND ? " +
                    "GROUP BY DATE(dh.dat_luc), p.ma_phim, p.ten_phim, ph.suc_chua " +
                    "ORDER BY date DESC";

            try (PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setDate(1, Date.valueOf(fromDate));
                ps.setDate(2, Date.valueOf(toDate));
                ResultSet rs = ps.executeQuery();

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                while (rs.next()) {
                    String date = rs.getDate("date").toLocalDate().format(formatter);
                    String movie = rs.getString("ten_phim");
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