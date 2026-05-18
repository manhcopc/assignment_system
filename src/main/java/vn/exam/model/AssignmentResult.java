package vn.exam.model;

import java.util.List;

public class AssignmentResult {
    private List<PhanCong> danhSachPhanCong;
    private List<GiamSat> danhSachGiamSat;

    public AssignmentResult(List<PhanCong> danhSachPhanCong, List<GiamSat> danhSachGiamSat) {
        this.danhSachPhanCong = danhSachPhanCong;
        this.danhSachGiamSat = danhSachGiamSat;
    }

    public List<PhanCong> getDanhSachPhanCong() { return danhSachPhanCong; }
    public List<GiamSat> getDanhSachGiamSat() { return danhSachGiamSat; }
}
