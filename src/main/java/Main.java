import discord4j.core.DiscordClient;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.MessageChannel;
import discord4j.core.object.util.Snowflake;
import reactor.core.publisher.Mono;

import java.io.*;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class Main {

    //A passer en fichier
    private static final String pathSource = "D:\\Documents\\GitHub\\Test\\Image1\\";
    private static final String pathCopy = "D:\\Documents\\GitHub\\Test\\Image2\\";
    private static final String pathConfig = "D:\\Documents\\GitHub\\Test\\config\\";
    private static final String pathImage = "D:\\Mes images\\everyday ltk";
    private static int i = 0;

    private static File[] fichiers;
    private static TreeSet<Long> default_channel = new TreeSet<Long>();
    private static HashMap<String, TreeSet<String>> messages = new HashMap<String, TreeSet<String>>();
    private static HashMap<String, String> path = new HashMap<String, String>();
    private static final Map<String, Command> commands = new HashMap<>();

    public static void main(String[] args) throws IOException {
        path.putIfAbsent("pathConfig","D:\\Documents\\GitHub\\Test\\config\\");
//        LectureParam(false);
        InstanciationClass loadSaver = new InstanciationClass(commands, messages, path,default_channel);
        DiscordClient client = new DiscordClientBuilder("NjMxNzg1MDM2NjE3NzQ0Mzg0.XZ77wg.H8w6DcwljnmSLgOIRqf2YFGh4mg").build();

        commands.put("!save", event -> {
            try {
                loadSaver.LectureParam(true);
            } catch (IOException e) {
                System.out.println("erreur lors de la sauvegarde");
            }
            return null;
        });

        dispatcher(client);
        addDefault_channel(client);
        removeDefault_channel(client);
        ask(client);
        add(client);
        help(client);
        fichiers = chargeFichier();
        taskPost(client, fichiers);

        client.login().block();
    }


    private static void ask(DiscordClient client) {
        client.getEventDispatcher().on(MessageCreateEvent.class).subscribe(event -> event.getMessage().getContent().ifPresent(c -> {
            if (c.equals("!ask_lamas")) {
                long channel = event.getMessage().getChannel().map(ch -> ch.getId()).block().asLong();
                String autor = event.getMessage().getAuthor().get().getUsername();
                PostPhoto(client, fichiers, channel, "Voila une photo pour toi " + autor + ", j'espere qu'elle te plaira !");
            }
        }));
    }

    private static void addDefault_channel(DiscordClient client) {
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

    private static void removeDefault_channel(DiscordClient client) {
        client.getEventDispatcher().on(MessageCreateEvent.class).subscribe(event -> event.getMessage().getContent().ifPresent(c -> {
            if (c.equals("!undefault_lamas")) {
                default_channel.remove(event.getMessage().getChannel().map(ch -> ch.getId()).block().asLong());
            }
        }));
    }

    private static void add(DiscordClient client) {
        client.getEventDispatcher().on(MessageCreateEvent.class).subscribe(event -> event.getMessage().getContent().ifPresent(c -> {
            String[] res = c.split(" ", 2);
            if (res[0].equals("!add")) {
                if (res.length > 1) {
                    String[] res2 = res[1].split(" ", 2);
                } else {
                    long chanel = event.getMessage().getChannel().map(ch -> ch.getId()).block().asLong();
                    ((MessageChannel) client.getChannelById(Snowflake.of(chanel)).block()).createMessage(messageCreateSpec -> {
                        messageCreateSpec.setContent("add a besoin d'un argument, pour plus d'information utilise !help add");
                    }).subscribe();
                }
            }
        }));
    }

    private static void dispatcher(DiscordClient client) {
        client.getEventDispatcher().on(MessageCreateEvent.class).flatMap(event -> Mono.justOrEmpty(event.getMessage().getContent())
                .filter(content -> content.startsWith("!"))
                .map(content -> content.split(" "))
                .filter(strs -> commands.containsKey(strs[0]))
                .flatMap(argsCommand -> { commands.get(argsCommand[0]).execute(event);return Mono.empty().then();
                }))
                .subscribe();

    }

    private static void help(DiscordClient client) {
        client.getEventDispatcher().on(MessageCreateEvent.class).subscribe(event -> event.getMessage().getContent().ifPresent(c -> {
            if (c.equals("!help")) {
                String message = "";
                TreeSet<String> messagesCommande = messages.get("!help");
                for (String elem : messagesCommande) {
                    message = message + elem + "\n";
                }
                final String messEnvoie = message;
                long chanel = event.getMessage().getChannel().map(ch -> ch.getId()).block().asLong();
                ((MessageChannel) client.getChannelById(Snowflake.of(chanel)).block()).createMessage(messageCreateSpec -> {
                    messageCreateSpec.setContent(messEnvoie);
                }).subscribe();
            }
        }));
    }

    private static File[] chargeFichier() {
        File repertoire = new File(pathSource);
        File[] listFichier = repertoire.listFiles(new FileFilter() {
            public boolean accept(File pathname) {
                return pathname.isFile();
            }
        });
        return listFichier;
    }

    private static void taskPost(DiscordClient client, File[] fichiers) {

        TimerTask tache = new TimerTask() {
            @Override
            public void run() {

                for (long chanel : default_channel) {
                    PostPhoto(client, fichiers, chanel, "Bonne photo journalière");
                }
            }
        };
        Date tomorrow = Date.from(LocalDate.now().plus(1, ChronoUnit.DAYS).atStartOfDay(ZoneId.systemDefault()).toInstant());
        Timer timer = new Timer();
        timer.schedule(tache, tomorrow, TimeUnit.DAYS.toMillis(1));
    }

    private static void PostPhoto(DiscordClient client, File[] fichiers, long idchannel, String message) {
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
        File source = new File(pathSource + name);
        File destination = new File(pathCopy + name);
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



