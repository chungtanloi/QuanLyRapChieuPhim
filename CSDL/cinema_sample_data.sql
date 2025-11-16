-- ================================
-- DỮ LIỆU MẪU HỆ THỐNG QUẢN LÝ RẠP
-- PHÙ HỢP TRIGGER & CÁC PROC HIỆN TẠI
-- ================================
USE qlrapchieuphim;
SET FOREIGN_KEY_CHECKS = 0;

-- Nếu muốn reset sạch data thì bỏ comment các dòng TRUNCATE này
-- TRUNCATE TABLE don_khuyen_mai;
-- TRUNCATE TABLE thanh_toan;
-- TRUNCATE TABLE hang_hoa;
-- TRUNCATE TABLE don_ve;
-- TRUNCATE TABLE don_hang;
-- TRUNCATE TABLE ve;
-- TRUNCATE TABLE suat_chieu;
-- TRUNCATE TABLE ghe;
-- TRUNCATE TABLE loai_ghe;
-- TRUNCATE TABLE phong;
-- TRUNCATE TABLE dinh_dang;
-- TRUNCATE TABLE phim_the_loai;
-- TRUNCATE TABLE phim;
-- TRUNCATE TABLE the_loai;
-- TRUNCATE TABLE khuyen_mai;
-- TRUNCATE TABLE combo_chi_tiet;
-- TRUNCATE TABLE combo;
-- TRUNCATE TABLE san_pham;
-- TRUNCATE TABLE nhan_vien;
-- TRUNCATE TABLE khach_hang;
-- TRUNCATE TABLE tai_khoan;

-- 1) TAI_KHOAN – Users
-- Dùng INSERT IGNORE để không vỡ khi email đã tồn tại
INSERT IGNORE INTO tai_khoan (email, mat_khau_ma, ho_ten, so_dien_thoai, vai_tro, hoat_dong) VALUES
('admin@cinema.vn',   '$2y$10$abcdefghijklmnopqrstuv',   'Nguyen Van Admin',      '0901234567', 'QUAN_TRI',   1),
('manager@cinema.vn', '$2y$10$bcdefghijklmnopqrstuvw',   'Tran Thi Quan Ly',      '0902345678', 'NHAN_VIEN',  1),
('staff01@cinema.vn', '$2y$10$cdefghijklmnopqrstuvwx',   'Le Van Nhan Vien',      '0903456789', 'NHAN_VIEN',  1),
('staff02@cinema.vn', '$2y$10$defghijklmnopqrstuvwxy',   'Pham Thi Thu Ngan',     '0904567890', 'NHAN_VIEN',  1),
('customer01@gmail.com', '$2y$10$efghijklmnopqrstuvwxyz',   'Hoang Van Khach',    '0905678901', 'KHACH_HANG', 1),
('customer02@gmail.com', '$2y$10$fghijklmnopqrstuvwxyza',   'Nguyen Thi Lan',    '0906789012', 'KHACH_HANG', 1),
('customer03@gmail.com', '$2y$10$ghijklmnopqrstuvwxyzab',   'Tran Van Minh',     '0907890123', 'KHACH_HANG', 1),
('customer04@gmail.com', '$2y$10$hijklmnopqrstuvwxyzabc',   'Le Thi Huong',      '0908901234', 'KHACH_HANG', 1),
('customer05@gmail.com', '$2y$10$ijklmnopqrstuvwxyzabcd',   'Pham Van Thanh',    '0909012345', 'KHACH_HANG', 1),
('customer06@gmail.com', '$2y$10$jklmnopqrstuvwxyzabcde',   'Vo Thi Mai',        '0910123456', 'KHACH_HANG', 1);

-- Giả định: AUTO_INCREMENT của tai_khoan bắt đầu từ 1
-- => admin=1, manager=2, staff01=3, staff02=4, KH=5..10

-- 2) NHAN_VIEN – Employees
-- gắn theo ma_tai_khoan 2,3,4 như trên
INSERT IGNORE INTO nhan_vien (ma_tai_khoan, chuc_vu, ngay_vao_lam) VALUES
(2, 'Quan Ly Rap',       '2020-01-15'),
(3, 'Nhan Vien Ban Ve',  '2021-06-01'),
(4, 'Nhan Vien Quay Bar','2022-03-10');
-- Lúc này ma_nhan_vien sẽ lần lượt 1,2,3

