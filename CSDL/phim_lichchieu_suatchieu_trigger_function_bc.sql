USE qlrapchieuphim;
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS=0;

-- 1. DỮ LIỆU CƠ BẢN
INSERT INTO tai_khoan(ma_tai_khoan, email, mat_khau_ma, ho_ten, so_dien_thoai, vai_tro)
VALUES
  (1, 'admin@cinema.vn',    '$2y$10$hash', 'Quan tri he thong', '0909000001', 'QUAN_TRI'),
  (2, 'staff.ct@cinema.vn','$2y$10$hash', 'Nhan vien Can Tho', '0909000002', 'NHAN_VIEN'),
  (3, 'kh1.cantho@example.com', '$2y$10$hash', 'Nguyen Van A', '0909001001', 'KHACH_HANG')
ON DUPLICATE KEY UPDATE ho_ten=VALUES(ho_ten);

INSERT IGNORE INTO nhan_vien(ma_nhan_vien, ma_tai_khoan, chuc_vu, ngay_vao_lam)
VALUES (1, 2, 'Thu ngan', DATE('2023-09-01'));

INSERT IGNORE INTO khach_hang(ma_khach_hang, ma_tai_khoan, diem_tich_luy, ngay_sinh)
VALUES (1, 3, 120, DATE('2003-10-10'));

INSERT INTO rap (ma_rap, ten_rap, dia_chi) VALUES
(1, 'CGV Sense City Can Tho', 'Tầng 4, TTTM Sense City, 01 Đại lộ Hoà Bình, Ninh Kiều, Cần Thơ'),
(2, 'Lotte Cinema Ninh Kieu', 'Tầng 3, TTTM Lotte Mart, 84 Mậu Thân, An Hoà, Ninh Kiều, Cần Thơ')
ON DUPLICATE KEY UPDATE dia_chi=VALUES(dia_chi);

SET @ma_rap_cgv = 1;
SET @ma_phong_cgv1 = 1;
SET @ma_phong_cgv2 = 2;
SET @ma_phong_lotte1 = 3;

INSERT INTO phong(ma_phong, ten_phong, suc_chua, trang_thai, ma_rap) VALUES
(1, 'CGV Sense City - Phong 1', 120, 'HOAT_DONG', @ma_rap_cgv),
(2, 'CGV Sense City - Phong 2', 100, 'HOAT_DONG', @ma_rap_cgv),
(3, 'Lotte Ninh Kieu - Phong 1', 120, 'HOAT_DONG', 2)
ON DUPLICATE KEY UPDATE suc_chua=VALUES(suc_chua), ma_rap=VALUES(ma_rap);

INSERT INTO the_loai(ma_the_loai, ten_the_loai) VALUES 
(1, 'Hanh dong'), (2, 'Kinh di'), (3, 'Hai'), (4, 'Tam ly'), 
(5, 'Khoa hoc'), (6, 'Vien tuong'), (7, 'Gia dinh'), (8, 'Hoat hinh'), (9, 'Phieu luu')
ON DUPLICATE KEY UPDATE ten_the_loai=VALUES(ten_the_loai);

SET @tl_hanh_dong = 1; SET @tl_kinh_di = 2; SET @tl_hai = 3; SET @tl_gia_dinh = 7;
SET @tl_hoat_hinh = 8; SET @tl_phieu_luu = 9;

INSERT INTO phim(ma_phim, ten_phim, thoi_luong_phut, phan_loai, ngay_phat_hanh, mo_ta) VALUES
  (1, 'Cuc Vang Cua Ngoai', 119, 'T13', CURDATE() - INTERVAL 10 DAY, 'Phim Viet Nam the loai gia dinh/hai hước'),
  (2, 'Nha Ma Xo',          108, 'T16', CURDATE() + INTERVAL 5 DAY, 'Phim kinh di Viet Nam'),
  (3, 'TEE YOD 3: Quy An Tang', 104, 'T18', CURDATE() - INTERVAL 2 DAY, 'Phim kinh di Thai Lan'),
  (4, 'Tu Chien Tren Khong', 120, 'T13', CURDATE() - INTERVAL 30 DAY, 'Phim hanh dong khong chien'),
  (5, 'Mua Do',              122, 'T16', CURDATE() + INTERVAL 15 DAY, 'Phim Viet Nam noi bat 2025'),
  (6, 'Doremon: Nobita và Bản Giao Hưởng Địa Cầu', 107, 'P', CURDATE() - INTERVAL 5 DAY, 'Phim hoạt hình cho trẻ em'),
  (7, 'Spider-Man: Across the Spider-Verse', 140, 'P', CURDATE() - INTERVAL 7 DAY, 'Phim hoạt hình siêu anh hùng'),
  (8, 'Oppenheimer',         180, 'T18', CURDATE() - INTERVAL 40 DAY, 'Phim tiểu sử, lịch sử'),
  (9, 'Transformers: Quái Vật Trỗi Dậy', 127, 'T13', CURDATE() - INTERVAL 1 DAY, 'Phim hành động, khoa học viễn tưởng'),
  (10, 'Mission Impossible 8', 150, 'T16', CURDATE() + INTERVAL 20 DAY, 'Phim hành động kịch tính')
ON DUPLICATE KEY UPDATE ten_phim=VALUES(ten_phim), phan_loai=VALUES(phan_loai), ngay_phat_hanh=VALUES(ngay_phat_hanh);

