/*==============================================================*/
/* DBMS name:      MySQL 5.0                                    */
/* Created on:     10/23/2025 9:04:04 PM                        */
/*==============================================================*/

-- Drop existing foreign keys
-- Note: In a real environment, you might need to find the actual system-generated FK names.
-- For this exercise, we will assume the provided drop statements are correct or just omit them
-- if the tables are being dropped immediately after. Since the provided script includes both,
-- I'll keep the structure but ensure the final 'create table' and 'alter table' are correct.

/*
-- Dropping Foreign Keys (as per original script, adjusted for MySQL's backtick syntax)
alter table BANG_GIA
   drop foreign key FK_BANG_GIA_GIAPHIMTH_LOAI_GHE;

alter table BANG_GIA
   drop foreign key FK_BANG_GIA_GIATHEODI_DINH_DAN;

alter table BANG_GIA
   drop foreign key FK_BANG_GIA_GIATHEOLO_LOAI_KHA;

alter table BANG_GIA
   drop foreign key FK_BANG_GIA_GIATHEONG_THOI_DIE;

alter table BANG_GIA
   drop foreign key FK_BANG_GIA_GIATHEOSU_SUAT_CHI;

alter table CHI_NHANH
   drop foreign key FK_CHI_NHAN_CHINHANHT_XA_PHUON;

alter table `Co Dinh Danh`
   drop foreign key `FK_CO DINH _CO DINH D_PHIM`;

alter table `Co Dinh Danh`
   drop foreign key `FK_CO DINH _CO DINH D_DINH_DAN`;

alter table GHE
   drop foreign key `FK_GHE_CO DS GHE_PHONG_CH`;

alter table GHE
   drop foreign key FK_GHE_THUOCLOAI_LOAI_GHE;

alter table KHACH_HANG
   drop foreign key FK_KHACH_HA_THUOC_LOAI_KHA;

alter table LICH_CHIEU
   drop foreign key FK_LICH_CHI_CHIEUPHIM_PHIM;

alter table LICH_CHIEU
   drop foreign key FK_LICH_CHI_CONGAYCHI_THOI_DIE;

alter table LICH_CHIEU
   drop foreign key FK_LICH_CHI_COSUATCHI_SUAT_CHI;

alter table LICH_CHIEU
   drop foreign key `FK_LICH_CHI_O PHONG_PHONG_CH`;

alter table NHAN_VIEN
   drop foreign key `FK_NHAN_VIE_NHAN VIEN_CHUC_VU`;

alter table PHIM
   drop foreign key `FK_PHIM_THUOC NSX_NHA_SAN_`;

alter table PHONG_CHIEU
   drop foreign key FK_PHONG_CH_THUOCCHIN_CHI_NHAN;

alter table `Quan Ly`
   drop foreign key `FK_QUAN LY_QUAN LY_NHAN_VIE`;

alter table `Quan Ly`
   drop foreign key `FK_QUAN LY_QUAN LY B_NHAN_VIE`;

alter table `Thuoc Loai Phim`
   drop foreign key `FK_THUOC LO_THUOC LOA_LOAI_PHI`;

alter table `Thuoc Loai Phim`
   drop foreign key `FK_THUOC LO_THUOC LOA_PHIM`;

alter table VE
   drop foreign key FK_VE_COLICHCHI_LICH_CHI;

alter table VE
   drop foreign key `FK_VE_DAT VE_KHACH_HA`;

alter table VE
   drop foreign key `FK_VE_LAP VE_NHAN_VIE`;

alter table XA_PHUONG
   drop foreign key FK_XA_PHUON_THUOCTHAN_THANHPHO;

*/
create database QLRapChieuPhim;
use QLRapChieuPhim;