-- 3) KHACH_HANG – Customers
-- Có thể đã tự sinh bởi trigger trg_after_taikhoan_insert.
-- Dùng INSERT IGNORE + UPDATE để an toàn cả khi có/không có trigger.

INSERT IGNORE INTO khach_hang (ma_tai_khoan) VALUES (5),(6),(7),(8),(9),(10);

UPDATE khach_hang
   SET diem_tich_luy = 1200, ngay_sinh = '1990-05-15'
 WHERE ma_tai_khoan = 5;

UPDATE khach_hang
   SET diem_tich_luy = 850,  ngay_sinh = '1995-08-20'
 WHERE ma_tai_khoan = 6;

UPDATE khach_hang
   SET diem_tich_luy = 2500, ngay_sinh = '1988-12-03'
 WHERE ma_tai_khoan = 7;

UPDATE khach_hang
   SET diem_tich_luy = 450,  ngay_sinh = '2000-03-25'
 WHERE ma_tai_khoan = 8;

UPDATE khach_hang
   SET diem_tich_luy = 1800, ngay_sinh = '1992-07-18'
 WHERE ma_tai_khoan = 9;

UPDATE khach_hang
   SET diem_tich_luy = 600,  ngay_sinh = '1998-11-30'
 WHERE ma_tai_khoan = 10;

-- Giả định: ma_khach_hang ứng với ma_tai_khoan 5..10 lần lượt là 1..6

-- 4) THE_LOAI – Genres
INSERT IGNORE INTO the_loai (ten_the_loai) VALUES
('Hanh Dong'), ('Phieu Luu'), ('Hai Huoc'), ('Kinh Di'),
('Tam Ly'), ('Tinh Cam'), ('Khoa Hoc Vien Tuong'), ('Hoat Hinh'),
('Gia Dinh'), ('Bi An'), ('Chien Tranh'), ('Lich Su');

-- 5) PHIM – Movies
INSERT IGNORE INTO phim (ten_phim, thoi_luong_phut, phan_loai, ngay_phat_hanh, mo_ta, poster_url) VALUES
('Avengers: Endgame', 181, 'T13', '2024-04-26', 'Cau chuyen hoanh trang ve cac sieu anh hung Marvel', 'https://example.com/avengers.jpg'),
('Mai', 130, 'T16', '2024-02-10', 'Phim tinh cam Viet Nam cua dao dien Tran Thanh', 'https://example.com/mai.jpg'),
('Kungfu Panda 4', 94, 'P', '2024-03-08', 'Phan tiep theo cua chu gau truc Po', 'https://example.com/kungfu.jpg'),
('Dune: Part Two', 166, 'T13', '2024-03-01', 'Hanh trinh tren hanh tinh sa mac Arrakis', 'https://example.com/dune.jpg'),
('Inside Out 2', 96, 'P', '2024-06-14', 'Hanh trinh cam xuc cua Riley tuoi thanh thieu nien', 'https://example.com/insideout.jpg'),
('Deadpool & Wolverine', 128, 'T18', '2024-07-26', 'Su ket hop bua cua hai sieu anh hung Marvel', 'https://example.com/deadpool.jpg'),
('Co Dau Hao Mon', 114, 'T16', '2024-01-12', 'Phim tam ly xa hoi Viet Nam', 'https://example.com/codauhao.jpg'),
('Godzilla x Kong', 115, 'T13', '2024-03-29', 'Cuoc chien giua hai quai vat huyen thoai', 'https://example.com/godzilla.jpg'),
('Lat Mat 7', 138, 'T16', '2024-04-26', 'Phan moi nhat cua loat phim Lat Mat', 'https://example.com/latmat.jpg'),
('The Batman 2', 175, 'T13', '2025-10-03', 'Nguoi doi pha an tiep tuc bao ve Gotham', 'https://example.com/batman.jpg');

