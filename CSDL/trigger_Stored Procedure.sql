use qlrapchieuphim;
/*Đăng ký kiểm tra điều điện*/
DELIMITER $$

CREATE PROCEDURE sp_dangky_khachhang (
    IN p_email VARCHAR(120),
    IN p_matkhau VARCHAR(255),
    IN p_hoten VARCHAR(120)
)
BEGIN
    DECLARE tk_count INT DEFAULT 0;

    -- Kiểm tra email trùng
    SELECT COUNT(*) INTO tk_count FROM tai_khoan WHERE email = p_email;

    IF tk_count > 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Email này đã tồn tại!';
    ELSE
        INSERT INTO tai_khoan (email, mat_khau_ma, ho_ten, vai_tro)
        VALUES (p_email, p_matkhau, p_hoten, 'KHACH_HANG');
    END IF;
END $$

DELIMITER ;

/*Lấy thông tin đăng nhập*/
DELIMITER $$

CREATE PROCEDURE sp_dangnhap (
    IN p_email VARCHAR(120),
    IN p_matkhau VARCHAR(255)
)
BEGIN
    -- Trả về vai_tro và ho_ten nếu khớp
    SELECT vai_tro, ho_ten
    FROM tai_khoan
    WHERE email = p_email AND mat_khau_ma = p_matkhau
    LIMIT 1;
END $$

DELIMITER ;

/*Trigger tự tạo bản ghi khach hang*/
DELIMITER $$

CREATE TRIGGER trg_after_taikhoan_insert
AFTER INSERT ON tai_khoan
FOR EACH ROW
BEGIN
    IF NEW.vai_tro = 'KHACH_HANG' THEN
        INSERT INTO khach_hang (ma_tai_khoan, diem_tich_luy)
        VALUES (NEW.ma_tai_khoan, 0);
    END IF;
END $$

DELIMITER ;

/*Kiem tra tai khoan nhan vien va admin con ton tâi khong va mat khau*/
DELIMITER $$

CREATE PROCEDURE sp_dangnhap_admin_nhanvien (
    IN p_email VARCHAR(120),
    IN p_matkhau VARCHAR(255)
)
BEGIN
    DECLARE tk_count INT DEFAULT 0;
    DECLARE active_status TINYINT DEFAULT 0;

    -- Kiểm tra email có tồn tại và vai trò phù hợp hay không
    SELECT COUNT(*) INTO tk_count
    FROM tai_khoan
    WHERE email = p_email
      AND vai_tro IN ('QUAN_TRI', 'NHAN_VIEN');

    IF tk_count = 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Tài khoản không tồn tại hoặc không có quyền truy cập!';
    ELSE
        -- Kiểm tra mật khẩu & trạng thái
        SELECT hoat_dong INTO active_status
        FROM tai_khoan
        WHERE email = p_email AND mat_khau_ma = p_matkhau;

        IF active_status IS NULL THEN
            SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Sai mật khẩu!';
        ELSEIF active_status = 0 THEN
            SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Tài khoản đã bị khóa!';
        ELSE
            -- Trả kết quả cho Java
            SELECT vai_tro, ho_ten
            FROM tai_khoan
            WHERE email = p_email AND mat_khau_ma = p_matkhau
              AND hoat_dong = 1
              AND vai_tro IN ('QUAN_TRI', 'NHAN_VIEN')
            LIMIT 1;
        END IF;
    END IF;
END $$

DELIMITER ;
