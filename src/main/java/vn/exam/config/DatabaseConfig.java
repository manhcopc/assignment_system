package vn.exam.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Cấu hình kết nối Database
 * 
 * Được tạo từ yêu cầu: Tự động hóa việc tính toán theo ca
 * Lưu trữ thông tin ca thi và phân công vào database
 */
public class DatabaseConfig {

    // Cấu hình kết nối Database
    private static final String DB_URL = "jdbc:mysql://localhost:3306/exam_assignment_system";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "123456";
    private static final String DB_DRIVER = "com.mysql.cj.jdbc.Driver";

    static {
        try {
            Class.forName(DB_DRIVER);
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL Driver không tìm thấy: " + e.getMessage());
        }
    }

    /**
     * Lấy kết nối Database
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    /**
     * Đóng kết nối (helper method)
     */
    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                System.err.println("Lỗi đóng kết nối: " + e.getMessage());
            }
        }
    }

    /**
     * Test kết nối
     */
    public static boolean testConnection() {
        try {
            Connection conn = getConnection();
            boolean connected = !conn.isClosed();
            closeConnection(conn);
            return connected;
        } catch (SQLException e) {
            System.err.println("Lỗi kết nối database: " + e.getMessage());
            return false;
        }
    }
}
