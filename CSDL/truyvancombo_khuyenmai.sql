
use qlrapchieuphim;
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;
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




-
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
