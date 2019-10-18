/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * Une classe pour gérer la connexion à une base de données et faire les requêtes
 * sur cette base de données.
 * @author Robin
 */

import org.sqlite.SQLiteConfig;

import java.sql.*;
import java.util.Properties;
import java.util.logging.*;

public class Connexion {
    
    //ATTRIBUTS
    /**
     * Le type de la base de données.
     * @see TypeDatabase
     */
    private TypeDatabase type;
    /**
     * Le chemin complet pour la base de données avec le préfixe et suffixe du jcbc.
     * @see TypeDatabase
     */
    private String database;
    /**
     * La connexion établie avec la base de données.
     * @see Connection
     */
    private Connection connexion;
    /**
     * L'objet qui permet d'exécuter les requêtes simples.
     * @see Statement
     */
    private Statement statement;
    /**
     * L'objet qui permet d'exécuter des requêtes préparées. Utilisé pour les SELECT
     * ou pour les UPDATE/INSERT/DELETE.
     * @see PreparedStatement
     */
    private PreparedStatement preparedStatement;
    
    //CONSTRUCTEUR
    public Connexion() {
        
    }
    
    
    //ACCESSSEURS
    /**
     * Retourne le type de la base de données connectée.
     * @return le type de la base de données connectée
     */
    public TypeDatabase getType() {
        return type;
    }
    
    /**
     * Retourne le statement simple pour exécuter les requêtes simples.
     * @return le statement pour les requêtes simples
     */
    public Statement getStatement() {
        return statement;
    }
    
    /**
     * Retourne le PreparedStatement utilisé pour les requêtes préparées. Utilisé
     * pour obtenir l'identifiant de la dernière requête d'insertion.
     * @return le PreparedStatement pour les requêtes préparées
     */
    public PreparedStatement getPreparedStatement() {
        return preparedStatement;
    }
    
    //MUTATEURS
    /**
     * Modifie le type de base de données sur laquelle se connecter.
     * @param type le nouveau type de la base de données
     * @see TypeDatabase
     */
    private void setType(TypeDatabase type) {
        this.type = type;
    }
    
    /**
     * Modifie le chemin de la base de données sur laquelle se connecter.
     * @param db la nouvelle  base de données
     */
    private void setDatabase(String db) {
        database = db;
    }
    
