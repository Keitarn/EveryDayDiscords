public class Properties {
    private String login_BDD;
    private String mdp_BDD;
    private String token_BOT;
    private String nom_BDD;
    private String path_BDD;
    private String pathImage;
    private String pathCopy;

    public Properties(String login, String mdp, String token, String nom, String path, String image, String copy){
        pathImage = image;
        login_BDD = login;
        mdp_BDD = mdp;
        token_BOT = token;
        nom_BDD = nom;
        path_BDD = path;
    }

    public String getLogin_BDD() {
        return login_BDD;
    }

    public String getMdp_BDD() {
        return mdp_BDD;
    }

    public String getToken_BOT() {
        return token_BOT;
    }

    public String getNom_BDD() {
        return nom_BDD;
    }

    public String getPath_BDD() {
        return path_BDD;
    }

    public String getPathImage() {
        return pathImage;
    }
}
