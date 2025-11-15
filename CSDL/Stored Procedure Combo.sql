DELIMITER //
CREATE PROCEDURE sp_ThemCombo(
    IN p_ten_combo VARCHAR(255),
    IN p_gia DECIMAL(18,2),
    IN p_hoat_dong BOOLEAN,
    OUT p_ma_combo INT
)
BEGIN
    INSERT INTO combo (ten_combo, gia, hoat_dong) 
    VALUES (p_ten_combo, p_gia, p_hoat_dong);
    SET p_ma_combo = LAST_INSERT_ID();
END //

CREATE PROCEDURE sp_LayComboTheoMa(IN p_ma_combo INT)
BEGIN
    SELECT * FROM combo WHERE ma_combo = p_ma_combo;
END //