
use qlrapchieuphim;
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;


SET SQL_SAFE_UPDATES = 0;

DELETE FROM san_pham
WHERE ten_san_pham = 'Combo Bap + Nước';

-- Sau khi xóa xong, bạn nên bật lại để đảm bảo an toàn
SET SQL_SAFE_UPDATES = 1;

-- Truy vấn lấy danh sách combo đang hoạt động:
SELECT 
    ma_combo,
    ten_combo,
    gia,
    mo_ta,
    hoat_dong,
    tao_luc
FROM combo 
WHERE hoat_dong = 1
ORDER BY ma_combo;


-- Truy vấn chi tiết combo kèm sản phẩm:
SELECT 
    c.ma_combo,
    c.ten_combo,
    c.gia as gia_combo,
    c.mo_ta,
    sp.ma_san_pham,
    sp.ten_san_pham,
    sp.gia as gia_san_pham,
    sp.loai,
    ct.so_luong,
    (sp.gia * ct.so_luong) as tong_gia_goc,
    (c.gia - (sp.gia * ct.so_luong)) as tien_tiet_kiem
FROM combo c
JOIN combo_chi_tiet ct ON c.ma_combo = ct.ma_combo
JOIN san_pham sp ON ct.ma_san_pham = sp.ma_san_pham
WHERE c.hoat_dong = 1 AND sp.hoat_dong = 1
ORDER BY c.ma_combo, sp.ma_san_pham;

-- Truy vấn thống kê combo

SELECT 
    c.ma_combo,
    c.ten_combo,
    c.gia,
    COUNT(ct.ma_san_pham) as so_loai_san_pham,
    SUM(ct.so_luong) as tong_so_luong
FROM combo c
LEFT JOIN combo_chi_tiet ct ON c.ma_combo = ct.ma_combo
WHERE c.hoat_dong = 1
GROUP BY c.ma_combo, c.ten_combo, c.gia
ORDER BY c.ma_combo;




-- Thêm dữ liệu mẫu cho khuyến mãi với ngày giờ hiện tại
INSERT INTO khuyen_mai (ma_code, kieu_giam, gia_tri_giam, bat_dau_luc, ket_thuc_luc, don_toi_thieu, hoat_dong) VALUES
-- Khuyến mãi ĐANG DIỄN RA (bắt đầu từ hôm qua, kết thúc sau 7 ngày)
('SALE_NOW1', 'PHAN_TRAM', 20.00, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 7 DAY), 150000.00, 1),
('SALE_NOW2', 'SO_TIEN', 50000.00, DATE_SUB(NOW(), INTERVAL 2 HOUR), DATE_ADD(NOW(), INTERVAL 30 DAY), 200000.00, 1),

-- Khuyến mãi SẮP DIỄN RA (bắt đầu sau 1 ngày)
('SALE_SOON1', 'PHAN_TRAM', 25.00, DATE_ADD(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 15 DAY), 180000.00, 1),
('SALE_SOON2', 'SO_TIEN', 35000.00, DATE_ADD(NOW(), INTERVAL 3 DAY), DATE_ADD(NOW(), INTERVAL 20 DAY), 120000.00, 1),

-- Khuyến mãi ĐÃ HẾT HẠN (kết thúc cách đây 1 ngày)
('SALE_END1', 'PHAN_TRAM', 15.00, DATE_SUB(NOW(), INTERVAL 10 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY), 100000.00, 0),
('SALE_END2', 'SO_TIEN', 20000.00, DATE_SUB(NOW(), INTERVAL 15 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY), 80000.00, 0),

-- Khuyến mãi theo giờ (hôm nay từ 8h-12h)
('MORNING_SPECIAL', 'PHAN_TRAM', 10.00, 
 CONCAT(CURDATE(), ' 08:00:00'), 
 CONCAT(CURDATE(), ' 12:00:00'), 
 80000.00, 1),

-- Khuyến mãi theo giờ (tối nay từ 18h-23h)
('EVENING_SPECIAL', 'SO_TIEN', 25000.00, 
 CONCAT(CURDATE(), ' 18:00:00'), 
 CONCAT(CURDATE(), ' 23:00:00'), 
 120000.00, 1),

