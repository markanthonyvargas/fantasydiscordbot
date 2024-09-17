package com.markvargas.discordbot.client.discord.service;

import com.markvargas.discordbot.client.discord.model.DiscordMessageRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
@Slf4j
public class DiscordService {

  @Autowired
  @Qualifier("discordRestClient")
  private RestClient discordRestClient;

  public void createMessage(String message) {
    DiscordMessageRequest messageRequest = new DiscordMessageRequest(message);
    try {
      log.info("Attempting to post message to Discord channel");
      ResponseEntity<String> response =
          discordRestClient.post().body(messageRequest).retrieve().toEntity(String.class);
      if (response.getStatusCode().is2xxSuccessful()) {
        log.info("Message posted successfully!");
      } else {
        log.info(
            "Message was not posted successfully to Discord, received the following response: {}",
            response.getBody());
      }
    } catch (Exception e) {
      log.error("Error occurred while attempting to post a message to Discord channel", e);
    }
  }
}
