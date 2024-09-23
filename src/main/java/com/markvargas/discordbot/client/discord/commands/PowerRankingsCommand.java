package com.markvargas.discordbot.client.discord.commands;

import com.markvargas.discordbot.client.yahoo.service.YahooService;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class PowerRankingsCommand implements SlashCommand {

  @Autowired private YahooService yahooService;

  @Override
  public String getName() {
    return "powerrankings";
  }

  @Override
  public Mono<Void> handle(ChatInputInteractionEvent event) {
    String username;
    if (event.getInteraction().getMember().isPresent()) {
      username = event.getInteraction().getMember().get().getDisplayName();
    } else {
      username = event.getInteraction().getUser().getUsername();
    }
    log.info("{} triggered the power rankings command", username);
    return event.reply().withEphemeral(true).withContent(yahooService.getPowerRankings());
  }
}