-- 6) PHIM_THE_LOAI
INSERT IGNORE INTO phim_the_loai (ma_phim, ma_the_loai) VALUES
(1, 1), (1, 7), (1, 2),
(2, 5), (2, 6),
(3, 8), (3, 1), (3, 9),
(4, 7), (4, 2), (4, 11),
(5, 8), (5, 9), (5, 3),
(6, 1), (6, 3), (6, 7),
(7, 5), (7, 6),
(8, 1), (8, 7), (8, 2),
(9, 1), (9, 10),
(10, 1), (10, 10), (10, 5);

-- 7) DINH_DANG – Formats
INSERT IGNORE INTO dinh_dang (ten_dinh_dang, ghi_chu) VALUES
('2D', 'Dinh dang chieu phim tieu chuan'),
('3D', 'Dinh dang ba chieu'),
('IMAX', 'Man hinh lon IMAX'),
('4DX', 'Rap chieu phim co hieu ung dac biet'),
('ScreenX', 'Man hinh 270 do');

-- 8) PHONG – Rooms
INSERT IGNORE INTO phong (ten_phong, suc_chua, trang_thai) VALUES
('Phong 1', 120, 'HOAT_DONG'),
('Phong 2', 150, 'HOAT_DONG'),
('Phong 3', 100, 'HOAT_DONG'),
('Phong 4', 180, 'HOAT_DONG'),
('Phong 5', 90,  'HOAT_DONG'),
('Phong VIP', 50, 'HOAT_DONG'),
('Phong IMAX', 250,'HOAT_DONG'),
('Phong 8', 120, 'BAO_TRI');

-- 9) LOAI_GHE
INSERT IGNORE INTO loai_ghe (ten_loai_ghe, he_so_gia) VALUES
 ('Thuong',   1.00),
 ('VIP',      1.50),
 ('Sweetbox', 2.00),
 ('IMAX',     1.80);

-- 10) GHE Phong 1
INSERT IGNORE INTO ghe (ma_phong, hang_ghe, so_ghe, ma_loai_ghe) VALUES
-- A–C: Thuong
('1', 'A', 1, 1), ('1', 'A', 2, 1), ('1', 'A', 3, 1), ('1', 'A', 4, 1), ('1', 'A', 5, 1),
('1', 'A', 6, 1), ('1', 'A', 7, 1), ('1', 'A', 8, 1), ('1', 'A', 9, 1), ('1', 'A',10, 1),
('1', 'B', 1, 1), ('1', 'B', 2, 1), ('1', 'B', 3, 1), ('1', 'B', 4, 1), ('1', 'B', 5, 1),
('1', 'B', 6, 1), ('1', 'B', 7, 1), ('1', 'B', 8, 1), ('1', 'B', 9, 1), ('1', 'B',10, 1),
('1', 'C', 1, 1), ('1', 'C', 2, 1), ('1', 'C', 3, 1), ('1', 'C', 4, 1), ('1', 'C', 5, 1),
('1', 'C', 6, 1), ('1', 'C', 7, 1), ('1', 'C', 8, 1), ('1', 'C', 9, 1), ('1', 'C',10, 1),
-- D–F: VIP
('1', 'D', 1, 2), ('1', 'D', 2, 2), ('1', 'D', 3, 2), ('1', 'D', 4, 2), ('1', 'D', 5, 2),
('1', 'D', 6, 2), ('1', 'D', 7, 2), ('1', 'D', 8, 2), ('1', 'D', 9, 2), ('1', 'D',10, 2),
('1', 'D',11, 2), ('1', 'D',12, 2),
('1', 'E', 1, 2), ('1', 'E', 2, 2), ('1', 'E', 3, 2), ('1', 'E', 4, 2), ('1', 'E', 5, 2),
('1', 'E', 6, 2), ('1', 'E', 7, 2), ('1', 'E', 8, 2), ('1', 'E', 9, 2), ('1', 'E',10, 2),
('1', 'E',11, 2), ('1', 'E',12, 2),
('1', 'F', 1, 2), ('1', 'F', 2, 2), ('1', 'F', 3, 2), ('1', 'F', 4, 2), ('1', 'F', 5, 2),
('1', 'F', 6, 2), ('1', 'F', 7, 2), ('1', 'F', 8, 2), ('1', 'F', 9, 2), ('1', 'F',10, 2),
('1', 'F',11, 2), ('1', 'F',12, 2),
-- G: Sweetbox
('1', 'G', 1, 3), ('1', 'G', 2, 3), ('1', 'G', 3, 3),
('1', 'G', 4, 3), ('1', 'G', 5, 3), ('1', 'G', 6, 3);

