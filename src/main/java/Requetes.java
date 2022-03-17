import discord4j.core.object.entity.User;
import discord4j.core.object.util.Snowflake;
import reactor.core.publisher.Mono;

import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Requetes {
    private Connexion connexion;


    public Requetes(Connexion connexion) {
        this.connexion = connexion;
    }

    public Connexion getConnexion() {
        return connexion;
    }

    public void setConnexion(Connexion connexion) {
        this.connexion = connexion;
    }

    public boolean addPhoto(String nameImage, String type) {
        String requete = "INSERT IGNORE INTO image VALUES (?,?)";
        connexion.executerPreparedUpdate(requete, nameImage,type);
        return false;
    }


    public ResultSet getphoto(String type) {
        String requete = "SELECT nomImage FROM image WHERE typeDossier = ? ORDER BY RAND () Limit 1";
        return connexion.executerPreparedSelect(requete, type);
    }

    public ResultSet getChannelJourna() {
        String requete = "SELECT idChanel,type FROM chanel WHERE abonne = true";
        return connexion.executerRequete(requete);
    }

    public ResultSet getGuildChanel(String [] idChannelTableau) {
        String requete = "SELECT idGuild FROM chanel WHERE idChanel = ?";
        if(idChannelTableau.length == 0){
            return null;
        }
        for(int i = 1; i < idChannelTableau.length; i++){
            requete += " OR idChanel = ?";

        }
        return connexion.executerPreparedSelect(requete, idChannelTableau);
    }

    public void addChanelDefault(long chanel,long guild) {
        updateChannelAbonne(chanel, guild,true);

    }

    public void removeChanelDefault(long chanel,long guild) {
        updateChannelAbonne(chanel, guild,false);
    }

    private void updateChannelAbonne(long chanel, long guild, boolean bool) {
        String ch = chanel+"";
        String gu = guild+"";
        String requeteGuild = "INSERT IGNORE INTO guild VALUES (?)";
        connexion.executerPreparedUpdate(requeteGuild,gu);
        String requete = "INSERT INTO chanel (idChanel,abonne,idGuild) VALUES (?, "+bool+", ?) ON DUPLICATE KEY UPDATE abonne = "+bool;
        connexion.executerPreparedUpdate(requete,ch,gu);
    }


    public void insertCoupleImageGuild(String guild,String name) {
        String requete = "INSERT IGNORE INTO lie(IdGuild,nomImage) VALUES (?,?)";
        connexion.executerPreparedUpdate(requete,guild,name);

    }

    public void insertReuqeteUser(String commande,long autorid) {
        Date date = new Date();
        String requete = "INSERT IGNORE INTO user VALUES (?)";
        connexion.executerPreparedUpdate(requete,""+autorid);
//       TexteConstantesConnexion.DATE +
        String requete2 = "INSERT INTO requete(dateRequete,TypeRequete,idUser) VALUES (?,?,?) ON DUPLICATE KEY UPDATE dateRequete = ?";

        connexion.executerPreparedUpdate(requete2,""+date.getTime() ,commande,""+autorid,""+date.getTime());

    }

    public ResultSet getTimeReqequete(String commande, long authorID) {
        String requete = "SELECT dateRequete FROM requete WHERE TypeRequete = ? AND idUser = ?";

        return connexion.executerPreparedSelect(requete, commande, ""+authorID);
    }

    public void addUserGuild(long idGuild, long autorid) {
        String requete = "INSERT IGNORE INTO userGuild(idUser,idGuild) VALUES (?,?)";
        connexion.executerPreparedUpdate(requete,""+autorid,""+idGuild);
    }

    public void incrementeRequete(String idGuild,String autorid,String commande) {
        String requete = "INSERT INTO requeteUserServer(idUser,idGuild,TypeRequete,nombreAppel) VALUES (?,?,?,1) ON DUPLICATE KEY UPDATE nombreAppel = nombreAppel+1 ";
        connexion.executerPreparedUpdate(requete,""+autorid,""+idGuild,commande);
    }

    public ResultSet recupClassementCommand(String idGuild, String commande) {
        String requete = "SELECT idUser,nombreAppel FROM requeteuserserver WHERE idGuild = ? AND typeRequete = ? ORDER BY nombreAppel DESC Limit 10";
        return connexion.executerPreparedSelect(requete,idGuild, commande);

    }

    public ResultSet getGuildChannel() {
        String requete = "SELECT idChanel, idGuild FROM chanel WHERE abonne = 1";
        return connexion.executerPreparedSelect(requete);
    }

    public ResultSet recupClassementCommandGlobal(String s) {
        String requete = "SELECT idUser, SUM(nombreAppel) FROM requeteuserserver WHERE typeRequete = ? GROUP BY idUser ORDER BY SUM(nombreAppel) DESC LIMIT 10";
        return connexion.executerPreparedSelect(requete,s);
    }

    public ResultSet recupClassementCommandGlobalPerso(String s, long iduser) {
        String requete = "SELECT idUser, SUM(nombreAppel) FROM requeteuserserver WHERE typeRequete = ? AND idUser = ? GROUP BY idUser ORDER BY SUM(nombreAppel) DESC LIMIT 10";
        return connexion.executerPreparedSelect(requete,s,""+iduser);
    }

    public ResultSet recupClassementCommandPerso(long idGuild, String s, long iduser) {
        String requete = "SELECT idUser,nombreAppel FROM requeteuserserver WHERE idGuild = ? AND typeRequete = ? AND idUser = ? ORDER BY nombreAppel DESC Limit 10";
        return connexion.executerPreparedSelect(requete,""+idGuild, s,""+iduser);
    }

    public ResultSet recupClassementCommandGlobalPersoCount(String s, int nbAppel) {
        String requete = "SELECT SUM(resultat) as result FROM (SELECT COUNT(*) as resultat FROM requeteuserserver WHERE typeRequete = ? GROUP BY idUser HAVING SUM(nombreAppel) > ? )AS blabla";
        return connexion.executerPreparedSelect(requete,s,""+nbAppel);
    }


    public ResultSet recupClassementCommandPersoCount(long idGuild, String s, long iduser) {
        String requete = "SELECT SUM(resultat) as result FROM (SELECT COUNT(*) as resultat FROM requeteuserserver WHERE idGuild = ? AND typeRequete = ? GROUP BY idUser HAVING SUM(nombreAppel) > ? )AS blabla";
        return connexion.executerPreparedSelect(requete,""+idGuild, s,""+iduser);
    }

    public ResultSet droitAdmin(long id, long idGuild) {
        String requete = "SELECT COUNT(*) as result FROM administrateurs WHERE (idAdmin = ? AND idGuild = ?) OR (idAdmin = ? AND idGuild = -1)";
        return connexion.executerPreparedSelect(requete,""+id, ""+idGuild, ""+id);
    }

    public void addAdminServer(long idAuteur, String username, long idGuild) {
        String requete = "INSERT IGNORE INTO administrateurs VALUES (?,?,?)";
        connexion.executerPreparedUpdate(requete,""+idAuteur,username,""+idGuild);
    }

    public void addHelp(String command, String help) {
        String requete = "INSERT IGNORE INTO aide(commande,aideCommande) VALUES (?,?)";
        connexion.executerPreparedUpdate(requete,command,""+help);
    }

    public ResultSet getHelp(String command) {
        String requete = "SELECT commande,aideCommande FROM aide WHERE commande = ?";
        return connexion.executerPreparedSelect(requete,command);
    }

    public void deleteAdminServer(long idAuteur, String username, long idGuild) {
        String requete = "DELETE FROM administrateurs WHERE idAuteur=? AND idGuild=?";
        connexion.executerPreparedUpdate(requete,""+idAuteur,username,""+idGuild);
    }

    public void ajoutAdminLamas(long idUser, String username, long idGuild) {
        String requete = "INSERT IGNORE INTO administrateurs VALUES (?,?,?)";
        connexion.executerPreparedUpdate(requete,""+idUser,username,""+idGuild);
    }
}