USE qlrapchieuphim;
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS=0;

-- ======================================================================
-- C. TRIGGER & FUNCTION (CẦN CÓ)
-- ======================================================================

-- 1. TRIGGER kiểm tra lịch chiếu trùng giờ (BEFORE INSERT)
DELIMITER $$
DROP TRIGGER IF EXISTS trg_check_suat_chieu_overlap$$
CREATE TRIGGER trg_check_suat_chieu_overlap
BEFORE INSERT ON suat_chieu FOR EACH ROW
BEGIN
    DECLARE film_duration INT;
    DECLARE conflict_count INT;

    SELECT thoi_luong_phut INTO film_duration
    FROM phim
    WHERE ma_phim = NEW.ma_phim;

    SET @new_end_time = DATE_ADD(NEW.bat_dau_luc, INTERVAL film_duration MINUTE);
    
    SELECT COUNT(*) INTO conflict_count
    FROM suat_chieu sc
    JOIN phim p ON sc.ma_phim = p.ma_phim
    WHERE sc.ma_phong = NEW.ma_phong
      AND sc.ma_suat_chieu != NEW.ma_suat_chieu 
      AND sc.bat_dau_luc < @new_end_time       
      AND DATE_ADD(sc.bat_dau_luc, INTERVAL p.thoi_luong_phut MINUTE) > NEW.bat_dau_luc; 
      
    IF conflict_count > 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'LỖI TRIGGER: Suất chiếu này bị trùng lặp thời gian với suất chiếu khác trong cùng phòng.';
    END IF;
END$$
-- Cần thêm Trigger cho BEFORE UPDATE nữa nếu bạn có chức năng sửa suất chiếu.

-- 2. FUNCTION tính tổng thời lượng phim trong ngày
DROP FUNCTION IF EXISTS func_tong_thoi_luong_phim_ngay$$
CREATE FUNCTION func_tong_thoi_luong_phim_ngay (target_date DATE)
RETURNS INT READS SQL DATA
BEGIN
    DECLARE total_duration INT DEFAULT 0;

    SELECT SUM(p.thoi_luong_phut) INTO total_duration
    FROM suat_chieu sc
    JOIN phim p ON sc.ma_phim = p.ma_phim
    WHERE DATE(sc.bat_dau_luc) = target_date;

    RETURN IFNULL(total_duration, 0);
END$$

DELIMITER ;