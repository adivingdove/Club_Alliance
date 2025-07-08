-- MySQL dump 10.13  Distrib 8.0.42, for Win64 (x86_64)
--
-- Host: localhost    Database: uclub
-- ------------------------------------------------------
-- Server version	8.0.42

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

-- Table structure for table `announcement`
DROP TABLE IF EXISTS `announcement`;
CREATE TABLE `announcement` (
  `id` int NOT NULL AUTO_INCREMENT,
  `club_id` int DEFAULT NULL COMMENT '����ID��ΪNULL��ʾϵͳ����',
  `title` varchar(100) COLLATE utf8mb4_general_ci NOT NULL COMMENT '�������',
  `type` enum('ϵͳ','����') COLLATE utf8mb4_general_ci NOT NULL COMMENT '��������',
  `content` text COLLATE utf8mb4_general_ci NOT NULL COMMENT '��������',
  `creator_id` int NOT NULL COMMENT '������ID',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '����ʱ��',
  PRIMARY KEY (`id`),
  KEY `club_id` (`club_id`),
  KEY `creator_id` (`creator_id`),
  CONSTRAINT `announcement_ibfk_1` FOREIGN KEY (`club_id`) REFERENCES `club` (`id`) ON DELETE SET NULL,
  CONSTRAINT `announcement_ibfk_2` FOREIGN KEY (`creator_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- Table structure for table `club`
DROP TABLE IF EXISTS `club`;
CREATE TABLE `club` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(100) COLLATE utf8mb4_general_ci NOT NULL COMMENT '��������',
  `logo_url` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '���� Logo',
  `tags` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '��ǩ�����ŷָ���',
  `creator_id` int NOT NULL COMMENT '�������û�ID',
  `description` text COLLATE utf8mb4_general_ci COMMENT '���ż��',
  `status` enum('����','�����','�ѷ��') COLLATE utf8mb4_general_ci DEFAULT '�����' COMMENT '����״̬',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '����ʱ��',
  PRIMARY KEY (`id`),
  KEY `creator_id` (`creator_id`),
  CONSTRAINT `club_ibfk_1` FOREIGN KEY (`creator_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- Table structure for table `club_activity`
DROP TABLE IF EXISTS `club_activity`;
CREATE TABLE `club_activity` (
  `id` int NOT NULL AUTO_INCREMENT,
  `club_id` int NOT NULL COMMENT '��������ID',
  `title` varchar(100) COLLATE utf8mb4_general_ci NOT NULL COMMENT '�����',
  `description` text COLLATE utf8mb4_general_ci COMMENT '����ݼ��',
  `location` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '��ص�',
  `start_time` datetime NOT NULL COMMENT '��ʼʱ��',
  `end_time` datetime NOT NULL COMMENT '����ʱ��',
  `max_participants` int DEFAULT NULL COMMENT '����������',
  `apply_status` enum('�����','ͨ��','�ܾ�') COLLATE utf8mb4_general_ci DEFAULT '�����' COMMENT '����״̬',
  `creator_id` int NOT NULL COMMENT '������ID',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '����ʱ��',
  PRIMARY KEY (`id`),
  KEY `club_id` (`club_id`),
  KEY `creator_id` (`creator_id`),
  CONSTRAINT `club_activity_ibfk_1` FOREIGN KEY (`club_id`) REFERENCES `club` (`id`) ON DELETE CASCADE,
  CONSTRAINT `club_activity_ibfk_2` FOREIGN KEY (`creator_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- �����ֶ� image_url
ALTER TABLE `club_activity` ADD COLUMN `image_url` VARCHAR(255) DEFAULT NULL COMMENT '�ͼƬ';

-- Table structure for table `club_member`
DROP TABLE IF EXISTS `club_member`;
CREATE TABLE `club_member` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL COMMENT '�û�ID',
  `club_id` int NOT NULL COMMENT '����ID',
  `role` enum('��Ա','����','���糤','�糤') COLLATE utf8mb4_general_ci DEFAULT '��Ա' COMMENT '���ڽ�ɫ',
  `join_status` enum('�����','��ͨ��','�Ѿܾ�') COLLATE utf8mb4_general_ci DEFAULT '�����' COMMENT '����״̬',
  `joined_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '����ʱ��',
  PRIMARY KEY (`id`),
  KEY `user_id` (`user_id`),
  KEY `club_id` (`club_id`),
  CONSTRAINT `club_member_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `club_member_ibfk_2` FOREIGN KEY (`club_id`) REFERENCES `club` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- Table structure for table `comment`
DROP TABLE IF EXISTS `comment`;
CREATE TABLE `comment` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `post_id` BIGINT NOT NULL,
  `user_id` BIGINT NOT NULL,
  `content` TEXT COLLATE utf8mb4_general_ci NOT NULL,
  `status` ENUM('active','deleted','hidden','violated') COLLATE utf8mb4_general_ci DEFAULT 'active',
  `like_count` INT DEFAULT '0',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `parent_comment_id` BIGINT DEFAULT NULL COMMENT '���ظ�������ID��Ϊ�ձ�ʾ��һ�����ۣ�',
  PRIMARY KEY (`id`),
  KEY `idx_parent_comment` (`parent_comment_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- Table structure for table `like`
DROP TABLE IF EXISTS `like`;
CREATE TABLE `like` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `target_type` enum('post','comment') COLLATE utf8mb4_general_ci NOT NULL,
  `target_id` bigint NOT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_user_target` (`user_id`,`target_type`,`target_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- Table structure for table `post`
DROP TABLE IF EXISTS `post`;
CREATE TABLE `post` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `club_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `title` varchar(100) COLLATE utf8mb4_general_ci NOT NULL,
  `content` text COLLATE utf8mb4_general_ci NOT NULL,
  `image_urls` json DEFAULT NULL,
  `status` enum('active','deleted','hidden','violated') COLLATE utf8mb4_general_ci DEFAULT 'active',
  `like_count` int DEFAULT '0',
  `comment_count` int DEFAULT '0',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- Table structure for table `report`
DROP TABLE IF EXISTS `report`;
CREATE TABLE `report` (
  `id` int NOT NULL AUTO_INCREMENT,
  `reporter_id` int NOT NULL COMMENT '�ٱ���ID',
  `target_type` enum('����','����','�û�','�','����') COLLATE utf8mb4_general_ci NOT NULL COMMENT '�ٱ���������',
  `target_id` int NOT NULL COMMENT '��Ӧ����ID',
  `reason` text COLLATE utf8mb4_general_ci COMMENT '�ٱ�����',
  `status` enum('������','�Ѵ���') COLLATE utf8mb4_general_ci DEFAULT '������' COMMENT '����״̬',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '�ٱ�ʱ��',
  PRIMARY KEY (`id`),
  KEY `reporter_id` (`reporter_id`),
  CONSTRAINT `report_ibfk_1` FOREIGN KEY (`reporter_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- Table structure for table `user`
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id` int NOT NULL AUTO_INCREMENT,
  `email` varchar(100) COLLATE utf8mb4_general_ci NOT NULL COMMENT '�����ַ',
  `password` varchar(100) COLLATE utf8mb4_general_ci NOT NULL COMMENT '��������',
  `nickname` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '�ǳ�',
  `avatar_url` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'ͷ�� URL',
  `role` enum('��ͨ�û�','���Ź���Ա','ϵͳ����Ա') COLLATE utf8mb4_general_ci DEFAULT '��ͨ�û�' COMMENT '�û���ɫ',
  `status` enum('����','����','���') COLLATE utf8mb4_general_ci DEFAULT '����' COMMENT '�˺�״̬',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '����ʱ��',
  PRIMARY KEY (`id`),
  UNIQUE KEY `email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- Table structure for table `chat_message`
CREATE TABLE `chat_message` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `room` VARCHAR(50) NOT NULL COMMENT '������ ID���� public �� club-1',
  `sender_id` BIGINT NOT NULL COMMENT '������ user_id',
  `sender_name` VARCHAR(100) DEFAULT NULL COMMENT '�ǳ�',
  `avatar_url` VARCHAR(255) DEFAULT NULL COMMENT 'ͷ�� URL',
  `content` TEXT NOT NULL COMMENT '��Ϣ����',
  `role` VARCHAR(20) DEFAULT NULL COMMENT '��ɫ���� �糤',
  `time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '����ʱ��',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- �ָ�����
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;
/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-06-24 19:51:53
