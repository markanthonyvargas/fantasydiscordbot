package com.markvargas.discordbot.client.discord.commands;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import reactor.core.publisher.Mono;

public interface SlashCommand {

  String getName();

  @Retryable(backoff = @Backoff(delay = 3000))
  Mono<Void> handle(ChatInputInteractionEvent event);
}
