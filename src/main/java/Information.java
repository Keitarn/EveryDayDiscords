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

    public void classementAskFunction(DiscordClient client, long idChanel, long idGuild) {
        ResultSet res = requetes.recupClassementAsk(""+idGuild, "!ask_lamas");
        int i = 1;
        String message = "top 10 pour ask :\n";
        while(true){
            try {
                if (!res.next()) { break; }
                long idAutor = res.getLong("idUser");
                String autorName = (client.getUserById(Snowflake.of(idAutor)).block().getUsername().toString());
                message += i +") "+autorName +" avec "+ res.getInt("nombreAppel")+" demande(s) d'image\n";
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
            classementAskFunction(client, idChanel, idGuild);
        }
    }
}