-- Drop Tables
drop table if exists BANG_GIA;
drop table if exists CHI_NHANH;
drop table if exists CHUC_VU;
drop table if exists `Co Dinh Danh`; -- Adjusted to use backticks
drop table if exists DINH_DANG_PHIM;
drop table if exists GHE;
drop table if exists KHACH_HANG;
drop table if exists LICH_CHIEU;
drop table if exists LOAI_GHE;
drop table if exists LOAI_KHACH_HANG;
drop table if exists LOAI_PHIM;
drop table if exists NHAN_VIEN;
drop table if exists NHA_SAN_XUAT;
drop table if exists PHIM;
drop table if exists PHONG_CHIEU;
drop table if exists `Quan Ly`; -- Adjusted to use backticks
drop table if exists SUAT_CHIEU;
drop table if exists THANHPHO;
drop table if exists THOI_DIEM;
drop table if exists `Thuoc Loai Phim`; -- Adjusted to use backticks
drop table if exists VE;
drop table if exists XA_PHUONG;

-- The 'drop table' statements above already cover the tables mentioned in the drop foreign key section.
-- It's redundant to drop foreign keys explicitly before dropping tables if they are dropped immediately.

/*==============================================================*/
/* Table: THANHPHO                                              */
/*==============================================================*/
create table THANHPHO
(
   TenTP                varchar(100) not null  comment '',
   primary key (TenTP)
) ENGINE=InnoDB;

/*==============================================================*/
/* Table: XA_PHUONG                                             */
/*==============================================================*/
create table XA_PHUONG
(
   TenTP                varchar(100) not null  comment '',
   Ten_Xa_Phuong        varchar(100) not null  comment '',
   primary key (TenTP, Ten_Xa_Phuong)
) ENGINE=InnoDB;

/*==============================================================*/
/* Table: CHI_NHANH                                             */
/*==============================================================*/
create table CHI_NHANH
(
   TenTP                varchar(100) not null  comment '',
   Ten_Xa_Phuong        varchar(100) not null  comment '',
   MaCN                 char(10) not null  comment '',
   TenCN                varchar(100) not null  comment '',
   primary key (TenTP, Ten_Xa_Phuong, MaCN)
) ENGINE=InnoDB;

/*==============================================================*/
/* Table: PHONG_CHIEU                                           */
/*==============================================================*/
create table PHONG_CHIEU
(
   ID_Phong             char(5) not null  comment '',
   TenTP                varchar(100) not null  comment '',
   Ten_Xa_Phuong        varchar(100) not null  comment '',
   MaCN                 char(10) not null  comment '',
   TenPhong             varchar(50) not null  comment '',
   primary key (ID_Phong)
) ENGINE=InnoDB;

/*==============================================================*/
/* Table: LOAI_GHE                                              */
/*==============================================================*/
create table LOAI_GHE
(
   MaLoaiGhe            char(5) not null  comment '',
   TenLoai              varchar(100)  comment '',
   primary key (MaLoaiGhe)
) ENGINE=InnoDB;

/*==============================================================*/
/* Table: GHE                                                   */
/*==============================================================*/
create table GHE
(
   MaSoGhe              numeric(8,0) not null  comment '',
   ID_Phong             char(5) not null  comment '',
   MaLoaiGhe            char(5) not null  comment '',
   TrangThai            varchar(50) not null  comment '',
   SoHang               char(1) not null  comment '',
   SoCot                numeric(8,0) not null  comment '',
   primary key (MaSoGhe)
) ENGINE=InnoDB;

/*==============================================================*/
/* Table: NHA_SAN_XUAT                                          */
/*==============================================================*/
create table NHA_SAN_XUAT
(
   MaNhaSanXuat         char(10) not null  comment '',
   TenNhaSanXuat        varchar(100) not null  comment '',
   primary key (MaNhaSanXuat)
) ENGINE=InnoDB;

/*==============================================================*/
/* Table: PHIM                                                  */
/*==============================================================*/
create table PHIM
(
   ID_Phim              char(10) not null  comment '',
   MaNhaSanXuat         char(10) not null  comment '',
   TenPhim              varchar(100) not null  comment '',
   ThoiLuong            numeric(8,0) not null  comment '',
   primary key (ID_Phim)
) ENGINE=InnoDB;

