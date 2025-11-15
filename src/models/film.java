package models;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 * Model film dùng cho:
 *  - Hiển thị danh sách phim (7 thuộc tính)
 *  - Hiển thị chi tiết film (mô tả + quốc gia)
 *
 * Class này đã được chuẩn hóa để hoạt động 100% với:
 *  - PhimController.java
 *  - ThemSuaphimController.java
 */
public class film {

    // ====== Các thuộc tính dùng cho TableView ======
    private final SimpleLongProperty maPhim;
    private final SimpleStringProperty tenPhim;
    private final SimpleIntegerProperty thoiLuongPhut;
    private final SimpleStringProperty theLoai;
    private final SimpleStringProperty phanLoai;
    private final SimpleStringProperty ngayPhatHanh;
    private final SimpleStringProperty trangThai;

    // ====== Các thuộc tính dùng khi xem/sửa film ======
    private String moTa;      // KHÔNG dùng Property vì chỉ hiển thị TextArea
    private String quocGia;   // tương tự

    // ============================================================
    // Constructor 1 – dùng cho TableView (7 fields)
    // ============================================================
    public film(long maPhim, String tenPhim, int thoiLuongPhut,
                String theLoai, String phanLoai,
                String ngayPhatHanh, String trangThai) {

        this.maPhim = new SimpleLongProperty(maPhim);
        this.tenPhim = new SimpleStringProperty(tenPhim);
        this.thoiLuongPhut = new SimpleIntegerProperty(thoiLuongPhut);
        this.theLoai = new SimpleStringProperty(theLoai);
        this.phanLoai = new SimpleStringProperty(phanLoai);
        this.ngayPhatHanh = new SimpleStringProperty(ngayPhatHanh);
        this.trangThai = new SimpleStringProperty(trangThai);

        // Chi tiết
        this.moTa = "";
        this.quocGia = "";
    }

    // ============================================================
    // Constructor 2 – dùng khi lấy chi tiết từ CSDL (Sửa phim)
    // ============================================================
    public film(long maPhim) {
        this.maPhim = new SimpleLongProperty(maPhim);

        this.tenPhim = new SimpleStringProperty("");
        this.thoiLuongPhut = new SimpleIntegerProperty(0);
        this.theLoai = new SimpleStringProperty("");
        this.phanLoai = new SimpleStringProperty("");
        this.ngayPhatHanh = new SimpleStringProperty("");
        this.trangThai = new SimpleStringProperty("");

        this.moTa = "";
        this.quocGia = "";
    }

    // ============================================================
    // Getter – Setter – Property cho TableView
    // ============================================================
    public long getMaPhim() { return maPhim.get(); }
    public SimpleLongProperty maPhimProperty() { return maPhim; }

    public String getTenPhim() { return tenPhim.get(); }
    
public SimpleStringProperty tenPhimProperty() { return tenPhim; }

    public int getThoiLuongPhut() { return thoiLuongPhut.get(); }
    public SimpleIntegerProperty thoiLuongPhutProperty() { return thoiLuongPhut; }

    public String getTheLoai() { return theLoai.get(); }
    public SimpleStringProperty theLoaiProperty() { return theLoai; }

    public String getPhanLoai() { return phanLoai.get(); }
    public SimpleStringProperty phanLoaiProperty() { return phanLoai; }

    public String getNgayPhatHanh() { return ngayPhatHanh.get(); }
    public SimpleStringProperty ngayPhatHanhProperty() { return ngayPhatHanh; }

    public String getTrangThai() { return trangThai.get(); }
    public SimpleStringProperty trangThaiProperty() { return trangThai; }

    // ============================================================
    // Getter/Setter cho Chi Tiết Phim (không phải Property)
    // ============================================================
    public String getMoTa() { return moTa; }
    public void setMoTa(String moTa) { this.moTa = moTa; }

    public String getQuocGia() { return quocGia; }
    public void setQuocGia(String quocGia) { this.quocGia = quocGia; }

    // THÊM PHƯƠNG THỨC NÀY
    public void setNgayPhatHanh(String ngayPhatHanh) { 
        this.ngayPhatHanh.set(ngayPhatHanh); 
    }
}