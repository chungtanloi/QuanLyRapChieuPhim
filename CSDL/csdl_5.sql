/*==============================================================*/
/* BỔ SUNG CHỨC NĂNG BÁN BẮP NƯỚC & COMBO                      */
/*==============================================================*/
use qlrapchieuphim;
drop table if exists CT_HOA_DON_COMBO;
drop table if exists CT_HOA_DON_SP;
drop table if exists HOA_DON_QUAY;
drop table if exists COMBO_CHI_TIET;
drop table if exists COMBO;
drop table if exists GIA_SAN_PHAM;
drop table if exists SAN_PHAM;
drop table if exists LOAI_SAN_PHAM;
drop table if exists VE_KHUYEN_MAI;
drop table if exists HDQ_KHUYEN_MAI;
drop table if exists KM_SUAT;
drop table if exists KM_LOAI_GHE;
drop table if exists KM_DINH_DANG;
drop table if exists KM_PHIM;
drop table if exists KM_COMBO;
drop table if exists KM_SAN_PHAM;
drop table if exists KM_LOAI_KH;
drop table if exists KM_CHI_NHANH;
drop table if exists KHUYEN_MAI;

 /*------------------------------------------------------------*/
 /* Danh mục & giá sản phẩm quầy (bắp, nước, snack, v.v.)      */
 /*------------------------------------------------------------*/
create table LOAI_SAN_PHAM
(
   MaLoaiSP             char(5)      not null comment '',
   TenLoai              varchar(100) not null comment '',
   primary key (MaLoaiSP)
) ENGINE=InnoDB;

create table SAN_PHAM
(
   MaSP                 char(10)      not null comment '',
   MaLoaiSP             char(5)       not null comment '',
   TenSP                varchar(100)  not null comment '',
   DonViTinh            varchar(20)   not null comment '',
   TrangThai            varchar(20)   not null comment '', -- 'DangBan','NgungBan',...
   primary key (MaSP)
) ENGINE=InnoDB;

-- Giá sản phẩm theo ngày (tận dụng bảng THOI_DIEM như phần vé)
create table GIA_SAN_PHAM
(
   MaSP                 char(10)  not null comment '',
   Ngay                 date      not null comment '',
   DonGia               float(15,3) not null comment '',
   primary key (MaSP, Ngay)
) ENGINE=InnoDB;

 /*------------------------------------------------------------*/
 /* Combo và thành phần combo                                  */
 /*------------------------------------------------------------*/
create table COMBO
(
   MaCombo              char(10)      not null comment '',
   TenCombo             varchar(100)  not null comment '',
   MoTa                 varchar(255)           comment '',
   TrangThai            varchar(20)    not null comment '', -- 'DangBan','NgungBan',...
   primary key (MaCombo)
) ENGINE=InnoDB;

create table COMBO_CHI_TIET
(
   MaCombo              char(10) not null comment '',
   MaSP                 char(10) not null comment '',
   SoLuong              int      not null comment '',
   primary key (MaCombo, MaSP)
) ENGINE=InnoDB;

 /*------------------------------------------------------------*/
 /* Hóa đơn quầy (bán bắp nước)                                */
 /* Có thể gắn với MaVe (mua kèm vé) hoặc để NULL (mua lẻ)     */
 /* Gắn chi nhánh (TenTP, Ten_Xa_Phuong, MaCN) nếu bán tại quầy*/
 /*------------------------------------------------------------*/
create table HOA_DON_QUAY
(
   MaHDQ                char(12)    not null comment '',
   MaNV                 char(10)    not null comment '',
   MaKH                 char(10)             comment '', -- cho khách thành viên; NULL nếu khách lẻ
   NgayBan              datetime    not null comment '',
   TongTien             float(15,3) not null comment '',

   -- Thông tin chi nhánh (tùy chọn, nếu cần bám theo điểm bán cụ thể)
   TenTP                varchar(100)         comment '',
   Ten_Xa_Phuong        varchar(100)         comment '',
   MaCN                 char(10)             comment '',

   -- Liên kết với vé nếu mua kèm
   MaVe                 char(10)             comment '',

   primary key (MaHDQ)
) ENGINE=InnoDB;

 /*------------------------------------------------------------*/
 /* Chi tiết hóa đơn: bán theo SẢN PHẨM                        */
 /* DonGia/ThanhTien chụp giá tại thời điểm bán                */
 /*------------------------------------------------------------*/