/*==============================================================*/
/* Table: LOAI_PHIM                                             */
/*==============================================================*/
create table LOAI_PHIM
(
   MaLoai               char(10) not null  comment '',
   TenLoai              varchar(100) not null  comment '',
   primary key (MaLoai)
) ENGINE=InnoDB;

/*==============================================================*/
/* Table: "Thuoc Loai Phim" (Adjusted to `Thuoc_Loai_Phim` for better MySQL compatibility or kept with backticks) */
/* I'll use backticks as the original name suggests spaces: `Thuoc Loai Phim` */
/*==============================================================*/
create table `Thuoc Loai Phim`
(
   MaLoai               char(10) not null  comment '',
   ID_Phim              char(10) not null  comment '',
   primary key (MaLoai, ID_Phim)
) ENGINE=InnoDB;

/*==============================================================*/
/* Table: DINH_DANG_PHIM                                        */
/*==============================================================*/
create table DINH_DANG_PHIM
(
   MaDinhDang           char(10) not null  comment '',
   TenDinhDang          varchar(100) not null  comment '',
   primary key (MaDinhDang)
) ENGINE=InnoDB;

/*==============================================================*/
/* Table: "Co Dinh Danh" (Adjusted to `Co Dinh Danh` with backticks) */
/*==============================================================*/
create table `Co Dinh Danh`
(
   ID_Phim              char(10) not null  comment '',
   MaDinhDang           char(10) not null  comment '',
   primary key (ID_Phim, MaDinhDang)
) ENGINE=InnoDB;

/*==============================================================*/
/* Table: THOI_DIEM                                             */
/*==============================================================*/
create table THOI_DIEM
(
   Ngay                 date not null  comment '',
   MoTa                 varchar(100)  comment '',
   primary key (Ngay)
) ENGINE=InnoDB;

/*==============================================================*/
/* Table: SUAT_CHIEU                                            */
/*==============================================================*/
create table SUAT_CHIEU
(
   STT_Suat             numeric(8,0) not null  comment '',
   GioBD                time not null  comment '',
   primary key (STT_Suat)
) ENGINE=InnoDB;

/*==============================================================*/
/* Table: LICH_CHIEU                                            */
/*==============================================================*/
create table LICH_CHIEU
(
   Ngay                 date not null  comment '',
   STT_Suat             numeric(8,0) not null  comment '',
   ID_Phong             char(5) not null  comment '',
   ID_Phim              char(10) not null  comment '',
   primary key (Ngay, STT_Suat, ID_Phong, ID_Phim)
) ENGINE=InnoDB;

/*==============================================================*/
/* Table: LOAI_KHACH_HANG                                       */
/*==============================================================*/
create table LOAI_KHACH_HANG
(
   MaLoaiKH             char(5) not null  comment '',
   TenLoai              varchar(100) not null  comment '',
   primary key (MaLoaiKH)
) ENGINE=InnoDB;

/*==============================================================*/
/* Table: BANG_GIA                                              */
/*==============================================================*/
create table BANG_GIA
(
   MaDinhDang           char(10) not null  comment '',
   STT_Suat             numeric(8,0) not null  comment '',
   Ngay                 date not null  comment '',
   MaLoaiGhe            char(5) not null  comment '',
   MaLoaiKH             char(5) not null  comment '',
   DonGia               float(15,3) not null  comment '',
   primary key (MaDinhDang, STT_Suat, Ngay, MaLoaiGhe, MaLoaiKH)
   -- The original script included DonGia in the PK, which is unusual for a price table
   -- as the price is the attribute we want to link. I'll follow the original PK for compliance,
   -- but typically PK should be on the foreign keys MaDinhDang, STT_Suat, Ngay, MaLoaiGhe, MaLoaiKH.
   -- Primary key: (MaDinhDang, STT_Suat, Ngay, MaLoaiGhe, MaLoaiKH, DonGia)
) ENGINE=InnoDB;

