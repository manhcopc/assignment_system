package vn.exam.service;

import java.util.ArrayList;
import java.util.List;

import vn.exam.model.AssignmentResult;
import vn.exam.model.CanBo;
import vn.exam.model.GiamSat;
import vn.exam.model.PhanCong;
import vn.exam.model.PhongThi;

public class AssignmentService {
    public AssignmentResult assign(List<CanBo> canBoList, List<PhongThi> phongThiList,
                                   int soPhongThi, int soCanBoGiamSat) {
        validateInput(canBoList, phongThiList, soPhongThi, soCanBoGiamSat);

        int tongCanBoCanDung = soPhongThi * 2 + soCanBoGiamSat;
        List<PhongThi> phongDuocDung = phongThiList.subList(0, soPhongThi);
        List<PhanCong> phanCongList = new ArrayList<PhanCong>();
        List<GiamSat> giamSatList = new ArrayList<GiamSat>();

        int canBoIndex = 0;
        int sttPhanCong = 1;
        for (int i = 0; i < soPhongThi; i++) {
            PhongThi phongThi = phongDuocDung.get(i);
            phanCongList.add(new PhanCong(sttPhanCong++, canBoList.get(canBoIndex++), true, false, phongThi));
            phanCongList.add(new PhanCong(sttPhanCong++, canBoList.get(canBoIndex++), false, true, phongThi));
        }

        for (int i = 0; i < soCanBoGiamSat; i++) {
            PhongThi phongThiDuocGiamSat = phongDuocDung.get(i % soPhongThi);
            giamSatList.add(new GiamSat(i + 1, canBoList.get(canBoIndex++), phongThiDuocGiamSat));
        }

        return new AssignmentResult(phanCongList, giamSatList, soPhongThi, soCanBoGiamSat,
                tongCanBoCanDung, phongThiList.size(), canBoList.size());
    }

    private void validateInput(List<CanBo> canBoList, List<PhongThi> phongThiList,
                               int soPhongThi, int soCanBoGiamSat) {
        if (soPhongThi <= 0) {
            throw new IllegalArgumentException("Số phòng thi cần sử dụng phải lớn hơn 0.");
        }
        if (soCanBoGiamSat < 0) {
            throw new IllegalArgumentException("Số cán bộ giám sát không được âm.");
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
        int tongCanBoCanDung = soPhongThi * 2 + soCanBoGiamSat;
        if (tongCanBoCanDung > canBoList.size()) {
            throw new IllegalArgumentException("Không đủ cán bộ. Cần " + tongCanBoCanDung
                    + " cán bộ nhưng file chỉ có " + canBoList.size() + " cán bộ.");
        }
    }
}
