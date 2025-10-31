package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    // ⚙️ Cấu hình kết nối
    private static final String URL = "jdbc:mysql://localhost:3306/qlrapchieuphim";
    private static final String USER = "b2306555";      // đổi nếu bạn có username khác
    private static final String PASSWORD = "1234";      // nếu MySQL có mật khẩu thì điền vào đây
    private static Connection connection = null;

    // 📦 Lấy kết nối tới MySQL
    public static Connection getConnection() {
        try {
            // Nạp driver JDBC (MySQL Connector/J)
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Nếu chưa có kết nối hoặc đã đóng thì tạo mới
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("✅ Kết nối MySQL thành công!");
            }
        } catch (ClassNotFoundException e) {
            System.err.println("❌ Không tìm thấy MySQL Driver. Hãy kiểm tra thư viện Connector/J!");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("❌ Lỗi khi kết nối MySQL: " + e.getMessage());
            e.printStackTrace();
        }
        return connection;
    }

    // 🧹 Đóng kết nối khi không dùng nữa
    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("🔒 Đã đóng kết nối MySQL.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
