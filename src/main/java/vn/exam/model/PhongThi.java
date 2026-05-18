package vn.exam.model;

public class PhongThi {
    private int stt;
    private String tenPhong;
    private String ghiChu;

    public PhongThi(int stt, String tenPhong, String ghiChu) {
        this.stt = stt;
        this.tenPhong = tenPhong;
        this.ghiChu = ghiChu;
    }

    public int getStt() { return stt; }
    public String getTenPhong() { return tenPhong; }
    public String getGhiChu() { return ghiChu; }
}