-- Khuyến mãi cuối tuần (thứ 7, chủ nhật)
('WEEKEND_FUN', 'PHAN_TRAM', 15.00, 
 DATE_SUB(NOW(), INTERVAL 1 DAY),  -- Bắt đầu từ hôm qua
 DATE_ADD(NOW(), INTERVAL 2 DAY),  -- Kết thúc sau 2 ngày
 100000.00, 1),

-- Khuyến mãi thành viên (luôn áp dụng)
('MEMBER_VIP', 'PHAN_TRAM', 10.00, DATE_SUB(NOW(), INTERVAL 30 DAY), DATE_ADD(NOW(), INTERVAL 30 DAY), 0.00, 1),
('MEMBER_GOLD', 'PHAN_TRAM', 15.00, DATE_SUB(NOW(), INTERVAL 30 DAY), DATE_ADD(NOW(), INTERVAL 30 DAY), 0.00, 1);



-- 1. Cập nhật loại khách hàng cho các khách hàng đã có
UPDATE khach_hang kh
JOIN tai_khoan tk ON kh.ma_tai_khoan = tk.ma_tai_khoan
SET kh.ma_loai_khach_hang = 
    CASE 
        WHEN kh.diem_tich_luy >= 1000 THEN 4  -- KIM_CUONG
        WHEN kh.diem_tich_luy >= 500 THEN 3   -- BACH_KIM
        WHEN kh.diem_tich_luy >= 100 THEN 2   -- VANG
        ELSE 1                                -- BAC
    END
WHERE kh.ma_loai_khach_hang IS NULL;

-- 2. Thêm đơn hàng (sử dụng mã khách hàng thực tế)
INSERT INTO don_hang (ma_khach_hang, dat_luc, kenh, trang_thai, ghi_chu, tong_tien) 
SELECT 
    kh.ma_khach_hang,
    CASE 
        WHEN tk.email = 'kh1.cantho@example.com' THEN DATE_SUB(NOW(), INTERVAL 5 DAY)
        WHEN tk.email = 'kh2.cantho@example.com' THEN DATE_SUB(NOW(), INTERVAL 3 DAY)
        ELSE NOW()
    END as dat_luc,
    CASE 
        WHEN tk.email = 'kh1.cantho@example.com' THEN 'TRUC_TIEP'
        WHEN tk.email = 'kh2.cantho@example.com' THEN 'TRUC_TUYEN'
        ELSE 'TRUC_TIEP'
    END as kenh,
    'DA_THANH_TOAN' as trang_thai,
    CASE 
        WHEN tk.email = 'kh1.cantho@example.com' THEN 'Mua vé xem phim hành động'
        WHEN tk.email = 'kh2.cantho@example.com' THEN 'Đặt online combo bắp nước'
        ELSE 'Giao dịch tại quầy'
    END as ghi_chu,
    CASE 
        WHEN tk.email = 'kh1.cantho@example.com' THEN 185000.00
        WHEN tk.email = 'kh2.cantho@example.com' THEN 220000.00
        ELSE 150000.00
    END as tong_tien
FROM khach_hang kh
JOIN tai_khoan tk ON kh.ma_tai_khoan = tk.ma_tai_khoan
WHERE tk.vai_tro = 'KHACH_HANG';

-- Thêm thêm vài đơn hàng vãng lai
INSERT INTO don_hang (ma_khach_hang, dat_luc, kenh, trang_thai, ghi_chu, tong_tien) VALUES
(NULL, DATE_SUB(NOW(), INTERVAL 2 DAY), 'TRUC_TIEP', 'DA_THANH_TOAN', 'Khách vãng lai - vé 2D', 80000.00),
(NULL, DATE_SUB(NOW(), INTERVAL 1 DAY), 'TRUC_TIEP', 'CHO_THANH_TOAN', 'Khách vãng lai - đang chờ', 120000.00),
(NULL, NOW(), 'TRUC_TIEP', 'DA_HUY', 'Khách hủy vé', 95000.00);