DELETE FROM phim_the_loai; 
INSERT INTO phim_the_loai(ma_phim, ma_the_loai) VALUES 
(1, @tl_hai), (1, @tl_gia_dinh), (2, @tl_kinh_di), (3, @tl_kinh_di), 
(4, @tl_hanh_dong), (5, 4), (5, @tl_kinh_di), (6, @tl_hoat_hinh), 
(7, @tl_hoat_hinh), (7, @tl_phieu_luu), (8, 4), (9, @tl_hanh_dong), 
(9, 6), (10, @tl_hanh_dong), (10, 5); 

INSERT IGNORE INTO dinh_dang(ma_dinh_dang, ten_dinh_dang) VALUES (1, '2D'),(2, '3D'),(3, 'IMAX');
INSERT IGNORE INTO loai_ghe(ma_loai_ghe, ten_loai_ghe, he_so_gia) VALUES (1, 'Thuong',1.00),(2, 'VIP',1.30),(3, 'Sweetbox',1.50);
SET @ma_dd_2d = 1; SET @ma_dd_3d = 2;
SET @ma_phim_cvcn = 1; SET @ma_phim_nha_ma_xo = 2; SET @ma_phim_spider = 7; SET @ma_phim_oppen = 8;
SET @ma_phim_trans = 9;

-- XÓA Suất chiếu cũ và chèn dữ liệu mới cho ngày hiện tại (CURDATE())
DELETE FROM suat_chieu;

-- Suất chiếu cho HÔM NAY (Giả sử 12/11/2025 - Hôm nay là 12/11)
INSERT INTO suat_chieu (ma_suat_chieu, ma_phim, ma_phong, ma_dinh_dang, bat_dau_luc, gia_co_ban, trang_thai) VALUES
-- CGV Phong 1 (Hiện tại là 22:12)
(1, @ma_phim_cvcn,    @ma_phong_cgv1, @ma_dd_2d, CURDATE() + INTERVAL 9 HOUR, 50000.00, 'MO_BAN'), -- 09:00 (Đã Chiếu)
(2, @ma_phim_cvcn,    @ma_phim_trans, @ma_dd_2d, CURDATE() + INTERVAL 12 HOUR, 50000.00, 'MO_BAN'), -- 12:00 (Đã Chiếu)
(3, @ma_phim_spider, @ma_phong_cgv1, @ma_dd_3d, CURDATE() + INTERVAL 15 HOUR, 65000.00, 'MO_BAN'), -- 15:00 (Đã Chiếu)
(4, @ma_phim_trans,  @ma_phong_cgv1, @ma_dd_2d, CURDATE() + INTERVAL 20 HOUR, 55000.00, 'MO_BAN'), -- 20:00 (Đã Chiếu)
(5, @ma_phim_nha_ma_xo, @ma_phong_cgv1, @ma_dd_2d, CURDATE() + INTERVAL 22 HOUR + INTERVAL 30 MINUTE, 60000.00, 'MO_BAN'), -- 22:30 (Sắp Chiếu)
-- CGV Phong 2 
(6, @ma_phim_oppen,  @ma_phong_cgv2, @ma_dd_2d, CURDATE() + INTERVAL 18 HOUR, 50000.00, 'MO_BAN'), -- 18:00 (Đã Chiếu)
(7, @ma_phim_cvcn,    @ma_phong_cgv2, @ma_dd_2d, CURDATE() + INTERVAL 23 HOUR, 50000.00, 'MO_BAN'); -- 23:00 (Sắp Chiếu)

-- Suất chiếu cho NGÀY MAI (+1 ngày) - 13/11/2025
INSERT INTO suat_chieu (ma_suat_chieu, ma_phim, ma_phong, ma_dinh_dang, bat_dau_luc, gia_co_ban, trang_thai) VALUES
(8, @ma_phim_nha_ma_xo, @ma_phong_cgv1, @ma_dd_2d, CURDATE() + INTERVAL 1 DAY + INTERVAL 10 HOUR, 60000.00, 'MO_BAN'), 
(9, @ma_phim_spider,    @ma_phong_cgv1, @ma_dd_3d, CURDATE() + INTERVAL 1 DAY + INTERVAL 17 HOUR, 70000.00, 'MO_BAN'),
(10, @ma_phim_cvcn,      @ma_phong_lotte1, @ma_dd_2d, CURDATE() + INTERVAL 1 DAY + INTERVAL 14 HOUR, 50000.00, 'MO_BAN');

-- Dữ liệu ghế mẫu 
SET @ma_loai_thuong = 1; SET @ma_loai_vip = 2;
DELETE FROM ghe; -- Xóa tất cả ghế cũ

-- CGV Phong 1 (120 ghế)
-- Dòng A (VIP)
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe) 
SELECT @ma_phong_cgv1, 'A', N, @ma_loai_vip FROM (SELECT 1 AS N UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9 UNION ALL SELECT 10) AS t;
-- Dòng B (Thường)
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe) 
SELECT @ma_phong_cgv1, 'B', N, @ma_loai_thuong FROM (SELECT 1 AS N UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9 UNION ALL SELECT 10 UNION ALL SELECT 11 UNION ALL SELECT 12 UNION ALL SELECT 13 UNION ALL SELECT 14 UNION ALL SELECT 15) AS t;
-- Dòng C (Thường)
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe) 
SELECT @ma_phong_cgv1, 'C', N, @ma_loai_thuong FROM (SELECT 1 AS N UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9 UNION ALL SELECT 10 UNION ALL SELECT 11 UNION ALL SELECT 12 UNION ALL SELECT 13 UNION ALL SELECT 14 UNION ALL SELECT 15) AS t;

SET FOREIGN_KEY_CHECKS = 1;


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