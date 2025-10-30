-- MySQL dump 10.13  Distrib 8.0.43, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: qlrapchieuphim
-- ------------------------------------------------------
-- Server version	8.4.6

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `hoa_don_quay`
--

DROP TABLE IF EXISTS `hoa_don_quay`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `hoa_don_quay` (
  `MaHDQ` char(12) NOT NULL,
  `MaNV` char(10) NOT NULL,
  `MaKH` char(10) DEFAULT NULL,
  `NgayBan` datetime NOT NULL,
  `TongTien` float(15,3) NOT NULL,
  `TenTP` varchar(100) DEFAULT NULL,
  `Ten_Xa_Phuong` varchar(100) DEFAULT NULL,
  `MaCN` char(10) DEFAULT NULL,
  `MaVe` char(10) DEFAULT NULL,
  PRIMARY KEY (`MaHDQ`),
  KEY `FK_HDQ_KH` (`MaKH`),
  KEY `FK_HDQ_VE` (`MaVe`),
  KEY `FK_HDQ_CHINHANH` (`TenTP`,`Ten_Xa_Phuong`,`MaCN`),
  KEY `IDX_HDQ_NGAY` (`NgayBan`),
  KEY `IDX_HDQ_NV` (`MaNV`),
  CONSTRAINT `FK_HDQ_CHINHANH` FOREIGN KEY (`TenTP`, `Ten_Xa_Phuong`, `MaCN`) REFERENCES `chi_nhanh` (`TenTP`, `Ten_Xa_Phuong`, `MaCN`),
  CONSTRAINT `FK_HDQ_KH` FOREIGN KEY (`MaKH`) REFERENCES `khach_hang` (`MaKH`),
  CONSTRAINT `FK_HDQ_NV` FOREIGN KEY (`MaNV`) REFERENCES `nhan_vien` (`MaNV`),
  CONSTRAINT `FK_HDQ_VE` FOREIGN KEY (`MaVe`) REFERENCES `ve` (`MaVe`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `hoa_don_quay`
--

LOCK TABLES `hoa_don_quay` WRITE;
/*!40000 ALTER TABLE `hoa_don_quay` DISABLE KEYS */;
/*!40000 ALTER TABLE `hoa_don_quay` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-10-30 10:37:59
