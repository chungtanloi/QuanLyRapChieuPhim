package models;

import java.time.LocalDateTime;

public class DonHang {
    private long maDonHang;
    private long maKhachHang;
    private String kenh;
    private String trangThai;
    private String ghiChu;
    private LocalDateTime taoLuc;

    public DonHang(long maDon, long maKH, String kenh, String trangThai, String ghiChu, LocalDateTime taoLuc) {
        this.maDonHang = maDon;
        this.maKhachHang = maKH;
        this.kenh = kenh;
        this.trangThai = trangThai;
        this.ghiChu = ghiChu;
        this.taoLuc = taoLuc;
    }

    public long getMaDonHang() { return maDonHang; }
    public long getMaKhachHang() { return maKhachHang; }
    public String getKenh() { return kenh; }
    public String getTrangThai() { return trangThai; }
    public String getGhiChu() { return ghiChu; }
    public LocalDateTime getTaoLuc() { return taoLuc; }
}
