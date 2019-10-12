import discord4j.core.DiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
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
    private Map<String, Command> commands = new HashMap<>();
    private HashMap<String, TreeSet<String>> messages = new HashMap<String, TreeSet<String>>();
    private HashMap<String, String> path = new HashMap<String, String>();
    private TreeSet<Long> default_channel = new TreeSet<Long>();
    private File[] fichiers;

    PostPhoto(Map<String, Command> commands, HashMap<String, TreeSet<String>> messages, HashMap<String, String> path, TreeSet<Long> default_channel) {
        this.commands = commands;
        this.messages = messages;
        this.default_channel = default_channel;
        this.path = path;
        this.fichiers = chargeFichier();

    }

    private void addDefault_channel(DiscordClient client) {
        client.getEventDispatcher().on(MessageCreateEvent.class).subscribe(event -> event.getMessage().getContent().ifPresent(c -> {
            if (c.equals("!default_lamas")) {
                long chanel = event.getMessage().getChannel().map(ch -> ch.getId()).block().asLong();
                ((MessageChannel) client.getChannelById(Snowflake.of(chanel)).block()).createMessage(messageCreateSpec -> {
                    messageCreateSpec.setContent("Ce chanel a été ajouté au serveur par défault s'il ne l'était pas déja");
                }).subscribe();
                default_channel.add(chanel);
            }
        }));
    }

    private void removeDefault_channel(DiscordClient client) {
        client.getEventDispatcher().on(MessageCreateEvent.class).subscribe(event -> event.getMessage().getContent().ifPresent(c -> {
            if (c.equals("!undefault_lamas")) {
                default_channel.remove(event.getMessage().getChannel().map(ch -> ch.getId()).block().asLong());
            }
        }));
    }

    private File[] chargeFichier() {
        System.out.println(path.get("pathSource"));
        File repertoire = new File(path.get("pathSource"));
        File[] listFichier = repertoire.listFiles(new FileFilter() {
            public boolean accept(File pathname) {
                return pathname.isFile();
            }
        });
        return listFichier;
    }

    private void taskPost(DiscordClient client, File[] fichiers) {

        TimerTask tache = new TimerTask() {
            @Override
            public void run() {

                for (long chanel : default_channel) {
                    postPhoto(client, chanel, "Bonne photo journalière");
                }
            }
        };
        Date tomorrow = Date.from(LocalDate.now().plus(1, ChronoUnit.DAYS).atStartOfDay(ZoneId.systemDefault()).toInstant());
        Timer timer = new Timer();
        timer.schedule(tache, tomorrow, TimeUnit.DAYS.toMillis(1));
    }

    public void postPhoto(DiscordClient client, long idchannel, String message) {
        if (idchannel == 0) {
            return;
        }
        if (fichiers.length <= i) {
            fichiers = chargeFichier();
            if (fichiers.length == 0) {
                //ouvrir un fichier attention et l'affiché sans le deplacer
                ((MessageChannel) client.getChannelById(Snowflake.of(idchannel)).block()).createMessage(messageCreateSpec -> {
                    messageCreateSpec.setContent("```diff\n- Erreur lors de l'envoie de la photo, le dossier d'image est vide, pensez a remettre de nouvelles images d'ici la prochaine demande ou image journalière\n```");
                }).subscribe();
                return;
            }
        }
        String name = fichiers[i].getName();
        i++;
        File source = new File(path.get("pathSource") + name);
        File destination = new File(path.get("pathCopy") + name);
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
