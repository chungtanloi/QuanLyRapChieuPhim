USE qlrapchieuphim;

-- =========================================================
-- 🔹 PROCEDURE: Đăng ký khách hàng (kiểm tra email trùng)
-- =========================================================
DELIMITER $$

CREATE PROCEDURE sp_dangky_khachhang (
    IN p_email      VARCHAR(120),
    IN p_matkhau    VARCHAR(255),
    IN p_hoten      VARCHAR(120)
)
BEGIN
    DECLARE tk_count INT DEFAULT 0;

    -- Kiểm tra email trùng
    SELECT COUNT(*) INTO tk_count
    FROM tai_khoan
    WHERE email = p_email;

    IF tk_count > 0 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Email này đã tồn tại!';
    ELSE
        INSERT INTO tai_khoan (email, mat_khau_ma, ho_ten, vai_tro)
        VALUES (p_email, p_matkhau, p_hoten, 'KHACH_HANG');
    END IF;
END $$

DELIMITER ;

-- =========================================================
-- 🔹 PROCEDURE: Đăng nhập (khách hàng)
-- =========================================================
DELIMITER $$

CREATE PROCEDURE sp_dangnhap (
    IN p_email      VARCHAR(120),
    IN p_matkhau    VARCHAR(255)
)
BEGIN
    SELECT vai_tro, ho_ten
    FROM tai_khoan
    WHERE email = p_email
      AND mat_khau_ma = p_matkhau
    LIMIT 1;
END $$

DELIMITER ;




-- =========================================================
-- 🔹 PROCEDURE: Đăng nhập nhân viên / quản trị
-- =========================================================
DELIMITER $$

CREATE PROCEDURE sp_dangnhap_admin_nhanvien (
    IN p_email      VARCHAR(120),
    IN p_matkhau    VARCHAR(255)
)
BEGIN
    DECLARE tk_count INT DEFAULT 0;
    DECLARE active_status TINYINT DEFAULT 0;

    -- Kiểm tra tài khoản tồn tại
    SELECT COUNT(*) INTO tk_count
    FROM tai_khoan
    WHERE email = p_email
      AND vai_tro IN ('QUAN_TRI', 'NHAN_VIEN');

    IF tk_count = 0 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Tài khoản không tồn tại hoặc không có quyền truy cập!';
    ELSE
        -- Kiểm tra mật khẩu và trạng thái
        SELECT hoat_dong INTO active_status
        FROM tai_khoan
        WHERE email = p_email
          AND mat_khau_ma = p_matkhau;

        IF active_status IS NULL THEN
            SIGNAL SQLSTATE '45000'
                SET MESSAGE_TEXT = 'Sai mật khẩu!';
        ELSEIF active_status = 0 THEN
            SIGNAL SQLSTATE '45000'
                SET MESSAGE_TEXT = 'Tài khoản đã bị khóa!';
        ELSE
            SELECT vai_tro, ho_ten
            FROM tai_khoan
            WHERE email = p_email
              AND mat_khau_ma = p_matkhau
              AND hoat_dong = 1
              AND vai_tro IN ('QUAN_TRI', 'NHAN_VIEN')
            LIMIT 1;
        END IF;
    END IF;
END $$

DELIMITER ;
DELIMITER $$


DELIMITER ;
DROP FUNCTION IF EXISTS fn_ten_tai_khoan;
DELIMITER $$
CREATE FUNCTION fn_ten_tai_khoan(p_ma_tai_khoan BIGINT)
RETURNS VARCHAR(255)
DETERMINISTIC
READS SQL DATA
BEGIN
  RETURN (
    SELECT tk.ho_ten
    FROM tai_khoan tk
    WHERE tk.ma_tai_khoan = p_ma_tai_khoan
    LIMIT 1
  );
END$$
DELIMITer ;

