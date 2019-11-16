import discord4j.core.DiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.MessageChannel;
import discord4j.core.object.util.Snowflake;
import net.sourceforge.argparse4j.inf.Namespace;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Set;

public class Information {
    private Requetes requetes;
    private Aide help;

    public Information(Requetes requetes) {
        this.requetes = requetes;
        this.help = help;
    }

    public void classementAskFunction(DiscordClient client, long idChanel, long idGuild, boolean inters, String s) {
        ResultSet res = null;
        int i = 1;
        String message;
        if (inters) {
            res = requetes.recupClassementCommandGlobal(s);
            message = "top 10 pour "+s+" interserveur :\n";
        } else {
            res = requetes.recupClassementCommand("" + idGuild, s);
            String name = client.getGuildById(Snowflake.of(idGuild)).block().getName();
            message = "top 10 pour "+s+" du serveur \"" + name + "\" :\n";
        }

        while (true) {
            try {
                if (!res.next()) {
                    break;
                }
                long idAutor = res.getLong("idUser");
                String autorName = (client.getUserById(Snowflake.of(idAutor)).block().getUsername());
                int nbAppel = recupNbAppel(inters, res);
                message += i + ") " + autorName + " avec " + nbAppel + " demande(s) d'image\n";
                i++;
            } catch (SQLException e) {
                break;
            }
        }
        message += "fin du classement.";
        String MessageFinal = message;
        ((MessageChannel) client.getChannelById(Snowflake.of(idChanel)).block()).createMessage(messageCreateSpec -> {
            messageCreateSpec.setContent(MessageFinal);
        }).subscribe();
    }

    public void classement(DiscordClient client, MessageCreateEvent event, Namespace arg) {
        if (arg.getString("type").equals("photo")) {
            long idChanel = event.getMessage().getChannel().map(ch -> ch.getId()).block().asLong();
            long idGuild = event.getMessage().getGuild().map(gu -> gu.getId()).block().asLong();
            boolean global = arg.getBoolean("global");
            if (arg.getBoolean("perso")) {
                classementCommandFunctionPerso(client, idChanel, idGuild, event.getMessage().getAuthor().get().getId().asLong(), global,"!ask_lamas");
            } else if (!(arg.getString("cible").equals(""))) {
                Set<Snowflake> mentions = event.getMessage().getUserMentionIds();
                if(mentions.size()>1){
                    String MessageFinal = "Une seule mention est possible par commande de classement";
                    ((MessageChannel) client.getChannelById(Snowflake.of(idChanel)).block()).createMessage(messageCreateSpec -> {
                        messageCreateSpec.setContent(MessageFinal);
                    }).subscribe();
                    return;
                }
                Iterator<Snowflake> it = mentions.iterator();
                Snowflake id = it.next();
                classementCommandFunctionPerso(client, idChanel, idGuild,id.asLong(), global, "!ask_lamas");
            } else {
                classementAskFunction(client, idChanel, idGuild, global, "!ask_lamas");
            }
        }

    }

    private void classementCommandFunctionPerso(DiscordClient client, long idChanel, long idGuild, long idAuteur, boolean inters, String s) {
        ResultSet res = null;
        int i = 1;
        String message = "Le classement ";
        int nbAppel = 0;
        if (inters) {
            res = requetes.recupClassementCommandGlobalPerso(s, idAuteur);
            message += "interserveur de ";
        } else {
            res = requetes.recupClassementCommandPerso((idGuild), s, idAuteur);
            String name = client.getGuildById(Snowflake.of(idGuild)).block().getName();
            message += "sur le serveur \"" + name + "\" de ";
        }
        String autorName = (client.getUserById(Snowflake.of(idAuteur)).block().getUsername());
        message+= "la commande "+s+" de "+autorName+" :\n";
        try {
            if (res.next()) {

                nbAppel = recupNbAppel(inters, res);
            }
        } catch (SQLException e) {

        }

        if (inters) {
            res = requetes.recupClassementCommandGlobalPersoCount(s, nbAppel);
        } else {
            res = requetes.recupClassementCommandPersoCount((idGuild), s, nbAppel);
        }
        int count = 1;
        try {
            if (res.next()) {
                if (inters) {
                    count = res.getInt("result") + 1;
                } else {
                    count = res.getInt("result") + 1;
                }
            }
        } catch (SQLException e) {
        }
        message += "position " + count + " avec un nombre de vote de " + nbAppel;
        String MessageFinal = message;
        ((MessageChannel) client.getChannelById(Snowflake.of(idChanel)).block()).createMessage(messageCreateSpec -> {
            messageCreateSpec.setContent(MessageFinal);
        }).subscribe();
    }

    private int recupNbAppel(boolean inters, ResultSet res) throws SQLException {
        int nbAppel;
        if (inters) {
            nbAppel = res.getInt("sum(nombreAppel)");
        } else {
            nbAppel = res.getInt("nombreAppel");
        }
        return nbAppel;
    }
}
