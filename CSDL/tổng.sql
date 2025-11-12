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


use qlrapchieuphim;
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;
/* Trigger cập nhật doanh thu khi vé thay đổi trạng thái */
DELIMITER $$

CREATE TRIGGER trigger_cap_nhat_doanh_thu_ve
AFTER UPDATE ON ve
FOR EACH ROW
BEGIN
    DECLARE v_ma_suat_chieu BIGINT UNSIGNED;
    DECLARE v_ngay_chieu DATE;
    DECLARE v_tong_doanh_thu DECIMAL(15,2);
    DECLARE v_so_ve_da_ban INT UNSIGNED;
    DECLARE v_doanh_thu_thuc DECIMAL(15,2);
    
    -- Chỉ xử lý khi trạng thái vé thay đổi liên quan đến doanh thu
    IF (OLD.trang_thai != NEW.trang_thai) AND 
       (NEW.trang_thai IN ('DA_BAN', 'HOAN', 'HUY') OR OLD.trang_thai IN ('DA_BAN')) THEN
        
        SET v_ma_suat_chieu = NEW.ma_suat_chieu;
        
        -- Lấy ngày chiếu từ suất chiếu
        SELECT DATE(sc.ngay_gio_chieu) INTO v_ngay_chieu
        FROM suat_chieu sc
        WHERE sc.ma_suat_chieu = v_ma_suat_chieu;
        
        -- Tính tổng doanh thu và số vé đã bán cho suất chiếu này
        SELECT 
            COUNT(*), 
            COALESCE(SUM(gia_ban), 0),
            -- Doanh thu thực chỉ tính vé đã bán, không tính vé hủy/huỷ
            COALESCE(SUM(CASE WHEN trang_thai = 'DA_BAN' THEN gia_ban ELSE 0 END), 0)
        INTO v_so_ve_da_ban, v_tong_doanh_thu, v_doanh_thu_thuc
        FROM ve
        WHERE ma_suat_chieu = v_ma_suat_chieu 
        AND trang_thai IN ('DA_BAN', 'HOAN', 'HUY');
        
        -- Cập nhật hoặc chèn vào bảng doanh_thu
        INSERT INTO doanh_thu (ma_suat_chieu, ngay_chieu, so_ve_da_ban, tong_doanh_thu, doanh_thu_thuc)
        VALUES (v_ma_suat_chieu, v_ngay_chieu, v_so_ve_da_ban, v_tong_doanh_thu, v_doanh_thu_thuc)
        ON DUPLICATE KEY UPDATE 
            so_ve_da_ban = VALUES(so_ve_da_ban),
            tong_doanh_thu = VALUES(tong_doanh_thu),
            doanh_thu_thuc = VALUES(doanh_thu_thuc),
            cap_nhat_luc = CURRENT_TIMESTAMP;
    END IF;
END$$

DELIMITER ;


/* Trigger cập nhật doanh thu khi thêm vé mới */
DELIMITER $$

CREATE TRIGGER trigger_cap_nhat_doanh_thu_ve_insert
AFTER INSERT ON ve
FOR EACH ROW
BEGIN
    DECLARE v_ma_suat_chieu BIGINT UNSIGNED;
    DECLARE v_ngay_chieu DATE;
    DECLARE v_tong_doanh_thu DECIMAL(15,2);
    DECLARE v_so_ve_da_ban INT UNSIGNED;
    DECLARE v_doanh_thu_thuc DECIMAL(15,2);
    
    -- Chỉ xử lý khi vé được bán ngay lập tức
    IF NEW.trang_thai = 'DA_BAN' THEN
        SET v_ma_suat_chieu = NEW.ma_suat_chieu;
        
        -- Lấy ngày chiếu từ suất chiếu
        SELECT DATE(sc.ngay_gio_chieu) INTO v_ngay_chieu
        FROM suat_chieu sc
        WHERE sc.ma_suat_chieu = v_ma_suat_chieu;
        
        -- Tính tổng doanh thu và số vé đã bán cho suất chiếu này
        SELECT 
            COUNT(*), 
            COALESCE(SUM(gia_ban), 0),
            COALESCE(SUM(CASE WHEN trang_thai = 'DA_BAN' THEN gia_ban ELSE 0 END), 0)
        INTO v_so_ve_da_ban, v_tong_doanh_thu, v_doanh_thu_thuc
        FROM ve
        WHERE ma_suat_chieu = v_ma_suat_chieu 
        AND trang_thai IN ('DA_BAN', 'HOAN', 'HUY');
        
        -- Cập nhật hoặc chèn vào bảng doanh_thu
        INSERT INTO doanh_thu (ma_suat_chieu, ngay_chieu, so_ve_da_ban, tong_doanh_thu, doanh_thu_thuc)
        VALUES (v_ma_suat_chieu, v_ngay_chieu, v_so_ve_da_ban, v_tong_doanh_thu, v_doanh_thu_thuc)
        ON DUPLICATE KEY UPDATE 
            so_ve_da_ban = VALUES(so_ve_da_ban),
            tong_doanh_thu = VALUES(tong_doanh_thu),
            doanh_thu_thuc = VALUES(doanh_thu_thuc),
            cap_nhat_luc = CURRENT_TIMESTAMP;
    END IF;
