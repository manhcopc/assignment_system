package vn.exam.model;

public class CanBo {
    private int tt;
    private String maGv;
    private String hoTen;
    private String ngaySinh;
    private String donViCongTac;

    public CanBo(int tt, String maGv, String hoTen, String ngaySinh, String donViCongTac) {
        this.tt = tt;
        this.maGv = maGv;
        this.hoTen = hoTen;
        this.ngaySinh = ngaySinh;
        this.donViCongTac = donViCongTac;
    }

    public int getTt() { return tt; }
    public String getMaGv() { return maGv; }
    public String getHoTen() { return hoTen; }
    public String getNgaySinh() { return ngaySinh; }
    public String getDonViCongTac() { return donViCongTac; }
}
