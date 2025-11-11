/* 0️⃣ Chọn DB */
USE qlrapchieuphim;

/* 1️⃣ Xóa trigger cũ trên khach_hang, tai_khoan */
DELIMITER $$
DROP PROCEDURE IF EXISTS drop_triggers_kh_tk $$
CREATE PROCEDURE drop_triggers_kh_tk()
BEGIN
  DROP TRIGGER IF EXISTS tg_tai_khoan_bu;
  DROP TRIGGER IF EXISTS tg_khach_hang_bu;
END$$
DELIMITER ;

CALL drop_triggers_kh_tk();


/* 2️⃣ Trigger cập nhật thời gian */
DELIMITER $$
CREATE TRIGGER tg_tai_khoan_bu
BEFORE UPDATE ON tai_khoan
FOR EACH ROW
BEGIN
  SET NEW.cap_nhat_luc = NOW();
END $$

CREATE TRIGGER tg_khach_hang_bu
BEFORE UPDATE ON khach_hang
FOR EACH ROW
BEGIN
  SET NEW.cap_nhat_luc = NOW();
END $$
DELIMITER ;


/* 3️⃣ PROC thêm khách hàng */
DELIMITER $$
DROP PROCEDURE IF EXISTS proc_kh_insert $$
CREATE PROCEDURE proc_kh_insert(
    IN  p_ho_ten        VARCHAR(200),
    IN  p_email         VARCHAR(120),
    IN  p_sdt           VARCHAR(20),
    IN  p_ngay_sinh     DATE,
    IN  p_hoat_dong     TINYINT,
    IN  p_mat_khau      VARCHAR(255),
    OUT p_ma_khach_hang BIGINT
)
BEGIN
    DECLARE v_ma_tai_khoan BIGINT;

    /* 1. Thêm vào bảng tai_khoan */
    INSERT INTO tai_khoan(ho_ten, email, so_dien_thoai, hoat_dong, mat_khau_ma, vai_tro, tao_luc, cap_nhat_luc)
    VALUES (p_ho_ten, p_email, p_sdt, p_hoat_dong, p_mat_khau, 'KHACH_HANG', NOW(), NOW());
    SET v_ma_tai_khoan = LAST_INSERT_ID();

    /* 2. Thêm vào bảng khach_hang */
    INSERT INTO khach_hang(ma_tai_khoan, diem_tich_luy, ngay_sinh, tao_luc, cap_nhat_luc)
    VALUES (v_ma_tai_khoan, 0, p_ngay_sinh, NOW(), NOW());

    SET p_ma_khach_hang = LAST_INSERT_ID();
END $$
DELIMITER ;


/* 4️⃣ PROC cập nhật khách hàng */
DELIMITER $$
DROP PROCEDURE IF EXISTS proc_kh_update $$
CREATE PROCEDURE proc_kh_update(
    IN p_ma_khach_hang  BIGINT,
    IN p_ho_ten         VARCHAR(200),
    IN p_email          VARCHAR(120),
    IN p_sdt            VARCHAR(20),
    IN p_ngay_sinh      DATE,
    IN p_hoat_dong      TINYINT
)
BEGIN
    DECLARE v_ma_tai_khoan BIGINT;

    SELECT ma_tai_khoan INTO v_ma_tai_khoan
    FROM khach_hang WHERE ma_khach_hang = p_ma_khach_hang LIMIT 1;

    IF v_ma_tai_khoan IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Không tìm thấy khách hàng hoặc tài khoản liên kết';
    END IF;

    /* Cập nhật bảng tai_khoan */
    UPDATE tai_khoan
       SET ho_ten        = p_ho_ten,
           email         = p_email,
           so_dien_thoai = p_sdt,
           hoat_dong     = p_hoat_dong
     WHERE ma_tai_khoan = v_ma_tai_khoan;

    /* Cập nhật bảng khach_hang */
    UPDATE khach_hang
       SET ngay_sinh = p_ngay_sinh
     WHERE ma_khach_hang = p_ma_khach_hang;
END $$
DELIMITER ;


/* 5️⃣ PROC đổi mật khẩu */
DELIMITER $$
DROP PROCEDURE IF EXISTS proc_doi_mat_khau_khach_hang $$
CREATE PROCEDURE proc_doi_mat_khau_khach_hang(
    IN p_ma_khach_hang BIGINT,
    IN p_mat_khau_moi  VARCHAR(255)
)
BEGIN
    DECLARE v_ma_tai_khoan BIGINT;

    SELECT ma_tai_khoan INTO v_ma_tai_khoan
    FROM khach_hang WHERE ma_khach_hang = p_ma_khach_hang LIMIT 1;

    IF v_ma_tai_khoan IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Không tìm thấy khách hàng';
    END IF;

    UPDATE tai_khoan
       SET mat_khau_ma = p_mat_khau_moi
     WHERE ma_tai_khoan = v_ma_tai_khoan;
END $$
DELIMITER ;


/* 6️⃣ PROC bật/tắt hoạt động */
DELIMITER $$
DROP PROCEDURE IF EXISTS proc_toggle_hoat_dong_by_ma_kh $$
CREATE PROCEDURE proc_toggle_hoat_dong_by_ma_kh(IN p_ma_khach_hang BIGINT)
BEGIN
    DECLARE v_ma_tai_khoan BIGINT;
    DECLARE v_curr INT;

    SELECT kh.ma_tai_khoan, tk.hoat_dong
      INTO v_ma_tai_khoan, v_curr
    FROM khach_hang kh
    JOIN tai_khoan tk ON tk.ma_tai_khoan = kh.ma_tai_khoan
    WHERE kh.ma_khach_hang = p_ma_khach_hang
    LIMIT 1;

    IF v_ma_tai_khoan IS NOT NULL THEN
        UPDATE tai_khoan
           SET hoat_dong = CASE WHEN v_curr = 1 THEN 0 ELSE 1 END
         WHERE ma_tai_khoan = v_ma_tai_khoan;
    END IF;
END $$
DELIMITER ;