-- GHE Phong VIP (ma_phong = 6)
INSERT IGNORE INTO ghe (ma_phong, hang_ghe, so_ghe, ma_loai_ghe)
SELECT 6, hang, so, 2 FROM (
  SELECT 'A' AS hang, n AS so FROM (SELECT 1 n UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9 UNION SELECT 10) t
  UNION ALL
  SELECT 'B', n FROM (SELECT 1 n UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9 UNION SELECT 10) t
  UNION ALL
  SELECT 'C', n FROM (SELECT 1 n UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9 UNION SELECT 10) t
  UNION ALL
  SELECT 'D', n FROM (SELECT 1 n UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9 UNION SELECT 10) t
  UNION ALL
  SELECT 'E', n FROM (SELECT 1 n UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9 UNION SELECT 10) t
) seats;

-- 11) SUAT_CHIEU
-- 11) SUAT_CHIEU (Dữ liệu 2025)
INSERT IGNORE INTO suat_chieu (ma_phim, ma_phong, ma_dinh_dang, bat_dau_luc, ket_thuc_luc, gia_co_ban, trang_thai) VALUES
-- 16/11/2025
(1,1,2,'2025-11-16 09:00:00','2025-11-16 12:01:00',80000,'MO_BAN'),
(2,2,1,'2025-11-16 09:30:00','2025-11-16 11:40:00',75000,'MO_BAN'),
(3,3,2,'2025-11-16 10:00:00','2025-11-16 11:34:00',70000,'MO_BAN'),
(4,4,3,'2025-11-16 10:30:00','2025-11-16 13:16:00',120000,'MO_BAN'),
(1,1,2,'2025-11-16 13:00:00','2025-11-16 16:01:00',90000,'MO_BAN'),
(5,2,1,'2025-11-16 14:00:00','2025-11-16 15:36:00',75000,'MO_BAN'),
(6,6,1,'2025-11-16 15:00:00','2025-11-16 17:08:00',85000,'MO_BAN'),
(7,3,1,'2025-11-16 16:00:00','2025-11-16 17:54:00',80000,'MO_BAN'),
(8,4,4,'2025-11-16 17:00:00','2025-11-16 18:55:00',100000,'MO_BAN'),
(1,1,2,'2025-11-16 19:00:00','2025-11-16 22:01:00',100000,'MO_BAN'),
(9,2,1,'2025-11-16 20:00:00','2025-11-16 22:18:00',85000,'MO_BAN'),
(6,6,1,'2025-11-16 21:00:00','2025-11-16 23:08:00',90000,'MO_BAN'),
-- 17/11/2025
(2,1,1,'2025-11-17 09:00:00','2025-11-17 11:10:00',75000,'MO_BAN'),
(3,3,2,'2025-11-17 10:00:00','2025-11-17 11:34:00',70000,'MO_BAN'),
(5,2,1,'2025-11-17 11:30:00','2025-11-17 13:06:00',75000,'MO_BAN'),
(4,7,3,'2025-11-17 13:00:00','2025-11-17 15:46:00',150000,'MO_BAN'),
(1,1,2,'2025-11-17 14:00:00','2025-11-17 17:01:00',90000,'MO_BAN'),
(8,4,4,'2025-11-17 15:30:00','2025-11-17 17:25:00',100000,'MO_BAN'),
(6,6,1,'2025-11-17 18:00:00','2025-11-17 20:08:00',85000,'MO_BAN'),
(9,2,1,'2025-11-17 19:00:00','2025-11-17 21:18:00',90000,'MO_BAN'),
(1,1,2,'2025-11-17 20:00:00','2025-11-17 23:01:00',110000,'MO_BAN');

