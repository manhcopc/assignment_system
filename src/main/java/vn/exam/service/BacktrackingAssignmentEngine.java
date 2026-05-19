package vn.exam.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import vn.exam.model.CanBo;
import vn.exam.model.GiamSat;
import vn.exam.model.PhanCong;
import vn.exam.model.PhongThi;

/**
 * Engine phân công cán bộ coi thi sử dụng Backtracking + Retry (Đã tối ưu)
 */
public class BacktrackingAssignmentEngine {
    private static final int MAX_RETRY = 100;
    private static final int MAX_BACKTRACK_STEPS = 50000; // Ngưỡng chặn để kích hoạt Fail-fast tránh treo ứng dụng

    private AssignmentConstraint constraint;
    private int retryCount;
    private int maxRetry;
    private int backtrackSteps; // Biến đếm số bước đệ quy của ca hiện tại

    public BacktrackingAssignmentEngine() {
        this(MAX_RETRY);
    }

    public BacktrackingAssignmentEngine(int maxRetry) {
        this.maxRetry = maxRetry > 0 ? maxRetry : MAX_RETRY;
        this.retryCount = 0;
    }

    /**
     * Thực hiện phân công nhiều ca thi
     * * @param canBoList - danh sách toàn bộ cán bộ
     * 
     * @param phongThiList - danh sách toàn bộ phòng thi
     * @param soPhongThi   - số phòng thi cần sử dụng
     * @param soGiamThi    - số giám thị cần sử dụng
     * @param soCaThi      - số ca thi
     * @return kết quả phân công
     * @throws IllegalArgumentException nếu không tìm được phương án hợp lệ
     */
    public PhanCongResult phanCongNhieuCa(List<CanBo> canBoList, List<PhongThi> phongThiList,
            int soPhongThi, int soGiamThi, int soCaThi) {

        // Kiểm tra tính hợp lệ cơ bản của đầu vào trước khi chạy
        if (soPhongThi * 2 > soGiamThi) {
            throw new IllegalArgumentException("Số lượng cán bộ không đủ để phân công 2 giám thị/phòng (Cần ít nhất "
                    + (soPhongThi * 2) + " người).");
        }

        List<PhongThi> phongDuocDung = new ArrayList<>(phongThiList.subList(0, soPhongThi));
        List<CanBo> canBoDuocDung = new ArrayList<>(canBoList.subList(0, soGiamThi));
        int soGiamThiCan = soPhongThi * 2;
        int soCanBoGiamSat = soGiamThi - soGiamThiCan;

        List<PhanCong> danhSachPhanCong = new ArrayList<>();
        List<GiamSat> danhSachGiamSat = new ArrayList<>();

        // Vòng lặp Retry ngoài cùng: Thử tìm phương án tổng thể cho tất cả các ca
        for (int attempt = 0; attempt <= maxRetry; attempt++) {
            try {
                this.retryCount = attempt;
                danhSachPhanCong.clear();
                danhSachGiamSat.clear();

                // ✅ FIX BUG: Khởi tạo lại một bộ ràng buộc hoàn toàn mới khi thực hiện lượt thử
                // mới.
                // Vì ta làm lại từ đầu (Ca 1), việc xóa bỏ các ràng buộc sai lầm của attempt cũ
                // là bắt buộc.
                this.constraint = new AssignmentConstraint();

                // Xáo trộn ngẫu nhiên danh sách để thuật toán đi theo các nhánh cây nghiệm mới
                // khác nhau
                Collections.shuffle(canBoDuocDung);
                Collections.shuffle(phongDuocDung);

                // Tiến hành phân công tuần tự từng ca thi
                for (int caThi = 1; caThi <= soCaThi; caThi++) {
                    List<RoomAssignment> phanCongCa = new ArrayList<>();
                    Set<String> canBoDaDungTrongCa = new HashSet<>();

                    // Reset bộ đếm bước đệ quy cho ca này
                    this.backtrackSteps = 0;

                    // Giai đoạn 1: Phân công giám thị cho ca này (Sử dụng Backtracking kết hợp tối
                    // ưu tổ hợp)
                    if (!phanCongGiamThiChoCa(canBoDuocDung, phongDuocDung, 0, canBoDaDungTrongCa, phanCongCa)) {
                        throw new IllegalArgumentException(
                                "Không tìm được phương án giám thị (hoặc vượt ngưỡng tính toán) tại ca " + caThi);
                    }

                    // Sau khi tìm được nghiệm cho ca này, ghi nhận phân công và lưu vào hệ thống
                    // ràng buộc lâu dài
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

                    // Giai đoạn 2: Phân công giám sát cho ca này (Thuật toán tham lam dựa trên danh
                    // sách đã được trộn)
                    int sttGiamSat = 1;
                    for (CanBo canBo : canBoDuocDung) {
                        if (sttGiamSat > soCanBoGiamSat) {
                            break;
                        }
                        if (!canBoDaDungTrongCa.contains(canBo.getMaGv())) {
                            PhongThi phongGiamSat = phongDuocDung.get((sttGiamSat - 1) % soPhongThi);

                            // Kiểm tra ràng buộc của vị trí giám sát phòng
                            if (constraint.coTheGiamSatPhong(canBo, phongGiamSat)) {
                                danhSachGiamSat.add(new GiamSat(caThi, sttGiamSat, canBo, phongGiamSat));
                                constraint.ghiNhanGiamSatPhong(canBo, phongGiamSat);
                                canBoDaDungTrongCa.add(canBo.getMaGv());
                                sttGiamSat++;
                            }
                        }
                    }

                    if (sttGiamSat <= soCanBoGiamSat) {
                        throw new IllegalArgumentException(
                                "Không đủ cán bộ hợp lệ để xếp vị trí giám sát cho ca " + caThi);
                    }
                }

                // Nếu vượt qua toàn bộ các ca thi mà không ném ra ngoại lệ -> Thành công!
                return new PhanCongResult(danhSachPhanCong, danhSachGiamSat, retryCount);

            } catch (IllegalArgumentException e) {
                // Nếu đã chạm tới giới hạn Retry tối đa mà vẫn lỗi thì ném ngoại lệ ra ngoài
                // báo tử
                if (attempt >= maxRetry) {
                    throw new IllegalArgumentException(
                            "Không thể tìm được phương án phân công hợp lệ sau " + maxRetry
                                    + " lần thử. Lỗi cuối cùng: "
                                    + e.getMessage());
                }
                // Nếu chưa hết số lần retry, vòng lặp for sẽ tiếp tục sang lượt kế tiếp (thực
                // hiện shuffle mới)
            }
        }

        throw new IllegalArgumentException("Không thể tìm được phương án phân công hợp lệ");
    }