END$$

DELIMITER ;
USE qlrapchieuphim;
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 1) Hàm tính giá vé theo loại ghế * giá cơ bản của suất
DROP FUNCTION IF EXISTS fn_gia_ve;
DELIMITER //
CREATE FUNCTION fn_gia_ve(p_ma_suat BIGINT, p_ma_ghe BIGINT)
RETURNS DECIMAL(10,2) DETERMINISTIC
BEGIN
  DECLARE v_base DECIMAL(10,2);
  DECLARE v_heso DECIMAL(5,2);
  SELECT gia_co_ban INTO v_base FROM suat_chieu WHERE ma_suat_chieu = p_ma_suat;
  SELECT lg.he_so_gia INTO v_heso
  FROM ghe g JOIN loai_ghe lg ON g.ma_loai_ghe = lg.ma_loai_ghe
  WHERE g.ma_ghe = p_ma_ghe;
  RETURN ROUND(COALESCE(v_base,0) * COALESCE(v_heso,1), 0);
END//
DELIMITER ;

-- 2) Proc tạo vé cho toàn bộ ghế của phòng ứng với 1 suất
DROP PROCEDURE IF EXISTS sp_tao_ve_cho_suat;
DELIMITER //
CREATE PROCEDURE sp_tao_ve_cho_suat(IN p_ma_suat BIGINT)
BEGIN
  DECLARE v_phong BIGINT;
  SELECT ma_phong INTO v_phong FROM suat_chieu WHERE ma_suat_chieu = p_ma_suat;
  IF v_phong IS NULL THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Khong tim thay suat_chieu';
  END IF;

  INSERT INTO ve (ma_suat_chieu, ma_ghe, gia_ban, trang_thai)
  SELECT p_ma_suat, g.ma_ghe, fn_gia_ve(p_ma_suat, g.ma_ghe), 'SAN_SANG'
  FROM ghe g
  WHERE g.ma_phong = v_phong
    AND NOT EXISTS (
      SELECT 1 FROM ve v WHERE v.ma_suat_chieu = p_ma_suat AND v.ma_ghe = g.ma_ghe
    );
END//
DELIMITER ;

-- 3) Trigger tự sinh vé khi tạo suất mới
DROP TRIGGER IF EXISTS trg_sc_ai_gen_ve;
DELIMITER //
CREATE TRIGGER trg_sc_ai_gen_ve
AFTER INSERT ON suat_chieu
FOR EACH ROW
BEGIN
  CALL sp_tao_ve_cho_suat(NEW.ma_suat_chieu);
END//
DELIMITER ;

-- 4) Backfill vé cho các suất đã có nhưng chưa sinh vé
INSERT INTO ve (ma_suat_chieu, ma_ghe, gia_ban, trang_thai)
SELECT sc.ma_suat_chieu, g.ma_ghe, ROUND(sc.gia_co_ban * lg.he_so_gia, 0), 'SAN_SANG'
FROM suat_chieu sc
JOIN ghe g  ON g.ma_phong = sc.ma_phong
JOIN loai_ghe lg ON lg.ma_loai_ghe = g.ma_loai_ghe
LEFT JOIN ve v ON v.ma_suat_chieu = sc.ma_suat_chieu AND v.ma_ghe = g.ma_ghe
WHERE v.ma_ve IS NULL;

