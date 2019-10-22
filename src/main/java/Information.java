import discord4j.core.DiscordClient;
import discord4j.core.object.entity.MessageChannel;
import discord4j.core.object.util.Snowflake;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Information {
    private Requetes requetes;

    public Information(Requetes requetes) {
        this.requetes = requetes;
    }

    public void classementAskFunction(DiscordClient client, long idChanel, long idGuild) {
        ResultSet res = requetes.recupClassement(""+idGuild, "!ask_lamas");
        int i = 1;
        String message = "le classement est :\n";
        while(true){
            try {
                if (!res.next()) { break; }
                long idAutor = Long.parseLong(res.getString("idUser"));
                String autorName = (client.getGuildById(Snowflake.of(idGuild))).block().getMemberById(Snowflake.of(idAutor)).block().getDisplayName().toString();
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
}
