package controllers;

import database.DBConnection;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.ResourceBundle;
public class HomeController implements Initializable {

    @FXML private Label lblWelcome;
    @FXML private Label lblTotalRevenue;
    @FXML private Label lblRevenueChange;
    @FXML private Label lblTotalTickets;
    @FXML private Label lblTicketsChange;
    @FXML private Label lblTotalCustomers;
    @FXML private Label lblCustomersChange;
    @FXML private Label lblTotalMovies;
    @FXML private Label lblMoviesInfo;
    @FXML private Label lblTodayScreenings;
    @FXML private Label lblScreeningsInfo;
    @FXML private Label lblTotalStaff;
    @FXML private Label lblStaffInfo;
    @FXML private LineChart<String, Number> revenueChart;
    @FXML private BarChart<String, Number> topMoviesChart;
    private Connection connection;
    private NumberFormat currencyFormat;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        connection = DBConnection.getConnection();
        currencyFormat = NumberFormat.getInstance(new Locale("vi", "VN"));
        
        if (connection != null) {
            loadDashboardData();
            loadRevenueChart();
            loadTopMoviesChart();
        } else {
            showAlert("Lỗi", "Không thể kết nối đến cơ sở dữ liệu!", Alert.AlertType.ERROR);
        }
    }

    /**
     * Tải dữ liệu tổng quan cho dashboard
     */
    private void loadDashboardData() {
        try {
            // Tổng doanh thu
            loadTotalRevenue();
            
            // Tổng vé đã bán
            loadTotalTickets();
            
            // Tổng khách hàng
            loadTotalCustomers();
            
            // Tổng phim đang chiếu
            loadTotalMovies();
            
            // Suất chiếu hôm nay
            loadTodayScreenings();
            
            // Tổng nhân viên
            loadTotalStaff();
            
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Lỗi", "Không thể tải dữ liệu dashboard: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    /**
     * Tải tổng doanh thu và so sánh với năm trước
     */
    private void loadTotalRevenue() throws SQLException {
    // Tính doanh thu từ đầu năm đến hiện tại
    LocalDate startOfYear = LocalDate.of(LocalDate.now().getYear(), 1, 1);
    LocalDate today = LocalDate.now();
    
    // Tính doanh thu năm trước (cùng kỳ)
    LocalDate startOfLastYear = startOfYear.minusYears(1);
    LocalDate endOfLastYear = today.minusYears(1);
    
    String sql = "SELECT " +
                 "  fn_tinh_doanh_thu(?, ?) as doanh_thu_nam_nay, " +
                 "  fn_tinh_doanh_thu(?, ?) as doanh_thu_nam_truoc";
    
    try (PreparedStatement ps = connection.prepareStatement(sql)) {
        ps.setDate(1, Date.valueOf(startOfYear));
        ps.setDate(2, Date.valueOf(today));
        ps.setDate(3, Date.valueOf(startOfLastYear));
        ps.setDate(4, Date.valueOf(endOfLastYear));
        
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            double revenueThisYear = rs.getDouble("doanh_thu_nam_nay");
            double revenueLastYear = rs.getDouble("doanh_thu_nam_truoc");
            
            System.out.println("DEBUG - Doanh thu năm nay: " + revenueThisYear);
            System.out.println("DEBUG - Doanh thu năm trước: " + revenueLastYear);
            
            lblTotalRevenue.setText(currencyFormat.format(revenueThisYear) + " ₫");
            
            if (revenueLastYear > 0) {
                double change = ((revenueThisYear - revenueLastYear) / revenueLastYear) * 100;
                String arrow = change >= 0 ? "↑" : "↓";
                lblRevenueChange.setText(String.format("%s %.1f%% so với năm trước", arrow, Math.abs(change)));
            } else {
                lblRevenueChange.setText("Năm 2025");
            }
        }
    } catch (SQLException e) {
        System.err.println("Lỗi khi load tổng doanh thu: " + e.getMessage());
        e.printStackTrace();
        lblTotalRevenue.setText("0 ₫");
        lblRevenueChange.setText("Không có dữ liệu");
    }
}

    /**
     * Tải tổng số vé đã bán
     */
    private void loadTotalTickets() throws SQLException {
        String sql = "SELECT " +
                     "  COUNT(CASE WHEN YEAR(ban_luc) = YEAR(CURDATE()) " +
                     "             THEN 1 END) as ve_nam_nay, " +
                     "  COUNT(CASE WHEN YEAR(ban_luc) = YEAR(CURDATE()) - 1 " +
                     "             THEN 1 END) as ve_nam_truoc " +
                     "FROM ve WHERE trang_thai = 'DA_BAN'";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                int ticketsThisYear = rs.getInt("ve_nam_nay");
                int ticketsLastYear = rs.getInt("ve_nam_truoc");
                
                lblTotalTickets.setText(currencyFormat.format(ticketsThisYear));
                
                if (ticketsLastYear > 0) {
                    double change = ((double)(ticketsThisYear - ticketsLastYear) / ticketsLastYear) * 100;
                    String arrow = change >= 0 ? "↑" : "↓";
                    lblTicketsChange.setText(String.format("%s %.1f%% so với năm trước", arrow, Math.abs(change)));
                } else {
                    lblTicketsChange.setText("Năm 2025");
                }
            }
        }
    }

    /**
     * Tải tổng số khách hàng
     */
    private void loadTotalCustomers() throws SQLException {
        String sql = "SELECT COUNT(*) as total, " +
                     "  SUM(CASE WHEN YEAR(tao_luc) = YEAR(CURDATE()) THEN 1 ELSE 0 END) as new_customers " +
                     "FROM khach_hang";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                int total = rs.getInt("total");
                int newCustomers = rs.getInt("new_customers");
                
                lblTotalCustomers.setText(currencyFormat.format(total));
                lblCustomersChange.setText("↑ " + newCustomers + " khách hàng mới năm nay");
            }
        }
    }

    /**
     * Tải tổng số phim đang chiếu
     */
    private void loadTotalMovies() throws SQLException {
        String sqlMovies = "SELECT COUNT(DISTINCT ma_phim) as total FROM suat_chieu " +
                          "WHERE bat_dau_luc >= CURDATE() AND trang_thai != 'HUY'";
        String sqlRooms = "SELECT COUNT(*) as total FROM phong WHERE trang_thai = 'HOAT_DONG'";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs1 = stmt.executeQuery(sqlMovies)) {
            if (rs1.next()) {
                lblTotalMovies.setText(String.valueOf(rs1.getInt("total")));
            }
        }
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs2 = stmt.executeQuery(sqlRooms)) {
            if (rs2.next()) {
                lblMoviesInfo.setText("Trong " + rs2.getInt("total") + " phòng chiếu");
            }
        }
    }

    /**
     * Tải suất chiếu hôm nay
     */
    private void loadTodayScreenings() throws SQLException {
        String sql = "SELECT COUNT(*) as total, " +
                     "  SUM(CASE WHEN trang_thai = 'MO_BAN' THEN 1 ELSE 0 END) as open " +
                     "FROM suat_chieu " +
                     "WHERE DATE(bat_dau_luc) = CURDATE()";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                int total = rs.getInt("total");
                int open = rs.getInt("open");
                
                lblTodayScreenings.setText(String.valueOf(total));
                lblScreeningsInfo.setText(open + " suất đang mở bán");
            }
        }
    }

    /**
     * Tải tổng số nhân viên
     */
    private void loadTotalStaff() throws SQLException {
        String sql = "SELECT COUNT(*) as total FROM nhan_vien nv " +
                     "JOIN tai_khoan tk ON nv.ma_tai_khoan = tk.ma_tai_khoan " +
                     "WHERE tk.hoat_dong = 1";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                int total = rs.getInt("total");
                lblTotalStaff.setText(String.valueOf(total));
                lblStaffInfo.setText("Đang hoạt động");
            }
        }
    }

    /**
     * Tải biểu đồ doanh thu 7 ngày qua
     */
    private void loadRevenueChart() {
    try {
        String sql = "SELECT DATE(dat_luc) as ngay, SUM(tong_tien) as doanh_thu " +
                    "FROM don_hang " +
                    "WHERE trang_thai = 'DA_THANH_TOAN' " +
                    "  AND dat_luc >= DATE_SUB(CURDATE(), INTERVAL 7 DAY) " +
                    "GROUP BY DATE(dat_luc) " +
                    "ORDER BY ngay";
        
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Doanh thu");
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                // Lấy ngày và revenue
                java.sql.Date sqlDate = rs.getDate("ngay");
                double revenue = rs.getDouble("doanh_thu");
                
                // Format ngày
                String dateStr = "";
                if (sqlDate != null) {
                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM");
                    dateStr = sdf.format(sqlDate);
                }
                
                series.getData().add(new XYChart.Data<>(dateStr, revenue));
            }
        }
        
        revenueChart.getData().clear();
        revenueChart.getData().add(series);
        
    } catch (SQLException e) {
        e.printStackTrace();
        System.out.println("Lỗi load revenue chart: " + e.getMessage());
    }
}
    /**
     * Tải biểu đồ top 5 phim có doanh thu cao nhất
     */
    private void loadTopMoviesChart() {
        try {
            String sql = "SELECT p.ten_phim, SUM(dh.tong_tien) as doanh_thu " +
                        "FROM don_hang dh " +
                        "JOIN don_ve dv ON dh.ma_don_hang = dv.ma_don_hang " +
                        "JOIN ve v ON dv.ma_ve = v.ma_ve " +
                        "JOIN suat_chieu sc ON v.ma_suat_chieu = sc.ma_suat_chieu " +
                        "JOIN phim p ON sc.ma_phim = p.ma_phim " +
                        "WHERE dh.trang_thai = 'DA_THANH_TOAN' " +
                        "  AND YEAR(dh.dat_luc) = YEAR(CURDATE()) " +
                        "GROUP BY p.ma_phim, p.ten_phim " +
                        "ORDER BY doanh_thu DESC " +
                        "LIMIT 5";
            
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Doanh thu");
            
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    String movieName = rs.getString("ten_phim");
                    double revenue = rs.getDouble("doanh_thu");
                    
                    // Rút gọn tên phim nếu quá dài
                    if (movieName.length() > 20) {
                        movieName = movieName.substring(0, 17) + "...";
                    }
                    
                    series.getData().add(new XYChart.Data<>(movieName, revenue));
                }
            }
            
            topMoviesChart.getData().clear();
            topMoviesChart.getData().add(series);
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Hiển thị hộp thoại thông báo
     */
    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}