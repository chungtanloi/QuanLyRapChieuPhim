DELIMITER //
CREATE TRIGGER tr_BeforeInsert_KhuyenMai_CheckDuplicate
BEFORE INSERT ON khuyen_mai
FOR EACH ROW
BEGIN
    DECLARE existing_count INT;
    
    SELECT COUNT(*) INTO existing_count 
    FROM khuyen_mai 
    WHERE ma_code = NEW.ma_code;
    
    IF existing_count > 0 THEN
        SIGNAL SQLSTATE '45000' 
        SET MESSAGE_TEXT = 'Mã khuyến mãi đã tồn tại';
    END IF;
END //
select *from tai_khoan;