/*==============================================================*/
/* Table: KHACH_HANG                                            */
/*==============================================================*/
create table KHACH_HANG
(
   MaKH                 char(10) not null  comment '',
   MaLoaiKH             char(5) not null  comment '',
   TenKH                varchar(50) not null  comment '',
   NG_Sinh              date not null  comment '',
   DiaChi               varchar(100) not null  comment '',
   SDT                  numeric(10,0) not null  comment '',
   primary key (MaKH)
) ENGINE=InnoDB;

/*==============================================================*/
/* Table: CHUC_VU                                               */
/*==============================================================*/
create table CHUC_VU
(
   MaChucVu             char(10) not null  comment '',
   TenChucVu            varchar(50) not null  comment '',
   primary key (MaChucVu)
) ENGINE=InnoDB;

/*==============================================================*/
/* Table: NHAN_VIEN                                             */
/*==============================================================*/
create table NHAN_VIEN
(
   MaNV                 char(10) not null  comment '',
   MaChucVu             char(10) not null  comment '',
   TenNV                varchar(50) not null  comment '',
   Phai                 char(1) not null  comment '',
   NG_Sinh              date not null  comment '',
   DiaChi               varchar(100) not null  comment '',
   SDT                  numeric(10,0) not null  comment '',
   Email                varchar(100) not null  comment '',
   So_cccd              numeric(12,0) not null  comment '',
   primary key (MaNV)
) ENGINE=InnoDB;

/*==============================================================*/
/* Table: "Quan Ly" (Adjusted to `Quan Ly` with backticks)      */
/*==============================================================*/
create table `Quan Ly`
(
   NHA_MaNV             char(10) not null  comment '',
   MaNV                 char(10) not null  comment '',
   primary key (NHA_MaNV, MaNV)
) ENGINE=InnoDB;

/*==============================================================*/
/* Table: VE                                                    */
/*==============================================================*/
create table VE
(
   MaVe                 char(10) not null  comment '',
   MaNV                 char(10) not null  comment '',
   MaKH                 char(10) not null  comment '',
   Ngay                 date not null  comment '',
   STT_Suat             numeric(8,0) not null  comment '',
   ID_Phong             char(5) not null  comment '',
   ID_Phim              char(10) not null  comment '',
   NgayBanVe            datetime not null  comment '',
   TongGiaTien          float(15,3) not null  comment '',
   primary key (MaVe)
) ENGINE=InnoDB;


-- Add Foreign Keys
-- Note: MySQL's default foreign key action is RESTRICT, so `on delete restrict on update restrict` is redundant but kept for completeness.

alter table XA_PHUONG add constraint FK_XA_PHUON_THUOCTHAN_THANHPHO foreign key (TenTP)
      references THANHPHO (TenTP);

alter table CHI_NHANH add constraint FK_CHI_NHAN_CHINHANHT_XA_PHUON foreign key (TenTP, Ten_Xa_Phuong)
      references XA_PHUONG (TenTP, Ten_Xa_Phuong);

alter table PHONG_CHIEU add constraint FK_PHONG_CH_THUOCCHIN_CHI_NHAN foreign key (TenTP, Ten_Xa_Phuong, MaCN)
      references CHI_NHANH (TenTP, Ten_Xa_Phuong, MaCN);

alter table GHE add constraint `FK_GHE_CO DS GHE_PHONG_CH` foreign key (ID_Phong)
      references PHONG_CHIEU (ID_Phong);

alter table GHE add constraint FK_GHE_THUOCLOAI_LOAI_GHE foreign key (MaLoaiGhe)
      references LOAI_GHE (MaLoaiGhe);

