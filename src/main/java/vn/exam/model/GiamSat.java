package vn.exam.model;

public class GiamSat {
    private int stt;
    private CanBo canBo;
    private PhongThi phongThiDuocGiamSat;

    public GiamSat(int stt, CanBo canBo, PhongThi phongThiDuocGiamSat) {
        this.stt = stt;
        this.canBo = canBo;
        this.phongThiDuocGiamSat = phongThiDuocGiamSat;
    }

    public int getStt() { return stt; }
    public CanBo getCanBo() { return canBo; }
    public PhongThi getPhongThiDuocGiamSat() { return phongThiDuocGiamSat; }
}
