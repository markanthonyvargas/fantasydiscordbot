package com.markvargas.discordbot.client.discord.service;

import com.markvargas.discordbot.client.discord.model.DiscordMessageRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class DiscordService {

  @Autowired
  @Qualifier("discordRestTemplate")
  private RestTemplate discordRestTemplate;

  @Value("${botToken}")
  private String botToken;

  @Value("${channelId}")
  private String channelId;

  public void createMessage(String message) {
    String url = "https://discord.com/api/v10/channels/" + channelId + "/messages";
    HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.AUTHORIZATION, "Bot " + botToken);
    headers.add(HttpHeaders.USER_AGENT, "DiscordBot (fantasydiscordbot.onrender.com, 1");
    headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
    DiscordMessageRequest messageRequest = new DiscordMessageRequest(message);
    HttpEntity<DiscordMessageRequest> entity = new HttpEntity<>(messageRequest, headers);
    try {
      log.info("Attempting to post message to Discord channel");
      ResponseEntity<String> response =
          discordRestTemplate.exchange(url, HttpMethod.POST, entity, String.class);
      if (response.getStatusCode().is2xxSuccessful()) {
        log.info("Message posted successfully!");
      } else {
        log.info(
            "Message was not posted successfully to Discord, received the following response:\n"
                + response.getBody());
      }
    } catch (Exception e) {
      log.error("Error occurred while attempting to post a message to Discord channel", e);
    }
  }
}
