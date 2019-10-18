import discord4j.core.DiscordClient;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.MessageChannel;
import discord4j.core.object.util.Snowflake;
import reactor.core.publisher.Mono;

import java.io.*;
import java.util.*;

public class Main {
    private static HashMap<String, TreeSet<String>> messages = new HashMap<String, TreeSet<String>>();
    private static String config = System.getProperty("user.dir") + "\\config";
    private static final Map<String, Command> commands = new HashMap<>();
    private static Properties properties;
    private static Connexion connexion = new Connexion();
    private static Requetes requetes;

    public static void main(String[] args) throws IOException {
        Loader loadSaver = new Loader(commands, messages, config);
        properties = loadSaver.LectureParam();

        connexion.connecter(TypeDatabase.MySQL , properties.getPath_BDD()+"/"+properties.getNom_BDD(), properties.getLogin_BDD() , properties.getMdp_BDD().toCharArray());
        requetes = new Requetes(connexion);

        PostPhoto postPhoto = new PostPhoto(commands, messages, properties, requetes);
        Aide help = new Aide(messages);

        DiscordClient client = new DiscordClientBuilder(properties.getToken_BOT()).build();
        commands.put("!ask_lamas", (event,arg) -> {
            long idChanel = event.getMessage().getChannel().map(ch -> ch.getId()).block().asLong();
            String autor = event.getMessage().getAuthor().get().getUsername();
            long autorid = event.getMessage().getAuthor().get().getId().asLong();

            long idGuild = event.getMessage().getGuild().map(gu -> gu.getId()).block().asLong();
            postPhoto.postPhoto(client, idChanel, idGuild, autorid, autor, "Voila une photo pour toi " + autor + ", j'espere qu'elle te plaira !");
            return null;
        });


        commands.put("!default_lamas", (event,arg) -> {
            long chanel = event.getMessage().getChannel().map(ch -> ch.getId()).block().asLong();
            ((MessageChannel) client.getChannelById(Snowflake.of(chanel)).block()).createMessage(messageCreateSpec -> {
                messageCreateSpec.setContent("Ce chanel a été ajouté au serveur par défault s'il ne l'était pas déja");
            }).subscribe();
            long guild = event.getGuild().map(gu -> gu.getId()).block().asLong();
            requetes.addChanelDefault(chanel,guild);
            return null;
        });


        commands.put("!undefault_lamas", (event,arg) -> {
            long chanel = event.getMessage().getChannel().map(ch -> ch.getId()).block().asLong();
            ((MessageChannel) client.getChannelById(Snowflake.of(chanel)).block()).createMessage(messageCreateSpec -> {
                messageCreateSpec.setContent("Ce chanel a été retiré des serveurs par défault s'il y était");
            }).subscribe();;
            long guild = event.getGuild().map(gu -> gu.getId()).block().asLong();
            requetes.removeChanelDefault(chanel,guild);
            return null;
        });

//        commands.put("!help", (event,arg) -> {
//            help.helpFunction(client, event.getMessage().getChannel().map(ch -> ch.getId()).block().asLong(), arg);
//            return null;
//        });



//        add(client);
        postPhoto.lanceTache(client);
        dispatcher(client);
        client.login().block();
    }

    private static void dispatcher(DiscordClient client) {
        client.getEventDispatcher().on(MessageCreateEvent.class).flatMap(event -> Mono.justOrEmpty(event.getMessage().getContent())
                .filter(content -> content.startsWith("!"))
                .map(content -> content.split(" ", 2))
                .filter(strs -> commands.containsKey(strs[0]))
                .flatMap(argsCommand -> { if(argsCommand.length > 1) {
                    commands.get(argsCommand[0]).execute(event,argsCommand[1]);
                }else {
                    commands.get(argsCommand[0]).execute(event, "");

                }
                    return Mono.empty().then();
                }))
                .subscribe();
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