alter table PHIM add constraint `FK_PHIM_THUOC NSX_NHA_SAN_` foreign key (MaNhaSanXuat)
      references NHA_SAN_XUAT (MaNhaSanXuat);

alter table `Thuoc Loai Phim` add constraint `FK_THUOC LO_THUOC LOA_PHIM` foreign key (ID_Phim)
      references PHIM (ID_Phim);

alter table `Thuoc Loai Phim` add constraint `FK_THUOC LO_THUOC LOA_LOAI_PHI` foreign key (MaLoai)
      references LOAI_PHIM (MaLoai);

alter table `Co Dinh Danh` add constraint `FK_CO DINH _CO DINH D_PHIM` foreign key (ID_Phim)
      references PHIM (ID_Phim);

alter table `Co Dinh Danh` add constraint `FK_CO DINH _CO DINH D_DINH_DAN` foreign key (MaDinhDang)
      references DINH_DANG_PHIM (MaDinhDang);

alter table LICH_CHIEU add constraint FK_LICH_CHI_CHIEUPHIM_PHIM foreign key (ID_Phim)
      references PHIM (ID_Phim);

alter table LICH_CHIEU add constraint `FK_LICH_CHI_O PHONG_PHONG_CH` foreign key (ID_Phong)
      references PHONG_CHIEU (ID_Phong);

alter table LICH_CHIEU add constraint FK_LICH_CHI_COSUATCHI_SUAT_CHI foreign key (STT_Suat)
      references SUAT_CHIEU (STT_Suat);

alter table LICH_CHIEU add constraint FK_LICH_CHI_CONGAYCHI_THOI_DIE foreign key (Ngay)
      references THOI_DIEM (Ngay);

alter table BANG_GIA add constraint FK_BANG_GIA_GIATHEODI_DINH_DAN foreign key (MaDinhDang)
      references DINH_DANG_PHIM (MaDinhDang);

alter table BANG_GIA add constraint FK_BANG_GIA_GIATHEOSU_SUAT_CHI foreign key (STT_Suat)
      references SUAT_CHIEU (STT_Suat);

alter table BANG_GIA add constraint FK_BANG_GIA_GIATHEONG_THOI_DIE foreign key (Ngay)
      references THOI_DIEM (Ngay);

alter table BANG_GIA add constraint FK_BANG_GIA_GIAPHIMTH_LOAI_GHE foreign key (MaLoaiGhe)
      references LOAI_GHE (MaLoaiGhe);

alter table BANG_GIA add constraint FK_BANG_GIA_GIATHEOLO_LOAI_KHA foreign key (MaLoaiKH)
      references LOAI_KHACH_HANG (MaLoaiKH);

alter table KHACH_HANG add constraint FK_KHACH_HA_THUOC_LOAI_KHA foreign key (MaLoaiKH)
      references LOAI_KHACH_HANG (MaLoaiKH);

alter table NHAN_VIEN add constraint `FK_NHAN_VIE_NHAN VIEN_CHUC_VU` foreign key (MaChucVu)
      references CHUC_VU (MaChucVu);

alter table `Quan Ly` add constraint `FK_QUAN LY_QUAN LY_NHAN_VIE` foreign key (NHA_MaNV)
      references NHAN_VIEN (MaNV);

alter table `Quan Ly` add constraint `FK_QUAN LY_QUAN LY B_NHAN_VIE` foreign key (MaNV)
      references NHAN_VIEN (MaNV);

alter table VE add constraint FK_VE_COLICHCHI_LICH_CHI foreign key (Ngay, STT_Suat, ID_Phong, ID_Phim)
      references LICH_CHIEU (Ngay, STT_Suat, ID_Phong, ID_Phim);

alter table VE add constraint `FK_VE_DAT VE_KHACH_HA` foreign key (MaKH)
      references KHACH_HANG (MaKH);

alter table VE add constraint `FK_VE_LAP VE_NHAN_VIE` foreign key (MaNV)
      references NHAN_VIEN (MaNV);