/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Application;

/**
 *
 * @author ASUS PC
 */
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/qlrapchieuphim";
    private static final String USER = "b2306555";
    private static final String PASSWORD = "1234"; // nếu có password thì điền vào

    public static Connection getConnection() {
        Connection conn = null;
        try {
            // Kết nối
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Kết nối MySQL thành công!");
        } catch (SQLException e) {
            System.out.println("Kết nối thất bại: " + e.getMessage());
        }
        return conn;
    }

    public static void main(String[] args) {
        getConnection(); // test thử
    }
}

