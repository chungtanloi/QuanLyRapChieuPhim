/* 0️⃣ Chọn DB */
USE qlrapchieuphim;

/* 1️⃣ Dọn sạch mọi trigger trên 2 bảng: nhan_vien, tai_khoan */
DELIMITER $$
DROP PROCEDURE IF EXISTS drop_triggers_nv_tk $$
CREATE PROCEDURE drop_triggers_nv_tk()
BEGIN
  DECLARE done INT DEFAULT 0;
  DECLARE v_stmt TEXT;

  DECLARE cur CURSOR FOR
    SELECT CONCAT('DROP TRIGGER IF EXISTS `', TRIGGER_NAME, '`')
    FROM information_schema.TRIGGERS
    WHERE TRIGGER_SCHEMA = DATABASE()
      AND EVENT_OBJECT_TABLE IN ('nhan_vien','tai_khoan');

  DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = 1;

  OPEN cur;
  read_loop: LOOP
    FETCH cur INTO v_stmt;
    IF done = 1 THEN LEAVE read_loop; END IF;
    SET @s = v_stmt;
    PREPARE x FROM @s;
    EXECUTE x;
    DEALLOCATE PREPARE x;
  END LOOP;
  CLOSE cur;
END $$
DELIMITER ;

CALL drop_triggers_nv_tk();
DROP PROCEDURE IF EXISTS drop_triggers_nv_tk;