SET FOREIGN_KEY_CHECKS = 1;

-- 5) Kiểm tra nhanh: mỗi suất có bao nhiêu vé
SELECT sc.ma_suat_chieu, sc.bat_dau_luc, COUNT(v.ma_ve) AS so_ve
FROM suat_chieu sc
LEFT JOIN ve v ON v.ma_suat_chieu = sc.ma_suat_chieu
GROUP BY sc.ma_suat_chieu, sc.bat_dau_luc
ORDER BY sc.bat_dau_luc;

-- ------------------------------------------------------------------
-- 1) HÀM TÍNH GIÁ VÉ: base * hệ số ghế
--    - Dùng trong sinh vé & khi đặt vé
-- ------------------------------------------------------------------
DELIMITER //
CREATE FUNCTION fn_gia_ve(p_ma_suat_chieu BIGINT, p_ma_ghe BIGINT)
RETURNS DECIMAL(10,2)
DETERMINISTIC
BEGIN
  DECLARE v_base DECIMAL(10,2);
  DECLARE v_heso DECIMAL(5,2);
  SELECT gia_co_ban INTO v_base FROM suat_chieu WHERE ma_suat_chieu = p_ma_suat_chieu;
  SELECT lg.he_so_gia INTO v_heso
  FROM ghe g JOIN loai_ghe lg ON g.ma_loai_ghe = lg.ma_loai_ghe
  WHERE g.ma_ghe = p_ma_ghe;
  IF v_base IS NULL OR v_heso IS NULL THEN
    RETURN NULL;
  END IF;
  RETURN ROUND(v_base * v_heso, 0);
END//
DELIMITER ;

-- ------------------------------------------------------------------
-- 2) TRIGGER trước khi INSERT suất chiếu
--    - Tự set 'ket_thuc_luc' nếu NULL = bat_dau_luc + thoi_luong_phim (phút)
-- ------------------------------------------------------------------
DELIMITER //
CREATE TRIGGER trg_sc_bi_set_ketthuc
BEFORE INSERT ON suat_chieu
FOR EACH ROW
BEGIN
  DECLARE v_tg SMALLINT;
  IF NEW.ket_thuc_luc IS NULL THEN
    SELECT thoi_luong_phut INTO v_tg FROM phim WHERE ma_phim = NEW.ma_phim;
    IF v_tg IS NOT NULL THEN
      SET NEW.ket_thuc_luc = DATE_ADD(NEW.bat_dau_luc, INTERVAL v_tg MINUTE);
    END IF;
  END IF;
END//
DELIMITER ;

-- ------------------------------------------------------------------
-- 3) THỦ TỤC sinh toàn bộ vé cho 1 suất chiếu dựa vào cấu hình ghế của phòng
-- ------------------------------------------------------------------
DELIMITER //
CREATE PROCEDURE sp_tao_ve_cho_suat(IN p_ma_suat_chieu BIGINT)
BEGIN
  DECLARE v_ma_phong BIGINT;
  SELECT ma_phong INTO v_ma_phong FROM suat_chieu WHERE ma_suat_chieu = p_ma_suat_chieu;

  /* Chỉ sinh các vé chưa tồn tại (an toàn nếu chạy lại) */
  INSERT INTO ve (ma_suat_chieu, ma_ghe, gia_ban, trang_thai, giu_cho_luc, ban_luc)
  SELECT p_ma_suat_chieu, g.ma_ghe, fn_gia_ve(p_ma_suat_chieu, g.ma_ghe), 'SAN_SANG', NULL, NULL
  FROM ghe g
  WHERE g.ma_phong = v_ma_phong
    AND NOT EXISTS (
      SELECT 1 FROM ve v WHERE v.ma_suat_chieu = p_ma_suat_chieu AND v.ma_ghe = g.ma_ghe
    );
