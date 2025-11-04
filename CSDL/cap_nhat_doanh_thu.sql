
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