/* 2️⃣ Bảng log (nếu chưa có) */
CREATE TABLE IF NOT EXISTS log_tim_kiem (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  tu_khoa VARCHAR(255),
  thoi_gian TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

/* 3️⃣ Tạo 2 trigger BEFORE UPDATE (trước đó drop nếu còn) */
DROP TRIGGER IF EXISTS tg_tai_khoan_bu;
DROP TRIGGER IF EXISTS tg_nhan_vien_bu;

DELIMITER $$
CREATE TRIGGER tg_tai_khoan_bu
BEFORE UPDATE ON tai_khoan
FOR EACH ROW
BEGIN
  SET NEW.cap_nhat_luc = NOW();
END $$

CREATE TRIGGER tg_nhan_vien_bu
BEFORE UPDATE ON nhan_vien
FOR EACH ROW
BEGIN
  SET NEW.cap_nhat_luc = NOW();
END $$
DELIMITER ;

/* 4️⃣ Function tiện ích (chỉ SELECT) */
DELIMITER $$
DROP FUNCTION IF EXISTS fn_ten_tai_khoan $$
CREATE FUNCTION fn_ten_tai_khoan(p_ma_tai_khoan BIGINT)
RETURNS VARCHAR(200)
DETERMINISTIC
READS SQL DATA
BEGIN
  DECLARE v_ten VARCHAR(200);
  SELECT COALESCE(ho_ten, email)
    INTO v_ten
    FROM tai_khoan
   WHERE ma_tai_khoan = p_ma_tai_khoan
   LIMIT 1;
  RETURN v_ten;
END $$
DELIMITER ;

/* 5️⃣ Procedures an toàn */
DELIMITER $$

/* === proc_nv_insert === */
DROP PROCEDURE IF EXISTS proc_nv_insert $$
CREATE PROCEDURE proc_nv_insert(
    IN  p_ho_ten       VARCHAR(200),
    IN  p_email        VARCHAR(120),
    IN  p_sdt          VARCHAR(20),
    IN  p_ngay_vao     DATE,
    IN  p_hoat_dong    TINYINT,
    IN  p_mat_khau     VARCHAR(255),
    OUT p_ma_nhan_vien BIGINT
)
BEGIN
    DECLARE v_ma_tai_khoan BIGINT;

    INSERT INTO tai_khoan(ho_ten, email, so_dien_thoai, hoat_dong, mat_khau_ma, vai_tro, tao_luc, cap_nhat_luc)
    VALUES (p_ho_ten, p_email, p_sdt, p_hoat_dong, p_mat_khau, 'NHAN_VIEN', NOW(), NOW());
    SET v_ma_tai_khoan = LAST_INSERT_ID();

    INSERT INTO nhan_vien(ma_tai_khoan, ngay_vao_lam, tao_luc, cap_nhat_luc)
    VALUES (v_ma_tai_khoan, p_ngay_vao, NOW(), NOW());

    SET p_ma_nhan_vien = LAST_INSERT_ID();
END $$

/* === proc_nv_update === */
DROP PROCEDURE IF EXISTS proc_nv_update $$
CREATE PROCEDURE proc_nv_update(
    IN p_ma_nhan_vien BIGINT,
    IN p_ho_ten       VARCHAR(200),
    IN p_email        VARCHAR(120),
    IN p_sdt          VARCHAR(20),
    IN p_ngay_vao     DATE,
    IN p_hoat_dong    TINYINT
)
BEGIN
    DECLARE v_ma_tai_khoan BIGINT;

    SELECT ma_tai_khoan INTO v_ma_tai_khoan
    FROM nhan_vien WHERE ma_nhan_vien = p_ma_nhan_vien LIMIT 1;

    IF v_ma_tai_khoan IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Không tìm thấy nhân viên hoặc tài khoản liên kết';
    END IF;

    UPDATE tai_khoan
       SET ho_ten        = p_ho_ten,
           email         = p_email,
           so_dien_thoai = p_sdt,
           hoat_dong     = p_hoat_dong
     WHERE ma_tai_khoan = v_ma_tai_khoan;

    UPDATE nhan_vien
       SET ngay_vao_lam = p_ngay_vao
     WHERE ma_nhan_vien = p_ma_nhan_vien;
END $$

/* === proc_doi_mat_khau_nhan_vien === */
DROP PROCEDURE IF EXISTS proc_doi_mat_khau_nhan_vien $$
CREATE PROCEDURE proc_doi_mat_khau_nhan_vien(
    IN p_ma_nhan_vien BIGINT,
    IN p_mat_khau_moi VARCHAR(255)
)
BEGIN
    DECLARE v_ma_tai_khoan BIGINT;

    SELECT ma_tai_khoan INTO v_ma_tai_khoan
    FROM nhan_vien WHERE ma_nhan_vien = p_ma_nhan_vien LIMIT 1;

    IF v_ma_tai_khoan IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Không tìm thấy nhân viên';
    END IF;

    UPDATE tai_khoan
       SET mat_khau_ma = p_mat_khau_moi
     WHERE ma_tai_khoan = v_ma_tai_khoan;
END $$

/* === proc_toggle_hoat_dong_by_ma_nv === */
DROP PROCEDURE IF EXISTS proc_toggle_hoat_dong_by_ma_nv $$
CREATE PROCEDURE proc_toggle_hoat_dong_by_ma_nv(IN p_ma_nhan_vien BIGINT)
BEGIN
    DECLARE v_ma_tai_khoan BIGINT;
    DECLARE v_curr INT;

    SELECT nv.ma_tai_khoan, tk.hoat_dong
      INTO v_ma_tai_khoan, v_curr
    FROM nhan_vien nv
    JOIN tai_khoan tk ON tk.ma_tai_khoan = nv.ma_tai_khoan
    WHERE nv.ma_nhan_vien = p_ma_nhan_vien
    LIMIT 1;

    IF v_ma_tai_khoan IS NOT NULL THEN
        UPDATE tai_khoan
           SET hoat_dong = CASE WHEN v_curr = 1 THEN 0 ELSE 1 END
         WHERE ma_tai_khoan = v_ma_tai_khoan;
    END IF;
END $$

/* === proc_tim_kiem_tong_hop === */
DROP PROCEDURE IF EXISTS proc_tim_kiem_tong_hop $$
CREATE PROCEDURE proc_tim_kiem_tong_hop(IN tu_khoa VARCHAR(255))
BEGIN
    INSERT INTO log_tim_kiem(tu_khoa) VALUES (tu_khoa);

    SELECT 'Phim' AS loai, CAST(p.ma_phim AS CHAR) AS ma,
           p.ten_phim AS ten, CONCAT('Thời lượng ', p.thoi_luong_phut, ' phút') AS thong_tin
    FROM phim p
    WHERE p.ten_phim LIKE CONCAT('%', tu_khoa, '%')

    UNION ALL
    SELECT 'Khách hàng', CAST(kh.ma_khach_hang AS CHAR),
           COALESCE(tk.ho_ten, tk.email),
           tk.so_dien_thoai
    FROM khach_hang kh
    JOIN tai_khoan tk ON tk.ma_tai_khoan = kh.ma_tai_khoan
    WHERE tk.ho_ten LIKE CONCAT('%', tu_khoa, '%')
       OR tk.email LIKE CONCAT('%', tu_khoa, '%')
       OR tk.so_dien_thoai LIKE CONCAT('%', tu_khoa, '%')
       OR kh.ma_khach_hang LIKE CONCAT('%', tu_khoa, '%')

    UNION ALL
    SELECT 'Đơn hàng', CAST(dh.ma_don_hang AS CHAR),
           COALESCE(tk.ho_ten, tk.email),
           CAST(dh.trang_thai AS CHAR)
    FROM don_hang dh
    LEFT JOIN khach_hang kh ON kh.ma_khach_hang = dh.ma_khach_hang
    LEFT JOIN tai_khoan tk ON tk.ma_tai_khoan = kh.ma_tai_khoan
    WHERE dh.ma_don_hang LIKE CONCAT('%', tu_khoa, '%')
       OR tk.ho_ten LIKE CONCAT('%', tu_khoa, '%')
       OR tk.email LIKE CONCAT('%', tu_khoa, '%')
       OR tk.so_dien_thoai LIKE CONCAT('%', tu_khoa, '%')

    UNION ALL
    SELECT 'Nhân viên', CAST(nv.ma_nhan_vien AS CHAR),
           COALESCE(tk.ho_ten, tk.email),
           CONCAT('Hoạt động: ', tk.hoat_dong)
    FROM nhan_vien nv
    JOIN tai_khoan tk ON tk.ma_tai_khoan = nv.ma_tai_khoan
    WHERE tk.ho_ten LIKE CONCAT('%', tu_khoa, '%')
       OR tk.email LIKE CONCAT('%', tu_khoa, '%')
       OR tk.so_dien_thoai LIKE CONCAT('%', tu_khoa, '%')
       OR nv.ma_nhan_vien LIKE CONCAT('%', tu_khoa, '%')

    UNION ALL
    SELECT 'Sản phẩm', CAST(sp.ma_san_pham AS CHAR),
           sp.ten_san_pham,
           CONCAT('Loại: ', sp.loai, ' | Giá: ', sp.gia, ' | Hoạt động: ', sp.hoat_dong)
    FROM san_pham sp
    WHERE sp.ten_san_pham LIKE CONCAT('%', tu_khoa, '%')
       OR sp.loai LIKE CONCAT('%', tu_khoa, '%')
       OR CAST(sp.gia AS CHAR) LIKE CONCAT('%', tu_khoa, '%')

    UNION ALL
    SELECT 'Combo', CAST(c.ma_combo AS CHAR),
           c.ten_combo,
           CAST(c.hoat_dong AS CHAR)
    FROM combo c
    WHERE c.ten_combo LIKE CONCAT('%', tu_khoa, '%');
END $$
DELIMITER ;

/* 6️⃣ Reset AUTO_INCREMENT của tai_khoan theo MAX hiện có (nếu cần) */
SET @next_id = (SELECT IFNULL(MAX(ma_tai_khoan),0)+1 FROM tai_khoan);
SET @sql = CONCAT('ALTER TABLE tai_khoan AUTO_INCREMENT = ', @next_id);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
CALL proc_nv_insert('Nguyễn Văn A','k@example.com','0912345678','2025-11-11',1,'pw_demo',@out);
SELECT @out;         -- xem mã nhân viên vừa tạo
SHOW TRIGGERS;       -- chỉ còn 2 trigger BEFORE UPDATE như trên