    /**
     * Initialise la connexion avec la base de données avec les propriétés "properties".
     * @param properties les propriétés avec lesquelles se connecter (utilisateur
     * ,mot de passe, et autres)
     */
    private void setConnection(Properties properties) {
        try {
            Class.forName(type.getDriver());
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(Connexion.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        try {
            connexion = DriverManager.getConnection(database,properties);
            statement = connexion.createStatement();
        } catch (SQLException ex) {
            Logger.getLogger(Connexion.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Se connecte à une base de données (fichier) dont le chemin est path.
     * @param path le chemin de la base de données
     */
    public void connecter(String path) {
        String lowerCase = path.toLowerCase();
        boolean isAccess = lowerCase.endsWith(".accdb") || lowerCase.endsWith(".mdb");
        boolean isSQLite = lowerCase.endsWith(".db") || lowerCase.endsWith(".sdb")
                        || lowerCase.endsWith(".sqlite") || lowerCase.endsWith(".db3")
                        || lowerCase.endsWith(".s3db") || lowerCase.endsWith(".sqlite3")
                        || lowerCase.endsWith(".sl3") || lowerCase.endsWith(".db2")
                        || lowerCase.endsWith(".s2db") || lowerCase.endsWith(".sqlite2")
                        || lowerCase.endsWith(".sl2") || lowerCase.endsWith(".kexi");
        
        if (isAccess) {
            setType(TypeDatabase.Access);
        }
        SQLiteConfig config = null;
        if (isSQLite) {
            setType(TypeDatabase.SQLite);
            config = new SQLiteConfig();
            config.enforceForeignKeys(true);
        }
        
        setDatabase(type.getPrefixe() + path + type.getSuffixe());
        Properties properties = (null != config) ? config.toProperties() : new Properties();
        properties.setProperty("user", System.getProperty("user.name"));
        properties.setProperty("password", "");
        setConnection(properties);
        
        try {
            statement.setQueryTimeout(30);
        } catch (SQLException ex) {
            Logger.getLogger(Connexion.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Se connecte à une base de données (serveur) avec le type, l'adresse de la
     * base de données, l'utilisateur et le mot de passe.
     * @param type le type de la base de données (serveur)
     * @param serveur l'adresse de la base de données serveur (dont le nom de la
     * base de données à laquelle se connecter
     * @param user l'utilisateur utilisé pour se connecter
     * @param password le mot de passe de l'utilisateur
     */
    public void connecter(TypeDatabase type, String serveur, String user, char[] password) {
        setType(type);
        setDatabase(type.getPrefixe() + serveur + type.getSuffixe());
        Properties properties = new Properties();
        properties.setProperty("user", user);
        properties.setProperty("password", new String(password));
        setConnection(properties);
        try {
            statement.setQueryTimeout(30);
        } catch (SQLException ex) {
            Logger.getLogger(Connexion.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Prépare les paramètres pour une requête préparée.
     * @param prepared le PreparedStatement à préparer
     * @param args les arguments à mettre dans le PreparedStatement en String
     * @throws SQLException 
     */
    private void preparedSetParameters(PreparedStatement prepared, String ... args) throws SQLException {
        //mise en place des paramètres pour le PreparedStatement
        for (int i = 0; i < args.length; i++) {
            //si l'argument représente un booléen
            if (args[i].startsWith(TexteConstantesConnexion.BOOL)) {
                prepared.setBoolean(i+1, Boolean.parseBoolean(args[i].replace(TexteConstantesConnexion.BOOL,"")));
            }
            else {
                //si l'argument représenté un entier
                if (args[i].startsWith(TexteConstantesConnexion.INT)) {
                    prepared.setInt(i+1, Integer.parseInt(args[i].replace(TexteConstantesConnexion.INT, "")));
                }
                else {
                    //si l'argument représente un entier long
                    if (args[i].startsWith(TexteConstantesConnexion.LONG)) {
                         prepared.setLong(i+1, Long.parseLong(args[i].replace(TexteConstantesConnexion.LONG,"")));
                    }
                    else {
                        //si l'argument représente une datetime
                        if (args[i].startsWith(TexteConstantesConnexion.DATETIME)) {
                            //si connecté à une base de données PostgreSQL
                            if (this.type.equals(TypeDatabase.PostgreSQL)) {
                                prepared.setTimestamp(i+1,Timestamp.valueOf(args[i].replace(TexteConstantesConnexion.DATETIME,"")));
                            }
                            else {
                                prepared.setString(i+1,args[i].replace(TexteConstantesConnexion.DATETIME,""));
                            }
                        }
                        else {
                            //si l'argument représente une date simple
                            if(args[i].startsWith(TexteConstantesConnexion.DATE)) {
                                //si connecté à une base de données SQLite
                                if (this.type.equals(TypeDatabase.SQLite)) {
                                    prepared.setString(i+1,args[i].replace(TexteConstantesConnexion.DATE,""));
                                }
                                else {
                                    prepared.setDate(i+1, Date.valueOf(args[i].replace(TexteConstantesConnexion.DATE,"")));
                                }
                            }
                            //si l'argument représente une chaîne de caracères
                            else {
                                prepared.setString(i+1, args[i]);
                            }
                        }
                    }
                }
            }
        }
    }
    
    public ResultSet executerPreparedSelect(String requete, String ... args) {
        if (requete.toUpperCase().startsWith("SELECT")) {
            try {
                preparedStatement = connexion.prepareStatement(requete, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
                
                preparedSetParameters(preparedStatement, args);
                
                return preparedStatement.executeQuery();
            } catch (SQLException ex) {
                Logger.getLogger(Connexion.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return null;
    }
    
    public int executerPreparedUpdate(String requete, String  ... args) {
        String upperCase = requete.substring(0,7).toUpperCase();
        int res = -1;
        if (upperCase.startsWith("UPDATE") || upperCase.startsWith("INSERT") || upperCase.startsWith("DELETE")) {
            try {
                preparedStatement = connexion.prepareStatement(requete, Statement.RETURN_GENERATED_KEYS /*+ ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY*/);
                //mise en place des paramètres pour le PreparedStatement
                preparedSetParameters(preparedStatement, args);
                
                res = preparedStatement.executeUpdate();
//                preparedStatement.close();
            } catch (SQLException ex) {
                Logger.getLogger(Connexion.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return res;
    }
    
    public ResultSet executerRequete(String requete) {
        ResultSet res = null;
        try {
            res = statement.executeQuery(requete);
        } catch (SQLException ex) {
            Logger.getLogger(Connexion.class.getName()).log(Level.SEVERE, null, ex);
        }
        return res;
    }
    
    public void executerUpdate(String requete) {
        try {
            statement.executeUpdate(requete);
        } catch (SQLException ex) {
            Logger.getLogger(Connexion.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void deconnecter() {
        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException ex) {
                Logger.getLogger(Connexion.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if (preparedStatement != null) {
            try {
                preparedStatement.close();
            } catch (SQLException ex) {
                Logger.getLogger(Connexion.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if (connexion != null) {
            try {
                connexion.close();
            } catch (SQLException ex) {
                Logger.getLogger(Connexion.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
}
