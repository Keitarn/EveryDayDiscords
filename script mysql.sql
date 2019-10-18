#------------------------------------------------------------
#        Script MySQL.
#------------------------------------------------------------


#------------------------------------------------------------
# Table: Chanel
#------------------------------------------------------------

CREATE TABLE Chanel(
        idChanel Double NOT NULL ,
        idGuild  Double NOT NULL
	,CONSTRAINT Chanel_PK PRIMARY KEY (idChanel)
)ENGINE=InnoDB;


#------------------------------------------------------------
# Table: Guild
#------------------------------------------------------------

CREATE TABLE Guild(
        IdGuild  Double NOT NULL ,
        idChanel Double
	,CONSTRAINT Guild_PK PRIMARY KEY (IdGuild)

	,CONSTRAINT Guild_Chanel_FK FOREIGN KEY (idChanel) REFERENCES Chanel(idChanel)
)ENGINE=InnoDB;


#------------------------------------------------------------
# Table: Client
#------------------------------------------------------------

CREATE TABLE Client(
        idClient Double NOT NULL
	,CONSTRAINT Client_PK PRIMARY KEY (idClient)
)ENGINE=InnoDB;


#------------------------------------------------------------
# Table: Image
#------------------------------------------------------------

CREATE TABLE Image(
        nomImage Text NOT NULL
	,CONSTRAINT Image_PK PRIMARY KEY (nomImage)
)ENGINE=InnoDB;


#------------------------------------------------------------
# Table: lie
#------------------------------------------------------------

CREATE TABLE lie(
        nomImage Text NOT NULL ,
        IdGuild  Double NOT NULL ,
        isUsed   Bool NOT NULL
	,CONSTRAINT lie_PK PRIMARY KEY (nomImage,IdGuild)

	,CONSTRAINT lie_Image_FK FOREIGN KEY (nomImage) REFERENCES Image(nomImage)
	,CONSTRAINT lie_Guild0_FK FOREIGN KEY (IdGuild) REFERENCES Guild(IdGuild)
)ENGINE=InnoDB;


#------------------------------------------------------------
# Table: ClientGuild
#------------------------------------------------------------

CREATE TABLE ClientGuild(
        idClient Double NOT NULL ,
        IdGuild  Double NOT NULL
	,CONSTRAINT ClientGuild_PK PRIMARY KEY (idClient,IdGuild)

	,CONSTRAINT ClientGuild_Client_FK FOREIGN KEY (idClient) REFERENCES Client(idClient)
	,CONSTRAINT ClientGuild_Guild0_FK FOREIGN KEY (IdGuild) REFERENCES Guild(IdGuild)
)ENGINE=InnoDB;

