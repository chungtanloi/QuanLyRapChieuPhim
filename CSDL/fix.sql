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