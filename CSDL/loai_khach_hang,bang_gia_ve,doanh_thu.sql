USE qlrapchieuphim;
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- Bảng Loại khách hàng
CREATE TABLE loai_khach_hang (
    ma_loai_khach_hang INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    ten_loai_khach_hang VARCHAR(50) NOT NULL UNIQUE,
    ti_le_giam_gia DECIMAL(5,2) DEFAULT 0,
    diem_toi_thieu INT UNSIGNED DEFAULT 0,
    mo_ta TEXT,
    tao_luc DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- Bảng Khách hàng (đã có - bổ sung thêm)
ALTER TABLE khach_hang 
ADD COLUMN ma_loai_khach_hang INT UNSIGNED,
ADD CONSTRAINT fk_khachhang_loaikhachhang 
FOREIGN KEY (ma_loai_khach_hang) REFERENCES loai_khach_hang(ma_loai_khach_hang);

-- Bảng Bảng giá vé
CREATE TABLE bang_gia_ve (
    ma_bang_gia INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    ma_loai_khach_hang INT UNSIGNED NOT NULL,
    ma_dinh_dang_phim INT UNSIGNED NOT NULL, -- 2D, 3D, IMAX
    gia_ve DECIMAL(10,2) NOT NULL,
    ngay_ap_dung DATE NOT NULL,
    trang_thai ENUM('AP_DUNG','NGUNG_AP_DUNG') DEFAULT 'AP_DUNG',
    tao_luc DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (ma_loai_khach_hang) REFERENCES loai_khach_hang(ma_loai_khach_hang)
);

-- Bảng Doanh thu
CREATE TABLE doanh_thu (
    ma_doanh_thu BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    ma_suat_chieu BIGINT UNSIGNED NOT NULL,
    ngay_chieu DATE NOT NULL,
    so_ve_da_ban INT UNSIGNED DEFAULT 0,
    tong_doanh_thu DECIMAL(15,2) DEFAULT 0,
    doanh_thu_thuc DECIMAL(15,2) DEFAULT 0,
    tao_luc DATETIME DEFAULT CURRENT_TIMESTAMP,
    cap_nhat_luc DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uq_doanhthu_suat_ngay (ma_suat_chieu, ngay_chieu),
    FOREIGN KEY (ma_suat_chieu) REFERENCES suat_chieu(ma_suat_chieu) ON DELETE CASCADE
);

SET FOREIGN_KEY_CHECKS = 1;

