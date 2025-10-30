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
-- Table structure for table `khuyen_mai`
--

DROP TABLE IF EXISTS `khuyen_mai`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `khuyen_mai` (
  `MaKM` char(10) NOT NULL,
  `TenKM` varchar(150) NOT NULL,
  `MoTa` varchar(300) DEFAULT NULL,
  `LoaiKM` varchar(10) NOT NULL,
  `GiaTri` float(15,3) NOT NULL,
  `GioiHanGiamToiDa` float(15,3) DEFAULT NULL,
  `TuNgay` date NOT NULL,
  `DenNgay` date NOT NULL,
  `GioBD` time DEFAULT NULL,
  `GioKT` time DEFAULT NULL,
  `Kenh` varchar(10) NOT NULL,
  `ApDungToanHeThong` tinyint(1) NOT NULL DEFAULT '0',
  `ChoPhepCongDon` tinyint(1) NOT NULL DEFAULT '0',
  `TrangThai` varchar(15) NOT NULL,
  `MinTongTien` float(15,3) DEFAULT NULL,
  `MinSoLuong` int DEFAULT NULL,
  `MaxLanSuDungToanHeThong` int DEFAULT NULL,
  `MaxLanSuDungMoiKH` int DEFAULT NULL,
  PRIMARY KEY (`MaKM`),
  KEY `IDX_KM_TG` (`TuNgay`,`DenNgay`,`TrangThai`),
  KEY `IDX_KM_KENH` (`Kenh`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `khuyen_mai`
--

LOCK TABLES `khuyen_mai` WRITE;
/*!40000 ALTER TABLE `khuyen_mai` DISABLE KEYS */;
INSERT INTO `khuyen_mai` VALUES ('KMCB20K','Combo -20K','Giảm 20K khi mua Combo Couple','AMOUNT',20000.000,NULL,'2025-10-28','2025-10-31',NULL,NULL,'OFFLINE',1,1,'HIEULUC',NULL,1,NULL,NULL),('KMDU10','Đồ uống -10%','Giảm 10% đồ uống tại quầy','PERCENT',10.000,NULL,'2025-10-30','2025-10-31',NULL,NULL,'ALL',1,1,'HIEULUC',NULL,NULL,NULL,NULL),('KMV10','Giảm vé HSSV 10%','HSSV giảm 10% tất cả suất 2D','PERCENT',10.000,20000.000,'2025-10-25','2025-10-31',NULL,NULL,'ALL',1,0,'HIEULUC',NULL,NULL,NULL,NULL);
/*!40000 ALTER TABLE `khuyen_mai` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-10-30 10:38:01
