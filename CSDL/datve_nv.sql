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
