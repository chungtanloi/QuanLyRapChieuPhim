package models;

import javafx.beans.property.*;

import java.time.LocalDateTime;

public class LichChieu {
    private final LongProperty maSuatChieu;
    private final StringProperty tenPhim;
    private final StringProperty tenPhong;
    private final ObjectProperty<LocalDateTime> batDauLuc;
    private final ObjectProperty<LocalDateTime> ketThucLuc;
    private final StringProperty dinhDang;
    private final StringProperty trangThai;
    private final StringProperty mauSac;
    
    public LichChieu(long maSuatChieu, String tenPhim, String tenPhong, 
                        LocalDateTime batDauLuc, LocalDateTime ketThucLuc, 
                        String dinhDang, String trangThai) {
        this.maSuatChieu = new SimpleLongProperty(maSuatChieu);
        this.tenPhim = new SimpleStringProperty(tenPhim);
        this.tenPhong = new SimpleStringProperty(tenPhong);
        this.batDauLuc = new SimpleObjectProperty<>(batDauLuc);
        this.ketThucLuc = new SimpleObjectProperty<>(ketThucLuc);
        this.dinhDang = new SimpleStringProperty(dinhDang);
        this.trangThai = new SimpleStringProperty(trangThai);
        this.mauSac = new SimpleStringProperty(determineColor(trangThai));
    }
    
    private String determineColor(String trangThai) {
        return switch (trangThai) {
            case "SẮP CHIẾU" -> "#4CAF50"; // Xanh lá
            case "ĐANG CHIẾU" -> "#2196F3"; // Xanh dương
            case "ĐÃ CHIẾU" -> "#9E9E9E"; // Xám
            default -> "#FF9800"; // Cam
        };
    }
    
    // Getters
    public long getMaSuatChieu() { return maSuatChieu.get(); }
    public String getTenPhim() { return tenPhim.get(); }
    public String getTenPhong() { return tenPhong.get(); }
    public LocalDateTime getBatDauLuc() { return batDauLuc.get(); }
    public LocalDateTime getKetThucLuc() { return ketThucLuc.get(); }
    public String getDinhDang() { return dinhDang.get(); }
    public String getTrangThai() { return trangThai.get(); }
    public String getMauSac() { return mauSac.get(); }
    
    // Property getters
    public LongProperty maSuatChieuProperty() { return maSuatChieu; }
    public StringProperty tenPhimProperty() { return tenPhim; }
    public StringProperty tenPhongProperty() { return tenPhong; }
    public ObjectProperty<LocalDateTime> batDauLucProperty() { return batDauLuc; }
    public ObjectProperty<LocalDateTime> ketThucLucProperty() { return ketThucLuc; }
    public StringProperty dinhDangProperty() { return dinhDang; }
    public StringProperty trangThaiProperty() { return trangThai; }
    public StringProperty mauSacProperty() { return mauSac; }
}