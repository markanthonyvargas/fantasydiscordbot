package com.markvargas.discordbot.client.discord.model;

import lombok.Data;

@Data
public class DiscordMessageRequest {
  private String content;

  public DiscordMessageRequest(String content) {
    this.content = content;
  }
}
