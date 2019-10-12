import discord4j.core.DiscordClient;
import discord4j.core.object.entity.MessageChannel;
import discord4j.core.object.util.Snowflake;

public class Aide {

    public Aide() {
    }

    public void helpFunction(DiscordClient client, Long chanel){
        ((MessageChannel) client.getChannelById(Snowflake.of(chanel)).block()).createMessage(messageCreateSpec -> {
            messageCreateSpec.setContent("Test d'aide");
        }).subscribe();
    }
}