-- 12) VE
-- Ve cho suat 1, phong 1 – dùng gia fix cho dễ test
INSERT IGNORE INTO ve (ma_suat_chieu, ma_ghe, gia_ban, trang_thai, ban_luc)
SELECT 1, ma_ghe,
 CASE 
   WHEN ma_loai_ghe = 1 THEN 80000
   WHEN ma_loai_ghe = 2 THEN 120000
   WHEN ma_loai_ghe = 3 THEN 160000
 END,
 CASE 
   WHEN ma_ghe <= 15 THEN 'DA_BAN'
   WHEN ma_ghe <= 20 THEN 'GIU_CHO'
   ELSE 'SAN_SANG'
 END,
 CASE 
   WHEN ma_ghe <= 15 THEN '2025-11-15 10:30:00'
   ELSE NULL
 END
FROM ghe WHERE ma_phong = 1;

-- Ve cho cac suat chieu 2–10
INSERT IGNORE INTO ve (ma_suat_chieu, ma_ghe, gia_ban, trang_thai)
SELECT sc.ma_suat_chieu, g.ma_ghe,
  sc.gia_co_ban * lg.he_so_gia,
  'SAN_SANG'
FROM suat_chieu sc
JOIN ghe g       ON g.ma_phong      = sc.ma_phong
JOIN loai_ghe lg ON lg.ma_loai_ghe  = g.ma_loai_ghe
WHERE sc.ma_suat_chieu BETWEEN 2 AND 10;

-- 13) SAN_PHAM
INSERT IGNORE INTO san_pham (ten_san_pham, loai, gia, hoat_dong) VALUES
('Popcorn Caramel - Size L',  'POPCORN', 60000, 1),
('Popcorn Caramel - Size M',  'POPCORN', 45000, 1),
('Popcorn Bo - Size L',       'POPCORN', 55000, 1),
('Popcorn Bo - Size M',       'POPCORN', 40000, 1),
('Coca Cola - Size L',        'NUOC',    35000, 1),
('Coca Cola - Size M',        'NUOC',    25000, 1),
('Pepsi - Size L',            'NUOC',    35000, 1),
('Sprite - Size M',           'NUOC',    25000, 1),
('Nuoc Suoi',                 'NUOC',    15000, 1),
('Hotdog',                    'AN_VAT',  30000, 1),
('Nachos Pho Mai',            'AN_VAT',  40000, 1),
('Khoai Tay Chien',           'AN_VAT',  35000, 1);

-- 14) COMBO
INSERT IGNORE INTO combo (ten_combo, gia, hoat_dong) VALUES
('Combo Solo',   90000, 1),
('Combo Couple', 150000,1),
('Combo Family', 220000,1),
('Combo VIP',    180000,1);

-- 15) COMBO_CHI_TIET
INSERT IGNORE INTO combo_chi_tiet (ma_combo, ma_san_pham, so_luong) VALUES
(1, 2, 1), (1, 6, 1),
(2, 1, 1), (2, 6, 2),
(3, 1, 2), (3, 6, 4), (3,10, 2),
(4, 1, 1), (4, 5, 2), (4,11, 1);

-- 16) KHUYEN_MAI
INSERT IGNORE INTO khuyen_mai (ma_code, kieu_giam, gia_tri_giam, bat_dau_luc, ket_thuc_luc, don_toi_thieu, hoat_dong) VALUES
('KHAIGIANG2024','PHAN_TRAM', 15.00,  '2024-09-01 00:00:00','2024-09-30 23:59:59',100000,1),
('GIANG20K',     'SO_TIEN',   20000.00,'2024-11-01 00:00:00','2024-12-31 23:59:59',150000,1),
('WEEKEND30',    'PHAN_TRAM', 30.00,  '2024-11-01 00:00:00','2024-11-30 23:59:59',200000,1),
('MEMBER50K',    'SO_TIEN',   50000.00,'2024-10-01 00:00:00','2024-12-31 23:59:59',300000,1),
('FIRSTTIME',    'PHAN_TRAM', 25.00,  '2024-01-01 00:00:00','2024-12-31 23:59:59',0,     1);

