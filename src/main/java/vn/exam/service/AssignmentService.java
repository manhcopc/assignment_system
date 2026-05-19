package vn.exam.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import vn.exam.model.AssignmentResult;
import vn.exam.model.CanBo;
import vn.exam.model.GiamSat;
import vn.exam.model.PhanCong;
import vn.exam.model.PhongThi;

/**
 * Dịch vụ phân công cán bộ coi thi
 * 
 * Tích hợp thuật toán Backtracking với Retry:
 * - Sử dụng BacktrackingAssignmentEngine để phân công
 * - Nếu không tìm được phương án, tự động retry với trạng thái mới
 * - Đảm bảo các ràng buộc: không coi lại phòng, không ghép lại cặp, không giám sát lại phòng
 */
public class AssignmentService {
    public static final String VAI_TRO_GIAM_THI_1 = "Giám thị 1";
    public static final String VAI_TRO_GIAM_THI_2 = "Giám thị 2";

    private int maxRetry = 100;

    /**
     * Constructor mặc định
     */
    public AssignmentService() {
    }

    /**
     * Constructor cho phép tuỳ chỉnh số lần retry tối đa
     */
    public AssignmentService(int maxRetry) {
        this.maxRetry = maxRetry > 0 ? maxRetry : 100;
    }

    /**
     * Phân công nhiều ca thi sử dụng thuật toán Backtracking + Retry
     * @param danhSachCanBo - danh sách toàn bộ cán bộ
     * @param danhSachPhong - danh sách toàn bộ phòng thi
     * @param soPhongThi - số phòng thi cần sử dụng
     * @param soGiamThi - số giám thị cần sử dụng
     * @param soCaThi - số ca thi
     * @return kết quả phân công
     */
    public AssignmentResult phanCongNhieuCa(List<CanBo> danhSachCanBo, List<PhongThi> danhSachPhong,
                                            int soPhongThi, int soGiamThi, int soCaThi) {
        validateInput(danhSachCanBo, danhSachPhong, soPhongThi, soGiamThi, soCaThi);

        // Sử dụng BacktrackingAssignmentEngine với cơ chế retry
        BacktrackingAssignmentEngine engine = new BacktrackingAssignmentEngine(maxRetry);
        BacktrackingAssignmentEngine.PhanCongResult result = engine.phanCongNhieuCa(
                danhSachCanBo, danhSachPhong, soPhongThi, soGiamThi, soCaThi);

        return new AssignmentResult(result.danhSachPhanCong, result.danhSachGiamSat);
    }

    /**
     * Phân công 1 ca thi (wrapper cho compatibility)
     */
    public AssignmentResult assign(List<CanBo> canBoList, List<PhongThi> phongThiList,
                                   int soPhongThi, int soGiamThi) {
        return phanCongNhieuCa(canBoList, phongThiList, soPhongThi, soGiamThi, 1);
    }

    /**
     * Kiểm tra tính hợp lệ của dữ liệu đầu vào
     */
    private void validateInput(List<CanBo> canBoList, List<PhongThi> phongThiList,
                               int soPhongThi, int soGiamThi, int soCaThi) {
        if (soPhongThi <= 0) {
            throw new IllegalArgumentException("Số phòng thi cần sử dụng phải lớn hơn 0.");
        }
        if (soGiamThi <= 0) {
            throw new IllegalArgumentException("Số giám thị phải lớn hơn 0.");
        }
        if (soCaThi <= 0) {
            throw new IllegalArgumentException("Số ca thi phải lớn hơn 0.");
        }
        if (phongThiList == null || phongThiList.size() == 0) {
            throw new IllegalArgumentException("File PHONGTHI.xlsx không có dữ liệu phòng thi.");
        }
        if (canBoList == null || canBoList.size() == 0) {
            throw new IllegalArgumentException("File CANBOCOITHI.xlsx không có dữ liệu cán bộ coi thi.");
        }
        if (soPhongThi > phongThiList.size()) {
            throw new IllegalArgumentException("Số phòng thi nhập vào (" + soPhongThi
                    + ") lớn hơn số phòng trong file (" + phongThiList.size() + ").");
        }
        if (soGiamThi > canBoList.size()) {
            throw new IllegalArgumentException("Số giám thị nhập vào (" + soGiamThi
                    + ") lớn hơn số cán bộ trong file (" + canBoList.size() + ").");
        }
        int soGiamThiCan = soPhongThi * 2;
        if (soGiamThi < soGiamThiCan) {
            throw new IllegalArgumentException("Số giám thị nhập vào phải >= số phòng thi * 2. Cần ít nhất "
                    + soGiamThiCan + " giám thị nhưng chỉ nhập " + soGiamThi + ".");
        }
    }
}
