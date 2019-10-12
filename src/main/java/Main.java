import com.sun.org.apache.xerces.internal.util.SynchronizedSymbolTable;
import discord4j.core.DiscordClient;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.MessageChannel;
import discord4j.core.object.util.Snowflake;
import reactor.core.publisher.Mono;

import java.io.*;
import java.util.*;

public class Main {
    private static TreeSet<Long> default_channel = new TreeSet<Long>();
    private static HashMap<String, TreeSet<String>> messages = new HashMap<String, TreeSet<String>>();
    private static HashMap<String, String> path = new HashMap<String, String>();
    private static final Map<String, Command> commands = new HashMap<>();

    public static void main(String[] args) throws IOException {
        path.putIfAbsent("pathConfig","D:\\Documents\\GitHub\\Test\\config\\");

        SaveLoader loadSaver = new SaveLoader(commands, messages, path,default_channel);
        loadSaver.LectureParam(false);

        PostPhoto postPhoto = new PostPhoto(commands, messages, path,default_channel);
        Aide help = new Aide();

        DiscordClient client = new DiscordClientBuilder("NjMxNzg1MDM2NjE3NzQ0Mzg0.XZ77wg.H8w6DcwljnmSLgOIRqf2YFGh4mg").build();

        commands.put("!save", event -> {
            try {
                loadSaver.LectureParam(true);
                long chanel = event.getMessage().getChannel().map(ch -> ch.getId()).block().asLong();
                ((MessageChannel) client.getChannelById(Snowflake.of(chanel)).block()).createMessage(messageCreateSpec -> {
                    messageCreateSpec.setContent("Save éffectué");
                }).subscribe();
            } catch (IOException e) {
                System.out.println("erreur lors de la sauvegarde");
            }
            return null;
        });

        commands.put("!load", event -> {
            try {
                loadSaver.LectureParam(false);
                long chanel = event.getMessage().getChannel().map(ch -> ch.getId()).block().asLong();
                ((MessageChannel) client.getChannelById(Snowflake.of(chanel)).block()).createMessage(messageCreateSpec -> {
                    messageCreateSpec.setContent("Load éffectué");
                }).subscribe();
            } catch (IOException e) {
                System.out.println("erreur lors du chargement");
            }
            return null;
        });

        commands.put("!ask_lamas", event -> {
            long chanel = event.getMessage().getChannel().map(ch -> ch.getId()).block().asLong();
            String autor = event.getMessage().getAuthor().get().getUsername();
            postPhoto.postPhoto(client, chanel, "Voila une photo pour toi " + autor + ", j'espere qu'elle te plaira !");
            return null;
        });


        commands.put("!default_lamas", event -> {
            long chanel = event.getMessage().getChannel().map(ch -> ch.getId()).block().asLong();
            ((MessageChannel) client.getChannelById(Snowflake.of(chanel)).block()).createMessage(messageCreateSpec -> {
                messageCreateSpec.setContent("Ce chanel a été ajouté au serveur par défault s'il ne l'était pas déja");
            }).subscribe();
            default_channel.add(chanel);
            return null;
        });


        commands.put("!undefault_lamas", event -> {
            long chanel = event.getMessage().getChannel().map(ch -> ch.getId()).block().asLong();
            ((MessageChannel) client.getChannelById(Snowflake.of(chanel)).block()).createMessage(messageCreateSpec -> {
                messageCreateSpec.setContent("Ce chanel a été retiré des serveurs par défault s'il y était");
            }).subscribe();;
            default_channel.remove(chanel);
            return null;
        });

        commands.put("!help", event -> {
            help.helpFunction(client, event.getMessage().getChannel().map(ch -> ch.getId()).block().asLong());
            return null;
        });


        add(client);
//        help(client);
        postPhoto.lanceTache(client);
        dispatcher(client);
        client.login().block();
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

//    private static void help(DiscordClient client) {
//        client.getEventDispatcher().on(MessageCreateEvent.class).subscribe(event -> event.getMessage().getContent().ifPresent(c -> {
//            if (c.equals("!help")) {
//                String message = "";
//                TreeSet<String> messagesCommande = messages.get("!help");
//                for (String elem : messagesCommande) {
//                    message = message + elem + "\n";
//                }
//                final String messEnvoie = message;
//                long chanel = event.getMessage().getChannel().map(ch -> ch.getId()).block().asLong();
//                ((MessageChannel) client.getChannelById(Snowflake.of(chanel)).block()).createMessage(messageCreateSpec -> {
//                    messageCreateSpec.setContent(messEnvoie);
//                }).subscribe();
//            }
//        }));
//    }




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




}



