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

public class AssignmentService {
    public static final String VAI_TRO_GIAM_THI_1 = "Giám thị 1";
    public static final String VAI_TRO_GIAM_THI_2 = "Giám thị 2";

    private Map<String, Set<String>> phongDaCoi;
    private Set<String> capDaGhep;

    public AssignmentResult phanCongNhieuCa(List<CanBo> danhSachCanBo, List<PhongThi> danhSachPhong,
                                            int soPhongThi, int soCanBoGiamSat, int soCaThi) {
        validateInput(danhSachCanBo, danhSachPhong, soPhongThi, soCanBoGiamSat, soCaThi);

        phongDaCoi = new HashMap<String, Set<String>>();
        capDaGhep = new HashSet<String>();
        List<PhongThi> phongDuocDung = new ArrayList<PhongThi>(danhSachPhong.subList(0, soPhongThi));
        List<PhanCong> danhSachPhanCong = new ArrayList<PhanCong>();
        List<GiamSat> danhSachGiamSat = new ArrayList<GiamSat>();

        for (int caThi = 1; caThi <= soCaThi; caThi++) {
            List<RoomAssignment> phanCongCa = new ArrayList<RoomAssignment>();
            Set<String> canBoDaDungTrongCa = new HashSet<String>();

            if (!chonGiamThiChoCa(danhSachCanBo, phongDuocDung, 0, canBoDaDungTrongCa, phanCongCa)) {
                throw new IllegalArgumentException("Không thể phân công giám thị cho ca " + caThi
                        + " mà vẫn thỏa mãn các ràng buộc phòng đã coi và cặp đã ghép.");
            }

            int sttPhanCong = 1;
            for (RoomAssignment assignment : phanCongCa) {
                danhSachPhanCong.add(new PhanCong(caThi, sttPhanCong++, assignment.giamThi1,
                        assignment.phongThi, VAI_TRO_GIAM_THI_1));
                danhSachPhanCong.add(new PhanCong(caThi, sttPhanCong++, assignment.giamThi2,
                        assignment.phongThi, VAI_TRO_GIAM_THI_2));
                ghiNhanPhongDaCoi(assignment.giamThi1, assignment.phongThi);
                ghiNhanPhongDaCoi(assignment.giamThi2, assignment.phongThi);
                capDaGhep.add(taoKhoaCap(assignment.giamThi1, assignment.giamThi2));
            }

            int sttGiamSat = 1;
            for (CanBo canBo : danhSachCanBo) {
                if (sttGiamSat > soCanBoGiamSat) {
                    break;
                }
                if (!canBoDaDungTrongCa.contains(canBo.getMaGv())) {
                    PhongThi phongGiamSat = phongDuocDung.get((sttGiamSat - 1) % soPhongThi);
                    danhSachGiamSat.add(new GiamSat(caThi, sttGiamSat, canBo, phongGiamSat));
                    canBoDaDungTrongCa.add(canBo.getMaGv());
                    sttGiamSat++;
                }
            }

            if (sttGiamSat <= soCanBoGiamSat) {
                throw new IllegalArgumentException("Không đủ cán bộ chưa dùng trong ca " + caThi
                        + " để phân công " + soCanBoGiamSat + " cán bộ giám sát.");
            }
        }

        return new AssignmentResult(danhSachPhanCong, danhSachGiamSat);
    }

    public AssignmentResult assign(List<CanBo> canBoList, List<PhongThi> phongThiList,
                                   int soPhongThi, int soCanBoGiamSat) {
        return phanCongNhieuCa(canBoList, phongThiList, soPhongThi, soCanBoGiamSat, 1);
    }

