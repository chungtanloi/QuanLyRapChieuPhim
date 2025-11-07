USE qlrapchieuphim;
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;


DELIMITER $$

CREATE FUNCTION tinh_gia_ve_theo_loai_khach(
    p_ma_suat_chieu BIGINT UNSIGNED,
    p_ma_khach_hang BIGINT UNSIGNED
) 
RETURNS DECIMAL(10,2)
READS SQL DATA
DETERMINISTIC
BEGIN
    DECLARE v_gia_goc DECIMAL(10,2);
    DECLARE v_ti_le_giam DECIMAL(5,2) DEFAULT 0;
    DECLARE v_ma_loai_khach INT UNSIGNED;
    DECLARE v_gia_sau_giam DECIMAL(10,2);
    DECLARE v_ngay_chieu DATE;
    DECLARE v_gio_chieu TIME;
    DECLARE v_ngay_trong_tuan INT;
    
    -- Lấy thông tin suất chiếu: giá gốc, ngày giờ chiếu
    SELECT sc.gia_ve, DATE(sc.ngay_gio_chieu), TIME(sc.ngay_gio_chieu)
    INTO v_gia_goc, v_ngay_chieu, v_gio_chieu
    FROM suat_chieu sc
    WHERE sc.ma_suat_chieu = p_ma_suat_chieu;
    
    -- Tính ngày trong tuần (1=Chủ nhật, 2=Thứ 2, ..., 7=Thứ 7)
    SET v_ngay_trong_tuan = DAYOFWEEK(v_ngay_chieu);
    
    -- Áp dụng hệ số giá theo khung giờ
    IF v_gio_chieu BETWEEN '17:00:00' AND '22:00:00' THEN
        -- Giờ cao điểm: tăng 20%
        SET v_gia_goc = v_gia_goc * 1.2;
    ELSEIF v_gio_chieu BETWEEN '12:00:00' AND '17:00:00' THEN
        -- Giờ thường: giữ nguyên
        SET v_gia_goc = v_gia_goc;
    ELSE
        -- Giờ thấp điểm: giảm 15%
        SET v_gia_goc = v_gia_goc * 0.85;
    END IF;
    
    -- Áp dụng hệ số ngày cuối tuần
    IF v_ngay_trong_tuan = 1 OR v_ngay_trong_tuan = 7 THEN
        -- Cuối tuần: tăng 25%
        SET v_gia_goc = v_gia_goc * 1.25;
    END IF;
    
    -- Lấy loại khách hàng và tỷ lệ giảm giá
    IF p_ma_khach_hang IS NOT NULL THEN
        SELECT kh.ma_loai_khach_hang, lkh.ti_le_giam_gia 
        INTO v_ma_loai_khach, v_ti_le_giam
        FROM khach_hang kh
        JOIN loai_khach_hang lkh ON kh.ma_loai_khach_hang = lkh.ma_loai_khach_hang
        WHERE kh.ma_khach_hang = p_ma_khach_hang;
    END IF;
    
    -- Nếu không có loại khách hàng, mặc định là loại Thường
    IF v_ma_loai_khach IS NULL THEN
        SET v_ma_loai_khach = 1; -- Loại Thường
        SELECT ti_le_giam_gia INTO v_ti_le_giam 
        FROM loai_khach_hang WHERE ma_loai_khach_hang = 1;
    END IF;
    
    -- Tính giá sau giảm
    SET v_gia_sau_giam = v_gia_goc * (1 - v_ti_le_giam);
    
    -- Đảm bảo giá vé tối thiểu là 10,000 VND
    IF v_gia_sau_giam < 10000 THEN
        SET v_gia_sau_giam = 10000;
    END IF;
    
    -- Làm tròn đến hàng nghìn
    SET v_gia_sau_giam = ROUND(v_gia_sau_giam / 1000) * 1000;
    
    RETURN v_gia_sau_giam;
END$$

DELIMITER ;