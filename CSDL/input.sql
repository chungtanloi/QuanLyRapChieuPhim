-- SEED DATA for MySQL 8 schema (Can Tho)
use qlrapchieuphim;
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS=0;

INSERT INTO tai_khoan(email, mat_khau_ma, ho_ten, so_dien_thoai, vai_tro)
VALUES
  ('admin@cinema.vn',   '$2y$10$hash', 'Quan tri he thong', '0909000001', 'QUAN_TRI'),
  ('staff.ct@cinema.vn','$2y$10$hash', 'Nhan vien Can Tho', '0909000002', 'NHAN_VIEN'),
  ('kh1.cantho@example.com', '$2y$10$hash', 'Nguyen Van A', '0909001001', 'KHACH_HANG'),
  ('kh2.cantho@example.com', '$2y$10$hash', 'Tran Thi B',   '0909001002', 'KHACH_HANG')
ON DUPLICATE KEY UPDATE ho_ten=VALUES(ho_ten);

INSERT INTO nhan_vien(ma_tai_khoan, chuc_vu, ngay_vao_lam)
SELECT ma_tai_khoan, 'Thu ngan', DATE('2023-09-01') FROM tai_khoan WHERE email='staff.ct@cinema.vn'
ON DUPLICATE KEY UPDATE chuc_vu=VALUES(chuc_vu);


INSERT INTO khach_hang(ma_tai_khoan, diem_tich_luy, ngay_sinh)
SELECT ma_tai_khoan, 120, DATE('2003-10-10') FROM tai_khoan WHERE email='kh1.cantho@example.com'
ON DUPLICATE KEY UPDATE diem_tich_luy=VALUES(diem_tich_luy);


INSERT INTO khach_hang(ma_tai_khoan, diem_tich_luy, ngay_sinh)
SELECT ma_tai_khoan, 45, DATE('2004-05-12') FROM tai_khoan WHERE email='kh2.cantho@example.com'
ON DUPLICATE KEY UPDATE diem_tich_luy=VALUES(diem_tich_luy);


INSERT INTO the_loai(ten_the_loai) VALUES ('Hanh dong'),('Kinh di'),('Hai'),('Tam ly')
ON DUPLICATE KEY UPDATE ten_the_loai=VALUES(ten_the_loai);


INSERT IGNORE INTO dinh_dang(ten_dinh_dang) VALUES ('2D'),('3D'),('IMAX');


INSERT INTO phim(ten_phim, thoi_luong_phut, phan_loai, ngay_phat_hanh, mo_ta) VALUES
  ('Cuc Vang Cua Ngoai', 119, 'T13', DATE('2025-10-01'), 'Phim Viet Nam the loai gia dinh/hao hinh'),
  ('Nha Ma Xo',          108, 'T16', DATE('2025-10-15'), 'Phim kinh di Viet Nam'),
  ('TEE YOD 3: Quy An Tang', 104, 'T18', DATE('2025-10-20'), 'Phim kinh di Thai Lan'),
  ('Tu Chien Tren Khong', 120, 'T13', DATE('2025-08-21'), 'Phim hanh dong khong chien'),
  ('Mua Do',             122, 'T16', DATE('2025-08-21'), 'Phim Viet Nam noi bat 2025')
ON DUPLICATE KEY UPDATE mo_ta=VALUES(mo_ta);


INSERT IGNORE INTO phim_the_loai(ma_phim, ma_the_loai)
SELECT p.ma_phim, tl.ma_the_loai FROM phim p
JOIN the_loai tl ON ( (p.ten_phim='Cuc Vang Cua Ngoai' AND tl.ten_the_loai='Hai')
                  OR (p.ten_phim='Nha Ma Xo'          AND tl.ten_the_loai='Kinh di')
                  OR (p.ten_phim='TEE YOD 3: Quy An Tang' AND tl.ten_the_loai='Kinh di')
                  OR (p.ten_phim='Tu Chien Tren Khong' AND tl.ten_the_loai='Hanh dong')
                  OR (p.ten_phim='Mua Do'              AND tl.ten_the_loai='Tam ly') );


INSERT INTO phong(ten_phong, suc_chua, trang_thai) VALUES
  ('CGV Sense City - Phong 1', 120, 'HOAT_DONG'),
  ('CGV Sense City - Phong 2', 100, 'HOAT_DONG'),
  ('Lotte Ninh Kieu - Phong 1', 120, 'HOAT_DONG'),
  ('Lotte Ninh Kieu - Phong 2', 100, 'HOAT_DONG')
ON DUPLICATE KEY UPDATE suc_chua=VALUES(suc_chua);


INSERT IGNORE INTO loai_ghe(ten_loai_ghe, he_so_gia) VALUES ('Thuong',1.00),('VIP',1.30),('Sweetbox',1.50);

INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'A', 1, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='CGV Sense City - Phong 1' AND lg.ten_loai_ghe='VIP';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'A', 2, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='CGV Sense City - Phong 1' AND lg.ten_loai_ghe='VIP';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'A', 3, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='CGV Sense City - Phong 1' AND lg.ten_loai_ghe='VIP';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'A', 4, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='CGV Sense City - Phong 1' AND lg.ten_loai_ghe='VIP';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'A', 5, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='CGV Sense City - Phong 1' AND lg.ten_loai_ghe='VIP';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'A', 6, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='CGV Sense City - Phong 1' AND lg.ten_loai_ghe='VIP';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'A', 7, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='CGV Sense City - Phong 1' AND lg.ten_loai_ghe='VIP';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'A', 8, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='CGV Sense City - Phong 1' AND lg.ten_loai_ghe='VIP';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'A', 9, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='CGV Sense City - Phong 1' AND lg.ten_loai_ghe='VIP';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'A', 10, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='CGV Sense City - Phong 1' AND lg.ten_loai_ghe='VIP';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'A', 11, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='CGV Sense City - Phong 1' AND lg.ten_loai_ghe='VIP';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'A', 12, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='CGV Sense City - Phong 1' AND lg.ten_loai_ghe='VIP';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'B', 1, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='CGV Sense City - Phong 1' AND lg.ten_loai_ghe='VIP';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'B', 2, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='CGV Sense City - Phong 1' AND lg.ten_loai_ghe='VIP';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'B', 3, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='CGV Sense City - Phong 1' AND lg.ten_loai_ghe='VIP';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'B', 4, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='CGV Sense City - Phong 1' AND lg.ten_loai_ghe='VIP';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'B', 5, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='CGV Sense City - Phong 1' AND lg.ten_loai_ghe='VIP';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'B', 6, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='CGV Sense City - Phong 1' AND lg.ten_loai_ghe='VIP';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'B', 7, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='CGV Sense City - Phong 1' AND lg.ten_loai_ghe='VIP';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'B', 8, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='CGV Sense City - Phong 1' AND lg.ten_loai_ghe='VIP';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'B', 9, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='CGV Sense City - Phong 1' AND lg.ten_loai_ghe='VIP';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'B', 10, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='CGV Sense City - Phong 1' AND lg.ten_loai_ghe='VIP';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'B', 11, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='CGV Sense City - Phong 1' AND lg.ten_loai_ghe='VIP';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'B', 12, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='CGV Sense City - Phong 1' AND lg.ten_loai_ghe='VIP';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'C', 1, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='CGV Sense City - Phong 1' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'C', 2, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='CGV Sense City - Phong 1' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'C', 3, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='CGV Sense City - Phong 1' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'C', 4, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='CGV Sense City - Phong 1' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'C', 5, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='CGV Sense City - Phong 1' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'C', 6, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='CGV Sense City - Phong 1' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'C', 7, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='CGV Sense City - Phong 1' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'C', 8, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='CGV Sense City - Phong 1' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'C', 9, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='CGV Sense City - Phong 1' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'C', 10, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='CGV Sense City - Phong 1' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'C', 11, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='CGV Sense City - Phong 1' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'C', 12, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='CGV Sense City - Phong 1' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'D', 1, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='CGV Sense City - Phong 1' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'D', 2, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='CGV Sense City - Phong 1' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'D', 3, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='CGV Sense City - Phong 1' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'D', 4, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='CGV Sense City - Phong 1' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'D', 5, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='CGV Sense City - Phong 1' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'D', 6, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='CGV Sense City - Phong 1' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'D', 7, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='CGV Sense City - Phong 1' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'D', 8, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='CGV Sense City - Phong 1' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'D', 9, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='CGV Sense City - Phong 1' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'D', 10, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='CGV Sense City - Phong 1' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'D', 11, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='CGV Sense City - Phong 1' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'D', 12, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='CGV Sense City - Phong 1' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'E', 1, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='CGV Sense City - Phong 1' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'E', 2, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='CGV Sense City - Phong 1' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'E', 3, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='CGV Sense City - Phong 1' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'E', 4, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='CGV Sense City - Phong 1' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'E', 5, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='CGV Sense City - Phong 1' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'E', 6, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='CGV Sense City - Phong 1' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'E', 7, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='CGV Sense City - Phong 1' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'E', 8, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='CGV Sense City - Phong 1' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'E', 9, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='CGV Sense City - Phong 1' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'E', 10, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='CGV Sense City - Phong 1' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'E', 11, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='CGV Sense City - Phong 1' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'E', 12, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='CGV Sense City - Phong 1' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'F', 1, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='CGV Sense City - Phong 1' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'F', 2, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='CGV Sense City - Phong 1' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'F', 3, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='CGV Sense City - Phong 1' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'F', 4, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='CGV Sense City - Phong 1' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'F', 5, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='CGV Sense City - Phong 1' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'F', 6, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='CGV Sense City - Phong 1' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'F', 7, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='CGV Sense City - Phong 1' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'F', 8, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='CGV Sense City - Phong 1' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'F', 9, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='CGV Sense City - Phong 1' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'F', 10, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='CGV Sense City - Phong 1' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'F', 11, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='CGV Sense City - Phong 1' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'F', 12, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='CGV Sense City - Phong 1' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'A', 1, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='CGV Sense City - Phong 2' AND lg.ten_loai_ghe='VIP';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'A', 2, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='CGV Sense City - Phong 2' AND lg.ten_loai_ghe='VIP';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'A', 3, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='CGV Sense City - Phong 2' AND lg.ten_loai_ghe='VIP';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'A', 4, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='CGV Sense City - Phong 2' AND lg.ten_loai_ghe='VIP';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'A', 5, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='CGV Sense City - Phong 2' AND lg.ten_loai_ghe='VIP';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'A', 6, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='CGV Sense City - Phong 2' AND lg.ten_loai_ghe='VIP';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'A', 7, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='CGV Sense City - Phong 2' AND lg.ten_loai_ghe='VIP';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'A', 8, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='CGV Sense City - Phong 2' AND lg.ten_loai_ghe='VIP';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'A', 9, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='CGV Sense City - Phong 2' AND lg.ten_loai_ghe='VIP';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'A', 10, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='CGV Sense City - Phong 2' AND lg.ten_loai_ghe='VIP';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'A', 11, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='CGV Sense City - Phong 2' AND lg.ten_loai_ghe='VIP';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'A', 12, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='CGV Sense City - Phong 2' AND lg.ten_loai_ghe='VIP';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'B', 1, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='CGV Sense City - Phong 2' AND lg.ten_loai_ghe='VIP';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'B', 2, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='CGV Sense City - Phong 2' AND lg.ten_loai_ghe='VIP';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'B', 3, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='CGV Sense City - Phong 2' AND lg.ten_loai_ghe='VIP';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'B', 4, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='CGV Sense City - Phong 2' AND lg.ten_loai_ghe='VIP';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'B', 5, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='CGV Sense City - Phong 2' AND lg.ten_loai_ghe='VIP';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'B', 6, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='CGV Sense City - Phong 2' AND lg.ten_loai_ghe='VIP';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'B', 7, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='CGV Sense City - Phong 2' AND lg.ten_loai_ghe='VIP';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'B', 8, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='CGV Sense City - Phong 2' AND lg.ten_loai_ghe='VIP';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'B', 9, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='CGV Sense City - Phong 2' AND lg.ten_loai_ghe='VIP';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'B', 10, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='CGV Sense City - Phong 2' AND lg.ten_loai_ghe='VIP';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'B', 11, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='CGV Sense City - Phong 2' AND lg.ten_loai_ghe='VIP';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'B', 12, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='CGV Sense City - Phong 2' AND lg.ten_loai_ghe='VIP';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'C', 1, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='CGV Sense City - Phong 2' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'C', 2, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='CGV Sense City - Phong 2' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'C', 3, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='CGV Sense City - Phong 2' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'C', 4, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='CGV Sense City - Phong 2' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'C', 5, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='CGV Sense City - Phong 2' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'C', 6, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='CGV Sense City - Phong 2' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'C', 7, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='CGV Sense City - Phong 2' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'C', 8, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='CGV Sense City - Phong 2' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'C', 9, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='CGV Sense City - Phong 2' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'C', 10, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='CGV Sense City - Phong 2' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'C', 11, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='CGV Sense City - Phong 2' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'C', 12, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='CGV Sense City - Phong 2' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'D', 1, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='CGV Sense City - Phong 2' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'D', 2, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='CGV Sense City - Phong 2' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'D', 3, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='CGV Sense City - Phong 2' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'D', 4, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='CGV Sense City - Phong 2' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'D', 5, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='CGV Sense City - Phong 2' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'D', 6, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='CGV Sense City - Phong 2' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'D', 7, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='CGV Sense City - Phong 2' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'D', 8, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='CGV Sense City - Phong 2' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'D', 9, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='CGV Sense City - Phong 2' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'D', 10, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='CGV Sense City - Phong 2' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'D', 11, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='CGV Sense City - Phong 2' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'D', 12, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='CGV Sense City - Phong 2' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'E', 1, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='CGV Sense City - Phong 2' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'E', 2, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='CGV Sense City - Phong 2' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'E', 3, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='CGV Sense City - Phong 2' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'E', 4, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='CGV Sense City - Phong 2' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'E', 5, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='CGV Sense City - Phong 2' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'E', 6, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='CGV Sense City - Phong 2' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'E', 7, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='CGV Sense City - Phong 2' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'E', 8, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='CGV Sense City - Phong 2' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'E', 9, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='CGV Sense City - Phong 2' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'E', 10, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='CGV Sense City - Phong 2' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'E', 11, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='CGV Sense City - Phong 2' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'E', 12, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='CGV Sense City - Phong 2' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'F', 1, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='CGV Sense City - Phong 2' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'F', 2, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='CGV Sense City - Phong 2' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'F', 3, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='CGV Sense City - Phong 2' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'F', 4, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='CGV Sense City - Phong 2' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'F', 5, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='CGV Sense City - Phong 2' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'F', 6, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='CGV Sense City - Phong 2' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'F', 7, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='CGV Sense City - Phong 2' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'F', 8, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='CGV Sense City - Phong 2' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'F', 9, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='CGV Sense City - Phong 2' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'F', 10, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='CGV Sense City - Phong 2' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'F', 11, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='CGV Sense City - Phong 2' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'F', 12, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='CGV Sense City - Phong 2' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'A', 1, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='Lotte Ninh Kieu - Phong 1' AND lg.ten_loai_ghe='VIP';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'A', 2, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='Lotte Ninh Kieu - Phong 1' AND lg.ten_loai_ghe='VIP';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'A', 3, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='Lotte Ninh Kieu - Phong 1' AND lg.ten_loai_ghe='VIP';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'A', 4, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='Lotte Ninh Kieu - Phong 1' AND lg.ten_loai_ghe='VIP';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'A', 5, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='Lotte Ninh Kieu - Phong 1' AND lg.ten_loai_ghe='VIP';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'A', 6, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='Lotte Ninh Kieu - Phong 1' AND lg.ten_loai_ghe='VIP';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'A', 7, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='Lotte Ninh Kieu - Phong 1' AND lg.ten_loai_ghe='VIP';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'A', 8, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='Lotte Ninh Kieu - Phong 1' AND lg.ten_loai_ghe='VIP';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'A', 9, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='Lotte Ninh Kieu - Phong 1' AND lg.ten_loai_ghe='VIP';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'A', 10, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='Lotte Ninh Kieu - Phong 1' AND lg.ten_loai_ghe='VIP';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'A', 11, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='Lotte Ninh Kieu - Phong 1' AND lg.ten_loai_ghe='VIP';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'A', 12, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='Lotte Ninh Kieu - Phong 1' AND lg.ten_loai_ghe='VIP';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'B', 1, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='Lotte Ninh Kieu - Phong 1' AND lg.ten_loai_ghe='VIP';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'B', 2, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='Lotte Ninh Kieu - Phong 1' AND lg.ten_loai_ghe='VIP';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'B', 3, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='Lotte Ninh Kieu - Phong 1' AND lg.ten_loai_ghe='VIP';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'B', 4, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='Lotte Ninh Kieu - Phong 1' AND lg.ten_loai_ghe='VIP';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'B', 5, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='Lotte Ninh Kieu - Phong 1' AND lg.ten_loai_ghe='VIP';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'B', 6, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='Lotte Ninh Kieu - Phong 1' AND lg.ten_loai_ghe='VIP';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'B', 7, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='Lotte Ninh Kieu - Phong 1' AND lg.ten_loai_ghe='VIP';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'B', 8, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='Lotte Ninh Kieu - Phong 1' AND lg.ten_loai_ghe='VIP';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'B', 9, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='Lotte Ninh Kieu - Phong 1' AND lg.ten_loai_ghe='VIP';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'B', 10, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='Lotte Ninh Kieu - Phong 1' AND lg.ten_loai_ghe='VIP';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'B', 11, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='Lotte Ninh Kieu - Phong 1' AND lg.ten_loai_ghe='VIP';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'B', 12, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='Lotte Ninh Kieu - Phong 1' AND lg.ten_loai_ghe='VIP';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'C', 1, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='Lotte Ninh Kieu - Phong 1' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'C', 2, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='Lotte Ninh Kieu - Phong 1' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'C', 3, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='Lotte Ninh Kieu - Phong 1' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'C', 4, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='Lotte Ninh Kieu - Phong 1' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'C', 5, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='Lotte Ninh Kieu - Phong 1' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'C', 6, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='Lotte Ninh Kieu - Phong 1' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'C', 7, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='Lotte Ninh Kieu - Phong 1' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'C', 8, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='Lotte Ninh Kieu - Phong 1' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'C', 9, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='Lotte Ninh Kieu - Phong 1' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'C', 10, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='Lotte Ninh Kieu - Phong 1' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'C', 11, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='Lotte Ninh Kieu - Phong 1' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'C', 12, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='Lotte Ninh Kieu - Phong 1' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'D', 1, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='Lotte Ninh Kieu - Phong 1' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'D', 2, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='Lotte Ninh Kieu - Phong 1' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'D', 3, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='Lotte Ninh Kieu - Phong 1' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'D', 4, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='Lotte Ninh Kieu - Phong 1' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'D', 5, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='Lotte Ninh Kieu - Phong 1' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'D', 6, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='Lotte Ninh Kieu - Phong 1' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'D', 7, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='Lotte Ninh Kieu - Phong 1' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'D', 8, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='Lotte Ninh Kieu - Phong 1' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'D', 9, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='Lotte Ninh Kieu - Phong 1' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'D', 10, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='Lotte Ninh Kieu - Phong 1' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'D', 11, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='Lotte Ninh Kieu - Phong 1' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'D', 12, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='Lotte Ninh Kieu - Phong 1' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'E', 1, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='Lotte Ninh Kieu - Phong 1' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'E', 2, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='Lotte Ninh Kieu - Phong 1' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'E', 3, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='Lotte Ninh Kieu - Phong 1' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'E', 4, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='Lotte Ninh Kieu - Phong 1' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'E', 5, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='Lotte Ninh Kieu - Phong 1' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'E', 6, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='Lotte Ninh Kieu - Phong 1' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'E', 7, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='Lotte Ninh Kieu - Phong 1' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'E', 8, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='Lotte Ninh Kieu - Phong 1' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'E', 9, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='Lotte Ninh Kieu - Phong 1' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'E', 10, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='Lotte Ninh Kieu - Phong 1' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'E', 11, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='Lotte Ninh Kieu - Phong 1' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'E', 12, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='Lotte Ninh Kieu - Phong 1' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'F', 1, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='Lotte Ninh Kieu - Phong 1' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'F', 2, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='Lotte Ninh Kieu - Phong 1' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'F', 3, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='Lotte Ninh Kieu - Phong 1' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'F', 4, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='Lotte Ninh Kieu - Phong 1' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'F', 5, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='Lotte Ninh Kieu - Phong 1' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'F', 6, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='Lotte Ninh Kieu - Phong 1' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'F', 7, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='Lotte Ninh Kieu - Phong 1' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'F', 8, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='Lotte Ninh Kieu - Phong 1' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'F', 9, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='Lotte Ninh Kieu - Phong 1' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'F', 10, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='Lotte Ninh Kieu - Phong 1' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'F', 11, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='Lotte Ninh Kieu - Phong 1' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'F', 12, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='Lotte Ninh Kieu - Phong 1' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'A', 1, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='Lotte Ninh Kieu - Phong 2' AND lg.ten_loai_ghe='VIP';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'A', 2, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='Lotte Ninh Kieu - Phong 2' AND lg.ten_loai_ghe='VIP';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'A', 3, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='Lotte Ninh Kieu - Phong 2' AND lg.ten_loai_ghe='VIP';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'A', 4, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='Lotte Ninh Kieu - Phong 2' AND lg.ten_loai_ghe='VIP';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'A', 5, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='Lotte Ninh Kieu - Phong 2' AND lg.ten_loai_ghe='VIP';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'A', 6, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='Lotte Ninh Kieu - Phong 2' AND lg.ten_loai_ghe='VIP';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'A', 7, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='Lotte Ninh Kieu - Phong 2' AND lg.ten_loai_ghe='VIP';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'A', 8, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='Lotte Ninh Kieu - Phong 2' AND lg.ten_loai_ghe='VIP';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'A', 9, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='Lotte Ninh Kieu - Phong 2' AND lg.ten_loai_ghe='VIP';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'A', 10, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='Lotte Ninh Kieu - Phong 2' AND lg.ten_loai_ghe='VIP';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'A', 11, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='Lotte Ninh Kieu - Phong 2' AND lg.ten_loai_ghe='VIP';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'A', 12, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='Lotte Ninh Kieu - Phong 2' AND lg.ten_loai_ghe='VIP';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'B', 1, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='Lotte Ninh Kieu - Phong 2' AND lg.ten_loai_ghe='VIP';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'B', 2, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='Lotte Ninh Kieu - Phong 2' AND lg.ten_loai_ghe='VIP';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'B', 3, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='Lotte Ninh Kieu - Phong 2' AND lg.ten_loai_ghe='VIP';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'B', 4, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='Lotte Ninh Kieu - Phong 2' AND lg.ten_loai_ghe='VIP';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'B', 5, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='Lotte Ninh Kieu - Phong 2' AND lg.ten_loai_ghe='VIP';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'B', 6, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='Lotte Ninh Kieu - Phong 2' AND lg.ten_loai_ghe='VIP';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'B', 7, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='Lotte Ninh Kieu - Phong 2' AND lg.ten_loai_ghe='VIP';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'B', 8, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='Lotte Ninh Kieu - Phong 2' AND lg.ten_loai_ghe='VIP';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'B', 9, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='Lotte Ninh Kieu - Phong 2' AND lg.ten_loai_ghe='VIP';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'B', 10, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='Lotte Ninh Kieu - Phong 2' AND lg.ten_loai_ghe='VIP';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'B', 11, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='Lotte Ninh Kieu - Phong 2' AND lg.ten_loai_ghe='VIP';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'B', 12, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='Lotte Ninh Kieu - Phong 2' AND lg.ten_loai_ghe='VIP';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'C', 1, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='Lotte Ninh Kieu - Phong 2' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'C', 2, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='Lotte Ninh Kieu - Phong 2' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'C', 3, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='Lotte Ninh Kieu - Phong 2' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'C', 4, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='Lotte Ninh Kieu - Phong 2' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'C', 5, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='Lotte Ninh Kieu - Phong 2' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'C', 6, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='Lotte Ninh Kieu - Phong 2' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'C', 7, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='Lotte Ninh Kieu - Phong 2' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'C', 8, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='Lotte Ninh Kieu - Phong 2' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'C', 9, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='Lotte Ninh Kieu - Phong 2' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'C', 10, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='Lotte Ninh Kieu - Phong 2' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'C', 11, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='Lotte Ninh Kieu - Phong 2' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'C', 12, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='Lotte Ninh Kieu - Phong 2' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'D', 1, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='Lotte Ninh Kieu - Phong 2' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'D', 2, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='Lotte Ninh Kieu - Phong 2' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'D', 3, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='Lotte Ninh Kieu - Phong 2' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'D', 4, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='Lotte Ninh Kieu - Phong 2' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'D', 5, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='Lotte Ninh Kieu - Phong 2' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'D', 6, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='Lotte Ninh Kieu - Phong 2' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'D', 7, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='Lotte Ninh Kieu - Phong 2' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'D', 8, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='Lotte Ninh Kieu - Phong 2' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'D', 9, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='Lotte Ninh Kieu - Phong 2' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'D', 10, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='Lotte Ninh Kieu - Phong 2' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'D', 11, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='Lotte Ninh Kieu - Phong 2' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'D', 12, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='Lotte Ninh Kieu - Phong 2' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'E', 1, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='Lotte Ninh Kieu - Phong 2' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'E', 2, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='Lotte Ninh Kieu - Phong 2' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'E', 3, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='Lotte Ninh Kieu - Phong 2' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'E', 4, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='Lotte Ninh Kieu - Phong 2' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'E', 5, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='Lotte Ninh Kieu - Phong 2' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'E', 6, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='Lotte Ninh Kieu - Phong 2' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'E', 7, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='Lotte Ninh Kieu - Phong 2' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'E', 8, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='Lotte Ninh Kieu - Phong 2' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'E', 9, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='Lotte Ninh Kieu - Phong 2' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'E', 10, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='Lotte Ninh Kieu - Phong 2' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'E', 11, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='Lotte Ninh Kieu - Phong 2' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'E', 12, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='Lotte Ninh Kieu - Phong 2' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'F', 1, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='Lotte Ninh Kieu - Phong 2' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'F', 2, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='Lotte Ninh Kieu - Phong 2' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'F', 3, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='Lotte Ninh Kieu - Phong 2' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'F', 4, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='Lotte Ninh Kieu - Phong 2' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'F', 5, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='Lotte Ninh Kieu - Phong 2' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'F', 6, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='Lotte Ninh Kieu - Phong 2' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'F', 7, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='Lotte Ninh Kieu - Phong 2' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'F', 8, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='Lotte Ninh Kieu - Phong 2' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'F', 9, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='Lotte Ninh Kieu - Phong 2' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'F', 10, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='Lotte Ninh Kieu - Phong 2' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'F', 11, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='Lotte Ninh Kieu - Phong 2' AND lg.ten_loai_ghe='Thuong';
INSERT IGNORE INTO ghe(ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT ph.ma_phong, 'F', 12, lg.ma_loai_ghe
FROM phong ph, loai_ghe lg
WHERE ph.ten_phong='Lotte Ninh Kieu - Phong 2' AND lg.ten_loai_ghe='Thuong';

INSERT INTO suat_chieu(ma_phim, ma_phong, ma_dinh_dang, bat_dau_luc, ket_thuc_luc, gia_co_ban, trang_thai)
SELECT p.ma_phim, ph.ma_phong, dd.ma_dinh_dang, '2025-10-31 10:00:00', NULL, 85000, 'MO_BAN'
FROM phim p, phong ph, dinh_dang dd
WHERE p.ten_phim='Cuc Vang Cua Ngoai' AND ph.ten_phong='CGV Sense City - Phong 1' AND dd.ten_dinh_dang='2D'
ON DUPLICATE KEY UPDATE gia_co_ban=VALUES(gia_co_ban);


INSERT INTO suat_chieu(ma_phim, ma_phong, ma_dinh_dang, bat_dau_luc, ket_thuc_luc, gia_co_ban, trang_thai)
SELECT p.ma_phim, ph.ma_phong, dd.ma_dinh_dang, '2025-10-31 20:00:00', NULL, 90000, 'MO_BAN'
FROM phim p, phong ph, dinh_dang dd
WHERE p.ten_phim='Nha Ma Xo' AND ph.ten_phong='CGV Sense City - Phong 2' AND dd.ten_dinh_dang='2D'
ON DUPLICATE KEY UPDATE gia_co_ban=VALUES(gia_co_ban);


INSERT INTO suat_chieu(ma_phim, ma_phong, ma_dinh_dang, bat_dau_luc, ket_thuc_luc, gia_co_ban, trang_thai)
SELECT p.ma_phim, ph.ma_phong, dd.ma_dinh_dang, '2025-10-31 19:00:00', NULL, 95000, 'MO_BAN'
FROM phim p, phong ph, dinh_dang dd
WHERE p.ten_phim='TEE YOD 3: Quy An Tang' AND ph.ten_phong='Lotte Ninh Kieu - Phong 1' AND dd.ten_dinh_dang='2D'
ON DUPLICATE KEY UPDATE gia_co_ban=VALUES(gia_co_ban);


INSERT INTO suat_chieu(ma_phim, ma_phong, ma_dinh_dang, bat_dau_luc, ket_thuc_luc, gia_co_ban, trang_thai)
SELECT p.ma_phim, ph.ma_phong, dd.ma_dinh_dang, '2025-10-31 17:00:00', NULL, 110000, 'MO_BAN'
FROM phim p, phong ph, dinh_dang dd
WHERE p.ten_phim='Tu Chien Tren Khong' AND ph.ten_phong='Lotte Ninh Kieu - Phong 2' AND dd.ten_dinh_dang='3D'
ON DUPLICATE KEY UPDATE gia_co_ban=VALUES(gia_co_ban);


INSERT INTO suat_chieu(ma_phim, ma_phong, ma_dinh_dang, bat_dau_luc, ket_thuc_luc, gia_co_ban, trang_thai)
SELECT p.ma_phim, ph.ma_phong, dd.ma_dinh_dang, '2025-11-01 18:30:00', NULL, 90000, 'MO_BAN'
FROM phim p, phong ph, dinh_dang dd
WHERE p.ten_phim='Mua Do' AND ph.ten_phong='CGV Sense City - Phong 1' AND dd.ten_dinh_dang='2D'
ON DUPLICATE KEY UPDATE gia_co_ban=VALUES(gia_co_ban);


INSERT INTO suat_chieu(ma_phim, ma_phong, ma_dinh_dang, bat_dau_luc, ket_thuc_luc, gia_co_ban, trang_thai)
SELECT p.ma_phim, ph.ma_phong, dd.ma_dinh_dang, '2025-11-01 14:30:00', NULL, 115000, 'MO_BAN'
FROM phim p, phong ph, dinh_dang dd
WHERE p.ten_phim='Tu Chien Tren Khong' AND ph.ten_phong='CGV Sense City - Phong 2' AND dd.ten_dinh_dang='3D'
ON DUPLICATE KEY UPDATE gia_co_ban=VALUES(gia_co_ban);


INSERT INTO suat_chieu(ma_phim, ma_phong, ma_dinh_dang, bat_dau_luc, ket_thuc_luc, gia_co_ban, trang_thai)
SELECT p.ma_phim, ph.ma_phong, dd.ma_dinh_dang, '2025-11-01 21:00:00', NULL, 90000, 'MO_BAN'
FROM phim p, phong ph, dinh_dang dd
WHERE p.ten_phim='Nha Ma Xo' AND ph.ten_phong='Lotte Ninh Kieu - Phong 1' AND dd.ten_dinh_dang='2D'
ON DUPLICATE KEY UPDATE gia_co_ban=VALUES(gia_co_ban);


INSERT INTO suat_chieu(ma_phim, ma_phong, ma_dinh_dang, bat_dau_luc, ket_thuc_luc, gia_co_ban, trang_thai)
SELECT p.ma_phim, ph.ma_phong, dd.ma_dinh_dang, '2025-11-01 09:30:00', NULL, 80000, 'MO_BAN'
FROM phim p, phong ph, dinh_dang dd
WHERE p.ten_phim='Cuc Vang Cua Ngoai' AND ph.ten_phong='Lotte Ninh Kieu - Phong 2' AND dd.ten_dinh_dang='2D'
ON DUPLICATE KEY UPDATE gia_co_ban=VALUES(gia_co_ban);


INSERT INTO san_pham(ten_san_pham, loai, gia, hoat_dong) VALUES
  ('Bap rang 60oz', 'POPCORN', 45000, 1),
  ('Nuoc ngot 22oz', 'NUOC',    35000, 1),
  ('Combo Bap + Nuoc', 'KHAC',   0, 1)
ON DUPLICATE KEY UPDATE gia=VALUES(gia);


INSERT INTO combo(ten_combo, gia, hoat_dong) VALUES
  ('Combo 1 - Bap + Nuoc', 75000, 1),
  ('Combo 2 - 2 Nuoc + Bap', 105000, 1)
ON DUPLICATE KEY UPDATE gia=VALUES(gia);


-- Link combo -> san pham
INSERT IGNORE INTO combo_chi_tiet(ma_combo, ma_san_pham, so_luong)
SELECT c.ma_combo, sp.ma_san_pham, 1
FROM combo c, san_pham sp
WHERE c.ten_combo='Combo 1 - Bap + Nuoc' AND sp.ten_san_pham IN ('Bap rang 60oz','Nuoc ngot 22oz');

INSERT IGNORE INTO combo_chi_tiet(ma_combo, ma_san_pham, so_luong)
SELECT c.ma_combo, sp.ma_san_pham, CASE WHEN sp.ten_san_pham='Nuoc ngot 22oz' THEN 2 ELSE 1 END
FROM combo c, san_pham sp
WHERE c.ten_combo='Combo 2 - 2 Nuoc + Bap' AND sp.ten_san_pham IN ('Bap rang 60oz','Nuoc ngot 22oz');

SET FOREIGN_KEY_CHECKS=1;

-- 🎬 Cập nhật poster_url cho từng phim
UPDATE phim SET poster_url = 'https://vn.images.search.yahoo.com/images/view;_ylt=Awr1QNIB0QVpzlgTAb1tUwx.;_ylu=c2VjA3NyBHNsawNpbWcEb2lkAzU3OTMwOWJhZjlkNDFlNGEyMGUyZGI0Y2U0NjU0NmM2BGdwb3MDMgRpdANiaW5n?back=https%3A%2F%2Fvn.images.search.yahoo.com%2Fsearch%2Fimages%3Fp%3Dposter%2Bphim%2Bcu%2Bvang%2Bcua%2Bngoai%26type%3DE210VN885G0-E210VN885G0%26fr%3Dmcafee%26fr2%3Dpiv-web%26tab%3Dorganic%26ri%3D2&w=500&h=750&imgurl=metiz.vn%2Fmedia%2Fposter_film%2Fcuc-vang-cua-ngoai.jpg&rurl=https%3A%2F%2Fmetiz.vn%2F&size=435KB&p=poster+phim+cu+vang+cua+ngoai&oid=579309baf9d41e4a20e2db4ce46546c6&fr2=piv-web&fr=mcafee&tt=Metiz+Cinema&b=0&ni=21&no=2&ts=&tab=organic&sigr=JyKLqEqgbHXv&sigb=j9U1XHtWfYmX&sigi=bFKzxKScDbS3&sigt=1zEpgf82mCqB&.crumb=DCDVWauIfN1&fr=mcafee&fr2=piv-web&type=E210VN885G0-E210VN885G0'
WHERE ten_phim = 'Cuc Vang Cua Ngoai';

UPDATE phim SET poster_url = 'https://vn.images.search.yahoo.com/images/view;_ylt=Awr1QNKb0QVpLZ8T2FhtUwx.;_ylu=c2VjA3NyBHNsawNpbWcEb2lkAzU1MDE1NDMyOWYwZmI2YTFmZjI1M2VhZjQ0Zjc2MDg3BGdwb3MDNQRpdANiaW5n?back=https%3A%2F%2Fvn.images.search.yahoo.com%2Fsearch%2Fimages%3Fp%3Dposter%2Bphim%2Bnha%2Bma%2Bxo%26ei%3DUTF-8%26type%3DE210VN885G0-E210VN885G0%26fr%3Dmcafee%26fr2%3Dp%253As%252Cv%253Ai%252Cm%253Asb-top%26tab%3Dorganic%26ri%3D5&w=1150&h=1440&imgurl=lookaside.fbsbx.com%2Flookaside%2Fcrawler%2Fmedia%2F%3Fmedia_id%3D1111447081169562&rurl=https%3A%2F%2Fwww.facebook.com%2Fmegagsdistribution.vn%2F&size=172KB&p=poster+phim+nha+ma+xo&oid=550154329f0fb6a1ff253eaf44f76087&fr2=p%3As%2Cv%3Ai%2Cm%3Asb-top&fr=mcafee&tt=Mega+GS+Movies&b=0&ni=160&no=5&ts=&tab=organic&sigr=VvOQnHf8jv7C&sigb=4NBKrLaKMg2x&sigi=oz5dkWfrziH8&sigt=XzXiVhjNi704&.crumb=DCDVWauIfN1&fr=mcafee&fr2=p%3As%2Cv%3Ai%2Cm%3Asb-top&type=E210VN885G0-E210VN885G0'
WHERE ten_phim = 'Nha Ma Xo';

UPDATE phim SET poster_url = 'https://vn.images.search.yahoo.com/images/view;_ylt=AwrPplDt0QVpVEQaF4RtUwx.;_ylu=c2VjA3NyBHNsawNpbWcEb2lkA2Y3NTM4NGQxZDFmNmYwODdkZjFlYzc2YjRiNTU1MThiBGdwb3MDMQRpdANiaW5n?back=https%3A%2F%2Fvn.images.search.yahoo.com%2Fsearch%2Fimages%3Fp%3Dposter%2Bphim%2Bquy%2Ban%2Btang%2B3%26type%3DE210VN885G0-E210VN885G0-E210VN885G0-E210VN885G0%26fr%3Dmcafee%26fr2%3Dpiv-web%26tab%3Dorganic%26ri%3D1&w=1280&h=1117&imgurl=daknong.1cdn.vn%2F2024%2F10%2F16%2Fimages2.thanhnien.vn-528068263637045248-2024-10-13-_h2-17288413805091450266602.jpeg&rurl=https%3A%2F%2Fbaolamdong.vn%2Freview-phim-tee-yod-quy-an-tang-2-tiep-noi-sieu-pham-phan-1-231796.html&size=134KB&p=poster+phim+quy+an+tang+3&oid=f75384d1d1f6f087df1ec76b4b55518b&fr2=piv-web&fr=mcafee&tt=Review+phim+Tee+Yod+Qu%E1%BB%B7+%C4%82n+T%E1%BA%A1ng+2%3A+Ti%E1%BA%BFp+n%E1%BB%91i+si%C3%AAu+ph%E1%BA%A9m+ph%E1%BA%A7n+1&b=0&ni=21&no=1&ts=&tab=organic&sigr=UQTHEe3OLyn9&sigb=egQyKVv_.awr&sigi=bRHrfYXmNiP0&sigt=rDRL_WbkicQo&.crumb=DCDVWauIfN1&fr=mcafee&fr2=piv-web&type=E210VN885G0-E210VN885G0-E210VN885G0-E210VN885G0'
WHERE ten_phim = 'TEE YOD 3: Quy An Tang';

UPDATE phim SET poster_url = 'https://vn.images.search.yahoo.com/images/view;_ylt=AwrPplAZ0gVpefUaZDxtUwx.;_ylu=c2VjA3NyBHNsawNpbWcEb2lkA2Q3NDJjMDE4NzBmYWM5NjVmOWY5ODc1MDU0MDM3MGMxBGdwb3MDMgRpdANiaW5n?back=https%3A%2F%2Fvn.images.search.yahoo.com%2Fsearch%2Fimages%3Fp%3Dposter%2Bphim%2Btu%2Bchien%2Btren%2Bkhong%26ei%3DUTF-8%26type%3DE210VN885G0-E210VN885G0-E210VN885G0-E210VN885G0%26fr%3Dmcafee%26fr2%3Dp%253As%252Cv%253Ai%252Cm%253Asb-top%26tab%3Dorganic%26ri%3D2&w=500&h=624&imgurl=st.download.com.vn%2Fdata%2Fimage%2F2025%2F09%2F15%2Ftu-chien-tren-khong-1.jpg&rurl=https%3A%2F%2Fdownload.com.vn%2Ftu-chien-tren-khong-197082&size=65KB&p=poster+phim+tu+chien+tren+khong&oid=d742c01870fac965f9f98750540370c1&fr2=p%3As%2Cv%3Ai%2Cm%3Asb-top&fr=mcafee&tt=T%E1%BB%AD+chi%E1%BA%BFn+tr%C3%AAn+kh%C3%B4ng+-+Phim+h%C3%A0nh+%C4%91%E1%BB%99ng+%C4%91i%E1%BB%87n+%E1%BA%A3nh+Vi%E1%BB%87t+Nam+%C4%91%E1%BB%81+t%C3%A0i+kh%C3%B4ng+t%E1%BA%B7c&b=0&ni=160&no=2&ts=&tab=organic&sigr=c9jQ7lCglhiU&sigb=L5wcVIOHnFKy&sigi=Ss6IuGvzsUGR&sigt=aSzomJ0GcDca&.crumb=DCDVWauIfN1&fr=mcafee&fr2=p%3As%2Cv%3Ai%2Cm%3Asb-top&type=E210VN885G0-E210VN885G0-E210VN885G0-E210VN885G0'
WHERE ten_phim = 'Tu Chien Tren Khong';

UPDATE phim SET poster_url = 'https://upload.wikimedia.org/wikipedia/vi/8/84/Mua_Do_poster.jpg'
WHERE ten_phim = 'Mua Do';

