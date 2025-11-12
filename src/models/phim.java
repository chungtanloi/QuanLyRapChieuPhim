package models;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;

// Lưu ý: Tên class nên là Phim (viết hoa chữ cái đầu) theo chuẩn Java
// Nhưng để khớp với code của bạn, tôi giữ nguyên 'phim'
public class phim { 
    
    // Khai báo các thuộc tính (fields)
    private final SimpleLongProperty maPhim;
    private final SimpleStringProperty tenPhim;
    private final SimpleIntegerProperty thoiLuongPhut;
    private final SimpleStringProperty theLoai; // Kết quả GROUP_CONCAT
    private final SimpleStringProperty phanLoai;
    private final SimpleStringProperty ngayPhatHanh; // Giữ dưới dạng String sau khi FORMAT SQL
    private final SimpleStringProperty trangThai; // Kết quả CASE WHEN SQL

    // Constructor phải KHỚP CHÍNH XÁC với thứ tự và kiểu dữ liệu từ ResultSet
    public phim(long maPhim, String tenPhim, int thoiLuongPhut, String theLoai, String phanLoai, String ngayPhatHanh, String trangThai) {
        this.maPhim = new SimpleLongProperty(maPhim);
        this.tenPhim = new SimpleStringProperty(tenPhim);
        this.thoiLuongPhut = new SimpleIntegerProperty(thoiLuongPhut);
        this.theLoai = new SimpleStringProperty(theLoai);
        this.phanLoai = new SimpleStringProperty(phanLoai);
        this.ngayPhatHanh = new SimpleStringProperty(ngayPhatHanh);
        this.trangThai = new SimpleStringProperty(trangThai);
    }
    
    // --- Các phương thức Getter/Setter/Property cho JavaFX TableView ---

    // PHẢI CÓ các Getter theo cú pháp: get<Tên thuộc tính> hoặc <Tên thuộc tính>Property
    
    // Getter chuẩn cho PropertyValueFactory
    public Long getMaPhim() {
        return maPhim.get();
    }
    // Dùng cho TableColumn<Phim, Long>
    public SimpleLongProperty maPhimProperty() {
        return maPhim;
    }
    
    public String getTenPhim() {
        return tenPhim.get();
    }
    public SimpleStringProperty tenPhimProperty() {
        return tenPhim;
    }
    
    public Integer getThoiLuongPhut() {
        return thoiLuongPhut.get();
    }
    public SimpleIntegerProperty thoiLuongPhutProperty() {
        return thoiLuongPhut;
    }
    
    public String getTheLoai() {
        return theLoai.get();
    }
    public SimpleStringProperty theLoaiProperty() {
        return theLoai;
    }
    
    public String getPhanLoai() {
        return phanLoai.get();
    }
    public SimpleStringProperty phanLoaiProperty() {
        return phanLoai;
    }
    
    public String getNgayPhatHanh() {
        return ngayPhatHanh.get();
    }
    public SimpleStringProperty ngayPhatHanhProperty() {
        return ngayPhatHanh;
    }
    
    public String getTrangThai() {
        return trangThai.get();
    }
    public SimpleStringProperty trangThaiProperty() {
        return trangThai;
    }
}