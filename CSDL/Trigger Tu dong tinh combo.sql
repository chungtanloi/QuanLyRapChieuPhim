

DELIMITER //
CREATE TRIGGER tr_AfterInsertUpdate_ComboChiTiet_AutoCalculate
AFTER INSERT ON combo_chi_tiet
FOR EACH ROW
BEGIN
    DECLARE total_price DECIMAL(18,2);
    
    -- Tính tổng giá gốc của tất cả sản phẩm trong combo
    SELECT SUM(sp.gia * ct.so_luong) INTO total_price
    FROM combo_chi_tiet ct
    JOIN san_pham sp ON ct.ma_san_pham = sp.ma_san_pham
    WHERE ct.ma_combo = NEW.ma_combo;
    
    -- Cập nhật giá combo (giá gốc * 0.9 - giảm 10%)
    UPDATE combo 
    SET gia = total_price * 0.9
    WHERE ma_combo = NEW.ma_combo;
END //

CREATE TRIGGER tr_AfterDelete_ComboChiTiet_AutoCalculate
AFTER DELETE ON combo_chi_tiet
FOR EACH ROW
BEGIN
    DECLARE total_price DECIMAL(18,2);
    DECLARE product_count INT;
    
    -- Đếm số sản phẩm còn lại trong combo
    SELECT COUNT(*) INTO product_count
    FROM combo_chi_tiet
    WHERE ma_combo = OLD.ma_combo;
    
    -- Nếu không còn sản phẩm nào, set giá về 0
    IF product_count = 0 THEN
        UPDATE combo SET gia = 0 WHERE ma_combo = OLD.ma_combo;
    ELSE
        -- Tính lại tổng giá
        SELECT SUM(sp.gia * ct.so_luong) INTO total_price
        FROM combo_chi_tiet ct
        JOIN san_pham sp ON ct.ma_san_pham = sp.ma_san_pham
        WHERE ct.ma_combo = OLD.ma_combo;
        
        UPDATE combo SET gia = total_price * 0.9 WHERE ma_combo = OLD.ma_combo;
    END IF;
END //