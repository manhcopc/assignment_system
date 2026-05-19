package vn.exam.model;

import java.time.LocalDateTime;

/**
 * Mô hình Ca Thi (Exam Session)
 * 
 * Đại diện cho một phiên phân công cán bộ coi thi
 * Tự động tính toán dựa trên số lượng cán bộ và phòng thi
 */
public class ExamSession {
    
    private Long id;                          // ID tự tăng
    private String maCaThi;                   // "ca_thi_20260519_083744_017"
    private String tenCa;                     // "Ca 1", "Ca 2", ...
    private String tenCaThi;                  // "Ca thi buổi sáng"
    private int soLuongCanBo;                 // 30
    private int soLuongPhongThi;              // 10
    private int soGiamThiCan;                 // Tự động: phòng * 2
    private int soCanBoGiamSat;               // Tự động: canBo - giamThi
    private String tenFileExcel;              // "database", "upload"
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Constructor
    public ExamSession() {}
    
    public ExamSession(int soLuongCanBo, int soLuongPhongThi, String tenCaThi) {
        this.soLuongCanBo = soLuongCanBo;
        this.soLuongPhongThi = soLuongPhongThi;
        this.tenCaThi = tenCaThi;
        
        // Tự động tính toán
        this.soGiamThiCan = soLuongPhongThi * 2;
        this.soCanBoGiamSat = soLuongCanBo - this.soGiamThiCan;
        
        // Tạo mã ca thi
        this.maCaThi = generateSessionCode();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Tạo mã ca thi dựa trên timestamp
     * Format: ca_thi_yyyyMMdd_HHmmss_SSS
     */
    private String generateSessionCode() {
        LocalDateTime now = LocalDateTime.now();
        String timestamp = String.format(
            "ca_thi_%04d%02d%02d_%02d%02d%02d_%03d",
            now.getYear(),
            now.getMonthValue(),
            now.getDayOfMonth(),
            now.getHour(),
            now.getMinute(),
            now.getSecond(),
            now.getNano() / 1_000_000
        );
        return timestamp;
    }
    
    /**
     * Cập nhật tên ca thi tự động sau khi có ID
     */
    public void updateAutoTenCa() {
        if (this.id != null) {
            this.tenCa = "Ca " + this.id;
            this.updatedAt = LocalDateTime.now();
        }
    }
    
    /**
     * Kiểm tra xem phân công có hợp lệ không
     */
    public boolean isValid() {
        return soLuongCanBo > 0 
            && soLuongPhongThi > 0 
            && soGiamThiCan > 0 
            && soGiamThiCan <= soLuongCanBo;
    }
    
    /**
     * Lấy thông tin tóm tắt của ca thi
     */
    public String getSummary() {
        return String.format(
            "Ca: %s | Cán bộ: %d | Phòng: %d | Giám thị: %d | Giám sát: %d",
            tenCa != null ? tenCa : maCaThi,
            soLuongCanBo,
            soLuongPhongThi,
            soGiamThiCan,
            soCanBoGiamSat
        );
    }
    
    // Getters & Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getMaCaThi() {
        return maCaThi;
    }
    
    public void setMaCaThi(String maCaThi) {
        this.maCaThi = maCaThi;
    }
    
    public String getTenCa() {
        return tenCa;
    }
    
    public void setTenCa(String tenCa) {
        this.tenCa = tenCa;
    }
    
    public String getTenCaThi() {
        return tenCaThi;
    }
    
    public void setTenCaThi(String tenCaThi) {
        this.tenCaThi = tenCaThi;
    }
    
    public int getSoLuongCanBo() {
        return soLuongCanBo;
    }
    
    public void setSoLuongCanBo(int soLuongCanBo) {
        this.soLuongCanBo = soLuongCanBo;
        // Tự động tính toán lại
        this.soCanBoGiamSat = soLuongCanBo - this.soGiamThiCan;
    }
    
    public int getSoLuongPhongThi() {
        return soLuongPhongThi;
    }
    
    public void setSoLuongPhongThi(int soLuongPhongThi) {
        this.soLuongPhongThi = soLuongPhongThi;
        // Tự động tính toán lại
        this.soGiamThiCan = soLuongPhongThi * 2;
        this.soCanBoGiamSat = soLuongCanBo - this.soGiamThiCan;
    }
    
    public int getSoGiamThiCan() {
        return soGiamThiCan;
    }
    
    public int getSoCanBoGiamSat() {
        return soCanBoGiamSat;
    }
    
    public String getTenFileExcel() {
        return tenFileExcel;
    }
    
    public void setTenFileExcel(String tenFileExcel) {
        this.tenFileExcel = tenFileExcel;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
