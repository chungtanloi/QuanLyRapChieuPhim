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

-- CHÈN DỮ LIỆU MẪU

-- Chèn dữ liệu loại khách hàng
INSERT INTO loai_khach_hang (ten_loai_khach_hang, ti_le_giam_gia, diem_toi_thieu, mo_ta) VALUES
('Thường', 0.00, 0, 'Khách hàng mới'),
('Bạc', 0.05, 100, 'Khách hàng thân thiết'),
('Vàng', 0.10, 500, 'Khách hàng VIP'),
('Kim Cương', 0.15, 1000, 'Khách hàng cao cấp');

-- Chèn dữ liệu bảng giá vé
INSERT INTO bang_gia_ve (ma_loai_khach_hang, ma_dinh_dang_phim, gia_ve, ngay_ap_dung, trang_thai) VALUES
-- Giá vé cho loại Thường
(1, 1, 60000, '2024-01-01', 'AP_DUNG'),
(1, 2, 80000, '2024-01-01', 'AP_DUNG'),
(1, 3, 100000, '2024-01-01', 'AP_DUNG'),

-- Giá vé cho loại Bạc (giảm 5%)
(2, 1, 57000, '2024-01-01', 'AP_DUNG'),
(2, 2, 76000, '2024-01-01', 'AP_DUNG'),
(2, 3, 95000, '2024-01-01', 'AP_DUNG'),

-- Giá vé cho loại Vàng (giảm 10%)
(3, 1, 54000, '2024-01-01', 'AP_DUNG'),
(3, 2, 72000, '2024-01-01', 'AP_DUNG'),
(3, 3, 90000, '2024-01-01', 'AP_DUNG'),

-- Giá vé cho loại Kim Cương (giảm 15%)
(4, 1, 51000, '2024-01-01', 'AP_DUNG'),
(4, 2, 68000, '2024-01-01', 'AP_DUNG'),
(4, 3, 85000, '2024-01-01', 'AP_DUNG');

-- Chèn dữ liệu mẫu cho doanh thu (giả sử đã có suất chiếu)
INSERT IGNORE INTO doanh_thu (ma_suat_chieu, ngay_chieu, so_ve_da_ban, tong_doanh_thu, doanh_thu_thuc) VALUES
(1, '2024-01-15', 50, 3000000, 3000000),
(2, '2024-01-15', 30, 2400000, 2400000),
(3, '2024-01-16', 25, 2000000, 2000000);