create table CT_HOA_DON_SP
(
   MaHDQ                char(12)    not null comment '',
   MaSP                 char(10)    not null comment '',
   SoLuong              int         not null comment '',
   DonGia               float(15,3) not null comment '',
   ThanhTien            float(15,3) not null comment '',
   primary key (MaHDQ, MaSP)
) ENGINE=InnoDB;

 /*------------------------------------------------------------*/
 /* Chi tiết hóa đơn: bán theo COMBO                           */
 /*------------------------------------------------------------*/
create table CT_HOA_DON_COMBO
(
   MaHDQ                char(12)    not null comment '',
   MaCombo              char(10)    not null comment '',
   SoLuong              int         not null comment '',
   DonGia               float(15,3) not null comment '',
   ThanhTien            float(15,3) not null comment '',
   primary key (MaHDQ, MaCombo)
) ENGINE=InnoDB;
create table KHUYEN_MAI
(
  MaKM             char(10)       not null comment '',
  TenKM            varchar(150)   not null comment '',
  MoTa             varchar(300)            comment '',
  LoaiKM           varchar(10)    not null comment '', -- 'PERCENT' | 'AMOUNT'
  GiaTri           float(15,3)    not null comment '', -- % hoặc số tiền
  GioiHanGiamToiDa float(15,3)            comment '', -- optional cap cho PERCENT
  TuNgay           date           not null comment '',
  DenNgay          date           not null comment '',
  GioBD            time                    comment '', -- optional: áp theo khung giờ
  GioKT            time                    comment '',
  Kenh             varchar(10)    not null comment '', -- 'ONLINE'|'OFFLINE'|'ALL'
  ApDungToanHeThong tinyint(1)    not null default 0 comment '', -- 1: không cần chọn chi nhánh
  ChoPhepCongDon    tinyint(1)    not null default 0 comment '', -- cho phép cộng dồn với KM khác
  TrangThai         varchar(15)   not null comment '', -- 'HIEULUC'|'TAMNGUNG'|'HETHAN'
  -- Điều kiện tối thiểu (bill-level)
  MinTongTien      float(15,3)             comment '',
  MinSoLuong       int                     comment '',
  -- Giới hạn sử dụng
  MaxLanSuDungToanHeThong int              comment '',
  MaxLanSuDungMoiKH      int              comment '',
  primary key (MaKM)
) ENGINE=InnoDB;

-- Phạm vi áp dụng
create table KM_CHI_NHANH
(
  MaKM             char(10)      not null,
  TenTP            varchar(100)  not null,
  Ten_Xa_Phuong    varchar(100)  not null,
  MaCN             char(10)      not null,
  primary key (MaKM, TenTP, Ten_Xa_Phuong, MaCN)
) ENGINE=InnoDB;

create table KM_LOAI_KH
(
  MaKM        char(10) not null,
  MaLoaiKH    char(5)  not null,
  primary key (MaKM, MaLoaiKH)
) ENGINE=InnoDB;

-- Áp cho vé theo nội dung trình chiếu
create table KM_PHIM
(
  MaKM        char(10) not null,
  ID_Phim     char(10) not null,
  primary key (MaKM, ID_Phim)
) ENGINE=InnoDB;

create table KM_DINH_DANG
(
  MaKM        char(10)  not null,
  MaDinhDang  char(10)  not null,
  primary key (MaKM, MaDinhDang)
) ENGINE=InnoDB;

