package com.markvargas.discordbot.client.discord.service;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.retry.support.RetrySynchronizationManager;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DiscordService {

  @Value("${channelId}")
  private String channelId;

  @Value("${directMessageChannelId}")
  private String directMessageChannelId;

  @Autowired private GatewayDiscordClient discordClient;

  @Retryable(backoff = @Backoff(delay = 3000))
  public void createMessage(String message) {
    log.info("Retry number: {}", RetrySynchronizationManager.getContext().getRetryCount());
    log.info("Attempting to post message to Discord channel");
    discordClient
        .getRestClient()
        .getChannelById(Snowflake.of(channelId))
        .createMessage(message)
        .block();
    log.info("Message posted successfully!");
  }

  @Retryable(backoff = @Backoff(delay = 3000))
  public void sendMessage() {
    log.info("Retry number: {}", RetrySynchronizationManager.getContext().getRetryCount());
    log.info("Attempting to send Discord message for auth error");
    discordClient
        .getRestClient()
        .getChannelById(Snowflake.of(directMessageChannelId))
        .createMessage(
            "There was an issue while getting an auth token for Yahoo, please investigate")
        .block();
    log.info("Message sent successfully.");
  }
}
