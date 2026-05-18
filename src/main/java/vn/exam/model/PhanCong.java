package vn.exam.model;

public class PhanCong {
    private int stt;
    private CanBo canBo;
    private boolean giamThi1;
    private boolean giamThi2;
    private PhongThi phongThi;

    public PhanCong(int stt, CanBo canBo, boolean giamThi1, boolean giamThi2, PhongThi phongThi) {
        this.stt = stt;
        this.canBo = canBo;
        this.giamThi1 = giamThi1;
        this.giamThi2 = giamThi2;
        this.phongThi = phongThi;
    }

    public int getStt() { return stt; }
    public CanBo getCanBo() { return canBo; }
    public boolean isGiamThi1() { return giamThi1; }
    public boolean isGiamThi2() { return giamThi2; }
    public PhongThi getPhongThi() { return phongThi; }
}
