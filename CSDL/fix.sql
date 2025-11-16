-- =====================================================
-- SỬA LỖI don_gia TRONG CÁC BẢNG LIÊN QUAN SẢN PHẨM
-- =====================================================

-- =====================================================
-- 1. BẢNG san_pham
-- =====================================================
DESC san_pham;

-- Nếu thiếu cột don_gia, thêm vào
ALTER TABLE san_pham 
ADD COLUMN don_gia BIGINT NULL 
AFTER gia;

-- Đồng bộ dữ liệu
SET SQL_SAFE_UPDATES = 0;

UPDATE san_pham 
SET don_gia = gia 
WHERE don_gia IS NULL;

SET SQL_SAFE_UPDATES = 1;

-- Trigger tự động cho san_pham
DROP TRIGGER IF EXISTS trg_san_pham_auto_don_gia;

DELIMITER $$

CREATE TRIGGER trg_san_pham_auto_don_gia
BEFORE INSERT ON san_pham
FOR EACH ROW
BEGIN
    IF NEW.don_gia IS NULL OR NEW.don_gia = 0 THEN
        SET NEW.don_gia = NEW.gia;
    END IF;
END$$

DELIMITER ;

-- Trigger cho UPDATE
DROP TRIGGER IF EXISTS trg_san_pham_update_don_gia;

DELIMITER $$

CREATE TRIGGER trg_san_pham_update_don_gia
BEFORE UPDATE ON san_pham
FOR EACH ROW
BEGIN
    IF NEW.gia != OLD.gia THEN
        SET NEW.don_gia = NEW.gia;
    END IF;
END$$

DELIMITER ;

-- =====================================================
-- 2. BẢNG don_combo
-- =====================================================
DESC don_combo;
ALTER TABLE don_combo 
add COLUMN gia_ban BIGINT NULL;

-- Nếu cột don_gia là NOT NULL, cho phép NULL
ALTER TABLE don_combo 
MODIFY COLUMN gia_ban BIGINT NULL;

-- Cập nhật dữ liệu cũ từ gia_ban
SET SQL_SAFE_UPDATES = 0;

UPDATE don_combo 
SET gia_ban = don_gia 
WHERE gia_ban IS NULL;

SET SQL_SAFE_UPDATES = 1;

-- Trigger tự động cho don_combo
DROP TRIGGER IF EXISTS trg_don_combo_auto_don_gia;

DELIMITER $$

CREATE TRIGGER trg_don_combo_auto_don_gia
BEFORE INSERT ON don_combo
FOR EACH ROW
BEGIN
    IF NEW.don_gia IS NULL OR NEW.don_gia = 0 THEN
        SET NEW.gia_ban = NEW.don_gia;
    END IF;
END$$

DELIMITER ;

-- =====================================================
-- 3. KIỂM TRA VÀ SỬA CÁC RECORD NULL
-- =====================================================

-- Kiểm tra san_pham có record nào NULL
SELECT * FROM san_pham WHERE don_gia IS NULL;

-- Kiểm tra don_combo có record nào NULL
SELECT * FROM don_combo WHERE don_gia IS NULL;

-- =====================================================
-- 4. KIỂM TRA KẾT QUẢ
-- =====================================================
DESC san_pham;
DESC don_combo;

SHOW TRIGGERS WHERE `Table` IN ('san_pham', 'don_combo');

-- Xem dữ liệu mẫu
SELECT ma_san_pham, ten_san_pham, gia, don_gia FROM san_pham LIMIT 5;
SELECT ma_don_hang, ma_combo, gia_ban, don_gia FROM don_combo LIMIT 5;
DELIMITER $$
CREATE TRIGGER trg_don_ve_auto_don_gia
BEFORE INSERT ON don_ve
FOR EACH ROW
BEGIN
    DECLARE v_gia_ban BIGINT;
    
    -- Lấy giá bán từ bảng ve
    SELECT gia_ban INTO v_gia_ban
    FROM ve
    WHERE ma_ve = NEW.ma_ve
    LIMIT 1;
    
    -- Tự động điền don_gia nếu NULL hoặc 0
    IF NEW.don_gia IS NULL OR NEW.don_gia = 0 THEN
        SET NEW.don_gia = COALESCE(v_gia_ban, 0);
    END IF;
