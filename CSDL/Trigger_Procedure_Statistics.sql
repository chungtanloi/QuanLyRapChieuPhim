-- =====================================================
-- SCRIPT KHẮC PHỤC - Tạo Stored Procedures
-- Chạy script này trong MySQL Workbench
-- =====================================================

USE qlrapchieuphim;

DELIMITER $$

-- =====================================================
-- 1. Procedure: Tổng quan doanh thu
-- =====================================================
DROP PROCEDURE IF EXISTS sp_tong_quan_doanh_thu$$
CREATE PROCEDURE sp_tong_quan_doanh_thu(
    IN p_tu_ngay DATE,
    IN p_den_ngay DATE
)
BEGIN
    DECLARE v_so_ngay INT;
    DECLARE v_tu_ngay_truoc DATE;
    DECLARE v_den_ngay_truoc DATE;
    
    -- Tính kỳ trước
    SET v_so_ngay = DATEDIFF(p_den_ngay, p_tu_ngay) + 1;
    SET v_den_ngay_truoc = DATE_SUB(p_tu_ngay, INTERVAL 1 DAY);
    SET v_tu_ngay_truoc = DATE_SUB(v_den_ngay_truoc, INTERVAL v_so_ngay - 1 DAY);
    
    SELECT 
        -- Kỳ hiện tại
        COALESCE(SUM(CASE WHEN DATE(dh.dat_luc) BETWEEN p_tu_ngay AND p_den_ngay 
            THEN dh.tong_tien ELSE 0 END), 0) AS tong_doanh_thu,
        
        COUNT(DISTINCT CASE WHEN DATE(dh.dat_luc) BETWEEN p_tu_ngay AND p_den_ngay 
            THEN dv.ma_ve END) AS tong_ve_ban,
        
        (SELECT COUNT(*) FROM khach_hang 
         WHERE DATE(tao_luc) BETWEEN p_tu_ngay AND p_den_ngay) AS khach_hang_moi,
        
        (SELECT COUNT(*) FROM suat_chieu 
         WHERE DATE(bat_dau_luc) BETWEEN p_tu_ngay AND p_den_ngay 
         AND trang_thai != 'HUY') AS tong_suat_chieu,
        
        -- Kỳ trước
        COALESCE(SUM(CASE WHEN DATE(dh.dat_luc) BETWEEN v_tu_ngay_truoc AND v_den_ngay_truoc 
            THEN dh.tong_tien ELSE 0 END), 0) AS doanh_thu_ky_truoc,
        
        COUNT(DISTINCT CASE WHEN DATE(dh.dat_luc) BETWEEN v_tu_ngay_truoc AND v_den_ngay_truoc 
            THEN dv.ma_ve END) AS ve_ban_ky_truoc,
        
        (SELECT COUNT(*) FROM khach_hang 
         WHERE DATE(tao_luc) BETWEEN v_tu_ngay_truoc AND v_den_ngay_truoc) AS khach_hang_ky_truoc,
        
        (SELECT COUNT(*) FROM suat_chieu 
         WHERE DATE(bat_dau_luc) BETWEEN v_tu_ngay_truoc AND v_den_ngay_truoc 
         AND trang_thai != 'HUY') AS suat_chieu_ky_truoc,
        
        0 AS ty_le_lap_day
    FROM don_hang dh
    LEFT JOIN don_ve dv ON dh.ma_don_hang = dv.ma_don_hang
    WHERE dh.trang_thai = 'DA_THANH_TOAN';
END$$

-- =====================================================
-- 2. Procedure: Doanh thu theo ngày
-- =====================================================
DROP PROCEDURE IF EXISTS sp_doanh_thu_theo_ngay$$
CREATE PROCEDURE sp_doanh_thu_theo_ngay(
    IN p_tu_ngay DATE,
    IN p_den_ngay DATE
)
BEGIN
    SELECT 
        DATE(dh.dat_luc) as ngay,
        COUNT(DISTINCT dh.ma_don_hang) as so_don_hang,
        COUNT(DISTINCT dv.ma_ve) as so_ve_ban,
        COALESCE(SUM(dh.tong_tien), 0) as doanh_thu
    FROM don_hang dh
    LEFT JOIN don_ve dv ON dh.ma_don_hang = dv.ma_don_hang
    WHERE dh.trang_thai = 'DA_THANH_TOAN'
    AND DATE(dh.dat_luc) BETWEEN p_tu_ngay AND p_den_ngay
    GROUP BY DATE(dh.dat_luc)
    ORDER BY ngay;
END$$

