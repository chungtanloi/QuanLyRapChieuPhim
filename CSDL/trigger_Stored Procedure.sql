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
-- 🔹 TRIGGER: Tự tạo bản ghi khách hàng sau khi thêm tài khoản KH
-- =========================================================
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
/**TIM KIEM*/
DELIMITER $$

DELIMITER $$

CREATE PROCEDURE sp_TimKiemToanCuc (IN tuKhoa VARCHAR(100))
BEGIN
    -- Kết quả phim
    SELECT 'Phim' AS Loai,
           p.ma_phim AS MaSo,
           p.ten_phim AS Ten,
           t.ten_the_loai AS Phu1,
           p.thoi_luong_phut AS Phu2
    FROM phim p
    LEFT JOIN phim_the_loai pt ON p.ma_phim = pt.ma_phim
    LEFT JOIN the_loai t ON pt.ma_loai = t.ma_loai
    WHERE LOWER(p.ten_phim) LIKE CONCAT('%', tuKhoa, '%')
       OR LOWER(t.ten_the_loai) LIKE CONCAT('%', tuKhoa, '%')
       OR LOWER(p.poster_url) LIKE CONCAT('%', tuKhoa, '%')

    UNION ALL

    -- Suất chiếu
    SELECT 'Suất chiếu', s.ma_suat_chieu, p.ten_phim, ph.ten_phong, DATE_FORMAT(s.gio_chieu, '%H:%i')
    FROM suat_chieu s
    JOIN phim p ON s.ma_phim = p.ma_phim
    JOIN phong ph ON s.ma_phong = ph.ma_phong
    WHERE LOWER(p.ten_phim) LIKE CONCAT('%', tuKhoa, '%')
       OR LOWER(ph.ten_phong) LIKE CONCAT('%', tuKhoa, '%')
       OR s.ma_suat_chieu LIKE CONCAT('%', tuKhoa, '%')

    UNION ALL

    -- Vé
    SELECT 'Vé', v.ma_ve, p.ten_phim, v.trang_thai, v.gia_ban
    FROM ve v
    JOIN suat_chieu s ON v.ma_suat_chieu = s.ma_suat_chieu
    JOIN phim p ON s.ma_phim = p.ma_phim
    WHERE v.ma_ve LIKE CONCAT('%', tuKhoa, '%')
       OR LOWER(p.ten_phim) LIKE CONCAT('%', tuKhoa, '%')

    UNION ALL

    -- Nhân viên
    SELECT 'Nhân viên', nv.ma_nhan_vien, nv.ten_nhan_vien, nv.chuc_vu, nv.email
    FROM nhan_vien nv
    WHERE LOWER(nv.ten_nhan_vien) LIKE CONCAT('%', tuKhoa, '%')
       OR LOWER(nv.chuc_vu) LIKE CONCAT('%', tuKhoa, '%')
       OR LOWER(nv.email) LIKE CONCAT('%', tuKhoa, '%')

    UNION ALL

    -- Khách hàng
    SELECT 'Khách hàng', kh.ma_khach_hang, kh.ten_khach_hang, kh.SDT, kh.email
    FROM khach_hang kh
    WHERE LOWER(kh.ten_khach_hang) LIKE CONCAT('%', tuKhoa, '%')
       OR LOWER(kh.SDT) LIKE CONCAT('%', tuKhoa, '%')
       OR LOWER(kh.email) LIKE CONCAT('%', tuKhoa, '%');
END$$

DELIMITER ;
