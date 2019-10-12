import discord4j.core.DiscordClient;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.MessageChannel;
import discord4j.core.object.util.Snowflake;

import java.io.*;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class Main {

    private static final String pathSource = "D:\\Documents\\GitHub\\Test\\Image1\\";
    private static final String pathCopy = "D:\\Documents\\GitHub\\Test\\Image2\\";
    private static final String pathConfig = "D:\\Documents\\GitHub\\Test\\config\\";
    private static final String pathImage = "D:\\Mes images\\everyday ltk";
    private static int i = 0;
    private static File[] fichiers;
    private static TreeSet<Long> default_channel = new TreeSet<Long>();
    private static HashMap<String,TreeSet<String>> messages = new HashMap<String,TreeSet<String>>();

    public static void main(String[] args) throws IOException {

        LectureParam(false);
        DiscordClient client = new DiscordClientBuilder("NjMxNzg1MDM2NjE3NzQ0Mzg0.XZ77wg.H8w6DcwljnmSLgOIRqf2YFGh4mg").build();

        addDefault_channel(client);
        removeDefault_channel(client);
        save(client);
        ask(client);
        add(client);
        help(client);
        fichiers = chargeFichier();
        taskPost(client, fichiers);

        client.login().block();
    }

    private static void LectureParam(boolean save) throws IOException {
        FileReader input = new FileReader(pathConfig+"config.txt");
        BufferedReader bufRead = new BufferedReader(input);
        String myLine = null;

        while ( (myLine = bufRead.readLine()) != null)
        {
            String[] array = myLine.split(":");
            // check to make sure you have valid data
            array[0] = array[0].replaceAll(" ", "");
            array[1] = array[1].replaceAll(" ", "");
            switch (array[0]){
                case "Message" :
                    if(!save){
                        mapMessage(array[1]);
                    }
                    break;
                case "Serveur" :
                    if(!save){
                        saveChanel(array[1]);
                    } else {
                        recupChanel(array[1]);
                    }
                    break;
            }
        }
    }

    private static void saveMessage(String fileName){
        final File fichier =new File(pathConfig+fileName);
        try {

            fichier .createNewFile();

            final FileWriter writer = new FileWriter(fichier);
            try {
                for(Map.Entry commandes : messages.entrySet()){
                    for ( String messages : (TreeSet<String>)commandes.getValue()) {
                        writer.write(commandes.getKey().toString()+" : \""+messages+"\"\n");
                    }
                }


            } finally {
                writer.close();
            }
        } catch (Exception e) {
            System.out.println("Impossible de creer le fichier");
        }
    }

    private static void saveChanel(String fileName){
        final File fichier =new File(pathConfig+fileName);
        try {

            fichier .createNewFile();

            final FileWriter writer = new FileWriter(fichier);
            try {
                for ( Long elem : default_channel) {
                    writer.write(elem.toString()+"\n");
                }

            } finally {
                // quoiqu'il arrive, on ferme le fichier
                writer.close();
            }
        } catch (Exception e) {
            System.out.println("Impossible de creer le fichier");
        }
    }

    private static void mapMessage(String fileName) throws IOException {
        final File fichier =new File(pathConfig+fileName);
        fichier .createNewFile();
        FileReader input = new FileReader(pathConfig+fileName);
        BufferedReader bufRead = new BufferedReader(input);
        String myLine = null;

        while ( (myLine = bufRead.readLine()) != null)
        {
            String[] array = myLine.split(":");
            array[0] = array[0].replaceAll(" ", "");
            array[1] = array[1].replaceAll(" \"", "");
            array[1] = array[1].replaceAll("\"", "");
            messages.putIfAbsent(array[0],new TreeSet<String>());
            messages.get(array[0]).add(array[1]);
        }
    }

    private static void recupChanel(String fileName) throws IOException {
        final File fichier =new File(pathConfig+fileName);
        fichier .createNewFile();
        FileReader input = new FileReader(pathConfig+fileName);
        BufferedReader bufRead = new BufferedReader(input);
        String myLine = null;

        while ( (myLine = bufRead.readLine()) != null)
        {
            default_channel.add(new Long(myLine));
        }
    }

    private static void ask(DiscordClient client){
        client.getEventDispatcher().on(MessageCreateEvent.class).subscribe(event -> event.getMessage().getContent().ifPresent(c -> {
            if(c.equals("!ask_lamas")){
                long channel = event.getMessage().getChannel().map(ch -> ch.getId()).block().asLong();
                String autor = event.getMessage().getAuthor().get().getUsername();
                PostPhoto(client,fichiers,channel,"Voila une photo pour toi "+autor+", j'espere qu'elle te plaira !");
            }
        }));
    }

    private static void addDefault_channel(DiscordClient client){
        client.getEventDispatcher().on(MessageCreateEvent.class).subscribe(event -> event.getMessage().getContent().ifPresent(c -> {
            if(c.equals("!default_lamas")){
                long chanel = event.getMessage().getChannel().map(ch -> ch.getId()).block().asLong();
                ((MessageChannel) client.getChannelById(Snowflake.of(chanel)).block()).createMessage(messageCreateSpec -> {
                    messageCreateSpec.setContent("Ce chanel a été ajouté au serveur par défault s'il ne l'était pas déja");
                }).subscribe();
                default_channel.add(chanel);
            }
        }));
    }

    private static void removeDefault_channel(DiscordClient client){
        client.getEventDispatcher().on(MessageCreateEvent.class).subscribe(event -> event.getMessage().getContent().ifPresent(c -> {
            if(c.equals("!undefault_lamas")){
                default_channel.remove(event.getMessage().getChannel().map(ch -> ch.getId()).block().asLong());
            }
        }));
    }

    private static void add(DiscordClient client){
        client.getEventDispatcher().on(MessageCreateEvent.class).subscribe(event -> event.getMessage().getContent().ifPresent(c -> {
            String[] res = c.split(" ",2);
            if(res[0].equals("!add")){
                if(res.length > 1){
                    String[] res2 = res[1].split(" ",2);
                } else {
                    long chanel = event.getMessage().getChannel().map(ch -> ch.getId()).block().asLong();
                    ((MessageChannel) client.getChannelById(Snowflake.of(chanel)).block()).createMessage(messageCreateSpec -> {
                        messageCreateSpec.setContent("add a besoin d'un argument, pour plus d'information utilise !help add");
                    }).subscribe();
                }
            }
        }));
    }

    private static void save(DiscordClient client){
        client.getEventDispatcher().on(MessageCreateEvent.class).subscribe(event -> event.getMessage().getContent().ifPresent(c -> {
            if(c.equals("!save")){
                try {
                    LectureParam(true);
                } catch (IOException e) {
                    System.out.println("erreur lors de la sauvegarde");
                }
            }
        }));
    }

    private static void help(DiscordClient client){
        client.getEventDispatcher().on(MessageCreateEvent.class).subscribe(event -> event.getMessage().getContent().ifPresent(c -> {
            if(c.equals("!help")){
                String message = "";
                TreeSet<String> messagesCommande = messages.get("!help");
                for(String elem : messagesCommande){
                    message = message+elem+"\n";
                }
                final String messEnvoie = message;
                long chanel = event.getMessage().getChannel().map(ch -> ch.getId()).block().asLong();
                ((MessageChannel) client.getChannelById(Snowflake.of(chanel)).block()).createMessage(messageCreateSpec -> {
                    messageCreateSpec.setContent(messEnvoie);
                }).subscribe();
            }
        }));
    }

    private static File[] chargeFichier(){
        File repertoire = new File(pathSource);
        File[] listFichier = repertoire.listFiles(new FileFilter() {
            public boolean accept(File pathname) {
                return pathname.isFile();
            }
        });
        return listFichier;
    }

    private static void taskPost(DiscordClient client, File[] fichiers){

        TimerTask tache = new TimerTask() {
            @Override
            public void run() {

                for (long chanel :default_channel                     ) {
                    PostPhoto(client,fichiers,chanel,"Bonne photo journalière");
                }
            }
        };
//        LocalDateTime date = LocalDateTime.now();
//        date.plusDays(1);
//        date.minusHours(date.getHour());
//        date.minusMinutes(date.getMinute());
//        date.minusSeconds(date.getSecond() - 1);
//        ZoneId zone = ZoneId.of("Europe/Paris");
//        ZoneOffset zoneOffSet = zone.getRules().getOffset(date);
        Date tomorrow = Date.from(LocalDate.now().plus(1, ChronoUnit.DAYS).atStartOfDay(ZoneId.systemDefault()).toInstant());
        Timer timer = new Timer();
//        Date dateD = Date.from(date.toLocalDate().atTime(LocalTime.now()).toInstant(zoneOffSet));
        timer.schedule(tache,tomorrow,TimeUnit.DAYS.toMillis(1));
    }

    private static void PostPhoto(DiscordClient client, File[] fichiers,long idchannel, String message){
        if(idchannel == 0){
            return;
        }
        if(fichiers.length <= i ){
            fichiers = chargeFichier();
            if(fichiers.length == 0){
                //ouvrir un fichier attention et l'affiché sans le deplacer
                ((MessageChannel) client.getChannelById(Snowflake.of(idchannel)).block()).createMessage(messageCreateSpec -> {
                    messageCreateSpec.setContent("```diff\n- Erreur lors de l'envoie de la photo, le dossier d'image est vide, pensez a remettre de nouvelles images d'ici la prochaine demande ou image journalière\n```");
                }).subscribe();
                return;
            }
        }
        String name = fichiers[i].getName();
        i++;
        File source = new File(pathSource+name);
        File destination = new File(pathCopy+name);
        source.renameTo(destination);
        try {
            FileInputStream test3 = new FileInputStream(destination);
            ((MessageChannel) client.getChannelById(Snowflake.of(idchannel)).block()).createMessage(messageCreateSpec -> {
                messageCreateSpec.setContent(message); messageCreateSpec.addFile(name, test3);
            }).subscribe(
                    t -> {
                        try {
                            test3.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
            );
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }


}