-- =====================================================
-- 3. Procedure: Phân bố doanh thu
-- =====================================================
DROP PROCEDURE IF EXISTS sp_phan_bo_doanh_thu$$
CREATE PROCEDURE sp_phan_bo_doanh_thu(
    IN p_tu_ngay DATE,
    IN p_den_ngay DATE
)
BEGIN
    -- Doanh thu từ vé
    SELECT 
        'Ve phim' as loai,
        COALESCE(SUM(dv.don_gia), 0) as doanh_thu,
        ROUND((COALESCE(SUM(dv.don_gia), 0) * 100.0) / 
              NULLIF((SELECT SUM(tong_tien) FROM don_hang 
                      WHERE trang_thai = 'DA_THANH_TOAN' 
                      AND DATE(dat_luc) BETWEEN p_tu_ngay AND p_den_ngay), 0), 2) as ty_le_phan_tram
    FROM don_ve dv
    JOIN don_hang dh ON dv.ma_don_hang = dh.ma_don_hang
    WHERE dh.trang_thai = 'DA_THANH_TOAN'
    AND DATE(dh.dat_luc) BETWEEN p_tu_ngay AND p_den_ngay
    
    UNION ALL
    
    -- Doanh thu từ hàng hóa
    SELECT 
        'Hang hoa' as loai,
        COALESCE(SUM(hh.don_gia * hh.so_luong), 0) as doanh_thu,
        ROUND((COALESCE(SUM(hh.don_gia * hh.so_luong), 0) * 100.0) / 
              NULLIF((SELECT SUM(tong_tien) FROM don_hang 
                      WHERE trang_thai = 'DA_THANH_TOAN' 
                      AND DATE(dat_luc) BETWEEN p_tu_ngay AND p_den_ngay), 0), 2) as ty_le_phan_tram
    FROM hang_hoa hh
    JOIN don_hang dh ON hh.ma_don_hang = dh.ma_don_hang
    WHERE dh.trang_thai = 'DA_THANH_TOAN'
    AND DATE(dh.dat_luc) BETWEEN p_tu_ngay AND p_den_ngay;
END$$

-- =====================================================
-- 4. Procedure: Top phim theo doanh thu
-- =====================================================
DROP PROCEDURE IF EXISTS sp_top_phim_doanh_thu$$
CREATE PROCEDURE sp_top_phim_doanh_thu(
    IN p_tu_ngay DATE,
    IN p_den_ngay DATE,
    IN p_limit INT
)
BEGIN
    SELECT 
        p.ma_phim,
        p.ten_phim,
        COUNT(DISTINCT sc.ma_suat_chieu) as so_suat_chieu,
        COUNT(DISTINCT v.ma_ve) as so_ve_ban,
        COALESCE(SUM(dv.don_gia), 0) as doanh_thu,
        0 as ty_le_lap_day_tb
    FROM phim p
    JOIN suat_chieu sc ON p.ma_phim = sc.ma_phim
    JOIN ve v ON sc.ma_suat_chieu = v.ma_suat_chieu
    JOIN don_ve dv ON v.ma_ve = dv.ma_ve
    JOIN don_hang dh ON dv.ma_don_hang = dh.ma_don_hang
    WHERE dh.trang_thai = 'DA_THANH_TOAN'
    AND DATE(dh.dat_luc) BETWEEN p_tu_ngay AND p_den_ngay
    GROUP BY p.ma_phim, p.ten_phim
    ORDER BY doanh_thu DESC
    LIMIT p_limit;
END$$

-- =====================================================
-- 5. Procedure: Chi tiết doanh thu
-- =====================================================
DROP PROCEDURE IF EXISTS sp_chi_tiet_doanh_thu$$
CREATE PROCEDURE sp_chi_tiet_doanh_thu(
    IN p_tu_ngay DATE,
    IN p_den_ngay DATE
)
BEGIN
    SELECT 
        DATE(dh.dat_luc) as ngay,
        p.ten_phim,
        COUNT(DISTINCT sc.ma_suat_chieu) as so_suat_chieu,
        COUNT(DISTINCT v.ma_ve) as so_ve_ban,
        COALESCE(SUM(dv.don_gia), 0) as doanh_thu,
        0 as ty_le_lap_day
    FROM don_hang dh
    JOIN don_ve dv ON dh.ma_don_hang = dv.ma_don_hang
    JOIN ve v ON dv.ma_ve = v.ma_ve
    JOIN suat_chieu sc ON v.ma_suat_chieu = sc.ma_suat_chieu
    JOIN phim p ON sc.ma_phim = p.ma_phim
    WHERE dh.trang_thai = 'DA_THANH_TOAN'
    AND DATE(dh.dat_luc) BETWEEN p_tu_ngay AND p_den_ngay
    GROUP BY DATE(dh.dat_luc), p.ma_phim, p.ten_phim
    ORDER BY ngay DESC, doanh_thu DESC;
END$$

