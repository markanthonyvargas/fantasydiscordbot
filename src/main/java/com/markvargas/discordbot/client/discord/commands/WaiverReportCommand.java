package com.markvargas.discordbot.client.discord.commands;

import com.markvargas.discordbot.client.yahoo.service.YahooService;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.support.RetrySynchronizationManager;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class WaiverReportCommand implements SlashCommand {

  @Autowired private YahooService yahooService;

  @Override
  public String getName() {
    return "waiverreport";
  }

  @Override
  public Mono<Void> handle(ChatInputInteractionEvent event) {
    log.info("Retry number: {}", RetrySynchronizationManager.getContext().getRetryCount());
    try {
      String username;
      if (event.getInteraction().getMember().isPresent()) {
        username = event.getInteraction().getMember().get().getDisplayName();
      } else {
        username = event.getInteraction().getUser().getUsername();
      }
      log.info("{} triggered the waiver report command", username);
      return event.reply().withEphemeral(true).withContent(yahooService.getWaiverTransactions());
    } catch (Exception e) {
      log.warn("Error while attempting to respond to slash command, will retry");
      throw new RuntimeException();
    }
  }
}
