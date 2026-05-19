-- ============================================================================
-- DATABASE SCHEMA: EXAM ASSIGNMENT SYSTEM
-- Tự động hóa phân công cán bộ coi thi theo ca
-- Created: 2026-05-19
-- ============================================================================

-- ============================================================================
-- 1. TẠO DATABASE NẾU CHƯA TỒN TẠI
-- ============================================================================
CREATE DATABASE IF NOT EXISTS exam_assignment_system 
CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE exam_assignment_system;

-- ============================================================================
-- 2. BẢNG: exam_session (Ca Thi)
-- ============================================================================
CREATE TABLE IF NOT EXISTS exam_session (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID tự tăng',
    ma_ca_thi VARCHAR(50) UNIQUE NOT NULL COMMENT 'ca_thi_20260519_083744_017',
    ten_ca VARCHAR(50) COMMENT 'Ca 1, Ca 2, Ca 3 (tự động từ ID)',
    ten_ca_thi VARCHAR(255) COMMENT 'Ca thi buổi sáng',
    so_luong_can_bo INT NOT NULL COMMENT 'Số cán bộ được phân công',
    so_luong_phong_thi INT NOT NULL COMMENT 'Số phòng thi',
    so_giam_thi_can INT NOT NULL COMMENT 'Tự động tính: phòng * 2',
    so_can_bo_giam_sat INT NOT NULL COMMENT 'Tự động tính: canBo - giamThi',
    ten_file_excel VARCHAR(255) COMMENT 'database, upload',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Thời gian tạo',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Thời gian cập nhật',
    
    -- RÀNG BUỘC: Kiểm tra tính hợp lệ
    CONSTRAINT chk_giam_thi_can CHECK (so_giam_thi_can > 0 AND so_giam_thi_can <= so_luong_can_bo),
    CONSTRAINT chk_giam_sat CHECK (so_can_bo_giam_sat >= 0),
    
    -- CHỈ MỤC
    INDEX idx_ma_ca_thi (ma_ca_thi),
    INDEX idx_created_at (created_at),
    INDEX idx_updated_at (updated_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci 
COMMENT='Lưu thông tin ca thi (phiên phân công)';

-- ============================================================================
-- 3. BẢNG: phan_cong_coi_thi (Phân Công Giám Thị)
-- ============================================================================
CREATE TABLE IF NOT EXISTS phan_cong_coi_thi (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID tự tăng',
    ca_thi_id BIGINT NOT NULL COMMENT 'Khóa ngoại → exam_session.id',
    stt INT NOT NULL COMMENT 'Số thứ tự (1, 2, 3, ...)',
    ma_gv VARCHAR(50) NOT NULL COMMENT 'Mã giáo viên',
    ho_ten VARCHAR(255) NOT NULL COMMENT 'Họ tên giáo viên',
    vai_tro VARCHAR(50) NOT NULL COMMENT 'GIAM_THI_1 hoặc GIAM_THI_2',
    phong_thi VARCHAR(50) NOT NULL COMMENT 'Tên phòng thi (P101, P102, ...)',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- KHÓA NGOẠI
    FOREIGN KEY (ca_thi_id) REFERENCES exam_session(id) ON DELETE CASCADE,
    
    -- CHỈ MỤC
    INDEX idx_ca_thi_id (ca_thi_id),
    INDEX idx_ma_gv (ma_gv),
    INDEX idx_phong_thi (phong_thi),
    INDEX idx_vai_tro (vai_tro)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci 
COMMENT='Lưu phân công giám thị (2 người/phòng)';

-- ============================================================================
-- 4. BẢNG: phan_cong_giam_sat (Phân Công Giám Sát)
-- ============================================================================
CREATE TABLE IF NOT EXISTS phan_cong_giam_sat (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID tự tăng',
    ca_thi_id BIGINT NOT NULL COMMENT 'Khóa ngoại → exam_session.id',
    stt INT NOT NULL COMMENT 'Số thứ tự (1, 2, 3, ...)',
    ma_gv VARCHAR(50) NOT NULL COMMENT 'Mã giáo viên',
    ho_ten VARCHAR(255) NOT NULL COMMENT 'Họ tên giáo viên',
    phong_thi_duoc_giam_sat VARCHAR(500) NOT NULL COMMENT 'P101, P102, P103 (các phòng được giám sát)',
    mo_ta_phong_giam_sat VARCHAR(500) COMMENT 'Mô tả phòng giám sát',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- KHÓA NGOẠI
    FOREIGN KEY (ca_thi_id) REFERENCES exam_session(id) ON DELETE CASCADE,
    
    -- CHỈ MỤC
    INDEX idx_ca_thi_id (ca_thi_id),
    INDEX idx_ma_gv (ma_gv)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci 
COMMENT='Lưu phân công giám sát (người còn lại)';

-- ============================================================================
-- 5. BẢNG: assignment_history (Lịch Sử Phân Công)
-- ============================================================================
CREATE TABLE IF NOT EXISTS assignment_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID tự tăng',
    ma_gv_1 VARCHAR(50) NOT NULL COMMENT 'Mã giáo viên 1',
    ma_gv_2 VARCHAR(50) COMMENT 'Mã giáo viên 2 (nếu là cặp)',
    phong_thi VARCHAR(50) NOT NULL COMMENT 'Phòng thi',
    loai_ghi_nhan VARCHAR(50) NOT NULL COMMENT 'GIAM_THI, GIAM_SAT, CAP_GIAM_THI',
    ca_thi_id BIGINT NOT NULL COMMENT 'Khóa ngoại → exam_session.id',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- KHÓA NGOẠI
    FOREIGN KEY (ca_thi_id) REFERENCES exam_session(id) ON DELETE CASCADE,
    
    -- CHỈ MỤC
    INDEX idx_ma_gv_1 (ma_gv_1),
    INDEX idx_phong_thi (phong_thi),
    INDEX idx_loai_ghi_nhan (loai_ghi_nhan),
    INDEX idx_ca_thi_id (ca_thi_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci 
COMMENT='Lưu lịch sử phân công (để tránh lặp lại)';

-- ============================================================================
-- 6. VIEW: v_exam_session_summary (Thống Kê Ca Thi)
-- ============================================================================
CREATE OR REPLACE VIEW v_exam_session_summary AS
SELECT 
    es.id,
    es.ten_ca,
    es.ten_ca_thi,
    es.so_luong_can_bo,
    es.so_luong_phong_thi,
    es.so_giam_thi_can,
    es.so_can_bo_giam_sat,
    COUNT(DISTINCT CASE WHEN pct.vai_tro = 'GIAM_THI_1' THEN pct.ma_gv END) AS tong_giam_thi_1,
    COUNT(DISTINCT CASE WHEN pct.vai_tro = 'GIAM_THI_2' THEN pct.ma_gv END) AS tong_giam_thi_2,
    COUNT(DISTINCT pct.ma_gv) AS tong_giam_thi,
    COUNT(DISTINCT pgs.ma_gv) AS tong_giam_sat,
    COUNT(DISTINCT pct.phong_thi) AS tong_phong_phan_cong,
    es.created_at,
    es.updated_at
FROM exam_session es
LEFT JOIN phan_cong_coi_thi pct ON es.id = pct.ca_thi_id
LEFT JOIN phan_cong_giam_sat pgs ON es.id = pgs.ca_thi_id
GROUP BY es.id, es.ten_ca, es.ten_ca_thi, es.so_luong_can_bo, 
         es.so_luong_phong_thi, es.so_giam_thi_can, es.so_can_bo_giam_sat, 
         es.created_at, es.updated_at;

-- ============================================================================
-- 7. TRIGGER: Tự động cập nhật ten_ca khi insert exam_session
-- ============================================================================
DROP TRIGGER IF EXISTS trg_auto_ten_ca_insert;

CREATE TRIGGER trg_auto_ten_ca_insert
AFTER INSERT ON exam_session
FOR EACH ROW
UPDATE exam_session 
SET ten_ca = CONCAT('Ca ', NEW.id)
WHERE id = NEW.id;

-- ============================================================================
-- 8. STORED PROCEDURE: Lấy phân công của ca thi
-- ============================================================================
DROP PROCEDURE IF EXISTS sp_get_assignment_by_session;

CREATE PROCEDURE sp_get_assignment_by_session(
    IN p_session_id BIGINT
)
BEGIN
    SELECT * FROM exam_session WHERE id = p_session_id;
    
    SELECT 'GIAM_THI' AS loai, stt, ma_gv, ho_ten, vai_tro, phong_thi 
    FROM phan_cong_coi_thi 
    WHERE ca_thi_id = p_session_id
    ORDER BY phong_thi, vai_tro, stt;
    
    SELECT 'GIAM_SAT' AS loai, stt, ma_gv, ho_ten, phong_thi_duoc_giam_sat 
    FROM phan_cong_giam_sat 
    WHERE ca_thi_id = p_session_id
    ORDER BY stt;
END;

-- ============================================================================
-- 9. STORED PROCEDURE: Xóa ca thi (với kiểm tra)
-- ============================================================================
DROP PROCEDURE IF EXISTS sp_delete_session_safely;

CREATE PROCEDURE sp_delete_session_safely(
    IN p_session_id BIGINT,
    OUT p_result VARCHAR(255)
)
BEGIN
    DECLARE v_session_count INT;
    
    SELECT COUNT(*) INTO v_session_count FROM exam_session WHERE id = p_session_id;
    
    IF v_session_count = 0 THEN
        SET p_result = 'ERROR: Ca thi không tồn tại';
    ELSE
        START TRANSACTION;
        
        DELETE FROM phan_cong_coi_thi WHERE ca_thi_id = p_session_id;
        DELETE FROM phan_cong_giam_sat WHERE ca_thi_id = p_session_id;
        DELETE FROM assignment_history WHERE ca_thi_id = p_session_id;
        DELETE FROM exam_session WHERE id = p_session_id;
        
        COMMIT;
        
        SET p_result = 'SUCCESS: Đã xóa ca thi thành công';
    END IF;
END;

-- ============================================================================
-- END OF SCHEMA
-- ============================================================================
