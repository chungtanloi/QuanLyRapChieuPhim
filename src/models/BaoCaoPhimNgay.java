package models;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

// Model để chứa dữ liệu cho Báo cáo (danh sách phim theo ngày)
public class BaoCaoPhimNgay {
    private final SimpleStringProperty tenPhim;
    private final SimpleIntegerProperty tongSuatChieu;
    private final SimpleIntegerProperty thoiLuongPhut;

    public BaoCaoPhimNgay(String tenPhim, int tongSuatChieu, int thoiLuongPhut) {
        this.tenPhim = new SimpleStringProperty(tenPhim);
        this.tongSuatChieu = new SimpleIntegerProperty(tongSuatChieu);
        this.thoiLuongPhut = new SimpleIntegerProperty(thoiLuongPhut);
    }
    
    // --- Getters cho JavaFX TableView ---
    public SimpleStringProperty tenPhimProperty() {
        return tenPhim;
    }
    public SimpleIntegerProperty tongSuatChieuProperty() {
        return tongSuatChieu;
    }
    public SimpleIntegerProperty thoiLuongPhutProperty() {
        return thoiLuongPhut;
    }
    
    // Getters thường
    public String getTenPhim() {
        return tenPhim.get();
    }
    public int getTongSuatChieu() {
        return tongSuatChieu.get();
    }
    public int getThoiLuongPhut() {
        return thoiLuongPhut.get();
    }
}