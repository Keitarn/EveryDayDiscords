public class Requetes {
    private Connexion connexion;

    private static final String IMAGE = "image";
    public Requetes(Connexion connexion) {
        this.connexion = connexion;
    }

    public Connexion getConnexion() {
        return connexion;
    }

    public void setConnexion(Connexion connexion) {
        this.connexion = connexion;
    }

    public boolean addPhoto(String nameImage){
        String requete = "INSERT IGNORE INTO "+IMAGE+" VALUES (?) ";
        connexion.executerPreparedUpdate(requete, nameImage);
        return false;
    }
}