END//
DELIMITER ;

-- ------------------------------------------------------------------
-- 4) TRIGGER sau khi INSERT suất chiếu
--    - Tự động sinh vé cho toàn bộ ghế của phòng
-- ------------------------------------------------------------------
DELIMITER //
CREATE TRIGGER trg_sc_ai_gen_ve
AFTER INSERT ON suat_chieu
FOR EACH ROW
BEGIN
  CALL sp_tao_ve_cho_suat(NEW.ma_suat_chieu);
END//
DELIMITER ;

-- ------------------------------------------------------------------
-- 5) THỦ TỤC tính lại tổng tiền đơn hàng
--    - Gom từ bảng don_ve
-- ------------------------------------------------------------------
DELIMITER //
CREATE PROCEDURE sp_tinh_tong_don(IN p_ma_don_hang BIGINT)
BEGIN
  UPDATE don_hang dh
  JOIN (
    SELECT ma_don_hang, COALESCE(SUM(don_gia),0) AS sum_dong
    FROM don_ve
    WHERE ma_don_hang = p_ma_don_hang
    GROUP BY ma_don_hang
  ) x ON x.ma_don_hang = dh.ma_don_hang
  SET dh.tong_tien = x.sum_dong;
END//
DELIMITER ;

-- Trigger đồng bộ tổng tiền sau INSERT/UPDATE/DELETE don_ve
DELIMITER //
CREATE TRIGGER trg_donve_ai_tinh_tong
AFTER INSERT ON don_ve
FOR EACH ROW
BEGIN
  CALL sp_tinh_tong_don(NEW.ma_don_hang);
END//
DELIMITER ;

DELIMITER //
CREATE TRIGGER trg_donve_au_tinh_tong
AFTER UPDATE ON don_ve
FOR EACH ROW
BEGIN
  CALL sp_tinh_tong_don(NEW.ma_don_hang);
END//
DELIMITER ;

DELIMITER //
CREATE TRIGGER trg_donve_ad_tinh_tong
AFTER DELETE ON don_ve
FOR EACH ROW
BEGIN
  CALL sp_tinh_tong_don(OLD.ma_don_hang);
END//
DELIMITER ;

-- ------------------------------------------------------------------
-- 6) VIEW hỗ trợ UI: Giá thấp nhất theo phim trong 1 ngày
--    - UI có thể dùng: SELECT * FROM v_gia_min_theo_phim_ngay WHERE ngay = 'YYYY-MM-DD';
-- ------------------------------------------------------------------
CREATE VIEW v_gia_min_theo_phim_ngay AS
SELECT
  DATE(sc.bat_dau_luc) AS ngay,
  p.ma_phim,
  p.ten_phim,
  MIN(v.gia_ban) AS gia_min
FROM suat_chieu sc
JOIN phim p       ON p.ma_phim = sc.ma_phim
JOIN ve   v       ON v.ma_suat_chieu = sc.ma_suat_chieu
GROUP BY DATE(sc.bat_dau_luc), p.ma_phim, p.ten_phim;

