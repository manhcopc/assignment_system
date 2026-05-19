package vn.exam.service;

import java.util.List;

import vn.exam.dao.ExamSessionDAO;
import vn.exam.model.AssignmentResult;
import vn.exam.model.CanBo;
import vn.exam.model.ExamSession;
import vn.exam.model.PhongThi;
import vn.exam.util.ExcelReader;

/**
 * Service Layer cho ExamSession
 * 
 * Xử lý logic phân công theo ca:
 * - Tự động tính toán số ca dựa trên cán bộ và phòng thi
 * - Tạo ca thi mới
 * - Lưu phân công vào database
 */
public class ExamSessionService {
    
    private ExamSessionDAO sessionDAO = new ExamSessionDAO();
    private BacktrackingAssignmentEngine assignmentEngine = new BacktrackingAssignmentEngine();
    private ExcelReader excelReader = new ExcelReader();
    
    /**
     * Tạo và thực hiện phân công cho một ca thi
     * 
     * @param soCanBo - Số cán bộ
     * @param soPhongThi - Số phòng thi
     * @param tenCaThi - Tên ca thi
     * @param tenFileExcel - Tên file Excel input
     * @return ExamSession đã tạo với phân công
     */
    public ExamSession createAndAssignExamSession(
            int soCanBo, 
            int soPhongThi, 
            String tenCaThi,
            String tenFileExcel) throws Exception {
        
        // Bước 1: Tạo ExamSession object
        ExamSession session = new ExamSession(soCanBo, soPhongThi, tenCaThi);
        session.setTenFileExcel(tenFileExcel);
        
        // Bước 2: Kiểm tra dữ liệu hợp lệ
        if (!session.isValid()) {
            throw new IllegalArgumentException(
                "Dữ liệu không hợp lệ: " + session.getSummary()
            );
        }
        
        // Bước 3: Lấy dữ liệu từ file Excel
        List<CanBo> danhSachCanBo = excelReader.readCanBo("data/CANBOCOITHI.xlsx");
        if (danhSachCanBo.size() < soCanBo) {
            throw new IllegalArgumentException(
                "Số cán bộ trong file (" + danhSachCanBo.size() + 
                ") nhỏ hơn yêu cầu (" + soCanBo + ")"
            );
        }
        danhSachCanBo = danhSachCanBo.subList(0, soCanBo);
        
        List<PhongThi> danhSachPhongThi = excelReader.readPhongThi("data/PHONGTHI.xlsx");
        if (danhSachPhongThi.size() < soPhongThi) {
            throw new IllegalArgumentException(
                "Số phòng thi trong file (" + danhSachPhongThi.size() + 
                ") nhỏ hơn yêu cầu (" + soPhongThi + ")"
            );
        }
        danhSachPhongThi = danhSachPhongThi.subList(0, soPhongThi);
        
        // Bước 4: Gọi thuật toán phân công
        BacktrackingAssignmentEngine.PhanCongResult phanCongResult = 
            assignmentEngine.phanCongNhieuCa(
                danhSachCanBo, 
                danhSachPhongThi, 
                soPhongThi, 
                soCanBo, 
                1  // Chỉ 1 ca thi
            );
        
        AssignmentResult assignmentResult = new AssignmentResult(
            phanCongResult.danhSachPhanCong,
            phanCongResult.danhSachGiamSat
        );
        
        // Bước 5: Lưu ca thi vào database (Transaction)
        Long sessionId = sessionDAO.saveExamSession(session, tenFileExcel);
        
        // Bước 6: Cập nhật tên ca thi tự động
        sessionDAO.updateAutoTenCa(sessionId);
        
        // Bước 7: Lưu phân công coi thi
        savePhanCongCoiThi(sessionId, assignmentResult.getDanhSachPhanCong());
        
        // Bước 8: Lưu phân công giám sát
        savePhanCongGiamSat(sessionId, assignmentResult.getDanhSachGiamSat());
        
        // Bước 9: Cập nhật session
        session.setId(sessionId);
        session.updateAutoTenCa();
        
        return session;
    }
    
    /**
     * Lưu phân công coi thi vào database
     */
    private void savePhanCongCoiThi(Long sessionId, java.util.List<vn.exam.model.PhanCong> danhSach) 
            throws Exception {
        String sql = "INSERT INTO phan_cong_coi_thi " +
                     "(ca_thi_id, stt, ma_gv, ho_ten, vai_tro, phong_thi) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        
        try (java.sql.Connection conn = vn.exam.config.DatabaseConfig.getConnection();
             java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            for (vn.exam.model.PhanCong phanCong : danhSach) {
                pstmt.setLong(1, sessionId);
                pstmt.setInt(2, phanCong.getStt());
                pstmt.setString(3, phanCong.getCanBo().getMaGv());
                pstmt.setString(4, phanCong.getCanBo().getHoTen());
                pstmt.setString(5, phanCong.getVaiTro());
                pstmt.setString(6, phanCong.getPhongThi().getTenPhong());
                pstmt.addBatch();
            }
            
            pstmt.executeBatch();
        }
    }
    
    /**
     * Lưu phân công giám sát vào database
     */
    private void savePhanCongGiamSat(Long sessionId, java.util.List<vn.exam.model.GiamSat> danhSach) 
            throws Exception {
        String sql = "INSERT INTO phan_cong_giam_sat " +
                     "(ca_thi_id, stt, ma_gv, ho_ten, phong_thi_duoc_giam_sat, mo_ta_phong_giam_sat) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        
        try (java.sql.Connection conn = vn.exam.config.DatabaseConfig.getConnection();
             java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            for (vn.exam.model.GiamSat giamSat : danhSach) {
                pstmt.setLong(1, sessionId);
                pstmt.setInt(2, giamSat.getStt());
                pstmt.setString(3, giamSat.getCanBo().getMaGv());
                pstmt.setString(4, giamSat.getCanBo().getHoTen());
                pstmt.setString(5, giamSat.getPhongThiDuocGiamSat().getTenPhong());
                pstmt.setString(6, giamSat.getPhongThiDuocGiamSat().getTenPhong());
                pstmt.addBatch();
            }
            
            pstmt.executeBatch();
        }
    }
    
    /**
     * Lấy ca thi theo ID
     */
    public ExamSession getExamSessionById(Long id) throws Exception {
        return sessionDAO.getExamSessionById(id);
    }
    
    /**
     * Lấy tất cả ca thi
     */
    public List<ExamSession> getAllExamSessions() throws Exception {
        return sessionDAO.getAllExamSessions();
    }
    
    /**
     * Lấy số ca thi đã tạo
     */
    public long getTotalSessions() throws Exception {
        return sessionDAO.getTotalSessions();
    }
}
