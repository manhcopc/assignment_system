package vn.exam.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import vn.exam.model.CanBo;
import vn.exam.model.GiamSat;
import vn.exam.model.PhanCong;
import vn.exam.model.PhongThi;

/**
 * Engine phân công cán bộ coi thi sử dụng Backtracking + Retry
 * 
 * Thuật toán:
 * 1. Giai đoạn 1: Phân công Giám thị coi thi (2 người/phòng)
 * - Mỗi phòng thi phải có đúng 2 giám thị
 * - Một cán bộ không được phân lại cùng phòng
 * - Một cặp cán bộ không được làm giám thị chung nếu đã làm trước đó
 * 
 * 2. Giai đoạn 2: Phân công Giám sát
 * - Cán bộ còn lại sẽ làm giám sát
 * - Cán bộ không được giám sát phòng mà đã coi thi trước đó
 * - Cán bộ không được giám sát cùng phòng 2 lần
 * 
 * 3. Cơ chế Retry: Nếu không tìm được phương án hợp lệ, rollback toàn bộ và thử
 * lại
 */
public class BacktrackingAssignmentEngine {
    private static final int MAX_RETRY = 100;
    private static final Random random = new Random();

    private AssignmentConstraint constraint;
    private int retryCount;
    private int maxRetry;

    public BacktrackingAssignmentEngine() {
        this(MAX_RETRY);
    }

    public BacktrackingAssignmentEngine(int maxRetry) {
        this.constraint = new AssignmentConstraint();
        this.maxRetry = maxRetry > 0 ? maxRetry : MAX_RETRY;
        this.retryCount = 0;
    }

    /**
     * Thực hiện phân công nhiều ca thi
     * 
     * @param canBoList    - danh sách toàn bộ cán bộ
     * @param phongThiList - danh sách toàn bộ phòng thi
     * @param soPhongThi   - số phòng thi cần sử dụng
     * @param soGiamThi    - số giám thị cần sử dụng
     * @param soCaThi      - số ca thi
     * @return kết quả phân công
     * @throws IllegalArgumentException nếu không tìm được phương án hợp lệ
     */
    public PhanCongResult phanCongNhieuCa(List<CanBo> canBoList, List<PhongThi> phongThiList,
            int soPhongThi, int soGiamThi, int soCaThi) {
        List<PhongThi> phongDuocDung = new ArrayList<>(phongThiList.subList(0, soPhongThi));
        List<CanBo> canBoDuocDung = new ArrayList<>(canBoList.subList(0, soGiamThi));
        int soGiamThiCan = soPhongThi * 2;
        int soCanBoGiamSat = soGiamThi - soGiamThiCan;

        List<PhanCong> danhSachPhanCong = new ArrayList<>();
        List<GiamSat> danhSachGiamSat = new ArrayList<>();

        // Thử phân công với cơ chế retry
        for (int attempt = 0; attempt <= maxRetry; attempt++) {
            try {
                retryCount = attempt;
                danhSachPhanCong.clear();
                danhSachGiamSat.clear();

                // Reset constraint nếu đây là lần thử lại
                if (attempt > 0) {
                    // ✅ Tạo constraint mới để reset tất cả ràng buộc
                    AssignmentConstraint newConstraint = new AssignmentConstraint();

                    // Xáo trộn lại danh sách cán bộ và phòng thi
                    Collections.shuffle(canBoDuocDung);
                    Collections.shuffle(phongDuocDung);

                    // Sao chép ràng buộc từ những ca đã thành công (attempt trước)
                    // Chỉ giữ lại constraint từ những ca đã phân công thành công
                    for (PhanCong pc : danhSachPhanCong) {
                        newConstraint.ghiNhanPhongDaCoi(pc.getCanBo(), pc.getPhongThi());
                    }
                    for (GiamSat gs : danhSachGiamSat) {
                        newConstraint.ghiNhanGiamSatPhong(gs.getCanBo(), gs.getPhongThiDuocGiamSat());
                    }

                    // ✅ QUAN TRỌNG: Gán newConstraint vào this.constraint để constraint mới được sử
                    // dụng
                    this.constraint = newConstraint;
                }

                // Phân công từng ca thi
                for (int caThi = 1; caThi <= soCaThi; caThi++) {
                    List<RoomAssignment> phanCongCa = new ArrayList<>();
                    Set<String> canBoDaDungTrongCa = new HashSet<>();

                    // Giai đoạn 1: Phân công giám thị cho ca này
                    if (!phanCongGiamThiChoCa(canBoDuocDung, phongDuocDung, 0,
                            canBoDaDungTrongCa, phanCongCa)) {
                        throw new IllegalArgumentException("Không tìm được phương án giám thị cho ca " + caThi);
                    }

                    // Ghi nhận phân công giám thị
                    int sttPhanCong = 1;
                    for (RoomAssignment assignment : phanCongCa) {
                        danhSachPhanCong.add(new PhanCong(caThi, sttPhanCong++, assignment.giamThi1,
                                assignment.phongThi, AssignmentService.VAI_TRO_GIAM_THI_1));
                        danhSachPhanCong.add(new PhanCong(caThi, sttPhanCong++, assignment.giamThi2,
                                assignment.phongThi, AssignmentService.VAI_TRO_GIAM_THI_2));
                        constraint.ghiNhanPhongDaCoi(assignment.giamThi1, assignment.phongThi);
                        constraint.ghiNhanPhongDaCoi(assignment.giamThi2, assignment.phongThi);
                        constraint.ghiNhanCapDaGhep(assignment.giamThi1, assignment.giamThi2);
                    }

                    // Giai đoạn 2: Phân công giám sát cho ca này
                    int sttGiamSat = 1;
                    for (CanBo canBo : canBoDuocDung) {
                        if (sttGiamSat > soCanBoGiamSat) {
                            break;
                        }
                        if (!canBoDaDungTrongCa.contains(canBo.getMaGv())) {
                            PhongThi phongGiamSat = phongDuocDung.get((sttGiamSat - 1) % soPhongThi);

                            // Kiểm tra ràng buộc giám sát
                            if (constraint.coTheGiamSatPhong(canBo, phongGiamSat)) {
                                danhSachGiamSat.add(new GiamSat(caThi, sttGiamSat, canBo, phongGiamSat));
                                constraint.ghiNhanGiamSatPhong(canBo, phongGiamSat);
                                canBoDaDungTrongCa.add(canBo.getMaGv());
                                sttGiamSat++;
                            }
                        }
                    }

                    if (sttGiamSat <= soCanBoGiamSat) {
                        throw new IllegalArgumentException("Không đủ cán bộ hợp lệ để phân công giám sát ca " + caThi);
                    }
                }

                // Thành công! Trả về kết quả
                return new PhanCongResult(danhSachPhanCong, danhSachGiamSat, retryCount);

            } catch (IllegalArgumentException e) {
                // Nếu đây là lần cuối cùng, ném lỗi
                if (attempt >= maxRetry) {
                    throw new IllegalArgumentException(
                            "Không thể tìm được phương án phân công hợp lệ sau " + maxRetry + " lần thử: "
                                    + e.getMessage());
                }
                // Nếu không phải lần cuối, tiếp tục thử lại
            }
        }

        throw new IllegalArgumentException("Không thể tìm được phương án phân công hợp lệ");
    }

