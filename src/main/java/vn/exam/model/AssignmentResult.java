package vn.exam.model;

import java.util.List;

public class AssignmentResult {
    private List<PhanCong> danhSachPhanCong;
    private List<GiamSat> danhSachGiamSat;
    private int soPhongThiSuDung;
    private int soCanBoGiamSat;
    private int tongCanBoCanDung;
    private int tongPhongTrongFile;
    private int tongCanBoTrongFile;

    public AssignmentResult(List<PhanCong> danhSachPhanCong, List<GiamSat> danhSachGiamSat,
                            int soPhongThiSuDung, int soCanBoGiamSat, int tongCanBoCanDung,
                            int tongPhongTrongFile, int tongCanBoTrongFile) {
        this.danhSachPhanCong = danhSachPhanCong;
        this.danhSachGiamSat = danhSachGiamSat;
        this.soPhongThiSuDung = soPhongThiSuDung;
        this.soCanBoGiamSat = soCanBoGiamSat;
        this.tongCanBoCanDung = tongCanBoCanDung;
        this.tongPhongTrongFile = tongPhongTrongFile;
        this.tongCanBoTrongFile = tongCanBoTrongFile;
    }

    public List<PhanCong> getDanhSachPhanCong() { return danhSachPhanCong; }
    public List<GiamSat> getDanhSachGiamSat() { return danhSachGiamSat; }
    public int getSoPhongThiSuDung() { return soPhongThiSuDung; }
    public int getSoCanBoGiamSat() { return soCanBoGiamSat; }
    public int getTongCanBoCanDung() { return tongCanBoCanDung; }
    public int getTongPhongTrongFile() { return tongPhongTrongFile; }
    public int getTongCanBoTrongFile() { return tongCanBoTrongFile; }
}
