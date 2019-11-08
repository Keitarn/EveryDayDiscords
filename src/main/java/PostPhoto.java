import discord4j.core.DiscordClient;
import discord4j.core.object.entity.Channel;
import discord4j.core.object.entity.MessageChannel;
import discord4j.core.object.util.Snowflake;

import java.io.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class PostPhoto {
    private static int i = 0;
    private Map<String, Command> commands = new HashMap<String, Command>();
    private HashMap<String, TreeSet<String>> messages = new HashMap<String, TreeSet<String>>();
    private Properties properties;
    private Requetes requetes;
    private boolean runner = false;
    private boolean photo = true;

    PostPhoto(Map<String, Command> commands, HashMap<String, TreeSet<String>> messages, Properties properties,  Requetes requetes) {
        this.commands = commands;
        this.messages = messages;
        this.properties = properties;
        this.requetes = requetes;
        taskChargeFichier();
    }

    private void taskChargeFichier() {
        TimerTask tache = new TimerTask() {
            @Override
            public void run() {
                chargeFichier();
            }
        };
        Date monday_4h = getNextMonday_4h();
        Timer timer = new Timer();
        timer.schedule(tache, monday_4h, TimeUnit.DAYS.toMillis(7));
    }

    public void lanceTache(DiscordClient client){
            if(!runner){
                runner = true;
                taskPost(client);
            }
    }

    public void load(DiscordClient client){

    }
    public void chargeFichier() {
        File repertoire = new File(properties.getPathImage());
        if(!repertoire.exists()){
            photo = false;
        }
        File[] listFichier = repertoire.listFiles(new FileFilter() {
            public boolean accept(File pathname) {
                return pathname.isFile();
            }
        });
        for (int i =0; i < listFichier.length; i++){
            requetes.addPhoto(listFichier[i].getName());
        }
    }

    private void taskPost(DiscordClient client) {

        TimerTask tache = new TimerTask() {
            @Override
            public void run() {
                 postPhotoJourna(client,  "Bonne photo journalière");
            }
        };
        Date tomorrow = Date.from(LocalDate.now().plus(1, ChronoUnit.DAYS).atStartOfDay(ZoneId.systemDefault()).toInstant());
        Timer timer = new Timer();
        timer.schedule(tache, tomorrow, TimeUnit.DAYS.toMillis(1));
    }

    public void postPhotoJourna(DiscordClient client, String message) {
        // a recup dans la base
        String name = "";
        String [] idChannelTableau = new String[0];
        ResultSet chanelJourna = requetes.getChannelJourna();
        ArrayList<String> idchannel = new ArrayList<String>();
        try {
            while (chanelJourna.next()){
                idchannel.add(chanelJourna.getString("idChanel"));
            }
            idChannelTableau = idchannel.toArray(new String[0]);
        } catch (SQLException e) {
            return;
        }

        ResultSet guildJourna = requetes.getGuildChanel(idChannelTableau);
        String [] idGuildTableau = new String[0];
        ArrayList<String> idGuild = new ArrayList<String>();
        try {
            while (guildJourna.next()){
                idGuild.add(guildJourna.getString("idGuild"));
            }
            idGuildTableau = idGuild.toArray(new String[0]);
        } catch (SQLException e) {
            return;
        }

        ResultSet res = requetes.getphoto(idGuildTableau);
        try {
            if(res.next()){
                name = res.getString("nomImage");
            }
        } catch (SQLException e) {
        }


        if (name == "") {
            for (int j = 0; j < idChannelTableau.length; j++) {
                ((MessageChannel) client.getChannelById(Snowflake.of(idChannelTableau[j])).block()).createMessage(messageCreateSpec -> {
                    messageCreateSpec.setContent("```diff\n- Une erreur s'est produite ! La photo journalière ne pourra pas être posté\n```");
                }).subscribe();
            }
            return;
        }


        for (int j = 0; j < idChannelTableau.length; j++) {
            if(photo){
                envoiePhoto(client,Long.parseLong(idChannelTableau[j]),message, name);
            } else {
                ((MessageChannel) client.getChannelById(Snowflake.of(idChannelTableau[j])).block()).createMessage(messageCreateSpec -> {
                    messageCreateSpec.setContent("```diff\n- La photo journalière ne peut être envoyé car elle a été désactivé pour problème technique\n```");
                }).subscribe();
            }
        }
        for(int k = 0; k < idGuildTableau.length; k++){
            requetes.insertCoupleImageGuild(idGuildTableau[k],name);
        }

    }

    public void postPhoto(DiscordClient client, long idchannel, long idGuild, long autorid,String autorName, String message) {

        requetes.addUserGuild(idGuild,autorid);
        if(!photo){
            ((MessageChannel) client.getChannelById(Snowflake.of(idchannel)).block()).createMessage(messageCreateSpec -> {
                messageCreateSpec.setContent("```diff\n- La commande n'a pu être éffectué, la commande d'envoie de photo est actuellement désactivé\n```");
            }).subscribe();
            return;
        }



        long time = 11000;
        String datePost;
        ResultSet resTime = requetes.getTimeReqequete("!ask_lamas", autorid);
        try {
            if(resTime.next()){
                datePost = resTime.getString("dateRequete");
                time = Long.parseLong(datePost);
                Date dateNow = new Date();
                long timeNow = dateNow.getTime();
                time = (timeNow - time)/1000;


            }
        } catch (SQLException e) {
        }
        if(time < 10800 & idGuild != 631786237753032715L){
            long timeRestant = (10800-time);
            long heure = timeRestant / 3600;
            long minute = (timeRestant % 3600) / 60 ;
            long seconde = (timeRestant % 3600) % 60 ;

            ((MessageChannel) client.getChannelById(Snowflake.of(idchannel)).block()).createMessage(messageCreateSpec -> {
                messageCreateSpec.setContent("```diff\n- Tu n'as le droit qu'a une seule photo toute les trois heures, "+autorName+" tu dois encore attendre "+heure+"h"+minute+"min"+seconde+"s\n```");
            }).subscribe();
            return;
        }

        String name = "";
        String[] stringGuildID = {""+idGuild};
        ResultSet res = requetes.getphoto(stringGuildID);
            try {
                if(res.next()){
                    name = res.getString("nomImage");
                }
            } catch (SQLException e) {
            }


        if(name == ""){
            ((MessageChannel) client.getChannelById(Snowflake.of(idchannel)).block()).createMessage(messageCreateSpec -> {
                messageCreateSpec.setContent("```diff\n- Erreur lors de l'envoie de la photo, le dossier d'image est vide, pensez a remettre de nouvelles images d'ici la prochaine demande ou image journalière\n```");
            }).subscribe();
        }

            envoiePhoto(client,idchannel,message, name);
            requetes.insertCoupleImageGuild(""+idGuild,name);
            requetes.insertReuqeteUser("!ask_lamas",autorid);
            requetes.incrementeRequete(""+idGuild,""+autorid,"!ask_lamas");

    }

    public void envoiePhoto(DiscordClient client, long idchannel, String message, String name ){
        File source = new File(properties.getPathImage() + '\\' + name);
        try {
            FileInputStream test3 = new FileInputStream(source);
            ((MessageChannel) client.getChannelById(Snowflake.of(idchannel)).block()).createMessage(messageCreateSpec -> {
                messageCreateSpec.setContent(message);
                messageCreateSpec.addFile(name, test3);
            }).subscribe(
                    t -> {
                        try {
                            test3.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
            );
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private Date getNextMonday_4h() {
        Date date = Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().plus(4, ChronoUnit.HOURS));
        Calendar maDate = new java.util.GregorianCalendar();
        maDate.setTime(date);
        // On se positionne sur le Lundi de la semaine courante :
        maDate.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        // Puis on ajoute 7 jours :
        maDate.add(Calendar.DATE, 7);
        return maDate.getTime();
    }
}
