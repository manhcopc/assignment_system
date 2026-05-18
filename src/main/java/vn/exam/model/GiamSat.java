package vn.exam.model;

public class GiamSat {
    private int caThi;
    private int stt;
    private CanBo canBo;
    private PhongThi phongThiDuocGiamSat;

    public GiamSat(int caThi, int stt, CanBo canBo, PhongThi phongThiDuocGiamSat) {
        this.caThi = caThi;
        this.stt = stt;
        this.canBo = canBo;
        this.phongThiDuocGiamSat = phongThiDuocGiamSat;
    }

    public int getCaThi() { return caThi; }
    public int getStt() { return stt; }
    public CanBo getCanBo() { return canBo; }
    public PhongThi getPhongThiDuocGiamSat() { return phongThiDuocGiamSat; }
}