    /**
     * Phân công giám thị cho một ca thi (sử dụng backtracking)
     * 
     * @param canBoList          - danh sách cán bộ có thể sử dụng
     * @param phongList          - danh sách phòng thi
     * @param phongIndex         - chỉ số phòng thi hiện tại
     * @param canBoDaDungTrongCa - Set cán bộ đã dùng trong ca này
     * @param phanCongCa         - danh sách phân công phòng trong ca này
     * @return true nếu tìm được phương án, false nếu không
     */
    private boolean phanCongGiamThiChoCa(List<CanBo> canBoList, List<PhongThi> phongList,
            int phongIndex, Set<String> canBoDaDungTrongCa,
            List<RoomAssignment> phanCongCa) {
        // Base case: đã phân công hết tất cả phòng
        if (phongIndex == phongList.size()) {
            return true;
        }

        PhongThi phongThi = phongList.get(phongIndex);

        // Thử lần lượt từng cán bộ làm giám thị 1
        for (int i = 0; i < canBoList.size(); i++) {
            CanBo giamThi1 = canBoList.get(i);

            // Kiểm tra giám thị 1 có hợp lệ không
            if (!constraint.coTheCoiPhong(giamThi1, phongThi, canBoDaDungTrongCa)) {
                continue;
            }

            canBoDaDungTrongCa.add(giamThi1.getMaGv());

            // Thử lần lượt từng cán bộ làm giám thị 2
            for (int j = 0; j < canBoList.size(); j++) {
                if (i == j) {
                    continue;
                }

                CanBo giamThi2 = canBoList.get(j);

                // Kiểm tra giám thị 2 có hợp lệ không
                if (!constraint.coTheCoiPhong(giamThi2, phongThi, canBoDaDungTrongCa)) {
                    continue;
                }

                // Kiểm tra cặp có được ghép không
                if (!constraint.coTheGhepCap(giamThi1, giamThi2)) {
                    continue;
                }

                // Thêm phân công này
                canBoDaDungTrongCa.add(giamThi2.getMaGv());
                phanCongCa.add(new RoomAssignment(phongThi, giamThi1, giamThi2));

                // Đệ quy để phân công phòng tiếp theo
                if (phanCongGiamThiChoCa(canBoList, phongList, phongIndex + 1,
                        canBoDaDungTrongCa, phanCongCa)) {
                    return true;
                }

                // Backtrack: Xóa phân công nếu không tìm được lời giải
                phanCongCa.remove(phanCongCa.size() - 1);
                canBoDaDungTrongCa.remove(giamThi2.getMaGv());
            }

            canBoDaDungTrongCa.remove(giamThi1.getMaGv());
        }

        return false;
    }

    public int getRetryCount() {
        return retryCount;
    }

    /**
     * Kết quả phân công bao gồm danh sách và số lần retry
     */
    public static class PhanCongResult {
        public final List<PhanCong> danhSachPhanCong;
        public final List<GiamSat> danhSachGiamSat;
        public final int retryCount;

        public PhanCongResult(List<PhanCong> danhSachPhanCong, List<GiamSat> danhSachGiamSat, int retryCount) {
            this.danhSachPhanCong = danhSachPhanCong;
            this.danhSachGiamSat = danhSachGiamSat;
            this.retryCount = retryCount;
        }
    }

    /**
     * Helper class để lưu phân công 1 phòng thi
     */
    private static class RoomAssignment {
        PhongThi phongThi;
        CanBo giamThi1;
        CanBo giamThi2;

        RoomAssignment(PhongThi phongThi, CanBo giamThi1, CanBo giamThi2) {
            this.phongThi = phongThi;
            this.giamThi1 = giamThi1;
            this.giamThi2 = giamThi2;
        }
    }
}
