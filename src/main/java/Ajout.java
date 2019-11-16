import discord4j.core.DiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.util.Snowflake;

import java.util.Iterator;
import java.util.Set;

public class Ajout {
    private Properties properties;
    private Requetes requetes;

    public Ajout(Properties properties, Requetes requetes) {
        this.properties = properties;
        this.requetes = requetes;
    }

    public void ajoutLamasAdmin(DiscordClient client, MessageCreateEvent event, boolean lamas) {
        Set<Snowflake> mentions = event.getMessage().getUserMentionIds();
        Iterator<Snowflake> it = mentions.iterator();
        Snowflake id;
        while (it.hasNext()) {
            id = it.next();
            if(lamas) {
                requetes.ajoutAdminLamas(id.asLong(),client.getUserById(id).block().getUsername(),-1L);
            }else{
                requetes.ajoutAdminLamas(id.asLong(),client.getUserById(id).block().getUsername(),event.getGuild().block().getId().asLong());
            }
        }
    }
}
