package models;

import javafx.beans.property.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class SuatChieu {
    private final LongProperty maSuatChieu;
    private final StringProperty tenPhim;
    private final StringProperty tenPhong;
    private final ObjectProperty<LocalDateTime> batDauLuc;
    private final StringProperty dinhDang;
    private final ObjectProperty<BigDecimal> giaCoBan;
    private final StringProperty trangThai;
    
    // Thêm trường Long maPhim và maPhong (để phục vụ cho chức năng SỬA)
    private Long maPhim; 
    private Integer maPhong; 

    public SuatChieu(long maSuatChieu, String tenPhim, String tenPhong, 
                      LocalDateTime batDauLuc, String dinhDang, 
                      BigDecimal giaCoBan, String trangThai) {
        this.maSuatChieu = new SimpleLongProperty(maSuatChieu);
        this.tenPhim = new SimpleStringProperty(tenPhim);
        this.tenPhong = new SimpleStringProperty(tenPhong);
        this.batDauLuc = new SimpleObjectProperty<>(batDauLuc);
        this.dinhDang = new SimpleStringProperty(dinhDang);
        this.giaCoBan = new SimpleObjectProperty<>(giaCoBan);
        this.trangThai = new SimpleStringProperty(trangThai);
    }
    
    // Constructor đầy đủ (dùng cho SỬA/Lưu khi cần lấy ID phim/phòng)
    public SuatChieu(long maSuatChieu, String tenPhim, String tenPhong, 
                      LocalDateTime batDauLuc, String dinhDang, 
                      BigDecimal giaCoBan, String trangThai, Long maPhim, Integer maPhong) {
        this(maSuatChieu, tenPhim, tenPhong, batDauLuc, dinhDang, giaCoBan, trangThai);
        this.maPhim = maPhim;
        this.maPhong = maPhong;
    }

    // Getters
    public long getMaSuatChieu() { return maSuatChieu.get(); }
    public String getTenPhim() { return tenPhim.get(); }
    public String getTenPhong() { return tenPhong.get(); }
    public LocalDateTime getBatDauLuc() { return batDauLuc.get(); }
    public String getDinhDang() { return dinhDang.get(); }
    public BigDecimal getGiaCoBan() { return giaCoBan.get(); }
    public String getTrangThai() { return trangThai.get(); }
    public Long getMaPhim() { return maPhim; }
    public Integer getMaPhong() { return maPhong; }

    // Setters (cho Sửa)
    public void setMaPhim(Long maPhim) { this.maPhim = maPhim; }
    public void setMaPhong(Integer maPhong) { this.maPhong = maPhong; }

    // Property Getters
    public LongProperty maSuatChieuProperty() { return maSuatChieu; }
    public StringProperty tenPhimProperty() { return tenPhim; }
    public StringProperty tenPhongProperty() { return tenPhong; }
    public ObjectProperty<LocalDateTime> batDauLucProperty() { return batDauLuc; }
    public StringProperty dinhDangProperty() { return dinhDang; }
    public ObjectProperty<BigDecimal> giaCoBanProperty() { return giaCoBan; }
    public StringProperty trangThaiProperty() { return trangThai; }
}