create table KM_LOAI_GHE
(
  MaKM        char(10)  not null,
  MaLoaiGhe   char(5)   not null,
  primary key (MaKM, MaLoaiGhe)
) ENGINE=InnoDB;

create table KM_SUAT
(
  MaKM        char(10) not null,
  STT_Suat    numeric(8,0) not null,
  primary key (MaKM, STT_Suat)
) ENGINE=InnoDB;

-- Áp cho bán quầy
create table KM_SAN_PHAM
(
  MaKM        char(10) not null,
  MaSP        char(10) not null,
  primary key (MaKM, MaSP)
) ENGINE=InnoDB;

create table KM_COMBO
(
  MaKM        char(10) not null,
  MaCombo     char(10) not null,
  primary key (MaKM, MaCombo)
) ENGINE=InnoDB;

-- Lưu vết khuyến mãi áp dụng trên hóa đơn quầy
create table HDQ_KHUYEN_MAI
(
  MaHDQ           char(12)    not null,
  MaKM            char(10)    not null,
  SoTienGiam      float(15,3) not null,
  ThanhTienSauGiam float(15,3) not null,
  primary key (MaHDQ, MaKM)
) ENGINE=InnoDB;

-- Lưu vết khuyến mãi áp dụng trên vé
create table VE_KHUYEN_MAI
(
  MaVe            char(10)    not null,
  MaKM            char(10)    not null,
  SoTienGiam      float(15,3) not null,
  TongTienSauGiam float(15,3) not null,
  primary key (MaVe, MaKM)
) ENGINE=InnoDB;

 /*==============================================================*/
 /* Ràng buộc khóa ngoại                                         */
 /*==============================================================*/

-- Sản phẩm & loại
alter table SAN_PHAM
  add constraint FK_SP_THUOC_LOAI_LOAI_SP
  foreign key (MaLoaiSP) references LOAI_SAN_PHAM (MaLoaiSP);

-- Giá sản phẩm bám sản phẩm & ngày (THOI_DIEM)
alter table GIA_SAN_PHAM
  add constraint FK_GIA_SP_SP
  foreign key (MaSP) references SAN_PHAM (MaSP);

alter table GIA_SAN_PHAM
  add constraint FK_GIA_SP_NGAY
  foreign key (Ngay) references THOI_DIEM (Ngay);

-- Combo chi tiết bám combo & sản phẩm
alter table COMBO_CHI_TIET
  add constraint FK_CBCT_COMBO
  foreign key (MaCombo) references COMBO (MaCombo);

alter table COMBO_CHI_TIET
  add constraint FK_CBCT_SP
  foreign key (MaSP) references SAN_PHAM (MaSP);

-- Hóa đơn quầy bám nhân viên, khách hàng (tùy chọn), vé (tùy chọn), chi nhánh (tùy chọn)
alter table HOA_DON_QUAY
  add constraint FK_HDQ_NV
  foreign key (MaNV) references NHAN_VIEN (MaNV);

alter table HOA_DON_QUAY
  add constraint FK_HDQ_KH
  foreign key (MaKH) references KHACH_HANG (MaKH);

alter table HOA_DON_QUAY
  add constraint FK_HDQ_VE
  foreign key (MaVe) references VE (MaVe);

alter table HOA_DON_QUAY
  add constraint FK_HDQ_CHINHANH
  foreign key (TenTP, Ten_Xa_Phuong, MaCN)
  references CHI_NHANH (TenTP, Ten_Xa_Phuong, MaCN);

-- Chi tiết hóa đơn theo sản phẩm
alter table CT_HOA_DON_SP
  add constraint FK_CTHDSP_HDQ
  foreign key (MaHDQ) references HOA_DON_QUAY (MaHDQ);

alter table CT_HOA_DON_SP
  add constraint FK_CTHDSP_SP
  foreign key (MaSP) references SAN_PHAM (MaSP);

