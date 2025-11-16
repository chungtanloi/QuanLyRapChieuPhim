-- =====================================================
-- TRIGGERS, FUNCTIONS & PROCEDURES CHO THỐNG KÊ DOANH THU
-- Hệ thống Quản lý Rạp Chiếu Phim
-- =====================================================

USE qlrapchieuphim;

DELIMITER $$

-- =====================================================
-- 1. FUNCTIONS - Hàm tính toán doanh thu
-- =====================================================

-- Hàm tính tổng doanh thu theo khoảng thời gian
DROP FUNCTION IF EXISTS fn_tinh_doanh_thu$$
CREATE FUNCTION fn_tinh_doanh_thu(
    p_tu_ngay DATE,
    p_den_ngay DATE
) RETURNS DECIMAL(15,2)
READS SQL DATA
BEGIN
    DECLARE v_doanh_thu DECIMAL(15,2);
    
    SELECT COALESCE(SUM(tong_tien), 0) INTO v_doanh_thu
    FROM don_hang
    WHERE trang_thai = 1
    AND DATE(dat_luc) BETWEEN p_tu_ngay AND p_den_ngay;
    
    RETURN v_doanh_thu;
END$$

-- Hàm tính doanh thu từ vé
DROP FUNCTION IF EXISTS fn_doanh_thu_ve$$
CREATE FUNCTION fn_doanh_thu_ve(
    p_tu_ngay DATE,
    p_den_ngay DATE
) RETURNS DECIMAL(15,2)
READS SQL DATA
BEGIN
    DECLARE v_doanh_thu DECIMAL(15,2);
    
    SELECT COALESCE(SUM(dv.don_gia), 0) INTO v_doanh_thu
    FROM don_ve dv
    JOIN don_hang dh ON dv.ma_don_hang = dh.ma_don_hang
    WHERE dh.trang_thai = 'DA_THANH_TOAN'
    AND DATE(dh.dat_luc) BETWEEN p_tu_ngay AND p_den_ngay;
    
    RETURN v_doanh_thu;
END$$

-- Hàm tính doanh thu từ hàng hóa
DROP FUNCTION IF EXISTS fn_doanh_thu_hang_hoa$$
CREATE FUNCTION fn_doanh_thu_hang_hoa(
    p_tu_ngay DATE,
    p_den_ngay DATE
) RETURNS DECIMAL(15,2)
READS SQL DATA
BEGIN
    DECLARE v_doanh_thu DECIMAL(15,2);
    
    SELECT COALESCE(SUM(hh.don_gia * hh.so_luong), 0) INTO v_doanh_thu
    FROM hang_hoa hh
    JOIN don_hang dh ON hh.ma_don_hang = dh.ma_don_hang
    WHERE dh.trang_thai = 'DA_THANH_TOAN'
    AND DATE(dh.dat_luc) BETWEEN p_tu_ngay AND p_den_ngay;
    
    RETURN v_doanh_thu;
END$$

-- Hàm tính tỷ lệ lấp đầy trung bình
DROP FUNCTION IF EXISTS fn_ty_le_lap_day$$
CREATE FUNCTION fn_ty_le_lap_day(
    p_tu_ngay DATE,
    p_den_ngay DATE
) RETURNS DECIMAL(5,2)
READS SQL DATA
BEGIN
    DECLARE v_ty_le DECIMAL(5,2);
    
    SELECT COALESCE(
        AVG((ve_da_ban * 100.0) / tong_ghe), 0
    ) INTO v_ty_le
    FROM (
        SELECT 
            sc.ma_suat_chieu,
            COUNT(CASE WHEN v.trang_thai = 'DA_BAN' THEN 1 END) as ve_da_ban,
            COUNT(*) as tong_ghe
        FROM suat_chieu sc
        JOIN ve v ON sc.ma_suat_chieu = v.ma_suat_chieu
        WHERE DATE(sc.bat_dau_luc) BETWEEN p_tu_ngay AND p_den_ngay
        AND sc.trang_thai != 'HUY'
        GROUP BY sc.ma_suat_chieu
    ) AS stats;
    
    RETURN v_ty_le;
END$$

-- Hàm tính số vé đã bán
DROP FUNCTION IF EXISTS fn_tong_ve_ban$$
CREATE FUNCTION fn_tong_ve_ban(
    p_tu_ngay DATE,
    p_den_ngay DATE
) RETURNS INT
READS SQL DATA
BEGIN
    DECLARE v_tong INT;
    
    SELECT COUNT(DISTINCT dv.ma_ve) INTO v_tong
    FROM don_ve dv
    JOIN don_hang dh ON dv.ma_don_hang = dh.ma_don_hang
    WHERE dh.trang_thai = 'DA_THANH_TOAN'
    AND DATE(dh.dat_luc) BETWEEN p_tu_ngay AND p_den_ngay;
    
    RETURN v_tong;
END$$

-- Hàm tính tỷ lệ tăng trưởng
DROP FUNCTION IF EXISTS fn_ty_le_tang_truong$$
CREATE FUNCTION fn_ty_le_tang_truong(
    p_gia_tri_hien_tai DECIMAL(15,2),
    p_gia_tri_truoc DECIMAL(15,2)
) RETURNS DECIMAL(10,2)
DETERMINISTIC
BEGIN
    IF p_gia_tri_truoc = 0 THEN
        RETURN 0;
    END IF;
    
    RETURN ROUND(((p_gia_tri_hien_tai - p_gia_tri_truoc) / p_gia_tri_truoc) * 100, 2);
END$$

DELIMITER ;
select * from don_hang;