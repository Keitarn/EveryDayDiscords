import discord4j.core.DiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.MessageChannel;
import discord4j.core.object.util.Snowflake;
import net.sourceforge.argparse4j.inf.Namespace;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Information {
    private Requetes requetes;
    private Aide help;

    public Information(Requetes requetes, Aide help) {
        this.requetes = requetes;
        this.help = help;
    }

    public void classementAskFunction(DiscordClient client, long idChanel, long idGuild, boolean inters) {
        ResultSet res = null;
        int i = 1;
        String message;
        if (inters) {
            res = requetes.recupClassementAskGlobal("!ask_lamas");
            message = "top 10 pour ask interserveur :\n";
        } else {
            res = requetes.recupClassementAsk("" + idGuild, "!ask_lamas");
            String name = client.getGuildById(Snowflake.of(idGuild)).block().getName();
            message = "top 10 pour ask du serveur \"" + name + "\" :\n";
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
                classementAskFunctionPerso(client, idChanel, idGuild, event.getMessage().getAuthor().get().getId().asLong(), global);
            } else if (arg.getBoolean("cible")) {
                classementAskFunctionPerso(client, idChanel, idGuild, event.getMessage().getAuthor().get().getId().asLong(), global);
            } else {
                classementAskFunction(client, idChanel, idGuild, global);
            }
        }

    }

    private void classementAskFunctionPerso(DiscordClient client, long idChanel, long idGuild, long idAuteur, boolean inters) {
        ResultSet res = null;
        int i = 1;
        String message;
        int nbAppel = 0;
        if (inters) {
            res = requetes.recupClassementAskGlobalPerso("!ask_lamas", idAuteur);
            message = "Ton classement interserveur :\n";
        } else {
            res = requetes.recupClassementAskPerso((idGuild), "!ask_lamas", idAuteur);
            String name = client.getGuildById(Snowflake.of(idGuild)).block().getName();
            message = "Ton classement sur le serveur \"" + name + "\" :\n";
        }
        try {
            if (res.next()) {
                long idAutor = res.getLong("idUser");
                String autorName = (client.getUserById(Snowflake.of(idAutor)).block().getUsername());
                nbAppel = recupNbAppel(inters, res);
            }
        } catch (SQLException e) {
        }

        if (inters) {
            res = requetes.recupClassementAskGlobalPersoCount("!ask_lamas", nbAppel);
        } else {
            res = requetes.recupClassementAskPersoCount((idGuild), "!ask_lamas", nbAppel);
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
