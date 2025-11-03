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

