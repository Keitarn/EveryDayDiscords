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

    public boolean addPhoto(String nameImage) {
        String requete = "INSERT IGNORE INTO image VALUES (?)";
        connexion.executerPreparedUpdate(requete, nameImage);
        return false;
    }


    public ResultSet getphoto(String[] idGuildTableau) {

        String requete = "SELECT nomImage FROM image WHERE nomImage NOT IN (SELECT lie.nomImage FROM lie where (lie.nomImage=image.nomImage";
        for(int i = 0; i < idGuildTableau.length; i++){
            if(i == 0){
                requete+= " AND lie.idGuild = ?";
            }else{
                requete+= ") OR (lie.nomImage=image.nomImage AND lie.idGuild = ?";

            }
        }
        requete+= ")) Limit 1";
        return connexion.executerPreparedSelect(requete,idGuildTableau);
    }

    public ResultSet getChannelJourna() {
        String requete = "SELECT idChanel FROM chanel WHERE abonne = true";
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
        updateChannelAbonne(chanel, guild,"true");

    }

    public void removeChanelDefault(long chanel,long guild) {
        updateChannelAbonne(chanel, guild,"false");
    }

    private void updateChannelAbonne(long chanel, long guild, String bool) {
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
        String requete2 = "INSERT INTO requeteuser(dateRequete,TypeRequete,idUser) VALUES (?,?,?) ON DUPLICATE KEY UPDATE dateRequete = ?";

        connexion.executerPreparedUpdate(requete2,""+date.getTime() ,commande,""+autorid,""+date.getTime());

    }

    public ResultSet getTimeReqequete(String commande, long authorID) {
        String requete = "SELECT dateRequete FROM requeteuser WHERE TypeRequete = ? AND idUser = ?";

        return connexion.executerPreparedSelect(requete, commande, ""+authorID);
    }
}