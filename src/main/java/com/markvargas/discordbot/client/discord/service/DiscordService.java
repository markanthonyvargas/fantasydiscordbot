package com.markvargas.discordbot.client.discord.service;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DiscordService {

  @Value("${channelId}")
  private String channelId;

  @Autowired private GatewayDiscordClient discordClient;

  public void createMessage(String message) {
    log.info("Attempting to post message to Discord channel");
    discordClient
        .getRestClient()
        .getChannelById(Snowflake.of(channelId))
        .createMessage(message)
        .block();
    log.info("Message posted successfully!");
  }
}
