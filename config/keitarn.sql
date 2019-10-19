-- phpMyAdmin SQL Dump
-- version 4.8.5
-- https://www.phpmyadmin.net/
--
-- Hôte : 127.0.0.1:3306
-- Généré le :  sam. 19 oct. 2019 à 09:12
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
-- Structure de la table `chanel`
--

DROP TABLE IF EXISTS `chanel`;
CREATE TABLE IF NOT EXISTS `chanel` (
  `idChanel` varchar(255) NOT NULL,
  `abonne` tinyint(1) NOT NULL,
  `IdGuild` varchar(255) NOT NULL,
  PRIMARY KEY (`idChanel`),
  KEY `Chanel_Guild_FK` (`IdGuild`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Structure de la table `clientuser`
--

DROP TABLE IF EXISTS `clientuser`;
CREATE TABLE IF NOT EXISTS `clientuser` (
  `idUser` varchar(50) NOT NULL,
  `IdGuild` varchar(255) NOT NULL,
  PRIMARY KEY (`idUser`,`IdGuild`),
  KEY `ClientUser_Guild0_FK` (`IdGuild`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Structure de la table `guild`
--

DROP TABLE IF EXISTS `guild`;
CREATE TABLE IF NOT EXISTS `guild` (
  `IdGuild` varchar(255) NOT NULL,
  PRIMARY KEY (`IdGuild`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Structure de la table `image`
--

DROP TABLE IF EXISTS `image`;
CREATE TABLE IF NOT EXISTS `image` (
  `nomImage` varchar(255) NOT NULL,
  PRIMARY KEY (`nomImage`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Structure de la table `lie`
--

DROP TABLE IF EXISTS `lie`;
CREATE TABLE IF NOT EXISTS `lie` (
  `nomImage` varchar(255) NOT NULL,
  `IdGuild` varchar(255) NOT NULL,
  PRIMARY KEY (`nomImage`,`IdGuild`),
  KEY `lie_Guild0_FK` (`IdGuild`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Structure de la table `requeteuser`
--

DROP TABLE IF EXISTS `requeteuser`;
CREATE TABLE IF NOT EXISTS `requeteuser` (
  `dateRequete` varchar(255) NOT NULL,
  `TypeRequete` varchar(255) NOT NULL,
  `idUser` varchar(50) NOT NULL,
  PRIMARY KEY (`TypeRequete`,`idUser`),
  KEY `RequeteUser_User_FK` (`idUser`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Structure de la table `user`
--

DROP TABLE IF EXISTS `user`;
CREATE TABLE IF NOT EXISTS `user` (
  `idUser` varchar(50) NOT NULL,
  PRIMARY KEY (`idUser`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Contraintes pour les tables déchargées
--

--
-- Contraintes pour la table `chanel`
--
ALTER TABLE `chanel`
  ADD CONSTRAINT `Chanel_Guild_FK` FOREIGN KEY (`IdGuild`) REFERENCES `guild` (`IdGuild`);

--
-- Contraintes pour la table `clientuser`
--
ALTER TABLE `clientuser`
  ADD CONSTRAINT `ClientUser_Guild0_FK` FOREIGN KEY (`IdGuild`) REFERENCES `guild` (`IdGuild`),
  ADD CONSTRAINT `ClientUser_User_FK` FOREIGN KEY (`idUser`) REFERENCES `user` (`idUser`);

--
-- Contraintes pour la table `lie`
--
ALTER TABLE `lie`
  ADD CONSTRAINT `lie_Guild0_FK` FOREIGN KEY (`IdGuild`) REFERENCES `guild` (`IdGuild`),
  ADD CONSTRAINT `lie_Image_FK` FOREIGN KEY (`nomImage`) REFERENCES `image` (`nomImage`);

--
-- Contraintes pour la table `requeteuser`
--
ALTER TABLE `requeteuser`
  ADD CONSTRAINT `RequeteUser_User_FK` FOREIGN KEY (`idUser`) REFERENCES `user` (`idUser`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
