import discord4j.core.DiscordClient;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.MessageChannel;
import discord4j.core.object.util.Snowflake;
import reactor.core.publisher.Mono;

import java.io.*;
import java.util.*;

public class Main {

    //A passer en fichier
    private static final String pathSource = "D:\\Documents\\GitHub\\Test\\Image1\\";
    private static final String pathCopy = "D:\\Documents\\GitHub\\Test\\Image2\\";
    private static final String pathConfig = "D:\\Documents\\GitHub\\Test\\config\\";
    private static final String pathImage = "D:\\Mes images\\everyday ltk";

    private static TreeSet<Long> default_channel = new TreeSet<Long>();
    private static HashMap<String, TreeSet<String>> messages = new HashMap<String, TreeSet<String>>();
    private static HashMap<String, String> path = new HashMap<String, String>();
    private static final Map<String, Command> commands = new HashMap<>();

    public static void main(String[] args) throws IOException {
        path.putIfAbsent("pathConfig","D:\\Documents\\GitHub\\Test\\config\\");

        SaveLoader loadSaver = new SaveLoader(commands, messages, path,default_channel);
        loadSaver.LectureParam(false);
        PostPhoto postPhoto = new PostPhoto(commands, messages, path,default_channel);
        DiscordClient client = new DiscordClientBuilder("NjMxNzg1MDM2NjE3NzQ0Mzg0.XZ77wg.H8w6DcwljnmSLgOIRqf2YFGh4mg").build();

        commands.put("!save", event -> {
            try {
                loadSaver.LectureParam(true);
            } catch (IOException e) {
                System.out.println("erreur lors de la sauvegarde");
            }
            return null;
        });

        commands.put("!ask_lamas", event -> {
            long channel = event.getMessage().getChannel().map(ch -> ch.getId()).block().asLong();
            String autor = event.getMessage().getAuthor().get().getUsername();
            postPhoto.postPhoto(client, channel, "Voila une photo pour toi " + autor + ", j'espere qu'elle te plaira !");
            return null;
        });

        dispatcher(client);
//        addDefault_channel(client);
//        removeDefault_channel(client);
//        ask(client);
        add(client);
        help(client);
//        taskPost(client, fichiers);

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



