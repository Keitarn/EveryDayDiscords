-- phpMyAdmin SQL Dump
-- version 4.8.5
-- https://www.phpmyadmin.net/
--
-- Hôte : 127.0.0.1:3306
-- Généré le :  lun. 21 oct. 2019 à 19:56
-- Version du serveur :  5.7.26
-- Version de PHP :  7.2.18

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET AUTOCOMMIT = 0;
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Base de données :  `keitarn`
--

-- --------------------------------------------------------

--
-- Structure de la table `requete`
--

DROP TABLE IF EXISTS `requete`;
CREATE TABLE IF NOT EXISTS `requete` (
  `dateRequete` varchar(255) NOT NULL,
  `TypeRequete` varchar(255) NOT NULL,
  `idUser` varchar(50) NOT NULL,
  PRIMARY KEY (`TypeRequete`,`idUser`),
  KEY `RequeteUser_User_FK` (`idUser`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Déchargement des données de la table `requete`
--

INSERT INTO `requete` (`dateRequete`, `TypeRequete`, `idUser`) VALUES
('1571675721787', '!ask_lamas', '167251064024727552'),
('1571472309079', '!ask_lamas', '174915295767429120'),
('1571614220086', '!ask_lamas', '177876925757390855'),
('1571580310548', '!ask_lamas', '188733789202022401'),
('1571657917662', '!ask_lamas', '195601829973852160'),
('1571522351877', '!ask_lamas', '201078052086611968'),
('1571686496119', '!ask_lamas', '252208361054011393'),
('1571657080571', '!ask_lamas', '292585648593043466'),
('1571440879720', '!ask_lamas', '320502749756129280'),
('1571676107239', '!ask_lamas', '331473028821155840'),
('1571686728673', '!ask_lamas', '486633414938722356'),
('1571682491314', '!ask_lamas', '611658274445721618');

--
-- Contraintes pour les tables déchargées
--

--
-- Contraintes pour la table `requete`
--
ALTER TABLE `requete`
  ADD CONSTRAINT `RequeteUser_User_FK` FOREIGN KEY (`idUser`) REFERENCES `user` (`idUser`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