-- Chi tiết hóa đơn theo combo
alter table CT_HOA_DON_COMBO
  add constraint FK_CTHDCB_HDQ
  foreign key (MaHDQ) references HOA_DON_QUAY (MaHDQ);

alter table CT_HOA_DON_COMBO
  add constraint FK_CTHDCB_COMBO
  foreign key (MaCombo) references COMBO (MaCombo);
alter table KM_CHI_NHANH
  add constraint FK_KM_CN_KM foreign key (MaKM) references KHUYEN_MAI(MaKM),
  add constraint FK_KM_CN_CN foreign key (TenTP, Ten_Xa_Phuong, MaCN)
    references CHI_NHANH (TenTP, Ten_Xa_Phuong, MaCN);

alter table KM_LOAI_KH
  add constraint FK_KM_LKH_KM foreign key (MaKM) references KHUYEN_MAI(MaKM),
  add constraint FK_KM_LKH_LKH foreign key (MaLoaiKH) references LOAI_KHACH_HANG(MaLoaiKH);

alter table KM_PHIM
  add constraint FK_KM_PHIM_KM foreign key (MaKM) references KHUYEN_MAI(MaKM),
  add constraint FK_KM_PHIM_PHIM foreign key (ID_Phim) references PHIM(ID_Phim);

alter table KM_DINH_DANG
  add constraint FK_KM_DD_KM foreign key (MaKM) references KHUYEN_MAI(MaKM),
  add constraint FK_KM_DD_DD foreign key (MaDinhDang) references DINH_DANG_PHIM(MaDinhDang);

alter table KM_LOAI_GHE
  add constraint FK_KM_LG_KM foreign key (MaKM) references KHUYEN_MAI(MaKM),
  add constraint FK_KM_LG_LG foreign key (MaLoaiGhe) references LOAI_GHE(MaLoaiGhe);

alter table KM_SUAT
  add constraint FK_KM_SUAT_KM foreign key (MaKM) references KHUYEN_MAI(MaKM),
  add constraint FK_KM_SUAT_SUAT foreign key (STT_Suat) references SUAT_CHIEU(STT_Suat);

alter table KM_SAN_PHAM
  add constraint FK_KM_SP_KM foreign key (MaKM) references KHUYEN_MAI(MaKM),
  add constraint FK_KM_SP_SP foreign key (MaSP) references SAN_PHAM(MaSP);

alter table KM_COMBO
  add constraint FK_KM_CB_KM foreign key (MaKM) references KHUYEN_MAI(MaKM),
  add constraint FK_KM_CB_CB foreign key (MaCombo) references COMBO(MaCombo);

alter table HDQ_KHUYEN_MAI
  add constraint FK_HDQKM_HDQ foreign key (MaHDQ) references HOA_DON_QUAY(MaHDQ),
  add constraint FK_HDQKM_KM  foreign key (MaKM) references KHUYEN_MAI(MaKM);

alter table VE_KHUYEN_MAI
  add constraint FK_VEKM_VE foreign key (MaVe) references VE(MaVe),
  add constraint FK_VEKM_KM foreign key (MaKM) references KHUYEN_MAI(MaKM);

/*================= Chỉ mục gợi ý =================*/
create index IDX_KM_TG on KHUYEN_MAI (TuNgay, DenNgay, TrangThai);
create index IDX_KM_KENH on KHUYEN_MAI (Kenh);
create index IDX_VEKM_MAVE on VE_KHUYEN_MAI (MaVe);
create index IDX_HDQKM_MAHDQ on HDQ_KHUYEN_MAI (MaHDQ);
 /*==============================================================*/
 /* Gợi ý chỉ mục thực tế (tùy chọn, giúp tra cứu nhanh)        */
 /*==============================================================*/
create index IDX_SP_TEN on SAN_PHAM (TenSP);
create index IDX_GIA_SP_NGAY on GIA_SAN_PHAM (MaSP, Ngay);
create index IDX_HDQ_NGAY on HOA_DON_QUAY (NgayBan);
create index IDX_HDQ_NV on HOA_DON_QUAY (MaNV);

 /*==============================================================*/
 /* Dữ liệu       */
 /*==============================================================*/