-- 17) DON_HANG
-- Giả định: don_hang có cột: ma_khach_hang, ma_nhan_vien, tao_luc, kenh, trang_thai (TINYINT), tong_tien
-- 1 = Đã thanh toán, 0 = Đã hủy, 2 = Chờ xử lý
INSERT IGNORE INTO don_hang (ma_khach_hang, ma_nhan_vien, tao_luc, kenh, trang_thai, tong_tien) VALUES
(1,2,'2025-11-15 10:00:00','TRUC_TUYEN',1,250000),
(2,2,'2025-11-15 11:30:00','TRUC_TUYEN',1,340000),
(3,3,'2025-11-15 14:20:00','TRUC_TIEP',1,180000),
(1,2,'2025-11-16 09:00:00','TRUC_TUYEN',1,420000),
(4,3,'2025-11-16 10:30:00','TRUC_TIEP',1,160000),
(5,2,'2025-11-16 15:45:00','TRUC_TUYEN',1,295000);

-- 18) DON_VE – Ticket orders
INSERT IGNORE INTO don_ve (ma_don_hang, ma_ve, don_gia) VALUES
(1, 1, 80000),(1, 2, 80000),
(2, 3, 80000),(2, 4, 80000),(2, 5, 80000),
(3, 6, 80000),(3, 7, 80000),
(4, 8, 80000),(4, 9, 80000),(4,10, 80000),
(5,11, 80000),(5,12, 80000),
(6,13, 80000),(6,14, 80000);

-- 19) HANG_HOA – Order items (sp/combo)
INSERT IGNORE INTO hang_hoa (ma_don_hang, ma_san_pham, ma_combo, so_luong, don_gia) VALUES
(1, NULL, 1, 1,  90000),
(2, NULL, 2, 1, 150000),
(3, 6,   NULL, 2, 25000),
(4, NULL, 3, 1, 220000),
(5, 2,   NULL, 1, 45000),
(5, 6,   NULL, 1, 25000),
(6, NULL, 4, 1, 180000);

-- 20) THANH_TOAN
INSERT IGNORE INTO thanh_toan (ma_don_hang, so_tien, phuong_thuc, trang_thai, thanh_toan_luc, ma_tham_chieu) VALUES
(1,250000,'VI_DIEN_TU','THANH_CONG','2025-11-15 10:02:00','MOMO_TXN_001'),
(2,340000,'THE','THANH_CONG','2025-11-15 11:32:00','CARD_TXN_002'),
(3,180000,'TIEN_MAT','THANH_CONG','2025-11-15 14:22:00',NULL),
(4,420000,'CHUYEN_KHOAN','THANH_CONG','2025-11-16 09:05:00','BANK_TXN_003'),
(5,160000,'TIEN_MAT','THANH_CONG','2025-11-16 10:32:00',NULL),
(6,295000,'VI_DIEN_TU','THANH_CONG','2025-11-16 15:47:00','MOMO_TXN_004');

-- 21) DON_KHUYEN_MAI
INSERT IGNORE INTO don_khuyen_mai (ma_don_hang, ma_khuyen_mai) VALUES
(2, 2),
(4, 3),
(6, 2);

SET FOREIGN_KEY_CHECKS = 1;
UPDATE khuyen_mai
SET bat_dau_luc  = CONCAT('2025', SUBSTRING(bat_dau_luc,5)),
    ket_thuc_luc = CONCAT('2025', SUBSTRING(ket_thuc_luc,5))
WHERE YEAR(bat_dau_luc)=2024;

-- Kiểm tra nhanh
SELECT 'Tong so tai khoan:'   AS Thong_ke, COUNT(*) AS So_luong FROM tai_khoan
UNION ALL SELECT 'Tong so phim:',          COUNT(*) FROM phim
UNION ALL SELECT 'Tong so suat chieu:',    COUNT(*) FROM suat_chieu
UNION ALL SELECT 'Tong so ve:',            COUNT(*) FROM ve
UNION ALL SELECT 'Tong so don hang:',      COUNT(*) FROM don_hang
UNION ALL SELECT 'Tong so san pham:',      COUNT(*) FROM san_pham
UNION ALL SELECT 'Tong so combo:',         COUNT(*) FROM combo
UNION ALL SELECT 'Tong so khuyen mai:',    COUNT(*) FROM khuyen_mai;
