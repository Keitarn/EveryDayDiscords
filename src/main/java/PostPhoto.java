import discord4j.core.DiscordClient;
import discord4j.core.object.entity.Channel;
import discord4j.core.object.entity.MessageChannel;
import discord4j.core.object.util.Snowflake;

import java.io.*;
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
    private TreeSet<Long> default_channel = new TreeSet<Long>();
    private Requetes requetes;
    private boolean runner = false;
    private boolean photo = true;

    PostPhoto(Map<String, Command> commands, HashMap<String, TreeSet<String>> messages, Properties properties, TreeSet<Long> default_channel, Requetes requetes) {
        this.commands = commands;
        this.messages = messages;
        this.default_channel = default_channel;
        this.properties = properties;
        this.requetes = requetes;
        chargeFichier();
    }

    public void lanceTache(DiscordClient client){
            if(!runner){
                runner = true;
                taskPost(client);
            }
    }

    private void chargeFichier() {
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
        String name ="";
        if (name == "") {
            for (long chanel : default_channel) {
                ((MessageChannel) client.getChannelById(Snowflake.of(chanel)).block()).createMessage(messageCreateSpec -> {
                    messageCreateSpec.setContent("```diff\n- Une erreur s'est produite ! La photo journalière ne pourra pas être posté\n```");
                }).subscribe();
            }
            return;
        }


        for (long chanel : default_channel) {
            if(photo){
                envoiePhoto(client,chanel,message, name);
            } else {
                ((MessageChannel) client.getChannelById(Snowflake.of(chanel)).block()).createMessage(messageCreateSpec -> {
                    messageCreateSpec.setContent("```diff\n- La photo journalière ne peut être envoyé car elle a été désactivé pour problème technique\n```");
                }).subscribe();
            }

        }

    }


        public void postPhoto(DiscordClient client, long idchannel, String message) {
            Channel channel =  client.getChannelById(Snowflake.of(idchannel)).block();
            System.out.println(channel.getClient().getGuilds().toString());
        if (idchannel == 0) {
            return;
        }
        if(!photo){
            ((MessageChannel) client.getChannelById(Snowflake.of(idchannel)).block()).createMessage(messageCreateSpec -> {
                messageCreateSpec.setContent("```diff\n- La commande n'a pu être éffectué, la commande d'envoie de photo est actuellement désactivé\n```");
            }).subscribe();
            return;
        }

            //a recup dans la base
            String name = "";

        if(name == ""){
            //TODO : ouvrir un fichier attention et l'affiché sans le deplacer
            ((MessageChannel) client.getChannelById(Snowflake.of(idchannel)).block()).createMessage(messageCreateSpec -> {
                messageCreateSpec.setContent("```diff\n- Erreur lors de l'envoie de la photo, le dossier d'image est vide, pensez a remettre de nouvelles images d'ici la prochaine demande ou image journalière\n```");
            }).subscribe();
        }

            envoiePhoto(client,idchannel,message, name);
            i++;
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
}