/* ================== CÁC DANH MỤC CƠ BẢN ================== */
-- Địa điểm/tổ chức tối thiểu để lên lịch chiếu
insert into THANHPHO (TenTP) values ('TPHCM') on duplicate key update TenTP=values(TenTP);
insert into XA_PHUONG (TenTP, Ten_Xa_Phuong) values ('TPHCM','Bến Nghé')
  on duplicate key update Ten_Xa_Phuong=values(Ten_Xa_Phuong);
insert into CHI_NHANH (TenTP, Ten_Xa_Phuong, MaCN, TenCN) values
 ('TPHCM','Bến Nghé','CN001','CGV Nguyễn Huệ')
on duplicate key update TenCN=values(TenCN);

insert into PHONG_CHIEU (ID_Phong, TenTP, Ten_Xa_Phuong, MaCN, TenPhong) values
 ('P001','TPHCM','Bến Nghé','CN001','PHÒNG 1')
on duplicate key update TenPhong=values(TenPhong);

-- Ghế, loại ghế, loại KH
insert into LOAI_GHE (MaLoaiGhe, TenLoai) values
 ('LG001','Standard'),
 ('LG002','VIP')
on duplicate key update TenLoai=values(TenLoai);

insert into LOAI_KHACH_HANG (MaLoaiKH, TenLoai) values
 ('LKH01','Người lớn'),
 ('LKH02','HSSV')
on duplicate key update TenLoai=values(TenLoai);

-- Suất chiếu cơ bản
insert into SUAT_CHIEU (STT_Suat, GioBD) values
 (1,'10:00:00'),(2,'13:30:00'),(3,'16:00:00'),(4,'19:30:00'),(5,'21:45:00')
on duplicate key update GioBD=values(GioBD);

-- Các mốc ngày dùng cho giá/lịch (10/2025)
insert into THOI_DIEM (Ngay, MoTa) values
 ('2025-10-03','Đầu tháng 10'),
 ('2025-10-10','Tuần 2'),
 ('2025-10-17','Tuần 3'),
 ('2025-10-24','Tuần 4'),
 ('2025-10-30','Hôm nay'),
 ('2025-10-31','Cuối tháng')
on duplicate key update MoTa=values(MoTa);

/* ================== DINH DẠNG PHIM ================== */
insert into DINH_DANG_PHIM (MaDinhDang, TenDinhDang) values
 ('DD2D','2D'),
 ('DD3D','3D')
on duplicate key update TenDinhDang=values(TenDinhDang);

/* ================== NHÀ SẢN XUẤT (tạm gom đơn giản) ================== */
insert into NHA_SAN_XUAT (MaNhaSanXuat, TenNhaSanXuat) values
 ('NSX001','CJ CGV'),
 ('NSX002','Galaxy Studio'),
 ('NSX003','National Cinema Center VN'),
 ('NSX004','Quốc tế/Khác')
on duplicate key update TenNhaSanXuat=values(TenNhaSanXuat);

/* ================== PHIM ĐANG CHIẾU 10/2025 (có thời lượng) ==================
   Nguồn: CGV "Now Showing", Trung tâm Chiếu phim Quốc gia, MoMo/Galaxy | tham chiếu bên dưới */
insert into PHIM (ID_Phim, MaNhaSanXuat, TenPhim, ThoiLuong) values
 ('P0001','NSX001','Phá Đám - Sinh Nhật Mẹ',91),     -- CGV: 91 phút
 ('P0002','NSX004','Dolphin Boy 2',96),               -- CGV: 96 phút
 ('P0003','NSX002','Cục Vàng Của Ngoại',119),         -- CGV: 119 phút
 ('P0004','NSX003','Xà Thuật Tiểu Tam',94),           -- NCC: 94 phút
 ('P0005','NSX003','Phỏng Vấn Sát Nhân',107),         -- NCC: 107 phút
 ('P0006','NSX003','Cải Mả',115)                      -- NCC: 115 phút
