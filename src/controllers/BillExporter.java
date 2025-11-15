package controllers;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import database.DBConnection;

import java.io.File;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.sql.*;
import java.text.NumberFormat;
import java.util.Locale;

public class BillExporter {

    public static void export(int maDon) throws Exception {

        // ================== TẠO THƯ MỤC ==================
        File dir = new File("HoadonPDF");
        if (!dir.exists()) dir.mkdirs();

        String fileName = "HoadonPDF/HoaDon_" + maDon + ".pdf";

        Document doc = new Document(PageSize.A5, 36, 36, 54, 36);
        PdfWriter writer = PdfWriter.getInstance(doc, new FileOutputStream(fileName));
        doc.open();

        // ================== FONT TIẾNG VIỆT ==================
        BaseFont bf = BaseFont.createFont("c:/windows/fonts/arial.ttf",
                BaseFont.IDENTITY_H, BaseFont.EMBEDDED);

        Font titleFont = new Font(bf, 16, Font.BOLD, BaseColor.BLUE);
        Font normalFont = new Font(bf, 12, Font.NORMAL);
        Font boldFont = new Font(bf, 12, Font.BOLD);

        NumberFormat nf = NumberFormat.getInstance(new Locale("vi", "VN"));

        // ================== TIÊU ĐỀ ==================
        Paragraph title = new Paragraph("HÓA ĐƠN BÁN HÀNG\n\n", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        doc.add(title);

        // ================== LẤY THÔNG TIN ĐƠN ==================
        String sqlHD = """
            SELECT dh.ma_don_hang, dh.tao_luc, dh.tong_tien,
                   COALESCE(tkKH.ho_ten, 'Khách lẻ') AS khach,
                   COALESCE(tkNV.ho_ten, 'Hệ thống') AS nhanvien,
                   CASE dh.trang_thai
                       WHEN 1 THEN 'Đã thanh toán'
                       WHEN 0 THEN 'Đã hủy'
                       ELSE 'Chờ xử lý'
                   END AS trang_thai
            FROM don_hang dh
            LEFT JOIN khach_hang kh ON kh.ma_khach_hang = dh.ma_khach_hang
            LEFT JOIN tai_khoan tkKH ON tkKH.ma_tai_khoan = kh.ma_tai_khoan
            LEFT JOIN nhan_vien nv ON nv.ma_nhan_vien = dh.ma_nhan_vien
            LEFT JOIN tai_khoan tkNV ON tkNV.ma_tai_khoan = nv.ma_tai_khoan
            WHERE dh.ma_don_hang = ?
        """;

        String ma = "", ngay = "", khach = "", nv = "", ttHD = "";
        BigDecimal tong = BigDecimal.ZERO;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sqlHD)) {

            ps.setInt(1, maDon);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                ma = String.valueOf(rs.getInt("ma_don_hang"));
                ngay = rs.getTimestamp("tao_luc").toLocalDateTime()
                        .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
                khach = rs.getString("khach");
                nv = rs.getString("nhanvien");
                tong = rs.getBigDecimal("tong_tien");
                ttHD = rs.getString("trang_thai");
            }
        }

        // ================== BẢNG THÔNG TIN ==================
        PdfPTable info = new PdfPTable(2);
        info.setWidthPercentage(100);

        info.addCell(cell("Mã hóa đơn:", boldFont));
        info.addCell(cell(ma, normalFont));

        info.addCell(cell("Ngày lập:", boldFont));
        info.addCell(cell(ngay, normalFont));

        info.addCell(cell("Khách hàng:", boldFont));
        info.addCell(cell(khach, normalFont));

        info.addCell(cell("Nhân viên:", boldFont));
        info.addCell(cell(nv, normalFont));

        info.addCell(cell("Trạng thái:", boldFont));
        info.addCell(cell(ttHD, normalFont));

        doc.add(info);
        doc.add(new Paragraph("\n"));

        // ================== LOAD CHI TIẾT – VÉ & SẢN PHẨM ==================
        PdfPTable tb = new PdfPTable(4);
        tb.setWidthPercentage(100);

        tb.addCell(cell("Sản phẩm", boldFont));
        tb.addCell(cell("SL", boldFont));
        tb.addCell(cell("Giá", boldFont));
        tb.addCell(cell("T.Tiền", boldFont));

        try (Connection conn = DBConnection.getConnection()) {

            // ===== VÉ PHIM =====
            PreparedStatement ps1 = conn.prepareStatement("""
                SELECT p.ten_phim, sc.bat_dau_luc, v.gia_ban, v.ten_ghe
                FROM don_ve dv
                JOIN ve v ON v.ma_ve = dv.ma_ve
                JOIN suat_chieu sc ON sc.ma_suat_chieu = v.ma_suat_chieu
                JOIN phim p ON p.ma_phim = sc.ma_phim
                WHERE dv.ma_don_hang = ?
            """);
            ps1.setInt(1, maDon);
            ResultSet rs1 = ps1.executeQuery();

            while (rs1.next()) {
                String ten = rs1.getString("ten_phim");
                String ghe = rs1.getString("ten_ghe");
                long gia = rs1.getLong("gia_ban");

                tb.addCell(cell(ten + " - Ghế " + ghe, normalFont));
                tb.addCell(cell("1", normalFont));
                tb.addCell(cell(nf.format(gia), normalFont));
                tb.addCell(cell(nf.format(gia), normalFont));
            }

            // ===== COMBO/SẢN PHẨM =====
            PreparedStatement ps2 = conn.prepareStatement("""
                SELECT sp.ten_san_pham, dc.so_luong, dc.gia_ban
                FROM don_combo dc
                JOIN san_pham sp ON sp.ma_san_pham = dc.ma_combo
                WHERE dc.ma_don_hang = ?
            """);
            ps2.setInt(1, maDon);
            ResultSet rs2 = ps2.executeQuery();

            while (rs2.next()) {
                String ten = rs2.getString("ten_san_pham");
                int sl = rs2.getInt("so_luong");
                long gia = rs2.getLong("gia_ban");
                long tt = gia * sl;

                tb.addCell(cell(ten, normalFont));
                tb.addCell(cell(String.valueOf(sl), normalFont));
                tb.addCell(cell(nf.format(gia), normalFont));
                tb.addCell(cell(nf.format(tt), normalFont));
            }

        }

        doc.add(tb);

        // ================== TỔNG TIỀN ==================
        PdfPTable total = new PdfPTable(2);
        total.setWidthPercentage(100);

        PdfPCell left = new PdfPCell(new Phrase("TỔNG TIỀN:", boldFont));
        left.setBorder(Rectangle.TOP);
        left.setHorizontalAlignment(Element.ALIGN_RIGHT);

        PdfPCell right = new PdfPCell(new Phrase(nf.format(tong) + " đ", boldFont));
        right.setBorder(Rectangle.TOP);

        total.addCell(left);
        total.addCell(right);

        doc.add(total);

        // ================== FOOTER ==================
        doc.add(new Paragraph("\nCảm ơn quý khách!\n", normalFont));

        doc.close();
        writer.close();

        System.out.println("Đã xuất hóa đơn: " + fileName);
    }

    private static PdfPCell cell(String text, Font font) {
        PdfPCell c = new PdfPCell(new Phrase(text, font));
        c.setBorder(Rectangle.NO_BORDER);
        return c;
    }
}
