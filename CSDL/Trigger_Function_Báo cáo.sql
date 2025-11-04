-- TRIGEER--
DELIMITER $$

CREATE TRIGGER trg_kiem_tra_trung_gio
BEFORE INSERT ON suat_chieu
FOR EACH ROW
BEGIN
    DECLARE dem INT;

    SELECT COUNT(*) INTO dem
    FROM suat_chieu
    WHERE ma_phong = NEW.ma_phong
      AND (
           (NEW.bat_dau_luc BETWEEN bat_dau_luc AND ket_thuc_luc)
        OR (NEW.ket_thuc_luc BETWEEN bat_dau_luc AND ket_thuc_luc)
        OR (bat_dau_luc BETWEEN NEW.bat_dau_luc AND NEW.ket_thuc_luc)
      );

    IF dem > 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = '❌ Lỗi: Suất chiếu trùng giờ trong cùng phòng!';
    END IF;
END$$

DELIMITER ;

-- FUNCTION--
DELIMITER $$

CREATE FUNCTION tong_thoi_luong_phim_trong_ngay(p_ngay DATE)
RETURNS INT
DETERMINISTIC
BEGIN
    DECLARE tong INT;

    SELECT COALESCE(SUM(p.thoi_luong_phut), 0)
    INTO tong
    FROM suat_chieu sc
    JOIN phim p ON sc.ma_phim = p.ma_phim
    WHERE DATE(sc.bat_dau_luc) = p_ngay;

    RETURN tong;
END$$

DELIMITER ;
SELECT tong_thoi_luong_phim_trong_ngay('2025-11-05') AS tong_phut;

 -- BAO CAO--
CREATE OR REPLACE VIEW v_danh_sach_phim_theo_ngay AS
SELECT 
    DATE(sc.bat_dau_luc) AS ngay_chieu,
    p.ten_phim,
    dd.ten_dinh_dang,
    sc.ma_phong,
    sc.bat_dau_luc,
    sc.ket_thuc_luc,
    sc.gia_co_ban,
    sc.trang_thai
FROM suat_chieu sc
JOIN phim p ON sc.ma_phim = p.ma_phim
JOIN dinh_dang dd ON sc.ma_dinh_dang = dd.ma_dinh_dang
ORDER BY ngay_chieu, sc.ma_phong, sc.bat_dau_luc;