import discord4j.core.DiscordClient;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.MessageChannel;
import discord4j.core.object.util.Snowflake;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class Main {

    static final String pathSource = "D:\\Documents\\GitHub\\Test\\Image1\\";
    static final String pathCopy = "D:\\Documents\\GitHub\\Test\\Image2\\";
    static final String pathImage = "D:\\Mes images\\everyday ltk";
    static int i = 0;

    public static void main(String[] args) throws IOException {


        DiscordClient client = new DiscordClientBuilder("NjMxNzg1MDM2NjE3NzQ0Mzg0.XZ77wg.H8w6DcwljnmSLgOIRqf2YFGh4mg").build();

        client.getEventDispatcher().on(MessageCreateEvent.class).subscribe(event -> event.getMessage().getContent().ifPresent(c -> System.out.println(c))); // "subscribe" is the method you need to call to actually make sure that it's doing something.


        File repertoire = new File(pathSource);
        File[] fichiers = repertoire.listFiles(new FileFilter() {
            public boolean accept(File pathname) {
                return pathname.isFile();
            }
        });

        int size = fichiers.length;

        taskPost(client, fichiers);

        client.login().block();
    }



    public static void taskPost(DiscordClient client, File[] fichiers){

        TimerTask tache = new TimerTask() {
            @Override
            public void run() {
                String name = fichiers[i].getName();
                i++;
                File source = new File(pathSource+name);
                File destination = new File(pathCopy+name);
                source.renameTo(destination);
                try {
                    FileInputStream test3 = new FileInputStream(destination);
                    ((MessageChannel) client.getChannelById(Snowflake.of(631830909376528384L)).block()).createMessage(messageCreateSpec -> {
                        messageCreateSpec.addFile(name, test3);
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
        };
            LocalDate date = LocalDate.now();
            Timer timer = new Timer();
            Date dateD = Date.from(date.atTime(LocalTime.now()).toInstant(ZoneOffset.ofHours(2)));
        timer.schedule(tache,dateD,TimeUnit.SECONDS.toMillis(5));
        }

}



