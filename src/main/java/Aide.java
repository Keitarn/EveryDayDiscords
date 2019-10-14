import discord4j.core.DiscordClient;
import discord4j.core.object.entity.MessageChannel;
import discord4j.core.object.util.Snowflake;

import java.util.HashMap;
import java.util.TreeSet;


public class Aide {

    private HashMap<String, TreeSet<String>> messages = new HashMap<String, TreeSet<String>>();

    public Aide(HashMap<String, TreeSet<String>> messages) {
        this.messages = messages;
    }

    public void helpFunction(DiscordClient client, Long chanel, String param){
        if(param.equals("")){
            helpBasique(client,chanel);
            return;
        }else{
            String[] split = param.split(" ");
            if(split.length > 1){
                TreeSet<String> reponse = messages.get("!help_error");
                String message = "";
                for (String elem : reponse) {
                    message = message + elem + "\n";
                }
               final String rep = message;
                ((MessageChannel) client.getChannelById(Snowflake.of(chanel)).block()).createMessage(messageCreateSpec -> {
                    messageCreateSpec.setContent(rep);
                }).subscribe();
            } else{
                helpCommande(client,chanel);
            }
        }



    }

    private void helpBasique(DiscordClient client, Long chanel){
        ((MessageChannel) client.getChannelById(Snowflake.of(chanel)).block()).createMessage(messageCreateSpec -> {
            messageCreateSpec.setContent("Test d'aide");
        }).subscribe();
    }

    private void helpCommande(DiscordClient client, Long chanel){
        ((MessageChannel) client.getChannelById(Snowflake.of(chanel)).block()).createMessage(messageCreateSpec -> {
            messageCreateSpec.setContent("Test d'aide commande");
        }).subscribe();
    }


//    private static void help(DiscordClient client) {
//        client.getEventDispatcher().on(MessageCreateEvent.class).subscribe(event -> event.getMessage().getContent().ifPresent(c -> {
//            if (c.equals("!help")) {
//                String message = "";
//                TreeSet<String> messagesCommande = messages.get("!help");
//                for (String elem : messagesCommande) {
//                    message = message + elem + "\n";
//                }
//                final String messEnvoie = message;
//                long chanel = event.getMessage().getChannel().map(ch -> ch.getId()).block().asLong();
//                ((MessageChannel) client.getChannelById(Snowflake.of(chanel)).block()).createMessage(messageCreateSpec -> {
//                    messageCreateSpec.setContent(messEnvoie);
//                }).subscribe();
//            }
//        }));
//    }


}

