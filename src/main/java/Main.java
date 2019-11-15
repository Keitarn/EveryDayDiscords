import discord4j.core.DiscordClient;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.event.domain.channel.TextChannelDeleteEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.MessageChannel;
import discord4j.core.object.util.Permission;
import discord4j.core.object.util.PermissionSet;
import discord4j.core.object.util.Snowflake;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import reactor.core.publisher.Mono;

import java.io.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;


public class Main {
    private static HashMap<String, TreeSet<String>> messages = new HashMap<String, TreeSet<String>>();
    private static String config = System.getProperty("user.dir") + "\\config";
    private static final Map<String, Command> commands = new HashMap<>();
    private static Properties properties;
    private static Connexion connexion = new Connexion();
    private static Requetes requetes;
    private static DiscordClient client;
    private static Aide help;
    private static Information info;
    private static PostPhoto postPhoto;

    public static void main(String[] args) throws IOException {
        Loader loadSaver = new Loader(commands, messages, config);
        properties = loadSaver.LectureParam();

        connexion.connecter(TypeDatabase.MySQL, properties.getPath_BDD() + "/" + properties.getNom_BDD(), properties.getLogin_BDD(), properties.getMdp_BDD().toCharArray());
        requetes = new Requetes(connexion);

        postPhoto = new PostPhoto(commands, messages, properties, requetes);
        help = new Aide(requetes);
        info = new Information(requetes, help);
        client = new DiscordClientBuilder(properties.getToken_BOT()).build();

        ArgumentParser parserAsk = ArgumentParsers.newFor("!ask_lamas").build()
                .usage("\n!ask_lamas  --> renvoie un message ( par defaut avec une image)\n\n" +
                        "Options possibles a rajouter :\n" +
                        "  -t {photo} --> recupere le type choisis parmis ceux entre accolade\n" +
                        "\nExemple : !ask_lamas -t photo --> renvoie une image");

        parserAsk.addArgument("-t", "--type")
                .choices("photo","test").setDefault("photo");

        commands.put("!ask_lamas", (event, arg) -> {

            long chanel = event.getMessage().getChannel().map(ch -> ch.getId()).block().asLong();
            if (MessagePrivée(event, chanel)) return null;
            if (verifPermissionSend(client, event)) return null;
            Namespace res = null;
            res = getNamespace(parserAsk, arg, chanel);
            if(res == null) return null;
            ask_lamas(client, event, res);
            return null;
        });

        ArgumentParser parserDefault = ArgumentParsers.newFor("!default_lamas").build()
                .usage("\n!default_lamas  --> abonne le canal pour un type de publication journalier ( par defaut les photos )\n\n" +
                        "Options possible a rajouter :\n" +
                        "  -t {photo} --> recupere le type choisis parmis ceux entre accolade\n" +
                        "\nExemple : !default_lamas -t photo");
        parserDefault.addArgument("-t", "--type")
                .choices("photo").setDefault("photo");

        commands.put("!default_lamas", (event, arg) -> {
            long chanel = event.getMessage().getChannel().map(ch -> ch.getId()).block().asLong();

            if (MessagePrivée(event, chanel)) return null;
            if (verifPermissionSend(client, event)) return null;
            Namespace res = null;
            res = getNamespace(parserDefault, arg, chanel);
            if(res == null) return null;

            ((MessageChannel) client.getChannelById(Snowflake.of(chanel)).block()).createMessage(messageCreateSpec -> {
                messageCreateSpec.setContent("Ce chanel a été ajouté au serveur par défault s'il ne l'était pas déja");
            }).subscribe();
            long guild = event.getGuild().map(gu -> gu.getId()).block().asLong();
            requetes.addChanelDefault(chanel, guild);
            return null;
        });

        ArgumentParser parserUndefault = ArgumentParsers.newFor("!undefault_lamas").build()
                .usage("\n!undefault_lamas  --> désabonne le canal pour un type de publication journalier ( par defaut les photos )\n\n" +
                        "Options possible a rajouter :\n" +
                        "  -t {photo} --> recupere le type choisis parmis ceux entre accolade\n" +
                        "\nExemple : !undefault_lamas -t photo");
        parserUndefault.addArgument("-t", "--type")
                .choices("photo").setDefault("photo");

        commands.put("!undefault_lamas", (event, arg) -> {
            long chanel = event.getMessage().getChannel().map(ch -> ch.getId()).block().asLong();
            if (MessagePrivée(event, chanel)) return null;
            if (verifPermissionSend(client, event)) return null;
            Namespace res = null;
            res = getNamespace(parserUndefault, arg, chanel);
            if(res == null) return null;

            ((MessageChannel) client.getChannelById(Snowflake.of(chanel)).block()).createMessage(messageCreateSpec -> {
                messageCreateSpec.setContent("Ce chanel a été retiré des serveurs par défault s'il y était");
            }).subscribe();
            long guild = event.getGuild().map(gu -> gu.getId()).block().asLong();
            requetes.removeChanelDefault(chanel, guild);
            return null;
        });

//        commands.put("!help", (event,arg) -> {
//            help.helpFunction(client, event.getMessage().getChannel().map(ch -> ch.getId()).block().asLong(), arg);
//            return null;
//        });

        ArgumentParser parserLoad = ArgumentParsers.newFor("!load_lamas").build()
                .usage("\n!load_lamas  --> Met a jour toute les informations possibles en base de données\n\n" +
                        "Nécessite des droits administrateur");

        commands.put("!load_lamas", (event, arg) -> {
            long chanel = event.getMessage().getChannel().map(ch -> ch.getId()).block().asLong();

            if (MessagePrivée(event, chanel)) return null;
            if (verifPermissionSend(client, event)) return null;
            if(!verifAdmin(arg)) return null;
            Namespace res = null;
            res = getNamespace(parserLoad, arg, chanel);
            if(res == null) return null;
            postPhoto.chargeFichier();
            ((MessageChannel) client.getChannelById(Snowflake.of(chanel)).block()).createMessage(messageCreateSpec -> {
                messageCreateSpec.setContent("Load effectué");
            }).subscribe();
            return null;
        });

        ArgumentParser parserClassement = ArgumentParsers.newFor("!classement_lamas").build()
                        .description(":La commande !classement_lamas permet une de recevoir le classement demandé")
                        .usage("\n!classement_lamas --> renvoie un classement ( par defaut les photos )\n\n" +
                                "Options possible a rajouter :\n" +
                                "  -t {photo} --> recupere le type choisis parmis ceux entre accolade\n" +
                                "  -g --> permet d'obtenir le classement interveur\n" +
                                "  -p --> permet d'avoir sa position dans le classement\n" +
                                "  -c @Personne --> permet d'avoir la position d'une personne dans le classement ( si -p et -c sont les deux present, -p sera choisit )" +
                                "\nExemple : !classement_lamas -t photo -g -p --> renvoie le classement perso interserveur pour les photos");
        parserClassement.addArgument("-t", "--type")
                .choices("photo","test").setDefault("photo");
        parserClassement.addArgument("-g", "--global").action(Arguments.storeConst()).setConst(true)
                .setDefault(false);
        parserClassement.addArgument("-p", "--perso").action(Arguments.storeConst()).setConst(true)
                .setDefault(false);
        parserClassement.addArgument("-c", "--cible").nargs(1)
                .setDefault("");
        parserClassement.addArgument("-r", "--recu").action(Arguments.storeConst()).setConst(true)
                .setDefault(false);

        commands.put("!classement_lamas", (event, arg) -> {
            long chanel = event.getMessage().getChannel().map(ch -> ch.getId()).block().asLong();
            if (MessagePrivée(event, chanel)) return null;
            if (verifPermissionSend(client, event)) return null;
            Namespace res = null;
            res = getNamespace(parserClassement, arg, chanel);
            if(res == null) return null;
            info.classement(client, event, res);
            return null;
        });

        verifChannelDelete();
        postPhoto.lanceTache(client);
        dispatcher(client);
        client.login().block();
    }

