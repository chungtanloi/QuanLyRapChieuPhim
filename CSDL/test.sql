USE qlrapchieuphim;

-- ===== 1. TẠO VÉ =====
-- chọn vài ghế VIP ở phòng 1 & 2 làm ví dụ
INSERT INTO ve (ma_suat_chieu, ma_ghe, gia_ban, trang_thai, ban_luc)
SELECT sc.ma_suat_chieu, g.ma_ghe, sc.gia_co_ban, 'DA_BAN', NOW() - INTERVAL 6 DAY
FROM suat_chieu sc
JOIN ghe g ON g.ma_phong=sc.ma_phong
WHERE sc.ma_suat_chieu BETWEEN 1 AND 7 AND g.so_ghe<=3
LIMIT 15;

-- ===== 2. KHÁCH HÀNG & ĐƠN HÀNG =====
INSERT INTO don_hang (ma_khach_hang, dat_luc, kenh, trang_thai, tong_tien)
SELECT kh.ma_khach_hang, sc.bat_dau_luc - INTERVAL 30 MINUTE, 'TRUC_TUYEN', 'DA_THANH_TOAN', sc.gia_co_ban*2
FROM khach_hang kh
JOIN suat_chieu sc ON kh.ma_khach_hang IN (1,2)
LIMIT 7;

-- ===== 3. LIÊN KẾT ĐƠN HÀNG ↔️ VÉ =====
INSERT INTO don_ve (ma_don_hang, ma_ve, don_gia)
SELECT dh.ma_don_hang, v.ma_ve, v.gia_ban
FROM don_hang dh
JOIN ve v ON dh.ma_don_hang=v.ma_ve;

-- ===== 4. THANH TOÁN =====
INSERT INTO thanh_toan (ma_don_hang, so_tien, phuong_thuc, trang_thai, thanh_toan_luc)
SELECT dh.ma_don_hang, dh.tong_tien, 'THE', 'THANH_CONG', dh.dat_luc
FROM don_hang dh;

-- ===== 5. XÁC NHẬN MỘT SỐ VÉ MỚI HÔM NAY =====
UPDATE ve v
JOIN suat_chieu sc ON v.ma_suat_chieu=sc.ma_suat_chieu
SET v.ban_luc = NOW(), v.trang_thai='DA_BAN'
WHERE sc.bat_dau_luc >= CURDATE();
