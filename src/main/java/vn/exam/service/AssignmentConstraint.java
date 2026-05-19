package vn.exam.service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import vn.exam.model.CanBo;
import vn.exam.model.PhongThi;

/**
 * Quản lý các ràng buộc (constraints) trong phân công cán bộ coi thi
 * 
 * Các ràng buộc bao gồm:
 * - Phòng đã coi: Cán bộ không được coi lại phòng mà đã coi ở ca trước
 * - Cặp đã ghép: Hai cán bộ đã làm giám thị chung không được ghép lại ở ca sau
 * - Giám sát per phòng: Cán bộ không được giám sát cùng phòng 2 lần
 */
public class AssignmentConstraint {
    // phongDaCoi: Map<maGV, Set<tenPhong>> - lịch sử cán bộ đã coi phòng nào
    private final Map<String, Set<String>> phongDaCoi;

    // capDaGhep: Set<"maGV1|maGV2"> - lịch sử cặp cán bộ đã làm giám thị chung
    private final Set<String> capDaGhep;

    // giamSatPhong: Map<maGV, Set<tenPhong>> - lịch sử cán bộ đã giám sát phòng nào
    private final Map<String, Set<String>> giamSatPhong;

    public AssignmentConstraint() {
        this.phongDaCoi = new HashMap<>();
        this.capDaGhep = new HashSet<>();
        this.giamSatPhong = new HashMap<>();
    }

    /**
     * Kiểm tra cán bộ có thể coi phòng thi không
     * 
     * @param canBo              - cán bộ cần kiểm tra
     * @param phongThi           - phòng thi
     * @param canBoDaDungTrongCa - Set cán bộ đã dùng trong ca hiện tại
     * @return true nếu có thể coi, false nếu không
     */
    public boolean coTheCoiPhong(CanBo canBo, PhongThi phongThi, Set<String> canBoDaDungTrongCa) {
        String maGv = canBo.getMaGv();

        // Ràng buộc 1: Cán bộ không được xuất hiện 2 lần trong cùng 1 ca
        if (canBoDaDungTrongCa.contains(maGv)) {
            return false;
        }

        // Ràng buộc 2: Cán bộ không được coi lại phòng đã từng coi ở ca trước
        Set<String> danhSachPhongDaCoi = phongDaCoi.get(maGv);
        if (danhSachPhongDaCoi != null && danhSachPhongDaCoi.contains(phongThi.getTenPhong())) {
            return false;
        }

        return true;
    }

    /**
     * Kiểm tra hai cán bộ có thể làm giám thị chung không
     * 
     * @param canBo1 - cán bộ thứ nhất
     * @param canBo2 - cán bộ thứ hai
     * @return true nếu có thể ghép, false nếu đã ghép trước đó
     */
    public boolean coTheGhepCap(CanBo canBo1, CanBo canBo2) {
        String khoa = taoKhoaCap(canBo1, canBo2);
        return !capDaGhep.contains(khoa);
    }

    /**
     * Kiểm tra cán bộ có thể giám sát phòng thi không
     * 
     * @param canBo    - cán bộ cần kiểm tra
     * @param phongThi - phòng thi
     * @return true nếu có thể giám sát, false nếu đã giám sát phòng này trước đó
     */
    public boolean coTheGiamSatPhong(CanBo canBo, PhongThi phongThi) {
        // Ràng buộc: Cán bộ không được giám sát cùng phòng 2 lần
        Set<String> danhSachPhongGiamSat = giamSatPhong.get(canBo.getMaGv());
        if (danhSachPhongGiamSat != null && danhSachPhongGiamSat.contains(phongThi.getTenPhong())) {
            return false;
        }

        // Ràng buộc: Cán bộ không được giám sát phòng mà đã coi thi trước đó
        Set<String> phongDaCoi = this.phongDaCoi.get(canBo.getMaGv());
        if (phongDaCoi != null && phongDaCoi.contains(phongThi.getTenPhong())) {
            return false;
        }

        return true;
    }

    /**
     * Ghi nhận cán bộ đã coi phòng thi
     */
    public void ghiNhanPhongDaCoi(CanBo canBo, PhongThi phongThi) {
        String maGv = canBo.getMaGv();
        phongDaCoi.computeIfAbsent(maGv, k -> new HashSet<>()).add(phongThi.getTenPhong());
    }

    /**
     * Ghi nhận hai cán bộ đã làm giám thị chung
     */
    public void ghiNhanCapDaGhep(CanBo canBo1, CanBo canBo2) {
        capDaGhep.add(taoKhoaCap(canBo1, canBo2));
    }

    /**
     * Ghi nhận cán bộ đã giám sát phòng thi
     */
    public void ghiNhanGiamSatPhong(CanBo canBo, PhongThi phongThi) {
        String maGv = canBo.getMaGv();
        giamSatPhong.computeIfAbsent(maGv, k -> new HashSet<>()).add(phongThi.getTenPhong());
    }

    /**
     * Khôi phục lại trạng thái - xóa ràng buộc về phòng đã coi
     */
    public void rollbackPhongDaCoi(CanBo canBo, PhongThi phongThi) {
        String maGv = canBo.getMaGv();
        Set<String> phong = phongDaCoi.get(maGv);
        if (phong != null) {
            phong.remove(phongThi.getTenPhong());
            if (phong.isEmpty()) {
                phongDaCoi.remove(maGv);
            }
        }
    }

    /**
     * Khôi phục lại trạng thái - xóa ràng buộc về cặp đã ghép
     */
    public void rollbackCapDaGhep(CanBo canBo1, CanBo canBo2) {
        capDaGhep.remove(taoKhoaCap(canBo1, canBo2));
    }

    /**
     * Khôi phục lại trạng thái - xóa ràng buộc về giám sát phòng
     */
    public void rollbackGiamSatPhong(CanBo canBo, PhongThi phongThi) {
        String maGv = canBo.getMaGv();
        Set<String> phong = giamSatPhong.get(maGv);
        if (phong != null) {
            phong.remove(phongThi.getTenPhong());
            if (phong.isEmpty()) {
                giamSatPhong.remove(maGv);
            }
        }
    }

    /**
     * Tạo khóa duy nhất cho cặp cán bộ (đảm bảo thứ tự không ảnh hưởng)
     */
    private String taoKhoaCap(CanBo canBo1, CanBo canBo2) {
        String ma1 = canBo1.getMaGv();
        String ma2 = canBo2.getMaGv();
        if (ma1.compareTo(ma2) <= 0) {
            return ma1 + "|" + ma2;
        }
        return ma2 + "|" + ma1;
    }

    /**
     * Lấy bản sao deep copy của constraint hiện tại
     */
    public AssignmentConstraint copy() {
        AssignmentConstraint copy = new AssignmentConstraint();

        // Copy phongDaCoi
        for (Map.Entry<String, Set<String>> entry : phongDaCoi.entrySet()) {
            copy.phongDaCoi.put(entry.getKey(), new HashSet<>(entry.getValue()));
        }

        // Copy capDaGhep
        copy.capDaGhep.addAll(capDaGhep);

        // Copy giamSatPhong
        for (Map.Entry<String, Set<String>> entry : giamSatPhong.entrySet()) {
            copy.giamSatPhong.put(entry.getKey(), new HashSet<>(entry.getValue()));
        }

        return copy;
    }
}
