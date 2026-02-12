CREATE DATABASE IF NOT EXISTS `tododb`;
USE `tododb`;

CREATE TABLE IF NOT EXISTS `task` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `completed` bit(1) NOT NULL,
  `title` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

INSERT INTO `task` (`completed`, `title`) VALUES (0, 'Test Task 1');
INSERT INTO `task` (`completed`, `title`) VALUES (1, 'Test Task 2');