-- =====================================================
-- 6. Procedure: Doanh thu theo phòng
-- =====================================================
DROP PROCEDURE IF EXISTS sp_doanh_thu_theo_phong$$
CREATE PROCEDURE sp_doanh_thu_theo_phong(
    IN p_tu_ngay DATE,
    IN p_den_ngay DATE
)
BEGIN
    SELECT 
        ph.ma_phong,
        ph.ten_phong,
        COUNT(DISTINCT sc.ma_suat_chieu) as so_suat_chieu,
        COUNT(DISTINCT v.ma_ve) as so_ve_ban,
        COALESCE(SUM(dv.don_gia), 0) as doanh_thu,
        0 as ty_le_lap_day_tb
    FROM phong ph
    JOIN suat_chieu sc ON ph.ma_phong = sc.ma_phong
    JOIN ve v ON sc.ma_suat_chieu = v.ma_suat_chieu
    JOIN don_ve dv ON v.ma_ve = dv.ma_ve
    JOIN don_hang dh ON dv.ma_don_hang = dh.ma_don_hang
    WHERE dh.trang_thai = 'DA_THANH_TOAN'
    AND DATE(dh.dat_luc) BETWEEN p_tu_ngay AND p_den_ngay
    AND sc.trang_thai != 'HUY'
    GROUP BY ph.ma_phong, ph.ten_phong
    ORDER BY doanh_thu DESC;
END$$

-- =====================================================
-- 7. Procedure: Top sản phẩm
-- =====================================================
DROP PROCEDURE IF EXISTS sp_top_san_pham$$
CREATE PROCEDURE sp_top_san_pham(
    IN p_tu_ngay DATE,
    IN p_den_ngay DATE,
    IN p_limit INT
)
BEGIN
    -- Sản phẩm đơn lẻ
    SELECT 
        'San pham' as loai,
        sp.ten_san_pham as ten,
        SUM(hh.so_luong) as so_luong_ban,
        COALESCE(SUM(hh.don_gia * hh.so_luong), 0) as doanh_thu
    FROM hang_hoa hh
    JOIN san_pham sp ON hh.ma_san_pham = sp.ma_san_pham
    JOIN don_hang dh ON hh.ma_don_hang = dh.ma_don_hang
    WHERE dh.trang_thai = 'DA_THANH_TOAN'
    AND DATE(dh.dat_luc) BETWEEN p_tu_ngay AND p_den_ngay
    AND hh.ma_san_pham IS NOT NULL
    GROUP BY sp.ma_san_pham, sp.ten_san_pham
    
    UNION ALL
    
    -- Combo
    SELECT 
        'Combo' as loai,
        cb.ten_combo as ten,
        SUM(hh.so_luong) as so_luong_ban,
        COALESCE(SUM(hh.don_gia * hh.so_luong), 0) as doanh_thu
    FROM hang_hoa hh
    JOIN combo cb ON hh.ma_combo = cb.ma_combo
    JOIN don_hang dh ON hh.ma_don_hang = dh.ma_don_hang
    WHERE dh.trang_thai = 'DA_THANH_TOAN'
    AND DATE(dh.dat_luc) BETWEEN p_tu_ngay AND p_den_ngay
    AND hh.ma_combo IS NOT NULL
    GROUP BY cb.ma_combo, cb.ten_combo
    
    ORDER BY doanh_thu DESC
    LIMIT p_limit;
END$$

-- =====================================================
-- 8. Procedure: Doanh thu theo giờ
-- =====================================================
DROP PROCEDURE IF EXISTS sp_doanh_thu_theo_gio$$
CREATE PROCEDURE sp_doanh_thu_theo_gio(
    IN p_tu_ngay DATE,
    IN p_den_ngay DATE
)
BEGIN
    SELECT 
        HOUR(sc.bat_dau_luc) as gio_chieu,
        COUNT(DISTINCT sc.ma_suat_chieu) as so_suat_chieu,
        COUNT(DISTINCT v.ma_ve) as so_ve_ban,
        COALESCE(SUM(dv.don_gia), 0) as doanh_thu,
        0 as ty_le_lap_day_tb
    FROM suat_chieu sc
    JOIN ve v ON sc.ma_suat_chieu = v.ma_suat_chieu
    JOIN don_ve dv ON v.ma_ve = dv.ma_ve
    JOIN don_hang dh ON dv.ma_don_hang = dh.ma_don_hang
    WHERE dh.trang_thai = 'DA_THANH_TOAN'
    AND DATE(sc.bat_dau_luc) BETWEEN p_tu_ngay AND p_den_ngay
    AND sc.trang_thai != 'HUY'
    GROUP BY HOUR(sc.bat_dau_luc)
    ORDER BY gio_chieu;
END$$

DELIMITER ;

-- =====================================================
-- Test các procedure (chạy sau khi tạo xong)
-- =====================================================

-- Test 1: Tổng quan
-- CALL sp_tong_quan_doanh_thu(DATE_SUB(CURDATE(), INTERVAL 30 DAY), CURDATE());

-- Test 2: Doanh thu theo ngày
-- CALL sp_doanh_thu_theo_ngay(DATE_SUB(CURDATE(), INTERVAL 7 DAY), CURDATE());

-- Test 3: Top phim
-- CALL sp_top_phim_doanh_thu(DATE_SUB(CURDATE(), INTERVAL 30 DAY), CURDATE(), 10);

SELECT 'Tao tat ca procedures thanh cong!' as status;