    private static boolean verifAdmin(String[] arg) {
        return true;
    }

    private static Namespace getNamespace(ArgumentParser parserAsk, String[] arg, long chanel) {
        Namespace res = null;
        try {
            res = parserAsk.parseArgs(Arrays.copyOfRange(arg, 1, arg.length));
        } catch (ArgumentParserException e) {
            String rep = e.getParser().formatUsage();


        ((MessageChannel) client.getChannelById(Snowflake.of(chanel)).block()).createMessage(messageCreateSpec -> {
                messageCreateSpec.setContent(((rep)));
            }).subscribe();
        }
        return res;
    }

    private static void ask_lamas(DiscordClient client, MessageCreateEvent event, Namespace arg) {
        if(arg.getString("type").equals("photo")){
            long idChanel = event.getMessage().getChannel().map(ch -> ch.getId()).block().asLong();
            String autor = event.getMessage().getAuthor().get().getUsername();
            long autorid = event.getMessage().getAuthor().get().getId().asLong();
            long idGuild = event.getMessage().getGuild().map(gu -> gu.getId()).block().asLong();
            postPhoto.postPhoto(client, idChanel, idGuild, autorid, autor, "Voila une photo pour toi " + autor + ", j'espere qu'elle te plaira !");
        }
    }
    private static void verifChannelDelete() {
        final ResultSet res = requetes.getGuildChannel();
        try {
            while (res.next()) {
                long chanel = res.getLong("idChanel");
                long guild = res.getLong("idGuild");
                client.getChannelById(Snowflake.of(chanel)).doOnError(ch -> {
                    requetes.removeChanelDefault(chanel, guild);
                }).subscribe();
            }
        } catch (SQLException e) {
            return;
        }

        client.getEventDispatcher().on(TextChannelDeleteEvent.class).flatMap(event ->
        {
            long idGuild = event.getChannel().getGuild().block().getId().asLong();
            long idChanel = event.getChannel().getId().asLong();
            requetes.removeChanelDefault(idChanel, idGuild);
            return Mono.empty();
        }).subscribe();
    }