    private boolean chonGiamThiChoCa(List<CanBo> danhSachCanBo, List<PhongThi> phongDuocDung,
                                     int phongIndex, Set<String> canBoDaDungTrongCa,
                                     List<RoomAssignment> phanCongCa) {
        if (phongIndex == phongDuocDung.size()) {
            return true;
        }

        PhongThi phongThi = phongDuocDung.get(phongIndex);
        for (int i = 0; i < danhSachCanBo.size(); i++) {
            CanBo giamThi1 = danhSachCanBo.get(i);
            if (!coTheCoiPhong(giamThi1, phongThi, canBoDaDungTrongCa)) {
                continue;
            }
            canBoDaDungTrongCa.add(giamThi1.getMaGv());

            for (int j = 0; j < danhSachCanBo.size(); j++) {
                if (i == j) {
                    continue;
                }
                CanBo giamThi2 = danhSachCanBo.get(j);
                if (!coTheCoiPhong(giamThi2, phongThi, canBoDaDungTrongCa)) {
                    continue;
                }
                if (capDaGhep.contains(taoKhoaCap(giamThi1, giamThi2))) {
                    continue;
                }

                canBoDaDungTrongCa.add(giamThi2.getMaGv());
                phanCongCa.add(new RoomAssignment(phongThi, giamThi1, giamThi2));
                if (chonGiamThiChoCa(danhSachCanBo, phongDuocDung, phongIndex + 1,
                        canBoDaDungTrongCa, phanCongCa)) {
                    return true;
                }
                phanCongCa.remove(phanCongCa.size() - 1);
                canBoDaDungTrongCa.remove(giamThi2.getMaGv());
            }

            canBoDaDungTrongCa.remove(giamThi1.getMaGv());
        }
        return false;
    }

    private boolean coTheCoiPhong(CanBo canBo, PhongThi phongThi, Set<String> canBoDaDungTrongCa) {
        String maGv = canBo.getMaGv();
        if (canBoDaDungTrongCa.contains(maGv)) {
            return false;
        }
        Set<String> danhSachPhongDaCoi = phongDaCoi.get(maGv);
        return danhSachPhongDaCoi == null || !danhSachPhongDaCoi.contains(phongThi.getTenPhong());
    }

    private void ghiNhanPhongDaCoi(CanBo canBo, PhongThi phongThi) {
        Set<String> danhSachPhong = phongDaCoi.get(canBo.getMaGv());
        if (danhSachPhong == null) {
            danhSachPhong = new HashSet<String>();
            phongDaCoi.put(canBo.getMaGv(), danhSachPhong);
        }
        danhSachPhong.add(phongThi.getTenPhong());
    }

    private String taoKhoaCap(CanBo canBo1, CanBo canBo2) {
        String ma1 = canBo1.getMaGv();
        String ma2 = canBo2.getMaGv();
        if (ma1.compareTo(ma2) <= 0) {
            return ma1 + "|" + ma2;
        }
        return ma2 + "|" + ma1;
    }

    private void validateInput(List<CanBo> canBoList, List<PhongThi> phongThiList,
                               int soPhongThi, int soCanBoGiamSat, int soCaThi) {
        if (soPhongThi <= 0) {
            throw new IllegalArgumentException("Số phòng thi cần sử dụng phải lớn hơn 0.");
        }
        if (soCanBoGiamSat < 0) {
            throw new IllegalArgumentException("Số cán bộ giám sát mỗi ca không được âm.");
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
        int tongCanBoCanDungMoiCa = soPhongThi * 2 + soCanBoGiamSat;
        if (tongCanBoCanDungMoiCa > canBoList.size()) {
            throw new IllegalArgumentException("Không đủ cán bộ cho mỗi ca. Cần " + tongCanBoCanDungMoiCa
                    + " cán bộ nhưng file chỉ có " + canBoList.size() + " cán bộ.");
        }
    }

    private static class RoomAssignment {
        private PhongThi phongThi;
        private CanBo giamThi1;
        private CanBo giamThi2;

        private RoomAssignment(PhongThi phongThi, CanBo giamThi1, CanBo giamThi2) {
            this.phongThi = phongThi;
            this.giamThi1 = giamThi1;
            this.giamThi2 = giamThi2;
        }
    }
}
