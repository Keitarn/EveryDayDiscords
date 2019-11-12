import com.sun.org.apache.xml.internal.utils.NameSpace;
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
        ResultSet res= null;
        int i = 1;
        String message;
        if(inters){
            res = requetes.recupClassementAskGlobal("!ask_lamas");
            message = "top 10 pour ask  interserveur :\n";
        } else {
            res = requetes.recupClassementAsk(""+idGuild, "!ask_lamas");
            String name = client.getGuildById(Snowflake.of(idGuild)).block().getName();
            message = "top 10 pour ask du serveur \""+name+"\" :\n";
        }

        while(true){
            try {
                if (!res.next()) { break; }
                long idAutor = res.getLong("idUser");
                String autorName = (client.getUserById(Snowflake.of(idAutor)).block().getUsername());
                int nbAppel;
                if(inters){
                    nbAppel = res.getInt("sum(nombreAppel)");
                }else {
                    nbAppel = res.getInt("nombreAppel");
                }
                message += i +") "+autorName +" avec "+ nbAppel +" demande(s) d'image\n";
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

        if(arg.getString("type").equals("photo")){
            long idChanel = event.getMessage().getChannel().map(ch -> ch.getId()).block().asLong();
            long idGuild = event.getMessage().getGuild().map(gu -> gu.getId()).block().asLong();

            if(arg.getBoolean("global")){
                classementAskFunction(client, idChanel, idGuild, true);

            }else {
                classementAskFunction(client, idChanel, idGuild, false);
            }
        }

    }
}
