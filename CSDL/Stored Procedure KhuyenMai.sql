DELIMITER //
CREATE PROCEDURE sp_KiemTraKhuyenMai(
    IN p_ma_code VARCHAR(50),
    IN p_thoi_gian TIMESTAMP
)
BEGIN
    SELECT * FROM khuyen_mai 
    WHERE ma_code = p_ma_code 
    AND hoat_dong = 1
    AND bat_dau_luc <= p_thoi_gian 
    AND ket_thuc_luc >= p_thoi_gian;
END //