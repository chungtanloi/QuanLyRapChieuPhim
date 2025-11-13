-- ============================================
-- TRIGGERS, FUNCTIONS & STORED PROCEDURES
-- Riêng cho Quản Lý Rạp Chiếu Phim (Phòng)
-- ============================================

USE qlrapchieuphim;
DELIMITER $$

-- ============================================
-- 1. TRIGGER: Tự động tạo ghế khi thêm phòng mới
-- ============================================
DROP TRIGGER IF EXISTS trg_after_insert_phong$$
CREATE TRIGGER trg_after_insert_phong
AFTER INSERT ON phong
FOR EACH ROW
BEGIN
    DECLARE v_loai_ghe_id BIGINT;
    DECLARE v_rows INT;
    DECLARE v_seats_per_row INT DEFAULT 10;
    DECLARE v_current_row INT DEFAULT 0;
    DECLARE v_current_seat INT;
    DECLARE v_hang_ghe CHAR(1);
    DECLARE v_seat_count INT DEFAULT 0;
    
    -- Lấy loại ghế mặc định (Thường)
    SELECT ma_loai_ghe INTO v_loai_ghe_id 
    FROM loai_ghe 
    WHERE ten_loai_ghe = 'Thuong' 
    LIMIT 1;
    
    -- Nếu chưa có loại ghế, tạo mới
    IF v_loai_ghe_id IS NULL THEN
        INSERT INTO loai_ghe (ten_loai_ghe, he_so_gia) 
        VALUES ('Thuong', 1.0);
        SET v_loai_ghe_id = LAST_INSERT_ID();
    END IF;
    
    -- Tính số hàng (mỗi hàng 10 ghế)
    SET v_rows = CEIL(NEW.suc_chua / v_seats_per_row);
    
    -- Tạo ghế tự động
    WHILE v_current_row < v_rows AND v_seat_count < NEW.suc_chua DO
        SET v_hang_ghe = CHAR(65 + v_current_row); -- A, B, C...
        SET v_current_seat = 1;
        
        WHILE v_current_seat <= v_seats_per_row AND v_seat_count < NEW.suc_chua DO
            INSERT INTO ghe (ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
            VALUES (NEW.ma_phong, v_hang_ghe, v_current_seat, v_loai_ghe_id);
            
            SET v_current_seat = v_current_seat + 1;
            SET v_seat_count = v_seat_count + 1;
        END WHILE;
        
        SET v_current_row = v_current_row + 1;
    END WHILE;
END$$

-- ============================================
-- 2. TRIGGER: Cập nhật số ghế khi thay đổi sức chứa phòng
-- ============================================
DROP TRIGGER IF EXISTS trg_before_update_phong$$
CREATE TRIGGER trg_before_update_phong
BEFORE UPDATE ON phong
FOR EACH ROW
BEGIN
    DECLARE v_current_seats INT;
    
    -- Đếm số ghế hiện tại
    SELECT COUNT(*) INTO v_current_seats
    FROM ghe
    WHERE ma_phong = OLD.ma_phong;
    
    -- Nếu sức chứa mới < số ghế hiện tại, cảnh báo
    IF NEW.suc_chua < v_current_seats THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Suc chua moi khong the nho hon so ghe hien tai. Vui long xoa ghe truoc!';
    END IF;
END$$

-- ============================================
-- 3. TRIGGER: Kiểm tra trạng thái phòng trước khi xóa
-- ============================================
DROP TRIGGER IF EXISTS trg_before_delete_phong$$
CREATE TRIGGER trg_before_delete_phong
BEFORE DELETE ON phong
FOR EACH ROW
BEGIN
    DECLARE v_suat_count INT;
    
    -- Kiểm tra xem có suất chiếu nào đang hoạt động không
    SELECT COUNT(*) INTO v_suat_count
    FROM suat_chieu
    WHERE ma_phong = OLD.ma_phong
    AND trang_thai IN ('LEN_KE_HOACH', 'MO_BAN')
    AND bat_dau_luc > NOW();
    
    IF v_suat_count > 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Khong the xoa phong dang co suat chieu hoat dong!';
    END IF;
END$$

-- ============================================
-- 4. TRIGGER: Ghi log khi thay đổi trạng thái phòng
-- ============================================
DROP TABLE IF EXISTS phong_log$$
CREATE TABLE phong_log (
    ma_log BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    ma_phong BIGINT UNSIGNED NOT NULL,
    ten_phong VARCHAR(50),
    trang_thai_cu ENUM('HOAT_DONG','BAO_TRI','NGUNG'),
    trang_thai_moi ENUM('HOAT_DONG','BAO_TRI','NGUNG'),
    thao_tac ENUM('INSERT','UPDATE','DELETE'),
    thoi_gian DATETIME DEFAULT CURRENT_TIMESTAMP,
    ghi_chu TEXT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4$$

DROP TRIGGER IF EXISTS trg_log_phong_insert$$
CREATE TRIGGER trg_log_phong_insert
AFTER INSERT ON phong
FOR EACH ROW
BEGIN
    INSERT INTO phong_log (ma_phong, ten_phong, trang_thai_moi, thao_tac, ghi_chu)
    VALUES (NEW.ma_phong, NEW.ten_phong, NEW.trang_thai, 'INSERT', 
            CONCAT('Them phong moi - Suc chua: ', NEW.suc_chua));
END$$

DROP TRIGGER IF EXISTS trg_log_phong_update$$
CREATE TRIGGER trg_log_phong_update
AFTER UPDATE ON phong
FOR EACH ROW
BEGIN
    IF OLD.trang_thai != NEW.trang_thai THEN
        INSERT INTO phong_log (ma_phong, ten_phong, trang_thai_cu, trang_thai_moi, thao_tac, ghi_chu)
        VALUES (NEW.ma_phong, NEW.ten_phong, OLD.trang_thai, NEW.trang_thai, 'UPDATE',
                CONCAT('Thay doi trang thai phong'));
    END IF;
END$$

DROP TRIGGER IF EXISTS trg_log_phong_delete$$
CREATE TRIGGER trg_log_phong_delete
AFTER DELETE ON phong
FOR EACH ROW
BEGIN
    INSERT INTO phong_log (ma_phong, ten_phong, trang_thai_cu, thao_tac, ghi_chu)
    VALUES (OLD.ma_phong, OLD.ten_phong, OLD.trang_thai, 'DELETE',
            CONCAT('Xoa phong - Suc chua: ', OLD.suc_chua));
END$$

-- ============================================
-- 5. STORED PROCEDURE: Thêm phòng với validation
-- ============================================
DROP PROCEDURE IF EXISTS sp_them_phong$$
CREATE PROCEDURE sp_them_phong(
    IN p_ten_phong VARCHAR(50),
    IN p_suc_chua SMALLINT UNSIGNED,
    IN p_trang_thai ENUM('HOAT_DONG','BAO_TRI','NGUNG'),
    OUT p_ma_phong BIGINT,
    OUT p_message VARCHAR(255)
)
BEGIN
    DECLARE v_exists INT;
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        SET p_message = 'Loi: Khong the them phong!';
        SET p_ma_phong = NULL;
    END;
    
    START TRANSACTION;
    
    -- Kiểm tra tên phòng đã tồn tại chưa
    SELECT COUNT(*) INTO v_exists
    FROM phong
    WHERE ten_phong = p_ten_phong;
    
    IF v_exists > 0 THEN
        SET p_message = 'Loi: Ten phong da ton tai!';
        SET p_ma_phong = NULL;
        ROLLBACK;
    ELSE
        -- Kiểm tra sức chứa hợp lệ
        IF p_suc_chua < 20 OR p_suc_chua > 500 THEN
            SET p_message = 'Loi: Suc chua phai tu 20 den 500 ghe!';
            SET p_ma_phong = NULL;
            ROLLBACK;
        ELSE
            -- Thêm phòng mới
            INSERT INTO phong (ten_phong, suc_chua, trang_thai)
            VALUES (p_ten_phong, p_suc_chua, p_trang_thai);
            
            SET p_ma_phong = LAST_INSERT_ID();
            SET p_message = 'Thanh cong: Da them phong moi!';
            COMMIT;
        END IF;
    END IF;
END$$

-- ============================================
-- 6. STORED PROCEDURE: Cập nhật phòng
-- ============================================
DROP PROCEDURE IF EXISTS sp_cap_nhat_phong$$
CREATE PROCEDURE sp_cap_nhat_phong(
    IN p_ma_phong BIGINT,
    IN p_ten_phong VARCHAR(50),
    IN p_suc_chua SMALLINT UNSIGNED,
    IN p_trang_thai ENUM('HOAT_DONG','BAO_TRI','NGUNG'),
    OUT p_success BOOLEAN,
    OUT p_message VARCHAR(255)
)
BEGIN
    DECLARE v_exists INT;
    DECLARE v_current_seats INT;
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        SET p_success = FALSE;
        SET p_message = 'Loi: Khong the cap nhat phong!';
    END;
    
    START TRANSACTION;
    
    -- Kiểm tra phòng tồn tại
    SELECT COUNT(*) INTO v_exists
    FROM phong
    WHERE ma_phong = p_ma_phong;
    
    IF v_exists = 0 THEN
        SET p_success = FALSE;
        SET p_message = 'Loi: Phong khong ton tai!';
        ROLLBACK;
    ELSE
        -- Đếm số ghế hiện tại
        SELECT COUNT(*) INTO v_current_seats
        FROM ghe
        WHERE ma_phong = p_ma_phong;
        
        -- Kiểm tra sức chứa
        IF p_suc_chua < v_current_seats THEN
            SET p_success = FALSE;
            SET p_message = CONCAT('Loi: Suc chua moi phai >= so ghe hien tai (', v_current_seats, ')!');
            ROLLBACK;
        ELSE
            -- Cập nhật phòng
            UPDATE phong
            SET ten_phong = p_ten_phong,
                suc_chua = p_suc_chua,
                trang_thai = p_trang_thai
            WHERE ma_phong = p_ma_phong;
            
            SET p_success = TRUE;
            SET p_message = 'Thanh cong: Da cap nhat phong!';
            COMMIT;
        END IF;
    END IF;
END$$

-- ============================================
-- 7. STORED PROCEDURE: Xóa phòng an toàn
-- ============================================
DROP PROCEDURE IF EXISTS sp_xoa_phong$$
CREATE PROCEDURE sp_xoa_phong(
    IN p_ma_phong BIGINT,
    OUT p_success BOOLEAN,
    OUT p_message VARCHAR(255)
)
BEGIN
    DECLARE v_suat_count INT;
    DECLARE v_ghe_count INT;
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        SET p_success = FALSE;
        SET p_message = 'Loi: Khong the xoa phong!';
    END;
    
    START TRANSACTION;
    
    -- Kiểm tra suất chiếu đang hoạt động
    SELECT COUNT(*) INTO v_suat_count
    FROM suat_chieu
    WHERE ma_phong = p_ma_phong
    AND trang_thai IN ('LEN_KE_HOACH', 'MO_BAN')
    AND bat_dau_luc > NOW();
    
    IF v_suat_count > 0 THEN
        SET p_success = FALSE;
        SET p_message = 'Loi: Phong dang co suat chieu hoat dong!';
        ROLLBACK;
    ELSE
        -- Xóa ghế trước
        DELETE FROM ghe WHERE ma_phong = p_ma_phong;
        
        -- Xóa phòng
        DELETE FROM phong WHERE ma_phong = p_ma_phong;
        
        SET p_success = TRUE;
        SET p_message = 'Thanh cong: Da xoa phong!';
        COMMIT;
    END IF;
END$$

-- ============================================
-- 8. STORED PROCEDURE: Lấy thống kê phòng
-- ============================================
DROP PROCEDURE IF EXISTS sp_thong_ke_phong$$
CREATE PROCEDURE sp_thong_ke_phong()
BEGIN
    SELECT 
        COUNT(*) as tong_phong,
        SUM(CASE WHEN trang_thai = 'HOAT_DONG' THEN 1 ELSE 0 END) as dang_hoat_dong,
        SUM(CASE WHEN trang_thai = 'BAO_TRI' THEN 1 ELSE 0 END) as dang_bao_tri,
        SUM(CASE WHEN trang_thai = 'NGUNG' THEN 1 ELSE 0 END) as ngung_hoat_dong,
        SUM(suc_chua) as tong_ghe,
        ROUND(AVG(suc_chua), 0) as trung_binh_ghe
    FROM phong;
END$$

-- ============================================
-- 9. STORED PROCEDURE: Chi tiết phòng với ghế
-- ============================================
DROP PROCEDURE IF EXISTS sp_chi_tiet_phong$$
CREATE PROCEDURE sp_chi_tiet_phong(IN p_ma_phong BIGINT)
BEGIN
    -- Thông tin phòng
    SELECT 
        p.ma_phong,
        p.ten_phong,
        p.suc_chua,
        p.trang_thai,
        COUNT(g.ma_ghe) as so_ghe_thuc_te,
        p.tao_luc,
        p.cap_nhat_luc
    FROM phong p
    LEFT JOIN ghe g ON p.ma_phong = g.ma_phong
    WHERE p.ma_phong = p_ma_phong
    GROUP BY p.ma_phong;
    
    -- Danh sách ghế
    SELECT 
        g.ma_ghe,
        g.hang_ghe,
        g.so_ghe,
        lg.ten_loai_ghe,
        lg.he_so_gia
    FROM ghe g
    JOIN loai_ghe lg ON g.ma_loai_ghe = lg.ma_loai_ghe
    WHERE g.ma_phong = p_ma_phong
    ORDER BY g.hang_ghe, g.so_ghe;
END$$

-- ============================================
-- 10. FUNCTION: Kiểm tra phòng có thể xóa không
-- ============================================
DROP FUNCTION IF EXISTS fn_kiem_tra_xoa_phong$$
CREATE FUNCTION fn_kiem_tra_xoa_phong(p_ma_phong BIGINT)
RETURNS VARCHAR(100)
DETERMINISTIC
BEGIN
    DECLARE v_suat_count INT;
    DECLARE v_result VARCHAR(100);
    
    SELECT COUNT(*) INTO v_suat_count
    FROM suat_chieu
    WHERE ma_phong = p_ma_phong
    AND trang_thai IN ('LEN_KE_HOACH', 'MO_BAN')
    AND bat_dau_luc > NOW();
    
    IF v_suat_count > 0 THEN
        SET v_result = CONCAT('KHONG_THE_XOA (', v_suat_count, ' suat chieu)');
    ELSE
        SET v_result = 'CO_THE_XOA';
    END IF;
    
    RETURN v_result;
END$$

-- ============================================
-- 11. STORED PROCEDURE: Thay đổi trạng thái hàng loạt
-- ============================================
DROP PROCEDURE IF EXISTS sp_thay_doi_trang_thai_hang_loat$$
CREATE PROCEDURE sp_thay_doi_trang_thai_hang_loat(
    IN p_trang_thai_moi ENUM('HOAT_DONG','BAO_TRI','NGUNG'),
    IN p_danh_sach_ma_phong TEXT,
    OUT p_so_luong_cap_nhat INT,
    OUT p_message VARCHAR(255)
)
BEGIN
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        SET p_so_luong_cap_nhat = 0;
        SET p_message = 'Loi: Khong the cap nhat!';
    END;
    
    START TRANSACTION;
    
    -- Tạo bảng tạm để xử lý danh sách
    DROP TEMPORARY TABLE IF EXISTS temp_phong_ids;
    CREATE TEMPORARY TABLE temp_phong_ids (ma_phong BIGINT);
    
    -- Insert danh sách ID (giả sử format: "1,2,3,4")
    SET @sql = CONCAT('INSERT INTO temp_phong_ids VALUES (', 
                      REPLACE(p_danh_sach_ma_phong, ',', '),('), ')');
    PREPARE stmt FROM @sql;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
    
    -- Cập nhật trạng thái
    UPDATE phong p
    INNER JOIN temp_phong_ids t ON p.ma_phong = t.ma_phong
    SET p.trang_thai = p_trang_thai_moi;
    
    SET p_so_luong_cap_nhat = ROW_COUNT();
    SET p_message = CONCAT('Da cap nhat ', p_so_luong_cap_nhat, ' phong');
    
    DROP TEMPORARY TABLE IF EXISTS temp_phong_ids;
    COMMIT;
END$$

DELIMITER ;

-- ============================================
-- TEST DATA & EXAMPLES
-- ============================================

-- Test thêm phòng bằng procedure
CALL sp_them_phong('Phong Test 1', 100, 'HOAT_DONG', @ma_phong, @msg);
SELECT @ma_phong as ma_phong, @msg as message;

-- Test thống kê
CALL sp_thong_ke_phong();

-- Test kiểm tra xóa phòng
SELECT fn_kiem_tra_xoa_phong(1) as kiem_tra_xoa;

-- Xem log
SELECT * FROM phong_log ORDER BY thoi_gian DESC LIMIT 10;