    /**
     * Phân công giám thị cho một ca thi (Sử dụng Backtracking tối ưu)
     */
    private boolean phanCongGiamThiChoCa(List<CanBo> canBoList, List<PhongThi> phongList,
            int phongIndex, Set<String> canBoDaDungTrongCa, List<RoomAssignment> phanCongCa) {

        // ⚡ Cơ chế Fail-fast: Nếu số bước quay lui vượt ngưỡng, lập tức dừng lại để
        // chuyển sang lượt Retry mới,
        // giúp hệ thống không bị "đóng băng" khi rơi vào các nhánh cây nghiệm quá phức
        // tạp.
        if (++backtrackSteps > MAX_BACKTRACK_STEPS) {
            return false;
        }

        // Base case: Đã phân công thành công toàn bộ các phòng thi trong ca hiện tại
        if (phongIndex == phongList.size()) {
            return true;
        }

        PhongThi phongThi = phongList.get(phongIndex);

        // Duyệt tìm Giám thị 1
        for (int i = 0; i < canBoList.size(); i++) {
            CanBo giamThi1 = canBoList.get(i);

            if (!constraint.coTheCoiPhong(giamThi1, phongThi, canBoDaDungTrongCa)) {
                continue;
            }

            canBoDaDungTrongCa.add(giamThi1.getMaGv());

            // Duyệt tìm Giám thị 2
            // ✅ TỐI ƯU: Cho j chạy từ i + 1 để loại bỏ các trường hợp hoán vị trùng lặp cặp
            // đôi (A, B) và (B, A).
            // Do vai trò giám thị 1 và giám thị 2 có tính chất ràng buộc đối xứng, sự thay
            // đổi này giúp giảm 1/2 không gian tìm kiếm.
            for (int j = i + 1; j < canBoList.size(); j++) {
                CanBo giamThi2 = canBoList.get(j);

                if (!constraint.coTheCoiPhong(giamThi2, phongThi, canBoDaDungTrongCa)) {
                    continue;
                }

                if (!constraint.coTheGhepCap(giamThi1, giamThi2)) {
                    continue;
                }

                // Thêm cặp cán bộ hợp lệ vào trạng thái phòng thi hiện tại
                canBoDaDungTrongCa.add(giamThi2.getMaGv());
                phanCongCa.add(new RoomAssignment(phongThi, giamThi1, giamThi2));

                // Gọi đệ quy để tiếp tục xử lý cho phòng thi tiếp theo (phongIndex + 1)
                if (phanCongGiamThiChoCa(canBoList, phongList, phongIndex + 1, canBoDaDungTrongCa, phanCongCa)) {
                    return true;
                }

                // --- BACKTRACK (Quay lui) ---
                // Xóa trạng thái của Giám thị 2 nếu nhánh đệ quy phía trên không tìm được
                // nghiệm
                phanCongCa.remove(phanCongCa.size() - 1);
                canBoDaDungTrongCa.remove(giamThi2.getMaGv());
            }

            // Xóa trạng thái của Giám thị 1
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
     * Helper class định nghĩa cấu trúc phân công tạm thời của một phòng thi
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