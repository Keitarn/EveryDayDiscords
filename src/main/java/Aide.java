import discord4j.core.DiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.MessageChannel;
import discord4j.core.object.util.Snowflake;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;


public class Aide {

    private Requetes requetes;
    private Map<String, Command> commands;
    private Map<String, boolean[]> privileges = new HashMap<String, boolean[]>();

    public Aide(Requetes requetes) {
        this.requetes = requetes;

    }

    public void setCommands(Map<String, Command> commands1) {
        this.commands = commands1;
    }

    public void setPrivilege(Map<String, boolean[]> privileges1) {
        this.privileges = privileges1;

    }

    public void helpFunction(DiscordClient client, MessageCreateEvent event, String[] param, boolean error) {
        if (param.length <= 1) {
            basicHelp(client,event);
        } else {
            commandHelp(client,event,param);
        }
    }

    private void commandHelp(DiscordClient client, MessageCreateEvent event, String[] param) {
        //Est-ce que la commande existe
        if(privileges.get(param[1].replace("!",""))==null){
            final String messageFinal = "La commande que vous cherchez n'existe pas.";
            long chanelId = event.getMessage().getChannel().map(ch -> ch.getId()).block().asLong();
            ((MessageChannel) client.getChannelById(Snowflake.of(chanelId)).block()).createMessage(messageCreateSpec -> {
                messageCreateSpec.setContent(messageFinal);
            }).subscribe();
            return;
        }

        //elle existe on recup les droits de l'auteur
        long authorId = event.getMessage().getAuthorAsMember().block().getId().asLong();
        boolean droitAdmin = verifAdminPossession(event.getGuildId().get().asLong(), authorId, false);
        boolean droitSuperAdmin = verifAdminPossession(event.getGuildId().get().asLong(), authorId, true);
        boolean[] test = privileges.get(param[1].replace("!",""));
        //Il a les droits pour la commande ?
        if (test[0] || (test[1] && droitAdmin) || droitSuperAdmin) {
            //Il a les droits donc on recup l'aide et on l'envoie

            ResultSet res = requetes.getHelp(param[1].replace("!", ""));
            String result = "";
            try {
                while (res.next()) {
                    result  = res.getString("aideCommande");
                }
            } catch (SQLException e) {
                result ="Une erreur est survenue";
            }
            if(result.equals("")){
                result ="L'aide n'a pas encore été renseigné pour cette commande";
            }
            final String messageFinal = "L'aide pour la commande "+param[1]+" est : \n"+result;
            long chanelId = event.getMessage().getChannel().map(ch -> ch.getId()).block().asLong();
            ((MessageChannel) client.getChannelById(Snowflake.of(chanelId)).block()).createMessage(messageCreateSpec -> {
                messageCreateSpec.setContent(messageFinal);
            }).subscribe();
        }else{
            //Il n'a pas les droits
            final String messageFinal = "Vous ne possédez pas les droits pour voir l'aide de cette commande";
            long chanelId = event.getMessage().getChannel().map(ch -> ch.getId()).block().asLong();
            ((MessageChannel) client.getChannelById(Snowflake.of(chanelId)).block()).createMessage(messageCreateSpec -> {
                messageCreateSpec.setContent(messageFinal);
            }).subscribe();
        }


    }

    private void basicHelp(DiscordClient client, MessageCreateEvent event){
        String message = "Pour voir l'aide d'une commande en particulier, utiliser \"!help commande\" (en remplaçant \"commande\" par le nom de la commande)\nListe des commandes disponibles :\n";

        long chanelId = event.getMessage().getChannel().map(ch -> ch.getId()).block().asLong();
        long authorId = event.getMessage().getAuthorAsMember().block().getId().asLong();
        boolean droitAdmin = verifAdminPossession(event.getGuildId().get().asLong(), authorId, false);
        boolean droitSuperAdmin = verifAdminPossession(event.getGuildId().get().asLong(), authorId, true);
        for (Map.Entry mapentry : commands.entrySet()) {
            if (!mapentry.getKey().equals("!help")) {
                boolean[] test = privileges.get(mapentry.getKey().toString().replace("!",""));
                if (test[0] || (test[1] && droitAdmin) || droitSuperAdmin) {
                    message += mapentry.getKey() + "\n";
                }
            }
        }

        final String messageFinal = message;
        ((MessageChannel) client.getChannelById(Snowflake.of(chanelId)).block()).createMessage(messageCreateSpec -> {
            messageCreateSpec.setContent(messageFinal);
        }).subscribe();
    }

    private boolean verifAdminPossession(long guildId, long authorID, boolean lamas) {
        long idGuild;
        if (!lamas) {
            idGuild = guildId;
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

        return false;
    }

    public void addHelp(DiscordClient client, MessageCreateEvent event, String[] arg) {
        boolean droitSuperAdmin = verifAdminPossession(event.getGuildId().get().asLong(), event.getMessage().getAuthorAsMember().block().getId().asLong(), true);
        if(!droitSuperAdmin){
            final String messageFinal = "Vous ne possèdez pas les droits pour utilisez cette commande";
            long chanelId = event.getMessage().getChannel().map(ch -> ch.getId()).block().asLong();
            ((MessageChannel) client.getChannelById(Snowflake.of(chanelId)).block()).createMessage(messageCreateSpec -> {
                messageCreateSpec.setContent(messageFinal);
            }).subscribe();
            return;
        }
        if(arg.length < 3){
            final String messageFinal = "La commande n'est pas valide, utilisez \"!addHelp command messageAide\"";
            long chanelId = event.getMessage().getChannel().map(ch -> ch.getId()).block().asLong();
            ((MessageChannel) client.getChannelById(Snowflake.of(chanelId)).block()).createMessage(messageCreateSpec -> {
                messageCreateSpec.setContent(messageFinal);
            }).subscribe();
        }else{
            String message ="";
            for (int i = 2; i < arg.length; i++){
                message+=arg[i]+" ";
            }
            requetes.addHelp(arg[1].replace("!",""),message);
            final String messageFinal = "L'aide a été ajouté";
            long chanelId = event.getMessage().getChannel().map(ch -> ch.getId()).block().asLong();
            ((MessageChannel) client.getChannelById(Snowflake.of(chanelId)).block()).createMessage(messageCreateSpec -> {
                messageCreateSpec.setContent(messageFinal);
            }).subscribe();
        }

    }
}