on duplicate key update TenPhim=values(TenPhim), ThoiLuong=values(ThoiLuong);

/* Phim–Định dạng: tạm thời map tất cả sang 2D */
insert into `Co Dinh Danh` (ID_Phim, MaDinhDang) values
 ('P0001','DD2D'),('P0002','DD2D'),('P0003','DD2D'),
 ('P0004','DD2D'),('P0005','DD2D'),('P0006','DD2D')
on duplicate key update MaDinhDang=values(MaDinhDang);

/* ================== LỊCH CHIẾU MẪU (Hôm nay & ngày 31/10) ================== */
insert into LICH_CHIEU (Ngay, STT_Suat, ID_Phong, ID_Phim) values
 ('2025-10-30',1,'P001','P0003'),
 ('2025-10-30',3,'P001','P0001'),
 ('2025-10-30',4,'P001','P0005'),
 ('2025-10-31',2,'P001','P0001'),
 ('2025-10-31',4,'P001','P0006'),
 ('2025-10-31',5,'P001','P0004')
on duplicate key update ID_Phim=values(ID_Phim);

/* ================== BẢNG GIÁ VÉ (mẫu) ==================
   Giá theo: định dạng 2D, suất, ngày, loại ghế, loại KH */
-- Giá ngày 30/10
insert into BANG_GIA (MaDinhDang, STT_Suat, Ngay, MaLoaiGhe, MaLoaiKH, DonGia) values
 ('DD2D',1,'2025-10-30','LG001','LKH01',70000.000),
 ('DD2D',1,'2025-10-30','LG001','LKH02',60000.000),
 ('DD2D',1,'2025-10-30','LG002','LKH01',90000.000),
 ('DD2D',3,'2025-10-30','LG001','LKH01',80000.000),
 ('DD2D',4,'2025-10-30','LG002','LKH01',110000.000);
-- Giá ngày 31/10 (cuối tuần tăng nhẹ)
insert into BANG_GIA (MaDinhDang, STT_Suat, Ngay, MaLoaiGhe, MaLoaiKH, DonGia) values
 ('DD2D',2,'2025-10-31','LG001','LKH01',90000.000),
 ('DD2D',2,'2025-10-31','LG001','LKH02',80000.000),
 ('DD2D',4,'2025-10-31','LG002','LKH01',130000.000),
 ('DD2D',5,'2025-10-31','LG001','LKH01',100000.000);

/* ================== QUẦY BẮP NƯỚC / COMBO ================== */
insert into LOAI_SAN_PHAM (MaLoaiSP, TenLoai) values
 ('FOOD','Đồ ăn'),('DRINK','Đồ uống')
on duplicate key update TenLoai=values(TenLoai);

insert into SAN_PHAM (MaSP, MaLoaiSP, TenSP, DonViTinh, TrangThai) values
 ('PPC01','FOOD','Bắp rang bơ','Phần','DangBan'),
 ('SOD01','DRINK','Nước ngọt cỡ M','Ly','DangBan'),
 ('SOD02','DRINK','Nước ngọt cỡ L','Ly','DangBan')
on duplicate key update TenSP=values(TenSP), DonViTinh=values(DonViTinh), TrangThai=values(TrangThai);

-- Giá theo ngày
insert into GIA_SAN_PHAM (MaSP, Ngay, DonGia) values
 ('PPC01','2025-10-30',45000.000),
 ('SOD01','2025-10-30',35000.000),
 ('SOD02','2025-10-30',40000.000),
 ('PPC01','2025-10-31',49000.000),
 ('SOD01','2025-10-31',38000.000),
 ('SOD02','2025-10-31',43000.000)
on duplicate key update DonGia=values(DonGia);