-- 3. Thêm khuyến mãi (nếu chưa có)
INSERT IGNORE INTO khuyen_mai (ma_code, kieu_giam, gia_tri_giam, bat_dau_luc, ket_thuc_luc, don_toi_thieu, hoat_dong) VALUES
('SALE20', 'PHAN_TRAM', 20.00, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 7 DAY), 150000.00, 1),
('GIAM50K', 'SO_TIEN', 50000.00, DATE_SUB(NOW(), INTERVAL 2 HOUR), DATE_ADD(NOW(), INTERVAL 30 DAY), 200000.00, 1),
('FREESHIP', 'SO_TIEN', 25000.00, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 15 DAY), 100000.00, 1),
('HOTSALE', 'PHAN_TRAM', 30.00, DATE_ADD(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 10 DAY), 300000.00, 1);

-- 4. Liên kết đơn hàng với khuyến mãi
INSERT IGNORE INTO don_khuyen_mai (ma_don_hang, ma_khuyen_mai) VALUES
(1, 1),  -- Đơn 1 dùng SALE20
(2, 2),  -- Đơn 2 dùng GIAM50K
(3, 1),  -- Đơn 3 dùng SALE20
(4, 3);  -- Đơn 4 dùng FREESHIP




-- Kiểm tra toàn bộ dữ liệu khách hàng + đơn hàng + khuyến mãi
SELECT 
    dh.ma_don_hang,
    COALESCE(tk.ho_ten, '🎫 Khách vãng lai') as ten_khach,
    tk.email,
    lkh.ten_loai_khach_hang,
    kh.diem_tich_luy,
    dh.trang_thai,
    dh.tong_tien,
    km.ma_code as ma_khuyen_mai,
    CASE 
        WHEN km.kieu_giam = 'PHAN_TRAM' THEN CONCAT(km.gia_tri_giam, '%')
        WHEN km.kieu_giam = 'SO_TIEN' THEN CONCAT(FORMAT(km.gia_tri_giam, 0), 'đ')
        ELSE 'Không có'
    END as khuyen_mai,
    DATE_FORMAT(dh.dat_luc, '%d/%m/%Y %H:%i') as thoi_gian
FROM don_hang dh
LEFT JOIN khach_hang kh ON dh.ma_khach_hang = kh.ma_khach_hang
LEFT JOIN tai_khoan tk ON kh.ma_tai_khoan = tk.ma_tai_khoan
LEFT JOIN loai_khach_hang lkh ON kh.ma_loai_khach_hang = lkh.ma_loai_khach_hang
LEFT JOIN don_khuyen_mai dkm ON dh.ma_don_hang = dkm.ma_don_hang
LEFT JOIN khuyen_mai km ON dkm.ma_khuyen_mai = km.ma_khuyen_mai
ORDER BY dh.ma_don_hang;

-- Kiểm tra khuyến mãi đang hoạt động
SELECT 
    ma_khuyen_mai,
    ma_code as 'Mã KM',
    kieu_giam as 'Loại giảm',
    CASE 
        WHEN kieu_giam = 'PHAN_TRAM' THEN CONCAT(gia_tri_giam, '%')
        ELSE CONCAT(FORMAT(gia_tri_giam, 0), ' VNĐ')
    END as 'Mức giảm',
    CONCAT(FORMAT(don_toi_thieu, 0), ' VNĐ') as 'Đơn tối thiểu',
    DATE_FORMAT(bat_dau_luc, '%d/%m/%Y %H:%i') as 'Bắt đầu',
    DATE_FORMAT(ket_thuc_luc, '%d/%m/%Y %H:%i') as 'Kết thúc',
    CASE 
        WHEN hoat_dong = 1 AND bat_dau_luc <= NOW() AND ket_thuc_luc >= NOW() THEN '🟢 ĐANG ÁP DỤNG'
        WHEN ket_thuc_luc < NOW() THEN '🔴 HẾT HẠN' 
        WHEN bat_dau_luc > NOW() THEN '🟡 SẮP DIỄN RA'
        ELSE '⚪ KHÔNG ÁP DỤNG'
    END as 'Trạng thái'
FROM khuyen_mai 
ORDER BY 
    CASE 
        WHEN bat_dau_luc <= NOW() AND ket_thuc_luc >= NOW() THEN 1
        WHEN bat_dau_luc > NOW() THEN 2
        ELSE 3
    END,
    bat_dau_luc DESC;