END$$

DELIMITER ;
-- =====================================================
-- Stored Procedure: Tạo hoặc cập nhật vé
-- =====================================================
DROP PROCEDURE IF EXISTS sp_create_or_update_ve;

DELIMITER $$

CREATE PROCEDURE sp_create_or_update_ve(
    IN p_ma_suat_chieu INT,
    IN p_ma_ghe INT,
    IN p_gia_ban BIGINT,
    OUT p_ma_ve INT,
    OUT p_error_message VARCHAR(255)
)
BEGIN
    DECLARE v_existing_ma_ve INT;
    DECLARE v_trang_thai VARCHAR(20);
    
    -- Khởi tạo
    SET p_error_message = NULL;
    SET p_ma_ve = NULL;
    
    -- 1. Kiểm tra vé đã tồn tại chưa
    SELECT ma_ve, trang_thai 
    INTO v_existing_ma_ve, v_trang_thai
    FROM ve 
    WHERE ma_suat_chieu = p_ma_suat_chieu 
      AND ma_ghe = p_ma_ghe
    LIMIT 1;
    
    -- 2. Nếu vé đã tồn tại
    IF v_existing_ma_ve IS NOT NULL THEN
        -- Kiểm tra trạng thái
        IF v_trang_thai = 'DA_BAN' THEN
            SET p_error_message = 'Ghế đã được bán, vui lòng chọn ghế khác';
            SET p_ma_ve = NULL;
        ELSE
            -- Cập nhật vé thành đã bán
            UPDATE ve
            SET gia_ban = p_gia_ban,
                trang_thai = 'DA_BAN',
                ban_luc = NOW(),
                cap_nhat_luc = NOW()
            WHERE ma_ve = v_existing_ma_ve;
            
            SET p_ma_ve = v_existing_ma_ve;
        END IF;
    ELSE
        -- 3. Chưa có vé -> tạo mới
        INSERT INTO ve(
            ma_suat_chieu, 
            ma_ghe, 
            gia_ban, 
            trang_thai, 
            giu_cho_luc, 
            ban_luc, 
            tao_luc, 
            cap_nhat_luc
        )
        VALUES (
            p_ma_suat_chieu,
            p_ma_ghe,
            p_gia_ban,
            'DA_BAN',
            NULL,
            NOW(),
            NOW(),
            NOW()
        );
        
        SET p_ma_ve = LAST_INSERT_ID();
    END IF;
    
END$$

DELIMITER ;
-- =====================================================
-- THÊM CỘT ngay_gio_chieu VÀO BẢNG suat_chieu
-- =====================================================

-- Cách 1: Thêm cột DATETIME (nếu muốn lưu cả ngày và giờ)
ALTER TABLE suat_chieu 
ADD COLUMN ngay_gio_chieu DATETIME NULL 
AFTER bat_dau_luc;
SET SQL_SAFE_UPDATES = 0;
-- =====================================================
-- CẬP NHẬT DỮ LIỆU TỪ CỘT bat_dau_luc SANG ngay_gio_chieu
-- =====================================================
UPDATE suat_chieu 
SET ngay_gio_chieu = bat_dau_luc 
WHERE ngay_gio_chieu IS NULL;

-- =====================================================
-- (TÙY CHỌN) ĐẶT NOT NULL SAU KHI ĐÃ CẬP NHẬT DỮ LIỆU
-- =====================================================
ALTER TABLE suat_chieu 
MODIFY COLUMN ngay_gio_chieu DATETIME NOT NULL;
-- =====================================================
-- Test Stored Procedure
-- =====================================================
-- CALL sp_create_or_update_ve(1, 10, 50000, @ma_ve, @error);
-- SELECT @ma_ve AS ma_ve_result, @error AS error_message;
-- =====================================================
-- THÊM CỘT gia VÀ don_gia VÀO CÁC BẢNG
-- =====================================================

-- =====================================================
-- 1. BẢNG ve - Thêm cột gia (nếu chưa có)
-- =====================================================

-- Kiểm tra cột nào đang tồn tại
DESC ve;

