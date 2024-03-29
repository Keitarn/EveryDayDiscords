import discord4j.core.event.domain.guild.MemberJoinEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.core.publisher.Mono;

interface Command {
    Mono<Void> execute(MessageCreateEvent event, String[] arg);
}
