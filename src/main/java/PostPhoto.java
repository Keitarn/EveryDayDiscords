import discord4j.core.DiscordClient;
import discord4j.core.object.entity.MessageChannel;
import discord4j.core.object.util.Snowflake;

import java.io.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.sun.org.apache.xalan.internal.lib.ExsltDatetime.time;

public class PostPhoto {
    private static int i = 0;
    private Map<String, Command> commands = new HashMap<String, Command>();
    private HashMap<String, TreeSet<String>> messages = new HashMap<String, TreeSet<String>>();
    private HashMap<String, String> path = new HashMap<String, String>();
    private TreeSet<Long> default_channel = new TreeSet<Long>();
    private File[] fichiers;
    private boolean runner = false;

    PostPhoto(Map<String, Command> commands, HashMap<String, TreeSet<String>> messages, HashMap<String, String> path, TreeSet<Long> default_channel) {
        this.commands = commands;
        this.messages = messages;
        this.default_channel = default_channel;
        this.path = path;
        this.fichiers = chargeFichier();

    }

    public void lanceTache(DiscordClient client){
            if(!runner){
                runner = true;
                taskPost(client);
            }
    }

    private File[] chargeFichier() {
        File repertoire = new File(path.get("pathSource"));
        File[] listFichier = repertoire.listFiles(new FileFilter() {
            public boolean accept(File pathname) {
                return pathname.isFile();
            }
        });
        return listFichier;
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
        if (!verif()) {
            for (long chanel : default_channel) {
                //TODO : ouvrir un fichier attention et l'affiché sans le deplacer
                ((MessageChannel) client.getChannelById(Snowflake.of(chanel)).block()).createMessage(messageCreateSpec -> {
                    messageCreateSpec.setContent("```diff\n- Une erreur s'est produite ! La photo journalière ne pourra pas être posté\n```");
                }).subscribe();
            }
            return;
        }
        i++;
        for (long chanel : default_channel) {
            envoiePhoto(client,chanel,message);
        }
    }


        public void postPhoto(DiscordClient client, long idchannel, String message) {
        if (idchannel == 0) {
            return;
        }
        if(!verif()){
            //TODO : ouvrir un fichier attention et l'affiché sans le deplacer
            ((MessageChannel) client.getChannelById(Snowflake.of(idchannel)).block()).createMessage(messageCreateSpec -> {
                messageCreateSpec.setContent("```diff\n- Erreur lors de l'envoie de la photo, le dossier d'image est vide, pensez a remettre de nouvelles images d'ici la prochaine demande ou image journalière\n```");
            }).subscribe();
        }
            i++;
            envoiePhoto(client,idchannel,message);

    }

    public boolean verif(){
        if (fichiers.length <= i) {
            fichiers = chargeFichier();
            if (fichiers.length == 0) {
                return false;
            }
        }
        return true;
    }

    public void envoiePhoto(DiscordClient client, long idchannel, String message){
        String name = fichiers[i].getName();
        File source = new File(path.get("pathSource") + name);
        File destination = new File(path.get("pathCopy") + name);
        if(destination.exists()){
            destination = new File(path.get("pathCopy") +"_"+time()+""+ name);
        }
        source.renameTo(destination);
        try {
            FileInputStream test3 = new FileInputStream(destination);
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
