import discord4j.core.DiscordClient;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.event.domain.channel.TextChannelDeleteEvent;
import discord4j.core.event.domain.guild.GuildCreateEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.MessageChannel;
import discord4j.core.object.util.Permission;
import discord4j.core.object.util.PermissionSet;
import discord4j.core.object.util.Snowflake;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.helper.HelpScreenException;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.internal.UnrecognizedArgumentException;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;


public class Main {
    private static HashMap<String, TreeSet<String>> messages = new HashMap<String, TreeSet<String>>();
    private static String config = System.getProperty("user.dir") + "\\config";
    private static final Map<String, Command> commands = new HashMap<>();
    private static final Map<String, Boolean> types = new HashMap<>();
    private static Properties properties;
    private static Connexion connexion = new Connexion();
    private static Requetes requetes;
    private static DiscordClient client;
    private static Aide help;
    private static Information info;
    private static PostPhoto postPhoto;
    private static Ajout ajout;
    private static Map<String, boolean[]> commandsPrivileges = new HashMap<String, boolean[]>();

    public static void main(String[] args) throws IOException {
        Loader loadSaver = new Loader(commands, messages, config);
        properties = loadSaver.LectureParam();

        connexion.connecter(TypeDatabase.MySQL, properties.getPath_BDD() + "/" + properties.getNom_BDD(), properties.getLogin_BDD(), properties.getMdp_BDD().toCharArray());
        requetes = new Requetes(connexion);

        help = new Aide(requetes);
        info = new Information(requetes);
        ajout = new Ajout(properties, requetes);
        client = new DiscordClientBuilder(properties.getToken_BOT()).build();

        types.put("photo",false);
        types.put("anime",false);
        postPhoto = new PostPhoto(commands, messages, properties, requetes, types);

        boolean[] privilege = new boolean[2];
        /*
         *
         * Commande : !ask_lamas
         * Normal : true
         * Admin : true
         *
         */

        privilege[0] = true;
        privilege[1] = true;
        commandsPrivileges.put("ask_lamas", privilege);

        ArgumentParser parserAsk = ArgumentParsers.newFor("!ask_lamas").build();

        parserAsk.addArgument("-t", "--type")
                .choices("photo","anime").setDefault("photo");

        commands.put("!ask_lamas", (event, arg) -> {
            long chanel = event.getMessage().getChannel().map(ch -> ch.getId()).block().asLong();
            if (verifMauvaise(event, chanel, commandsPrivileges.get(arg[0].replace("!",""))[0],  commandsPrivileges.get(arg[0].replace("!",""))[1],arg[0].replace("!",""))) return null;
            Namespace res = getNamespace(parserAsk, arg);
            if (res == null) return null;
            ask_lamas(client, event, res);
            return null;
        });



        /*
         *
         * Commande : !add_photo
         * Normal : true
         * Admin : true
         *
         */
        privilege[0] = true;
        privilege[1] = true;
        commandsPrivileges.put("add_photo", privilege);

        ArgumentParser parserAddPhoto = ArgumentParsers.newFor("!ask_lamas").build();

        parserAddPhoto.addArgument("-t", "--type")
                .choices("photo", "anime").setDefault("photo");


        commands.put("!add_photo", (event, arg) -> {
            long chanel = event.getMessage().getChannel().map(ch -> ch.getId()).block().asLong();
            if (!MessagePrivée(event, chanel,arg[0].replace("!",""))) return null;
            Namespace res = getNamespace(parserAddPhoto, arg);
            if (res == null) return null;
            postPhoto.addPhoto(client, event, res);
            return null;
        });

        /*
         *
         * Commande : !default_lamas
         * Normal : false
         * Admin : true
         *
         */

        privilege = new boolean[2];
        privilege[0] = false;
        privilege[1] = true;
        commandsPrivileges.put("default_lamas", privilege);

        ArgumentParser parserDefault = ArgumentParsers.newFor("!default_lamas").build();

        parserDefault.addArgument("-t", "--type")
                .choices("photo").setDefault("photo");

        commands.put("!default_lamas", (event, arg) -> {
            long chanel = event.getMessage().getChannel().map(ch -> ch.getId()).block().asLong();
            if (verifMauvaise(event, chanel, commandsPrivileges.get(arg[0].replace("!",""))[0],  commandsPrivileges.get(arg[0].replace("!",""))[1],arg[0].replace("!",""))) return null;

            Namespace res = getNamespace(parserDefault, arg);
            if (res == null) return null;

            ((MessageChannel) client.getChannelById(Snowflake.of(chanel)).block()).createMessage(messageCreateSpec -> {
                messageCreateSpec.setContent("Ce chanel a été ajouté au serveur par défault s'il ne l'était pas déja");
            }).subscribe();
            long guild = event.getGuild().map(gu -> gu.getId()).block().asLong();
            requetes.addChanelDefault(chanel, guild);
            return null;
        });

        /*
         *
         * Commande : !undefault_lamas
         * Normal : false
         * Admin : true
         *
         */

        privilege = new boolean[2];
        privilege[0] = false;
        privilege[1] = true;
        commandsPrivileges.put("undefault_lamas", privilege);

        ArgumentParser parserUndefault = ArgumentParsers.newFor("!undefault_lamas").build();
        parserUndefault.addArgument("-t", "--type")
                .choices("photo").setDefault("photo");

        commands.put("!undefault_lamas", (event, arg) -> {
            long chanel = event.getMessage().getChannel().map(ch -> ch.getId()).block().asLong();
            if (verifMauvaise(event, chanel, commandsPrivileges.get(arg[0].replace("!",""))[0],  commandsPrivileges.get(arg[0].replace("!",""))[1],arg[0].replace("!",""))) return null;

            Namespace res = getNamespace(parserUndefault, arg);
            if (res == null) return null;

            ((MessageChannel) client.getChannelById(Snowflake.of(chanel)).block()).createMessage(messageCreateSpec -> {
                messageCreateSpec.setContent("Ce chanel a été retiré des serveurs par défault s'il y était");
            }).subscribe();
            long guild = event.getGuild().map(gu -> gu.getId()).block().asLong();
            requetes.removeChanelDefault(chanel, guild);
            return null;
        });


        /*
         *
         * Commande : !help
         * Normal : true
         * Admin : true
         *
         */
        privilege = new boolean[2];
        privilege[0] = true;
        privilege[1] = true;
        commandsPrivileges.put("help", privilege);

        commands.put("!help", (event, arg) -> {
            long chanel = event.getMessage().getChannel().map(ch -> ch.getId()).block().asLong();
            if (verifMauvaise(event, chanel, commandsPrivileges.get(arg[0].replace("!",""))[0],  commandsPrivileges.get(arg[0].replace("!",""))[1], arg[0].replace("!", ""))) return null;
            help.helpFunction(client, event, arg, false);
            return null;
        });


        /*
         *
         * Commande : !addHelp
         * Normal : false
         * Admin : false
         *
         */
        privilege = new boolean[2];
        privilege[0] = false;
        privilege[1] = false;
        commandsPrivileges.put("add_help", privilege);

        commands.put("!add_help", (event, arg) -> {
            long chanel = event.getMessage().getChannel().map(ch -> ch.getId()).block().asLong();
            if (verifMauvaise(event, chanel, commandsPrivileges.get(arg[0].replace("!",""))[0],  commandsPrivileges.get(arg[0].replace("!",""))[1], arg[0].replace("!", ""))) return null;
            help.addHelp(client, event, arg);
            return null;
        });


        /*
         *
         * Commande : !load_lamas
         * Normal : false
         * Admin : false
         *
         */
        privilege = new boolean[2];
        privilege[0] = false;
        privilege[1] = false;
        commandsPrivileges.put("load_lamas", privilege);

        ArgumentParser parserLoad = ArgumentParsers.newFor("!load_lamas").build()
                .usage("\n!load_lamas  --> Met a jour toute les informations possibles en base de données\n\n" +
                        "Nécessite des droits administrateur");

        commands.put("!load_lamas", (event, arg) -> {
            long chanel = event.getMessage().getChannel().map(ch -> ch.getId()).block().asLong();
            if (verifMauvaise(event, chanel, commandsPrivileges.get(arg[0].replace("!",""))[0],  commandsPrivileges.get(arg[0].replace("!",""))[1], arg[0].replace("!", ""))) return null;
            Namespace res = getNamespace(parserLoad, arg);
            if (res == null) return null;
            postPhoto.chargeFichier();
            ((MessageChannel) client.getChannelById(Snowflake.of(chanel)).block()).createMessage(messageCreateSpec -> {
                messageCreateSpec.setContent("Load effectué");
            }).subscribe();
            return null;
        });


        /*
         *
         * Commande : !classement_lamas
         * Normal : true
         * Admin : true
         *
         */
        privilege = new boolean[2];
        privilege[0] = true;
        privilege[1] = true;
        commandsPrivileges.put("classement_lamas", privilege);

        ArgumentParser parserClassement = ArgumentParsers.newFor("!classement_lamas").build()
                .description(":La commande !classement_lamas permet une de recevoir le classement demandé")
                .usage("\n!classement_lamas --> renvoie un classement ( par defaut les photos )\n\n" +
                        "Options possible a rajouter :\n" +
                        "  -t {photo} --> recupere le type choisis parmis ceux entre accolade\n" +
                        "  -g --> permet d'obtenir le classement interserveur\n" +
                        "  -p --> permet d'avoir sa position dans le classement\n" +
                        "  -c @Personne --> permet d'avoir la position d'une personne dans le classement ( si -p et -c sont les deux present, -p sera choisit )" +
                        "\nExemple : !classement_lamas -t photo -g -p --> renvoie le classement perso interserveur pour les photos");
        parserClassement.addArgument("-t", "--type")
                .choices("photo", "test").setDefault("photo");
        parserClassement.addArgument("-g", "--global").action(Arguments.storeConst()).setConst(true)
                .setDefault(false);
        parserClassement.addArgument("-p", "--perso").action(Arguments.storeConst()).setConst(true)
                .setDefault(false);
        parserClassement.addArgument("-c", "--cible").nargs(1)
                .setConst("")
                .setDefault("");
        parserClassement.addArgument("-r", "--recu").action(Arguments.storeConst()).setConst(true)
                .setDefault(false);



        commands.put("!classement_lamas", (event, arg) -> {
            long chanel = event.getMessage().getChannel().map(ch -> ch.getId()).block().asLong();
            if (verifMauvaise(event, chanel, commandsPrivileges.get(arg[0].replace("!",""))[0],  commandsPrivileges.get(arg[0].replace("!",""))[1], arg[0].replace("!", ""))) return null;
            Namespace res = getNamespace(parserClassement, arg);
            if (res == null) return null;
            info.classement(client, event, res);
            return null;
        });


        /*
         *
         * Commande : !lamas
         * Normal : false
         * Admin : false
         *
         */
        privilege = new boolean[2];
        privilege[0] = false;
        privilege[1] = false;
        commandsPrivileges.put("lamas", privilege);
        ArgumentParser parserLamas = ArgumentParsers.newFor("!lamas").build()
                .usage("\n!lamas -c @personne --> permet d'ajouter des admin interserveur\n\n" +
                        "Nécessite des droits administrateur interserveur");

        parserLamas.addArgument("-c", "--cible").nargs("?")
                .setConst("")
                .setDefault("");

        commands.put("!lamas", (event, arg) -> {
            long chanel = event.getMessage().getChannel().map(ch -> ch.getId()).block().asLong();
            if (verifMauvaise(event, chanel, commandsPrivileges.get(arg[0].replace("!",""))[0],  commandsPrivileges.get(arg[0].replace("!",""))[1], arg[0].replace("!", ""))) return null;
            Namespace res = null;
            res = getNamespace(parserLamas, arg);
            if (res == null) return null;

            ajout.ajoutLamasAdmin(client, event, true);
            ((MessageChannel) client.getChannelById(Snowflake.of(chanel)).block()).createMessage(messageCreateSpec -> {
                messageCreateSpec.setContent("Ajout en admin interserveur réalisé");
            }).subscribe();
            return null;
        });


        /*
         *
         * Commande : !admin
         * Normal : false
         * Admin : true
         *
         */
        privilege = new boolean[2];
        privilege[0] = false;
        privilege[1] = true;
        commandsPrivileges.put("admin", privilege);

        ArgumentParser parserAdmin = ArgumentParsers.newFor("!admin").build()
                .usage("\n!admin -c @personne --> permet d'ajouter des admin serveur\n\n" +
                        "Nécessite des droits administrateur serveur ou le droit de gerer le serveur");
        parserAdmin.addArgument("-c", "--cible").nargs("?")
                .setConst("")
                .setDefault("");
        commands.put("!admin", (event, arg) -> {
            long chanel = event.getMessage().getChannel().map(ch -> ch.getId()).block().asLong();
            if (verifMauvaise(event, chanel, commandsPrivileges.get(arg[0].replace("!",""))[0],  commandsPrivileges.get(arg[0].replace("!",""))[1], arg[0].replace("!", ""))) return null;
            Namespace res = null;
            res = getNamespace(parserAdmin, arg);
            if (res == null) return null;

            ajout.ajoutLamasAdmin(client, event, false);
            ((MessageChannel) client.getChannelById(Snowflake.of(chanel)).block()).createMessage(messageCreateSpec -> {
                messageCreateSpec.setContent("Ajout en admin serveur réalisé");
            }).subscribe();
            return null;
        });

        help.setCommands(commands);
        help.setPrivilege(commandsPrivileges);

        verifChannelDelete();
        postPhoto.lanceTache(client);
        dispatcher(client);
        client.login().block();
    }

