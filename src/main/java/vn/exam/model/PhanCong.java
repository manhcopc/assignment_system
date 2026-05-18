package vn.exam.model;

public class PhanCong {
    private int caThi;
    private int stt;
    private CanBo canBo;
    private PhongThi phongThi;
    private String vaiTro;

    public PhanCong(int caThi, int stt, CanBo canBo, PhongThi phongThi, String vaiTro) {
        this.caThi = caThi;
        this.stt = stt;
        this.canBo = canBo;
        this.phongThi = phongThi;
        this.vaiTro = vaiTro;
    }

    public int getCaThi() { return caThi; }
    public int getStt() { return stt; }
    public CanBo getCanBo() { return canBo; }
    public PhongThi getPhongThi() { return phongThi; }
    public String getVaiTro() { return vaiTro; }
}
