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
UPDATE phim SET poster_url = 'https://metiz.vn/media/poster_film/cuc-vang-cua-ngoai.jpg'
WHERE ten_phim = 'Cuc Vang Cua Ngoai';

UPDATE phim SET poster_url = 'https://scontent.fvca1-3.fna.fbcdn.net/v/t39.30808-6/555674947_1111436614503942_5215464436592486745_n.jpg?_nc_cat=103&ccb=1-7&_nc_sid=833d8c&_nc_ohc=C5y_HUqVSRgQ7kNvwHLBnXy&_nc_oc=AdkTbduFYiPbGq3lOyfFLe0TSOV0xUcq9kcyoz-eG-51pP_vkpjZvVRLno0IZkdUlNo&_nc_zt=23&_nc_ht=scontent.fvca1-3.fna&_nc_gid=Jp0qNvw_3fKnUJH7hFkOxg&oh=00_AfeOUbpSgGiT_wI1hyxRMCYivWm1DRTN9nYuD-kJK-y4eQ&oe=690BB4B7'
WHERE ten_phim = 'Nha Ma Xo';

UPDATE phim SET poster_url = 'https://i.ytimg.com/vi/9jyPXIf4wVk/maxresdefault.jpg'
WHERE ten_phim = 'TEE YOD 3: Quy An Tang';

UPDATE phim SET poster_url = 'https://st.download.com.vn/data/image/2025/09/15/tu-chien-tren-khong-1.jpg'
WHERE ten_phim = 'Tu Chien Tren Khong';

UPDATE phim SET poster_url = 'https://i.vgt.vn/2025/9/9/chi-tiet-trong-nguyen-tac-mua-do-bi-thay-doi-khi-len-phim-khan-gia-tiec-nuoi-982-7528970.webp'
WHERE ten_phim = 'Mua Do';

ALTER TABLE phim MODIFY COLUMN poster_url VARCHAR(1000);

USE qlrapchieuphim;
SET NAMES utf8mb4;

-- 1) Bảo đảm có định dạng/phòng/phim
INSERT INTO dinh_dang (ten_dinh_dang) VALUES ('2D')
ON DUPLICATE KEY UPDATE ten_dinh_dang=VALUES(ten_dinh_dang);

INSERT INTO phong (ten_phong, suc_chua, trang_thai)
VALUES ('Phòng 1', 60, 'HOAT_DONG')
ON DUPLICATE KEY UPDATE suc_chua=VALUES(suc_chua), trang_thai=VALUES(trang_thai);

INSERT INTO loai_ghe (ten_loai_ghe, he_so_gia)
VALUES ('STANDARD',1.00),('VIP',1.30)
ON DUPLICATE KEY UPDATE he_so_gia=VALUES(he_so_gia);

SET @id_phong := (SELECT ma_phong FROM phong WHERE ten_phong='Phòng 1' LIMIT 1);
SET @id_dd    := (SELECT ma_dinh_dang FROM dinh_dang WHERE ten_dinh_dang='2D' LIMIT 1);

-- Ghế A/B (STANDARD), C (VIP) nếu chưa có
INSERT INTO ghe (ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT @id_phong,'A',n, (SELECT ma_loai_ghe FROM loai_ghe WHERE ten_loai_ghe='STANDARD')
FROM (SELECT 1 n UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5
      UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9 UNION SELECT 10) x
ON DUPLICATE KEY UPDATE ma_phong=ma_phong;

INSERT INTO ghe (ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT @id_phong,'B',n, (SELECT ma_loai_ghe FROM loai_ghe WHERE ten_loai_ghe='STANDARD') FROM x
ON DUPLICATE KEY UPDATE ma_phong=ma_phong;

INSERT INTO ghe (ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT @id_phong,'C',n, (SELECT ma_loai_ghe FROM loai_ghe WHERE ten_loai_ghe='VIP') FROM x
ON DUPLICATE KEY UPDATE ma_phong=ma_phong;

INSERT INTO phim (ten_phim, thoi_luong_phut)
VALUES ('Inside Out 2',96)
ON DUPLICATE KEY UPDATE thoi_luong_phut=VALUES(thoi_luong_phut);

SET @id_phim := (SELECT ma_phim FROM phim WHERE ten_phim='Inside Out 2' LIMIT 1);

-- 2) Tạo 2 suất chiếu HÔM NAY
INSERT INTO suat_chieu (ma_phim, ma_phong, ma_dinh_dang, bat_dau_luc, gia_co_ban, trang_thai)
VALUES 
(@id_phim, @id_phong, @id_dd, CONCAT(CURDATE(),' 13:30:00'), 70000,'MO_BAN'),
(@id_phim, @id_phong, @id_dd, CONCAT(CURDATE(),' 19:00:00'), 70000,'MO_BAN')
ON DUPLICATE KEY UPDATE gia_co_ban=VALUES(gia_co_ban), trang_thai=VALUES(trang_thai);

-- 3) Sinh vé (nếu chưa có trigger tự sinh)
INSERT INTO ve (ma_suat_chieu, ma_ghe, gia_ban, trang_thai)
SELECT sc.ma_suat_chieu, g.ma_ghe, ROUND(sc.gia_co_ban * lg.he_so_gia,0), 'SAN_SANG'
FROM suat_chieu sc
JOIN ghe g  ON g.ma_phong = sc.ma_phong
JOIN loai_ghe lg ON lg.ma_loai_ghe = g.ma_loai_ghe
LEFT JOIN ve v ON v.ma_suat_chieu = sc.ma_suat_chieu AND v.ma_ghe = g.ma_ghe
WHERE DATE(sc.bat_dau_luc) = CURDATE() AND v.ma_ve IS NULL;