-- ------------------------------------------------------------------
-- 7) THỦ TỤC đặt vé 
--    Input:
--      - p_ma_khach_hang: BIGINT hoặc NULL (đặt khách lẻ)
--      - p_ids_ve: JSON array các ma_ve cần mua (vd: '[101,102,205]')
--      - p_phuong_thuc: 'TIEN_MAT'|'THE'|'VI_DIEN_TU'|'CHUYEN_KHOAN'
--    Hành vi:
--      - Tạo don_hang, chốt don_ve theo giá hiện tại của vé
--      - Đánh dấu vé 'DA_BAN' + ban_luc=NOW()
--      - Tính tổng tiền + ghi thanh_toan (THANH_CONG)
-- ------------------------------------------------------------------
DELIMITER //
CREATE PROCEDURE sp_dat_ve(
  IN p_ma_khach_hang BIGINT,
  IN p_ids_ve JSON,
  IN p_phuong_thuc ENUM('TIEN_MAT','THE','VI_DIEN_TU','CHUYEN_KHOAN')
)
BEGIN
  DECLARE v_don BIGINT;

  -- 1) Tạo đơn
  INSERT INTO don_hang (ma_khach_hang, kenh, trang_thai, ghi_chu)
  VALUES (p_ma_khach_hang, 'TRUC_TUYEN', 'CHO_THANH_TOAN', 'Đặt vé online');
  SET v_don = LAST_INSERT_ID();

  -- 2) Đổ dòng đơn_ve từ JSON (chỉ lấy những vé đang còn 'SAN_SANG')
  INSERT INTO don_ve (ma_don_hang, ma_ve, don_gia)
  SELECT v_don, v.ma_ve, v.gia_ban
  FROM JSON_TABLE(p_ids_ve, '$[*]' COLUMNS (ve_id BIGINT PATH '$')) jt
  JOIN ve v ON v.ma_ve = jt.ve_id AND v.trang_thai = 'SAN_SANG';

  -- 3) Đánh dấu vé đã bán
  UPDATE ve v
  JOIN JSON_TABLE(p_ids_ve, '$[*]' COLUMNS (ve_id BIGINT PATH '$')) jt
    ON v.ma_ve = jt.ve_id
  SET v.trang_thai = 'DA_BAN',
      v.ban_luc    = NOW()
  WHERE v.trang_thai = 'SAN_SANG';

  -- 4) Tính tổng đơn
  CALL sp_tinh_tong_don(v_don);

  -- 5) Ghi thanh toán (đơn giản hóa là thành công)
  INSERT INTO thanh_toan (ma_don_hang, so_tien, phuong_thuc, trang_thai, ma_tham_chieu)
  SELECT v_don, dh.tong_tien, p_phuong_thuc, 'THANH_CONG', CONCAT('AUTO-', v_don)
  FROM don_hang dh WHERE dh.ma_don_hang = v_don;

  -- 6) Chuyển trạng thái đơn
  UPDATE don_hang SET trang_thai = 'DA_THANH_TOAN' WHERE ma_don_hang = v_don;

  -- 7) Trả về id đơn
  SELECT v_don AS ma_don_hang;
END//
DELIMITER ;

-- ------------------------------------------------------------------
-- 8) INDEX gợi ý thêm để tăng tốc các truy vấn UI
-- ------------------------------------------------------------------
CREATE INDEX idx_sc_ngay_phim ON suat_chieu (ma_phim, bat_dau_luc);
CREATE INDEX idx_ve_suatchieu_trangthai ON ve (ma_suat_chieu, trang_thai);
CREATE INDEX idx_donhang_trangthai ON don_hang (trang_thai);

-- ------------------------------------------------------------------
-- 9) GỢI Ý TRUY VẤN CHO UI KHÁCH HÀNG 
-- ------------------------------------------------------------------
-- Lấy danh sách phim + giá min theo ngày cụ thể:
--   SELECT p.ma_phim, p.ten_phim, x.gia_min
--   FROM phim p
--   LEFT JOIN (
--     SELECT ma_phim, MIN(gia_ban) AS gia_min
--     FROM suat_chieu sc
--     JOIN ve v ON v.ma_suat_chieu = sc.ma_suat_chieu
--     WHERE DATE(sc.bat_dau_luc) = CURDATE()
--     GROUP BY ma_phim
--   ) x ON x.ma_phim = p.ma_phim
--   WHERE p.ten_phim LIKE CONCAT('%', :keyword, '%');

-- Lấy danh sách suất cho 1 phim + ngày:
--   SELECT sc.ma_suat_chieu, TIME(sc.bat_dau_luc) AS gio_bat_dau, sc.ma_phong, dd.ten_dinh_dang, sc.gia_co_ban
--   FROM suat_chieu sc
--   JOIN dinh_dang dd ON dd.ma_dinh_dang = sc.ma_dinh_dang
--   WHERE sc.ma_phim = :ma_phim AND DATE(sc.bat_dau_luc) = :ngay
--   ORDER BY sc.bat_dau_luc;

-- Gọi đặt vé (ví dụ mua các vé id 101,102):
--   CALL sp_dat_ve(NULL, JSON_ARRAY(101,102), 'CHUYEN_KHOAN');

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
