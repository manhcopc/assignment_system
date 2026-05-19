package vn.exam.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import vn.exam.config.DatabaseConfig;
import vn.exam.model.ExamSession;

/**
 * DAO Layer cho ExamSession
 * 
 * Quản lý: Create, Read, Update, Delete các ca thi
 * Thực hiện việc lưu trữ ca thi vào database
 */
public class ExamSessionDAO {
    
    /**
     * Lưu ca thi mới vào database
     * @return ID của ca thi vừa tạo
     */
    public Long saveExamSession(ExamSession session, String tenFileExcel) throws Exception {
        String sql = "INSERT INTO exam_session " +
                     "(ma_ca_thi, ten_ca_thi, so_luong_can_bo, so_luong_phong_thi, " +
                     "so_giam_thi_can, so_can_bo_giam_sat, ten_file_excel, created_at, updated_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, session.getMaCaThi());
            pstmt.setString(2, session.getTenCaThi());
            pstmt.setInt(3, session.getSoLuongCanBo());
            pstmt.setInt(4, session.getSoLuongPhongThi());
            pstmt.setInt(5, session.getSoGiamThiCan());
            pstmt.setInt(6, session.getSoCanBoGiamSat());
            pstmt.setString(7, tenFileExcel);
            pstmt.setTimestamp(8, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.setTimestamp(9, Timestamp.valueOf(LocalDateTime.now()));
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new Exception("Lưu ca thi thất bại");
            }
            
            // Lấy ID vừa được tạo
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    Long id = generatedKeys.getLong(1);
                    session.setId(id);
                    return id;
                } else {
                    throw new Exception("Không lấy được ID ca thi mới");
                }
            }
        }
    }
    
    /**
     * Cập nhật tên ca thi tự động sau khi có ID
     */
    public void updateAutoTenCa(Long sessionId) throws Exception {
        String sql = "UPDATE exam_session SET ten_ca = ?, updated_at = ? WHERE id = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            String tenCa = "Ca " + sessionId;
            pstmt.setString(1, tenCa);
            pstmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.setLong(3, sessionId);
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new Exception("Cập nhật tên ca thi thất bại");
            }
        }
    }
    
    /**
     * Lấy ca thi theo ID
     */
    public ExamSession getExamSessionById(Long id) throws Exception {
        String sql = "SELECT * FROM exam_session WHERE id = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToExamSession(rs);
                }
            }
        }
        
        return null;
    }
    
    /**
     * Lấy ca thi theo mã ca thi
     */
    public ExamSession getExamSessionByCode(String maCaThi) throws Exception {
        String sql = "SELECT * FROM exam_session WHERE ma_ca_thi = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, maCaThi);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToExamSession(rs);
                }
            }
        }
        
        return null;
    }
    
    /**
     * Lấy tất cả ca thi
     */
    public List<ExamSession> getAllExamSessions() throws Exception {
        String sql = "SELECT * FROM exam_session ORDER BY created_at DESC";
        List<ExamSession> sessions = new ArrayList<>();
        
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                sessions.add(mapResultSetToExamSession(rs));
            }
        }
        
        return sessions;
    }
    
    /**
     * Lấy số ca thi đã tạo
     */
    public long getTotalSessions() throws Exception {
        String sql = "SELECT COUNT(*) AS total FROM exam_session";
        
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getLong("total");
            }
        }
        
        return 0;
    }
    
    /**
     * Xóa ca thi (và tất cả phân công liên quan)
     * Sử dụng Transaction để đảm bảo toàn vẹn dữ liệu
     */
    public boolean deleteExamSession(Long sessionId) throws Exception {
        String deleteAssignmentsSql = "DELETE FROM phan_cong_coi_thi WHERE ca_thi_id = ?";
        String deleteSupervisionSql = "DELETE FROM phan_cong_giam_sat WHERE ca_thi_id = ?";
        String deleteSessionSql = "DELETE FROM exam_session WHERE id = ?";
        
        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false);
            
            try {
                // Xóa phân công coi thi
                try (PreparedStatement pstmt = conn.prepareStatement(deleteAssignmentsSql)) {
                    pstmt.setLong(1, sessionId);
                    pstmt.executeUpdate();
                }
                
                // Xóa phân công giám sát
                try (PreparedStatement pstmt = conn.prepareStatement(deleteSupervisionSql)) {
                    pstmt.setLong(1, sessionId);
                    pstmt.executeUpdate();
                }
                
                // Xóa ca thi
                try (PreparedStatement pstmt = conn.prepareStatement(deleteSessionSql)) {
                    pstmt.setLong(1, sessionId);
                    int affectedRows = pstmt.executeUpdate();
                    
                    conn.commit();
                    return affectedRows > 0;
                }
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        }
    }
    
    /**
     * Chuyển đổi ResultSet thành ExamSession object
     */
    private ExamSession mapResultSetToExamSession(ResultSet rs) throws Exception {
        ExamSession session = new ExamSession();
        session.setId(rs.getLong("id"));
        session.setMaCaThi(rs.getString("ma_ca_thi"));
        session.setTenCa(rs.getString("ten_ca"));
        session.setTenCaThi(rs.getString("ten_ca_thi"));
        session.setSoLuongCanBo(rs.getInt("so_luong_can_bo"));
        session.setSoLuongPhongThi(rs.getInt("so_luong_phong_thi"));
        session.setTenFileExcel(rs.getString("ten_file_excel"));
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            session.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            session.setUpdatedAt(updatedAt.toLocalDateTime());
        }
        
        return session;
    }
}