    private static boolean MessagePrivée(MessageCreateEvent event, long idChanel) {
        if (!event.getMessage().getGuild().hasElement().block()) {
            messagesPrivée(idChanel);
            return true;
        }
        return false;
    }

    private static void messagesPrivée(long idChanel) {

        ((MessageChannel) client.getChannelById(Snowflake.of(idChanel)).block()).createMessage(messageCreateSpec -> {
            messageCreateSpec.setContent("Il est interdit de faire des demandes au bot, si tu continues tu seras blacklisté");
        }).subscribe();
        ;
    }

    private static void dispatcher(DiscordClient client) {
        client.getEventDispatcher().on(MessageCreateEvent.class).flatMap(event -> Mono.justOrEmpty(event.getMessage().getContent())
                .filter(content -> content.startsWith("!"))
                .map(content -> content.split(" "))
                .filter(strs -> commands.containsKey(strs[0]))
                .flatMap(argsCommand -> {
                    commands.get(argsCommand[0]).execute(event, argsCommand);
                    return Mono.empty().then();
                })).subscribe();
    }

    private static boolean verifPermissionSend(DiscordClient client, MessageCreateEvent event) {
        long idGuild = event.getGuild().block().getId().asLong();
        long idchannel = event.getMessage().getChannel().block().getId().asLong();
        PermissionSet permission = client.getGuildById(Snowflake.of(idGuild)).block().getChannelById(Snowflake.of(idchannel)).block().getEffectivePermissions(client.getSelfId().get()).block();
        if(!(permission.contains(Permission.SEND_MESSAGES))){
            return true;
        }
        return false;
    }
}