-- Thêm cột gia nếu chỉ có gia_ban
ALTER TABLE ve 
ADD COLUMN don_gia BIGINT NULL 
AFTER gia_ban;

-- Tắt Safe Mode
SET SQL_SAFE_UPDATES = 0;

-- Đồng bộ dữ liệu từ gia_ban sang gia
UPDATE ve 
SET gia = gia_ban 
WHERE gia IS NULL;

-- Bật lại Safe Mode
SET SQL_SAFE_UPDATES = 1;

-- (Tùy chọn) Đặt NOT NULL
ALTER TABLE ve 
MODIFY COLUMN gia BIGINT NOT NULL;

-- =====================================================
-- 2. BẢNG san_pham - Thêm cột don_gia (nếu chỉ có gia)
-- =====================================================

-- Kiểm tra cột nào đang tồn tại
DESC san_pham;

-- Thêm cột don_gia nếu chỉ có gia
ALTER TABLE san_pham 
ADD COLUMN don_gia BIGINT NULL 
AFTER gia;

-- Tắt Safe Mode
SET SQL_SAFE_UPDATES = 0;

-- Đồng bộ dữ liệu từ gia sang don_gia
UPDATE san_pham 
SET don_gia = gia 
WHERE don_gia IS NULL;

-- Bật lại Safe Mode
SET SQL_SAFE_UPDATES = 1;

-- (Tùy chọn) Đặt NOT NULL
ALTER TABLE san_pham 
MODIFY COLUMN don_gia BIGINT NOT NULL;

-- =====================================================
-- 3. BẢNG don_combo - Thêm cột don_gia (nếu chỉ có gia_ban)
-- =====================================================

-- 1) Thêm cột ma_nhan_vien (cho phép NULL để không bị lỗi dữ liệu cũ)
ALTER TABLE don_hang
  ADD COLUMN ma_nhan_vien BIGINT NULL AFTER ma_khach_hang;

-- 2) Thêm khóa ngoại sang bảng nhan_vien
ALTER TABLE don_hang
  ADD CONSTRAINT fk_donhang_nhanvien
    FOREIGN KEY (ma_nhan_vien) REFERENCES nhan_vien(ma_nhan_vien);

-- Bảng chi tiết combo theo từng đơn hàng
CREATE TABLE don_combo (
    ma_don_hang BIGINT NOT NULL,
    ma_combo    BIGINT NOT NULL,
    so_luong    INT    NOT NULL DEFAULT 1,
    gia_ban     DECIMAL(12,2) NOT NULL,

    -- Không nhất thiết cần khóa tự tăng vì code KHÔNG dùng,
    -- dùng khóa chính tổng hợp cho gọn:
    PRIMARY KEY (ma_don_hang, ma_combo)

    
);

-- Kiểm tra cột nào đang tồn tại
DESC don_combo;

-- Thêm cột don_gia nếu chỉ có gia_ban
ALTER TABLE don_combo 
ADD COLUMN don_gia BIGINT NULL 
AFTER gia_ban;

-- Tắt Safe Mode
SET SQL_SAFE_UPDATES = 0;

-- Đồng bộ dữ liệu từ gia_ban sang don_gia
UPDATE don_combo 
SET don_gia = gia_ban 
WHERE don_gia IS NULL;

-- Bật lại Safe Mode
SET SQL_SAFE_UPDATES = 1;

-- (Tùy chọn) Đặt NOT NULL
ALTER TABLE don_combo 
MODIFY COLUMN don_gia BIGINT NOT NULL;
delimiter $$
CREATE TRIGGER trg_don_ve_auto_don_gia
BEFORE INSERT ON don_ve
FOR EACH ROW
BEGIN
    DECLARE v_gia_ban BIGINT;
    
    -- Lấy giá bán từ bảng ve
    SELECT gia_ban INTO v_gia_ban
    FROM ve
    WHERE ma_ve = NEW.ma_ve
    LIMIT 1;
    
    -- Tự động điền don_gia nếu NULL hoặc 0
    IF NEW.don_gia IS NULL OR NEW.don_gia = 0 THEN
        SET NEW.don_gia = COALESCE(v_gia_ban, 0);
    END IF;
END$$

DELIMITER ;

USE qlrapchieuphim;