-- Combo
insert into COMBO (MaCombo, TenCombo, MoTa, TrangThai) values
 ('CB01','Combo Couple','1 bắp + 2 nước L','DangBan')
on duplicate key update TenCombo=values(TenCombo), MoTa=values(MoTa), TrangThai=values(TrangThai);

insert into COMBO_CHI_TIET (MaCombo, MaSP, SoLuong) values
 ('CB01','PPC01',1),
 ('CB01','SOD02',2)
on duplicate key update SoLuong=values(SoLuong);

/* ================== KHUYẾN MÃI (áp cho cả vé & quầy) ================== */
-- KM vé: -10% cho HSSV, áp trong 25-31/10, kênh ALL, không cộng dồn
insert into KHUYEN_MAI
 (MaKM, TenKM, MoTa, LoaiKM, GiaTri, GioiHanGiamToiDa, TuNgay, DenNgay, GioBD, GioKT, Kenh, ApDungToanHeThong, ChoPhepCongDon, TrangThai, MinTongTien, MinSoLuong, MaxLanSuDungToanHeThong, MaxLanSuDungMoiKH)
values
 ('KMV10','Giảm vé HSSV 10%','HSSV giảm 10% tất cả suất 2D','PERCENT',10.000,20000.000,
  '2025-10-25','2025-10-31',NULL,NULL,'ALL',1,0,'HIEULUC',NULL,NULL,NULL,NULL)
on duplicate key update TenKM=values(TenKM), GiaTri=values(GiaTri), DenNgay=values(DenNgay), TrangThai=values(TrangThai);

insert into KM_LOAI_KH (MaKM, MaLoaiKH) values ('KMV10','LKH02')
on duplicate key update MaLoaiKH=values(MaLoaiKH);

-- KM combo: trừ 20,000 cho CB01, áp 28-31/10, OFFLINE (tại quầy), cho phép cộng dồn
insert into KHUYEN_MAI
 (MaKM, TenKM, MoTa, LoaiKM, GiaTri, GioiHanGiamToiDa, TuNgay, DenNgay, GioBD, GioKT, Kenh, ApDungToanHeThong, ChoPhepCongDon, TrangThai, MinTongTien, MinSoLuong, MaxLanSuDungToanHeThong, MaxLanSuDungMoiKH)
values
 ('KMCB20K','Combo -20K','Giảm 20K khi mua Combo Couple','AMOUNT',20000.000,NULL,
  '2025-10-28','2025-10-31',NULL,NULL,'OFFLINE',1,1,'HIEULUC',NULL,1,NULL,NULL)
on duplicate key update TenKM=values(TenKM), GiaTri=values(GiaTri), DenNgay=values(DenNgay), TrangThai=values(TrangThai);

insert into KM_COMBO (MaKM, MaCombo) values ('KMCB20K','CB01')
on duplicate key update MaCombo=values(MaCombo);

-- KM quầy: -10% cho đồ uống, áp 30-31/10, ALL, cộng dồn
insert into KHUYEN_MAI
 (MaKM, TenKM, MoTa, LoaiKM, GiaTri, GioiHanGiamToiDa, TuNgay, DenNgay, GioBD, GioKT, Kenh, ApDungToanHeThong, ChoPhepCongDon, TrangThai, MinTongTien, MinSoLuong, MaxLanSuDungToanHeThong, MaxLanSuDungMoiKH)
values
 ('KMDU10','Đồ uống -10%','Giảm 10% đồ uống tại quầy','PERCENT',10.000,NULL,
  '2025-10-30','2025-10-31',NULL,NULL,'ALL',1,1,'HIEULUC',NULL,NULL,NULL,NULL)
on duplicate key update TenKM=values(TenKM), GiaTri=values(GiaTri), DenNgay=values(DenNgay), TrangThai=values(TrangThai);

insert into KM_SAN_PHAM (MaKM, MaSP) values
 ('KMDU10','SOD01'), ('KMDU10','SOD02')
on duplicate key update MaSP=values(MaSP);


