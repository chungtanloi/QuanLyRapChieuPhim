USE qlrapchieuphim;

-- =========================================
-- A) SCHEMA CLEANUP (đồng bộ FK mật khẩu)
-- =========================================
-- 1) Bảng mat_khau (PK BIGINT, KHÔNG dùng VARCHAR cho khóa!)
CREATE TABLE IF NOT EXISTS mat_khau (
  mat_khau_ma   BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  mat_khau_hash VARCHAR(100)     NOT NULL,
  tao_luc       DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (mat_khau_ma)
) ENGINE=InnoDB;

-- 2) tai_khoan: đồng bộ cột, drop FK cũ nếu có, add FK chuẩn
--   (MySQL không có IF EXISTS cho DROP FOREIGN KEY trước 8.0.19;
--    dòng dưới dùng comment versioned: chỉ chạy nếu bản hỗ trợ)
--   Nếu báo không tồn tại thì bỏ qua.
/*!80019 ALTER TABLE tai_khoan DROP FOREIGN KEY IF EXISTS fk_tk_mk */;

ALTER TABLE tai_khoan
  MODIFY COLUMN ma_tai_khoan BIGINT UNSIGNED NOT NULL,
  MODIFY COLUMN email        VARCHAR(255) NULL,
  MODIFY COLUMN ho_ten       VARCHAR(255) NULL,
  MODIFY COLUMN so_dien_thoai VARCHAR(255) NULL,
  MODIFY COLUMN mat_khau_ma  BIGINT UNSIGNED NULL;

ALTER TABLE tai_khoan
  ADD INDEX ix_tk_mk (mat_khau_ma),
  ADD CONSTRAINT fk_tk_mk
    FOREIGN KEY (mat_khau_ma) REFERENCES mat_khau(mat_khau_ma)
    ON UPDATE CASCADE ON DELETE RESTRICT;

-- 3) nhan_vien: chuẩn hóa kiểu
ALTER TABLE nhan_vien
  MODIFY COLUMN ma_nhan_vien BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  MODIFY COLUMN ma_tai_khoan BIGINT UNSIGNED NULL;

-- =========================================
-- B) DỮ LIỆU MẪU (vé – đơn hàng – thanh toán)
-- =========================================
-- 1) Tạo vé mẫu (một số ghế đầu của các suất chiếu 1..7)
INSERT INTO ve (ma_suat_chieu, ma_ghe, gia_ban, trang_thai, ban_luc)
SELECT sc.ma_suat_chieu, g.ma_ghe, sc.gia_co_ban, 'DA_BAN', NOW() - INTERVAL 6 DAY
FROM suat_chieu sc
JOIN ghe g ON g.ma_phong = sc.ma_phong
WHERE sc.ma_suat_chieu BETWEEN 1 AND 7
  AND g.so_ghe <= 3
LIMIT 15;

-- 2) Tạo đơn hàng cho KH #1, #2 khớp thời điểm suất chiếu
INSERT INTO don_hang (ma_khach_hang, dat_luc, kenh, trang_thai, tong_tien)
SELECT kh.ma_khach_hang,
       sc.bat_dau_luc - INTERVAL 30 MINUTE,
       'TRUC_TUYEN',
       'DA_THANH_TOAN',
       sc.gia_co_ban * 2
FROM khach_hang kh
JOIN suat_chieu sc ON kh.ma_khach_hang IN (1, 2)
LIMIT 7;

-- 3) Ghép đơn hàng ↔ vé **đúng cách** (bắt cặp theo thứ tự)
WITH v AS (
  SELECT v.ma_ve, v.gia_ban, ROW_NUMBER() OVER (ORDER BY v.ma_ve) rn
  FROM ve v
  WHERE v.trang_thai = 'DA_BAN'
    AND NOT EXISTS (SELECT 1 FROM don_ve dv WHERE dv.ma_ve = v.ma_ve)
),
dh AS (
  SELECT dh.ma_don_hang, ROW_NUMBER() OVER (ORDER BY dh.ma_don_hang) rn
  FROM don_hang dh
)
INSERT INTO don_ve (ma_don_hang, ma_ve, don_gia)
SELECT dh.ma_don_hang, v.ma_ve, v.gia_ban
FROM dh
JOIN v USING (rn);

-- 4) Tạo bản ghi thanh toán tương ứng
INSERT INTO thanh_toan (ma_don_hang, so_tien, phuong_thuc, trang_thai, thanh_toan_luc)
SELECT dh.ma_don_hang, dh.tong_tien, 'THE', 'THANH_CONG', dh.dat_luc
FROM don_hang dh
WHERE NOT EXISTS (
  SELECT 1 FROM thanh_toan tt WHERE tt.ma_don_hang = dh.ma_don_hang
);

-- 5) Đánh dấu vé của các suất chiếu hôm nay là đã bán (nếu có)
UPDATE ve v
JOIN suat_chieu sc ON v.ma_suat_chieu = sc.ma_suat_chieu
SET v.ban_luc = NOW(), v.trang_thai = 'DA_BAN'
WHERE DATE(sc.bat_dau_luc) = CURDATE();

-- (Tùy chọn) Kiểm tra nhanh
-- SELECT COUNT(*) AS so_ve, SUM(don_gia) AS doanh_thu FROM don_ve;
-- SELECT * FROM don_hang ORDER BY ma_don_hang DESC LIMIT 10;