    private static boolean verifMauvaise(MessageCreateEvent event, long chanel, boolean normal, boolean admin, String commandName) {
        if (MessagePrivée(event, chanel,commandName)) return true;
        if (verifPermissionSend(client, event)) return true;
        if (normal) {
            return false;
        }else if (admin){
            return !verifAdmin(event, false);
        }else{
            return !verifAdmin(event, true);
        }
    }


    private static boolean verifAdmin(MessageCreateEvent event, boolean lamas) {
        long authorID = event.getMessage().getAuthor().get().getId().asLong();
        long idGuild;
        if (!lamas) {
            idGuild = event.getGuildId().get().asLong();
        } else {
            idGuild = -1;
        }
        final ResultSet res = requetes.droitAdmin(authorID, idGuild);
        int result = -1;
        try {
            while (res.next()) {
                result = res.getInt("result");
            }
        } catch (SQLException e) {
            return false;
        }

        if (result > 0) {
            return true;
        }

        ((MessageChannel) client.getChannelById(event.getMessage().getChannel().block().getId()).block()).createMessage(messageCreateSpec -> {
            messageCreateSpec.setContent((("Vous ne possédez pas des droits suffisants pour éffectuer cette action")));
        }).subscribe();
        return false;
    }

    private static Namespace getNamespace(ArgumentParser parserAsk, String[] arg) {
        Namespace res = null;
        try {
            res = parserAsk.parseArgs(Arrays.copyOfRange(arg, 1, arg.length));
        } catch (UnrecognizedArgumentException e) {
            System.out.print(e);
        } catch (HelpScreenException e) {

        } catch (ArgumentParserException e) {

        }
        return res;
    }

    private static void ask_lamas(DiscordClient client, MessageCreateEvent event, Namespace arg) {
        long idChanel = event.getMessage().getChannel().map(ch -> ch.getId()).block().asLong();

        String autor = event.getMessage().getAuthor().get().getUsername();
        long autorid = event.getMessage().getAuthor().get().getId().asLong();
        long idGuild = event.getMessage().getGuild().map(gu -> gu.getId()).block().asLong();
        postPhoto.postPhoto(client, idChanel, idGuild, autorid, autor,arg.getString("type"), "Voila une photo pour toi " + autor + ", j'espere qu'elle te plaira !");
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

    private static boolean MessagePrivée(MessageCreateEvent event, long idChanel, String param) {
        if (!event.getMessage().getGuild().hasElement().block() && !param.equals("add_photo")) {
            ((MessageChannel) client.getChannelById(Snowflake.of(idChanel)).block()).createMessage(messageCreateSpec -> {
                messageCreateSpec.setContent("La seul commande autorisé en message privé est \"!add_photo\"");
            }).subscribe();
            return true;
        }
        return false;
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
        client.getEventDispatcher().on(GuildCreateEvent.class).subscribe(event -> {
            long owner = event.getGuild().getOwnerId().asLong();
            String pseudo = event.getGuild().getOwner().block().getUsername();
            long guildId = event.getGuild().getId().asLong();
            ResultSet result = requetes.droitAdmin(owner, guildId);
            try {
                while ( result.next() )
                {
                    if (result.getInt("result") == 0) {
                        requetes.addAdminServer(owner, pseudo, guildId);

                        String text = "Tu as été ajouté au admin du serveur " + event.getGuild().getName()+"\n utilise !help dans un canal pour connaitre les différentes commandes";
                        ((MessageChannel) client.getChannelById(event.getGuild().getOwner().block().getPrivateChannel().block().getId()).block()).createMessage(messageCreateSpec -> {
                            messageCreateSpec.setContent(text);
                        }).subscribe();
                    }

                }

            }catch(SQLException e){
                    e.printStackTrace();
                }
            });
        }

        private static boolean verifPermissionSend (DiscordClient client, MessageCreateEvent event){
            long idGuild = event.getGuild().block().getId().asLong();
            long idchannel = event.getMessage().getChannel().block().getId().asLong();
            PermissionSet permission = client.getGuildById(Snowflake.of(idGuild)).block().getChannelById(Snowflake.of(idchannel)).block().getEffectivePermissions(client.getSelfId().get()).block();
            if (!(permission.contains(Permission.SEND_MESSAGES))) {
                return true;
            }
            return false;
        }
    }



